// Copyright 2008 Google Inc.  All Rights Reserved.
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

import junit.framework.TestCase;

/**
 * Class to test ScheduleStore.
 */
public class GenerationalStateStoreTest extends TestCase {

  protected ConnectorStateStore backingStore;
  protected GenerationalStateStore store;

  protected void setUp() {
    backingStore = new MockConnectorStateStore();
    store = new GenerationalStateStore(backingStore);
  }

  protected void tearDown() {
  }

  // Tests getting and setting for a valid connector name and state.
  // Makes sure generation number is unchanged over simple reads & writes.
  public void testGetandSetConnectorState() {
    String expectedState = "state of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    long myGeneration = store.myGeneration(storeContext);
    store.storeConnectorState(storeContext, expectedState);
    String resultState = store.getConnectorState(storeContext);
    assertTrue(resultState.equals(expectedState));
    assertTrue(myGeneration == store.myGeneration(storeContext));
    assertTrue(myGeneration ==
        GenerationalStateStore.currentGeneration(storeContext));
  }

  // Tests getting state for an unknown connector.
  public void testGetUnknownConnectorState() {
    String state = store.getConnectorState(
        new StoreContext("some wierd connector name"));
    assertNull(state);
  }

  // Test bumping the generation.
  public void testNewGeneration() {
    String connectorName = "foobar";
    String connectorState = "foobar's state";
    StoreContext storeContext = new StoreContext(connectorName);
    assertTrue(store.myGeneration(storeContext) ==
        GenerationalStateStore.currentGeneration(storeContext));
    String state = store.getConnectorState(storeContext);
    assertNull(state);
    store.storeConnectorState(storeContext, connectorState);
    state = store.getConnectorState(storeContext);
    assertEquals(connectorState, state);
    assertTrue(store.myGeneration(storeContext) ==
        GenerationalStateStore.currentGeneration(storeContext));
    GenerationalStateStore.newGeneration(storeContext);
    assertTrue(store.myGeneration(storeContext) !=
        GenerationalStateStore.currentGeneration(storeContext));
  }

  // Removing state implicitly bumps the generation.
  public void testRemoveState() {
    String connectorName = "foo";
    String connectorState = "foo's state";
    StoreContext storeContext = new StoreContext(connectorName);
    assertTrue(store.myGeneration(storeContext) ==
        GenerationalStateStore.currentGeneration(storeContext));
    String state = store.getConnectorState(storeContext);
    assertNull(state);
    store.storeConnectorState(storeContext, connectorState);
    state = store.getConnectorState(storeContext);
    assertEquals(connectorState, state);
    assertTrue(store.myGeneration(storeContext) ==
        GenerationalStateStore.currentGeneration(storeContext));
    store.removeConnectorState(storeContext);
    assertTrue(store.myGeneration(storeContext) !=
        GenerationalStateStore.currentGeneration(storeContext));
  }

  // The connector state should not be able to be
  // read or written by older generation instances.
  // Regression tests for Issue 47 and Issue 25.
  public void testDisabledAccess() {
    String barName = "bar";
    String barState = "bar's state";
    String barNewState = "bar's new state";
    StoreContext barContext = new StoreContext(barName);
    long barGeneration = store.myGeneration(barContext);

    String bazName = "baz";
    String bazState = "baz's state";
    String bazNewState = "baz's new state";
    StoreContext bazContext = new StoreContext(bazName);
    long bazGeneration = store.myGeneration(bazContext);

    String state = store.getConnectorState(barContext);
    assertNull(state);
    store.storeConnectorState(barContext, barState);

    store.storeConnectorState(bazContext, bazState);

    state = store.getConnectorState(barContext);
    assertEquals(barState, state);

    state = store.getConnectorState(bazContext);
    assertEquals(bazState, state);

    assertTrue(store.myGeneration(barContext) ==
        GenerationalStateStore.currentGeneration(barContext));
    assertTrue(store.myGeneration(bazContext) ==
        GenerationalStateStore.currentGeneration(bazContext));

    // Bump the generation number for bar.
    GenerationalStateStore.newGeneration(barContext);

    assertTrue(store.myGeneration(barContext) == barGeneration);
    assertTrue(barGeneration !=
        GenerationalStateStore.currentGeneration(barContext));
    assertTrue(store.myGeneration(barContext) !=
        GenerationalStateStore.currentGeneration(barContext));
    assertTrue(store.myGeneration(bazContext) ==
        GenerationalStateStore.currentGeneration(bazContext));
    assertTrue(bazGeneration ==
        GenerationalStateStore.currentGeneration(bazContext));

    // Although the store is disabled for bar,
    // we should still be able to get and set baz's state.
    store.storeConnectorState(bazContext, bazNewState);
    state = store.getConnectorState(bazContext);
    assertEquals(bazNewState, state);

    // Attempting to read or write to a disabled
    // connector state store should throw an exception.
    try {
      // This should throw an IllegalStateException.
      state = store.getConnectorState(barContext);
      fail("getConnectorState() should have thrown IllegalStateException");
    } catch (IllegalStateException expected) {
      assertEquals(
          "Attempt to access disabled Connector State Store for connector: bar",
          expected.getMessage());
    }

    try {
      // This should throw an IllegalStateException.
      store.storeConnectorState(barContext, barNewState);
      fail("storeConnectorState() should have thrown IllegalStateException");
    } catch (IllegalStateException expected1) {
      assertEquals(
          "Attempt to access disabled Connector State Store for connector: bar",
          expected1.getMessage());
    }

    // A new generational store should re-enable access to this
    // connector's connector state store.
    GenerationalStateStore newStore = new GenerationalStateStore(backingStore);

    // Make sure the new store attempted while disabled didn't take.
    state = newStore.getConnectorState(barContext);
    assertEquals(barState, state);

    // Make sure we can store using the new generation.
    newStore.storeConnectorState(barContext, barNewState);
    state = newStore.getConnectorState(barContext);
    assertEquals(barNewState, state);

    // We should still be able to remove the connector state,
    // even when disabled.
    store.removeConnectorState(barContext);
    newStore = new GenerationalStateStore(backingStore);
    state = newStore.getConnectorState(barContext);
    assertNull(state);

    newStore.removeConnectorState(bazContext);
  }
}
