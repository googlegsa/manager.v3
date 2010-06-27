// Copyright (C) 2009 Google Inc.
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

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.File;
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
  final File connectorDir;
  final TypeInfo typeInfo;
  final Map<String, String> origConfig;
  final List<Connector> connectors;

  /**
   * Constructor takes the items needed by {@code InstanceInfo}, but not
   * provided via {@code makeConnector}.
   *
   * @param connectorName the name of this connector instance.
   * @param connectorDir the directory containing the connector prototype.
   * @param typeInfo the connector type.
   * @param config the configuration provided to {@code validateConfig}.
   */
  public ConnectorInstanceFactory(String connectorName, File connectorDir,
      TypeInfo typeInfo, Map<String, String> config) {
    this.connectorName = connectorName;
    this.connectorDir = connectorDir;
    this.typeInfo = typeInfo;
    this.origConfig = config;
    this.connectors = new LinkedList<Connector>();
  }

  /**
   * Create an instance of this {@code Connector} based upon the supplied
   * configuration data. If the supplied configuration {@code Map} is
   * {@code null}, use the original configuration.
   *
   * @see com.google.enterprise.connector.spi.ConnectorFactory#makeConnector(Map)
   */
  public Connector makeConnector(Map<String, String> config)
    throws RepositoryException {
    try {
      // WARNING: This is a transient, in-memory Connector InstanceInfo.
      // Do not attempt to persist anything.
      InstanceInfo info =
        InstanceInfo.fromNewConfig(connectorName, connectorDir, typeInfo,
                                   ((config == null) ? origConfig : config));
      Connector connector = info.getConnector();
      synchronized (this) {
        connectors.add(connector);
      }
      return connector;
    } catch (InstantiatorException e) {
      throw new
          RepositoryException("ConnectorFactory failed to make connector.", e);
    }
  }

  /**
   * Shutdown any connector instances created by the factory.
   */
  synchronized void shutdown() {
    for (Connector connector : connectors) {
      if (connector instanceof ConnectorShutdownAware) {
        try {
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
