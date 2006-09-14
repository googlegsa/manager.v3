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
public class ScheduleStoreTest extends TestCase {

  // Tests getting and setting for a valid connector name and schedule.
  
  public void testGetandSetConnectorSchedule() throws PersistentStoreException {
    ScheduleStore store = new ScheduleStore();
    String expectedSchedule = "schedule of connectorA";
    String connectorName = "connectorA";
    store.storeConnectorSchedule(connectorName, expectedSchedule);
    String resultSchedule = store.getConnectorSchedule(connectorName);
    Assert.assertTrue(resultSchedule.equals(expectedSchedule));
  }
  
  // Tests getting schedule for an unknown connector
  public void testGetConnectorSchedule1() throws PersistentStoreException {
    ScheduleStore store = new ScheduleStore();
    String schedule = store.getConnectorSchedule("some wierd connector name");
    Assert.assertNull(schedule);
  }
  
  // Tests if the exception is thrown correctly when the connector name is null. 
  public void testGetConnectorSchedule2() {
    ScheduleStore store = new ScheduleStore();
    boolean exceptionCaught = false;
    try {
      String schedule = store.getConnectorSchedule(null);
    } catch (PersistentStoreException e) {
        exceptionCaught = true;
    }
    Assert.assertTrue(exceptionCaught);
  }
}