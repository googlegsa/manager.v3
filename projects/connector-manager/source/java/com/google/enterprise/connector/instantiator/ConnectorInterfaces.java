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

import com.google.enterprise.connector.persist.ConnectorStateStore;
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

/**
 * 
 */
public class ConnectorInterfaces {

  final String connectorName;
  final Connector connector;
  final Pusher pusher;
  final ConnectorStateStore connectorStateStore;

  // these are lazily constructed
  Traverser traverser = null;
  AuthenticationManager authenticationManager = null;
  AuthorizationManager authorizationManager = null;

  String username = null;
  String password = null;

  /**
   * This constructor is the normal constructor
   * @param connectorName
   * @param connector
   * @param pusher
   * @param connectorStateStore
   */
  ConnectorInterfaces(String connectorName, Connector connector, Pusher pusher,
      ConnectorStateStore connectorStateStore) {
    this.connectorName = connectorName;
    this.connector = connector;
    this.pusher = pusher;
    this.connectorStateStore = connectorStateStore;
  }

  /**
   * This constructor will only be used by unit tests
   * @param connectorName
   * @param traverser
   * @param authenticationManager
   * @param authorizationManager
   */
  ConnectorInterfaces(String connectorName, Traverser traverser, AuthenticationManager authenticationManager,
      AuthorizationManager authorizationManager) {
    this.connectorName = connectorName;
    this.connector = null;;
    this.pusher = null;
    this.connectorStateStore = null;
    this.traverser = traverser;
    this.authenticationManager = authenticationManager;
    this.authorizationManager = authorizationManager;
  }

  /**
   * @return the authenticationManager
   * @throws InstantiatorException
   */
  AuthenticationManager getAuthenticationManager() throws InstantiatorException {
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
