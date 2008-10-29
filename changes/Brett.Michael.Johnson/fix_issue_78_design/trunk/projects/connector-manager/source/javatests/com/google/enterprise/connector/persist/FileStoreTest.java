// Copyright 2008 Google Inc.  All Rights Reserved.
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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Class to test ScheduleStore.
 */
public class FileStoreTest extends TestCase {

  protected FileStore store;
  protected File storeDir;

  protected void setUp() throws IOException {
    storeDir = new File("testdata/tmp/filestore");
    storeDir.mkdirs();
    store = new FileStore();
  }

  protected void tearDown() throws IOException {
    ConnectorTestUtils.deleteAllFiles(storeDir);
  }

  // Tests getting and setting for a valid connector name and schedule.
  public void testGetandSetConnectorSchedule() {
    String expectedSchedule = "schedule of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    store.storeConnectorSchedule(storeContext, expectedSchedule);
    String resultSchedule = store.getConnectorSchedule(storeContext);
    Assert.assertTrue(resultSchedule.equals(expectedSchedule));
  }

  // Tests getting schedule for an unknown connector
  public void testGetConnectorSchedule1() {
    String schedule = store.getConnectorSchedule(
        new StoreContext("some wierd connector name", storeDir));
    Assert.assertNull(schedule);
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorSchedule2() {
    boolean exceptionCaught = false;
    String schedule = null;
    try {
      schedule = store.getConnectorSchedule(new StoreContext(null, storeDir));
    } catch (IllegalArgumentException e) {
        exceptionCaught = true;
    }
    Assert.assertTrue(exceptionCaught);
    Assert.assertNull(schedule);
  }

  // Tests schedule cannot be retrieved after removal.
  public void testRemoveConnectorSchedule() {
    String connectorName = "foo";
    String connectorSchedule = "foo's schedule";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    String schedule = store.getConnectorSchedule(storeContext);
    Assert.assertNull(schedule);
    store.storeConnectorSchedule(storeContext, connectorSchedule);
    schedule = store.getConnectorSchedule(storeContext);
    Assert.assertEquals(connectorSchedule, schedule);
    store.removeConnectorSchedule(storeContext);
    schedule = store.getConnectorSchedule(storeContext);
    Assert.assertNull(schedule);
  }

  // Tests getting and setting for a valid connector name and state.
  public void testGetandSetConnectorState() {
    String expectedState = "state of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    store.storeConnectorState(storeContext, expectedState);
    String resultState = store.getConnectorState(storeContext);
    Assert.assertTrue(resultState.equals(expectedState));
  }

  //Tests getting state for an unknown connector.
  public void testGetConnectorState1() {
    String state = store.getConnectorState(
        new StoreContext("some wierd connector name", storeDir));
    Assert.assertNull(state);
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorState2() {
    boolean exceptionCaught = false;
    try {
      String state = store.getConnectorState(new StoreContext(null, storeDir));
    } catch (IllegalArgumentException e) {
        exceptionCaught = true;
    }
    Assert.assertTrue(exceptionCaught);
  }

  // Tests state cannot be retrieved after removal.
  public void testRemoveConnectorState() {
    String connectorName = "foo";
    String connectorState = "foo's state";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    String state = store.getConnectorState(storeContext);
    Assert.assertNull(state);
    store.storeConnectorState(storeContext, connectorState);
    state = store.getConnectorState(storeContext);
    Assert.assertEquals(connectorState, state);
    store.removeConnectorState(storeContext);
    state = store.getConnectorState(storeContext);
    Assert.assertNull(state);
  }

  // Tests getting and setting for a valid connector name and config.
  public void testGetandSetConnectorConfiguration() {
    Properties expectedConfig = new Properties();
    expectedConfig.setProperty("property1", "value1");
    expectedConfig.setProperty("property2", "2");
    expectedConfig.setProperty("property3", "true");
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    Properties resultConfig = store.getConnectorConfiguration(storeContext);
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
    String connectorName = "connectorB";
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    Properties resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, resultConfig);
  }


  // Tests getting configuration for an unknown connector.
  public void testGetConnectorConfiguration1() {
    Properties config = store.getConnectorConfiguration(
        new StoreContext("some wierd connector name", storeDir));
    // Should return null, not an empty Properties object.
    Assert.assertNull(config);
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorConfiguration2() {
    boolean exceptionCaught = false;
    StoreContext storeContext = new StoreContext(null, storeDir);
    try {
      Properties config = store.getConnectorConfiguration(storeContext);
    } catch (IllegalArgumentException e) {
        exceptionCaught = true;
    }
    Assert.assertTrue(exceptionCaught);
  }

  // Tests configuration cannot be retrieved after removal.
  public void testRemoveConnectorConfiguration() {
    String connectorName = "foo";
    Properties expectedConfig = new Properties();
    expectedConfig.setProperty("property1", "value1");
    expectedConfig.setProperty("property2", "2");
    expectedConfig.setProperty("property3", "true");
    StoreContext storeContext = new StoreContext(connectorName, storeDir);
    Properties config = store.getConnectorConfiguration(storeContext);
    Assert.assertNull(config);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    config = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, config);
    store.removeConnectorConfiguration(storeContext);
    config = store.getConnectorConfiguration(storeContext);
    Assert.assertNull(config);
  }
}
