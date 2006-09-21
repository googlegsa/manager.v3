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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Admin servlet for Authenticate
 *
 */
public class Authenticate  extends HttpServlet {
  private static final Logger LOG =
    Logger.getLogger(Authenticate.class.getName());

  /**
   * Returns credentials for connectors.
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
   * Returns credentials for connectors.
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
    handleDoPost(out, xmlBody, manager);
    out.close();

  }

  public static void handleDoPost(PrintWriter out,
      String xmlBody, Manager manager) {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(xmlBody, errorHandler);
    NodeList nodeList =
        document.getElementsByTagName(ServletUtil.XMLTAG_AUTHN_REQUEST);
    if (nodeList.getLength() == 0) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE;
      LOG.info(ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE);
      ServletUtil.writeSimpleResponse(out, status);
      return;
    }

    NodeList credList =
      document.getElementsByTagName(ServletUtil.XMLTAG_AUTHN_CREDENTIAL);
    if (credList.getLength() == 0) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE;
      LOG.info(ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE);
      ServletUtil.writeSimpleResponse(out, status);
      return;
    }

    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, false);
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHN_RESPONSE, false);

    String username = ServletUtil.getFirstElementByTagName(
      (Element) credList.item(0), ServletUtil.XMLTAG_AUTHN_USERNAME);
    String password = ServletUtil.getFirstElementByTagName(
      (Element) credList.item(0), ServletUtil.XMLTAG_AUTHN_PASSWORD);
    List connList = manager.getConnectorStatuses();
    for (Iterator iter = connList.iterator(); iter.hasNext();) {
      String connectorName = ((ConnectorStatus) iter.next()).getName();
      boolean authn = manager.authenticate(connectorName, username, password);
      if (authn) {
        ServletUtil.writeXMLElementWithAttrs(
            out, 2, ServletUtil.XMLTAG_SUCCESS,
            ServletUtil.XMLTAG_CONNECTOR_NAME + "=\"" + connectorName + "\"");
        ServletUtil.writeXMLElement(
            out, 3, ServletUtil.XMLTAG_IDENTITY, username);
        ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_SUCCESS, true);
      } else {
        ServletUtil.writeXMLElementWithAttrs(
            out, 2, ServletUtil.XMLTAG_FAILURE,
            ServletUtil.XMLTAG_CONNECTOR_NAME + "=\"" + connectorName + "\"");
        ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_FAILURE, true);
      }
    }
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHN_RESPONSE, true);
    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);
    return;
  }
}
