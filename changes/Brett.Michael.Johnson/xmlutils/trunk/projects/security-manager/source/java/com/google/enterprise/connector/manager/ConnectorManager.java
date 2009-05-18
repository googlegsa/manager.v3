// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.connector.manager;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.scheduler.TraversalScheduler;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is temporary -- while the connector manager and the security manager are
 * different builds.  This functionality will eventually be merged with the connector
 * manager's {@link ProductionManager}.
 */
public class ConnectorManager extends ProductionManager {

  private static final Logger LOGGER = Logger.getLogger(ConnectorManager.class.getName());

  public ConnectorManager() {
    super();
    Logger.getLogger(TraversalScheduler.class.getName()).setLevel(Level.WARNING);
  }

  @Override
  public List<ConnectorStatus> getConnectorStatuses() {
    try {
      checkAndSetConnectorConfig("BasicAuth", "BasicAuthConnector",
                                 ImmutableMap.of("ServerUrl", "foo"),  // dummy parameter
                                 "en", false);
      checkAndSetConnectorConfig("FormAuth", "FormAuthConnector",
                                 ImmutableMap.of("CookieName", "bar"),  // dummy parameter
                                 "en", false);
      checkAndSetConnectorConfig("ConnAuth", "ConnAuthConnector",
                                 ImmutableMap.of("SpiVersion", "0"),
                                 "en", false);
    } catch (InstantiatorException e) {
      LOGGER.info("Instantiator: " + e.toString());
    } catch (PersistentStoreException e) {
      LOGGER.info("PersistentStore: " + e.toString());
    }
    List<ConnectorStatus> result = super.getConnectorStatuses();
    return result;
  }

  private void checkAndSetConnectorConfig(String connectorName,
                                          String connectorTypeName,
                                          Map<String, String> configData,
                                          String language,
                                          boolean update)
      throws PersistentStoreException, InstantiatorException {
    try {
      this.getConnectorConfig(connectorName);
    } catch (ConnectorNotFoundException e) {
      this.setConnectorConfig(connectorName, connectorTypeName, configData, language, update);
    }
  }

}
