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

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class to test persistent stores.
 */
/* The strange name is to avoid having to change the build rules. */
public abstract class PersistentStoreTestAbstract extends TestCase {
  /** Gets a new instance of Configuration. */
  protected static Configuration getConfiguration() {
    return new Configuration(null, Collections.<String, String>emptyMap(),
        null);
  }

  /** Gets a new instance of Schedule. */
  protected static Schedule getSchedule() {
    return new Schedule("name:200:300000:0-0");
  }

  /** Gets a new instance of String. */
  protected static String getCheckpoint() {
    return new Date().toString();
  }

  protected PersistentStore store;
  protected File storeDir;

  private Configuration configuration;
  private Schedule schedule;
  private String checkpoint;

  @Override
  protected void setUp() throws Exception {
    configuration = getConfiguration();
    schedule = getSchedule();
    checkpoint = getCheckpoint();
  }

  // Tests getting and setting for a valid connector name and schedule.
  public void testGetandSetConnectorSchedule() {
    String connectorName = "connectorA";
    String expectedSchedule = connectorName + ":200:300000:0-0";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    store.storeConnectorSchedule(storeContext, new Schedule(expectedSchedule));
    Schedule resultSchedule = store.getConnectorSchedule(storeContext);
    assertEquals(expectedSchedule, resultSchedule.toString());
  }

  // Tests getting schedule for an unknown connector
  public void testGetConnectorSchedule1() {
    Schedule schedule = store.getConnectorSchedule(
        new StoreContext("some weird connector name", storeDir));
    assertNull(schedule);
  }

  // Tests schedule cannot be retrieved after removal.
  public void testRemoveConnectorSchedule() {
    String connectorName = "foo";
    String connectorSchedule = connectorName + ":500:300000:18-0:0-6";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    Schedule schedule = store.getConnectorSchedule(storeContext);
    assertNull(schedule);
    store.storeConnectorSchedule(storeContext,
        new Schedule(connectorSchedule));
    schedule = store.getConnectorSchedule(storeContext);
    assertEquals(connectorSchedule, schedule.toString());
    store.removeConnectorSchedule(storeContext);
    schedule = store.getConnectorSchedule(storeContext);
    assertNull(schedule);
  }

  // Tests getting and setting for a valid connector name and state.
  public void testGetandSetConnectorState() {
    String expectedState = "state of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    store.storeConnectorState(storeContext, expectedState);
    String resultState = store.getConnectorState(storeContext);
    assertEquals(expectedState, resultState);
  }

  // Tests getting state for an unknown connector.
  public void testGetConnectorState1() {
    String state = store.getConnectorState(
        new StoreContext("some weird connector name", storeDir));
    assertNull(state);
  }

  // Tests state cannot be retrieved after removal.
  public void testRemoveConnectorState() {
    String connectorName = "foo";
    String connectorState = "foo's state";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    String state = store.getConnectorState(storeContext);
    assertNull(state);
    store.storeConnectorState(storeContext, connectorState);
    state = store.getConnectorState(storeContext);
    assertEquals(connectorState, state);
    store.removeConnectorState(storeContext);
    state = store.getConnectorState(storeContext);
    assertNull(state);
  }

  // Tests getting and setting for a valid connector name and config.
  public void testGetandSetConnectorConfiguration() {
    Map<String, String> expectedConfig = new HashMap<String, String>();
    expectedConfig.put("property1", "value1");
    expectedConfig.put("property2", "2");
    expectedConfig.put("property3", "true");
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    store.storeConnectorConfiguration(storeContext,
        new Configuration(null, expectedConfig, null));
    Configuration resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, resultConfig.getMap());
  }

  // Tests getting and setting a configuration that should encrypt
  // some properties.
  public void testEncryptedConnectorConfiguration() {
    Map<String, String> expectedConfig = new HashMap<String, String>();
    expectedConfig.put("property1", "value1");
    expectedConfig.put("property2", "2");
    expectedConfig.put("property3", "true");
    expectedConfig.put("password", "fred");
    expectedConfig.put("PASSWORDS", "fred");
    expectedConfig.put("xyzpasswordzy", "fred");
    String connectorName = "connectorB";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    store.storeConnectorConfiguration(storeContext,
        new Configuration(null, expectedConfig, null));
    Configuration resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, resultConfig.getMap());
  }


  // Tests getting configuration for an unknown connector.
  public void testGetConnectorConfiguration1() {
    Configuration config = store.getConnectorConfiguration(
        new StoreContext("some weird connector name", storeDir));
    // Should return null, not an empty map.
    assertNull(config);
  }

  // Tests configuration cannot be retrieved after removal.
  public void testRemoveConnectorConfiguration() {
    String connectorName = "foo";
    Map<String, String> expectedConfig = new HashMap<String, String>();
    expectedConfig.put("property1", "value1");
    expectedConfig.put("property2", "2");
    expectedConfig.put("property3", "true");
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    Configuration config = store.getConnectorConfiguration(storeContext);
    assertNull(config);
    store.storeConnectorConfiguration(storeContext,
        new Configuration(null, expectedConfig, null));
    config = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, config.getMap());
    store.removeConnectorConfiguration(storeContext);
    config = store.getConnectorConfiguration(storeContext);
    assertNull(config);
  }

  /** Tests the inventory of a store with one object. */
  public void testInventoryOneObject() {
    StoreContext context = new StoreContext("name");

    checkIsEmpty(store);
    store.storeConnectorState(context, checkpoint);
    checkContains(store, context);

    assertNull(store.getConnectorConfiguration(context));
    assertNull(store.getConnectorSchedule(context));
    assertSame(checkpoint, store.getConnectorState(context));

    store.removeConnectorState(context);
    checkIsEmpty(store);
  }

  /** Tests the inventory of a store with multiple object. */
  public void testInventoryMultipleObjects() {
    StoreContext context = new StoreContext("name");

    checkIsEmpty(store);
    store.storeConnectorConfiguration(context, configuration);
    store.storeConnectorSchedule(context, schedule);
    store.storeConnectorState(context, checkpoint);
    checkContains(store, context);

    assertSame(configuration, store.getConnectorConfiguration(context));
    assertSame(schedule, store.getConnectorSchedule(context));
    assertSame(checkpoint, store.getConnectorState(context));

    store.removeConnectorState(context);
    checkContains(store, context);
    store.removeConnectorConfiguration(context);
    checkContains(store, context);
    store.removeConnectorSchedule(context);
    checkIsEmpty(store);
  }

  /** Tests the inventory of a store with one object in multiple instances. */
  public void testInventoryMultipleInstances() {
    StoreContext one = new StoreContext("one");
    StoreContext two = new StoreContext("two");
    String checkpointTwo = getCheckpoint();
    assertNotSame(checkpoint, checkpointTwo);

    checkIsEmpty(store);
    store.storeConnectorState(one, checkpoint);
    checkContains(store, one);
    store.storeConnectorState(two, checkpointTwo);
    checkContains(store, one);
    checkContains(store, two);

    assertSame(checkpointTwo, store.getConnectorState(two));
    assertSame(checkpoint, store.getConnectorState(one));

    assertNull(store.getConnectorConfiguration(one));
    assertNull(store.getConnectorSchedule(one));
    assertNull(store.getConnectorConfiguration(two));
    assertNull(store.getConnectorSchedule(two));

    store.removeConnectorState(two);
    // TODO: checkNotContains(store, two);
    checkContains(store, one);
    store.removeConnectorState(one);
    checkIsEmpty(store);
  }

  /**
   * Tests the inventory of a store with multiple objects in multiple
   * instances.
   */
  public void testInventoryComplete() {
    StoreContext one = new StoreContext("one");
    StoreContext two = new StoreContext("two");
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

    assertSame(configuration, store.getConnectorConfiguration(one));
    assertSame(schedule, store.getConnectorSchedule(one));
    assertSame(checkpoint, store.getConnectorState(one));

    assertSame(configurationTwo, store.getConnectorConfiguration(two));
    assertSame(scheduleTwo, store.getConnectorSchedule(two));
    assertNull(store.getConnectorState(two));

    store.removeConnectorState(one);
    checkContains(store, one);
    store.removeConnectorConfiguration(one);
    checkContains(store, one);
    store.removeConnectorSchedule(one);
    // TODO: checkNotContains(store, one);

    store.removeConnectorConfiguration(two);
    checkContains(store, two);
    store.removeConnectorSchedule(two);
    checkIsEmpty(store);
  }

  private void checkIsEmpty(PersistentStore store) {
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
        store.getInventory();
    assertTrue(inventory.toString(), inventory.isEmpty());
  }

  private void checkContains(PersistentStore store, StoreContext context) {
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
        store.getInventory();
    assertFalse(inventory.toString(), inventory.isEmpty());
    assertTrue(inventory.keySet().toString(),
        inventory.keySet().contains(context));
  }
}
