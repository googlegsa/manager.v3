// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util.diffing;

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

import java.util.logging.Logger;

/**
 * Diffing connector implementation of the SPI {@link Connector} interface.
 *
 * @since 2.8
 */
public class DiffingConnector implements Connector,
    ConnectorShutdownAware, Session {
  private static final Logger LOG = Logger.getLogger(
      DiffingConnector.class.getName());

  private final AuthorizationManager authorizationManager;
  private final DocumentSnapshotRepositoryMonitorManager repositoryMonitorManager;
  private final TraversalContextManager traversalContextManager;
  private DiffingConnectorTraversalManager traversalManager = null;

  /**
   * Creates a DiffingConnector.
   *
   * @param authorizationManager an AuthorizationManager
   * @param repositoryMonitorManager a DocumentSnapshotRepositoryMonitorManager
   * @param traversalContextManager a TraversalContextManager
   */
  public DiffingConnector(AuthorizationManager authorizationManager,
      DocumentSnapshotRepositoryMonitorManager repositoryMonitorManager,
      TraversalContextManager traversalContextManager) {
    this.authorizationManager = authorizationManager;
    this.repositoryMonitorManager = repositoryMonitorManager;
    this.traversalContextManager = traversalContextManager;
  }

  /**
   * Shut down this connector: interrupt the background threads and wait for
   * them to terminate.
   */
  @Override
  public void shutdown() {
    LOG.info("Shutting down connector");
    deactivate();
    LOG.info("Connector shutdown complete");
  }

  /**
   * Delete the snapshot and persistent storage for this connector.
   * Invokes shutdown() first.
   */
  @Override
  public void delete() {
    LOG.info("Deleting connector");
    shutdown();
    repositoryMonitorManager.clean();
    LOG.info("Connector deletion complete");
  }

  @Override
  public Session login() {
    return this;
  }

  @Override
  public AuthenticationManager getAuthenticationManager() {
    return null;
  }

  @Override
  public AuthorizationManager getAuthorizationManager() {
    return authorizationManager;
  }

  private synchronized void deactivate() {
    if (null != traversalManager) {
      traversalManager.deactivate();
      traversalManager = null;
    }
  }

  /**
   * Creates and returns a {@link TraversalManager} which can start and
   * resume traversals. Getting a traversal manager invalidates
   * previously acquired TraversalManagers.  This operation
   * has the expense of stopping current crawls, forcing them
   * to be restarted (at initial or resume points) when further docs
   * are requested via returned TraversalManager.
   *
   * @return a Diffing Connector {@link TraversalManager}
   */
  @Override
  public synchronized TraversalManager getTraversalManager() {
    if (traversalManager != null) {
      deactivate();
    }
    traversalManager = new DiffingConnectorTraversalManager(
        repositoryMonitorManager, traversalContextManager);
    return traversalManager;
  }
}
