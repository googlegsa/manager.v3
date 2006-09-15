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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to get the connector status for a given connector.
 * 
 */
public class GetConnectorStatus extends HttpServlet {
  private static final Logger logger =
    Logger.getLogger(GetConnectorStatus.class.getName());

    /**
     * Returns the connector status.
     * @param req 
     * @param res 
     * @throws ServletException 
     * @throws IOException 
     * 
     */
  protected void doGet(HttpServletRequest req,
                       HttpServletResponse res)
      throws ServletException, IOException {
    String connectorName = req.getParameter("ConnectorName");
    if (connectorName.length() < 1) {
      logger.info("ConnectorName is null");
      return;
    }

    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    MockManager mockManager = MockManager.getInstance();
    ConnectorStatus connectorStatus = mockManager.getConnectorStatus(connectorName);
    if (connectorStatus == null) {
      logger.info("Connector manager returns null status for " +
                   connectorName + ".");
    }

    handleDoGet(out, connectorStatus);
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
  protected void doPost(HttpServletRequest req,
                        HttpServletResponse res)
      throws ServletException, IOException {
    doGet(req, res);
  }


  /**
   * Handler for doGet in order to do unit tests.
   * @param out PrintWriter Output for servlet response
   * @param status ConnectorStatus
   */
  public static void handleDoGet(PrintWriter out, ConnectorStatus status) {
    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, false);
    ServletUtil.writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID, "0");

    if (status == null) {
      // logger.info("Connector manager returns null status for " +
      //             connectorName + ".");
      // logger.info("Connector manager returns null status.");
      ServletUtil.writeXMLElement(
          out, 1, ServletUtil.XMLTAG_CONNECTOT_STATUS, "null");
      ServletUtil.writeXMLTag(
          out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);
      return;
    }

    ServletUtil.writeXMLTag(
        out, 1, ServletUtil.XMLTAG_CONNECTOT_STATUS, false);
    ServletUtil.writeXMLElement(
        out, 2, ServletUtil.XMLTAG_CONNECTOR_NAME, status.getName());
    ServletUtil.writeXMLElement(
        out, 2, ServletUtil.XMLTAG_CONNECTOR_TYPE, status.getType());
    ServletUtil.writeXMLElement(
        out, 2, ServletUtil.XMLTAG_STATUS,
       	Integer.toString(status.getStatus()));

    ServletUtil.writeXMLTag(
            out, 1, ServletUtil.XMLTAG_CONNECTOT_STATUS, true);
    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);
  }
}
