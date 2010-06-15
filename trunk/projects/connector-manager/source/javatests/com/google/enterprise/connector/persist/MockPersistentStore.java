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

package com.google.enterprise.connector.persist;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock persistent store. This implementation doesn't actually persist
 * any objects, it just uses memory.
 */
public class MockPersistentStore implements PersistentStore {
  private Map<StoreContext, String> checkpointMap =
      new HashMap<StoreContext, String>();
  private Map<StoreContext, Configuration> configurationMap =
      new HashMap<StoreContext, Configuration>();
  private Map<StoreContext, Schedule> scheduleMap =
      new HashMap<StoreContext, Schedule>();

  private final ConnectorStamps stamps = new ConnectorStamps(new MockStamp(2),
      new MockStamp(3), new MockStamp(5));

  private <T> void addAll(
      ImmutableMap.Builder<StoreContext, ConnectorStamps> builder,
      Map<StoreContext, T> map) {
    for (StoreContext context : map.keySet()) {
      builder.put(context, stamps);
    }
  }

  public ImmutableMap<StoreContext, ConnectorStamps> getInventory() {
    ImmutableMap.Builder<StoreContext, ConnectorStamps> builder =
        ImmutableMap.builder();
    addAll(builder, checkpointMap);
    addAll(builder, configurationMap);
    addAll(builder, scheduleMap);
    return builder.build();
  }

  /* @Override */
  public String getConnectorState(StoreContext context) {
    return checkpointMap.get(context);
  }

  /* @Override */
  public void storeConnectorState(StoreContext context,
      String connectorState) {
    checkpointMap.put(context, connectorState);
  }

  /* @Override */
  public void removeConnectorState(StoreContext context) {
    checkpointMap.remove(context);
  }

  /* @Override */
  public Configuration getConnectorConfiguration(StoreContext context) {
    return configurationMap.get(context);
  }

  /* @Override */
  public void storeConnectorConfiguration(StoreContext context,
      Configuration config) {
    configurationMap.put(context, config);
  }

  /* @Override */
  public void removeConnectorConfiguration(StoreContext context) {
    configurationMap.remove(context);
  }

  /* @Override */
  public Schedule getConnectorSchedule(StoreContext context) {
    return scheduleMap.get(context);
  }

  /* @Override */
  public void storeConnectorSchedule(StoreContext context, Schedule schedule) {
    scheduleMap.put(context, schedule);
  }

  /* @Override */
  public void removeConnectorSchedule(StoreContext context) {
    scheduleMap.remove(context);
  }
}
