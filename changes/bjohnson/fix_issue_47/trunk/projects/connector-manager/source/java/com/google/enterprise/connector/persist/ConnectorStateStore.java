// Copyright (C) 2006 Google Inc.
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
 * Interface describing the persistence needs of a Traverser
 */
public interface ConnectorStateStore {

  /**
   * Gets the stored state of a named connector.
   * 
   * @param connectorName
   * @return the state, or null if no state has been stored for this connector
   * @throws IllegalStateException if state store is disabled for this connector
   */
  public String getConnectorState(String connectorName);

  /**
   * Sets the stored state of a named connector.
   * 
   * @param connectorName
   * @param connectorState String to store
   * @throws IllegalStateException if state store is disabled for this connector
   */
  public void storeConnectorState(String connectorName, String connectorState);

  /**
   * Remove connector state.  If no such connector exists, do nothing.
   * @param connectorName name of the connector.
   */
  public void removeConnectorState(String connectorName);
  
  /**
   * Enables the ConnectorStateStore for this connector.
   * This allows the connector state for this connector to be
   * get and stored.  By default, the connector state store
   * for a connector is enabled.  It may be disabled when the
   * connector is deleted.
   * @param connectorName connector name.
   */
  public void enableConnectorState(String connectorName);

  /**
   * Disables the ConnectorStateStore for this connector.
   * Attempts to read from or write to a disabled store 
   * throws an IllegalStateException.
   * @param connectorName connector name.
   */
  public void disableConnectorState(String connectorName);

}
