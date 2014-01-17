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

import com.google.enterprise.connector.common.SecurityUtils;
import com.google.enterprise.connector.instantiator.ExtendedConfigureResponse;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An abstract class for Connector Manager GET servlets.
 * It contains an abstract method "processDoGet".
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
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
          .allowed(RemoteAddressFilter.Access.BLACK, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    res.setContentType(ServletUtil.MIMETYPE_XML);
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();
    String connectorName = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_NAME);
    NDC.pushAppend("Config " + connectorName);
    try {
      if (connectorName == null || connectorName.length() < 1) {
        ServletUtil.writeResponse(out, new ConnectorMessageCode(
            ConnectorMessageCode.RESPONSE_NULL_CONNECTOR));
        LOGGER.warning(ServletUtil.LOG_RESPONSE_NULL_CONNECTOR);
        return;
      }

      String lang = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
      if (lang == null || lang.length() < 1) {
        lang = null;
      }

      Manager manager = Context.getInstance().getManager();

      processDoGet(connectorName, lang, manager, out);
    } finally {
      out.close();
      NDC.pop();
    }
  }

  /**
   * Returns the XML response for a given request.
   * Simply call doGet
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doGet(req, res);
  }

  public static void writeConfigureResponse(PrintWriter out,
      ConnectorMessageCode status, ConfigureResponse configRes) {
    writeConfigureResponse(out, status, configRes, true);
  }

  /**
   * Write connector configuration to XML response
   *
   * @param out PrintWriter servlet response writer
   * @param status ConnectorMessageCode the servlet call status
   * @param configRes ConfigureResponse
   * @param doObfuscate set to true to obfuscate any sensitive values in the
   *        form snippet contained in the response.
   */
  public static void writeConfigureResponse(PrintWriter out,
      ConnectorMessageCode status, ConfigureResponse configRes,
      boolean doObfuscate) {
    if (configRes != null && LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("CONFIGURE RESPONSE: message = " + configRes.getMessage());
      // TODO: Add a separate logger for formSnippet (since they are huge).
      LOGGER.finest("CONFIGURE RESPONSE: formSnippet = "
          + ((configRes.getFormSnippet() == null) ? "null" : "[...]"
             // ServletUtil.filterSensitiveData(configRes.getFormSnippet())
             ));
      LOGGER.finest("CONFIGURE RESPONSE: configData = "
          + SecurityUtils.getMaskedMap(configRes.getConfigData()));
      if (configRes instanceof ExtendedConfigureResponse) {
        LOGGER.finest("CONFIGURE RESPONSE: configXML = "
            + ((ExtendedConfigureResponse) configRes).getConfigXml());
      }
    }

    ServletUtil.writeRootTag(out, false);
    // Have to check the configRes for a well formed HTML snippet before
    // committing to given status.
    String formSnippet = null;
    if (configRes != null && configRes.getFormSnippet() != null &&
        configRes.getFormSnippet().length() > 0) {
      formSnippet = configRes.getFormSnippet();
      // formSnippet is required to contain XHTML at this point.
      if (doObfuscate) {
        formSnippet = ServletUtil.filterSensitiveData(formSnippet);
      }
      // formSnippet may now contain XHTML or HTML, depending on if
      // filterSensitiveData made any modifications.
      if (formSnippet == null) {
        // Form snippet was not well formed.  Change status to reflect XML
        // parsing error.
        status = new ConnectorMessageCode(
            ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
        configRes = null;
      } else {
        // Remove CDATA sections and properly encode their old contents. This is
        // necessary for both the XHTML and HTML cases, since Xalan as shipped
        // with Java improperly includes CDATA sections in HTML.
        formSnippet = ServletUtil.removeNestedMarkers(formSnippet);
        // Now handle any ']]>' within the content by turning them into ]]&gt;.
        // These characters are completely valid within the (X)HTML, but will
        // not be once we place the (X)HTML within the XML document in a CDATA.
        // Note that this does change the meaning of the snippet within the XML,
        // but 1) the snippet will be parsed a second time by the browser, in
        // which case a &gt; would become > and 2) the GSA proactively reverses
        // the process by replacing ]]&gt; with ]]> before sending the HTML to
        // the browser.
        //
        // The XML-way of handling this problem is by stopping and restarting
        // the CDATA section around the ]]> such that the ]] are in one section
        // and the > is in another. However, this doesn't work because the GSA
        // expects only a single CDATA section and would ignore any later
        // sections.
        formSnippet = ServletUtil.escapeEndMarkers(formSnippet);
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
            ServletUtil.XML_CDATA_START + formSnippet
            + ServletUtil.XML_CDATA_END);
      }

      if (configRes instanceof ExtendedConfigureResponse) {
        String configXml =
            ((ExtendedConfigureResponse) configRes).getConfigXml();
        if (configXml != null) {
          ServletUtil.writeXMLElement(
              out, 2, ServletUtil.XMLTAG_CONNECTOR_CONFIG_XML,
              ServletUtil.XML_CDATA_START
              + ServletUtil.escapeEndMarkers(configXml)
              + ServletUtil.XML_CDATA_END);
        }
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
