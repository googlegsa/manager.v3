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

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.scheduler.Scheduler;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 */
public class SpringInstantiator implements Instantiator {

  private static final Logger LOGGER =
      Logger.getLogger(SpringInstantiator.class.getName());

  TypeMap typeMap = null;
  InstanceMap instanceMap = null;
  Pusher pusher;
  Map<String, ConnectorInterfaces> connectorCache;

  /**
   * Normal constructor.
   *
   * @param pusher
   */
  public SpringInstantiator(Pusher pusher) {
    this.pusher = pusher;
    connectorCache = new HashMap<String, ConnectorInterfaces>();
    // NOTE: we can't call initialize() here because then there would be a
    // circular dependency on the Context, which hasn't been constructed yet
  }

  /**
   * Constructor used by unit tests.  Provides a specific test Context.
   *
   * @param pusher
   * @param typeMap
   */
  public SpringInstantiator(Pusher pusher, TypeMap typeMap) {
    this(pusher);
    this.typeMap = typeMap;
    this.instanceMap = new InstanceMap(typeMap);
  }

  /*
   * Initializes the Context, post bean construction.
   * NOTE: Object lock must be held to call this method.
   */
  private void initialize() {
    if (typeMap != null) {
      return;
    }
    LOGGER.info("Initializing instantiator");
    typeMap = new TypeMap();
    instanceMap = new InstanceMap(typeMap);
  }

  /**
   * Shutdown all connector instances.
   */
  public synchronized void shutdown() {
    if (connectorCache != null) {
      connectorCache.clear();
    }
    if (instanceMap != null) {
      instanceMap.shutdown();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #removeConnector(java.lang.String)
   */
  public synchronized void removeConnector(String connectorName) {
    initialize();
    LOGGER.info("Dropping connector: " + connectorName);
    Scheduler scheduler = (Scheduler) Context.getInstance().
        getBean("TraversalScheduler", Scheduler.class);
    connectorCache.remove(connectorName);
    instanceMap.removeConnector(connectorName);
    if (scheduler != null) {
      scheduler.removeConnector(connectorName);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getAuthenticationManager(java.lang.String)
   */
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorInterfaces(connectorName).getAuthenticationManager();
  }

  private synchronized ConnectorInterfaces getConnectorInterfaces(
      String connectorName) throws ConnectorNotFoundException {
    ConnectorInterfaces connectorInterfaces = connectorCache.get(connectorName);
    if (connectorInterfaces == null) {
      InstanceInfo info = getInstanceInfo(connectorName);
      connectorInterfaces = new ConnectorInterfaces(connectorName,
          info.getConnector(), pusher, info.getTraversalStateStore());
      connectorCache.put(connectorName, connectorInterfaces);
    }
    return connectorInterfaces;
  }

  private synchronized InstanceInfo getInstanceInfo(String connectorName)
      throws ConnectorNotFoundException {
    initialize();
    InstanceInfo instanceInfo = instanceMap.get(connectorName);
    if (instanceInfo == null) {
      throw new ConnectorNotFoundException("Connector not found: "
          + connectorName);
    }
    return instanceInfo;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getAuthorizationManager(java.lang.String)
   */
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorInterfaces(connectorName).getAuthorizationManager();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConfigFormForConnector(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, Locale locale)
      throws ConnectorNotFoundException, InstantiatorException {
    InstanceInfo instanceInfo = getInstanceInfo(connectorName);
    TypeInfo typeInfo = instanceInfo.getTypeInfo();
    ConnectorType connectorType = typeInfo.getConnectorType();
    Map<String, String> configMap = instanceInfo.getConnectorConfig();
    try {
      return connectorType.getPopulatedConfigForm(configMap, locale);
    } catch (Exception e) {
      throw new InstantiatorException("Failed to get configuration form", e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorInstancePrototype(java.lang.String)
   */
  public String getConnectorInstancePrototype(String connectorTypeName) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorType(java.lang.String)
   */
  public synchronized ConnectorType getConnectorType(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    initialize();
    TypeInfo typeInfo = typeMap.getTypeInfo(connectorTypeName);
    if (typeInfo == null) {
      throw new ConnectorTypeNotFoundException("Connector Type not found: "
          + connectorTypeName);
    }
    return typeInfo.getConnectorType();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorTypeNames()
   */
  public synchronized Set<String> getConnectorTypeNames() {
    initialize();
    return Collections.unmodifiableSet(new TreeSet<String>(typeMap.keySet()));
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getTraverser(java.lang.String)
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorInterfaces(connectorName).getTraverser();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #restartConnectorTraversal(java.lang.String)
   */
  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException {
    initialize();
    LOGGER.info("Restarting traversal for Connector: " + connectorName);
    Scheduler scheduler = (Scheduler) Context.getInstance().
        getBean("TraversalScheduler", Scheduler.class);
    setConnectorState(connectorName, null);
    if (scheduler != null) {
      scheduler.removeConnector(connectorName);
    }
    connectorCache.remove(connectorName);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore
   *      #getConnectorNames()
   */
  public synchronized Set<String> getConnectorNames() {
    initialize();
    return Collections.unmodifiableSet(new TreeSet<String>(instanceMap.keySet()));
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore
   *      #getConnectorTypeName(java.lang.String)
   */
  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    return getInstanceInfo(connectorName).getTypeInfo().getConnectorTypeName();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #setConnectorConfig(java.lang.String, java.lang.String, java.util.Map)
   */
  public synchronized ConfigureResponse setConnectorConfig(String connectorName,
      String connectorTypeName, Map<String, String> configMap, Locale locale,
      boolean update) throws ConnectorNotFoundException,
      ConnectorExistsException, InstantiatorException {
    initialize();
    LOGGER.info("Configuring connector: " + connectorName);
    connectorCache.remove(connectorName);
    return instanceMap.updateConnector(
        connectorName, connectorTypeName, configMap, locale, update);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorConfig(java.lang.String)
   */
  public Map<String, String> getConnectorConfig(String connectorName)
      throws ConnectorNotFoundException {
    return getInstanceInfo(connectorName).getConnectorConfig();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #setConnectorSchedule(java.lang.String, java.lang.String)
   */
  public void setConnectorSchedule(String connectorName,
      String connectorSchedule) throws ConnectorNotFoundException {
    getInstanceInfo(connectorName).setConnectorSchedule(connectorSchedule);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorSchedule(java.lang.String)
   */
  public String getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return getInstanceInfo(connectorName).getConnectorSchedule();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #setConnectorState(java.lang.String, java.lang.String)
   */
  public void setConnectorState(String connectorName, String connectorState)
      throws ConnectorNotFoundException {
    getInstanceInfo(connectorName).setConnectorState(connectorState);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorState(java.lang.String)
   */
  public String getConnectorState(String connectorName)
      throws ConnectorNotFoundException {
    return getInstanceInfo(connectorName).getConnectorState();
  }
}
