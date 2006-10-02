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

import com.google.enterprise.connector.manager.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to get the connector status for a given connector.
 * 
 */
public class GetConnectorStatus extends HttpServlet {
  private static final Logger LOG =
      Logger.getLogger(GetConnectorStatus.class.getName());

  /**
   * Returns the connector status.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();

    if (connectorName == null || connectorName.length() < 1) {
      status = ServletUtil.XML_RESPONSE_STATUS_NULL_CONNECTOR;
      ServletUtil.writeSimpleResponse(out, status);
      LOG.info("ConnectorName is null");
      return;
    }

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();

    handleDoGet(out, manager, connectorName);
    out.close();
  }

  /**
   * Returns the connector status.
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
   * @param out PrintWriter Output for servlet response
   * @param status ConnectorStatus
   */
  public static void handleDoGet(
      PrintWriter out, Manager manager, String connectorName) {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;

    ConnectorStatus connectorStatus = manager.getConnectorStatus(connectorName);

    if (connectorStatus == null) {
      status = ServletUtil.XML_RESPONSE_STATUS_NULL_CONNECTOR_STATUS;
      ServletUtil.writeSimpleResponse(out, status);
      LOG.info("Connector manager returns null status for " + connectorName
          + ".");
     return;
    }

    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, false);
    ServletUtil.writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID, "0");
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOT_STATUS, false);
    ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_CONNECTOR_NAME,
        connectorStatus.getName());
    ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_CONNECTOR_TYPE,
        connectorStatus.getType());
    ServletUtil.writeXMLElement(out, 2, ServletUtil.XMLTAG_STATUS, Integer
        .toString(connectorStatus.getStatus()));

    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOT_STATUS, true);
    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);
  }
}
