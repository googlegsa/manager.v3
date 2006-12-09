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
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to set connector config.
 * 
 */
public class SetConnectorConfig extends ConnectorManagerServlet {
  private static final Logger LOGGER =
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
    ConnectorMessageCode status = new ConnectorMessageCode();
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
      ServletUtil.writeResponse(
          out, ConnectorMessageCode.EXCEPTION_CONNECTOR_TYPE_NOT_FOUND);
      LOGGER.log(Level.WARNING,
          ServletUtil.LOG_EXCEPTION_CONNECTOR_TYPE_NOT_FOUND, e);
    }

    if (formSnippet == null) {
      formSnippet = ServletUtil.DEFAULT_FORM;
    }

    GetConfigForm.handleDoGet(configResponse, status, out);
    out.close();
  }

  /**
   * Writes the XML response for setting the connector config.
   */
  /*
   * (non-Javadoc)
   * @see com.google.enterprise.connector.servlet.ConnectorManagerServlet
   * #processDoPost(java.lang.String,
   * com.google.enterprise.connector.manager.Manager, java.io.PrintWriter)
   */
  protected void processDoPost(
      String xmlBody, Manager manager, PrintWriter out) {
    SetConnectorConfigHandler handler =
        new SetConnectorConfigHandler(xmlBody, manager);
    ConnectorManagerGetServlet.writeConfigureResponse(
        out, handler.getStatus(), handler.getConfigRes());
    out.close();
  }

}
