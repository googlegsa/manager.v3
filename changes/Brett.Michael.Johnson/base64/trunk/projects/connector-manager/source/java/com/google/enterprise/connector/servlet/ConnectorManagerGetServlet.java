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

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An abstract class for Connector Manager GET servlets.
 * It contains an abstract method "processDoGet".
 *
 */
public abstract class ConnectorManagerGetServlet extends HttpServlet {

  private static final Logger LOGGER =
      Logger.getLogger(ConnectorManagerGetServlet.class.getName());

  /**
   * This abstract method processes servlet-specific GET request,
   * make servlet-specific call to the connector manager and write the
   * XML response body.
   *
   * @param connectorName String the parameter for connector name
   * @param lang String the parameter for language
   * @param manager Manager
   * @param out PrintWriter where the XML response body is written
   */
  protected abstract void processDoGet(
      String connectorName, String lang, Manager manager, PrintWriter out);

  /**
   * Returns the XML response for a given request.
   *
   * @param req
   * @param res
   * @throws IOException
   *
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.setContentType(ServletUtil.MIMETYPE_XML);
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();
    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    if (connectorName == null || connectorName.length() < 1) {
      ServletUtil.writeResponse(out, new ConnectorMessageCode(
          ConnectorMessageCode.RESPONSE_NULL_CONNECTOR));
      LOGGER.warning(ServletUtil.LOG_RESPONSE_NULL_CONNECTOR);
      out.close();
      return;
    }

    String lang = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
    if (lang == null || lang.length() < 1) {
      lang = null;
    }

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();

    processDoGet(connectorName, lang, manager, out);
    out.close();
  }

  /**
   * Returns the XML response for a given request.
   * Simply call doGet
   *
   * @param req
   * @param res
   * @throws IOException
   *
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    doGet(req, res);
  }

  /**
   * Write connector configuration to XML response
   *
   * @param out PrintWriter servlet response writer
   * @param status ConnectorMessageCode the servlet call status
   * @param configRes ConfigureResponse
   */
  public static void writeConfigureResponse(PrintWriter out,
      ConnectorMessageCode status, ConfigureResponse configRes) {
    ServletUtil.writeRootTag(out, false);
    // Have to check the configRes for a well formed HTML snippet before
    // committing to given status.
    String formSnippet = null;
    if (configRes != null && configRes.getFormSnippet() != null &&
        configRes.getFormSnippet().length() > 0) {
      formSnippet = configRes.getFormSnippet();
      if (Context.getInstance().gsaAdminRequiresPrefix()) {
        formSnippet = ServletUtil.prependCmPrefix(formSnippet);
      }
      formSnippet = ServletUtil.filterSensitiveData(formSnippet);
      if (formSnippet == null) {
        // Form snippet was not well formed.  Change status to reflect XML
        // parsing error.
        status = new ConnectorMessageCode(
            ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
        configRes = null;
      }
    }
    // Now write out the response.
    ServletUtil.writeMessageCode(out, status);
    if (configRes != null) {
      ServletUtil.writeXMLTag(
          out, 1, ServletUtil.XMLTAG_CONFIGURE_RESPONSE, false);
      if (formSnippet != null) {
        ServletUtil.writeXMLElement(
            out, 2, ServletUtil.XMLTAG_FORM_SNIPPET,
            "<![CDATA[" + formSnippet + "]]>");
      }
      if (configRes.getMessage() != null &&
          configRes.getMessage().length() > 0) {
        ServletUtil.writeXMLElement(
            out, 2, ServletUtil.XMLTAG_MESSAGE, configRes.getMessage());
      }
      ServletUtil.writeXMLTag(
          out, 1, ServletUtil.XMLTAG_CONFIGURE_RESPONSE, true);
    }
    ServletUtil.writeRootTag(out, true);
  }

}
