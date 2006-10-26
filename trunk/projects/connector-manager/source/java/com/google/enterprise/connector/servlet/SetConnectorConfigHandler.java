//Copyright (C) 2006 Google Inc.
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

import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.spi.ConfigureResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

  private String status = ServletUtil.XML_RESPONSE_SUCCESS;
  private String language;
  private String connectorName;
  private String connectorType;
  private boolean update = false;
  private Map configData;
  private ConfigureResponse configRes;

  /*
   * Reads from an input XML body string
   * @param manager Manager
   * @param language String.
   * @param xmlBody String Input XML body string.
   */
  public SetConnectorConfigHandler(Manager manager, String xmlBody) {
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(xmlBody, errorHandler);
    NodeList nodeList =
        document.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_CONFIG);
    if (nodeList.getLength() == 0) {
      this.status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE;
      LOGGER.log(Level.WARNING, ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE);
      return;
    }

    this.language = ServletUtil.getFirstElementByTagName(
        (Element) nodeList.item(0), ServletUtil.QUERY_PARAM_LANG);
    this.connectorName = ServletUtil.getFirstElementByTagName(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_CONNECTOR_NAME);
    if (this.connectorName == null) {
      this.status = ServletUtil.XML_RESPONSE_STATUS_NULL_CONNECTOR;
      return;
    }
    this.connectorType = ServletUtil.getFirstElementByTagName(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_CONNECTOR_TYPE);
    if (ServletUtil.getFirstElementByTagName((Element) nodeList.item(0),
        ServletUtil.XMLTAG_UPDATE_CONNECTOR).equalsIgnoreCase("true")) {
      this.update = true;
    }
    this.configData = ServletUtil.getAllAttributes(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_PARAMETERS);
    if (this.configData.isEmpty()) {
      this.status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_CONFIG_DATA;
    }

    this.configRes = null;
    try {
      this.configRes = manager.setConnectorConfig(this.connectorName,
          this.connectorType, this.configData, this.language, this.update);
    } catch (ConnectorManagerException e) {
      status = e.getMessage();
      LOGGER.log(Level.WARNING, e.getMessage(), e);
    } catch (Throwable t) {
      LOGGER.log(Level.WARNING, "", t);
    }
  }

  public String getStatus() {
    return status;
  }

  public Map getConfigData() {
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
