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

import java.util.Collection;
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

  /**
   * Migrates data from all legacy stores to the configured PersistentStore.
   */
  public void migrate() {
    for (PersistentStore legacyStore : legacyStores) {
      migrate(legacyStore, store, null, false);
    }
    checkMissing(store, null);
  }

  /**
   * Migrates data from the {@code sourceStore} to the {@code destStore}.
   *
   * @param sourceStore source {@link PersistentStore}
   * @param destStore destination {@link PersistentStore}
   * @param connectorNames Collection of connector names to migrate.
   * @param force if {@code true} overwrite existing data in the
   *        {@code destStore}.
   */
  public static void migrate(PersistentStore sourceStore,
                             PersistentStore destStore,
                             Collection<String> connectorNames,
                             boolean force) {
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
          sourceStore.getInventory();
    for (StoreContext context : inventory.keySet()) {
      if (connectorNames != null &&
          !connectorNames.contains(context.getConnectorName())) {
        continue;
      }
      // This double assignment ensures that we check the same
      // object type that we're storing.
      Configuration config = destStore.getConnectorConfiguration(context);
      if (force || config == null) {
        config = sourceStore.getConnectorConfiguration(context);
        if (config != null) {
          logMigration(sourceStore, destStore, context, "configuration");
          destStore.storeConnectorConfiguration(context, config);
        }
      }
      Schedule sched = destStore.getConnectorSchedule(context);
      if (force || sched == null) {
        sched = sourceStore.getConnectorSchedule(context);
        if (sched != null) {
          logMigration(sourceStore, destStore, context, "traversal schedule");
          destStore.storeConnectorSchedule(context, sched);
        }
      }
      String state = destStore.getConnectorState(context);
      if (force || state == null) {
        state = sourceStore.getConnectorState(context);
        if (state != null) {
          logMigration(sourceStore, destStore, context, "traversal state");
          destStore.storeConnectorState(context, state);
        }
      }
    }
  }

  /**
   * Checks for missing persistently stored data in the
   * {@code persistentStore}.  If items are missing from the store,
   * log a message.
   *
   * @param persistentStore PersistentStore to check.
   * @param connectorNames Collection of connector names to check.
   */
  public static void checkMissing(PersistentStore persistentStore,
        Collection<String> connectorNames) {
    // Check for missing objects in the specified store.
    ImmutableMap<StoreContext, ConnectorStamps> inventory =
        persistentStore.getInventory();
    for (StoreContext context : inventory.keySet()) {
      if (connectorNames != null &&
          !connectorNames.contains(context.getConnectorName())) {
        continue;
      }
      if (persistentStore.getConnectorConfiguration(context) == null) {
        logMissing(context, "configuration");
      }
      if (persistentStore.getConnectorSchedule(context) == null) {
        logMissing(context, "traversal schedule");
      }
      if (persistentStore.getConnectorState(context) == null) {
        logMissing(context, "traversal state");
      }
    }
  }

  private static void logMigration(
      PersistentStore sourceStore, PersistentStore destStore,
      StoreContext context, String objectType) {
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Migrating " + objectType + " information for connector "
          + context.getConnectorName() + " from "
          + sourceStore.getClass().getName() + " to "
          + destStore.getClass().getName());
    }
  }

  private static void logMissing(StoreContext context,
      String objectType) {
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Connector " + context.getConnectorName()
          + " lacks saved " + objectType + ".");
    }
  }
}
