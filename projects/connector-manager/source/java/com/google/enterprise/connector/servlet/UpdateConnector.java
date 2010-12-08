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
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    PrintWriter out = res.getWriter();
    NDC.push("Config " + connectorName);
    try {
      String language = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
      res.setContentType(ServletUtil.MIMETYPE_HTML);
      res.setCharacterEncoding("UTF-8");
      BufferedReader reader = req.getReader();
      String xmlBody = StringUtils.readAllToString(reader);
      if (xmlBody == null || xmlBody.length() < 1) {
        ServletUtil.writeResponse(
            out, ConnectorMessageCode.RESPONSE_EMPTY_REQUEST);
        LOGGER.warning(ServletUtil.LOG_RESPONSE_EMPTY_REQUEST);
        return;
      }

      Manager manager = Context.getInstance().getManager();
      out.print(handleDoGet(manager, xmlBody, connectorName, language,
                            req.getContextPath()));
    } finally {
      out.close();
      NDC.clear();
    }
  }

  /**
   * Returns the simple response if successfully updating the config.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    ConnectorMessageCode status = new ConnectorMessageCode();
    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    String connectorType = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_TYPE);
    res.setContentType(ServletUtil.MIMETYPE_XML);
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();

    NDC.push("Config " +  connectorType + " " + connectorName);
    try {
      String lang = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
      Map<String, String> configData = new TreeMap<String, String>();
      Enumeration<?> names = req.getParameterNames();
      for (Enumeration<?> e = names; e.hasMoreElements();) {
        String name = (String) e.nextElement();
        configData.put(name, req.getParameter(name));
      }

      Manager manager = Context.getInstance().getManager();
      ConfigureResponse configRes = null;
      try {
        configRes = manager.setConnectorConfig(connectorName, connectorType,
                                               configData, lang, true);
      } catch (ConnectorManagerException e) {
        status = new ConnectorMessageCode(
            ConnectorMessageCode.EXCEPTION_CONNECTOR_EXISTS, connectorName);
        LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_EXISTS,
            e);
      }
      if (configRes != null) {
        status = new ConnectorMessageCode(
            ConnectorMessageCode.INVALID_CONNECTOR_CONFIG);
      }
      ConnectorManagerGetServlet.writeConfigureResponse(out, status, configRes);
    } finally {
      out.close();
      NDC.clear();
    }
  }

  public static String handleDoGet(Manager manager, String xmlBody,
      String connectorName, String language, String contextPath) {
    Element root = ServletUtil.parseAndGetRootElement(
      xmlBody, ServletUtil.XMLTAG_CONNECTOR_CONFIG);
    if (root == null) {
      return htmlErrorPage(ServletUtil.LOG_RESPONSE_EMPTY_NODE);
    }

    String formSnippet = null;
    try {
      ConfigureResponse configRes =
          manager.getConfigFormForConnector(connectorName, language);
      formSnippet = configRes.getFormSnippet();
    } catch (ConnectorNotFoundException e) {
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_NOT_FOUND, e);
    } catch (InstantiatorException e) {
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_INSTANTIATOR, e);
    }
    if (formSnippet == null) {
      formSnippet = ServletUtil.DEFAULT_FORM;
    }

    StringBuilder sbuf =
        new StringBuilder(
            "<HTML><HEAD><TITLE>Update Connector Config</TITLE></HEAD>\n"
                + "<BODY><H3>Update Connector Config:</H3><HR>\n"
                + "<FORM METHOD=POST ACTION=\""
                + contextPath
                + "/updateConnector?"
                + ServletUtil.XMLTAG_CONNECTOR_NAME + "=" + connectorName + "&"
                + ServletUtil.QUERY_PARAM_LANG + "=" + language + "\"><TABLE>"
                + "<tr><td>Connector Name: " + connectorName
                + "</td></tr><tr>\n");
    int beginQuote = 0;
    int endQuote = 0;
    String snip = formSnippet;
    String value = null;
    Map<String, String> configData =
        ServletUtil.getAllAttributes(root, ServletUtil.XMLTAG_PARAMETERS);
    if (configData.isEmpty()) {
      return htmlErrorPage("Empty config data");
    }

    while ((beginQuote = snip.indexOf(ServletUtil.ATTRIBUTE_NAME)) != -1) {
      endQuote = snip.indexOf(ServletUtil.QUOTE, beginQuote
                              + ServletUtil.ATTRIBUTE_NAME.length());
      sbuf.append(snip.substring(0, endQuote + 1));
      String key = snip.substring(
          beginQuote + ServletUtil.ATTRIBUTE_NAME.length(), endQuote);
      value = configData.get(key);
      if (value != null) {
        sbuf.append(ServletUtil.ATTRIBUTE_VALUE).append(value).append(
            ServletUtil.QUOTE);
      }
      snip = snip.substring(endQuote + 1, snip.length());
    }
    sbuf.append(snip.substring(0, snip.length()));
    sbuf.append("<tr><td><INPUT TYPE=\"SUBMIT\" NAME=\"action\" VALUE="
        + "\"submit\"></td></tr></TABLE></FORM></BODY></HTML>\n");
    return sbuf.toString();
  }

  public static String htmlErrorPage(String status) {
    return "<HTML><BODY>Error: " + status + "</BODY></HTML>";
  }
}
