// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test UpdateConnector through a browser.
 *
 * http://localhost:8080/connector-manager/updateConnectorTest?
 * ConnectorName=connectorA&lang=en&Username=foo&Password=passwd
 *
 */
public class UpdateConnectorNoGSA extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(UpdateConnectorNoGSA.class.getName());

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
    NDC.push("Config");
    try {
      String language = null;
      String connectorName = null;
      StringWriter writer = new StringWriter();
      try {
        writer.write("<" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">");

        Enumeration<?> names = req.getParameterNames();
        for (Enumeration<?> e = names; e.hasMoreElements();) {
          String name = (String) e.nextElement();
          if (name.equalsIgnoreCase(ServletUtil.QUERY_PARAM_LANG)) {
            language = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
          } else if (name.equalsIgnoreCase(ServletUtil.XMLTAG_CONNECTOR_NAME)) {
            connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
            NDC.pushAppend(connectorName);
            writer.write("  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">"
                + connectorName + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME
                + ">\n");
          } else if (name.equalsIgnoreCase(ServletUtil.XMLTAG_CONNECTOR_TYPE)) {
            String connectorType =
                req.getParameter(ServletUtil.XMLTAG_CONNECTOR_TYPE);
            NDC.pushAppend(connectorType);
            writer.write("  <" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">"
                + connectorType + "</" + ServletUtil.XMLTAG_CONNECTOR_TYPE
                + ">\n");
          } else {
            writer.write("  <" + ServletUtil.XMLTAG_PARAMETERS + " name=\""
                + name + "\" value=\"" + req.getParameter(name) + "\"/>\n");
          }
        }
        writer.write("</" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">");
      } finally {
        writer.close();
      }

      PrintWriter out = res.getWriter();
      try {
        res.setContentType(ServletUtil.MIMETYPE_HTML);

        Manager manager = Context.getInstance().getManager();
        out.print(UpdateConnector.handleDoGet(manager, writer.getBuffer()
            .toString(), connectorName, language, req.getContextPath()));
      } finally {
        out.close();
      }
    } finally {
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
    PrintWriter out = res.getWriter();

    NDC.push("Config " + connectorName);
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
            ConnectorMessageCode.EXCEPTION_CONNECTOR_MANAGER);
        LOGGER.log(Level.WARNING,
            ServletUtil.LOG_EXCEPTION_CONNECTOR_MANAGER, e);
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
}
