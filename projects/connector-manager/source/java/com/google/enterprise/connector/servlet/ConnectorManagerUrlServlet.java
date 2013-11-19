// Copyright 2006 Google Inc.
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

import com.google.common.base.Strings;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An abstract class for Connector Manager servlets.
 * It contains an abstract method "processDoPost".
 */
public abstract class ConnectorManagerUrlServlet extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorManagerUrlServlet.class.getName());

  /**
   * This abstract method processes XML servlet-specific request body,
   * make servlet-specific call to the connector manager and write the
   * XML response body.
   *
   * @param connectorManagerUrl URL string for the Connector Manager servlet
   * @param xmlBody String the servlet-specific request body string in XML
   * @param manager Manager
   * @param out PrintWriter where the XML response body is written
   */
  protected abstract void processDoPost(String connectorManagerUrl, 
      String xmlBody, Manager manager, PrintWriter out);

  /**
   * Returns an XML response to the HTTP GET request.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doPost(req, res);
  }

  /**
   * Returns an XML response including full status (ConnectorMessageCode) to
   * the HTTP POST request.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
          .allowed(RemoteAddressFilter.Access.BLACK, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    if (req.getCharacterEncoding() == null) {
      req.setCharacterEncoding("UTF-8");
    }
    BufferedReader reader = req.getReader();
    res.setContentType(ServletUtil.MIMETYPE_XML);
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();
    NDC.push(NDC.peek());
    try {
      // I encountered a null reader if no content or body.
      String xmlBody =
          (reader == null) ? null : StringUtils.readAllToString(reader);
      if (Strings.isNullOrEmpty(xmlBody)) {
        ServletUtil.writeResponse(
            out, ConnectorMessageCode.RESPONSE_EMPTY_REQUEST);
        LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_EMPTY_REQUEST);
        return;
      }

      // Get the URL for the Connector Manager servlet context.
      StringBuffer requestUrl = req.getRequestURL();
      int index = requestUrl.indexOf(req.getServletPath());
      if (index > 0) {
        requestUrl.setLength(index);
      }
      if (requestUrl.charAt(requestUrl.length() - 1) == '/') {
        requestUrl.setLength(requestUrl.length() - 1);
      }
      String webappUrl = requestUrl.toString();

      Manager manager = Context.getInstance().getManager();
      processDoPost(webappUrl, xmlBody, manager, out);

    } finally {
      out.close();
      NDC.pop();
    }
  }
}
