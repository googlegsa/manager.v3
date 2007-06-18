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

package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.persist.ConnectorScheduleStore;
import com.google.enterprise.connector.persist.MockConnectorScheduleStore;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Test HostLoadManager class.
 */
public class HostLoadManagerTest extends TestCase {
  private ConnectorScheduleStore getScheduleStore() {
    return new MockConnectorScheduleStore();
  }
  
  private static void addLoad(ConnectorScheduleStore scheduleStore, 
      String connectorName, int load) {
    Schedule schedule = new Schedule(connectorName + ":" + load + ":0-0");
    String connectorSchedule = schedule.toString();
    scheduleStore.storeConnectorSchedule(connectorName, connectorSchedule);
  }
  
  public void testMaxFeedRateLimit() {
    final String connectorName = "cn1";
    ConnectorScheduleStore scheduleStore = getScheduleStore();
    addLoad(scheduleStore, connectorName, 60);
    HostLoadManager hostLoadManager = new HostLoadManager(scheduleStore);
    hostLoadManager.updateNumDocsTraversed(connectorName, 60);
    Assert.assertEquals(0, hostLoadManager.determineBatchHint(connectorName));
  }
  
  public void testMultipleUpdates() {
    final String connectorName = "cn1";
    ConnectorScheduleStore scheduleStore = getScheduleStore();
    addLoad(scheduleStore, connectorName, 60);
    HostLoadManager hostLoadManager = new HostLoadManager(scheduleStore);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    Assert.assertEquals(30, hostLoadManager.determineBatchHint(connectorName));    
  }
  
  public void testMultipleConnectors() {
    final String connectorName1 = "cn1";
    final String connectorName2 = "cn2";
    ConnectorScheduleStore scheduleStore = getScheduleStore();
    addLoad(scheduleStore, connectorName1, 60);
    addLoad(scheduleStore, connectorName2, 60);
    HostLoadManager hostLoadManager = new HostLoadManager(scheduleStore);
    hostLoadManager.updateNumDocsTraversed(connectorName1, 60);
    Assert.assertEquals(0, hostLoadManager.determineBatchHint(connectorName1));

    hostLoadManager.updateNumDocsTraversed(connectorName2, 50);
    Assert.assertEquals(10, hostLoadManager.determineBatchHint(connectorName2));
  }
  
  public void testPeriod() {
    final long periodInMillis = 1000;
    final String connectorName = "cn1";
    ConnectorScheduleStore scheduleStore = getScheduleStore();
    addLoad(scheduleStore, connectorName, 3600);
    HostLoadManager hostLoadManager = 
      new HostLoadManager(periodInMillis, scheduleStore);
    hostLoadManager.updateNumDocsTraversed(connectorName, 55);
    Assert.assertEquals(5, hostLoadManager.determineBatchHint(connectorName));
    // sleep a period (and then some) so that batchHint is reset 
    try {
      // extra time in ms in case sleeping the period is not long enough
      final long extraTime = 200;  
      Thread.sleep(periodInMillis + extraTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Assert.assertEquals(60, hostLoadManager.determineBatchHint(connectorName));
    hostLoadManager.updateNumDocsTraversed(connectorName, 15);
    Assert.assertEquals(45, hostLoadManager.determineBatchHint(connectorName));
    
  }
}
