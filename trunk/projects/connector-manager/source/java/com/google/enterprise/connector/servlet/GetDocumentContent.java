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
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.ValueImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

  private static boolean useCompression = false;

  public static void setUseCompression(boolean doCompression) {
    useCompression = doCompression;
  }

  // TODO: HEAD requests?
  // TODO: Range requests?

  /**
   * Fetch the document LastModified date.
   *
   * @param req an HttpServletRequest that specifies the connector name and
   *        docid for a document as query parameters
   * @return a long integer specifying the time the document was last modified,
   *         in milliseconds since midnight, January 1, 1970 GMT, or -1L if the
   *         time is not known
   */
  @Override
  protected long getLastModified(HttpServletRequest req) {
    /* NOTE: For reasons that defy comprehension, javax.servlet.http.HttpServlet
     * calls this even if the request does not include the "If-Modified-Since"
     * Header.  Fetching the lastModifiedDate from the ECM repository can be
     * quite expensive (certainly more expensive than req.containsHeader()).
     * So I disable this and perform "If-Modified-Since" checking in doGet().
     */
    return -1L;
    // TODO: Restore this if we choose to fully support getLastModified().
    /*
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
        .allowed(RemoteAddressFilter.Access.BLACK, req.getRemoteAddr())) {
      return -1L;
    }

    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    NDC.pushAppend("LastModified " + connectorName);
    try {
      Manager manager = Context.getInstance().getManager();
      String docid = req.getParameter(ServletUtil.QUERY_PARAM_DOCID);
      return handleGetLastModified(manager, connectorName, docid);
    } finally {
      NDC.pop();
    }
    */
  }

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
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
          .allowed(RemoteAddressFilter.Access.BLACK, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    NDC.pushAppend("Retrieve " + connectorName);
    Manager manager = Context.getInstance().getManager();
    String docid = req.getParameter(ServletUtil.QUERY_PARAM_DOCID);

    // Only fetch the lastModifiedDate from the repository if requested.
    // See the comment in getLastModified() above.
    long ifModifiedSince = req.getDateHeader(HDR_IF_MODIFIED);
    if (ifModifiedSince != -1L) {
      if (LOGGER.isLoggable(Level.FINEST)) {
        LOGGER.finest("RETRIEVER: Get Document " + docid + " "
            + HDR_IF_MODIFIED + " " + req.getHeader(HDR_IF_MODIFIED));
      }
      long lastModified = handleGetLastModified(manager, connectorName, docid);
      // If the document modification time is later, fall through to fetch
      // the content, otherwise return SC_NOT_MODIFIED.  Since the GSA sends
      // a modified-since time of the crawl time, rather than the document's
      // actual supplied lastModified meta-data, fudge the time by a minute
      // to avoid client/server clock disparities.
      if (lastModified < ifModifiedSince - (60 * 1000)) {
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("RETRIEVER: Document " + docid + " unchanged");
        }
        res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      }
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

    // TODO: setContentType?
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
    if (Strings.isNullOrEmpty(connectorName) || Strings.isNullOrEmpty(docid)) {
      return HttpServletResponse.SC_BAD_REQUEST;
    }

    InputStream in = null;
    try {
      in = manager.getDocumentContent(connectorName, docid);
      if (in == null) {
        return HttpServletResponse.SC_NO_CONTENT;
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
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document content", e);
      return HttpServletResponse.SC_NOT_FOUND;
    } catch (RepositoryDocumentException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document content", e);
      return HttpServletResponse.SC_NOT_FOUND;
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
   * Retrieves the last modified date of a document from a connector instance.
   *
   * @param manager a Manager
   * @param connectorName the name of the connector instance that
   *        can access the document
   * @param docId the document identifer
   * @return a long integer specifying the time the document was last modified,
   *         in milliseconds since midnight, January 1, 1970 GMT, or -1L if the
   *         time is not known
   */
  @VisibleForTesting
  static long handleGetLastModified(Manager manager, String connectorName,
                                    String docid) {
    if (Strings.isNullOrEmpty(connectorName) || Strings.isNullOrEmpty(docid)) {
      return -1L;
    }
    try {
      Document document = manager.getDocumentMetaData(connectorName, docid);
      if (document == null) {
        return -1L;
      }
      ValueImpl value = (ValueImpl)
          Value.getSingleValue(document, SpiConstants.PROPNAME_LASTMODIFIED);
      if (value == null) {
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Document " + docid + " does not contain "
                        + SpiConstants.PROPNAME_LASTMODIFIED);
        }
        return -1L;
      }
      if (value instanceof DateValue) {
        // DateValues don't give direct access to their Calendar object, but
        // I can get the Calendar back out by parsing the stringized version.
        // This method also applies the FeedTimeZone, if needed.
        // TODO: Add a DateValue.getTimeMillis() or getCalendar() method to
        // directly access the wrapped value.
        String lastModified = ((DateValue) value).toIso8601();
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Document " + docid + " last modified " + lastModified);
        }
        return Value.iso8601ToCalendar(lastModified).getTimeInMillis();
      }
      // TODO: Value and DateValue Calendar methods are too weak to try to get
      // last modified from non-DateValues.
      return -1L;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve document last modified", e);
      return -1L;
    }
  }
}
