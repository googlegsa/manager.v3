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
    store.storeConnectorSchedule(connectorName, expectedSchedule);
    Assert.assertTrue(store.flush());
    String resultSchedule = store.getConnectorSchedule(connectorName);
    Assert.assertTrue(resultSchedule.equals(expectedSchedule));
  }
  
  // Tests getting schedule for an unknown connector
  public void testGetConnectorSchedule1() {
    String schedule = store.getConnectorSchedule("some wierd connector name");
    Assert.assertNull(schedule);
  }
  
  // Tests if the exception is thrown correctly when the connector name is null. 
  public void testGetConnectorSchedule2() {
    boolean exceptionCaught = false;
    String schedule = null;
    try {
      schedule = store.getConnectorSchedule(null);
    } catch (NullPointerException e) {
        exceptionCaught = true;
    }
    Assert.assertTrue(exceptionCaught);
    Assert.assertNull(schedule);
  }

  public void testRemoveConnector() {
    String connectorName = "foo";
    String connectorSchedule = "foo's schedule";
    String schedule = store.getConnectorSchedule(connectorName);
    Assert.assertNull(schedule);
    store.storeConnectorSchedule(connectorName, connectorSchedule);
    schedule = store.getConnectorSchedule(connectorName);
    Assert.assertEquals(connectorSchedule, schedule);
    store.removeConnectorSchedule(connectorName);
    schedule = store.getConnectorSchedule(connectorName);
    Assert.assertNull(schedule);
  }
  
  //Tests getting and setting for a valid connector name and state.  
  public void testGetandSetConnectorState() {
    String expectedState = "state of connectorA";
    String connectorName = "connectorA";
    store.storeConnectorState(connectorName, expectedState);
    Assert.assertTrue(store.flush());
    String resultState = store.getConnectorState(connectorName);
    Assert.assertTrue(resultState.equals(expectedState));
  }
  
  //Tests getting state for an unknown connector.
  public void testGetConnectorState1() {
    String state = store.getConnectorState("some wierd connector name");
    Assert.assertNull(state);
  }
  
  // Tests if the exception is thrown correctly when the connector name is null. 
  public void testGetConnectorState2() {
    boolean exceptionCaught = false;
    try {
      String state = store.getConnectorState(null);
    } catch (NullPointerException e) {
        exceptionCaught = true;
    }
    Assert.assertTrue(exceptionCaught);
  }  

  public void testRemoveState() {
    String connectorName = "foo";
    String connectorState = "foo's state";
    String state = store.getConnectorState(connectorName);
    Assert.assertNull(state);
    store.storeConnectorState(connectorName, connectorState);
    state = store.getConnectorState(connectorName);
    Assert.assertEquals(connectorState, state);
    store.removeConnectorState(connectorName);
    state = store.getConnectorState(connectorName);
    Assert.assertNull(state);
  }
}