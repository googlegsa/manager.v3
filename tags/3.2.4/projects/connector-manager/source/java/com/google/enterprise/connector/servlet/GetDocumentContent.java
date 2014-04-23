// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.pusher.FeedConnection;
import com.google.enterprise.connector.pusher.XmlFeed;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentAccessException;
import com.google.enterprise.connector.spi.DocumentNotFoundException;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetDocumentContent extends HttpServlet {

  private static Logger LOGGER =
    Logger.getLogger(GetDocumentContent.class.getName());
  private static final String HDR_IF_MODIFIED = "If-Modified-Since";

  /**
   * Attribute name on the ServletRequest containing a cache of the parsed
   * query parameters.
   */
  private static final String PARAMETER_CACHE_NAME =
      GetDocumentContent.class.getName() + ".parameters";

  /**
   * Attribute name on the ServletRequest containing a cache of the metadata
   * of the requested document.
   */
  private static final String METADATA_CACHE_NAME =
      GetDocumentContent.class.getName() + ".document";
  /**
   * Distinguished attribute value denoting that the metadata is already known
   * to be unavailable.
   */
  private static final Object NEGATIVE_METADATA_CACHE_VALUE = new Object();

  /** HTTP header that contains the Document metadata. */
  private static final String EXTERNAL_METADATA_HEADER =
      "X-Gsa-External-Metadata";

  private static boolean useCompression = false;
  private static FeedConnection feedConnection;
  /**
   * GSA 7.0 introduces the ability to provide a HTTP header that specifies
   * whether the document is secure. In previous GSAs we are required to use
   * HTTP basic, which has to be configured correctly on the GSA.
   */
  private static Boolean securityHeaderSupported;

  public static void setUseCompression(boolean doCompression) {
    useCompression = doCompression;
  }

  /**
   * Set the feed connection to use to discover if the security header is
   * supported. This must be set during startup to take effect.
   */
  public static void setFeedConnection(FeedConnection fc) {
    feedConnection = fc;
  }

  private synchronized static boolean isSecurityHeaderSupported() {
    if (securityHeaderSupported != null) {
      return securityHeaderSupported;
    }

    if (feedConnection == null) {
      // FeedConnection is unavailable, so choose the pessimistic choice.
      securityHeaderSupported = false;
    } else {
      // The newer ACL format was added in same GSA version as security header,
      // so we abuse the ACL feature detection logic.
      securityHeaderSupported = feedConnection.supportsInheritedAcls();
    }
    return securityHeaderSupported;
  }

  // TODO: Range requests?

  /**
   * Retrieves the content of a document from a connector instance.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doGet(req, res);
  }

  /**
   * Retrieves the content of a document from a connector instance.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doGet(req, res, Context.getInstance().getManager());
  }

  /**
   * Fetches the last modified date for the document, in milliseconds since
   * the epoch; or -1 if the last modified date is not known or unavailable.
   *
   * @param req
   * @return a long integer specifying the time the document was last modified,
   * in milliseconds since midnight, January 1, 1970 GMT, or -1 if the time is
   * not known.
   */
  @Override
  protected long getLastModified(HttpServletRequest req) {
    Map<String, List<String>> params = getQueryParams(req);
    String connectorName = ServletUtil.getFirstParameter(
        params, ServletUtil.XMLTAG_CONNECTOR_NAME);
    String docid = ServletUtil.getFirstParameter(
        params, ServletUtil.QUERY_PARAM_DOCID);
    if (Strings.isNullOrEmpty(connectorName) || Strings.isNullOrEmpty(docid)) {
      return -1L;
    }
    return handleGetLastModified(getDocumentMetaDataNoThrow(req,
        Context.getInstance().getManager(), connectorName, docid));
  }

  /**
   * Returns a map of query parameters extracted from the request.
   */
  private static Map<String, List<String>> getQueryParams(HttpServletRequest req) {
    @SuppressWarnings("unchecked") Map<String, List<String>> params = 
        (Map<String, List<String>>)(req.getAttribute(PARAMETER_CACHE_NAME));
    if (params == null) {
      params = ServletUtil.parseQueryString(req.getQueryString());
      req.setAttribute(PARAMETER_CACHE_NAME, params);
    }
    return params;
  }

  /**
   * Retrieves the content of a document from a connector instance.
   *
   * @param req
   * @param res
   * @param manager manager to use for retrieving document information
   * @throws IOException
   */
  @VisibleForTesting
  static void doGet(HttpServletRequest req, HttpServletResponse res,
      Manager manager) throws IOException {
    // The servlet relies on proper security to be handled by a filter.

    if ("SecMgr".equals(req.getHeader("User-Agent")) || 
        req.getHeader("Range") != null ||
        "HEAD".equals(req.getMethod())) {
      // GSA does a GET with Range:0-0 to simulate head request.
      // Assume that a "HEAD" request to check authz is being performed
      // due to presence of Range header.
      // We don't support authz by hr so we always issue deny.
      // TODO(ejona): Remove checking for Range header and HEAD once 
      // Legacy Authz is removed from supported GSA versions.
      LOGGER.finest("RETRIEVER: Head request denied");
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    Map<String, List<String>> params = getQueryParams(req);
    String connectorName = ServletUtil.getFirstParameter(
        params, ServletUtil.XMLTAG_CONNECTOR_NAME);
    String docid = ServletUtil.getFirstParameter(
        params, ServletUtil.QUERY_PARAM_DOCID);
    if (Strings.isNullOrEmpty(connectorName) || Strings.isNullOrEmpty(docid)) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    NDC.pushAppend("Retrieve " + connectorName + " "
                   + docid.substring(docid.lastIndexOf('/') + 1));

    Document metadata;
    try {
      metadata = getDocumentMetaData(req, manager, connectorName, docid);
    } catch (Exception e) {
      res.sendError(handleException("metadata", e));
      return;
    }

    int securityCode = handleMarkingDocumentSecurity(req, res, metadata);
    if (securityCode != HttpServletResponse.SC_OK) {
      res.sendError(securityCode);
      return;
    }

    // Set the Content-Type. 
    String mimeType = handleGetContentType(metadata);
    LOGGER.log(Level.FINEST, "Document Content-Type {0}", mimeType);
    res.setContentType(mimeType);

    Integer contentLength = handleGetContentLength(metadata);
    if (contentLength != null) {
      LOGGER.log(Level.FINEST, "Document Content-Length {0}", contentLength);
      res.setContentLength(contentLength);
    }

    // Supply the document metadata in an X-Gsa-External-Metadata header.
    if (metadata != null) {
      res.setHeader(EXTERNAL_METADATA_HEADER, getMetadataHeader(metadata));
    }

    OutputStream out = res.getOutputStream();
    if (useCompression) {
      // Select Content-Encoding based on the client's Accept-Encoding header.
      // Choose GZIP if the header includes "gzip", otherwise no compression.
      String encodings = req.getHeader("Accept-Encoding");
      if (encodings != null && encodings.matches(".*\\bgzip\\b.*")) {
        res.setHeader("Content-Encoding", "gzip");
        out = new GZIPOutputStream(out, 64 * 1024);
      }
      res.setHeader("Vary", "Accept-Encoding");
    }

    // TODO: Configure chunked output?

    try {
      int code = handleDoGet(manager, connectorName, docid, out);
      if (code != HttpServletResponse.SC_OK) {
        res.sendError(code);
      } else {
        res.setStatus(code);
      }
    } finally {
      out.close();
      NDC.pop();
    }
  }

  /**
   * Builds the GSA-specific metadata header value for crawl-time metadata,
   * based upon the Document's supplied metadata.
   */
  // Warning: See XmlFeed.wrapMetaData() if you make changes here.
  @VisibleForTesting
  static String getMetadataHeader(Document metadata) {
    StringBuilder sb = new StringBuilder();
    Set<String> propertyNames = null;
    try {
      propertyNames = metadata.getPropertyNames();
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve property names", e);
    }
    if (propertyNames != null && !propertyNames.isEmpty()) {
      // Sort property names so that metadata is written in a canonical form.
      // The GSA's metadata change detection logic depends on the metadata to
      // be in the same order each time to prevent reindexing.
      propertyNames = new TreeSet<String>(propertyNames);
      for (String name : propertyNames) {
        if (XmlFeed.propertySkipSet.contains(name)) {
          continue;
        }
        try { 
          Property property = metadata.findProperty(name);
          if (property != null) {
            encodeOneProperty(sb, name, property);
          }
        } catch (RepositoryException e) {
          LOGGER.log(Level.WARNING, "Failed to retrieve property " + name, e);
        }
      }
    }
    return (sb.length() == 0) ? "" : sb.substring(0, sb.length() - 1);
  }
  
  /**
   * Adds one Property's values to the metadata header under contruction.
   */
  private static void encodeOneProperty(StringBuilder sb, String name,
      Property property) throws RepositoryException {
    ValueImpl value;
    while ((value = (ValueImpl) property.nextValue()) != null) {
      LOGGER.log(Level.FINEST, "PROPERTY: {0} = \"{1}\"",
                 new Object[] { name, value.toString() });
      String valString = value.toFeedXml();
      if (!Strings.isNullOrEmpty(valString)) {
        ServletUtil.percentEncode(sb, name, valString);
        sb.append(',');
      }
    }
  }

  /**
   * Retrieves the content of a document from a connector instance.
   *
   * @param manager a Manager
   * @param connectorName the name of the connector instance that
   *        can access the document
   * @param docId the document identifer
   * @param out OutputStream to which to write the content
   * @return an HTTP Status Code
   * @throws IOException
   */
  @VisibleForTesting
  static int handleDoGet(Manager manager, String connectorName, String docid,
      OutputStream out) throws IOException {
    InputStream in = null;
    try {
      in = manager.getDocumentContent(connectorName, docid);
      if (in == null) {
        // This is unlikely to happen, since Production Manager
        // will return an AlternateContent InputStream.
        in = new ByteArrayInputStream(new byte[0]);
      }
      byte[] buffer = new byte[1024 * 1024];
      int bytes;
      do {
        bytes = in.read(buffer);
        if (bytes > 0) {
          out.write(buffer, 0, bytes);
        }
      } while (bytes != -1);
      return HttpServletResponse.SC_OK;
    } catch (Exception e) {
      return handleException("content", e);
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  /**
   * Retrieve and cache the metadata of the currently requested document.
   * The metadata is cached for the life of the servlet request.
   * No checked exceptions are thrown.
   * If a problem occurs  {@code null} is returned.
   *
   * @param req Request to use for caching return value
   * @param manager a Manager
   * @param connectorName the name of the connector instance that
   *        can access the document
   * @param docId the document identifer
   * @return document's metadata or {@code null} if it is unavailable
   */
  private static Document getDocumentMetaDataNoThrow(HttpServletRequest req, 
      Manager manager, String connectorName, String docid) {
    try {
      return getDocumentMetaData(req, manager, connectorName, docid);
    } catch (ConnectorManagerException e) {
      return null;
    } catch (RepositoryException e) {
      return null;
    }
  }

  /**
   * Retrieve and cache the metadata of the currently requested document.
   * The metadata is cached for the life of the servlet request.
   *
   * @param req Request to use for caching return value
   * @param manager a Manager
   * @param connectorName the name of the connector instance that
   *        can access the document
   * @param docId the document identifer
   * @return document's metadata or {@code null} if it is unavailable
   */
  @VisibleForTesting
  static Document getDocumentMetaData(HttpServletRequest req,
      Manager manager, String connectorName, String docid)
      throws ConnectorManagerException, RepositoryException {
    Object cache = req.getAttribute(METADATA_CACHE_NAME);
    if (cache != null) {
      return cache == NEGATIVE_METADATA_CACHE_VALUE ? null : (Document) cache;
    }

    Document metadata = manager.getDocumentMetaData(connectorName, docid);
    req.setAttribute(METADATA_CACHE_NAME, 
        (metadata == null) ? NEGATIVE_METADATA_CACHE_VALUE : metadata);
    return metadata;
  }

  /**
   * Retrieves the last modified date of a document from a connector instance.
   *
   * @param metadata the Document metadata
   * @return a long integer specifying the time the document was last modified,
   *         in milliseconds since midnight, January 1, 1970 GMT, or -1L if the
   *         time is not known
   */
  @VisibleForTesting
  static long handleGetLastModified(Document metadata) {
    if (metadata == null) {
      return -1L;
    }

    try {
      // TODO: Value and DateValue Calendar methods are too weak to try to get
      // last modified from non-DateValues.
      ValueImpl value = (ValueImpl)
          Value.getSingleValue(metadata, SpiConstants.PROPNAME_LASTMODIFIED);
      if (value == null) {
        LOGGER.log(Level.FINEST, "Document does not contain {0}",
                   SpiConstants.PROPNAME_LASTMODIFIED);
      } else if (value instanceof DateValue) {
        // DateValues don't give direct access to their Calendar object, but
        // I can get the Calendar back out by parsing the stringized version.
        // This method also applies the FeedTimeZone, if needed.
        // TODO: Add a DateValue.getTimeMillis() or getCalendar() method to
        // directly access the wrapped value.
        String lastModified = ((DateValue) value).toIso8601();
        LOGGER.log(Level.FINEST, "Document last modified {0}", lastModified);
        return Value.iso8601ToCalendar(lastModified).getTimeInMillis();
      }
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve last-modified date", e);
    } catch (ParseException e) {
      LOGGER.log(Level.WARNING, "Failed to parse last-modified date", e);
    }
    return -1L;
  }

  /**
   * Retrieves the content type of a document from a connector instance.
   *
   * @param metadata the Document metadata
   * @return the content-type of the document, as a string, or 
   *         {@link SpiConstants.DEFAULT_MIMETYPE} if the content type
   *         is not supplied.
   */
  @VisibleForTesting
  static String handleGetContentType(Document metadata) {
    // NOTE: To maintain consistency with the XmlFeed, this code returns
    // SpiConstants.DEFAULT_MIMETYPE ("text/html") if the Document supplies
    // no mime type property. However, the GSA would really rather receive
    // MimeTypeDetector.UKNOWN_MIMETYPE ("application/octet-stream").
    if (metadata != null) {
      try {
        String mimeType = Value.getSingleValueString(metadata, 
            SpiConstants.PROPNAME_MIMETYPE);
        if (!Strings.isNullOrEmpty(mimeType)) {
          return mimeType;
        }
      } catch (RepositoryException e) {
        LOGGER.log(Level.WARNING, "Failed to retrieve content-type", e);
      }
    }
    return SpiConstants.DEFAULT_MIMETYPE;
  }
  
  /**
   * Retrieves the content length of a document from a connector instance.
   *
   * @param metadata the Document metadata
   * @return the content-length of the document, as an Integer, or {@code null}
   *         if the content length is not known, less than or equal to zero,
   *         or the value does not fit in an Integer.  Note that if the
   *         content-length returned by the connector is zero, this returns
   *         null, since the GSA does not support empty documents, so the
   *         empty content will be replaced by ProductionManager with alternate
   *         non-empty content.
   */
  @VisibleForTesting
  static Integer handleGetContentLength(Document metadata) {
    if (metadata != null) {
      try {
        String lengthStr = Value.getSingleValueString(metadata, 
            SpiConstants.PROPNAME_CONTENT_LENGTH);
        if (!Strings.isNullOrEmpty(lengthStr)) {
          Integer length = Integer.valueOf(lengthStr);
          return (length > 0) ? length : null;
        }
      } catch (NumberFormatException e) {
        LOGGER.log(Level.WARNING, "Failed to retrieve content-length", e);
      } catch (RepositoryException e) {
        LOGGER.log(Level.WARNING, "Failed to retrieve content-length", e);
      }
    }
    return null;
  }
  
  @VisibleForTesting
  static int handleMarkingDocumentSecurity(HttpServletRequest req,
      HttpServletResponse res, Document metadata) throws IOException {
    if (req.getHeader("Authorization") != null) {
      // GSA logged in; it is aware of the access restrictions on the document.
      return HttpServletResponse.SC_OK;
    }

    if (metadata == null) {
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    }

    ValueImpl isPublicVal;
    try {
      isPublicVal = (ValueImpl) Value.getSingleValue(metadata,
          SpiConstants.PROPNAME_ISPUBLIC);
    } catch (RepositoryException ex) {
      LOGGER.log(Level.WARNING, "Failed retrieving isPublic property", ex);
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    }
    boolean isPublic = isPublicVal == null || isPublicVal.toBoolean();

    if (isSecurityHeaderSupported()) {
      res.setHeader("X-Gsa-Serve-Security", isPublic ? "public" : "secure");
      return HttpServletResponse.SC_OK;
    } else {
      if (isPublic) {
        return HttpServletResponse.SC_OK;
      } else {
        res.setHeader("WWW-Authenticate", "Basic realm=\"Retriever\"");
        return HttpServletResponse.SC_UNAUTHORIZED;
      }
    }
  }

  /** Logs an Exception and returns an appropriate HTTP status code. */
  private static int handleException(String context, Exception e)
      throws IOException {
    if (e instanceof DocumentNotFoundException) {
      LOGGER.log(Level.FINE, "Failed to retrieve document {0}: {1}",
                 new Object[] {context, e.toString()});
      return HttpServletResponse.SC_NOT_FOUND;
    } else if (e instanceof SkippedDocumentException) {
      LOGGER.log(Level.FINE, "Failed to retrieve document {0}: {1}",
                 new Object[] {context, e.toString()});
      return HttpServletResponse.SC_NOT_FOUND;
    } else if (e instanceof DocumentAccessException) {
      LOGGER.log(Level.FINE, "Failed to retrieve document {0}: {1}",
                 new Object[] {context, e.toString()});
      return HttpServletResponse.SC_FORBIDDEN;
    } else if (e instanceof ConnectorNotFoundException) {
      LOGGER.log(Level.FINE, "Failed to retrieve document {0}: {1}",
                 new Object[] {context, e.toString()});
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    } else if (e instanceof RepositoryException) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document " + context, e);
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    } else if (e instanceof IOException) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document " + context, e);
      throw (IOException) e;
    } else if (e instanceof RuntimeException) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document " + context, e);
      throw (RuntimeException) e;
    } else { // ConnectorManagerException
      LOGGER.log(Level.SEVERE, "Failed to retrieve document " + context, e);
      return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
  }
}
