// Copyright (C) 2010 Google Inc.
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

/**
 * Stores and retrieves the persistent objects of a connector instance.
 */
// TODO: Change StoreContext to String (instance name).
public interface PersistentStore {
  /**
   * Gets the version stamps of all persistent objects.
   *
   * @return an immutable map containing the version stamps; may be
   * empty but not {@code null}
   */
  ImmutableMap<StoreContext, ConnectorStamps> getInventory();

  String getCheckpoint(StoreContext context);
  void setCheckpoint(StoreContext context, String checkpoint);
  void removeCheckpoint(StoreContext context);

  Configuration getConfiguration(StoreContext context);
  void setConfiguration(StoreContext context, Configuration configuration);
  void removeConfiguration(StoreContext context);

  Schedule getSchedule(StoreContext context);
  void setSchedule(StoreContext context, Schedule schedule);
  void removeSchedule(StoreContext context);
}
