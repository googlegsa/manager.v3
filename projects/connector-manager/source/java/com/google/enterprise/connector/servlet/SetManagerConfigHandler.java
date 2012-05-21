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

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.util.XmlParseUtil;

import org.w3c.dom.Element;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler class for SetManagerConfig servlet class.
 */
public class SetManagerConfigHandler {
  private static final Logger LOGGER =
      Logger.getLogger(SetManagerConfigHandler.class.getName());

  private ConnectorMessageCode status = new ConnectorMessageCode();
  private String feederGateProtocol = null;
  private String feederGateHost = null;
  private int feederGatePort = 0;
  private int feederGateSecurePort = Context.GSA_FEED_SECURE_PORT_INVALID;
  private final String connectorManagerUrl;

  /**
   * Reads from a request input XML string.
   *
   * @param manager Manager
   * @param xmlBody String Input XML body string.
   * @param connectorManagerUrl URL string for the Connector Manager servlet
   */
  public SetManagerConfigHandler(Manager manager, String xmlBody,
                                 String connectorManagerUrl) {
    this.status = new ConnectorMessageCode();
    this.connectorManagerUrl = connectorManagerUrl;
    Element root = XmlParseUtil.parseAndGetRootElement(
        xmlBody, ServletUtil.XMLTAG_MANAGER_CONFIG);
    if (root == null) {
      this.status = new ConnectorMessageCode(
          ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return;
    }

    // Get settings from request.
    this.feederGateProtocol = XmlParseUtil.getFirstAttribute(
        root, ServletUtil.XMLTAG_FEEDERGATE,
        ServletUtil.XMLTAG_FEEDERGATE_PROTOCOL);
    this.feederGateHost = XmlParseUtil.getFirstAttribute(
        root, ServletUtil.XMLTAG_FEEDERGATE,
        ServletUtil.XMLTAG_FEEDERGATE_HOST);
    this.feederGatePort = Integer.parseInt(XmlParseUtil.getFirstAttribute(
        root, ServletUtil.XMLTAG_FEEDERGATE,
        ServletUtil.XMLTAG_FEEDERGATE_PORT));
    String securePort = XmlParseUtil.getFirstAttribute(
        root, ServletUtil.XMLTAG_FEEDERGATE,
        ServletUtil.XMLTAG_FEEDERGATE_SECURE_PORT);
    this.feederGateSecurePort = (Strings.isNullOrEmpty(securePort))
        ? Context.GSA_FEED_SECURE_PORT_INVALID : Integer.parseInt(securePort);

    // Bail if the manager is currently locked and the request is
    // trying to change something.
    if (manager.isLocked()) {
      Properties currentSettings;
      try {
        currentSettings = manager.getConnectorManagerConfig();
      } catch (PersistentStoreException e) {
        this.status = new ConnectorMessageCode(
            ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
        LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_PERSISTENT_STORE,
            e);
        return;
      }
      String currentFeedProtocol = currentSettings.getProperty(
          Context.GSA_FEED_PROTOCOL_PROPERTY_KEY);
      String currentFeedHost = currentSettings.getProperty(
          Context.GSA_FEED_HOST_PROPERTY_KEY);
      String currentFeedPort = currentSettings.getProperty(
          Context.GSA_FEED_PORT_PROPERTY_KEY,
          Context.GSA_FEED_PORT_DEFAULT);
      String currentFeedSecurePort = currentSettings.getProperty(
          Context.GSA_FEED_SECURE_PORT_PROPERTY_KEY,
          Context.GSA_FEED_SECURE_PORT_DEFAULT);
      if ((!Strings.isNullOrEmpty(feederGateHost)
              && !feederGateHost.equals(currentFeedHost))
          || feederGatePort != Integer.parseInt(currentFeedPort)
          || (!Strings.isNullOrEmpty(feederGateProtocol)
              && !feederGateProtocol.equals(currentFeedProtocol))
          || (feederGateSecurePort != Context.GSA_FEED_SECURE_PORT_INVALID
              && feederGateSecurePort !=
                  Integer.parseInt(currentFeedSecurePort))) {
        String message = "Attempt has been made to change configuration on a"
            + " locked Connector Manager. You must update the locked property"
            + " on the Connector Manager before continuing.\n"
            + "Request: feederGateHost=" + feederGateHost
            + "; feederGatePort=" + feederGatePort;
        if (!Strings.isNullOrEmpty(feederGateProtocol)) {
          message += "; feederGateProtocol=" + feederGateProtocol;
        }
        if (feederGateSecurePort != Context.GSA_FEED_SECURE_PORT_INVALID) {
          message += "; feederGateSecurePort=" + feederGateSecurePort;
        }
        LOGGER.warning(message);
        this.status = new ConnectorMessageCode(
            ConnectorMessageCode.ATTEMPT_TO_CHANGE_LOCKED_CONNECTOR_MANAGER);
        return;
      }
    }

    // If we get here, update the manager configuration.
    try {
      manager.setConnectorManagerConfig(this.feederGateProtocol,
          this.feederGateHost, this.feederGatePort, this.feederGateSecurePort,
          this.connectorManagerUrl);
    } catch (PersistentStoreException e) {
      this.status = new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_PERSISTENT_STORE, e);
    }
  }

  /** For the unit tests. */
  String getConnectorManagerUrl() {
    return connectorManagerUrl;
  }

  /** For the unit tests. */
  String getFeederGateProtocol() {
    return feederGateProtocol;
  }

  /** For the unit tests. */
  String getFeederGateHost() {
    return feederGateHost;
  }

  /** For the unit tests. */
  int getFeederGatePort() {
    return feederGatePort;
  }

  /** For the unit tests. */
  int getFeederGateSecurePort() {
    return feederGateSecurePort;
  }

  /** For the unit tests. */
  ConnectorMessageCode getStatus() {
    return status;
  }
}
