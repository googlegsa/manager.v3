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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.PersistentStoreException;

import java.util.logging.Logger;

/**
 * Handler class for SetManagerConfig servlet class.
 *
 */
public class SetManagerConfigHandler {
  private static final Logger LOG =
    Logger.getLogger(SetManagerConfigHandler.class.getName());

  private String status = ServletUtil.XML_RESPONSE_SUCCESS;
  private boolean certAuth;
  private int maxFeedRate;
  private String feederGateHost;
  private int feederGatePort;

  /*
   * Reads from an input XML body string
   * @param xmlBody String Input XML body string.
   */
  public SetManagerConfigHandler(Manager manager, String xmlBody) {
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(xmlBody, errorHandler);
    NodeList nodeList =
        document.getElementsByTagName(ServletUtil.XMLTAG_MANAGER_CONFIG);
    if (nodeList.getLength() == 0) {
      this.status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE;
      LOG.info(ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE);
    }

    if (ServletUtil.getFirstElementByTagName((Element) nodeList.item(0),
        ServletUtil.XMLTAG_CERT_AUTHN).equalsIgnoreCase("true")) {
      this.certAuth = true;
    }
    this.feederGateHost = ServletUtil.getFirstAttribute(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_FEEDERGATE,
         ServletUtil.XMLTAG_FEEDERGATE_HOST);
    this.feederGatePort = Integer.parseInt(ServletUtil.getFirstAttribute(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_FEEDERGATE,
        ServletUtil.XMLTAG_FEEDERGATE_PORT));
    try {
      manager.setConnectorManagerConfig(this.certAuth, this.feederGateHost,
          this.feederGatePort, this.maxFeedRate);
    } catch (PersistentStoreException e) {
      LOG.info("PersistentStoreException");
      this.status = e.toString();
      e.printStackTrace();
    }
  }

  public boolean isCertAuth() {
    return certAuth;
  }

  public String getFeederGateHost() {
    return feederGateHost;
  }

  public int getFeederGatePort() {
    return feederGatePort;
  }

  public int getMaxFeedRate() {
    return maxFeedRate;
  }

  public String getStatus() {
    return status;
  }
}

