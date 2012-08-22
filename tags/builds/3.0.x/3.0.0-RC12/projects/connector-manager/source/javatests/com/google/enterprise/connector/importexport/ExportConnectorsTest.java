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
import com.google.enterprise.connector.persist.MockPersistentStore;
import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.scheduler.Schedule;

import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


/** Tests for {@link ExportConnectors} */
public class ExportConnectorsTest extends TestCase {

  private static final String EXPECTED_XML =
      "<ConnectorInstances>\n"
      + "  <ConnectorInstance>\n"
      + "    <ConnectorName>connector-01</ConnectorName>\n"
      + "    <ConnectorCheckpoint>connector-01 checkpoint</ConnectorCheckpoint>\n"
      + "    <ConnectorSchedules version=\"3\">\n"
      + "      <load>100</load>\n"
      + "      <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "      <TimeIntervals>0-0</TimeIntervals>\n"
      + "    </ConnectorSchedules>\n"
      + "    <ConnectorType>TestConnector</ConnectorType>\n"
      + "    <ConnectorConfig>\n"
      + "      <Param name=\"color\" value=\"red\"/>\n"
      + "      <Param name=\"googlePropertiesVersion\" value=\"3\"/>\n"
      + "    </ConnectorConfig>\n"
      + "  </ConnectorInstance>\n"
      + "  <ConnectorInstance>\n"
      + "    <ConnectorName>connector-02</ConnectorName>\n"
      + "    <ConnectorCheckpoint>connector-02 checkpoint</ConnectorCheckpoint>\n"
      + "    <ConnectorSchedules version=\"3\">\n"
      + "      <load>100</load>\n"
      + "      <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "      <TimeIntervals>0-0</TimeIntervals>\n"
      + "    </ConnectorSchedules>\n"
      + "    <ConnectorType>TestConnector</ConnectorType>\n"
      + "    <ConnectorConfig>\n"
      + "      <Param name=\"color\" value=\"blue\"/>\n"
      + "      <Param name=\"googlePropertiesVersion\" value=\"3\"/>\n"
      + "    </ConnectorConfig>\n"
      + "  </ConnectorInstance>\n"
      + "</ConnectorInstances>\n";


  /** Test ExportConnectors.toXml(). */
  public void testToXml() throws Exception {
    PersistentStore pstore = new MockPersistentStore(true);
    addConnector(pstore, "connector-01", "red", null);
    addConnector(pstore, "connector-02", "blue", null);

    ImportExportConnectorList connectors =
        new ExportConnectors(pstore, null).getConnectors();
    String exportXml = asXmlString(connectors);
    System.out.println("ExportConnectors:\n"+exportXml);
    assertEquals(EXPECTED_XML, exportXml);
  }

  /** Test that passwords are not include in clear text in the generated XML. */
  public void testEncryptedPasswords() throws Exception {
    PersistentStore pstore = new MockPersistentStore();
    addConnector(pstore, "connector-01", "red", "pwd");

    ImportExportConnectorList connectors =
        new ExportConnectors(pstore, null).getConnectors();
    String exportXml = asXmlString(connectors);
    // First make sure the password property is included in the output.
    assertTrue("password", exportXml.contains(
        "<Param name=\"password\" value="));
    // Then make sure the password value is not included in clear-text.
    assertFalse("password", exportXml.contains(
        "<Param name=\"password\" value=\"pwd\""));
  }

  private static void addConnector(PersistentStore pstore, String name,
                                   String color, String password) {
    Map<String, String> configMap = new HashMap<String, String>();
    configMap.put("color", color);
    if (password != null) {
      configMap.put("password", password);
    }
    Configuration config = new Configuration("TestConnector", configMap, null);
    StoreContext context = new StoreContext(name, "TestConnector");
    pstore.storeConnectorConfiguration(context, config);
    Schedule schedule = new Schedule(name, false, 100, 300000, "0-0");
    pstore.storeConnectorSchedule(context, schedule);
    pstore.storeConnectorState(context, name + " checkpoint");
  }

  private static String asXmlString(ImportExportConnectorList connectors) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    connectors.toXml(pw, 0);
    return StringUtils.normalizeNewlines(sw.toString());
  }
}
