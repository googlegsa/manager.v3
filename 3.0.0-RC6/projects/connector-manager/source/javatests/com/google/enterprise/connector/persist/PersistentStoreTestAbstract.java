// Copyright 2008 Google Inc.
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

package com.google.enterprise.connector.persist;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class to test persistent stores.
 */
/* The strange name is to avoid having to change the build rules. */
public abstract class PersistentStoreTestAbstract extends TestCase {
  static final String TYPENAME = "TestConnectorA";
  static final String CONFIG_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?><beans></beans>";

  /** Gets a new instance of Configuration. */
  protected static Configuration getConfiguration() {
    return getConfiguration(TYPENAME);
  }

  /** Gets a new instance of Configuration. */
  protected static Configuration getConfiguration(String typeName) {
    return new Configuration(typeName, Collections.<String, String>emptyMap(),
        CONFIG_XML);
  }

  /** Gets a new instance of Schedule. */
  protected static Schedule getSchedule() {
    return new Schedule("name:200:300000:0-0");
  }

  /** Gets a new instance of String. */
  protected static String getCheckpoint() {
    return new Date().toString();
  }

  /**
   * Gets a StoreContext.  May be overridden as needed by subclasses.
   */
  protected StoreContext getStoreContext(String connectorName) {
    return getStoreContext(connectorName, TYPENAME);
  }

  /**
   * Gets a StoreContext.
   * May be overridden as needed by subclasses.
   */
  protected StoreContext getStoreContext(String connectorName, String typeName) {
    return new StoreContext(connectorName, typeName);
  }

  protected PersistentStore store;

  private Configuration configuration;
  private Schedule schedule;
  private String checkpoint;

  @Override
  protected void setUp() throws Exception {
    configuration = getConfiguration();
    schedule = getSchedule();
    checkpoint = getCheckpoint();
  }

  // Tests if the exception is thrown correctly when the context is null.
  public void testGetConnectorScheduleNullContext() {
    try {
      store.getConnectorSchedule(null);
      fail("failed to throw exception");
    } catch (NullPointerException e) {
      assertEquals("StoreContext may not be null.", e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the context is null.
  public void testGetConnectorStateNullContext() {
    try {
      store.getConnectorState(null);
      fail("failed to throw exception");
    } catch (NullPointerException e) {
      assertEquals("StoreContext may not be null.", e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the context is null.
  public void testGetConnectorConfigurationNullContext() {
    try {
      store.getConnectorConfiguration(null);
      fail("failed to throw exception");
    } catch (NullPointerException e) {
      assertEquals("StoreContext may not be null.", e.getMessage());
    }
  }

  // Tests getting and setting for a valid connector name and schedule.
  public void testGetandSetConnectorSchedule() {
    String connectorName = "connectorA";
    StoreContext storeContext = getStoreContext(connectorName);
    String expectedSchedule = connectorName + ":200:300000:0-0";
    store.storeConnectorSchedule(storeContext, new Schedule(expectedSchedule));
    Schedule resultSchedule = store.getConnectorSchedule(storeContext);
    compareSchedules(expectedSchedule, resultSchedule);
  }

  // Tests changing a schedule.
  public void testChangeConnectorSchedule() {
    String connectorName = "connectorB";
    StoreContext storeContext = getStoreContext(connectorName);
    String expectedSchedule = connectorName + ":200:300000:0-0";
    store.storeConnectorSchedule(storeContext, new Schedule(expectedSchedule));
    Schedule resultSchedule = store.getConnectorSchedule(storeContext);
    compareSchedules(expectedSchedule, resultSchedule);
    // Now change it and make sure it sticks.
    expectedSchedule = connectorName + ":1000:-1:0-0";
    store.storeConnectorSchedule(storeContext, new Schedule(expectedSchedule));
    resultSchedule = store.getConnectorSchedule(storeContext);
    compareSchedules(expectedSchedule, resultSchedule);
  }

  // Tests getting schedule for an unknown connector
  public void testGetConnectorScheduleNoConnector() {
    Schedule schedule = store.getConnectorSchedule(
        getStoreContext("some weird connector name"));
    assertNull(schedule);
  }

  // Tests schedule cannot be retrieved after removal.
  public void testRemoveConnectorSchedule() {
    String connectorName = "foo";
    StoreContext storeContext = getStoreContext(connectorName);
    Schedule resultSchedule = store.getConnectorSchedule(storeContext);
    assertNull(resultSchedule);
    String expectedSchedule = connectorName + ":500:300000:18-0:0-6";
    store.storeConnectorSchedule(storeContext,
        new Schedule(expectedSchedule));
    resultSchedule = store.getConnectorSchedule(storeContext);
    compareSchedules(expectedSchedule, resultSchedule);
    store.removeConnectorSchedule(storeContext);
    resultSchedule = store.getConnectorSchedule(storeContext);
    assertNull(resultSchedule);
  }

  // Tests getting and setting for a valid connector name and state.
  public void testGetandSetConnectorState() {
    String connectorName = "connectorA";
    StoreContext storeContext = getStoreContext(connectorName);
    String expectedState = "state of connectorA";
    store.storeConnectorState(storeContext, expectedState);
    String resultState = store.getConnectorState(storeContext);
    assertEquals(expectedState, resultState);
  }

  // Tests changing connector state.
  public void testChangeConnectorState() {
    String connectorName = "connectorB";
    StoreContext storeContext = getStoreContext(connectorName);
    String expectedState = "state of connectorB";
    store.storeConnectorState(storeContext, expectedState);
    String resultState = store.getConnectorState(storeContext);
    assertEquals(expectedState, resultState);
    // Now change the state and make sure it takes.
    expectedState = "changed state of connectorB";
    store.storeConnectorState(storeContext, expectedState);
    resultState = store.getConnectorState(storeContext);
    assertEquals(expectedState, resultState);
  }

  // Tests getting state for an unknown connector.
  public void testGetConnectorStateNoConnector() {
    String state = store.getConnectorState(
        getStoreContext("some weird connector name"));
    assertNull(state);
  }

  // Tests state cannot be retrieved after removal.
  public void testRemoveConnectorState() {
    String connectorName = "foo";
    StoreContext storeContext = getStoreContext(connectorName);
    String state = store.getConnectorState(storeContext);
    assertNull(state);
    String connectorState = "foo's state";
    store.storeConnectorState(storeContext, connectorState);
    state = store.getConnectorState(storeContext);
    assertEquals(connectorState, state);
    store.removeConnectorState(storeContext);
    state = store.getConnectorState(storeContext);
    assertNull(state);
  }

  // Tests Configuration getters.
  public void testConfiguration() {
    Map<String, String> configMap = new HashMap<String, String>();
    configMap.put("property1", "value1");
    configMap.put("property2", "2");
    configMap.put("property3", "true");
    Configuration resultConfig =
        new Configuration(TYPENAME, configMap, CONFIG_XML);
    ConnectorTestUtils.compareMaps(configMap, resultConfig.getMap());
    assertEquals(TYPENAME, resultConfig.getTypeName());
    assertEquals(CONFIG_XML, resultConfig.getXml());
  }

  // Tests getting and setting for a valid connector name and config.
  public void testGetandSetConnectorConfiguration() {
    String connectorName = "connectorA";
    StoreContext storeContext = getStoreContext(connectorName);
    Map<String, String> configMap = new HashMap<String, String>();
    configMap.put("property1", "value1");
    configMap.put("property2", "2");
    configMap.put("property3", "true");
    Configuration expectedConfig =
        new Configuration(TYPENAME, configMap, CONFIG_XML);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    Configuration resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareConfigurations(expectedConfig, resultConfig);
  }

  // Tests changing a connector configuration.
  public void testChangeConnectorConfiguration() {
    String connectorName = "connectorB";
    StoreContext storeContext = getStoreContext(connectorName);
    Map<String, String> configMap = new HashMap<String, String>();
    configMap.put("property1", "value1");
    configMap.put("property2", "2");
    configMap.put("property3", "true");
    Configuration expectedConfig =
        new Configuration(TYPENAME, configMap, CONFIG_XML);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    Configuration resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareConfigurations(expectedConfig, resultConfig);

    // Now change the configuration and make sure it sticks.
    configMap.remove("property2");
    configMap.put("property4", "score");
    expectedConfig = new Configuration(TYPENAME, configMap, null);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareConfigurations(expectedConfig, resultConfig);
  }

  // Tests getting and setting a configuration that should encrypt
  // some properties.
  public void testEncryptedConnectorConfiguration() {
    String connectorName = "connectorB";
    StoreContext storeContext = getStoreContext(connectorName);
    Map<String, String> configMap = new HashMap<String, String>();
    configMap.put("property1", "value1");
    configMap.put("property2", "2");
    configMap.put("property3", "true");
    configMap.put("password", "fred");
    configMap.put("PASSWORDS", "fred");
    configMap.put("xyzpasswordzy", "fred");
    Configuration expectedConfig =
        new Configuration(TYPENAME, configMap, null);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    Configuration resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareConfigurations(expectedConfig, resultConfig);
  }

  // Tests getting configuration for an unknown connector.
  public void testGetConnectorConfigurationNoConnector() {
    Configuration config = store.getConnectorConfiguration(
        getStoreContext("some weird connector name"));
    // Should return null, not an empty map.
    assertNull(config);
  }

  // Tests configuration cannot be retrieved after removal.
  public void testRemoveConnectorConfiguration() {
    String connectorName = "foo";
    Map<String, String> configMap = new HashMap<String, String>();
    configMap.put("property1", "value1");
    configMap.put("property2", "2");
    configMap.put("property3", "true");
    StoreContext storeContext = getStoreContext(connectorName);
    Configuration config = store.getConnectorConfiguration(storeContext);
    assertNull(config);
    store.storeConnectorConfiguration(storeContext,
        new Configuration(TYPENAME, configMap, CONFIG_XML));
    config = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(configMap, config.getMap());
    store.removeConnectorConfiguration(storeContext);
    config = store.getConnectorConfiguration(storeContext);
    assertNull(config);
  }

  /** Tests the inventory of a store with one object. */
  public void testInventoryOneObject() {
    StoreContext context = getStoreContext("name");
    checkIsEmpty(store);
    store.storeConnectorConfiguration(context, configuration);
    checkContains(store, context);
    ConnectorTestUtils.compareConfigurations(configuration,
        store.getConnectorConfiguration(context));
    assertNull(store.getConnectorState(context));
    assertNull(store.getConnectorSchedule(context));
    store.removeConnectorConfiguration(context);
    checkIsEmpty(store);
  }

  /** Tests the inventory of a store with multiple object. */
  public void testInventoryMultipleObjects() {
    StoreContext context = getStoreContext("name");

    checkIsEmpty(store);
    store.storeConnectorConfiguration(context, configuration);
    store.storeConnectorSchedule(context, schedule);
    store.storeConnectorState(context, checkpoint);
    checkContains(store, context);

    ConnectorTestUtils.compareConfigurations(configuration,
        store.getConnectorConfiguration(context));
    assertEquals(schedule, store.getConnectorSchedule(context));
    assertEquals(checkpoint, store.getConnectorState(context));

    store.removeConnectorState(context);
    checkContains(store, context);
    store.removeConnectorSchedule(context);
    checkContains(store, context);
    store.removeConnectorConfiguration(context);
    checkIsEmpty(store);
  }

  /** Tests the inventory of a store with one object in multiple instances. */
  public void testInventoryMultipleInstances() {
    StoreContext one = getStoreContext("one");
    StoreContext two = getStoreContext("two");
    Configuration configuration2 = getConfiguration();
    assertNotSame(configuration, configuration2);

    checkIsEmpty(store);
    store.storeConnectorConfiguration(one, configuration);
    checkContains(store, one);
    store.storeConnectorConfiguration(two, configuration2);
    checkContains(store, one);
    checkContains(store, two);

    ConnectorTestUtils.compareConfigurations(configuration2,
        store.getConnectorConfiguration(two));
    ConnectorTestUtils.compareConfigurations(configuration,
        store.getConnectorConfiguration(one));

    assertNull(store.getConnectorState(one));
    assertNull(store.getConnectorSchedule(one));
    assertNull(store.getConnectorState(two));
    assertNull(store.getConnectorSchedule(two));

    store.removeConnectorConfiguration(two);
    // TODO: checkNotContains(store, two);
    checkContains(store, one);
    store.removeConnectorConfiguration(one);
    checkIsEmpty(store);
  }

  /** Tests the inventory of a store with instances in multiple types. */
  public void testInventoryMultipleTypes() {
    StoreContext one = getStoreContext("one");
    StoreContext two = getStoreContext("two", "TestConnectorB");
    Configuration configuration2 = getConfiguration("TestConnectorB");

    checkIsEmpty(store);
    store.storeConnectorConfiguration(one, configuration);
    checkContains(store, one);
    store.storeConnectorConfiguration(two, configuration2);
    checkContains(store, one);
    checkContains(store, two);

    ConnectorTestUtils.compareConfigurations(configuration2,
        store.getConnectorConfiguration(two));
    ConnectorTestUtils.compareConfigurations(configuration,
        store.getConnectorConfiguration(one));

    assertNull(store.getConnectorState(one));
    assertNull(store.getConnectorSchedule(one));
    assertNull(store.getConnectorState(two));
    assertNull(store.getConnectorSchedule(two));

    store.removeConnectorConfiguration(two);
    // TODO: checkNotContains(store, two);
    checkContains(store, one);
    store.removeConnectorConfiguration(one);
    checkIsEmpty(store);
  }

  /**
   * Tests the inventory of a store with multiple objects in multiple
   * instances.
   */
  public void testInventoryComplete() {
    StoreContext one = getStoreContext("one");
    StoreContext two = getStoreContext("two");
    Configuration configurationTwo = getConfiguration();
    Schedule scheduleTwo = getSchedule();
    assertNotSame(configuration, configurationTwo);
    assertNotSame(schedule, scheduleTwo);

    checkIsEmpty(store);
    store.storeConnectorConfiguration(one, configuration);
    store.storeConnectorSchedule(one, schedule);
    store.storeConnectorState(one, checkpoint);
    checkContains(store, one);
    store.storeConnectorConfiguration(two, configurationTwo);
    store.storeConnectorSchedule(two, scheduleTwo);
    // Leaving two's checkpoint null.
    checkContains(store, one);
    checkContains(store, two);

    ConnectorTestUtils.compareConfigurations(configuration,
        store.getConnectorConfiguration(one));
    assertEquals(schedule, store.getConnectorSchedule(one));
    assertEquals(checkpoint, store.getConnectorState(one));

    ConnectorTestUtils.compareConfigurations(configurationTwo,
        store.getConnectorConfiguration(two));
    assertEquals(scheduleTwo, store.getConnectorSchedule(two));
    assertNull(store.getConnectorState(two));

    store.removeConnectorState(one);
    checkContains(store, one);
    store.removeConnectorSchedule(one);
    checkContains(store, one);
    store.removeConnectorConfiguration(one);
    // TODO: checkNotContains(store, one);

    store.removeConnectorSchedule(two);
    checkContains(store, two);
    store.removeConnectorConfiguration(two);
    checkIsEmpty(store);
  }

  private static void checkIsEmpty(PersistentStore store) {
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
        store.getInventory();
    assertTrue(inventory.toString(), inventory.isEmpty());
  }

  private static void checkContains(PersistentStore store,
                                    StoreContext context) {
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
        store.getInventory();
    assertFalse(inventory.toString(), inventory.isEmpty());
    assertTrue(inventory.keySet().toString(),
        inventory.keySet().contains(context));
  }

  private static void compareSchedules(String expected, Schedule result) {
    assertEquals(new Schedule(expected), result);
  }
}
