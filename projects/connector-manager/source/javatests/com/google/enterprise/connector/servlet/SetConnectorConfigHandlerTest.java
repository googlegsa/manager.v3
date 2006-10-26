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

import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Tests SetConnectorConfigHandlerTest class for SetConnectorConfig
 * servlet class.
 *
 */
public class SetConnectorConfigHandlerTest extends TestCase {
  private static final Logger LOG =
      Logger.getLogger(SetConnectorConfigHandlerTest.class.getName());
  private String language;
  private String connectorName;
  private String connectorType;
  private Map configData;
  private boolean update;

  public void testSetConnectorConfigHandler1() {
    language = "en";
    connectorName = "connectorA";
    connectorType = "documentum";
    configData = new TreeMap();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    doTest(setXMLBody());
  }

  public void testSetConnectorConfigHandler2() {
    language = "en";
    connectorName = "";
    connectorType = "documentum";
    configData = new TreeMap();
    configData.put("name1", "valueB1");
    configData.put("name2", "valueB2");
    configData.put("name3", "valueB3");
    doTest(setXMLBody());
  }

  public void testSetConnectorConfigHandler3() {
    language = "en";
    connectorName = "connectorC";
    connectorType = "documentum";
    update = true;
    configData = new TreeMap();
    doTest(setXMLBody());
  }

  public void testSetConnectorConfigHandler4() {
    language = "en";
    connectorName = "connectorC";
    connectorType = "documentum";
    update = true;
    configData = new TreeMap();
    configData.put("name1", "valueB1");
    configData.put("name2", "valueB2");
    configData.put("name3", "valueB3");
    doTest(setXMLBody());
  }

  public void testSetConnectorConfigHandler5() {
    language = "en";
    connectorName = "connectorC";
    connectorType = "documentum";
    update = false;
    configData = new TreeMap();
    configData.put("name1", "valueB1");
    configData.put("name2", "valueB2");
    configData.put("name3", "valueB3");
    doTest(setXMLBody());
  }

  private void doTest(String xmlBody) {
    LOG.info("xmlBody: " + xmlBody);
    Manager manager = MockManager.getInstance();
    SetConnectorConfigHandler hdl = new SetConnectorConfigHandler(
        manager, xmlBody);
    LOG.info("ConnectorName: " + hdl.getConnectorName() + " this: " + this.connectorName);
    LOG.info("ConnectorType: " + hdl.getConnectorType() + " this: " + this.connectorType);
    if (hdl.getStatus().equals(ServletUtil.XML_RESPONSE_SUCCESS)) {
      Assert.assertEquals(hdl.getLanguage(), this.language);
      Assert.assertEquals(hdl.getConnectorName(), this.connectorName);
      Assert.assertEquals(hdl.getConnectorType(), this.connectorType);
      Assert.assertEquals(hdl.isUpdate(), this.update);
      Assert.assertEquals(hdl.getConfigData(), this.configData);
    } else if (hdl.getStatus().equals(
        ServletUtil.XML_RESPONSE_STATUS_NULL_CONNECTOR)) {
      Assert.assertEquals(0, this.connectorName.length());
    } else if (hdl.getStatus().equals(
        ServletUtil.XML_RESPONSE_STATUS_EMPTY_CONFIG_DATA)) {
      Assert.assertEquals(hdl.getConnectorName(), this.connectorName);
      Assert.assertEquals(hdl.getConfigData(), this.configData);
    }
  }

  public String setXMLBody() {
    String body = 
      "<" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">\n" +
      "  <" + ServletUtil.QUERY_PARAM_LANG + ">" + this.language + "</" + ServletUtil.QUERY_PARAM_LANG + ">\n" +
      "  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">" + this.connectorName + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">\n" +
      "  <" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">" + this.connectorType + "</" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">\n" +
      "  <" + ServletUtil.XMLTAG_UPDATE_CONNECTOR + ">" + this.update + "</" + ServletUtil.XMLTAG_UPDATE_CONNECTOR + ">\n";
    Set set = configData.entrySet();
    Iterator iterator = set.iterator();
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry)iterator.next();
      body += "  <" + ServletUtil.XMLTAG_PARAMETERS + " name=\"" + entry.getKey()
           + "\" value=\"" + entry.getValue() + "\"/>\n";
    }     
    return body + "</" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">";
  }
}
