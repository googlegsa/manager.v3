// Copyright (C) 2006 Google Inc.
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

import com.google.enterprise.connector.persist.ConnectorConfigStore;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.traversal.Traverser;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Instantiator for Connector objects. Uses Spring and the classpath.
 */
public class SpringConnectorInstantiator implements ConnectorInstantiator {

  private static final String NULL_CONNECTOR_STATE_STORE_MESSAGE =
      "SpringConnectorInstantiator requires non-null connectorStateStore";

  private static final String NULL_PUSHER_MESSAGE =
      "SpringConnectorInstantiator requires non-null pusher";

  private static final String NULL_STORE_MESSAGE =
      "SpringConnectorInstantiator requires non-null store";

  private static final Logger LOGGER =
      Logger.getLogger(SpringConnectorInstantiator.class.getName());

  // dependencies
  private Pusher pusher = null;
  private ConnectorStateStore connectorStateStore = null;
  private ConnectorConfigStore store = null;

  // implementation fields
  private SortedMap connectorMap = null;
  private boolean initialized = false;


  /**
   * Default, no-argument constructor
   * 
   */
  public SpringConnectorInstantiator() {
    connectorMap = new TreeMap();
  }

  /**
   * @param store the store to set
   */
  public void setStore(ConnectorConfigStore store) {
    if (store == null) {
      throw new IllegalArgumentException(NULL_STORE_MESSAGE);
    }
    this.store = store;
  }

  /**
   * @param connectorStateStore the connectorStateStore to set
   */
  public void setConnectorStateStore(ConnectorStateStore connectorStateStore) {
    if (connectorStateStore == null) {
      throw new IllegalArgumentException(NULL_CONNECTOR_STATE_STORE_MESSAGE);
    }
    this.connectorStateStore = connectorStateStore;
  }

  /**
   * @param pusher the pusher to set
   */
  public void setPusher(Pusher pusher) {
    if (pusher == null) {
      throw new IllegalArgumentException(NULL_PUSHER_MESSAGE);
    }
    this.pusher = pusher;
  }

  private void verifyDependencies() {
    if (store == null) {
      throw new IllegalStateException(NULL_STORE_MESSAGE);
    }
    if (pusher == null) {
      throw new IllegalStateException(NULL_PUSHER_MESSAGE);
    }
    if (connectorStateStore == null) {
      throw new IllegalStateException(NULL_CONNECTOR_STATE_STORE_MESSAGE);
    }
  }

  private void initialize() {
    if (initialized) {
      return;
    }
    verifyDependencies();
    instantiateAllKnownConnectors();
    initialized = true;
  }

  private void instantiateAllKnownConnectors() {
    for (Iterator i = store.getConnectorNames(); i.hasNext();) {
      String connectorName = (String) i.next();
      String connectorResource = null;
      try {
        connectorResource = store.getConnectorResourceString(connectorName);
      } catch (ConnectorNotFoundException e) {
        LOGGER.warning("Store asserts that there is a connector named "
            + connectorName + " but no resource is found.");
      } catch (PersistentStoreException e) {
        LOGGER
            .warning("Persistent store exception while accessing resource for "
                + connectorName + ".  Skipping this connector.");
        // TODO(ziff): need generic way of logging exceptions, that may contain
        // embedded exceptions
        LOGGER.warning("Exception: " + e.getMessage());
      }
      if (connectorResource != null) {
        ConnectorInterfaces connectorInterfaces =
            instantiateSingleConnectorFromResource(connectorName,
                connectorResource);
        if (connectorInterfaces != null) {
          connectorMap.put(connectorName, connectorInterfaces);
        }
      }
    }
  }

  private ConnectorInterfaces instantiateSingleConnectorFromResource(
      String connectorName, String resource) {
    String fullResource = "file:" + resource;
    ApplicationContext ac = new ClassPathXmlApplicationContext(fullResource);
    Connector c =
        (Connector) getConnectorBeanFromAppContext(connectorName, fullResource,
            ac, Connector.class);
    Map configMap = (Map) ac.getBean("ConnectorConfigMap", Map.class);
    ConnectorInterfaces connectorInterfaces =
        new ConnectorInterfaces(connectorName, c, pusher, connectorStateStore,
            configMap);
    return connectorInterfaces;
  }

  private Object getConnectorBeanFromAppContext(String name, String resource,
      ApplicationContext ac, Class clazz) {
    String[] beanList = ac.getBeanNamesForType(clazz);
    if (beanList.length < 1) {
      LOGGER.warning("Spring finds no " + clazz.getName()
          + " definition for " + name + " in resource "
          + resource + ".  Skipping");
      return null;
    }
    if (beanList.length > 1) {
      // TODO(ziff): maybe print out all the named Connectors found
      LOGGER
          .warning("Spring finds multiple " + clazz.getName() + " definitions for "
              + name + " in resource " + resource
              + ".  Instantiating only the first one.");
    }
    Object bean = ac.getBean(beanList[0]);
    return bean;
  }

  private ConnectorInterfaces findConnectorInterfaces(String connectorName)
      throws ConnectorNotFoundException {
    if (connectorName == null || connectorName.length() < 1) {
      throw new IllegalArgumentException();
    }
    initialize();
    ConnectorInterfaces connectorInterfaces =
        (ConnectorInterfaces) connectorMap.get(connectorName);
    if (connectorInterfaces == null) {
      throw new ConnectorNotFoundException(connectorName);
    }
    return connectorInterfaces;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.ConnectorInstantiator#getAuthenticationManager(java.lang.String)
   */
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    initialize();
    ConnectorInterfaces connectorInterfaces =
        findConnectorInterfaces(connectorName);
    AuthenticationManager result =
        connectorInterfaces.getAuthenticationManager();
    if (result == null) {
      throw new InstantiatorException();
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.ConnectorInstantiator#getAuthorizationManager(java.lang.String)
   */
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    initialize();
    ConnectorInterfaces connectorInterfaces =
        findConnectorInterfaces(connectorName);
    AuthorizationManager result = connectorInterfaces.getAuthorizationManager();
    if (result == null) {
      throw new InstantiatorException();
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.ConnectorInstantiator#getTraverser(java.lang.String)
   */
  public Traverser getTraverser(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    initialize();
    ConnectorInterfaces connectorInterfaces =
        findConnectorInterfaces(connectorName);
    Traverser result = connectorInterfaces.getTraverser();
    if (result == null) {
      throw new InstantiatorException();
    }
    return result;
  }

  public Map getConfigMap(String connectorName) throws ConnectorNotFoundException, InstantiatorException {
    initialize();
    ConnectorInterfaces connectorInterfaces =
        findConnectorInterfaces(connectorName);
    Map result = connectorInterfaces.getConfigMap();
    if (result == null) {
      throw new InstantiatorException();
    }
    return result;
  }

  public void setConnectorConfig(String connectorName,
      String connectorTypeName, Map configKeys, String prototypeString)
      throws ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException {
    initialize();
    // Find out if this is an existing connector
    if (connectorMap.containsKey(connectorName)) {
      throw new IllegalArgumentException(
          "Can't set an existing connector - first drop then add");
    }
    String newConfig =
        SpringUtils.makeConnectorInstanceXml(connectorName, prototypeString,
            configKeys);
    // store it away
    try {
      store.setConnectorConfig(connectorName, connectorTypeName, newConfig);
    } catch (PersistentStoreException e) {
      throw new InstantiatorException(e);
    }
    String resourceString = null;
    try {
      resourceString = store.getConnectorResourceString(connectorName);
    } catch (PersistentStoreException e) {
      throw new InstantiatorException("Config store malfunction");
    }

    ConnectorInterfaces connectorInterfaces =
        instantiateSingleConnectorFromResource(connectorName, resourceString);
    connectorMap.put(connectorName, connectorInterfaces);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.instantiator.ConnectorInstantiator#dropConnector(java.lang.String)
   */
  public void dropConnector(String connectorName) throws InstantiatorException {
    initialize();
    store.dropConnector(connectorName);
    connectorMap.remove(connectorName);
  }

  public String getConnectorType(String connectorName) throws ConnectorNotFoundException {
    String connectorType = store.getConnectorType(connectorName);
    return connectorType;
  }

}
