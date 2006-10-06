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
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to set connector config.
 * 
 */
public class SetConnectorConfig extends HttpServlet {
  private static final Logger LOG =
    Logger.getLogger(SetConnectorConfig.class.getName());

  /**
   * doGet just call doPost.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doGet(HttpServletRequest req,
                       HttpServletResponse res)
      throws ServletException, IOException {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    String language = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
    String connectorType = req.getParameter(ServletUtil.XMLTAG_CONNECTOR_TYPE);
    PrintWriter out = res.getWriter();
    res.setContentType(ServletUtil.MIMETYPE_XML);

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    String formSnippet = null;
    ConfigureResponse configResponse = null;
    try {
      configResponse = manager.getConfigForm(connectorType, language);
      if (configResponse != null) {
        formSnippet = configResponse.getFormSnippet();
      }
    } catch (ConnectorTypeNotFoundException e) {
      status = e.toString();
      ServletUtil.writeSimpleResponse(out, status);
      LOG.info(status);
      e.printStackTrace();
    }

    if (formSnippet == null) {
      formSnippet = ServletUtil.DEFAULT_FORM;
    }

    GetConfigForm.handleDoGet(out, status, configResponse);
    out.close();
  }

  /**
   * Returns the simple response if successfully setting the connector config.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doPost(HttpServletRequest req,
                        HttpServletResponse res)
      throws ServletException, IOException {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    // The GoogleHttpClient does not allow it. 
    // req.getParameter("lang"); would fail with empty xmlBody.
    // We decided to have <lang> inside of post xmlBody.
    BufferedReader reader = req.getReader();
    PrintWriter out = res.getWriter();
    res.setContentType(ServletUtil.MIMETYPE_XML);
    String xmlBody = StringUtils.readAllToString(reader);
    xmlBody = ServletUtil.stripCmPrefix(xmlBody);
    if (xmlBody.length() < 1) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_REQUEST;
      ServletUtil.writeSimpleResponse(out, status);
      LOG.info("The request is empty");
      return;
    }

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    SetConnectorConfigHandler handler =
        new SetConnectorConfigHandler(manager, xmlBody);
    ServletUtil.writeConfigureResponse(
        out, handler.getStatus(), handler.getConfigRes());
    out.close();
  }
}
