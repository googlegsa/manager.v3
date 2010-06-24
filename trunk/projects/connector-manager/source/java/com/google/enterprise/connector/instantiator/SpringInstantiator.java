// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Instantiator} that supports Spring based connector instantiation and
 * persistent storage of connector configuration, schedule and traversal state.
 */
public class SpringInstantiator implements Instantiator {

  private static final Logger LOGGER =
      Logger.getLogger(SpringInstantiator.class.getName());

  private final ConcurrentMap<String, ConnectorCoordinator> coordinatorMap;

  // State that is filled in by setters from Spring.
  private PusherFactory pusherFactory;
  private LoadManagerFactory loadManagerFactory;
  private ThreadPool threadPool;

  // State that is filled in by init.
  private TypeMap typeMap;
  private ChangeListener changeListener;

  /**
   * Normal constructor.
   */
  public SpringInstantiator() {
    this.coordinatorMap = new ConcurrentHashMap<String, ConnectorCoordinator>();

    // NOTE: we can't call init() here because then there would be a
    // circular dependency on the Context, which hasn't been constructed yet
  }

  /**
   * Sets the {@link PusherFactory} used to create instances of
   * {@link com.google.enterprise.connector.pusher.Pusher Pusher}
   * for pushing documents to the GSA.
   *
   * @param pusherFactory a {@link PusherFactory} implementation.
   */
  public void setPusherFactory(PusherFactory pusherFactory) {
    this.pusherFactory = pusherFactory;
  }

  /**
   * Sets the {@link LoadManagerFactory} used to create instances of
   * {@link com.google.enterprise.connector.scheduler.LoadManager LoadManager}
   * for controlling feed rate.
   *
   * @param loadManagerFactory a {@link LoadManagerFactory}.
   */
  public void setLoadManagerFactory(LoadManagerFactory loadManagerFactory) {
    this.loadManagerFactory = loadManagerFactory;
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
    changeListener = new ChangeListenerImpl(this);
    // TODO: create a ChangeDetector hooked up to this ChangeListener.
    ConnectorCoordinatorMapHelper.fillFromTypes(typeMap, coordinatorMap,
        pusherFactory, loadManagerFactory, threadPool);
  }

  /**
   * Shutdown all connector instances.
   */
  public void shutdown(boolean interrupt, long timeoutMillis) {
    for (ConnectorCoordinator cc : coordinatorMap.values()) {
      cc.shutdown();
    }
    try {
      if (threadPool != null) {
        threadPool.shutdown(interrupt, timeoutMillis);
      }
    } catch (InterruptedException ie) {
      LOGGER.log(Level.SEVERE, "TraversalScheduler shutdown interrupted: ", ie);
    }
  }

  public void removeConnector(String connectorName) {
    LOGGER.info("Dropping connector: " + connectorName);
    ConnectorCoordinator existing = coordinatorMap.get(connectorName);
    if (existing != null) {
      existing.removeConnector(); // TODO: PStore eventual setter
    }
  }

  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthenticationManager();
  }

  public ConnectorCoordinator getConnectorCoordinator(
      String connectorName) throws ConnectorNotFoundException {
    ConnectorCoordinator connectorCoordinator =
      coordinatorMap.get(connectorName);
    if (connectorCoordinator == null) {
      throw new ConnectorNotFoundException();
    }
    return connectorCoordinator;
  }

  public ChangeHandler getChangeHandler(String connectorName) {
      return (ChangeHandler) getOrAddConnectorCoordinator(connectorName);
  }

  private ConnectorCoordinator getOrAddConnectorCoordinator(
      String connectorName) {
    if (typeMap == null) {
      throw new IllegalStateException(
          "Init must be called before accessing connectors.");
    }
    ConnectorCoordinator connectorCoordinator =
        coordinatorMap.get(connectorName);
    if (connectorCoordinator == null) {
      ConnectorCoordinator ci = new ConnectorCoordinatorImpl(
          connectorName, pusherFactory, loadManagerFactory, threadPool);
      ConnectorCoordinator existing =
          coordinatorMap.putIfAbsent(connectorName, ci);
      connectorCoordinator = (existing == null) ? ci : existing;
    }
    return connectorCoordinator;
  }

  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthorizationManager();
  }

  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, Locale locale)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getConfigForm(locale);
  }

  public String getConnectorInstancePrototype(String connectorTypeName) {
    throw new UnsupportedOperationException();
  }

  public synchronized ConnectorType getConnectorType(String typeName)
      throws ConnectorTypeNotFoundException {
    return getTypeInfo(typeName).getConnectorType();
  }

  TypeInfo getTypeInfo(String typeName)
      throws ConnectorTypeNotFoundException {
    TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
    if (typeInfo == null) {
      throw new ConnectorTypeNotFoundException("Connector Type not found: "
          + typeName);
    }
    return typeInfo;
  }

  public synchronized Set<String> getConnectorTypeNames() {
    return Collections.unmodifiableSet(new TreeSet<String>(typeMap.keySet()));
  }

  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException {
    LOGGER.info("Restarting traversal for Connector: " + connectorName);
    getConnectorCoordinator(connectorName).restartConnectorTraversal(); // TODO: PStore eventual Setter
  }

  public Set<String> getConnectorNames() {
    Set<String> result = new TreeSet<String>();
    for (Map.Entry<String, ConnectorCoordinator> e :
        coordinatorMap.entrySet()) {
      if (e.getValue().exists()) {
        result.add(e.getKey());
      }
    }
    return Collections.unmodifiableSet(result);
  }

  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorTypeName();
  }

  public ConfigureResponse setConnectorConfig(String connectorName,
      String connectorTypeName, Map<String, String> configMap, Locale locale,
      boolean update) throws ConnectorNotFoundException,
      ConnectorExistsException, InstantiatorException {
    LOGGER.info("Configuring connector: " + connectorName);
    try {
      TypeInfo typeInfo = getTypeInfo(connectorTypeName);
      ConnectorCoordinator ci = getOrAddConnectorCoordinator(connectorName);
      return ci.setConnectorConfig(typeInfo, configMap, locale, update);  // TODO: PStore eventual setter
    } catch (ConnectorTypeNotFoundException ctnf) {
      throw new ConnectorNotFoundException("Incorrect type", ctnf);
    }
  }

  public Map<String, String> getConnectorConfig(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorConfig();
  }

  public void setConnectorSchedule(String connectorName,
      String connectorSchedule) throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).
        setConnectorSchedule(connectorSchedule);  // TODO: PStore eventual Setter
  }

  public String getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return  getConnectorCoordinator(connectorName).getConnectorSchedule();
  }
}
