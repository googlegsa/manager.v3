/**
 * 
 */
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
    configData = new TreeMap();
    doTest(setXMLBody());
  }

  private void doTest(String xmlBody) {
    LOG.info("xmlBody: " + xmlBody);
    Manager manager = MockManager.getInstance();
    SetConnectorConfigHandler hdl = new SetConnectorConfigHandler(
        manager, this.language, xmlBody);
    LOG.info("ConnectorName: " + hdl.getConnectorName() + " this: " + this.connectorName);
    LOG.info("ConnectorType: " + hdl.getConnectorType() + " this: " + this.connectorType);
    if (hdl.getStatus().equals(ServletUtil.XML_RESPONSE_SUCCESS)) {
      Assert.assertEquals(hdl.getConnectorName(), this.connectorName);
      Assert.assertEquals(hdl.getConnectorType(), this.connectorType);
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
      "  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">" + this.connectorName + "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">\n" +
      "  <" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">" + this.connectorType + "</" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">\n";
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
