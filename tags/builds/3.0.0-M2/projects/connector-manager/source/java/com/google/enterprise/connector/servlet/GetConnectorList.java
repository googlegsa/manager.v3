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

import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConnectorType;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to get a list of connector types.
 */
public class GetConnectorList extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(GetConnectorList.class.getName());

  /**
   * Returns a list of connector types.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doPost(req, res);
  }

  /**
   * Returns a list of connector types.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    // Make sure this requester is OK
    if (!RemoteAddressFilter.getInstance()
          .allowed(RemoteAddressFilter.Access.BLACK, req.getRemoteAddr())) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    NDC.pushAppend("Config");
    try {
      Manager manager = Context.getInstance().getManager();
      handleDoPost(manager, out);
    } finally {
      out.close();
      NDC.pop();
    }
  }

  /**
   * Handler for doGet in order to do unit tests.
   *
   * @param manager a Manager
   * @param out PrintWriter where the response is written
   */
  public static void handleDoPost(Manager manager, PrintWriter out) {
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeManagerSplash(out);

    Set<String> connectorTypes = manager.getConnectorTypeNames();
    if (connectorTypes == null || connectorTypes.size() == 0) {
      ServletUtil.writeStatusId(
          out, ConnectorMessageCode.RESPONSE_NULL_CONNECTOR_TYPE);
      ServletUtil.writeRootTag(out, true);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_NULL_CONNECTOR_TYPE);
      return;
    }

    ServletUtil.writeStatusId(out, ConnectorMessageCode.SUCCESS);
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_TYPES, false);
    for (String typeName : connectorTypes) {
      String version = null;
      try {
        ConnectorType connectorType = manager.getConnectorType(typeName);
        version = JarUtils.getJarVersion(connectorType.getClass());
      } catch (ConnectorTypeNotFoundException e) {
        // The JUnit tests might not have actual ConnectorTypes.
        LOGGER.warning("Connector type not found: " + typeName);
      }
      if (version != null && version.length() > 0) {
        // Write out the Connector version as an attribute on the tag.
        StringBuilder buffer = new StringBuilder();
        ServletUtil.writeXMLTagWithAttrs(buffer, 2,
            ServletUtil.XMLTAG_CONNECTOR_TYPE,
            ServletUtil.ATTRIBUTE_VERSION + version + ServletUtil.QUOTE,
            false);
        buffer.append(typeName);
        ServletUtil.writeXMLTag(buffer, 0,
            ServletUtil.XMLTAG_CONNECTOR_TYPE, true);
        out.println(buffer.toString());
      } else {
        ServletUtil.writeXMLElement(out, 2,
            ServletUtil.XMLTAG_CONNECTOR_TYPE, typeName);
      }
    }

    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_TYPES, true);
    ServletUtil.writeRootTag(out, true);
  }
}
