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
  private static final Logger LOG =
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
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    BufferedReader reader = req.getReader();
    PrintWriter out = res.getWriter();
    res.setContentType(ServletUtil.MIMETYPE_XML);
    String xmlBody = StringUtils.readAllToString(reader);
    if (xmlBody.length() < 1) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_REQUEST;
      ServletUtil.writeSimpleResponse(out, status);
      LOG.info("The request is empty");
      return;
    }
    
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    status = handleDoPost(manager, xmlBody);
    ServletUtil.writeSimpleResponse(out, status);
    out.close();
  }
  
  public static String handleDoPost(Manager manager, String xmlBody) {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(xmlBody, errorHandler);
    NodeList nodeList =
        document.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_SCHEDULES);
    if (nodeList.getLength() == 0) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE;
      LOG.info(ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE);
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
      LOG.info("ConnectorNotFoundException");
      status = e.toString();
      e.printStackTrace();
    } catch (PersistentStoreException e) {
      LOG.info("PersistentStoreException");
      status = e.toString();
      e.printStackTrace();
    }
    return status;
  }
}
