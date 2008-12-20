// Copyright (C) 2006-2008 Google Inc.
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

/**
 * Interface describing the persistence needs of a Traverser.
 */
public interface ConnectorStateStore {
  /**
   * Gets the stored state of a named connector.
   *
   * @param context a StoreContext
   * @return the state, or null if no state has been stored for this connector
   */
  public String getConnectorState(StoreContext context);

  /**
   * Sets the stored state of a named connector.
   *
   * @param context a StoreContext
   * @param connectorState String to store
   */
  public void storeConnectorState(StoreContext context, String connectorState);

  /**
   * Remove connector state.  If no such connector exists, do nothing.
   *
   * @param context a StoreContext
   */
  public void removeConnectorState(StoreContext context);
}
