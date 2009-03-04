// Copyright (C) 2006-2008 Google Inc.
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

import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test SetConnectorConfig servlet through a browser.
 *
 */
public class SetConnectorConfigNoGSA extends HttpServlet {
  private static final Logger LOGGER =
    Logger.getLogger(SetConnectorConfigNoGSA.class.getName());

  /**
   * Returns the connector config form for given connector type.
   * @param req
   * @param res
   * @throws IOException
   *
   */
  protected void doGet(HttpServletRequest req,
                       HttpServletResponse res)
      throws IOException {
    String connectorTypeName = req.getParameter(
        ServletUtil.XMLTAG_CONNECTOR_TYPE);
    String language = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
    res.setContentType(ServletUtil.MIMETYPE_HTML);
    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();

    ConfigureResponse configResponse = null;
    try {
      configResponse = manager.getConfigForm(connectorTypeName, language);
    } catch (ConnectorTypeNotFoundException e) {
      ServletUtil.writeResponse(
          out, ConnectorMessageCode.RESPONSE_NULL_CONNECTOR_TYPE);
      LOGGER.log(
          Level.WARNING, ServletUtil.LOG_RESPONSE_NULL_CONNECTOR_TYPE, e);
      out.close();
      return;
    } catch (InstantiatorException e) {
      ServletUtil.writeResponse(
          out, ConnectorMessageCode.EXCEPTION_INSTANTIATOR);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_INSTANTIATOR, e);
      out.close();
      return;
    }

    out.println("<HTML><HEAD><TITLE>Set Connector Config</TITLE></HEAD>");
    out.println("<BODY><H3>Connector Config:</H3>");
    out.println("<HR><FORM METHOD=POST " +
        "ACTION=\"/connector-manager/setConnectorConfigTest?" +
        ServletUtil.XMLTAG_CONNECTOR_TYPE + "=" + connectorTypeName +
        "&lang=" + language + "\"><TABLE>");
    out.println("<tr><td>Connector Name</td><td>" +
        "<INPUT TYPE=\"TEXT\" NAME=\"connectorName\"></td></tr>");
    out.println("<tr><td>Connector Type</td><td>" +
        "<INPUT TYPE=\"TEXT\" NAME=\"connectorType\" " +
        "VALUE=\"" + connectorTypeName + "\"></td></tr>");

    String formSnippet = null;
    if (configResponse == null || configResponse.getFormSnippet() == null) {
      formSnippet = ServletUtil.DEFAULT_FORM;
    } else {
      formSnippet = configResponse.getFormSnippet();
    }
    out.println(formSnippet);
    out.println("<tr><td><INPUT TYPE=\"SUBMIT\" NAME=\"action\"" +
        "VALUE=\"submit\"></td></tr>");
    out.println("</TABLE></FORM></BODY></HTML>");

    out.close();
  }

  /**
   * Returns the simple response if successfully setting the connector config.
   * @param req
   * @param res
   * @throws IOException
   *
   */
  protected void doPost(HttpServletRequest req,
                        HttpServletResponse res)
      throws IOException {
    String lang = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    String connectorType = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_TYPE);
    StringWriter writer = new StringWriter();
    writer.write("<" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">");
    writer.write("  <" + ServletUtil.QUERY_PARAM_LANG + ">"
        + lang + "</" + ServletUtil.QUERY_PARAM_LANG + ">\n");
    writer.write("  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">"
        + connectorName + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">");
    writer.write("  <" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">"
        + connectorType + "</" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">");
    writer.write("  <" + ServletUtil.XMLTAG_PARAMETERS
        + " name=\"name1\" value=\"" + req.getParameter("name1") + "\"/>");
    writer.write("  <" + ServletUtil.XMLTAG_PARAMETERS
        + " name=\"name2\" value=\"" + req.getParameter("name2") + "\"/>");
    writer.write("  <" + ServletUtil.XMLTAG_PARAMETERS
        + " name=\"name3\" value=\"" + req.getParameter("name3") + "\"/>");
    writer.write("</" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">");
    writer.close();

    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    SetConnectorConfigHandler handler = new SetConnectorConfigHandler(
        writer.getBuffer().toString(), manager);
    ConfigureResponse configRes = handler.getConfigRes();
    ConnectorMessageCode status = (configRes == null) ? handler.getStatus() :
      new ConnectorMessageCode(ConnectorMessageCode.INVALID_CONNECTOR_CONFIG);

    ConnectorManagerGetServlet.writeConfigureResponse(
        out, status, configRes);
    out.close();
  }
}
