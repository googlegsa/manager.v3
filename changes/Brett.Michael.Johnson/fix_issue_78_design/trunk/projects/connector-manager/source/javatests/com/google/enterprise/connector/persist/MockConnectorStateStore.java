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

import com.google.enterprise.connector.instantiator.TypeInfo;

import java.util.HashMap;

/**
 * MockConnectorStateStore - doesn't actually persist, just uses memory.
 */
public class MockConnectorStateStore extends HashMap 
    implements ConnectorStateStore {

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorStateStore
   * #getConnectorState(TypeInfo, java.lang.String)
   */
  public String getConnectorState(TypeInfo typeInfo, String connectorName) {
    return (String) this.get(connectorName);
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorStateStore
   * #storeConnectorState(TypeInfo, java.lang.String, java.lang.String)
   */
  public void storeConnectorState(TypeInfo typeInfo, String connectorName, 
      String connectorState) {
    this.put(connectorName, connectorState);
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorStateStore
   * #removeConnectorState(TypeInfo, java.lang.String)
   */
  public void removeConnectorState(TypeInfo typeInfo, String connectorName) {
    this.remove(connectorName);
  }
}
