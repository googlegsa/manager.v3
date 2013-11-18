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

import java.util.HashSet;

public class HybridPersistentStore implements PersistentStore {
 private final PersistentStore configurationStore;
 private final PersistentStore scheduleStore;
 private final PersistentStore stateStore;

 public HybridPersistentStore(PersistentStore configurationStore,
      PersistentStore scheduleStore, PersistentStore stateStore) {
   this.configurationStore = configurationStore;
   this.scheduleStore = scheduleStore;
   this.stateStore = stateStore;
 }

  @Override
  public boolean isDisabled() {
    return (configurationStore.isDisabled() &&
            scheduleStore.isDisabled() &&
            stateStore.isDisabled());
  }


  @Override
  public ImmutableMap<StoreContext, ConnectorStamps> getInventory() {
    ImmutableMap<StoreContext, ConnectorStamps> scheduleInventory =
        scheduleStore.getInventory();
    ImmutableMap<StoreContext, ConnectorStamps> stateInventory =
        stateStore.getInventory();
    ImmutableMap<StoreContext, ConnectorStamps> configurationInventory =
        configurationStore.getInventory();

    // TODO: This won't work for JdbcStore, as its StoreContext is
    // missing connectorDir.  This will correct itself when StoreContext
    // is changed to use ConnectorTypeName, rather than connectorDir.
    HashSet<StoreContext> contexts = new HashSet<StoreContext>();
    contexts.addAll(scheduleInventory.keySet());
    contexts.addAll(stateInventory.keySet());
    contexts.addAll(configurationInventory.keySet());

    ImmutableMap.Builder<StoreContext, ConnectorStamps> builder =
        ImmutableMap.builder();

    for (StoreContext context : contexts) {
      Stamp configurationStamp = null;
      Stamp checkpointStamp = null;
      Stamp scheduleStamp = null;
      ConnectorStamps stamps = configurationInventory.get(context);
      if (stamps != null) {
        configurationStamp = stamps.getConfigurationStamp();
      }
      stamps = scheduleInventory.get(context);
      if (stamps != null) {
        scheduleStamp = stamps.getScheduleStamp();
      }
      stamps = stateInventory.get(context);
      if (stamps != null) {
        checkpointStamp = stamps.getCheckpointStamp();
      }
      builder.put(context, new ConnectorStamps(
          checkpointStamp, configurationStamp, scheduleStamp));
    }
    return builder.build();
  }

  @Override
  public String getConnectorState(StoreContext context) {
    return stateStore.getConnectorState(context);
  }

  @Override
  public void storeConnectorState(StoreContext context,
      String connectorState) {
    stateStore.storeConnectorState(context, connectorState);
  }

  @Override
  public void removeConnectorState(StoreContext context) {
    stateStore.removeConnectorState(context);
  }

  @Override
  public Configuration getConnectorConfiguration(StoreContext context) {
    return configurationStore.getConnectorConfiguration(context);
  }

  @Override
  public void storeConnectorConfiguration(StoreContext context,
      Configuration config) {
    configurationStore.storeConnectorConfiguration(context, config);
  }

  @Override
  public void removeConnectorConfiguration(StoreContext context) {
    configurationStore.removeConnectorConfiguration(context);
  }

  @Override
  public Schedule getConnectorSchedule(StoreContext context) {
    return scheduleStore.getConnectorSchedule(context);
  }

  @Override
  public void storeConnectorSchedule(StoreContext context, Schedule schedule) {
    scheduleStore.storeConnectorSchedule(context, schedule);
  }

  @Override
  public void removeConnectorSchedule(StoreContext context) {
    scheduleStore.removeConnectorSchedule(context);
  }
}
