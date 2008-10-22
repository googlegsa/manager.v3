// Copyright 2006 Google Inc.  All Rights Reserved.
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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Class to test ScheduleStore.
 */
public class PrefsStoreTest extends TestCase {

  protected PrefsStore store;

  protected void setUp() {
    // Only testing use of userRoot
    store = new PrefsStore(true);
  }

  protected void tearDown() {
    store.clear();
  }

  // Tests getting and setting for a valid connector name and schedule.

  public void testGetandSetConnectorSchedule() {
    String expectedSchedule = "schedule of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    store.storeConnectorSchedule(storeContext, expectedSchedule);
    Assert.assertTrue(store.flush());
    String resultSchedule = store.getConnectorSchedule(storeContext);
    Assert.assertTrue(resultSchedule.equals(expectedSchedule));
  }

  // Tests getting schedule for an unknown connector
  public void testGetConnectorSchedule1() {
    String schedule = store.getConnectorSchedule(
        new StoreContext("some wierd connector name"));
    Assert.assertNull(schedule);
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorSchedule2() {
    boolean exceptionCaught = false;
    String schedule = null;
    try {
      schedule = store.getConnectorSchedule(new StoreContext(null));
    } catch (NullPointerException e) {
        exceptionCaught = true;
    }
    Assert.assertTrue(exceptionCaught);
    Assert.assertNull(schedule);
  }

  public void testRemoveConnector() {
    String connectorName = "foo";
    String connectorSchedule = "foo's schedule";
    StoreContext storeContext = new StoreContext(connectorName);
    String schedule = store.getConnectorSchedule(storeContext);
    Assert.assertNull(schedule);
    store.storeConnectorSchedule(storeContext, connectorSchedule);
    schedule = store.getConnectorSchedule(storeContext);
    Assert.assertEquals(connectorSchedule, schedule);
    store.removeConnectorSchedule(storeContext);
    schedule = store.getConnectorSchedule(storeContext);
    Assert.assertNull(schedule);
  }

  //Tests getting and setting for a valid connector name and state.
  public void testGetandSetConnectorState() {
    String expectedState = "state of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    store.storeConnectorState(storeContext, expectedState);
    Assert.assertTrue(store.flush());
    String resultState = store.getConnectorState(storeContext);
    Assert.assertTrue(resultState.equals(expectedState));
  }

  //Tests getting state for an unknown connector.
  public void testGetConnectorState1() {
    String state = store.getConnectorState(
        new StoreContext("some wierd connector name"));
    Assert.assertNull(state);
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorState2() {
    boolean exceptionCaught = false;
    try {
      String state = store.getConnectorState(new StoreContext(null));
    } catch (NullPointerException e) {
        exceptionCaught = true;
    }
    Assert.assertTrue(exceptionCaught);
  }

  public void testRemoveState() {
    String connectorName = "foo";
    String connectorState = "foo's state";
    StoreContext storeContext = new StoreContext(connectorName);
    String state = store.getConnectorState(storeContext);
    Assert.assertNull(state);
    store.storeConnectorState(storeContext, connectorState);
    state = store.getConnectorState(storeContext);
    Assert.assertEquals(connectorState, state);
    store.removeConnectorState(storeContext);
    state = store.getConnectorState(storeContext);
    Assert.assertNull(state);
  }
}
