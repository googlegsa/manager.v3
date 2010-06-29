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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copies the objects from zero or more legacy persistent stores to
 * the production persistent store. Existing objects in the production
 * store are not overwritten.
 */
// TODO: We could optionally support overwriting existing objects in
// the target and copying objects from the legacy stores rather than
// moving them. For manual migration, we might want options to delete
// the existing production store and then migrate the legacy stores
// (so that nulls overwrite objects, for example), or to selectively
// overwrite objects, or even to prompt for each object copied.
public class StoreMigrator {
  private static final Logger LOGGER =
      Logger.getLogger(StoreMigrator.class.getName());

  private final PersistentStore store;
  private final List<PersistentStore> legacyStores;

  public StoreMigrator(PersistentStore store,
      List<PersistentStore> legacyStores) {
    this.store = store;
    this.legacyStores = (legacyStores == null)
        ? Collections.<PersistentStore>emptyList() : legacyStores;
  }

  // TODO: StoreContext instances are not portable, because they
  // contain a connectorDir, which the JdbcStore does not have. That
  // means that we cannot migrate from JdbcStore to FileStore, which
  // requires the connectorDir. For generality, we need to store
  // something in all stores that is enough for FileStore, either the
  // connectorDir directly or more abstractly the type name.
  public void migrate() {
    for (PersistentStore legacyStore : legacyStores) {
      ImmutableMap<StoreContext, ConnectorStamps> inventory =
          legacyStore.getInventory();
      for (StoreContext context : inventory.keySet()) {
        // This double assignment ensures that we check the same
        // object type that we're storing.
        Configuration config = store.getConnectorConfiguration(context);
        if (config == null) {
          config = legacyStore.getConnectorConfiguration(context);
          if (config != null) {
            logMigration(legacyStore, context, "configuration");
            store.storeConnectorConfiguration(context, config);
          }
        }

        Schedule sched = store.getConnectorSchedule(context);
        if (sched == null) {
          sched = legacyStore.getConnectorSchedule(context);
          if (sched != null) {
            logMigration(legacyStore, context, "traversal schedule");
            store.storeConnectorSchedule(context, sched);
          }
        }

        String state = store.getConnectorState(context);
        if (state == null) {
          state = legacyStore.getConnectorState(context);
          if (state != null) {
            logMigration(legacyStore, context, "traversal state");
            store.storeConnectorState(context, state);
          }
        }
      }
    }

    // Check for missing objects in the production store.
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
        store.getInventory();
    for (StoreContext context : inventory.keySet()) {
      if (store.getConnectorConfiguration(context) == null) {
        logMissing(context, "configuration");
      }
      if (store.getConnectorSchedule(context) == null) {
        logMissing(context, "traversal schedule");
      }
      if (store.getConnectorState(context) == null) {
        logMissing(context, "traversal state");
      }
    }
  }

  private void logMigration(PersistentStore legacyStore,
      StoreContext context, String objectType) {
    if (LOGGER.isLoggable(Level.CONFIG)) {
      LOGGER.config("Migrating " + objectType + " information for connector "
          + context.getConnectorName() + " from legacy storage "
          + legacyStore.getClass().getName() + " to "
          + store.getClass().getName());
    }
  }

  private void logMissing(StoreContext context,
      String objectType) {
    if (LOGGER.isLoggable(Level.CONFIG)) {
      LOGGER.config("Connector " + context.getConnectorName()
          + " lacks saved " + objectType + ".");
    }
  }
}
