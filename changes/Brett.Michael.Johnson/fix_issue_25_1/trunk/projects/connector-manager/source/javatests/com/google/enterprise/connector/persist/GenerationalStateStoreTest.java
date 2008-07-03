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
public class GenerationalStateStoreTest extends TestCase {

  protected PrefsStore prefsStore;
  protected GenerationalStateStore store;

  protected void setUp() {
    // Only testing use of userRoot
    prefsStore = new PrefsStore(true);
    store = new GenerationalStateStore(prefsStore);
  }

  protected void tearDown() {
    prefsStore.clear();
  }

  // Tests getting and setting for a valid connector name and state.
  // Makes sure generation number is unchanged over simple reads & writes.
  public void testGetandSetConnectorState() {
    String expectedState = "state of connectorA";
    String connectorName = "connectorA";
    long myGeneration = store.myGeneration(connectorName);
    store.storeConnectorState(connectorName, expectedState);
    String resultState = store.getConnectorState(connectorName);
    Assert.assertTrue(resultState.equals(expectedState));
    Assert.assertTrue(myGeneration == store.myGeneration(connectorName));
    Assert.assertTrue(myGeneration ==
        GenerationalStateStore.currentGeneration(connectorName));
  }

  // Tests getting state for an unknown connector.
  public void testGetUnknownConnectorState() {
    String state = store.getConnectorState("some wierd connector name");
    Assert.assertNull(state);
  }

  // Test bumping the generation.
  public void testNewGeneration() {
    String connectorName = "foobar";
    String connectorState = "foobar's state";
    Assert.assertTrue(store.myGeneration(connectorName) ==
        GenerationalStateStore.currentGeneration(connectorName));
    String state = store.getConnectorState(connectorName);
    Assert.assertNull(state);
    store.storeConnectorState(connectorName, connectorState);
    state = store.getConnectorState(connectorName);
    Assert.assertEquals(connectorState, state);
    Assert.assertTrue(store.myGeneration(connectorName) ==
        GenerationalStateStore.currentGeneration(connectorName));
    GenerationalStateStore.newGeneration(connectorName);
    Assert.assertTrue(store.myGeneration(connectorName) !=
        GenerationalStateStore.currentGeneration(connectorName));
  }

  // Removing state implicitly bumps the generation.
  public void testRemoveState() {
    String connectorName = "foo";
    String connectorState = "foo's state";
    Assert.assertTrue(store.myGeneration(connectorName) ==
        GenerationalStateStore.currentGeneration(connectorName));
    String state = store.getConnectorState(connectorName);
    Assert.assertNull(state);
    store.storeConnectorState(connectorName, connectorState);
    state = store.getConnectorState(connectorName);
    Assert.assertEquals(connectorState, state);
    Assert.assertTrue(store.myGeneration(connectorName) ==
        GenerationalStateStore.currentGeneration(connectorName));
    store.removeConnectorState(connectorName);
    Assert.assertTrue(store.myGeneration(connectorName) !=
        GenerationalStateStore.currentGeneration(connectorName));
  }

  // The connector state should not be able to be
  // read or written by older generation instances.
  // Regression tests for Issue 47 and Issue 25.
  public void testDisabledAccess() {
    String barName = "bar";
    String barState = "bar's state";
    String barNewState = "bar's new state";
    long barGeneration = store.myGeneration(barName);

    String bazName = "baz";
    String bazState = "baz's state";
    String bazNewState = "baz's new state";
    long bazGeneration = store.myGeneration(bazName);

    String state = store.getConnectorState(barName);
    Assert.assertNull(state);
    store.storeConnectorState(barName, barState);

    store.storeConnectorState(bazName, bazState);

    state = store.getConnectorState(barName);
    Assert.assertEquals(barState, state);

    state = store.getConnectorState(bazName);
    Assert.assertEquals(bazState, state);

    Assert.assertTrue(store.myGeneration(barName) ==
        GenerationalStateStore.currentGeneration(barName));
    Assert.assertTrue(store.myGeneration(bazName) ==
        GenerationalStateStore.currentGeneration(bazName));

    // Bump the generation number for bar.
    GenerationalStateStore.newGeneration(barName);

    Assert.assertTrue(store.myGeneration(barName) == barGeneration);
    Assert.assertTrue(barGeneration !=
        GenerationalStateStore.currentGeneration(barName));
    Assert.assertTrue(store.myGeneration(barName) !=
        GenerationalStateStore.currentGeneration(barName));
    Assert.assertTrue(store.myGeneration(bazName) ==
        GenerationalStateStore.currentGeneration(bazName));
    Assert.assertTrue(bazGeneration ==
        GenerationalStateStore.currentGeneration(bazName));

    // Although the store is disabled for bar,
    // we should still be able to get and set baz's state.
    store.storeConnectorState(bazName, bazNewState);
    state = store.getConnectorState(bazName);
    Assert.assertEquals(bazNewState, state);

    // Attempting to read or write to a disabled
    // connector state store should throw an exception.
    try {
      // This should throw an IllegalStateException.
      state = store.getConnectorState(barName);
      fail("getConnectorState() should have thrown IllegalStateException");
    } catch (IllegalStateException expected) {
      Assert.assertEquals(
          "Attempt to access disabled Connector State Store for connector: bar",
          expected.getMessage());
    }

    try {
      // This should throw an IllegalStateException.
      store.storeConnectorState(barName, barNewState);
      fail("storeConnectorState() should have thrown IllegalStateException");
    } catch (IllegalStateException expected1) {
      Assert.assertEquals(
          "Attempt to access disabled Connector State Store for connector: bar",
          expected1.getMessage());
    }

    // A new generational store should re-enable access to this
    // connector's connector state store.
    GenerationalStateStore newStore = new GenerationalStateStore(prefsStore);

    // Make sure the new store attempted while disabled didn't take.
    state = newStore.getConnectorState(barName);
    Assert.assertEquals(barState, state);

    // Make sure we can store using the new generation.
    newStore.storeConnectorState(barName, barNewState);
    state = newStore.getConnectorState(barName);
    Assert.assertEquals(barNewState, state);

    // We should still be able to remove the connector state,
    // even when disabled.
    store.removeConnectorState(barName);
    newStore = new GenerationalStateStore(prefsStore);
    state = newStore.getConnectorState(barName);
    Assert.assertNull(state);

    newStore.removeConnectorState(bazName);
  }
}
