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

import org.w3c.dom.Document;

import java.util.Iterator;

/**
 * Interface describing the persistence needs of the Instantiator
 */
public interface ConnectorConfigStore {

  /**
   * Gets the names of all known connectors in this store
   * 
   * @return an iterations of String connector names
   */
  public Iterator getConnectorNames();

  /**
   * Gets the configuration for a named connector
   * 
   * @param connectorName
   * @return a DOM-tree (org.w3c.document) for a named connector. Note: a null
   *         return means that this is an unknown connectorName; an empty
   *         document means that this is a known connector name, but it has no
   *         configuration at this time.
   */
  public Document getConnectorConfig(String connectorName);

}
