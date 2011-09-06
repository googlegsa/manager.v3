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
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.util.XmlParseUtil;

import org.w3c.dom.Element;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler class for SetConnectorConfig servlet class.
 *
 */
public class SetConnectorConfigHandler {
  private static final Logger LOGGER =
    Logger.getLogger(SetConnectorConfigHandler.class.getName());

  private ConnectorMessageCode status;
  private String language = null;
  private String connectorName;
  private String connectorType = null;
  private boolean update = false;
  private Map<String, String> configData = null;
  private ConfigureResponse configRes;

  /*
   * Reads from an input XML body string
   * @param manager Manager
   * @param xmlBody String Input XML body string.
   */
  public SetConnectorConfigHandler(String xmlBody, Manager manager) {
    this.status = new ConnectorMessageCode();
    if (Context.getInstance().gsaAdminRequiresPrefix()) {
      xmlBody = ServletUtil.stripCmPrefix(xmlBody);
    }
    Element root = XmlParseUtil.parseAndGetRootElement(
        xmlBody, ServletUtil.XMLTAG_CONNECTOR_CONFIG);
    if (root == null) {
      this.status.setMessageId(ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return;
    }

    this.language = XmlParseUtil.getFirstElementByTagName(
        root, ServletUtil.QUERY_PARAM_LANG);
    this.connectorName = XmlParseUtil.getFirstElementByTagName(
        root, ServletUtil.XMLTAG_CONNECTOR_NAME);
    if (this.connectorName == null) {
      this.status.setMessageId(ConnectorMessageCode.RESPONSE_NULL_CONNECTOR);
      return;
    }
    this.connectorType = XmlParseUtil.getFirstElementByTagName(
        root, ServletUtil.XMLTAG_CONNECTOR_TYPE);
    if (XmlParseUtil.getFirstElementByTagName(root,
        ServletUtil.XMLTAG_UPDATE_CONNECTOR).equalsIgnoreCase("true")) {
      this.update = true;
    } else {
      // GSA 5.2 and greater wants only lowercase connector names,
      // so force all new connector names to be lower case.
      // Unfortunately, we cannot do this for existing connectors.
      this.connectorName = this.connectorName.toLowerCase();
    }
    NDC.pushAppend(this.connectorName);
    this.configData = XmlParseUtil.getAllAttributes(
        root, ServletUtil.XMLTAG_PARAMETERS);
    if (this.configData.isEmpty()) {
      this.status.setMessageId(ConnectorMessageCode.RESPONSE_NULL_CONFIG_DATA);
    }
    if (update) {
      try {
        Map<String, String> previousConfigData =
            manager.getConnectorConfig(connectorName);
        ServletUtil.replaceSensitiveData(configData, previousConfigData);
      } catch (ConnectorNotFoundException unexpected) {
        // Trying to update a connector that is not currently known to the
        // manager.  This case is handled below so dropping through for now.
      }
    }

    this.configRes = null;
    try {
      this.configRes = manager.setConnectorConfig(this.connectorName,
          this.connectorType, this.configData, this.language, this.update);
    } catch (ConnectorNotFoundException e) {
      this.status = new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND, connectorName);
      LOGGER.log(
          Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_NOT_FOUND, e);
    } catch (ConnectorExistsException e) {
      this.status = new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_CONNECTOR_EXISTS, connectorName);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_EXISTS, e);
    } catch (InstantiatorException e) {
       this.status.setMessageId(ConnectorMessageCode.EXCEPTION_INSTANTIATOR);
       LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_INSTANTIATOR, e);
    } catch (PersistentStoreException e) {
      this.status.setMessageId(
          ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_PERSISTENT_STORE, e);
    } catch (Throwable t) {
      this.status.setMessageId(ConnectorMessageCode.EXCEPTION_THROWABLE);
      LOGGER.log(Level.WARNING, "", t);
    }
    NDC.pop();
  }

  public ConnectorMessageCode getStatus() {
    return status;
  }

  public Map<String, String> getConfigData() {
    return configData;
  }

  public ConfigureResponse getConfigRes() {
    return configRes;
  }

  public String getConnectorName() {
    return connectorName;
  }

  public String getConnectorType() {
    return connectorType;
  }

  public String getLanguage() {
    return language;
  }

  public boolean isUpdate() {
    return update;
  }
}
