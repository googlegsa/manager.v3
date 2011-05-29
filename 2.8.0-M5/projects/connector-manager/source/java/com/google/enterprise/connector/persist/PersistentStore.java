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
 * <p>
 * Each call to one of the set or remove methods on this interface
 * should update the version stamp of the corresponding object to a
 * new and unique value. Specifically, removing an object and then
 * re-adding it should not produce a version stamp for the new object
 * that might have been returned by the original object.
 */
// TODO: Change StoreContext to String (instance name).
public interface PersistentStore {
  /**
   * Determines if a {@code PersistentStore} is currently disabled for any
   * reason.
   *
   * @return {@code true} if this PersistentStore is disabled, {@code false}
   * otherwise.
   */
  boolean isDisabled();

  /**
   * Gets the version stamps of all persistent objects.
   *
   * @return an immutable map containing the version stamps; may be
   * empty but not {@code null}
   */
  ImmutableMap<StoreContext, ConnectorStamps> getInventory();

  String getConnectorState(StoreContext context);
  void storeConnectorState(StoreContext context, String checkpoint);
  void removeConnectorState(StoreContext context);

  Configuration getConnectorConfiguration(StoreContext context);
  void storeConnectorConfiguration(StoreContext context,
      Configuration configuration);
  void removeConnectorConfiguration(StoreContext context);

  Schedule getConnectorSchedule(StoreContext context);
  void storeConnectorSchedule(StoreContext context, Schedule schedule);
  void removeConnectorSchedule(StoreContext context);
}
