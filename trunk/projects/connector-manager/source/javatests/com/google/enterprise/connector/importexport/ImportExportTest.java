// Copyright 2009 Google Inc. All Rights Reserved.
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

import com.google.enterprise.connector.scheduler.Schedule;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportExportTest extends TestCase {
  private static final String OLD_FORMAT =
      "<ConnectorInstances>\n"
      + "  <ConnectorInstance>\n"
      + "    <ConnectorName>connector-01</ConnectorName>\n"
      + "    <ConnectorType>TestConnector</ConnectorType>\n"
      + "    <ConnectorSchedule>connector-01:100:0-0</ConnectorSchedule>\n"
      + "    <ConnectorConfig>\n"
      + "      <Param name=\"color\" value=\"red\"/>\n"
      + "      <Param name=\"password\" value=\"pwd\"/>\n"
      + "      <Param name=\"username\" value=\"name\"/>\n"
      + "    </ConnectorConfig>\n"
      + "  </ConnectorInstance>\n"
      + "</ConnectorInstances>\n";
  private static final String NEW_FORMAT_NULL_SCHEDULE =
      "<ConnectorInstances>\n"
      + "  <ConnectorInstance>\n"
      + "    <ConnectorName>connector-01</ConnectorName>\n"
      + "    <ConnectorType>TestConnector</ConnectorType>\n"
      + "    <ConnectorSchedules version=\"3\">#null:0:-1:"
      + "</ConnectorSchedules>\n"
      + "    <ConnectorConfig>\n"
      + "      <Param name=\"color\" value=\"red\"/>\n"
      + "      <Param name=\"password\" value=\"pwd\"/>\n"
      + "      <Param name=\"username\" value=\"name\"/>\n"
      + "    </ConnectorConfig>\n"
      + "  </ConnectorInstance>\n"
      + "</ConnectorInstances>\n";
  private static final String NEW_FORMAT_WITH_SCHEDULE =
      "<ConnectorInstances>\n"
      + "  <ConnectorInstance>\n"
      + "    <ConnectorName>connector-02</ConnectorName>\n"
      + "    <ConnectorType>TestConnector</ConnectorType>\n"
      + "    <ConnectorSchedules version=\"3\">connector-02:100:300000:0-0"
      + "</ConnectorSchedules>\n"
      + "    <ConnectorConfig>\n"
      + "      <Param name=\"color\" value=\"red\"/>\n"
      + "      <Param name=\"password\" value=\"pwd\"/>\n"
      + "      <Param name=\"username\" value=\"name\"/>\n"
      + "    </ConnectorConfig>\n"
      + "  </ConnectorInstance>\n"
      + "</ConnectorInstances>\n";
  private static final Map<String, String> CONFIG_MAP =
      new HashMap<String, String>() {{
        put("username", "name");
        put("password", "pwd");
        put("color", "red");
      }};

  public final void testFromXmlString() {
    List<ImportExportConnector> connectors =
        ImportExport.fromXmlString(OLD_FORMAT);
    assertTrue(connectors.size() == 1);
    ImportExportConnector connector = connectors.remove(0);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnector", connector.getType());
    assertEquals("schedule",
        "connector-01:100:0-0", connector.getScheduleString());
    Map<String, String> config = connector.getConfig();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
    // New format.
    connectors = ImportExport.fromXmlString(NEW_FORMAT_WITH_SCHEDULE);
    assertTrue(connectors.size() == 1);
    connector = connectors.remove(0);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnector", connector.getType());
    assertEquals("schedule",
        "connector-02:100:300000:0-0", connector.getScheduleString());
    config = connector.getConfig();
    assertContains(config, "username", "name");
    assertContains(config, "password", "pwd");
    assertContains(config, "color", "red");
    assertTrue(config.size() == 0);
  }

  private void assertContains(Map<String, String> config,
      String key, String value) {
    for (Map.Entry<String,String> entry : config.entrySet()) {
      if (entry.getKey().equals(key) && entry.getValue().equals(value)) {
        config.remove(key);
        return;
      }
    }
    fail("Failed to find key=" + key + ", value=" + value);
  }

  public final void testAsXmlString() {
    List<ImportExportConnector> connectors =
        new ArrayList<ImportExportConnector>();
    String schedule = new Schedule().toString();
    ImportExportConnector connector =
        createSampleConnector("connector-01", schedule);
    connectors.add(connector);
    String xmlResult = ImportExport.asXmlString(connectors);
    assertEquals("connector-01", NEW_FORMAT_NULL_SCHEDULE, xmlResult);
    // Now try it with a real schedule.
    schedule = new Schedule("connector-02", false, 100, 300000, "0-0")
        .toString();
    connector = createSampleConnector("connector-02", schedule);
    connectors.clear();
    connectors.add(connector);
    xmlResult = ImportExport.asXmlString(connectors);
    assertEquals("connector-02", NEW_FORMAT_WITH_SCHEDULE, xmlResult);
  }

  private ImportExportConnector createSampleConnector(String connectorName,
      String schedule) {
    return new ImportExportConnector(connectorName, "TestConnector", schedule,
            CONFIG_MAP);
  }
}
