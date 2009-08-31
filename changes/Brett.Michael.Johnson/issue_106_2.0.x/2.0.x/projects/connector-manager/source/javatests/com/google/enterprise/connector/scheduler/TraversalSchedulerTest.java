// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.common.I18NUtil;
import com.google.enterprise.connector.common.WorkQueue;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.instantiator.SpringInstantiator;
import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.monitor.HashMapMonitor;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.test.JsonObjectAsMap;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the Scheduler.
 *
 */
public class TraversalSchedulerTest extends TestCase {

  private static final String TEST_DIR_NAME = "testdata/tempSchedulerTests";
  private static final String TEST_CONFIG_FILE = "classpath*:config/connectorType.xml";
  private File baseDirectory;

  @Override
  protected void setUp() throws Exception {
    // Make sure that the test directory does not exist
    baseDirectory = new File(TEST_DIR_NAME);
    assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
    // Then recreate it empty
    assertTrue(baseDirectory.mkdirs());
  }

  @Override
  protected void tearDown() throws Exception {
    assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
  }


  private TraversalScheduler runWithSchedules(List<Schedule> schedules,
      Instantiator instantiator, boolean shutdown) {
    storeSchedules(schedules, instantiator);
    WorkQueue workQueue = new WorkQueue(2, 5000);
    TraversalScheduler scheduler =
        new TraversalScheduler(instantiator, new HashMapMonitor(), workQueue,
            new HostLoadManager(instantiator));
    scheduler.init();
    Thread thread = new Thread(scheduler, "TraversalScheduler");
    thread.start();
    if (shutdown) {
      // sleep to give it a chance to schedule something
      try {
        Thread.sleep(200);
      } catch (InterruptedException ie) {
        ie.printStackTrace();
        fail(ie.toString());
      }
      scheduler.shutdown(false, 5000);
    }

    return scheduler;
  }

  private TraversalScheduler runWithSchedules(List<Schedule> schedules,
      Instantiator instantiator) {
    return runWithSchedules(schedules, instantiator, true);
  }

  /**
   * Create an object that can return all connector instances referenced in
   * MockInstantiator.
   */
  private void storeSchedules(List<Schedule> schedules, Instantiator instantiator) {
    for (Schedule schedule : schedules) {
      String connectorName = schedule.getConnectorName();
      String connectorSchedule = schedule.toString();
      try {
        instantiator.setConnectorSchedule(connectorName, connectorSchedule);
      } catch (ConnectorNotFoundException e) {
        fail("Connector " + connectorName + " Not Found: " + e.toString());
      }
    }
  }

  private Instantiator createMockInstantiator() {
    MockInstantiator instantiator = new MockInstantiator();
    instantiator.setupTestTraversers();
    return instantiator;
  }

  private Instantiator createRealInstantiator() {
    Instantiator instantiator = new SpringInstantiator(
        new MockPusher(), new TypeMap(TEST_CONFIG_FILE, TEST_DIR_NAME));

    // Instantiate a couple of connectors.
    addConnector(instantiator, "connectorA", "TestConnectorA",
                 "{Username:foo, Password:bar, Color:red, "
                 + "RepositoryFile:MockRepositoryEventLog3.txt}");
    addConnector(instantiator, "connectorB", "TestConnectorB",
                 "{Username:foo, Password:bar, Flavor:minty-fresh, "
                 + "RepositoryFile:MockRepositoryEventLog3.txt}");
    return instantiator;
  }

  private void addConnector(Instantiator instantiator,
      String name, String typeName, String configString) {
    try {
      instantiator.setConnectorConfig(name, typeName,
          new JsonObjectAsMap(new JSONObject(configString)),
          I18NUtil.getLocaleFromStandardLocaleString("en"), false);
    } catch (ConnectorExistsException cee) {
      fail(cee.toString());
    } catch (ConnectorNotFoundException cnfe) {
      fail(cnfe.toString());
    } catch (ConnectorTypeNotFoundException ctnfe) {
      fail(ctnfe.toString());
    } catch (InstantiatorException ie) {
      fail(ie.toString());
    } catch (JSONException je) {
      fail(je.toString());
    }
  }

  /**
   * Retrieve a schedule that will always run the particular traverser.
   * @param traverserName name of the traverser
   * @param delay retry delay in milliseconds
   * @return a List of Schedule objects
   */
  private List<Schedule> getSchedules(String traverserName, int delay) {
    List<ScheduleTimeInterval> intervals =
        new ArrayList<ScheduleTimeInterval>();
    intervals.add(new ScheduleTimeInterval(
        new ScheduleTime(0),
        new ScheduleTime(0)));

    List<Schedule> schedules = new ArrayList<Schedule>();
    Schedule schedule = new Schedule(traverserName, false, 60, delay, intervals);
    schedules.add(schedule);
    return schedules;
  }

  private List<Schedule> getSchedules(String traverserName) {
    return getSchedules(traverserName, 0);
  }

  public void testRemoveConnector() {
    String connectorName = MockInstantiator.TRAVERSER_NAME_CANCELLABLE;
    List<Schedule> schedules = getSchedules(connectorName);
    TraversalScheduler scheduler =
      runWithSchedules(schedules, createMockInstantiator(), false);

    // sleep to give it a chance to schedule something
    try {
      Thread.sleep(100);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
      fail(ie.toString());
    }

    scheduler.removeConnector(connectorName);
  }

  public void testNoopTraverser() {
    runWithSchedules(getSchedules(MockInstantiator.TRAVERSER_NAME_NOOP),
                     createMockInstantiator());
  }

  /**
   * Test a long running traverser and show that it can properly get
   * interrupted.
   */
  public void testLongRunningTraverser() {
    runWithSchedules(getSchedules(MockInstantiator.TRAVERSER_NAME_LONG_RUNNING),
                     createMockInstantiator());
  }

  /**
   * Test a traverser that doesn't get interrupted.  We ignore the thread
   * eventually.
   */
  public void testNeverEndingTraverser() {
    runWithSchedules(getSchedules(MockInstantiator.TRAVERSER_NAME_NEVER_ENDING),
                     createMockInstantiator());
  }

  /**
   * Test a traverser that can get interrupted.
   */
  public void testInterruptibleTraverser() {
    runWithSchedules(getSchedules(MockInstantiator.TRAVERSER_NAME_INTERRUPTIBLE),
                     createMockInstantiator());
  }

  /**
   * Test that tests two mock Traverser objects.
   */
  public void testTwoTraversers() {
    List<Schedule> schedules = getSchedules(MockInstantiator.TRAVERSER_NAME1);
    schedules.addAll(getSchedules(MockInstantiator.TRAVERSER_NAME2));
    runWithSchedules(schedules, createMockInstantiator());
  }

  public void testRealInstantiator() {
    List<Schedule> schedules = getSchedules("connectorA");
    schedules.addAll(getSchedules("connectorB"));
    runWithSchedules(schedules, createRealInstantiator());
  }
}
