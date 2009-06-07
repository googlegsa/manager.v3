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
import com.google.enterprise.connector.scheduler.Scheduler;
import com.google.enterprise.connector.scheduler.ThreadPool;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * {@link Instantiator} that supports Spring based connector instantiation and
 * persistent storage of connector configuration, schedule and traversal state.
 */
public class SpringInstantiator implements Instantiator {

  private static final Logger LOGGER =
      Logger.getLogger(SpringInstantiator.class.getName());

  TypeMap typeMap = null;
  final ConcurrentMap<String, ConnectorCoordinator> cim;
  final Pusher pusher;
  Scheduler scheduler;
  final ThreadPool threadPool = null;

  /**
   * Normal constructor.
   *
   * @param pusher
   */
  public SpringInstantiator(Pusher pusher) {
    this.pusher = pusher;
    this.cim = new ConcurrentHashMap<String, ConnectorCoordinator>();
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
    ConnectorCoordinatorMapHelper.fillFromTypes(typeMap, cim, pusher,
        threadPool);
  }

  /**
   * Set the Scheduler.
   *
   * @param scheduler a Scheduler.
   */
  /* Setter Injector */
  public synchronized void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  /**
   * Initializes the Context, post bean construction.
   */
  public synchronized void init() {
    if (typeMap == null) {
      LOGGER.info("Initializing instantiator");

      typeMap = new TypeMap();
      ConnectorCoordinatorMapHelper.fillFromTypes(typeMap, cim, pusher, threadPool);
    }
  }

  /**
   * Shutdown all connector instances.
   */
  public void shutdown() {
    for (ConnectorCoordinator cc : cim.values()) {
      cc.shutdown();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #removeConnector(java.lang.String)
   */
  public synchronized void removeConnector(String connectorName) {
    LOGGER.info("Dropping connector: " + connectorName);
    ConnectorCoordinator removeMe = cim.get(connectorName);
    if(removeMe != null) {
      removeMe.removeConnector();
    }
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
    return getConnectorCoordinator(connectorName).getAuthenticationManager();
  }

  public ConnectorCoordinator getConnectorCoordinator(
      String connectorName) throws ConnectorNotFoundException {
    ConnectorCoordinator connectorInstance = cim.get(connectorName);
    if (connectorInstance == null) {
      throw new ConnectorNotFoundException();
    }
    return connectorInstance;
  }

  private synchronized ConnectorCoordinator getOrAddConnectorInstance(
      String connectorName) {
    ConnectorCoordinator connectorInstance = cim.get(connectorName);
    if (connectorInstance == null) {
      ConnectorCoordinator ci = new ConnectorCoordinatorImpl(connectorName, pusher, threadPool);
      ConnectorCoordinator existing = cim.putIfAbsent(connectorName, ci);
      connectorInstance =  existing == null ? ci : existing;
    }
    return connectorInstance;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getAuthorizationManager(java.lang.String)
   */
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthorizationManager();
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
    return getConnectorCoordinator(connectorName).getConfigForm(locale);
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
  public synchronized ConnectorType getConnectorType(String typeName)
      throws ConnectorTypeNotFoundException {
    return getTypeInfo(typeName).getConnectorType();
  }

  private TypeInfo getTypeInfo(String typeName) throws ConnectorTypeNotFoundException {
    TypeInfo typeInfo = typeMap.getTypeInfo(typeName);
    if (typeInfo == null) {
      throw new ConnectorTypeNotFoundException("Connector Type not found: "
          + typeName);
    }
    return typeInfo;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorTypeNames()
   */
  public synchronized Set<String> getConnectorTypeNames() {
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
    return getConnectorCoordinator(connectorName).getTraverser();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #restartConnectorTraversal(java.lang.String)
   */
  public synchronized void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException {
    LOGGER.info("Restarting traversal for Connector: " + connectorName);
    getConnectorCoordinator(connectorName).restartConnectorTraversal();
    if (scheduler != null) {
      scheduler.removeConnector(connectorName);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore
   *      #getConnectorNames()
   */
  public Set<String> getConnectorNames() {
    Set<String> result = new TreeSet<String>();
    for (Map.Entry<String, ConnectorCoordinator> e : cim.entrySet()) {
      if(e.getValue().exists()) {
        result.add(e.getKey());
      }
    }
    return Collections.unmodifiableSet(result);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.persist.ConnectorConfigStore
   *      #getConnectorTypeName(java.lang.String)
   */
  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorTypeName();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #setConnectorConfig(java.lang.String, java.lang.String, java.util.Map)
   */
  public ConfigureResponse setConnectorConfig(String connectorName,
      String connectorTypeName, Map<String, String> configMap, Locale locale,
      boolean update) throws ConnectorNotFoundException,
      ConnectorExistsException, InstantiatorException {
    LOGGER.info("Configuring connector: " + connectorName);
    if (update) {
      if (scheduler != null) {
        scheduler.removeConnector(connectorName);
      }
    }
    try {
      TypeInfo typeInfo = getTypeInfo(connectorTypeName);
      ConnectorCoordinator ci = getOrAddConnectorInstance(connectorName);
      return ci.setConnectorConfig(typeInfo, configMap, locale, update);
    } catch (ConnectorTypeNotFoundException ctnf) {
      throw new ConnectorNotFoundException("Incorrect type", ctnf);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorConfig(java.lang.String)
   */
  public Map<String, String> getConnectorConfig(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorConfig();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #setConnectorSchedule(java.lang.String, java.lang.String)
   */
  public void setConnectorSchedule(String connectorName,
      String connectorSchedule) throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).setConnectorSchedule(connectorSchedule);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.instantiator.Instantiator
   *      #getConnectorSchedule(java.lang.String)
   */
  public String getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return  getConnectorCoordinator(connectorName).getConnectorSchedule();
  }
}
