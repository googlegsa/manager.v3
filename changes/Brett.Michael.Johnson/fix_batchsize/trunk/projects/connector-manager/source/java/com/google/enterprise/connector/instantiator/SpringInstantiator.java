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
import com.google.enterprise.connector.pusher.Pusher;
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

  final ConcurrentMap<String, ConnectorCoordinator> coordinatorMap;
  final Pusher pusher;
  final ThreadPool threadPool;

  // State that is filled in by init.
  TypeMap typeMap;

  /**
   * Normal constructor.
   *
   * @param pusher
   */
  public SpringInstantiator(Pusher pusher, ThreadPool threadPool) {
    this.pusher = pusher;
    this.threadPool = threadPool;
    this.coordinatorMap = new ConcurrentHashMap<String, ConnectorCoordinator>();
    // NOTE: we can't call init() here because then there would be a
    // circular dependency on the Context, which hasn't been constructed yet
  }

  /**
   * Constructor used by unit tests.  Provides a specific test Context.
   *
   * @param pusher
   * @param typeMap
   */
  public SpringInstantiator(Pusher pusher, ThreadPool threadPool,
      TypeMap typeMap) {
    this(pusher, threadPool);
    this.typeMap = typeMap;
    init(typeMap);
  }

  /**
   * Initializes the Context, post bean construction.
   */
  public synchronized void init() {
    if (typeMap == null) {
      LOGGER.info("Initializing instantiator");
      init(new TypeMap());
      typeMap = new TypeMap();
    }
  }

  private void init(TypeMap typeMap) {
    ConnectorCoordinatorMapHelper.fillFromTypes(typeMap, coordinatorMap,
        pusher, threadPool);
  }

  /**
   * Shutdown all connector instances.
   */
  public void shutdown(boolean interrupt, long timeoutMillis) {
    for (ConnectorCoordinator cc : coordinatorMap.values()) {
      cc.shutdown();
    }
    try {
      threadPool.shutdown(interrupt, timeoutMillis);
    } catch (InterruptedException ie) {
      LOGGER.log(Level.SEVERE, "TraversalScheduler shutdown interrupted: ", ie);
    }
  }

  public void removeConnector(String connectorName) {
    LOGGER.info("Dropping connector: " + connectorName);
    ConnectorCoordinator existing = coordinatorMap.get(connectorName);
    if (existing != null) {
      existing.removeConnector();
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

  private ConnectorCoordinator getOrAddConnectorCoordinator(
      String connectorName) {
    if (typeMap == null) {
      throw new IllegalStateException(
          "Init must be called before accessing connectors.");
    }
    ConnectorCoordinator connectorCoordinator =
        coordinatorMap.get(connectorName);
    if (connectorCoordinator == null) {
      ConnectorCoordinator ci =
          new ConnectorCoordinatorImpl(connectorName, pusher, threadPool);
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

  private TypeInfo getTypeInfo(String typeName)
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
    getConnectorCoordinator(connectorName).restartConnectorTraversal();
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
      return ci.setConnectorConfig(typeInfo, configMap, locale, update);
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
        setConnectorSchedule(connectorSchedule);
  }

  public String getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return  getConnectorCoordinator(connectorName).getConnectorSchedule();
  }
}
