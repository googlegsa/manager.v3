// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.importexport;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An encapsulation of all the information we export/import Connector
 * Manager instance.
 */
public class ExportConnectors {
  private static final Logger LOGGER =
      Logger.getLogger(ExportConnectors.class.getName());

  private final PersistentStore persistentStore;
  private final Manager manager;

  public ExportConnectors(PersistentStore persistentStore, Manager manager) {
    this.persistentStore = persistentStore;
    this.manager = manager;
  }

  /**
   * Returns a ImportExportConnectorList representing the current
   * set of connectors.
   *
   * @return a ImportExportConnectorList
   */
  public ImportExportConnectorList getConnectors() {
    return getConnectors(null);
  }

  /**
   * Returns a ImportExportConnectorList representing the specified
   * set of connectors.
   *
   * @param connectorNames Collection of names of connectors to include.
   *        If {@code null}, all connectors are included.
   * @return a ImportExportConnectorList
   */
  public ImportExportConnectorList getConnectors(Collection<String> connectorNames) {
    ImportExportConnectorList connectors = new ImportExportConnectorList();
    for (StoreContext storeContext : persistentStore.getInventory().keySet()) {
      if (connectorNames != null &&
          !connectorNames.contains(storeContext.getConnectorName())) {
        continue;
      }

      Configuration config =
          persistentStore.getConnectorConfiguration(storeContext);
      if (config != null) {
        // Strip the often transient google properties (such as google work dir).
        // Encrypt sensitive properties before including them in the output.
        // Note that the order of operations is important here.  We strip the
        // google* properties first, then encrypt passwords, which adds back a
        // googlePropertiesVersion - needed to decrypt the properties correctly.
        config = new Configuration(encryptSensitiveProperties(
            removeGoogleProperties(config.getMap())), config);
        // Try to determine the connector version.
        String typeVersion = null;
        if (manager != null) {
          try {
            typeVersion = JarUtils.getJarVersion(manager.getConnectorType(
                config.getTypeName()).getClass());
          } catch (ConnectorTypeNotFoundException ctnfe) {
            LOGGER.warning("Failed to locate ConnectorType for "
                           + config.getTypeName());
          }
        }

        // Add this connector to the list.
        connectors.add(new ImportExportConnector(
            storeContext.getConnectorName(), config, typeVersion,
            persistentStore.getConnectorSchedule(storeContext),
            persistentStore.getConnectorState(storeContext)));
      }
    }
    return connectors;
  }

  /**
   * Removes properties whose names start with "google" from the Configuration.
   *
   * @param configMap a Map of configuration properties.
   * @return configMap with "google*" properties filtered out.
   */
  private Map<String, String> removeGoogleProperties(
      Map<String, String> configMap) {
    // We don't bother making a copy, since encryptSensitiveProperties
    // makes a copy first thing.
    return Maps.filterKeys(configMap, new Predicate<String>() {
        public boolean apply(String input) {
          return !input.startsWith("google");
        }
      });
  }

  /**
   * Encrypts sensitive configuration properties in the supplied configMap.
   *
   * @param configMap a Map of configuration properties.
   * @return configMap with sensitive property values encrypted.
   */
  private Map<String, String> encryptSensitiveProperties(
      Map<String, String> configMap) {
    Properties props = PropertiesUtils.fromMap(configMap);
    PropertiesUtils.encryptSensitiveProperties(props);
    PropertiesUtils.stampPropertiesVersion(props);
    return PropertiesUtils.toMap(props);
  }
}

