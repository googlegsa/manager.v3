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

import java.util.Map;

public class LegacyImportExportConnectorTest extends TestCase {

  private static final String OLD_FORMAT =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-01</ConnectorName>\n"
      + "  <ConnectorType>TestConnector</ConnectorType>\n"
      + "  <ConnectorSchedule>connector-01:100:0-0</ConnectorSchedule>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "</ConnectorInstance>\n";


  // Test reading Legacy GSA Export format. (old Schedule format)
  public final void testReadOldFormatSchedule() {
    ImportExportConnector connector = fromXmlString(OLD_FORMAT);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertEquals("schedule",
        "connector-01:100:300000:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "username", "name");
    ImportExportConnectorTest.assertContains(config, "password", "pwd");
    ImportExportConnectorTest.assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading New GSA Export format. (modern Schedule format)
  public final void testReadNewFormatSchedule() {
    ImportExportConnector connector =
        fromXmlString(ImportExportConnectorTest.NEW_FORMAT_WITH_SCHEDULE);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertEquals("schedule",
        "connector-02:100:300000:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "username", "name");
    ImportExportConnectorTest.assertContains(config, "password", "pwd");
    ImportExportConnectorTest.assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading a missing schedule.
  public final void testReadNullSchedule() {
    ImportExportConnector connector =
        fromXmlString(ImportExportConnectorTest.NEW_FORMAT_NULL_SCHEDULE);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertNull("schedule", connector.getSchedule());
    Map<String, String> config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "username", "name");
    ImportExportConnectorTest.assertContains(config, "password", "pwd");
    ImportExportConnectorTest.assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading an empty schedule.
  public final void testReadEmptySchedule() {
    ImportExportConnector connector =
        fromXmlString(ImportExportConnectorTest.NEW_FORMAT_EMPTY_SCHEDULE);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertNull("schedule", connector.getSchedule());
    Map<String, String> config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "username", "name");
    ImportExportConnectorTest.assertContains(config, "password", "pwd");
    ImportExportConnectorTest.assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading checkpoint should return null.
  public final void testReadCheckpoint() {
    ImportExportConnector connector =
        fromXmlString(ImportExportConnectorTest.NEW_FORMAT_WITH_CHECKPOINT);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertEquals("schedule",
        "connector-02:100:300000:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "username", "name");
    ImportExportConnectorTest.assertContains(config, "password", "pwd");
    ImportExportConnectorTest.assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("checkpoint", connector.getCheckpoint());
    assertNull("configXml", connector.getConfigXml());
  }

  // Test Writing an null schedule generates an empty schedule element.
  public final void testWriteEmptySchedule() {
    ImportExportConnector connector = new LegacyImportExportConnector(
        "connector-01", new Configuration("TestConnector",
        ImportExportConnectorTest.CONFIG_MAP, null), null, null);

    String xmlResult = ImportExportConnectorTest.asXmlString(connector);
    assertEquals(ImportExportConnectorTest.NEW_FORMAT_EMPTY_SCHEDULE,
        StringUtils.normalizeNewlines(xmlResult));
  }

  // Test that schedules are written in new format.
  public final void testWriteNewSchedule() {
    Schedule schedule =
        new Schedule("connector-02", false, 100, 300000, "0-0");
    ImportExportConnector connector = new LegacyImportExportConnector(
        "connector-02", new Configuration("TestConnector",
        ImportExportConnectorTest.CONFIG_MAP, null), schedule, null);

    String xmlResult = ImportExportConnectorTest.asXmlString(connector);
    assertEquals(ImportExportConnectorTest.NEW_FORMAT_WITH_SCHEDULE,
        StringUtils.normalizeNewlines(xmlResult));
  }

  // Test that checkpoints are not included in the output.
  public final void testWriteCheckpoint() {
    Schedule schedule =
        new Schedule("connector-02", false, 100, 300000, "0-0");
    ImportExportConnector connector = new LegacyImportExportConnector(
        "connector-02", new Configuration("TestConnector",
        ImportExportConnectorTest.CONFIG_MAP, null), schedule, "checkpoint");

    String xmlResult = ImportExportConnectorTest.asXmlString(connector);
    assertEquals(ImportExportConnectorTest.NEW_FORMAT_WITH_SCHEDULE,
        StringUtils.normalizeNewlines(xmlResult));
  }

  private ImportExportConnector fromXmlString(String xmlString) {
    Document document =
        XmlParseUtil.parse(xmlString, new SAXParseErrorHandler(), null);
    ImportExportConnector connector = new LegacyImportExportConnector();
    connector.fromXml(document.getDocumentElement());
    return connector;
  }
}
