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
import java.util.Map;

/**
 * Interface describing the persistence needs of the Instantiator
 */
public interface ConnectorConfigStore {

  /**
   * Gets the names of all known connector types in this store. Note: at present
   * there is no programmatic way of adding connector types. They come from
   * external configuration only.
   * 
   * @return an iteration of String connector type names
   */
  public Iterator getConnectorTypeNames();

  /**
   * Gets the names of all known connectors in this store
   * 
   * @return an iteration of String connector names
   */
  public Iterator getConnectorNames();

  /**
   * Gets the configuration for a named connector
   * 
   * @param connectorName
   * @return a Map (<String>, <String>) of connector configuration data.
   * @throws ConnectorNotFoundException
   */
  public Map getConnectorConfig(String connectorName)
      throws ConnectorNotFoundException;

}
