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
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.pusher.FeedConnection;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentAccessException;
import com.google.enterprise.connector.spi.DocumentNotFoundException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetDocumentContent extends HttpServlet {

  private static Logger LOGGER =
    Logger.getLogger(GetDocumentContent.class.getName());
  private static final String HDR_IF_MODIFIED = "If-Modified-Since";
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

  // TODO: HEAD requests?
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

    if ("SecMgr".equals(req.getHeader("User-Agent"))) {
      // Assume that the SecMgr is performing a "HEAD" request to check authz.
      // We don't support this, so we always issue deny.
      LOGGER.finest("RETRIEVER: SecMgr request denied");
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    Map<String, List<String>> params = ServletUtil.parseQueryString(
        req.getQueryString());
    String connectorName = ServletUtil.getFirstParameter(
        params, ServletUtil.XMLTAG_CONNECTOR_NAME);
    NDC.pushAppend("Retrieve " + connectorName);
    String docid = ServletUtil.getFirstParameter(
        params, ServletUtil.QUERY_PARAM_DOCID);

    if (Strings.isNullOrEmpty(connectorName) || Strings.isNullOrEmpty(docid)) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    int securityCode =
        handleMarkingDocumentSecurity(req, res, manager, connectorName, docid);
    if (securityCode != HttpServletResponse.SC_OK) {
      res.sendError(securityCode);
      return;
    }

    // Manually process If-Modified-Since headers instead of implementing
    // getLastModified().
    //
    // getLastModified() is called for every request in order to see if doGet()
    // can be circumvented (via If-Modified-Since) and to set the Last-Modified
    // response header (whose date is typically provided in the
    // If-Modified-Since header in later requests).
    //
    // Retrieving the lastModifiedDate from the ECM repository can be quite
    // expensive (certainly more expensive than req.containsHeader()) and the
    // GSA ignores the Last-Modified response header, so we don't provide the
    // Last-Modified header and manually process If-Modified-Since.
    long ifModifiedSince = req.getDateHeader(HDR_IF_MODIFIED);
    if (ifModifiedSince != -1L) {
      if (LOGGER.isLoggable(Level.FINEST)) {
        LOGGER.finest("RETRIEVER: Get Document " + docid + " "
            + HDR_IF_MODIFIED + " " + req.getHeader(HDR_IF_MODIFIED));
      }
      long lastModified =
          handleGetLastModified(req, manager, connectorName, docid);

      // If the document modification time is later, fall through to fetch
      // the content, otherwise return SC_NOT_MODIFIED.  Since the GSA sends
      // a modified-since time of the crawl time, rather than the document's
      // actual supplied lastModified meta-data, fudge the time by a minute
      // to avoid client/server clock disparities.
      if (lastModified != -1 && lastModified < ifModifiedSince - (60 * 1000)) {
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("RETRIEVER: Document " + docid + " unchanged");
        }
        res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      }
    }

    // Set the Content-Type. 
    String mimeType = handleGetContentType(req, manager, connectorName, docid);
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Document Content-Type " + mimeType);
    }
    res.setContentType(mimeType);

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
    } catch (DocumentNotFoundException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document content: {0}",
                 e.toString());
      return HttpServletResponse.SC_NOT_FOUND;
    } catch (SkippedDocumentException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document content: {0}",
                 e.toString());
      return HttpServletResponse.SC_NOT_FOUND;
    } catch (DocumentAccessException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document content: {0}",
                 e.toString());
      return HttpServletResponse.SC_FORBIDDEN;
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document content: {0}",
                 e.toString());
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document content", e);
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    } catch (ConnectorManagerException e) {
      LOGGER.log(Level.SEVERE, "Failed to retrieve document content", e);
      return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    } finally {
      if (in != null) {
        in.close();
      }
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
  static Document getDocumentMetaData(HttpServletRequest req, Manager manager,
      String connectorName, String docid) throws ConnectorManagerException,
      RepositoryException {
    Object cache = req.getAttribute(METADATA_CACHE_NAME);
    if (cache != null) {
      return cache == NEGATIVE_METADATA_CACHE_VALUE ? null : (Document) cache;
    }

    Document document = null;
    try {
      document = manager.getDocumentMetaData(connectorName, docid);
    } finally {
      req.setAttribute(METADATA_CACHE_NAME, 
          (document == null) ? NEGATIVE_METADATA_CACHE_VALUE : document);
    }
    return document;
  }

  /**
   * Retrieves the last modified date of a document from a connector instance.
   *
   * @param req Request whose parameters identify the document of interest
   * @param manager a Manager
   * @param connectorName the name of the connector instance that
   *        can access the document
   * @param docId the document identifer
   * @return a long integer specifying the time the document was last modified,
   *         in milliseconds since midnight, January 1, 1970 GMT, or -1L if the
   *         time is not known
   */
  @VisibleForTesting
  static long handleGetLastModified(HttpServletRequest req, Manager manager,
      String connectorName, String docid) {
    try {
      Document metaData =
          getDocumentMetaData(req, manager, connectorName, docid);
      if (metaData == null) {
        return -1L;
      }

      ValueImpl value = (ValueImpl)
          Value.getSingleValue(metaData, SpiConstants.PROPNAME_LASTMODIFIED);
      if (value == null) {
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Document does not contain "
                        + SpiConstants.PROPNAME_LASTMODIFIED);
        }
      } else if (value instanceof DateValue) {
        // DateValues don't give direct access to their Calendar object, but
        // I can get the Calendar back out by parsing the stringized version.
        // This method also applies the FeedTimeZone, if needed.
        // TODO: Add a DateValue.getTimeMillis() or getCalendar() method to
        // directly access the wrapped value.
        String lastModified = ((DateValue) value).toIso8601();
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Document last modified " + lastModified);
        }
        return Value.iso8601ToCalendar(lastModified).getTimeInMillis();
      }
      // TODO: Value and DateValue Calendar methods are too weak to try to get
      // last modified from non-DateValues.
    } catch (RepositoryDocumentException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document last modified: {0}",
                 e.toString());
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document last modified", e);
    }
    return -1L;
  }

  /**
   * Retrieves the content type of a document from a connector instance.
   *
   * @param req Request whose parameters identify the document of interest
   * @param manager a Manager
   * @param connectorName the name of the connector instance that
   *        can access the document
   * @param docId the document identifer
   * @return the content-type of the document, as a string, or 
   *         {@link SpiConstants.DEFAULT_MIMETYPE} if the content type
   *         is not supplied.
   */
  @VisibleForTesting
  static String handleGetContentType(HttpServletRequest req, Manager manager,
      String connectorName, String docid) {
    // NOTE: To maintain consistency with the XmlFeed, this code returns
    // SpiConstants.DEFAULT_MIMETYPE ("text/html") if the Document supplies
    // no mime type property. However, the GSA would really rather receive
    // MimeTypeDetector.UKNOWN_MIMETYPE ("application/octet-stream").
    try {
      Document metaData =
          getDocumentMetaData(req, manager, connectorName, docid);
      if (metaData != null) {
        String mimeType = Value.getSingleValueString(metaData, 
                          SpiConstants.PROPNAME_MIMETYPE);
        if (!Strings.isNullOrEmpty(mimeType)) {
          return mimeType;
        }
      }
    } catch (RepositoryDocumentException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document content type: {0}",
                 e.toString());
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document content type", e);
    }
    return SpiConstants.DEFAULT_MIMETYPE;
  }
  
  @VisibleForTesting
  static int handleMarkingDocumentSecurity(HttpServletRequest req,
      HttpServletResponse res, Manager manager, String connectorName,
      String docid) throws IOException {
    if (req.getHeader("Authorization") != null) {
      // GSA logged in; it is aware of the access restrictions on the document.
      return HttpServletResponse.SC_OK;
    }

    Document document;
    try { 
      document = getDocumentMetaData(req, manager, connectorName, docid);
      if (document == null) {
        return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
      }
    } catch (DocumentNotFoundException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document metadata: {0}",
                 e.toString());
      return HttpServletResponse.SC_NOT_FOUND;
    } catch (SkippedDocumentException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document metadata: {0}",
                 e.toString());
      return HttpServletResponse.SC_NOT_FOUND;
    } catch (DocumentAccessException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document metadata: {0}",
                 e.toString());
      return HttpServletResponse.SC_FORBIDDEN;
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.FINE, "Failed to retrieve document metadata: {0}",
                 e.toString());
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document metadata", e);
      return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    } catch (ConnectorManagerException e) {
      LOGGER.log(Level.SEVERE, "Failed to retrieve document metadata", e);
      return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    ValueImpl isPublicVal;
    try {
      isPublicVal = (ValueImpl) Value.getSingleValue(document,
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
}
