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

import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Admin servlet to get the config form for a given connector type and language.
 */
public class GetConfigForm extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(GetConfigForm.class.getName());

  /**
   * Returns the connector config form.
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

    String connectorTypeName =
        req.getParameter(ServletUtil.XMLTAG_CONNECTOR_TYPE);
    res.setContentType(ServletUtil.MIMETYPE_XML);
    res.setCharacterEncoding("UTF-8");
    PrintWriter out = res.getWriter();

    NDC.pushAppend("Config " + connectorTypeName);
    try {
      String language = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
      if (language == null || language.length() < 1) {
        LOGGER.log(Level.WARNING, "Language is null");
        language = ServletUtil.DEFAULT_LANGUAGE;
      }

      Manager manager = Context.getInstance().getManager();
      handleDoGet(connectorTypeName, language, manager, out);

    } finally {
      out.close();
      NDC.pop();
    }
  }

  /**
   * Returns the connector config form.
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

  /**
   * Handler for doGet in order to do unit tests.
   *
   * @param connectorTypeName The name of a connector implementation - it should
   *        be one that this manager knows about (one that would be returned by
   *        a call to getConnectorTypes()).
   * @param language A locale string, such as "en" or "fr_CA" which the
   *        implementation may use to produce appropriate descriptions and
   *        messages
   * @param manager the Connector Manager
   * @param out PrintWriter where the response is written
   */
  public static void handleDoGet(String connectorTypeName, String language,
      Manager manager, PrintWriter out) {
    ConnectorMessageCode status = new ConnectorMessageCode();

    if (connectorTypeName == null || connectorTypeName.length() < 1) {
      status.setMessageId(ConnectorMessageCode.RESPONSE_NULL_CONNECTOR_TYPE);
      ServletUtil.writeResponse(out, status);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_NULL_CONNECTOR_TYPE);
      return;
    }

    try {
      ConfigureResponse configResponse =
          manager.getConfigForm(connectorTypeName, language);
      ConnectorManagerGetServlet.writeConfigureResponse(out, status, configResponse);
    } catch (ConnectorTypeNotFoundException e) {
      status = new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_CONNECTOR_TYPE_NOT_FOUND,
          connectorTypeName);
      ServletUtil.writeResponse(out, status);
      LOGGER.log(Level.WARNING,
          ServletUtil.LOG_EXCEPTION_CONNECTOR_TYPE_NOT_FOUND, e);
    } catch (InstantiatorException e) {
      status.setMessageId(ConnectorMessageCode.EXCEPTION_INSTANTIATOR);
      ServletUtil.writeResponse(out, status);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_INSTANTIATOR, e);
    }
  }
}
