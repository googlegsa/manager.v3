// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Instantiator} that supports Spring-based connector instantiation and
 * persistent storage of connector configuration, schedule and traversal state.
 */
public class SpringInstantiator implements Instantiator {
  private static final Logger LOGGER =
      Logger.getLogger(SpringInstantiator.class.getName());


  // State that is filled in by setters from Spring.
  private ConnectorCoordinatorMap coordinatorMap;
  private ThreadPool threadPool;

  // State that is filled in by init.
  private TypeMap typeMap;
  private ChangeListener changeListener;

  /**
   * Normal constructor.
   */
  public SpringInstantiator() {
    // NOTE: we can't call init() here because then there would be a
    // circular dependency on the Context, which hasn't been constructed yet
  }

  /**
   * Sets the {@link ConnectorCoordinatorMap} instance used to manage the
   * instances of {@link ConnectorCoordinator}.
   *
   * @param coordinatorMap a {@link ConnectorCoordinatorMap} instance
   */
  public void setConnectorCoordinatorMap(
      ConnectorCoordinatorMap coordinatorMap) {
    this.coordinatorMap = coordinatorMap;
  }

  /**
   * Sets the {@link ThreadPool} used for running traversals.
   *
   * @param threadPool a {@link ThreadPool} implementation.
   */
  public void setThreadPool(ThreadPool threadPool) {
    this.threadPool = threadPool;
  }

  /**
   * Sets the {@link TypeMap} of installed {@link ConnectorType}s.
   *
   * @param typeMap a {@link TypeMap}.
   */
  public void setTypeMap(TypeMap typeMap) {
    this.typeMap = typeMap;
  }

  /**
   * Initializes the Context, post bean construction.
   */
  public synchronized void init() {
    LOGGER.info("Initializing instantiator");
    if (typeMap == null) {
      setTypeMap(new TypeMap());
    }
    changeListener = new ChangeListenerImpl(typeMap, coordinatorMap);
    // TODO: create a ChangeDetector hooked up to this ChangeListener.
    ConnectorCoordinatorMapHelper.fillFromTypes(typeMap, coordinatorMap);
  }

  /**
   * Shutdown all connector instances.
   */
  /* @Override */
  public void shutdown(boolean interrupt, long timeoutMillis) {
    coordinatorMap.shutdown();
    try {
      if (threadPool != null) {
        threadPool.shutdown(interrupt, timeoutMillis);
      }
    } catch (InterruptedException ie) {
      LOGGER.log(Level.SEVERE, "TraversalScheduler shutdown interrupted: ", ie);
    }
  }

  /* @Override */
  public void removeConnector(String connectorName) {
    LOGGER.info("Dropping connector: " + connectorName);
    ConnectorCoordinator existing = coordinatorMap.get(connectorName);
    if (existing != null) {
      existing.removeConnector(); // TODO: PStore eventual setter
    }
  }

  /* @Override */
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthenticationManager();
  }

  /* @Override */
  public void startBatch(String connectorName)
      throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).startBatch();
  }

  @VisibleForTesting
  ConnectorCoordinator getConnectorCoordinator(String connectorName)
      throws ConnectorNotFoundException {
    ConnectorCoordinator connectorCoordinator =
        coordinatorMap.get(connectorName);
    if (connectorCoordinator == null) {
      throw new ConnectorNotFoundException();
    }
    return connectorCoordinator;
  }

  private ConnectorCoordinator getOrAddConnectorCoordinator(
      String connectorName) {
    if (typeMap == null) {
      throw new IllegalStateException(
          "Init must be called before accessing connectors.");
    }
    return coordinatorMap.getOrAdd(connectorName);
  }

  /* @Override */
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthorizationManager();
  }

  /* @Override */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, Locale locale)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getConfigForm(locale);
  }

  /* @Override */
  public String getConnectorInstancePrototype(String connectorTypeName) {
    throw new UnsupportedOperationException();
  }

  /* @Override */
  public synchronized ConnectorType getConnectorType(String typeName)
      throws ConnectorTypeNotFoundException {
    return typeMap.getTypeInfo(typeName).getConnectorType();
  }

  /* @Override */
  public synchronized Set<String> getConnectorTypeNames() {
    return typeMap.getConnectorTypeNames();
  }

  /* @Override */
  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException {
    LOGGER.info("Restarting traversal for Connector: " + connectorName);
    getConnectorCoordinator(connectorName).restartConnectorTraversal(); // TODO: PStore eventual Setter
  }

  /* @Override */
  public Set<String> getConnectorNames() {
    return coordinatorMap.getConnectorNames();
  }

  /* @Override */
  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorTypeName();
  }

  /* @Override */
  public ConfigureResponse setConnectorConfig(String connectorName,
      String connectorTypeName, Map<String, String> configMap, Locale locale,
      boolean update) throws ConnectorNotFoundException,
      ConnectorExistsException, InstantiatorException {
    LOGGER.info("Configuring connector: " + connectorName);
    try {
      TypeInfo typeInfo = typeMap.getTypeInfo(connectorTypeName);
      ConnectorCoordinator ci = getOrAddConnectorCoordinator(connectorName);
      return ci.setConnectorConfig(typeInfo, configMap, locale, update);  // TODO: PStore eventual setter
    } catch (ConnectorTypeNotFoundException ctnf) {
      throw new ConnectorNotFoundException("Incorrect type", ctnf);
    }
  }

  /* @Override */
  public Map<String, String> getConnectorConfig(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorConfig();
  }

  /* @Override */
  public void setConnectorSchedule(String connectorName,
      String connectorSchedule) throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).
        setConnectorSchedule(connectorSchedule);  // TODO: PStore eventual Setter
  }

  /* @Override */
  public String getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return  getConnectorCoordinator(connectorName).getConnectorSchedule();
  }
}
