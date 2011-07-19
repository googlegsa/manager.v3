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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectorCoordinatorMap {
  // State that is filled in by Spring.
  private ConnectorCoordinatorFactory connectorCoordinatorFactory;

  private final ConcurrentMap<String, ConnectorCoordinator> coordinatorMap;

  /** Constructor. */
  public ConnectorCoordinatorMap() {
    coordinatorMap = new ConcurrentHashMap<String, ConnectorCoordinator>();
  }

  public void setConnectorCoordinatorFactory(
      ConnectorCoordinatorFactory factory) {
    connectorCoordinatorFactory = factory;
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
      ConnectorCoordinator ci =
          connectorCoordinatorFactory.newConnectorCoordinator(connectorName);
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
