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

package com.google.enterprise.connector.persist;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StoreMigratorTest extends TestCase {
  private PersistentStore emptyStore;

  @Override
  protected void setUp() {
    emptyStore = new MockPersistentStore();
  }

  public void testZero() {
    List<PersistentStore> emptyList = Collections.<PersistentStore>emptyList();

    StoreMigrator migrator = new StoreMigrator(emptyStore, emptyList);
    migrator.migrate();
    assertTrue(emptyStore.toString(), emptyStore.getInventory().isEmpty());
  }

  public void testEmpty() {
    List<PersistentStore> emptyStores = Arrays.asList(new PersistentStore[] {
          new MockPersistentStore(), new MockPersistentStore() });

    StoreMigrator migrator = new StoreMigrator(emptyStore, emptyStores);
    migrator.migrate();
    assertTrue(emptyStore.toString(), emptyStore.getInventory().isEmpty());
  }

  public void testSingle() {
    ArrayList<PersistentStore> singleStore;
    singleStore = new ArrayList<PersistentStore>();
    PersistentStore store = new MockPersistentStore();
    StoreContext context = new StoreContext("name", "type");
    storeObjects(store, context, true, true, true);
    singleStore.add(store);

    StoreMigrator migrator = new StoreMigrator(emptyStore, singleStore);
    migrator.migrate();
    assertFalse(emptyStore.toString(), emptyStore.getInventory().isEmpty());
    checkEquals(singleStore.get(0), emptyStore);
  }

  public void testMultiple() {
    ArrayList<PersistentStore> multipleStores;
    multipleStores = new ArrayList<PersistentStore>();
    final boolean[] isObjects = { false, true };
    for (int i = 0; i < 3; i++) {
      StoreContext context;
      PersistentStore store;
      store = new MockPersistentStore();
      for (int icp = 0; icp < isObjects.length; icp++) {
        for (int ic = 0; ic < isObjects.length; ic++) {
          for (int is = 0; is < isObjects.length; is++) {
            context = new StoreContext("name" + i + icp + ic + is, "type");
            storeObjects(store, context, isObjects[icp], isObjects[ic],
                isObjects[is]);
          }
        }
      }
      // One of the iterations is false, false, false and stores nothing.
      assertEquals(7, store.getInventory().size());
      multipleStores.add(store);
    }
    StoreMigrator migrator = new StoreMigrator(emptyStore, multipleStores);
    migrator.migrate();
    assertFalse(emptyStore.toString(), emptyStore.getInventory().isEmpty());

    int instances = 0;
    for (PersistentStore store : multipleStores) {
      instances += store.getInventory().size();
    }
    assertEquals(instances, emptyStore.getInventory().size());
  }

  /**
   * Adds objects to the given store entry as defined by the booleans,
   * where <code>true</code> means store an object, and
   * <code>false</code> means do nothing for that object type.
   */
  private void storeObjects(PersistentStore store, StoreContext context,
      boolean isCheckpoint, boolean isConfig, boolean isSched) {
    if (isCheckpoint) {
      store.storeConnectorState(context, new Date().toString());
    }
    if (isConfig) {
      store.storeConnectorConfiguration(context, new Configuration(
          "testType", Collections.<String, String>emptyMap(), null));
    }
    if (isSched) {
      store.storeConnectorSchedule(context,
          new Schedule("name:200:300000:0-0"));
    }
  }

  /**
   * Compares two persistent stores to ensure they have the same contents.
   *
   * @throws AssertionFailedError if the inventory or stored objects
   * are different
   */
  private void checkEquals(PersistentStore expectedStore,
      PersistentStore actualStore) {
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
        expectedStore.getInventory();
    assertEquals(inventory.keySet(), actualStore.getInventory().keySet());
    for (StoreContext context : inventory.keySet()) {
      Configuration econfig = expectedStore.getConnectorConfiguration(context);
      Configuration aconfig = actualStore.getConnectorConfiguration(context);
      if (econfig == null) {
          assertNull(aconfig);
      } else {
        // TODO: Implement equals and hashCode for Configuration?
        assertNotNull(aconfig);
        assertEquals(econfig.getTypeName(), aconfig.getTypeName());
        assertEquals(econfig.getMap(), aconfig.getMap());
        assertEquals(econfig.getXml(), aconfig.getXml());
      }

      Schedule esched = expectedStore.getConnectorSchedule(context);
      Schedule asched = actualStore.getConnectorSchedule(context);
      if (esched == null) {
          assertNull(asched);
      } else {
        // TODO: Implement equals and hashCode for Schedule?
        assertEquals(esched.toString(), asched.toString());
      }

      String echeckpoint = expectedStore.getConnectorState(context);
      String acheckpoint = actualStore.getConnectorState(context);
      if (echeckpoint == null) {
          assertNull(acheckpoint);
      } else {
        assertEquals(echeckpoint, acheckpoint);
      }

      // If there's an entry in the inventory, we should have at least
      // one object.
      assertTrue(Arrays.asList(
          new Object[] { econfig, esched, echeckpoint }).toString(),
          econfig != null || esched != null || echeckpoint != null);
    }
  }
}
