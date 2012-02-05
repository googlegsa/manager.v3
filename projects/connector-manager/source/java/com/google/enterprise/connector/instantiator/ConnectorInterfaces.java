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

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.Lister;
import com.google.enterprise.connector.spi.ListerAware;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.Retriever;
import com.google.enterprise.connector.spi.RetrieverAware;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.logging.Logger;

/**
 * Access to the AuthenticationManager, AuthorizationManager, and
 * TraversalManagager for a Connector instance.
 */
public class ConnectorInterfaces {
  private static final Logger LOGGER =
      Logger.getLogger(ConnectorInterfaces.class.getName());

  private final String connectorName;
  private final Connector connector;

  // these are lazily constructed
  private TraversalManager traversalManager;
  private AuthenticationManager authenticationManager;
  private AuthorizationManager authorizationManager;
  private Retriever retriever;
  private boolean gotRetriever = false;
  private Lister lister;
  private boolean gotLister = false;

  ConnectorInterfaces(String connectorName, Connector connector) {
    this.connectorName = connectorName;
    this.connector = connector;
  }

  /**
   * Constructs a ConnectorIntefaces with Managers supplied by the caller
   * rather than the connector. This is for testing only.
   */
  ConnectorInterfaces(String connectorName, TraversalManager traversalManager,
                      AuthenticationManager authenticationManager,
                      AuthorizationManager authorizationManager) {
    this.connectorName = connectorName;
    this.connector = null;
    this.traversalManager = traversalManager;
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
        LOGGER.fine("Got AuthenticationManager " + authenticationManager);
      } catch (RepositoryException e) {
        // TODO(ziff): think about how this could be re-tried
        throw new InstantiatorException(e);
      } catch (Exception e) {
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
        LOGGER.fine("Got AuthorizationManager " + authorizationManager);
      } catch (RepositoryException e) {
        // TODO(ziff): think about how this could be re-tried
        throw new InstantiatorException(e);
      } catch (Exception e) {
        throw new InstantiatorException(e);
      }
    }
    return authorizationManager;
  }

  /**
   * Return a {@link Lister} that may be used to feed documents to the GSA.
   * If the connector does not support the {@link Lister} interface,
   * {@code null} is returned.
   *
   * @return a {@link Lister}, or {@code null} if none is available
   * @throws InstantiatorException if unable to instantiate the requested
   *         {@link Lister}
   */
  Lister getLister() throws InstantiatorException {
    if (!gotLister) {
      Session s = getSession();
      gotLister = true;
      lister = null;
      if (s instanceof ListerAware) {
        try {
          lister = ((ListerAware) s).getLister();
          LOGGER.fine("Got Lister " + lister);
        } catch (RepositoryException e) {
          // TODO(ziff): think about how this could be re-tried
          throw new InstantiatorException(e);
        } catch (Exception e) {
          throw new InstantiatorException(e);
        }
      }
    }
    return lister;
  }

  /**
   * Return a {@link Retriever} that may be used to access content for the
   * document identified by a document ID.  If the connector does not support
   * the {@link Retriever} interface, {@code null} is returned.
   *
   * @return a {@link Retriever}, or {@code null} if none is available
   * @throws InstantiatorException if unable to instantiate the requested
   *         {@link Retriever}
   */
  Retriever getRetriever() throws InstantiatorException {
    if (!gotRetriever) {
      Session s = getSession();
      gotRetriever = true;
      retriever = null;
      if (s instanceof RetrieverAware) {
        try {
          retriever = ((RetrieverAware) s).getRetriever();
          LOGGER.fine("Got Retriever " + retriever);
        } catch (RepositoryException e) {
          // TODO(ziff): think about how this could be re-tried
          throw new InstantiatorException(e);
        } catch (Exception e) {
          throw new InstantiatorException(e);
        }
      }
    }
    return retriever;
  }

  /**
   * @return the connector
   */
  // TODO(strellis) Remove this method or make it private so all connector
  // access is through InstanceInfo.
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
  TraversalManager getTraversalManager() throws InstantiatorException {
    if (traversalManager == null) {
      Session s = getSession();
      try {
        traversalManager = s.getTraversalManager();
        LOGGER.fine("Got TraversalManager " + traversalManager);
      } catch (RepositoryException ie) {
        throw new InstantiatorException(ie);
      } catch (Exception e) {
        throw new InstantiatorException(e);
      }
    }
    return traversalManager;
  }

  private Session getSession() throws InstantiatorException {
    Session s = null;
    try {
      LOGGER.fine("LOGIN: Getting Session from connector " + connectorName);
      s = connector.login();
      LOGGER.fine("Got Session " + s);
    } catch (RepositoryLoginException e) {
      // this is un-recoverable
      throw new InstantiatorException(e);
    } catch (RepositoryException e) {
      // for this one, we could try again later
      // TODO(ziff): think about how this could be re-tried
      throw new InstantiatorException(e);
    } catch (Exception e) {
      throw new InstantiatorException(e);
    }
    return s;
  }
}
