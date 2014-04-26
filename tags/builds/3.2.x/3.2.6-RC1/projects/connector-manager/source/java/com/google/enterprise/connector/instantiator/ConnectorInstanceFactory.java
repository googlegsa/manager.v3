// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.instantiator;

import com.google.common.base.Preconditions;
import com.google.enterprise.connector.database.ConnectorPersistentStoreFactory;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorPersistentStoreAware;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ConnectorFactory} implementation that creates transient
 * {@link Connector} instances on behalf of a {@link ConnectorType}
 * for the purposes of validating a supplied configuration.  The
 * {@code Connector} instances are temporary, have no on-disk
 * representation, and are not considered active instances.
 * <p>
 * Any {@code Connector} instances created by this factory will be destroyed
 * when {@code ConnectorType.validateConfig()} returns.
 * <p>
 * @see com.google.enterprise.connector.spi.ConnectorType#validateConfig(Map,
 *      Locale, ConnectorFactory)
 */
class ConnectorInstanceFactory implements ConnectorFactory {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorInstanceFactory.class.getName());

  final String connectorName;
  final TypeInfo typeInfo;
  final Configuration originalConfig;
  final ConnectorPersistentStoreFactory connectorPersistentStoreFactory;
  final List<Connector> connectors;

  /**
   * Constructor takes the items needed by {@code InstanceInfo}, but not
   * provided via {@code makeConnector}.
   *
   * @param connectorName the name of this connector instance.
   * @param typeInfo the connector type.
   * @param config the configuration provided to {@code validateConfig}.
   * @param cpsFactory a {@link ConnectorPersistentStoreFactory}.
   */
  public ConnectorInstanceFactory(String connectorName, TypeInfo typeInfo,
      Configuration config, ConnectorPersistentStoreFactory cpsFactory) {
    Preconditions.checkNotNull(connectorName, "connectorName must not be null");
    Preconditions.checkNotNull(typeInfo, "typeInfo must not be null");
    Preconditions.checkNotNull(config, "configuration must not be null");
    Preconditions.checkArgument((typeInfo.getConnectorTypeName()
                                 .equals(config.getTypeName())),
                                "TypeInfo must match Configuration type");
    this.connectorName = connectorName;
    this.typeInfo = typeInfo;
    this.originalConfig = config;
    this.connectorPersistentStoreFactory = cpsFactory;
    this.connectors = new LinkedList<Connector>();
  }

  /**
   * Create an instance of this {@code Connector} based upon the supplied
   * configuration data. If the supplied configuration {@code Map} is
   * {@code null}, use the original configuration.
   *
   * @see com.google.enterprise.connector.spi.ConnectorFactory#makeConnector(Map)
   */
  @Override
  public Connector makeConnector(Map<String, String> config)
      throws RepositoryException {
    try {
      Configuration configuration = new Configuration(config, originalConfig);
      if (LOGGER.isLoggable(Level.CONFIG)) {
        LOGGER.config("ConnectorFactory makes connector with configuration: "
                      + configuration);
      }
      Connector connector = InstanceInfo.makeConnectorWithSpring(
          connectorName, typeInfo, configuration);
      LOGGER.config("Constructed connector " + connector);
      synchronized (this) {
        connectors.add(connector);
      }
      if ((connectorPersistentStoreFactory != null) &&
          (connector instanceof ConnectorPersistentStoreAware)) {
        connectorPersistentStoreFactory.newConnectorPersistentStore(
            connectorName, typeInfo.getConnectorTypeName(), null);
        // Don't actually call connector.setDatabaseAccess(), since we
        // don't want the transient connector to attempt to write to it.
        // But configuration/connection errors should throw SQLException.
      }
      return connector;
    } catch (InstantiatorException e) {
      throw new
          RepositoryException("ConnectorFactory failed to make connector.", e);
    } catch (SQLException e) {
      throw new RepositoryException("Failed to configure database access for "
          + "connector.", e);
    }
  }

  /**
   * Shutdown any connector instances created by the factory.
   */
  synchronized void shutdown() {
    for (Connector connector : connectors) {
      if (connector instanceof ConnectorShutdownAware) {
        try {
          LOGGER.config("Shutdown connector " + connector);
          ((ConnectorShutdownAware) connector).shutdown();
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Failed to shutdown connector "
              + connectorName + " created by validateConfig", e);
        }
      }
    }
    connectors.clear();
  }
}
