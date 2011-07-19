// Copyright 2010 Google Inc.
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

import com.google.enterprise.connector.database.ConnectorPersistentStoreFactory;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.util.Clock;

/**
 * Factory for constructing ConnectorCoordinatorImpl instances.
 */
public class ConnectorCoordinatorImplFactory
    implements ConnectorCoordinatorFactory {

  // State that is filled in by Spring.
  private PusherFactory pusherFactory;
  private LoadManagerFactory loadManagerFactory;
  private ConnectorPersistentStoreFactory connectorPersistentStoreFactory;
  private ThreadPool threadPool;
  private ChangeDetector changeDetector;
  private Clock clock;

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
   * Sets the {@link ConnectorPersistentStoreFactory} used to create instances
   * of {@link com.google.enterprise.connector.spi.ConnectorPersistentStore}
   * for providing database access to Connectors that request it.
   *
   * @param connectorPersistentStoreFactory a
   *        {@link ConnectorPersistentStoreFactory} implementation.
   */
  public void setConnectorPersistentStoreFactory(
        ConnectorPersistentStoreFactory connectorPersistentStoreFactory) {
    this.connectorPersistentStoreFactory = connectorPersistentStoreFactory;
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
   * Sets the {@link Clock} used for timing traversals.
   *
   * @param clock a {@link Clock} implementation.
   */
  public void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Sets the {@link ChangeDetector} used for invoking the local
   * {@link ChangeHandler} for connector configuration, schedule, and
   * checkpoint changes that are initiated by this Manager instance.
   *
   * @param changeDetector a {@link ChangeDetector} implementation.
   */
  /* It should be noted that there is a circular dependency here.
   * ConnectorCoordinatorMap -> ConnectorCoordinatorFactory ->
   * ChangeDetector -> ChangeListener -> ConnectorCoordinatorMap.
   * ConnectorCoordinatorMap uses this Factory,supplies the
   * ChangeDetector to the ConnectorCoordinatorImpl where it is used
   * to trigger calls to its ChangeHandler methods.
   * ChangeListenerImpl needs the ConnectorCoordinatorMap to locate
   * the appropriate ChangeHandler for a named connector.
   */
  public void setChangeDetector(ChangeDetector changeDetector) {
    this.changeDetector = changeDetector;
  }

  /**
   * Factory method constructs a new {@link ConnectorCoordinator}
   * for the named {@link Connector} instance.
   *
   * @param connectorName the Connector instance name.
   * @return a new ConnectorCoordinator.
   */
  /* @Override */
  public ConnectorCoordinator newConnectorCoordinator(String connectorName) {
    return new ConnectorCoordinatorImpl(connectorName,
        pusherFactory, loadManagerFactory, connectorPersistentStoreFactory,
        threadPool, changeDetector, clock);
  }
}
