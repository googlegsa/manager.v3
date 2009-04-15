// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.util.Properties;

/**
 * Class to test PrefsStore.
 *
 * Note that PrefsStore is a deprecated class.
 */
@SuppressWarnings("deprecation")
public class PrefsStoreTest extends TestCase {

  // Can't call it a PrefsStore anymore without generating a "deprecation"
  // warning, even though I suppress "deprecation" warnings for this class.
  // See JDK bug: http://bugs.sun.com/view_bug.do?bug_id=6460147
  protected Object /* PrefsStore */ store;

  protected ConnectorConfigStore confStore;
  protected ConnectorStateStore stateStore;
  protected ConnectorScheduleStore schedStore;

  @Override
  protected void setUp() {
    // Only testing use of userRoot
    PrefsStore prefsStore = new PrefsStore(true, "testing");
    store = prefsStore;
    confStore = prefsStore;
    stateStore = prefsStore;
    schedStore = prefsStore;
  }

  @Override
  protected void tearDown() {
    ((PrefsStore)store).clear();
  }

  // Tests getting and setting for a valid connector name and schedule.
  public void testGetandSetConnectorSchedule() {
    String expectedSchedule = "schedule of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    schedStore.storeConnectorSchedule(storeContext, expectedSchedule);
    assertTrue(flushStore());
    String resultSchedule = schedStore.getConnectorSchedule(storeContext);
    assertTrue(resultSchedule.equals(expectedSchedule));
  }

  // Tests getting schedule for an unknown connector
  public void testGetConnectorSchedule1() {
    String schedule = schedStore.getConnectorSchedule(
        new StoreContext("some wierd connector name"));
    assertNull(schedule);
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorSchedule2() {
    boolean exceptionCaught = false;
    String schedule = null;
    try {
      schedule = schedStore.getConnectorSchedule(new StoreContext(null));
    } catch (NullPointerException e) {
        exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
    assertNull(schedule);
  }

  // Tests schedule cannot be retrieved after removal.
  public void testRemoveConnectorSchedule() {
    String connectorName = "foo";
    String connectorSchedule = "foo's schedule";
    StoreContext storeContext = new StoreContext(connectorName);
    String schedule = schedStore.getConnectorSchedule(storeContext);
    assertNull(schedule);
    schedStore.storeConnectorSchedule(storeContext, connectorSchedule);
    schedule = schedStore.getConnectorSchedule(storeContext);
    assertEquals(connectorSchedule, schedule);
    schedStore.removeConnectorSchedule(storeContext);
    schedule = schedStore.getConnectorSchedule(storeContext);
    assertNull(schedule);
  }

  // Tests getting and setting for a valid connector name and state.
  public void testGetandSetConnectorState() {
    String expectedState = "state of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    stateStore.storeConnectorState(storeContext, expectedState);
    assertTrue(flushStore());
    String resultState = stateStore.getConnectorState(storeContext);
    assertTrue(resultState.equals(expectedState));
  }

  //Tests getting state for an unknown connector.
  public void testGetConnectorState1() {
    String state = stateStore.getConnectorState(
        new StoreContext("some wierd connector name"));
    assertNull(state);
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorState2() {
    try {
      stateStore.getConnectorState(new StoreContext(null));
      fail("Expected exception to be thrown");
    } catch (NullPointerException expected) {
      assertEquals("Null key", expected.getMessage());
    }
  }

  // Tests state cannot be retrieved after removal.
  public void testRemoveConnectorState() {
    String connectorName = "foo";
    String connectorState = "foo's state";
    StoreContext storeContext = new StoreContext(connectorName);
    String state = stateStore.getConnectorState(storeContext);
    assertNull(state);
    stateStore.storeConnectorState(storeContext, connectorState);
    state = stateStore.getConnectorState(storeContext);
    assertEquals(connectorState, state);
    stateStore.removeConnectorState(storeContext);
    state = stateStore.getConnectorState(storeContext);
    assertNull(state);
  }

  // Tests getting and setting for a valid connector name and config.
  public void testGetandSetConnectorConfiguration() {
    Properties expectedConfig = new Properties();
    expectedConfig.setProperty("property1", "value1");
    expectedConfig.setProperty("property2", "2");
    expectedConfig.setProperty("property3", "true");
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    confStore.storeConnectorConfiguration(storeContext, expectedConfig);
    assertTrue(flushStore());
    Properties resultConfig = confStore.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, resultConfig);
  }

  // Tests getting and setting a configuration that should encrypt
  // some properties.
  public void testEncryptedConnectorConfiguration() {
    Properties expectedConfig = new Properties();
    expectedConfig.setProperty("property1", "value1");
    expectedConfig.setProperty("property2", "2");
    expectedConfig.setProperty("property3", "true");
    expectedConfig.setProperty("password", "fred");
    expectedConfig.setProperty("PASSWORDS", "fred");
    expectedConfig.setProperty("xyzpasswordzy", "fred");
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    confStore.storeConnectorConfiguration(storeContext, expectedConfig);
    assertTrue(flushStore());
    Properties resultConfig = confStore.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, resultConfig);
  }


  // Tests getting configuration for an unknown connector.
  public void testGetConnectorConfiguration1() {
    Properties config = confStore.getConnectorConfiguration(
        new StoreContext("some wierd connector name"));
    // Should return null, not an empty Properties object.
    assertNull(config);
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorConfiguration2() {
    StoreContext storeContext = new StoreContext(null);
    try {
      confStore.getConnectorConfiguration(storeContext);
      fail("Expected exception to be thrown");
    } catch (NullPointerException expected) {
      assertEquals("Null key", expected.getMessage());
    }
  }

  // Tests configuration cannot be retrieved after removal.
  public void testRemoveConnectorConfiguration() {
    String connectorName = "foo";
    Properties expectedConfig = new Properties();
    expectedConfig.setProperty("property1", "value1");
    expectedConfig.setProperty("property2", "2");
    expectedConfig.setProperty("property3", "true");
    StoreContext storeContext = new StoreContext(connectorName);
    Properties config = confStore.getConnectorConfiguration(storeContext);
    assertNull(config);
    confStore.storeConnectorConfiguration(storeContext, expectedConfig);
    config = confStore.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, config);
    confStore.removeConnectorConfiguration(storeContext);
    config = confStore.getConnectorConfiguration(storeContext);
    assertNull(config);
  }

  private boolean flushStore() {
    return ((PrefsStore)store).flush();
  }
}
