// Copyright 2006 Google Inc.  All Rights Reserved.
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
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Admin servlet to update connector config.
 * 
 */
public class UpdateConnector extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(UpdateConnector.class.getName());

  /**
   * Returns the config form filled with data.
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String language = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    BufferedReader reader = req.getReader();
    PrintWriter out = res.getWriter();
    res.setContentType(ServletUtil.MIMETYPE_HTML);
    String xmlBody = StringUtils.readAllToString(reader);
    if (xmlBody.length() < 1) {
      String status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_REQUEST;
      ServletUtil.writeSimpleResponse(out, status);
      LOGGER.info("The request is empty");
      return;
    }

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    out.print(handleDoGet(manager, xmlBody, connectorName, language));
    out.close();
  }

  /**
   * Returns the simple response if successfully updating the config.
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    String lang = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
    Map configData = new TreeMap();
    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    String connectorType = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_TYPE);
    Enumeration names = req.getParameterNames();
    for (Enumeration e = names; e.hasMoreElements();) {
      String name = (String) e.nextElement();
      configData.put(name, req.getParameter(name));
    }

    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    ConfigureResponse configRes = null;
    try {
      configRes =
          manager.setConnectorConfig(connectorName, connectorType, configData,
              lang);
    } catch (ConnectorNotFoundException e) {
      LOGGER.info("ConnectorNotFoundException" + e.getMessage());
      status = e.toString();
      e.printStackTrace();
    } catch (PersistentStoreException e) {
      LOGGER.info("PersistentStoreException" + e.getMessage());
      status = e.toString();
      e.printStackTrace();
    }

    ServletUtil.writeConfigureResponse(out, status, configRes);
    out.close();
  }

  public static String handleDoGet(Manager manager, String xmlBody,
      String connectorName, String language) {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(xmlBody, errorHandler);
    NodeList nodeList =
        document.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_CONFIG);
    if (nodeList.getLength() == 0) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE;
      LOGGER.info(ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE);
      return ServletUtil.htmlErrorPage(status);
    }

    String formSnippet = null;
    try {
      ConfigureResponse configRes =
          manager.getConfigFormForConnector(connectorName, language);
      formSnippet = configRes.getFormSnippet();
    } catch (ConnectorNotFoundException e) {
      status = e.toString();
      LOGGER.info(status);
      e.printStackTrace();
    } catch (InstantiatorException e) {
      status = e.toString();
      LOGGER.info(status);
      e.printStackTrace();
    }
    if (formSnippet == null) {
      formSnippet = ServletUtil.DEFAULT_FORM;
    }

    StringBuffer sbuf =
        new StringBuffer(
            "<HTML><HEAD><TITLE>Update Connector Config</TITLE></HEAD>\n"
                + "<BODY><H3>Update Connector Config:</H3><HR>\n"
                + "<FORM METHOD=POST ACTION=\"/connector-manager/updateConnector?"
                + ServletUtil.XMLTAG_CONNECTOR_NAME + "=" + connectorName + "&"
                + ServletUtil.QUERY_PARAM_LANG + "=" + language + "\"><TABLE>"
                + "<tr><td>Connector Name: " + connectorName
                + "</td></tr><tr>\n");
    int beginQuote = 0;
    int endQuote = 0;
    String snip = formSnippet;
    String value = null;
    Map configData =
        ServletUtil.getAllAttributes((Element) nodeList.item(0),
            ServletUtil.XMLTAG_PARAMETERS);
    if (configData.isEmpty()) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_CONFIG_DATA;
      return ServletUtil.htmlErrorPage(status);
    }

    while ((beginQuote = snip.indexOf(ServletUtil.HTML_NAME)) != -1) {
      endQuote =
          snip.indexOf(ServletUtil.HTML_QUOTE, beginQuote
              + ServletUtil.HTML_NAME.length());
      sbuf.append(snip.substring(0, endQuote + 1));
      String key =
          snip.substring(beginQuote + ServletUtil.HTML_NAME.length(), endQuote);
      value = (String) configData.get(key);
      if (value != null) {
        sbuf.append(ServletUtil.HTML_VALUE).append(value).append(
            ServletUtil.HTML_QUOTE);
      }
      snip = snip.substring(endQuote + 1, snip.length());

    }
    sbuf.append(snip.substring(0, snip.length()));
    sbuf.append("<tr><td><INPUT TYPE=\"SUBMIT\" NAME=\"action\" VALUE="
        + "\"submit\"></td></tr></TABLE></FORM></BODY></HTML>\n");
    return sbuf.toString();
  }
}
