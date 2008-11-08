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

package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test HostLoadManager class.
 */
public class HostLoadManagerTest extends TestCase {

  private MockInstantiator instantiator = new MockInstantiator();

  private void addLoad(String connectorName, int load) {
    Schedule schedule = new Schedule(connectorName + ":" + load + ":100:0-0");
    String connectorSchedule = schedule.toString();
    try {
      instantiator.setConnectorSchedule(connectorName, connectorSchedule);
    } catch (ConnectorNotFoundException cnfe) {
      // This test attempts to add schedules to connectors that do not exist.
      // If there is not yet a connector with this name, create a dummy one.
      try {
        instantiator.setupTraverser(connectorName, null);
        instantiator.setConnectorSchedule(connectorName, connectorSchedule);
      } catch (ConnectorNotFoundException e) {
        fail("Unexpected ConnectorNotFoundException : " + e.toString());
      }
    }
  }
  
  public void testMaxFeedRateLimit() {
    final String connectorName = "cn1";
    addLoad(connectorName, 60);
    HostLoadManager hostLoadManager = new HostLoadManager(instantiator);
    hostLoadManager.updateNumDocsTraversed(connectorName, 60);
    assertEquals(0, hostLoadManager.determineBatchHint(connectorName));
  }
  
  public void testMultipleUpdates() {
    final String connectorName = "cn1";
    addLoad(connectorName, 60);
    HostLoadManager hostLoadManager = new HostLoadManager(instantiator);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    hostLoadManager.updateNumDocsTraversed(connectorName, 10);
    assertEquals(30, hostLoadManager.determineBatchHint(connectorName));    
  }
  
  public void testMultipleConnectors() {
    final String connectorName1 = "cn1";
    final String connectorName2 = "cn2";
    addLoad(connectorName1, 60);
    addLoad(connectorName2, 60);
    HostLoadManager hostLoadManager = new HostLoadManager(instantiator);
    hostLoadManager.updateNumDocsTraversed(connectorName1, 60);
    assertEquals(0, hostLoadManager.determineBatchHint(connectorName1));

    hostLoadManager.updateNumDocsTraversed(connectorName2, 50);
    assertEquals(10, hostLoadManager.determineBatchHint(connectorName2));
  }
  
  public void testPeriod() {
    final long periodInMillis = 1000;
    final String connectorName = "cn1";
    addLoad(connectorName, 3600);
    HostLoadManager hostLoadManager = 
      new HostLoadManager(instantiator, periodInMillis);
    hostLoadManager.updateNumDocsTraversed(connectorName, 55);
    assertEquals(5, hostLoadManager.determineBatchHint(connectorName));
    // sleep a period (and then some) so that batchHint is reset 
    try {
      // extra time in ms in case sleeping the period is not long enough
      final long extraTime = 200;  
      Thread.sleep(periodInMillis + extraTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertEquals(60, hostLoadManager.determineBatchHint(connectorName));
    hostLoadManager.updateNumDocsTraversed(connectorName, 15);
    assertEquals(45, hostLoadManager.determineBatchHint(connectorName));
    
  }
  
  public void testRetryDelay() {
    final long periodInMillis = 1000;
    final String connectorName = "cn1";
    addLoad(connectorName, 60);
    HostLoadManager hostLoadManager = 
      new HostLoadManager(instantiator, periodInMillis);
    assertEquals(false, hostLoadManager.shouldDelay(connectorName));
    hostLoadManager.connectorFinishedTraversal(connectorName);
    assertEquals(true, hostLoadManager.shouldDelay(connectorName));
    // sleep more than 100ms the time set in MockConnectorSchedule
    // so that this connector can be allowed to run again without delay
    try {
      final long sleepTime = 250;  
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertEquals(false, hostLoadManager.shouldDelay(connectorName));
  }
}
