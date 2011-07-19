// Copyright 2009 Google Inc.
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

import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.ConnectorCoordinator;
import com.google.enterprise.connector.instantiator.ConnectorCoordinatorMap;
import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.util.SAXParseErrorHandler;
import com.google.enterprise.connector.util.XmlParseUtil;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ImportExportTest extends TestCase {

  private static final Map<String, String> CONFIG_MAP =
      new HashMap<String, String>() {
    {
      put("Username", "foo");
      put("Password", "bar");
      put("Color", "red");
      put("RepositoryFile", "MockRepositoryEventLog3.txt");
    }
  };

  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/TestContext.xml";

  @Override
  protected void setUp() throws Exception {
    refreshContext();
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    getTypeMap().init();
  }

  private void refreshContext() throws Exception {
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    getTypeMap().init();
  }


  /** Retrieve the TypeMap from the Spring Context. */
  private static TypeMap getTypeMap() {
    return (TypeMap) Context.getInstance().getRequiredBean(
        "TypeMap", TypeMap.class);
  }

  /** Retrieve the ConnectorCoordinatorMap from the Spring Context. */
  private static ConnectorCoordinatorMap getCoordinatorMap() {
    return (ConnectorCoordinatorMap) Context.getInstance().getRequiredBean(
        "ConnectorCoordinatorMap", ConnectorCoordinatorMap.class);
  }

  private void addConnector(String name, String color) throws Exception {
    Context context = Context.getInstance();
    ConnectorCoordinatorMap ccm = getCoordinatorMap();
    ConnectorCoordinator coordinator = ccm.getOrAdd(name);
    String typeName = "TestConnectorA";
    Map<String, String> configMap = new HashMap<String, String>(CONFIG_MAP);
    configMap.put("Color", color);
    Configuration config = new Configuration(typeName, configMap, null);
    ConfigureResponse response = coordinator.setConnectorConfiguration(
        getTypeMap().getTypeInfo(typeName), config, Locale.ENGLISH, false);
    assertNull((response == null) ? null : response.getMessage(), response);
    Schedule schedule = new Schedule(name, false, 100, 300000, "0-0");
    coordinator.setConnectorSchedule(schedule);
    coordinator.setConnectorState("checkpoint");
  }

  // Tests the correct construction of List<ImportExportConnector> from
  // an existing installation.
  public final void testGetConnectors() throws Exception {
    Context context = Context.getInstance();
    ConnectorCoordinatorMap ccm = getCoordinatorMap();

    addConnector("connector-01", "red");
    addConnector("connector-02", "blue");

    ImportExportConnectorList connectors = ImportExport.getConnectors();
    assertTrue(connectors.size() == 2);

    ImportExportConnector connector = connectors.remove(0);
    assertEquals("name", "connector-01", connector.getName());
    assertEquals("type", "TestConnectorA", connector.getTypeName());
    assertNull("checkpoint", connector.getCheckpoint());
    assertEquals("schedule", "connector-01:100:300000:0-0",
        connector.getScheduleString());
    Map<String, String> config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "Username", "foo");
    ImportExportConnectorTest.assertContains(config, "Password", "bar");
    ImportExportConnectorTest.assertContains(config, "Color", "red");

    connector = connectors.remove(0);
    assertEquals("name", "connector-02", connector.getName());
    assertEquals("type", "TestConnectorA", connector.getTypeName());
    assertNull("checkpoint", connector.getCheckpoint());
    assertEquals("schedule", "connector-02:100:300000:0-0",
        connector.getScheduleString());
    config = connector.getConfigMap();
    ImportExportConnectorTest.assertContains(config, "Username", "foo");
    ImportExportConnectorTest.assertContains(config, "Password", "bar");
    ImportExportConnectorTest.assertContains(config, "Color", "blue");
  }

  // Test that the exported XML is in the legacy ImportExport
  // format for the GSA: Stringified Schedules, no traversal state,
  // plain-text passwords.
  public final void testExportFormat() throws Exception {
    Context context = Context.getInstance();
    ConnectorCoordinatorMap ccm = getCoordinatorMap();

    addConnector("connector-01", "red");

    ImportExportConnectorList connectors = ImportExport.getConnectors();
    assertTrue(connectors.size() == 1);

    String exportXml = ImportExportConnectorListTest.asXmlString(connectors);

    // Make sure there are no checkpoints in the output.
    assertFalse("checkpoint in output",
        exportXml.contains(ServletUtil.XMLTAG_CONNECTOR_CHECKPOINT));

    // Make sure there are no exploded schedules in the output.
    assertTrue("schedule in output",
        exportXml.contains(ServletUtil.XMLTAG_CONNECTOR_SCHEDULES));
    assertFalse("exploded schedule in output",
        exportXml.contains(ServletUtil.XMLTAG_DISABLED));
    assertFalse("exploded schedule in output",
        exportXml.contains(ServletUtil.XMLTAG_LOAD));
    assertFalse("exploded schedule in output",
        exportXml.contains(ServletUtil.XMLTAG_DELAY));
    assertFalse("exploded schedule in output",
        exportXml.contains(ServletUtil.XMLTAG_TIME_INTERVALS));

    // Make sure passwords are in plain-text in the output.
    assertTrue(exportXml.contains("Param name=\"Password\" value=\"bar\""));
  }

  // Tests that an import into a barren installation creates new
  // connectors in the import set.
  public final void testSetConnectors() throws Exception {
    Context context = Context.getInstance();
    ConnectorCoordinatorMap ccm = getCoordinatorMap();

    addConnector("connector-01", "red");
    addConnector("connector-02", "blue");

    ImportExportConnectorList connectors = ImportExport.getConnectors();
    assertTrue(connectors.size() == 2);

    String exportXml = ImportExportConnectorListTest.asXmlString(connectors);

    // Now blow away the context, including our connectors.
    refreshContext();
    context = Context.getInstance();
    ccm = getCoordinatorMap();
    assertTrue(ccm.getConnectorNames().isEmpty());

    connectors = fromXmlString(exportXml);
    assertTrue(connectors.size() == 2);

    ImportExport.setConnectors(connectors, true);

    Set<String> connectorNames = ccm.getConnectorNames();
    assertTrue(connectorNames.contains("connector-01"));
    assertTrue(connectorNames.contains("connector-02"));

    ConnectorCoordinator coordinator = ccm.get("connector-01");
    Configuration config = coordinator.getConnectorConfiguration();
    assertEquals("type", "TestConnectorA", config.getTypeName());
    ImportExportConnectorTest.assertContains(config.getMap(), "Color", "red");
    assertNull("checkpoint", coordinator.getConnectorState());
    Schedule schedule = coordinator.getConnectorSchedule();
    assertFalse(schedule.isDisabled());
    assertTrue((schedule.getLoad() == 100));
    assertTrue((schedule.getRetryDelayMillis() == 300000));
    assertEquals("0-0", schedule.getTimeIntervalsAsString());

    coordinator = ccm.get("connector-02");
    config = coordinator.getConnectorConfiguration();
    assertEquals("type", "TestConnectorA", config.getTypeName());
    ImportExportConnectorTest.assertContains(config.getMap(), "Color", "blue");
    assertNull("checkpoint", coordinator.getConnectorState());
    schedule = coordinator.getConnectorSchedule();
    assertFalse(schedule.isDisabled());
    assertTrue((schedule.getLoad() == 100));
    assertTrue((schedule.getRetryDelayMillis() == 300000));
    assertEquals("0-0", schedule.getTimeIntervalsAsString());
  }

  // Tests that imported connectors over an existing installation
  // modifies the configuration, but leaves unimported data (like
  // the checkoint) in place.
  public final void testUpdateConnectors() throws Exception {
    Context context = Context.getInstance();
    ConnectorCoordinatorMap ccm = getCoordinatorMap();

    addConnector("connector-01", "red");
    addConnector("connector-02", "blue");

    ImportExportConnectorList connectors = ImportExport.getConnectors();
    assertTrue(connectors.size() == 2);

    // Change the colors, so they get imported differently.
    for (ImportExportConnector connector : connectors) {
      Map<String, String> configMap = connector.getConfigMap();
      String color = configMap.get("Color");
      configMap.put("Color", (color.equals("red") ? "blue" : "red"));
    }

    String exportXml = ImportExportConnectorListTest.asXmlString(connectors);

    connectors = fromXmlString(exportXml);
    assertTrue(connectors.size() == 2);

    ImportExport.setConnectors(connectors, true);

    Set<String> connectorNames = ccm.getConnectorNames();
    assertTrue(connectorNames.contains("connector-01"));
    assertTrue(connectorNames.contains("connector-02"));

    ConnectorCoordinator coordinator = ccm.get("connector-01");
    Configuration config = coordinator.getConnectorConfiguration();
    assertEquals("type", "TestConnectorA", config.getTypeName());
    ImportExportConnectorTest.assertContains(config.getMap(), "Color", "blue");
    assertEquals("checkpoint", "checkpoint", coordinator.getConnectorState());
    Schedule schedule = coordinator.getConnectorSchedule();
    assertFalse(schedule.isDisabled());
    assertTrue((schedule.getLoad() == 100));
    assertTrue((schedule.getRetryDelayMillis() == 300000));
    assertEquals("0-0", schedule.getTimeIntervalsAsString());

    coordinator = ccm.get("connector-02");
    config = coordinator.getConnectorConfiguration();
    assertEquals("type", "TestConnectorA", config.getTypeName());
    ImportExportConnectorTest.assertContains(config.getMap(), "Color", "red");
    assertEquals("checkpoint", "checkpoint", coordinator.getConnectorState());
    schedule = coordinator.getConnectorSchedule();
    assertFalse(schedule.isDisabled());
    assertTrue((schedule.getLoad() == 100));
    assertTrue((schedule.getRetryDelayMillis() == 300000));
    assertEquals("0-0", schedule.getTimeIntervalsAsString());
  }

  // Test that connectors not in the import set are not removed on import
  // if noRemove is true.
  public final void testNoRemove() throws Exception {
    Context context = Context.getInstance();
    ConnectorCoordinatorMap ccm = getCoordinatorMap();

    addConnector("connector-01", "red");
    addConnector("connector-02", "blue");

    ImportExportConnectorList connectors = ImportExport.getConnectors();
    assertTrue(connectors.size() == 2);

    // Change the colors, so they get imported differently.
    for (ImportExportConnector connector : connectors) {
      Map<String, String> configMap = connector.getConfigMap();
      String color = configMap.get("Color");
      configMap.put("Color", (color.equals("red") ? "blue" : "red"));
    }

    String exportXml = ImportExportConnectorListTest.asXmlString(connectors);

    connectors = fromXmlString(exportXml);
    assertTrue(connectors.size() == 2);

    // Add a new connector.
    addConnector("connector-03", "green");

    Set<String> connectorNames = ccm.getConnectorNames();
    assertTrue(connectorNames.contains("connector-01"));
    assertTrue(connectorNames.contains("connector-02"));
    assertTrue(connectorNames.contains("connector-03"));

    ImportExport.setConnectors(connectors, true);

    connectorNames = ccm.getConnectorNames();
    assertTrue(connectorNames.contains("connector-01"));
    assertTrue(connectorNames.contains("connector-02"));
    assertTrue(connectorNames.contains("connector-03"));

    ConnectorCoordinator coordinator = ccm.get("connector-01");
    Configuration config = coordinator.getConnectorConfiguration();
    assertEquals("type", "TestConnectorA", config.getTypeName());
    ImportExportConnectorTest.assertContains(config.getMap(), "Color", "blue");
    assertEquals("checkpoint", "checkpoint", coordinator.getConnectorState());
    Schedule schedule = coordinator.getConnectorSchedule();
    assertFalse(schedule.isDisabled());
    assertTrue((schedule.getLoad() == 100));
    assertTrue((schedule.getRetryDelayMillis() == 300000));
    assertEquals("0-0", schedule.getTimeIntervalsAsString());

    coordinator = ccm.get("connector-02");
    config = coordinator.getConnectorConfiguration();
    assertEquals("type", "TestConnectorA", config.getTypeName());
    ImportExportConnectorTest.assertContains(config.getMap(), "Color", "red");
    assertEquals("checkpoint", "checkpoint", coordinator.getConnectorState());
    schedule = coordinator.getConnectorSchedule();
    assertFalse(schedule.isDisabled());
    assertTrue((schedule.getLoad() == 100));
    assertTrue((schedule.getRetryDelayMillis() == 300000));
    assertEquals("0-0", schedule.getTimeIntervalsAsString());
  }

  // Test that connectors not in the import set are removed on import.
  public final void testDoRemove() throws Exception {
    Context context = Context.getInstance();
    ConnectorCoordinatorMap ccm = getCoordinatorMap();

    addConnector("connector-01", "red");
    addConnector("connector-02", "blue");

    ImportExportConnectorList connectors = ImportExport.getConnectors();
    assertTrue(connectors.size() == 2);

    // Change the colors, so they get imported differently.
    for (ImportExportConnector connector : connectors) {
      Map<String, String> configMap = connector.getConfigMap();
      String color = configMap.get("Color");
      configMap.put("Color", (color.equals("red") ? "blue" : "red"));
    }

    String exportXml = ImportExportConnectorListTest.asXmlString(connectors);

    connectors = fromXmlString(exportXml);
    assertTrue(connectors.size() == 2);

    // Add a new connector.
    addConnector("connector-03", "green");

    Set<String> connectorNames = ccm.getConnectorNames();
    assertTrue(connectorNames.contains("connector-01"));
    assertTrue(connectorNames.contains("connector-02"));
    assertTrue(connectorNames.contains("connector-03"));

    ImportExport.setConnectors(connectors, false);

    connectorNames = ccm.getConnectorNames();
    assertTrue(connectorNames.contains("connector-01"));
    assertTrue(connectorNames.contains("connector-02"));
    assertFalse(connectorNames.contains("connector-03"));

    ConnectorCoordinator coordinator = ccm.get("connector-01");
    Configuration config = coordinator.getConnectorConfiguration();
    assertEquals("type", "TestConnectorA", config.getTypeName());
    ImportExportConnectorTest.assertContains(config.getMap(), "Color", "blue");
    assertEquals("checkpoint", "checkpoint", coordinator.getConnectorState());
    Schedule schedule = coordinator.getConnectorSchedule();
    assertFalse(schedule.isDisabled());
    assertTrue((schedule.getLoad() == 100));
    assertTrue((schedule.getRetryDelayMillis() == 300000));
    assertEquals("0-0", schedule.getTimeIntervalsAsString());

    coordinator = ccm.get("connector-02");
    config = coordinator.getConnectorConfiguration();
    assertEquals("type", "TestConnectorA", config.getTypeName());
    ImportExportConnectorTest.assertContains(config.getMap(), "Color", "red");
    assertEquals("checkpoint", "checkpoint", coordinator.getConnectorState());
    schedule = coordinator.getConnectorSchedule();
    assertFalse(schedule.isDisabled());
    assertTrue((schedule.getLoad() == 100));
    assertTrue((schedule.getRetryDelayMillis() == 300000));
    assertEquals("0-0", schedule.getTimeIntervalsAsString());
  }

  private static ImportExportConnectorList fromXmlString(String xmlString) {
    Document document =
        XmlParseUtil.parse(xmlString, new SAXParseErrorHandler(), null);
    Element connectorsElement = document.getDocumentElement();
    ImportExportConnectorList connectors = new ImportExportConnectorList();
    connectors.fromXml(document.getDocumentElement(),
        LegacyImportExportConnector.class);
    return connectors;
  }
}
