// Copyright (C) 2006 Google Inc.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;

/*
 * Admin servlet for removing a connector by a given name.
 */
public class RemoveConnector extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(RemoveConnector.class.getName());

  /**
   * Returns the simple response if successfully removing the manager config.
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   *
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String connectorName =
        req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    PrintWriter out = res.getWriter();
    res.setContentType(ServletUtil.MIMETYPE_XML);

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();

    handleDoGet(out, manager, connectorName);
    out.close();
  }

  /**
   * Returns the simple response if successfully removing the manager config.
   * 
   * Just call doGet
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   *
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doGet(req, res);
  }

  /**
   * Handler for doGet in order to do unit tests.
   * 
   * @param out
   * @param manager
   * @param connectorName
   */
  public static void handleDoGet(
      PrintWriter out, Manager manager, String connectorName) {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    if (connectorName == null || connectorName.length() < 1) {
      status = ServletUtil.XML_RESPONSE_STATUS_NULL_CONNECTOR;
      ServletUtil.writeSimpleResponse(out, status);
      LOGGER.log(Level.SEVERE, status);
      return;
    }

    try {
      manager.removeConnector(connectorName);
    } catch (ConnectorManagerException e) {
      LOGGER.log(Level.WARNING,
          "Unable to remove the connector: " + connectorName, e);
    }

    ServletUtil.writeSimpleResponse(out, status);
  }

}
