// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Tests SetConnectorConfigHandlerTest class for SetConnectorConfig
 * servlet class.
 *
 */
public class SetConnectorConfigHandlerTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(SetConnectorConfigHandlerTest.class.getName());
  private String language;
  private String connectorName;
  private String connectorType;
  private Map <String, String> configData;
  private boolean update;

  public void testSetConnectorConfigHandler1() {
    language = "en";
    connectorName = "connectorA";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    doTest(setXMLBody());
  }

  public void testSetConnectorConfigHandler2() {
    language = "en";
    connectorName = "";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
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
    configData = new TreeMap<String, String>();
    doTest(setXMLBody());
  }

  public void testSetConnectorConfigHandler4() {
    language = "en";
    connectorName = "connectorC";
    connectorType = "documentum";
    update = true;
    configData = new TreeMap<String, String>();
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
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueB1");
    configData.put("name2", "valueB2");
    configData.put("name3", "valueB3");
    doTest(setXMLBody());
  }

  private void doTest(String xmlBody) {
    LOGGER.info("xmlBody: " + xmlBody);
    String name = this.connectorName;
    if (!this.update) {
      // GSA 5.2 wants connectorNames to be lower case.
      name = name.toLowerCase();
    }

    Manager manager = MockManager.getInstance();
    SetConnectorConfigHandler hdl = new SetConnectorConfigHandler(
        xmlBody, manager);
    LOGGER.info("ConnectorName: " + hdl.getConnectorName() + " this: " + this.connectorName);
    LOGGER.info("ConnectorType: " + hdl.getConnectorType() + " this: " + this.connectorType);
    if (hdl.getStatus().isSuccess()) {
      Assert.assertEquals(hdl.getLanguage(), this.language);
      Assert.assertEquals(hdl.getConnectorName(), name);
      Assert.assertEquals(hdl.getConnectorType(), this.connectorType);
      Assert.assertEquals(hdl.isUpdate(), this.update);
      Assert.assertEquals(hdl.getConfigData(), this.configData);
    } else if (hdl.getStatus().getMessageId() ==
        ConnectorMessageCode.RESPONSE_NULL_CONNECTOR) {
      Assert.assertEquals(0, name.length());
    } else if (hdl.getStatus().getMessageId() ==
        ConnectorMessageCode.RESPONSE_NULL_CONFIG_DATA) {
      Assert.assertEquals(hdl.getConnectorName(), name);
      Assert.assertEquals(hdl.getConfigData(), this.configData);
    }
  }

  public String setXMLBody() {
    String name = this.connectorName;
    if (!this.update ) {
      // GSA 5.2 wants connectorNames to be lower case.
      // But we can only enforce it for new connectors, not existing ones.
      name = name.toLowerCase();
    }
    String body =
      "<" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">\n" +
      "  <" + ServletUtil.QUERY_PARAM_LANG + ">" + this.language + "</" + ServletUtil.QUERY_PARAM_LANG + ">\n" +
      "  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">" + name + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">\n" +
      "  <" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">" + this.connectorType + "</" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">\n" +
      "  <" + ServletUtil.XMLTAG_UPDATE_CONNECTOR + ">" + this.update + "</" + ServletUtil.XMLTAG_UPDATE_CONNECTOR + ">\n";

    Iterator <Map.Entry <String, String>> iterator = configData.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry <String, String> entry = iterator.next();
      body += "  <" + ServletUtil.XMLTAG_PARAMETERS + " name=\"" + entry.getKey()
           + "\" value=\"" + entry.getValue() + "\"/>\n";
    }
    return body + "</" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">";
  }
}
