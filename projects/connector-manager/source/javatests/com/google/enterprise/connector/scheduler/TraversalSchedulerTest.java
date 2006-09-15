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
import com.google.enterprise.connector.instantiator.ConnectorTypeInstantiator;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.instantiator.SpringConnectorInstantiator;
import com.google.enterprise.connector.instantiator.SpringConnectorTypeInstantiator;
import com.google.enterprise.connector.instantiator.SpringInstantiator;
import com.google.enterprise.connector.monitor.HashMapMonitor;
import com.google.enterprise.connector.persist.FilesystemConnectorConfigStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the Scheduler.
 *
 */
public class TraversalSchedulerTest extends TestCase {
  private void runWithSchedules(List schedules, Instantiator instantiator) {
    WorkQueue workQueue = new WorkQueue(2, 5000);
    TraversalScheduler scheduler = 
      new TraversalScheduler(instantiator, new HashMapMonitor(),
        workQueue, schedules);
    scheduler.init();
    Thread thread = new Thread(scheduler);
    thread.start();
    // sleep to give it a chance to schedule something
    try {  
      Thread.sleep(200);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
      Assert.fail(ie.toString());
    }
    scheduler.shutdown(false);
  }

  private Instantiator createMockInstantiator() {
    return new MockInstantiator();
  }
  
  private Instantiator createRealInstantiator() {
    ConnectorTypeInstantiator connectorTypeInstantiator =
      new SpringConnectorTypeInstantiator();

    SpringConnectorInstantiator connectorInstantiator =
      new SpringConnectorInstantiator();
   
    // set up a ConnectorConfigStore
    ResourceLoader rl = new FileSystemResourceLoader();
    Resource resource =
        rl.getResource("testdata/staticConnectorConfig/connectors");
    File baseDir = null;
    try {
      baseDir = resource.getFile();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      fail();
    }
    FilesystemConnectorConfigStore store = new FilesystemConnectorConfigStore();
    store.setBaseDirectory(baseDir);

    // set up a pusher
    MockPusher pusher = new MockPusher(System.out);

    // set up a ConnectorStateStore
    MockConnectorStateStore css = new MockConnectorStateStore();

    // wire up the dependencies
    connectorInstantiator.setStore(store);
    connectorInstantiator.setConnectorStateStore(css);
    connectorInstantiator.setPusher(pusher);
    
    Instantiator instantiator =
      new SpringInstantiator(connectorTypeInstantiator, connectorInstantiator);
    
    return instantiator;
  }
  
  /**
   * Retrieve a schedule that will always run the particular traverser.
   * @param traverserName name of the traverser
   * @return a List of Schedule objects
   */
  private List getSchedules(String traverserName) {
    List schedules = new ArrayList();
    List intervals = new ArrayList();
    intervals.add(new ScheduleTimeInterval(
      new ScheduleTime(0),
      new ScheduleTime(0)));
    Schedule schedule = new Schedule(traverserName, intervals);
    schedules.add(schedule);
    
    return schedules;
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
   * Test that tests to mock Traverser objects.
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
