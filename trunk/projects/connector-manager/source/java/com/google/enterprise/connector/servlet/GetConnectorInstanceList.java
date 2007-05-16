// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to get a list of connector types.
 * 
 */
public class GetConnectorInstanceList extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(GetConnectorInstanceList.class.getName());

  /**
   * Returns a list of connector types.
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  /**
   * Returns a list of connector types.
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    List connList = manager.getConnectorStatuses();
    handleDoPost(connList, out);
    out.close();
  }

  /**
   * Handler for doGet in order to do unit tests.
   * 
   * @param connectorInstances List a list of connector types
   * @param out PrintWriter where the response is written
   */
  public static void handleDoPost(List connectorInstances, PrintWriter out) {
    if (connectorInstances == null || connectorInstances.size() == 0) {
      ServletUtil.writeResponse(out,
          ConnectorMessageCode.RESPONSE_NULL_CONNECTOR);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_NULL_CONNECTOR);
      return;
    }

    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeStatusId(out, ConnectorMessageCode.SUCCESS);
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_INSTANCES,
        false);

    for (Iterator iter = connectorInstances.iterator(); iter.hasNext();) {
      ConnectorStatus connectorStatus = (ConnectorStatus) iter.next();
      ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_CONNECTOR_INSTANCE,
          false);
      ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_CONNECTOR_NAME,
          connectorStatus.getName());
      ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_CONNECTOR_TYPE,
          connectorStatus.getType());
      ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_STATUS, Integer
          .toString(connectorStatus.getStatus()));
      ServletUtil.writeXMLElement(out, 3,
          ServletUtil.XMLTAG_CONNECTOR_SCHEDULE, connectorStatus.getSchedule());
      ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_CONNECTOR_INSTANCE,
          true);
    }

    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_INSTANCES,
        true);
    ServletUtil.writeRootTag(out, true);
  }
}
