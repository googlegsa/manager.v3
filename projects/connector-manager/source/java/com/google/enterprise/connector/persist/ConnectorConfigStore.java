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

import java.util.Iterator;

/**
 * Config storage needs of the instantiator
 * 
 */
public interface ConnectorConfigStore {

  /**
   * Get the names of all known connectors
   * 
   * @return an Iterator of String names
   */
  public Iterator getConnectorNames();

  /**
   * Get the type for a known connector
   * 
   * @param connectorName the connector to look up
   * @return its type, as a String
   * @throws ConnectorNotFoundException if the named connector is not found
   */
  public String getConnectorType(String connectorName)
      throws ConnectorNotFoundException;

  /**
   * Get the resource string for a named connector. A resource string is loosely
   * defined for the moment - in practice, its a filename, for now. TODO:
   * specify whether this is relative or absolute or what.
   * 
   * @param connectorName the connector to look up
   * @return the resource String
   * @throws ConnectorNotFoundException if the named connector is not found
   * @throws PersistentStoreException if something unrecoverable happens
   */
  public String getConnectorResourceString(String connectorName)
      throws ConnectorNotFoundException, PersistentStoreException;

  /**
   * Drops the connector resource for the named connector.
   * 
   * @param connectorName
   */
  public void dropConnector(String connectorName);

  /**
   * Creates a new connector config for the named connector of the named type.
   * 
   * @param connectorName
   * @param connectorTypeName
   * @param newConfig
   * @throws PersistentStoreException 
   */
  public void setConnectorConfig(String connectorName,
      String connectorTypeName, String newConfig) throws PersistentStoreException;

}
