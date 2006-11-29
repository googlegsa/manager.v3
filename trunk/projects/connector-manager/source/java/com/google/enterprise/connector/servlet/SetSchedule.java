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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;

/**
 * Admin servlet for SetSchedule
 *
 */
public class SetSchedule extends HttpServlet {
  private static final Logger LOGGER =
    Logger.getLogger(SetSchedule.class.getName());

    /**
     * Returns the simple response if successfully setting the schedule.
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
   * Returns the simple response if successfully setting the schedule.
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   *
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    BufferedReader reader = req.getReader();
    PrintWriter out = res.getWriter();
    res.setContentType(ServletUtil.MIMETYPE_XML);
    String xmlBody = StringUtils.readAllToString(reader);
    if (xmlBody == null || xmlBody.length() < 1) {
      ServletUtil.writeSimpleResponse(
          out, ConnectorMessageCode.RESPONSE_EMPTY_REQUEST);
      LOGGER.info("The request is empty");
      return;
    }
    
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    ConnectorMessageCode status = handleDoPost(manager, xmlBody);
    ServletUtil.writeResponse(out, status);
    out.close();
  }
  
  public static ConnectorMessageCode handleDoPost(Manager manager, String xmlBody) {
    ConnectorMessageCode status = new ConnectorMessageCode();
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(xmlBody, errorHandler);
    if (document == null) {
      status.setMessageId(ConnectorMessageCode.EXCEPTION_XML_PARSING);
      return status;
    }
    NodeList nodeList =
        document.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_SCHEDULES);
    if (nodeList == null || nodeList.getLength() == 0) {
      status.setMessageId(ConnectorMessageCode.RESPONSE_EMPTY_NODE);
      LOGGER.warning("Error: " + ConnectorMessageCode.RESPONSE_EMPTY_NODE);
      return status;
    }

    String connectorName = ServletUtil.getFirstElementByTagName(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_CONNECTOR_NAME);
    int load = Integer.parseInt(ServletUtil.getFirstElementByTagName(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_LOAD));
    String timeIntervals = ServletUtil.getFirstElementByTagName(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_TIME_INTERVALS);

    try {
      manager.setSchedule(connectorName, load, timeIntervals);
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.WARNING, "Connector Not Found Exception: ", e);
      status.setMessageId(ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND);
      String[] params = {connectorName};
      status.setParams(params);
    } catch (PersistentStoreException e) {
      LOGGER.log(Level.WARNING, "Persistent Store Exception: ", e);
      status.setMessageId(ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
    }
    return status;
  }
}
