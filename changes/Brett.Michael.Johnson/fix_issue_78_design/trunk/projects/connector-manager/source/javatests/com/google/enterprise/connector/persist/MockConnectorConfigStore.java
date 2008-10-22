// Copyright 2008 Google Inc.  All Rights Reserved.
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
import java.util.Properties;

/**
 * Mock Configuration Store - doesn't actually persist, just uses memory.
 */
public class MockConnectorConfigStore extends HashMap 
    implements ConnectorConfigStore {

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore
   * #getConnectorConfig(StoreContext)
   */
  public Properties getConnectorConfiguration(StoreContext context) {
    return (Properties) this.get(context.getConnectorName());
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore
   * #storeConnectorConfig(StoreContext, java.util.Properties)
   */
  public void storeConnectorConfiguration(StoreContext context,
      Properties connectorConfig) {
    this.put(context.getConnectorName(), connectorConfig);
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore
   * #removeConnectorConfig(StoreContext)
   */
  public void removeConnectorConfiguration(StoreContext context) {
    this.remove(context.getConnectorName());
  }
}
