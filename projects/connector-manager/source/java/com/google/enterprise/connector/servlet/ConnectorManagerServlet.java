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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An abstract class for Connector Manager servlets.
 * It contains an abstract method "processDoPost".
 */
public abstract class ConnectorManagerServlet extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorManagerServlet.class.getName());

  /**
   * This abstract method processes XML servlet-specific request body,
   * make servlet-specific call to the connector manager and write the
   * XML response body.
   *
   * @param xmlBody String the servlet-specific request body string in XML
   * @param manager Manager
   * @param out PrintWriter where the XML response body is written
   */
  protected abstract void processDoPost(
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
    try {
      Enumeration<?> headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String name = (String) headerNames.nextElement();
        LOGGER.log(Level.INFO, "HEADER " + name + ": " + req.getHeader(name));
      }
      String xmlBody = StringUtils.readAllToString(reader);
      if (xmlBody == null || xmlBody.length() < 1) {
        ServletUtil.writeResponse(
            out, ConnectorMessageCode.RESPONSE_EMPTY_REQUEST);
        LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_EMPTY_REQUEST);
        return;
      }

      Manager manager = Context.getInstance().getManager();
      processDoPost(xmlBody, manager, out);

    } finally {
      out.close();
      NDC.clear();
    }
  }
}
