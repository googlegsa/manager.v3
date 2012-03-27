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
  private MockChangeListener listener;
  private ChangeDetector detector;

  private List<String> expectedChanges;

  public ChangeDetectorTest() {
  }

  @Override
  public void setUp() {
    store = new MockPersistentStore();
    listener = new MockChangeListener();
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

  /** Deletes an instance from the store and records the action. */
  private void removeConnector(String connectorName) {
    store.removeConnectorConfiguration(
        new StoreContext(connectorName, "testType"));
    expectedChanges.add(MockChangeListener.CONNECTOR_REMOVED + connectorName);
  }

  /**
   * Compares the contents of the two lists as multisets, that is,
   * considering only the members and not the ordering.
   *
   * @param expected the expected value
   * @param actual the actual value
   */
  private void assertEqualsMultiSet(List<String> expected,
      List<String> actual) {
    // Copy, sort, and compare.
    List<String> left = new ArrayList<String>(expected);
    List<String> right = new ArrayList<String>(actual);
    Collections.sort(left);
    Collections.sort(right);
    assertEquals(left, right);
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

    addConnector("c2");
    addConnector("a1");
    removeConnector("b2");
    detector.detect();
    assertEqualsMultiSet(expectedChanges, listener.getChanges());
  }
}
