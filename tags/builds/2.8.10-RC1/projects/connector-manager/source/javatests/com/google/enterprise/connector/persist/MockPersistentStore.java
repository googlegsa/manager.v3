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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mock persistent store. This implementation doesn't actually persist
 * any objects, it just uses memory.
 */
public class MockPersistentStore implements PersistentStore {
  /* Property Names */
  static final String CONFIGURATION = "configuration";
  static final String SCHEDULE = "schedule";
  static final String CHECKPOINT = "checkpoint";

  private static class StoreKey {
    public final StoreContext context;
    public final String property;

    public StoreKey(StoreContext context, String property) {
      testStoreContext(context);
      this.context = context;
      this.property = property;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || !(other instanceof StoreKey))
        return false;
      StoreKey otherKey = (StoreKey) other;
      return context.equals(otherKey.context) &&
          property.equals(otherKey.property);
    }

    @Override
    public int hashCode() {
      // See Effective Java by Joshua Bloch, Item 8.
      int result = 131;
      result = 17 * result + context.hashCode();
      result = 17 * result + property.hashCode();
      return result;
    }
  }

  private static class StoreEntry {
    public final Object object;
    public final Stamp stamp;

    public StoreEntry(Object object, Stamp stamp) {
      this.object = object;
      this.stamp = stamp;
    }
  }

  /** Incremented stamp value for constructing updated stamps. */
  private static int stampValue = 0;

  private final Map<StoreKey, StoreEntry> storeMap =
      new HashMap<StoreKey, StoreEntry>();

  // Sort Inventory for predictable test results.
  private final boolean sortInventory;

  public MockPersistentStore() {
    this(false);
  }

  public MockPersistentStore(boolean sortInventory) {
    this.sortInventory = sortInventory;
  }

  public void clear() {
    storeMap.clear();
    stampValue = 0;
  }

  /* @GuardedBy(getInventory) */
  private Stamp getStamp(StoreContext context, String property) {
    StoreEntry entry = storeMap.get(new StoreKey(context, property));
    return (entry == null) ? null : entry.stamp;
  }

  private synchronized Object getObject(StoreContext context, String property) {
    StoreEntry entry = storeMap.get(new StoreKey(context, property));
    return (entry == null) ? null : entry.object;
  }

  private synchronized Object storeObject(StoreContext context, String property,
      Object object) {
    return storeMap.put(new StoreKey(context, property),
        new StoreEntry(object, new MockStamp(stampValue++)));
  }

  private synchronized void removeObject(StoreContext context, String property) {
    storeMap.remove(new StoreKey(context, property));
  }

  /* @Override */
  public boolean isDisabled() {
    return false;
  }

  /* @Override */
  public ImmutableMap<StoreContext, ConnectorStamps> getInventory() {
    ImmutableMap.Builder<StoreContext, ConnectorStamps> builder;
    if (sortInventory) {
      builder = ImmutableSortedMap.naturalOrder();
    } else {
      builder = ImmutableMap.builder();
    }
    Set<StoreContext> instances = new HashSet<StoreContext>();
    synchronized(this) {
      for (StoreKey key : storeMap.keySet()) {
        instances.add(key.context);
      }
      for (StoreContext context : instances) {
        builder.put(context, new ConnectorStamps(getStamp(context, CHECKPOINT),
            getStamp(context, CONFIGURATION), getStamp(context, SCHEDULE)));
      }
    }
    return builder.build();
  }

  /* @Override */
  public String getConnectorState(StoreContext context) {
    return (String) getObject(context, CHECKPOINT);
  }

  /* @Override */
  public void storeConnectorState(StoreContext context,
      String connectorState) {
    storeObject(context, CHECKPOINT, connectorState);
  }

  /* @Override */
  public void removeConnectorState(StoreContext context) {
    removeObject(context, CHECKPOINT);
  }

  /* @Override */
  public Configuration getConnectorConfiguration(StoreContext context) {
    return (Configuration) getObject(context, CONFIGURATION);
  }

  /* @Override */
  public void storeConnectorConfiguration(StoreContext context,
      Configuration config) {
    storeObject(context, CONFIGURATION, config);
  }

  /* @Override */
  public void removeConnectorConfiguration(StoreContext context) {
    removeObject(context, CONFIGURATION);
  }

  /* @Override */
  public Schedule getConnectorSchedule(StoreContext context) {
    return (Schedule) getObject(context, SCHEDULE);
  }

  /* @Override */
  public void storeConnectorSchedule(StoreContext context, Schedule schedule) {
    storeObject(context, SCHEDULE, schedule);
  }

  /* @Override */
  public void removeConnectorSchedule(StoreContext context) {
    removeObject(context, SCHEDULE);
  }

  /**
   * Test the StoreContext to make sure it is sane.
   *
   * @param context a StoreContext
   */
  private static void testStoreContext(StoreContext context) {
    Preconditions.checkNotNull(context, "StoreContext may not be null.");
  }
}
