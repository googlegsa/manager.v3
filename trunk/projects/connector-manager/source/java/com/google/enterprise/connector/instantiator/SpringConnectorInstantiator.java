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
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.traversal.QueryTraverser;
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
        Connector c =
            instantiateSingleConnectorFromResource(connectorName,
                connectorResource);
        ConnectorInterfaces connectorInterfaces =
            new ConnectorInterfaces(connectorName, c);
        if (c != null) {
          connectorMap.put(connectorName, connectorInterfaces);
        }
      }
    }
  }

  private Connector instantiateSingleConnectorFromResource(
      String connectorName, String resource) {
    String fullResource = "file:" + resource;
    ApplicationContext ac = new ClassPathXmlApplicationContext(fullResource);
    String[] beanList = ac.getBeanNamesForType(Connector.class);
    if (beanList.length < 1) {
      LOGGER.warning("Spring finds no Connector definition for connector "
          + connectorName + " in resource " + fullResource
          + ".  Skipping this connector.");
      return null;
    }
    if (beanList.length > 1) {
      // TODO(ziff): maybe print out all the named Connectors found
      LOGGER
          .warning("Spring finds multiple Connector definitions for connector "
              + connectorName + " in resource " + fullResource
              + ".  Instantiating only the first one.");
    }
    Connector c = (Connector) ac.getBean(beanList[0]);
    return c;
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
    AuthenticationManager result = connectorInterfaces.getAuthenticationManager();
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
    ConnectorInterfaces connectorInterfaces =
        findConnectorInterfaces(connectorName);
    Traverser result = connectorInterfaces.getTraverser();
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
    Connector c =
        instantiateSingleConnectorFromResource(connectorName, resourceString);
    ConnectorInterfaces connectorInterfaces =
        new ConnectorInterfaces(connectorName, c);
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

  class ConnectorInterfaces {
    final String connectorName;
    final Connector connector;
    // these are lazily constructed
    Traverser traverser = null;
    AuthenticationManager authenticationManager = null;
    AuthorizationManager authorizationManager = null;

    String username = null;
    String password = null;

    ConnectorInterfaces(String connectorName, Connector connector) {
      this.connectorName = connectorName;
      this.connector = connector;
    }

    /**
     * @return the authenticationManager
     * @throws InstantiatorException
     */
    AuthenticationManager getAuthenticationManager()
        throws InstantiatorException {
      if (authenticationManager == null) {
        Session s = getSession();
        try {
          authenticationManager = s.getAuthenticationManager();
        } catch (RepositoryException e) {
          // TODO(ziff): think about how this could be re-tried
          throw new InstantiatorException(e);
        }
      }
      return authenticationManager;
    }

    /**
     * @return the authorizationManager
     * @throws InstantiatorException
     */
    AuthorizationManager getAuthorizationManager() throws InstantiatorException {
      if (authorizationManager == null) {
        Session s = getSession();
        try {
          authorizationManager = s.getAuthorizationManager();
        } catch (RepositoryException e) {
          // TODO(ziff): think about how this could be re-tried
          throw new InstantiatorException(e);
        }
      }
      return authorizationManager;
    }

    /**
     * @return the connector
     */
    Connector getConnector() {
      return connector;
    }

    /**
     * @return the connectorName
     */
    String getConnectorName() {
      return connectorName;
    }

    /**
     * @return the traverser
     * @throws InstantiatorException
     */
    Traverser getTraverser() throws InstantiatorException {
      if (traverser == null) {
        Session s = getSession();
        QueryTraversalManager qtm = null;
        try {
          qtm = s.getQueryTraversalManager();
        } catch (RepositoryException e) {
          // TODO(ziff): think about how this could be re-tried
        }
        traverser =
            new QueryTraverser(pusher, qtm, connectorStateStore, connectorName);
      }
      return traverser;
    }

    private Session getSession() throws InstantiatorException {
      Session s = null;
      getCredentials();
      try {
        s = connector.login(username, password);
      } catch (LoginException e) {
        // this is un-recoverable
        throw new InstantiatorException(e);
      } catch (RepositoryException e) {
        // for this one, we could try again later
        // TODO(ziff): think about how this could be re-tried
        throw new InstantiatorException(e);
      }
      return s;
    }

    private void getCredentials() {
      // TODO we have to move this back into the Connector config itself
      password = "";
      username = "";
    }

  }

}
