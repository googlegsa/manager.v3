// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Admin servlet for Authenticate
 *
 */
public class Authenticate extends ConnectorManagerServlet {
  private static final Logger LOGGER =
      Logger.getLogger(Authenticate.class.getName());

  @Override
  protected void processDoPost(
      String xmlBody, Manager manager, PrintWriter out) {
    NDC.push("AuthN");
    handleDoPost(xmlBody, manager, out);
    NDC.pop();
  }

  /**
   * Handler for doPost in order to do unit tests.
   * Writes credentials for connectors.
   *
   * @param xmlBody String the XML request body string
   * @param manager Manager
   * @param out PrintWriter where the response is written
   */
  public static void handleDoPost(
      String xmlBody, Manager manager, PrintWriter out) {
    Element root = ServletUtil.parseAndGetRootElement(
        xmlBody, ServletUtil.XMLTAG_AUTHN_REQUEST);
    if (root == null) {
      ServletUtil.writeResponse(
          out, ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return;
    }

    NodeList credList =
        root.getElementsByTagName(ServletUtil.XMLTAG_AUTHN_CREDENTIAL);
    if (credList.getLength() == 0) {
      LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_EMPTY_NODE);
      ServletUtil.writeResponse(
          out, ConnectorMessageCode.RESPONSE_EMPTY_NODE);
      return;
    }

    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHN_RESPONSE, false);

    String username = ServletUtil.getFirstElementByTagName(
      (Element) credList.item(0), ServletUtil.XMLTAG_AUTHN_USERNAME);
    NDC.pushAppend(username);

    String password = ServletUtil.getFirstElementByTagName(
        (Element) credList.item(0), ServletUtil.XMLTAG_AUTHN_PASSWORD);
    String domain = ServletUtil.getFirstElementByTagName(
        (Element) credList.item(0), ServletUtil.XMLTAG_AUTHN_DOMAIN);
    for (ConnectorStatus connector : manager.getConnectorStatuses()) {
      String connectorName = connector.getName();
      NDC.pushAppend(connectorName);
      try {
        AuthenticationIdentity identity =
            new SimpleAuthenticationIdentity(username, password, domain);
        boolean authn =
            manager.authenticate(connectorName, identity);
        if (authn) {
          ServletUtil.writeXMLTagWithAttrs(
              out, 2, ServletUtil.XMLTAG_SUCCESS,
              ServletUtil.XMLTAG_CONNECTOR_NAME + "=\"" + connectorName + "\"",
              false);
          ServletUtil.writeXMLElement(
              out, 3, ServletUtil.XMLTAG_IDENTITY, username);
          ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_SUCCESS, true);
        } else {
          ServletUtil.writeXMLTagWithAttrs(
              out, 2, ServletUtil.XMLTAG_FAILURE,
              ServletUtil.XMLTAG_CONNECTOR_NAME + "=\"" + connectorName + "\"",
              true);
        }
      } finally {
        NDC.pop();
      }
    }
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHN_RESPONSE, true);
    ServletUtil.writeRootTag(out, true);
    NDC.pop();
    return;
  }
}
