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
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

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

  private static boolean useCompression = false;

  public static void setUseCompression(boolean doCompression) {
    useCompression = doCompression;
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
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
          .allowed(RemoteAddressFilter.Access.BLACK, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    NDC.pushAppend("Retrieve " + connectorName);

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
      Manager manager = Context.getInstance().getManager();
      String docid = req.getParameter(ServletUtil.QUERY_PARAM_DOCID);
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
   * @param connectorName the name of the connector instances that
   *        can access the document
   * @param docId the document identifer
   * @param out PrintWriter to which to write the content
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
}
