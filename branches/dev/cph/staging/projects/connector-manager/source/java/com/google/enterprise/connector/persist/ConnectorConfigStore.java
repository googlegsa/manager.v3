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

import java.util.Properties;

/**
 * Interface describing the persistence needs of a Connector's Configuration.
 */
public interface ConnectorConfigStore {
  /**
   * Gets the stored configuration of a named connector.
   * 
   * @param context a StoreContext
   * @return the configuration Properties, or null if no configuration has 
   *         been stored for this connector.
   */
  public Properties getConnectorConfiguration(StoreContext context);

  /**
   * Sets the stored configuration of a named connector.
   * 
   * @param context a StoreContext
   * @param configuration Properties to store
   */
  public void storeConnectorConfiguration(StoreContext context,
      Properties configuration);
  
  /**
   * Remove a stored connector configuration.  If no such connector exists,
   * do nothing.
   *
   * @param context a StoreContext
   */
  public void removeConnectorConfiguration(StoreContext context);
}
