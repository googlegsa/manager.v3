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

import com.google.common.base.Strings;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.XmlUtils;
import com.google.enterprise.connector.util.XmlParseUtil;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    NDC.append("AuthN");
    handleDoPost(xmlBody, manager, out);
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
    Element root = XmlParseUtil.parseAndGetRootElement(
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

    Set<String> requestedConnectors = null;
    NodeList connectorList =
        root.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_NAME);
    if (connectorList.getLength() > 0) {
      requestedConnectors = new HashSet<String>();
      for (int i = 0; i < connectorList.getLength(); i++) {
        String name = connectorList.item(i).getTextContent();
        if (name != null) {
          requestedConnectors.add(name);
        }
      }
    }

    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHN_RESPONSE, false);

    String username = XmlParseUtil.getFirstElementByTagName(
      (Element) credList.item(0), ServletUtil.XMLTAG_AUTHN_USERNAME);
    String domain = XmlParseUtil.getFirstElementByTagName(
        (Element) credList.item(0), ServletUtil.XMLTAG_AUTHN_DOMAIN);
    NDC.append(Strings.isNullOrEmpty(domain) ? username
               : (domain + "/" + username));

    String password = XmlParseUtil.getOptionalElementByTagName(
        (Element) credList.item(0), ServletUtil.XMLTAG_AUTHN_PASSWORD);
    for (ConnectorStatus connector : manager.getConnectorStatuses()) {
      String connectorName = connector.getName();
      if (requestedConnectors != null &&
          !requestedConnectors.contains(connectorName)) {
        continue;
      }
      NDC.pushAppend(connectorName);
      try {
        AuthenticationIdentity identity =
            new SimpleAuthenticationIdentity(username, password, domain);
        AuthenticationResponse response =
            manager.authenticate(connectorName, identity);
        if (response.isValid()) {
          ServletUtil.writeXMLTagWithAttrs(
              out, 2, ServletUtil.XMLTAG_SUCCESS,
              ServletUtil.XMLTAG_CONNECTOR_NAME + "=\"" + connectorName + "\"",
              false);
          // TODO: Either fix ServletUtil XML code to XML escape attr values and
          // element text bodies, or add the ability to append attributes to
          // XmlUtils.appendStartTag().
          out.append(ServletUtil.indentStr(3));
          XmlUtils.xmlAppendStartTag(ServletUtil.XMLTAG_IDENTITY, out);
          XmlUtils.xmlAppendAttrValue(username, out);
          XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_IDENTITY, out);

          // Add any returned groups that the user may belong to.
          if (response.getGroups() != null) {
            for (Object item : response.getGroups()) {
              Principal group = (item instanceof String) ?
                  new Principal((String) item) : (Principal) item;
              out.append(ServletUtil.indentStr(3));
              out.append('<').append(ServletUtil.XMLTAG_GROUP);
              if (group.getPrincipalType() ==
                  SpiConstants.PrincipalType.UNQUALIFIED) {
                // UNQUALIFIED is a special-case on the GSA to allow us to
                // prevent the GSA from mistakeningly finding a domain in the
                // principal name.
                XmlUtils.xmlAppendAttr(
                    ServletUtil.XMLTAG_PRINCIPALTYPE_ATTRIBUTE,
                    SpiConstants.PrincipalType.UNQUALIFIED.toString(), out);
              }
              if (!Strings.isNullOrEmpty(group.getNamespace())) {
                XmlUtils.xmlAppendAttr(ServletUtil.XMLTAG_NAMESPACE_ATTRIBUTE,
                    group.getNamespace(), out);
              }
              out.append('>');
              XmlUtils.xmlAppendAttrValue(group.getName(), out);
              XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_GROUP, out);
            }
          }
          ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_SUCCESS, true);
        } else {
          ServletUtil.writeXMLTagWithAttrs(
              out, 2, ServletUtil.XMLTAG_FAILURE,
              ServletUtil.XMLTAG_CONNECTOR_NAME + "=\"" + connectorName + "\"",
              true);
        }
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Error writing Authentication Response", e);
      } finally {
        NDC.pop();
      }
    }
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHN_RESPONSE, true);
    ServletUtil.writeRootTag(out, true);
    return;
  }
}
