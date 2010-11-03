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

public class ImportExportConnectorTest extends TestCase {
  static final Map<String, String> CONFIG_MAP =
      new HashMap<String, String>() {
    {
      put("username", "name");
      put("password", "pwd");
      put("color", "red");
    }
  };

  static final String CONFIG_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<beans>\n"
      + "  <bean id=\"test\" class=\"java.lang.String\">\n"
      + "    <constructor-arg value=\"Hello World\"/>\n"
      + "  </bean>\n"
      + "</beans>\n";

  static final String CONFIG_XML_WITH_CDATA =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<beans>\n"
      + "  <bean id=\"test\" class=\"java.lang.String\">\n"
      + "    <![CDATA[\"Hello World\"/]]>\n"
      + "  </bean>\n"
      + "</beans>\n";

  static final String NEW_FORMAT_NULL_SCHEDULE =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-01</ConnectorName>\n"
      + "  <ConnectorType>TestConnector</ConnectorType>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "</ConnectorInstance>\n";

  static final String NEW_FORMAT_EMPTY_SCHEDULE =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-01</ConnectorName>\n"
      + "  <ConnectorSchedules version=\"3\">"
      + "</ConnectorSchedules>\n"
      + "  <ConnectorType>TestConnector</ConnectorType>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "</ConnectorInstance>\n";

  static final String NEW_FORMAT_WITH_SCHEDULE =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-02</ConnectorName>\n"
      + "  <ConnectorSchedules version=\"3\">connector-02:100:300000:0-0"
      + "</ConnectorSchedules>\n"
      + "  <ConnectorType>TestConnector</ConnectorType>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "</ConnectorInstance>\n";

  static final String NEW_FORMAT_WITH_CHECKPOINT =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-02</ConnectorName>\n"
      + "  <ConnectorCheckpoint>checkpoint</ConnectorCheckpoint>\n"
      + "  <ConnectorSchedules version=\"3\">\n"
      + "    <load>100</load>\n"
      + "    <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "    <TimeIntervals>0-0</TimeIntervals>\n"
      + "  </ConnectorSchedules>\n"
      + "  <ConnectorType>TestConnector</ConnectorType>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "</ConnectorInstance>\n";

  private static final String NEW_FORMAT_WITH_CONFIG_XML =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-02</ConnectorName>\n"
      + "  <ConnectorSchedules version=\"3\">\n"
      + "    <load>100</load>\n"
      + "    <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "    <TimeIntervals>0-0</TimeIntervals>\n"
      + "  </ConnectorSchedules>\n"
      + "  <ConnectorType>TestConnector</ConnectorType>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "  <ConnectorConfigXml>\n<![CDATA[" + CONFIG_XML + "]]>\n"
      + "  </ConnectorConfigXml>\n"
      + "</ConnectorInstance>\n";

  private static final String NEW_FORMAT_WITH_CONFIG_XML_CDATA =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-02</ConnectorName>\n"
      + "  <ConnectorSchedules version=\"3\">\n"
      + "    <load>100</load>\n"
      + "    <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "    <TimeIntervals>0-0</TimeIntervals>\n"
      + "  </ConnectorSchedules>\n"
      + "  <ConnectorType>TestConnector</ConnectorType>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "  <ConnectorConfigXml>\n<![CDATA["
      + CONFIG_XML_WITH_CDATA.replaceAll("]]>", "]]&gt;")
      + "]]>\n"
      + "  </ConnectorConfigXml>\n"
      + "</ConnectorInstance>\n";

  private static final String NEW_FORMAT_WITH_EXPLODED_SCHEDULE =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-02</ConnectorName>\n"
      + "  <ConnectorSchedules version=\"3\">\n"
      + "    <disabled>true</disabled>\n"
      + "    <load>100</load>\n"
      + "    <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "    <TimeIntervals>0-0</TimeIntervals>\n"
      + "  </ConnectorSchedules>\n"
      + "  <ConnectorType>TestConnector</ConnectorType>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "</ConnectorInstance>\n";

  static final String NEW_FORMAT_WITH_TYPE_VERSION =
      "<ConnectorInstance>\n"
      + "  <ConnectorName>connector-01</ConnectorName>\n"
      + "  <ConnectorType name=\"TestConnector\" version=\"1.0\"/>\n"
      + "  <ConnectorConfig>\n"
      + "    <Param name=\"color\" value=\"red\"/>\n"
      + "    <Param name=\"password\" value=\"pwd\"/>\n"
      + "    <Param name=\"username\" value=\"name\"/>\n"
      + "  </ConnectorConfig>\n"
      + "</ConnectorInstance>\n";


  // Test reading New GSA Export format. (modern Schedule format)
  public final void testReadNewFormatSchedule() {
    ImportExportConnector connector = fromXmlString(NEW_FORMAT_WITH_SCHEDULE);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertNull("typeVersion", connector.getTypeVersion());
    assertEquals("schedule",
        "connector-02:100:300000:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading a missing schedule.
  public final void testReadNullSchedule() {
    ImportExportConnector connector = fromXmlString(NEW_FORMAT_NULL_SCHEDULE);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertNull("schedule", connector.getSchedule());
    Map<String, String> config = connector.getConfigMap();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading an empty schedule.
  public final void testReadEmptySchedule() {
    ImportExportConnector connector = fromXmlString(NEW_FORMAT_EMPTY_SCHEDULE);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertNull("schedule", connector.getSchedule());
    Map<String, String> config = connector.getConfigMap();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading exploded schedule format. (easier to read)
  public final void testReadExplodedSchedule() {
    ImportExportConnector connector = fromXmlString(NEW_FORMAT_WITH_EXPLODED_SCHEDULE);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());

    assertEquals("schedule",
        "#connector-02:100:300000:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading checkpoint.
  public final void testReadCheckpoint() {
    ImportExportConnector connector = fromXmlString(NEW_FORMAT_WITH_CHECKPOINT);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertEquals("schedule",
        "connector-02:100:300000:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertEquals("checkpoint", "checkpoint", connector.getCheckpoint());
    assertNull("configXml", connector.getConfigXml());
  }

  // Test reading configuration with connectorInstance.xml.
  public final void testReadConfigXml() {
    ImportExportConnector connector = fromXmlString(NEW_FORMAT_WITH_CONFIG_XML);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertEquals("schedule",
        "connector-02:100:300000:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);

    assertEquals("configXml", CONFIG_XML, connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading configuration with connectorInstance.xml with embedded CDATA.
  public final void testReadConfigXmlEmbeddedCdata() {
    ImportExportConnector connector = fromXmlString(NEW_FORMAT_WITH_CONFIG_XML_CDATA);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertEquals("schedule",
        "connector-02:100:300000:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);

    assertEquals("configXml", CONFIG_XML_WITH_CDATA,
        connector.getConfigXml());
    assertNull("checkpoint", connector.getCheckpoint());
  }

  // Test reading versioned connector type.
  public final void testReadVersionedType() {
    ImportExportConnector connector = fromXmlString(NEW_FORMAT_WITH_TYPE_VERSION);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnector", connector.getTypeName());
    assertEquals("typeVersion", "1.0", connector.getTypeVersion());
    Map<String, String> config = connector.getConfigMap();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    assertNull("configXml", connector.getConfigXml());
  }

  // Test that schedules are written in exploded format.
  public final void testWriteExplodedSchedule() {
    Schedule schedule =
        new Schedule("connector-02", true, 100, 300000, "0-0");
    ImportExportConnector connector = new ImportExportConnector("connector-02",
        new Configuration("TestConnector", CONFIG_MAP, null), schedule, null);

    String xmlResult = asXmlString(connector);
    assertEquals(NEW_FORMAT_WITH_EXPLODED_SCHEDULE,
        StringUtils.normalizeNewlines(xmlResult));
  }

  // Test that checkpoints are included in the output.
  public final void testWriteCheckpoint() {
    Schedule schedule =
        new Schedule("connector-02", false, 100, 300000, "0-0");
    ImportExportConnector connector = new ImportExportConnector("connector-02",
        new Configuration("TestConnector", CONFIG_MAP, null),
        schedule, "checkpoint");

    String xmlResult = asXmlString(connector);
    System.out.println("testWriteCheckpoint:\n" + xmlResult);
    assertEquals(NEW_FORMAT_WITH_CHECKPOINT,
        StringUtils.normalizeNewlines(xmlResult));
  }

  // Test that configuration XML is included in the output.
  public final void testWriteConfigXml() {
    Schedule schedule =
        new Schedule("connector-02", false, 100, 300000, "0-0");
    ImportExportConnector connector = new ImportExportConnector("connector-02",
        new Configuration("TestConnector", CONFIG_MAP, CONFIG_XML),
        schedule, null);

    String xmlResult = asXmlString(connector);
    assertEquals(NEW_FORMAT_WITH_CONFIG_XML,
        StringUtils.normalizeNewlines(xmlResult));
  }

  // Test that configuration XML with embedded CDATA is properly escaped.
  public final void testWriteConfigXmlCdata() {
    Schedule schedule =
        new Schedule("connector-02", false, 100, 300000, "0-0");
    ImportExportConnector connector = new ImportExportConnector("connector-02",
        new Configuration("TestConnector", CONFIG_MAP, CONFIG_XML_WITH_CDATA),
        schedule, null);

    String xmlResult = asXmlString(connector);
    assertEquals(NEW_FORMAT_WITH_CONFIG_XML_CDATA,
        StringUtils.normalizeNewlines(xmlResult));
  }

  // Test that property values are properly escaped.
  public final void testWriteEscapedAttrValues() {
    HashMap<String, String> config = new HashMap<String, String>(CONFIG_MAP);
    config.put("uglyvalue", "one&two<three>four'five\"");
    ImportExportConnector connector = new ImportExportConnector("connector-01",
        new Configuration("TestConnector", config, null), null, null);

    String xmlResult = asXmlString(connector);
    assertTrue("attribute values", xmlResult.contains("Param name=\"uglyvalue\""
        + " value=\"one&amp;two&lt;three>four&#39;five&quot;\""));
  }

  static void assertContains(Map<String, String> config, String key, String value) {
    for (Map.Entry<String, String> entry : config.entrySet()) {
      if (entry.getKey().equals(key) && entry.getValue().equals(value)) {
        config.remove(key);
        return;
      }
    }
    fail("Failed to find key=" + key + ", value=" + value);
  }

  static String asXmlString(ImportExportConnector connector) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    connector.toXml(pw, 0);
    return sw.toString();
  }

  private static ImportExportConnector fromXmlString(String xmlString) {
    Document document =
        XmlParseUtil.parse(xmlString, new SAXParseErrorHandler(), null);
    ImportExportConnector connector = new ImportExportConnector();
    connector.fromXml(document.getDocumentElement());
    return connector;
  }
}
