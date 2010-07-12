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

import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectorCoordinatorMap {
  // State that is filled in by Spring.
  private PusherFactory pusherFactory;
  private LoadManagerFactory loadManagerFactory;
  private ThreadPool threadPool;
  private ChangeDetector changeDetector;

  private final ConcurrentMap<String, ConnectorCoordinator> coordinatorMap;

  /** Constructor. */
  public ConnectorCoordinatorMap() {
    coordinatorMap = new ConcurrentHashMap<String, ConnectorCoordinator>();
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
   * Sets the {@link ChangeDetector} used for invoking the local
   * {@link ChangeHandler} for connector configuration, schedule, and
   * checkpoint changes that are initiated by this Manager instance.
   *
   * @param changeDetector a {@link ChangeDetector} implementation.
   */
  /* It should be noted that there is a circular dependency here.
   * ConnectorCoordinatorMap -> ChangeDetector -> ChangeListener ->
   * ConnectorCoordinatorMap.  ConnectorCoordinatorMap supplies the
   * ChangeDetector to the ConnectorCoordinatorImpl where it is used
   * to trigger calls to its ChangeHandler methods.
   * ChangeListenerImpl needs the ConnectorCoordinatorMap to locate
   * the appropriate ChangeHandler for a named connector.
   */
  public void setChangeDetector(ChangeDetector changeDetector) {
    this.changeDetector = changeDetector;
  }

  public void shutdown() {
    for (ConnectorCoordinator cc : coordinatorMap.values()) {
      cc.shutdown();
    }
  }

  public ChangeHandler getChangeHandler(String connectorName) {
    return (ChangeHandler) getOrAdd(connectorName);
  }

  public ConnectorCoordinator getOrAdd(String connectorName) {
    ConnectorCoordinator connectorCoordinator =
        coordinatorMap.get(connectorName);
    if (connectorCoordinator == null) {
      ConnectorCoordinator ci = new ConnectorCoordinatorImpl(connectorName,
          pusherFactory, loadManagerFactory, threadPool, changeDetector);
      ConnectorCoordinator existing =
          coordinatorMap.putIfAbsent(connectorName, ci);
      connectorCoordinator = (existing == null) ? ci : existing;
    }
    return connectorCoordinator;
  }

  public ConnectorCoordinator get(String connectorName) {
    return coordinatorMap.get(connectorName);
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
}
