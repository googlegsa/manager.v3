// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.persist.MockPersistentStore;
import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.scheduler.Schedule;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tests for {@link ChangeDetectorImpl}. */
// TODO: Change StoreContext to String (instance name).
// TODO: More tests, especially including stamps and corner cases like
// deleted and then re-added, etc.
public class ChangeDetectorTest extends TestCase {
  private PersistentStore store;
  private ExceptionalChangeListener listener;
  private ChangeDetector detector;

  private List<String> expectedChanges;

  public ChangeDetectorTest() {
  }

  @Override
  public void setUp() {
    store = new MockPersistentStore();
    listener = new ExceptionalChangeListener();
    detector = new ChangeDetectorImpl(store, listener);

    expectedChanges = new ArrayList<String>();
  }

  /** Adds an instance to the store and records the action. */
  private void addConnector(String connectorName) {
    store.storeConnectorConfiguration(
        new StoreContext(connectorName, "testType"),
        new Configuration("testType", Collections.<String, String>emptyMap(),
                          null));
    expectedChanges.add(MockChangeListener.CONNECTOR_ADDED + connectorName);
  }

  /** Updates an instance to the store and records the action. */
  private void updateConnector(String connectorName) {
    StoreContext context = new StoreContext(connectorName, "testType");
    assertNotNull(store.getConnectorConfiguration(context));
    store.storeConnectorConfiguration(context, new Configuration(
        "testType", Collections.<String, String>emptyMap(), null));
    expectedChanges.add(MockChangeListener.CONFIGURATION_CHANGED
                        + connectorName);
  }

  /** Deletes an instance from the store and records the action. */
  private void removeConnector(String connectorName) {
    store.removeConnectorConfiguration(
        new StoreContext(connectorName, "testType"));
    expectedChanges.add(MockChangeListener.CONNECTOR_REMOVED + connectorName);
  }

  /** Sets the checkpoint for a connector instance and records the action. */
  private void setCheckpoint(String connectorName) {
    store.storeConnectorState(new StoreContext(connectorName, "testType"), "");
    expectedChanges.add(MockChangeListener.CHECKPOINT_CHANGED + connectorName);
  }

  /** Sets the Schedule for a connector instance and records the action. */
  private void setSchedule(String connectorName) {
    store.storeConnectorSchedule(new StoreContext(connectorName, "testType"),
                                 new Schedule());
    expectedChanges.add(MockChangeListener.SCHEDULE_CHANGED + connectorName);
  }

  /**
   * Compares the contents of the two lists as multisets, that is,
   * considering only the members and not the ordering.  Neither of
   * the lists should be empty.
   *
   * @param expected the expected value
   * @param actual the actual value
   */
  private void assertEqualsMultiSet(List<String> expected,
      List<String> actual) {
    assertFalse(expected.isEmpty());
    assertFalse(actual.isEmpty());
    // Copy, sort, and compare.
    List<String> left = new ArrayList<String>(expected);
    List<String> right = new ArrayList<String>(actual);
    Collections.sort(left);
    Collections.sort(right);
    assertEquals(left, right);
  }

  /**
   * Assertst that expected and actual lists each have no recorded changes.
   *
   * @param expected the expected value
   * @param actual the actual value
   */
  private void assertNoChanges(List<String> expected, List<String> actual) {
    assertTrue(expected.isEmpty());
    assertTrue(actual.isEmpty());
  }

  /** Basic test of adding and deleting connector instances. */
  public void testAddAndRemove() {
    addConnector("c1");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    addConnector("b2");
    addConnector("b1");
    addConnector("b3");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    expectedChanges.clear();
    listener.clear();

    addConnector("c2");
    addConnector("a1");
    removeConnector("b2");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());
  }

  /** Basic test of adding, updating, and deleting connector instances. */
  public void testAddUpdateRemove() {
    addConnector("c1");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    updateConnector("c1");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    removeConnector("c1");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());
  }

  /** Test of updating schedules and checkpoints. */
  public void testCheckpointsAndSchedules() {
    addConnector("c1");
    addConnector("c2");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    setSchedule("c1");
    setSchedule("c2");
    setCheckpoint("c2");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    expectedChanges.clear();
    listener.clear();

    setSchedule("c1");
    setCheckpoint("c2");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());
  }

  /** Test detecting no changes. */
  public void testNoChanges() {
    addConnector("c1");
    addConnector("c2");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // With no additional changes, change detector should find nothing.
    expectedChanges.clear();
    listener.clear();
    detector.detect();
    assertNoChanges(expectedChanges, listener.getChanges());

    // With no additional changes, change detector should still find nothing.
    detector.detect();
    assertNoChanges(expectedChanges, listener.getChanges());

    // Finally change something.
    updateConnector("c1");
    addConnector("b3");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());
  }

  /** Test retry connector instantiation if it fails. */
  public void testRetryInstantiationOnAdd() {
    addConnector("c1");
    addConnector("c2");
    detector.detect();
    assertFalse(listener.getChanges().isEmpty());
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // There should be no pending changes.
    expectedChanges.clear();
    listener.clear();
    detector.detect();
    assertNoChanges(expectedChanges, listener.getChanges());

    // Force a new connector instantiation to fail.
    listener.beBad = true;
    addConnector("c3");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // The ChangeDetector should retry this one.
    listener.clear();
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // The ChangeDetector should retry this one, but this time let it succeed.
    listener.beBad = false;
    listener.clear();
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // There should be no pending changes.
    expectedChanges.clear();
    listener.clear();
    detector.detect();
    assertNoChanges(expectedChanges, listener.getChanges());
  }

  /** Test retry connector instantiation if update config fails. */
  public void testRetryInstantiationOnUpdate() {
    addConnector("c1");
    addConnector("c2");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // There should be no pending changes.
    expectedChanges.clear();
    listener.clear();
    detector.detect();
    assertNoChanges(expectedChanges, listener.getChanges());

    // Force a updated connector instantiation to fail.
    listener.beBad = true;
    updateConnector("c1");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // The ChangeDetector should retry this one.
    listener.clear();
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // The ChangeDetector should retry this one, but this time let it succeed.
    listener.beBad = false;
    listener.clear();
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());

    // There should be no pending changes.
    expectedChanges.clear();
    listener.clear();
    detector.detect();
    assertNoChanges(expectedChanges, listener.getChanges());
  }

  /**
   * A ChangeListener that optionally throws InstantiatorException
   * for configuration changes.  Used to test instantiation retry.
   */
  private class ExceptionalChangeListener extends MockChangeListener {
    boolean beBad = false;

    @Override
    public void connectorAdded(String connectorName,
        Configuration configuration) throws InstantiatorException {
      super.connectorAdded(connectorName, configuration);
      if (beBad) {
        throw new InstantiatorException(connectorName);
      }
    }

    @Override
    public void connectorConfigurationChanged(String connectorName,
        Configuration configuration) throws InstantiatorException {
      super.connectorConfigurationChanged(connectorName, configuration);
      if (beBad) {
        throw new InstantiatorException(connectorName);
      }
    }
  }
}
