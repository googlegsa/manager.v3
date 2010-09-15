// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.importexport;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.util.SAXParseErrorHandler;
import com.google.enterprise.connector.util.XmlParseUtil;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ImportExportConnectorListTest extends TestCase {

  private static final String SINGLE_CONNECTOR =
      "<ConnectorInstances>\n"
      + "  <ConnectorInstance>\n"
      + "    <ConnectorName>connector-02</ConnectorName>\n"
      + "    <ConnectorCheckpoint>checkpoint</ConnectorCheckpoint>\n"
      + "    <ConnectorSchedules version=\"3\">\n"
      + "      <disabled>true</disabled>\n"
      + "      <load>100</load>\n"
      + "      <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "      <TimeIntervals>0-0</TimeIntervals>\n"
      + "    </ConnectorSchedules>\n"
      + "    <ConnectorType>TestConnector</ConnectorType>\n"
      + "    <ConnectorConfig>\n"
      + "      <Param name=\"color\" value=\"red\"/>\n"
      + "      <Param name=\"password\" value=\"pwd\"/>\n"
      + "      <Param name=\"username\" value=\"name\"/>\n"
      + "    </ConnectorConfig>\n"
      + "    <ConnectorConfigXml>\n<![CDATA["
      + ImportExportConnectorTest.CONFIG_XML + "]]>\n"
      + "    </ConnectorConfigXml>\n"
      + "  </ConnectorInstance>\n"
      + "</ConnectorInstances>\n";

  private static final String MULTIPLE_CONNECTORS =
      "<ConnectorInstances>\n"
      + "  <ConnectorInstance>\n"
      + "    <ConnectorName>connector-01</ConnectorName>\n"
      + "    <ConnectorType>TestConnector</ConnectorType>\n"
      + "    <ConnectorConfig>\n"
      + "      <Param name=\"color\" value=\"red\"/>\n"
      + "      <Param name=\"password\" value=\"pwd\"/>\n"
      + "      <Param name=\"username\" value=\"name\"/>\n"
      + "    </ConnectorConfig>\n"
      + "  </ConnectorInstance>\n"
      + "  <ConnectorInstance>\n"
      + "    <ConnectorName>connector-02</ConnectorName>\n"
      + "    <ConnectorSchedules version=\"3\">\n"
      + "      <load>100</load>\n"
      + "      <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "      <TimeIntervals>0-0</TimeIntervals>\n"
      + "    </ConnectorSchedules>\n"
      + "    <ConnectorType>TestConnector</ConnectorType>\n"
      + "    <ConnectorConfig>\n"
      + "      <Param name=\"color\" value=\"blue\"/>\n"
      + "      <Param name=\"password\" value=\"pwd\"/>\n"
      + "      <Param name=\"username\" value=\"name\"/>\n"
      + "    </ConnectorConfig>\n"
      + "  </ConnectorInstance>\n"
      + "</ConnectorInstances>\n";


  // Test reading a list containing a single connector.
  public final void testReadSingleConnector() {
    ImportExportConnectorList connectors = fromXmlString(SINGLE_CONNECTOR);
    assertTrue(connectors.size() == 1);
    ImportExportConnector connector = connectors.remove(0);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("checkpoint", "checkpoint", connector.getCheckpoint());
    assertEquals("schedule",
        "#connector-02:100:300000:0-0", connector.getScheduleString());
    assertEquals("type", "TestConnector", connector.getTypeName());
    Map<String, String> config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "username", "name");
    ImportExportConnectorTest.assertContains(config, "password", "pwd");
    ImportExportConnectorTest.assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertEquals("configXml", ImportExportConnectorTest.CONFIG_XML,
        connector.getConfigXml());
  }

  // Test Multiple Connector Instances
  public final void testReadMultipleConnectors() {
    ImportExportConnectorList connectors = fromXmlString(MULTIPLE_CONNECTORS);
    checkMultipleConnectors(connectors);
  }

  private final void checkMultipleConnectors(
      ImportExportConnectorList connectors) {
    assertTrue(connectors.size() == 2);
    ImportExportConnector connector = connectors.remove(0);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    Map<String, String> config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "username", "name");
    ImportExportConnectorTest.assertContains(config, "password", "pwd");
    ImportExportConnectorTest.assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("schedule", connector.getSchedule());
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());

    connector = connectors.remove(0);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertEquals("schedule",
        "connector-02:100:300000:0-0", connector.getScheduleString());
    config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "username", "name");
    ImportExportConnectorTest.assertContains(config, "password", "pwd");
    ImportExportConnectorTest.assertContains(config, "color", "blue");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test that schedules are written in exploded format.
  public final void testWriteSingleConnector() {
    Schedule schedule =
        new Schedule("connector-02", true, 100, 300000, "0-0");
    ImportExportConnector connector = new ImportExportConnector("connector-02",
        new Configuration("TestConnector", ImportExportConnectorTest.CONFIG_MAP,
        ImportExportConnectorTest.CONFIG_XML), schedule, "checkpoint");
    ImportExportConnectorList connectors = new ImportExportConnectorList();
    connectors.add(connector);

    String xmlResult = asXmlString(connectors);
    assertEquals(SINGLE_CONNECTOR, StringUtils.normalizeNewlines(xmlResult));
  }

  // Test Write Multiple Connector Instances
  public final void testWriteMultipleConnectors() {
    ImportExportConnectorList connectors = buildMultipleConnectors();
    String xmlResult = asXmlString(connectors);
    assertEquals(MULTIPLE_CONNECTORS,
        StringUtils.normalizeNewlines(xmlResult));
  }

  private final ImportExportConnectorList buildMultipleConnectors() {
    ImportExportConnector connector = new ImportExportConnector("connector-01",
        new Configuration("TestConnector", ImportExportConnectorTest.CONFIG_MAP,
        null), null, null);
    ImportExportConnectorList connectors = new ImportExportConnectorList();
    connectors.add(connector);

    Schedule schedule = new Schedule("connector-02", false, 100, 300000, "0-0");
    Map<String, String> config =
        new HashMap<String, String>(ImportExportConnectorTest.CONFIG_MAP);
    config.put("color", "blue");
    connector = new ImportExportConnector("connector-02",
        new Configuration("TestConnector", config, null), schedule, null);
    connectors.add(connector);
    return connectors;
  }

  static String asXmlString(ImportExportConnectorList connectors) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    connectors.toXml(pw, 0);
    return sw.toString();
  }

  private static ImportExportConnectorList fromXmlString(String xmlString) {
    Document document =
        XmlParseUtil.parse(xmlString, new SAXParseErrorHandler(), null);
    ImportExportConnector connector = new LegacyImportExportConnector();
    ImportExportConnectorList connectors = new ImportExportConnectorList();
    connectors.fromXml(document.getDocumentElement(),
        ImportExportConnector.class);
    return connectors;
  }
}
