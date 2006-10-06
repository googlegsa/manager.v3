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

import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to get the config form to edit with pre-filled data
 * for a given existing connector name and language.
 * 
 */
public class GetConnectorConfigToEdit extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(
    GetConnectorConfigToEdit.class.getName());

  /**
   * Returns the connector config form with pre-filled data.
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

    PrintWriter out = res.getWriter();
    res.setContentType(ServletUtil.MIMETYPE_XML);

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    handleDoGet(out, manager, connectorName, language);
    out.close();
  }

  /**
   * Returns the connector config form with pre-filled data.
   * Just call doGet
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doGet(req, res);
  }

  public static void handleDoGet(PrintWriter out, Manager manager,
      String connectorName, String language) {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    ConfigureResponse configResponse = null;
    try {
      configResponse =
          manager.getConfigFormForConnector(connectorName, language);
    } catch (ConnectorNotFoundException e) {
      status = e.toString();
      LOGGER.info(status);
      e.printStackTrace();
    } catch (InstantiatorException e) {
      status = e.toString();
      LOGGER.info(status);
      e.printStackTrace();
    }

    if (configResponse == null || configResponse.getFormSnippet().length() < 1) {
      ServletUtil.writeSimpleResponse(
          out, ServletUtil.XML_RESPONSE_STATUS_NULL_FORM_SNIPPET);
      return;
    }
    ServletUtil.writeConfigureResponse(out, status, configResponse);
  }
}
