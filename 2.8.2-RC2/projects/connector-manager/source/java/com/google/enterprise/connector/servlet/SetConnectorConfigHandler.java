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

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.instantiator.Configuration;
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
 * Handler class for {@link SetConnectorConfig} servlet.
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
  private String configXml = null;
  private ConfigureResponse configRes;

  /**
   * Reads configuration data from an input XML body string, then passes
   * the configuration change on to the Manager to process.
   *
   * @param xmlBody ConnectorConfig XML body string
   * @param manager Manager the Connector Manager
   */
  public SetConnectorConfigHandler(String xmlBody, Manager manager) {
    status = new ConnectorMessageCode();

    // Avoid a bug in GSA that displays "No connector configuration
    // returned by the connector manager.", rather than the error status.
    configRes = new ConfigureResponse(null, null, null);

    if (Context.getInstance().gsaAdminRequiresPrefix()) {
      xmlBody = ServletUtil.stripCmPrefix(xmlBody);
    }
    Element root = XmlParseUtil.parseAndGetRootElement(
        xmlBody, ServletUtil.XMLTAG_CONNECTOR_CONFIG);
    if (root == null) {
      status.setMessageId(ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return;
    }

    connectorName = XmlParseUtil.getFirstElementByTagName(
        root, ServletUtil.XMLTAG_CONNECTOR_NAME);
    if (connectorName == null) {
      status.setMessageId(ConnectorMessageCode.RESPONSE_NULL_CONNECTOR);
      return;
    }
    if (XmlParseUtil.getFirstElementByTagName(root,
        ServletUtil.XMLTAG_UPDATE_CONNECTOR).equalsIgnoreCase("true")) {
      update = true;
    } else {
      // GSA 5.2 and greater wants only lowercase connector names,
      // so force all new connector names to be lower case.
      // Unfortunately, we cannot do this for existing connectors.
      connectorName = connectorName.toLowerCase();
    }
    NDC.pushAppend(connectorName);

    language = XmlParseUtil.getFirstElementByTagName(
        root, ServletUtil.QUERY_PARAM_LANG);

    connectorType = XmlParseUtil.getFirstElementByTagName(
        root, ServletUtil.XMLTAG_CONNECTOR_TYPE);

    configData = XmlParseUtil.getAllAttributes(
        root, ServletUtil.XMLTAG_PARAMETERS);

    // TODO (bmj): I am not convinced that this is an error.  Nothing states
    // that a Connector *must* have configuration properties.
    if (configData.isEmpty()) {
      status.setMessageId(ConnectorMessageCode.RESPONSE_NULL_CONFIG_DATA);
    }

    // Extract the connectorInstance.xml, if present.
    Element configXmlElement = (Element) root.getElementsByTagName(
        ServletUtil.XMLTAG_CONNECTOR_CONFIG_XML).item(0);
    if (configXmlElement != null) {
      configXml = XmlParseUtil.getCdata(configXmlElement);
      if (configXml != null) {
        configXml = ServletUtil.restoreEndMarkers(configXml);
      }
    }

    try {
      if (update) {
        Configuration previousConfig =
            manager.getConnectorConfiguration(connectorName);
        ServletUtil.replaceSensitiveData(configData, previousConfig.getMap());
        if (configXml == null &&
            connectorType.equals(previousConfig.getTypeName())) {
          configXml = previousConfig.getXml();
        }
      }

      configRes = manager.setConnectorConfiguration(connectorName,
          new Configuration(connectorType, configData, configXml),
          language, update);

      // A non-null ConfigureResponse indicates a bad configuration.
      if (configRes != null) {
        status.setMessageId(ConnectorMessageCode.INVALID_CONNECTOR_CONFIG);
      }
    } catch (ConnectorNotFoundException e) {
      status = new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND, connectorName);
      LOGGER.log(
          Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_NOT_FOUND, e);
    } catch (ConnectorExistsException e) {
      status = new ConnectorMessageCode(
          ConnectorMessageCode.EXCEPTION_CONNECTOR_EXISTS, connectorName);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_CONNECTOR_EXISTS, e);
    } catch (InstantiatorException e) {
       status.setMessageId(ConnectorMessageCode.EXCEPTION_INSTANTIATOR);
       LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_INSTANTIATOR, e);
    } catch (PersistentStoreException e) {
      status.setMessageId(
          ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
      LOGGER.log(Level.WARNING, ServletUtil.LOG_EXCEPTION_PERSISTENT_STORE, e);
    } catch (Throwable t) {
      status.setMessageId(ConnectorMessageCode.EXCEPTION_THROWABLE);
      LOGGER.log(Level.WARNING, "", t);
    }
    NDC.pop();
  }

  ConnectorMessageCode getStatus() {
    return status;
  }

  ConfigureResponse getConfigRes() {
    return configRes;
  }

  @VisibleForTesting
  String getConnectorName() {
    return connectorName;
  }

  @VisibleForTesting
  String getConnectorType() {
    return connectorType;
  }

  @VisibleForTesting
  Map<String, String> getConfigData() {
    return configData;
  }

  @VisibleForTesting
  String getConfigXml() {
    return configXml;
  }

  @VisibleForTesting
  String getLanguage() {
    return language;
  }

  @VisibleForTesting
  boolean isUpdate() {
    return update;
  }
}
