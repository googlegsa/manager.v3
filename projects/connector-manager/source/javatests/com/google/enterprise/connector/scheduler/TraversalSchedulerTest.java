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

import com.google.enterprise.connector.common.WorkQueue;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.instantiator.SpringInstantiator;
import com.google.enterprise.connector.monitor.HashMapMonitor;
import com.google.enterprise.connector.persist.ConnectorScheduleStore;
import com.google.enterprise.connector.persist.MockConnectorScheduleStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests the Scheduler.
 *
 */
public class TraversalSchedulerTest extends TestCase {
  private TraversalScheduler runWithSchedules(List schedules, 
      Instantiator instantiator, boolean shutdown) {
    WorkQueue workQueue = new WorkQueue(2, 5000);
    ConnectorScheduleStore scheduleStore = 
      createConnectorScheduleStore(schedules);
    TraversalScheduler scheduler = 
      new TraversalScheduler(instantiator, new HashMapMonitor(),
        workQueue, scheduleStore);
    scheduler.init();
    Thread thread = new Thread(scheduler, "TraversalScheduler");
    thread.start();
    if (shutdown) {
      // sleep to give it a chance to schedule something
      try {  
        Thread.sleep(200);
      } catch (InterruptedException ie) {
        ie.printStackTrace();
        Assert.fail(ie.toString());
      }
      scheduler.shutdown(false, 5000);
    }
    
    return scheduler;
  }
  
  private TraversalScheduler runWithSchedules(List schedules,
      Instantiator instantiator) {
    return runWithSchedules(schedules, instantiator, true);
  }

  /**
   * Create an object that can return all connector instances referenced in 
   * MockInstantiator.
   * @return the ConnectorConfigStore
   */
  private ConnectorScheduleStore createConnectorScheduleStore(List schedules) {
    ConnectorScheduleStore store = new MockConnectorScheduleStore();
    Iterator iter = schedules.iterator();
    while (iter.hasNext()) {
      Schedule schedule = (Schedule) iter.next();
      String connectorName = schedule.getConnectorName();
      String connectorSchedule = schedule.toString();
      store.storeConnectorSchedule(connectorName, connectorSchedule);
    }
    return store;
  }
  
  private Instantiator createMockInstantiator() {
    return new MockInstantiator();
  }
  
  private Instantiator createRealInstantiator() {
    // set up a pusher
    MockPusher pusher = new MockPusher(System.out);

    // set up a ConnectorStateStore
    MockConnectorStateStore css = new MockConnectorStateStore();

    Instantiator instantiator =
      new SpringInstantiator(pusher, css);
    
    return instantiator;
  }
  
  /**
   * Retrieve a schedule that will always run the particular traverser.
   * @param traverserName name of the traverser
   * @param (optional) retry delay milliseconds
   * @return a List of Schedule objects
   */
  private List getSchedules(String traverserName, int delay) {
    List schedules = new ArrayList();
    List intervals = new ArrayList();
    intervals.add(new ScheduleTimeInterval(
      new ScheduleTime(0),
      new ScheduleTime(0)));
    Schedule schedule = new Schedule(traverserName, 60, delay, intervals);
    schedules.add(schedule);
    
    return schedules;
  }
  
  private List getSchedules(String traverserName) {
    return getSchedules(traverserName, 0);
  }
  
  public void testRemoveConnector() {
    String connectorName = MockInstantiator.TRAVERSER_NAME_LONG_RUNNING;
    List schedules = getSchedules(connectorName);
    TraversalScheduler scheduler = 
      runWithSchedules(schedules, createMockInstantiator(), false);

    // sleep to give it a chance to schedule something
    try {  
      Thread.sleep(100);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
      Assert.fail(ie.toString());
    }

    scheduler.removeConnector(connectorName);   
  }
  
  public void testNoopTraverser() {
    List schedules = getSchedules(MockInstantiator.TRAVERSER_NAME_NOOP);
    runWithSchedules(schedules, createMockInstantiator());  
  }
  
  /**
   * Test a long running traverser and show that it can properly get 
   * interrupted.
   */
  public void testLongRunningTraverser() {
    List schedules = getSchedules(MockInstantiator.TRAVERSER_NAME_LONG_RUNNING);
    runWithSchedules(schedules, createMockInstantiator());
  }

  /**
   * Test a traverser that doesn't get interrupted.  We ignore the thread
   * eventually.
   */
  public void testNeverEndingTraverser() {
    List schedules = getSchedules(MockInstantiator.TRAVERSER_NAME_NEVER_ENDING);
    runWithSchedules(schedules, createMockInstantiator());
  }

  /**
   * Test a traverser that can get interrupted.
   */
  public void testInterruptibleTraverser() {
    List schedules = getSchedules(MockInstantiator.TRAVERSER_NAME_INTERRUPTIBLE);
    runWithSchedules(schedules, createMockInstantiator());
  }
  
  public void testRequestsMoreTimeTraverser() {
    List schedules = getSchedules(
        MockInstantiator.TRAVERSER_NAME_REQUESTS_MORE_TIME);
    runWithSchedules(schedules, createMockInstantiator());
  }

  /**
   * Test that tests two mock Traverser objects.
   */
  public void testTwoTraversers() {
    List schedules = getSchedules(MockInstantiator.TRAVERSER_NAME1);
    schedules.addAll(getSchedules(MockInstantiator.TRAVERSER_NAME2));
    runWithSchedules(schedules, createMockInstantiator());
  }
  
  public void testRealInstantiator() {
    List schedules = getSchedules("connectorA");
    schedules.addAll(getSchedules("connectorB"));
    runWithSchedules(schedules, createRealInstantiator());
  }  
}
