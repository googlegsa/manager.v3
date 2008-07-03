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

import java.util.HashMap;

/**
 * MockConnectorStateStore - doesn't actually persist, just uses memory
 */
public class MockConnectorStateStore extends HashMap implements
    ConnectorStateStore {

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorStateStore
   * #getConnectorState(java.lang.String)
   */
  public String getConnectorState(String connectorName) {
    if (this.containsKey(connectorName + ".isDisabled")) {
      throw new IllegalStateException(
          "Reading from disabled ConnectorStateStore for connector "
          + connectorName);
    }
    return (String) this.get(connectorName);
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorStateStore
   * #storeConnectorState(java.lang.String, java.lang.String)
   */
  public void storeConnectorState(String connectorName, String connectorState) {
    if (this.containsKey(connectorName + ".isDisabled")) {
      throw new IllegalStateException(
          "Writing to disabled ConnectorStateStore for connector "
          + connectorName);
    }
    this.put(connectorName, connectorState);
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorStateStore
   * #removeConnectorState(java.lang.String)
   */
  public void removeConnectorState(String connectorName) {
    this.remove(connectorName);
  }

}
