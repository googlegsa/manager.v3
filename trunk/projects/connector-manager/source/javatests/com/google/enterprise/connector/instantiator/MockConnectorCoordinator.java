// Copyright 2009 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.persist.ConnectorConfigStore;
import com.google.enterprise.connector.persist.ConnectorScheduleStore;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.TraversalStateStore;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ConnectorCoordinator} for use with {@link MockInstantiator}.
 */
class MockConnectorCoordinator implements ConnectorCoordinator {

  private static final Logger LOGGER =
    Logger.getLogger(MockConnectorCoordinator.class.getName());

  private final String name;
  private final ConnectorInterfaces interfaces;
  private final Traverser traverser;

  private final TraversalStateStore stateStore;
  private final ConnectorConfigStore configStore;
  private final ConnectorScheduleStore scheduleStore;

  private final StoreContext storeContext;
  private final ThreadPool threadPool;
  private String typeName;

  // Batch context
  TaskHandle taskHandle;

  MockConnectorCoordinator(String name,
      ConnectorInterfaces connectorInterfaces, Traverser traverser,
      ConnectorStateStore stateStore, ConnectorConfigStore configStore,
      ConnectorScheduleStore scheduleStore, StoreContext storeContext,
      ThreadPool threadPool) {
    this.name = name;
    this.interfaces = connectorInterfaces;
    this.traverser = traverser;
    this.stateStore = new MockTraversalStateStore(stateStore, storeContext);
    this.configStore = configStore;
    this.scheduleStore = scheduleStore;
    this.storeContext = storeContext;
    this.threadPool = threadPool;
  }

   private void cancelBatch() {
     throw new UnsupportedOperationException();
  }

  public boolean exists() {
    return true;
  }

  public synchronized AuthenticationManager getAuthenticationManager()
      throws InstantiatorException {
    return interfaces.getAuthenticationManager();
  }

  public synchronized AuthorizationManager getAuthorizationManager()
      throws InstantiatorException {
    return interfaces.getAuthorizationManager();
  }

   public synchronized ConfigureResponse getConfigForm(Locale locale) {
    throw new UnsupportedOperationException();
  }

   public synchronized Map<String, String> getConnectorConfig() {
    Properties props = configStore.getConnectorConfiguration(storeContext);
    if (props == null) {
      return new HashMap<String, String>();
    } else {
      return PropertiesUtils.toMap(props);
    }
  }

  public synchronized String getConnectorSchedule() {
    return scheduleStore.getConnectorSchedule(storeContext);
  }

  public synchronized String getConnectorTypeName() {
    return typeName;
  }

  public TraversalManager  getTraversalManager() throws InstantiatorException {
    return interfaces.getTraversalManager();
  }

  public synchronized void removeConnector() {
    cancelBatch();
    stateStore.storeTraversalState(null);
    scheduleStore.removeConnectorSchedule(storeContext);
    configStore.removeConnectorConfiguration(storeContext);
  }

  public synchronized void restartConnectorTraversal() {
    cancelBatch();
    stateStore.storeTraversalState(null);
  }

  public ConfigureResponse setConnectorConfig(TypeInfo newTypeInfo,
      Map<String, String> configMap, Locale locale, boolean update) {
    configStore.storeConnectorConfiguration(
        storeContext, PropertiesUtils.fromMap(configMap));
    return null;
  }

  public synchronized void setConnectorSchedule(String connectorSchedule) {
    scheduleStore.storeConnectorSchedule(
        storeContext, connectorSchedule);
  }

  public synchronized void shutdown() {
    Connector connector = interfaces.getConnector();
    if (connector != null && (connector instanceof ConnectorShutdownAware)) {
      try {
        ((ConnectorShutdownAware)connector).shutdown();
      } catch (RepositoryException e) {
        LOGGER.log(Level.WARNING, "Problem shutting down connector "
                   + name, e);
      }
    }
  }

  public synchronized boolean startBatch(BatchResultRecorder resultRecorder,
      int batchHint) {
    if (taskHandle != null && !taskHandle.isDone()) {
      return false;
    }
    taskHandle = null;
    BatchCoordinator batchResultProcessor =
        new BatchCoordinator(stateStore, resultRecorder);
    TimedCancelable batch =
        new CancelableBatch(traverser, name, batchResultProcessor,
            batchResultProcessor, batchHint);
    taskHandle = threadPool.submit(batch);
    return true;
  }

  public String getTraversalState() {
    return stateStore.getTraversalState();
  }

  public synchronized void storeTraversalState(String state) {
    stateStore.storeTraversalState(state);
  }

  private class BatchCoordinator implements TraversalStateStore,
      BatchResultRecorder, BatchTimeout {
    private final TraversalStateStore traversalStateStore;
    private final BatchResultRecorder batchResultRecorder;

    BatchCoordinator(TraversalStateStore traversalStateStore,
        BatchResultRecorder batchResultRecorder) {
      this.traversalStateStore = traversalStateStore;
      this.batchResultRecorder = batchResultRecorder;
    }

    public String getTraversalState() {
      synchronized (MockConnectorCoordinator.this) {
          return traversalStateStore.getTraversalState();
      }
    }

    public void storeTraversalState(String state) {
      synchronized (MockConnectorCoordinator.this) {
          traversalStateStore.storeTraversalState(state);
      }
    }

    public void recordResult(BatchResult result) {
      synchronized (MockConnectorCoordinator.this) {
        batchResultRecorder.recordResult(result);
      }
    }

    public void timeout() {
      synchronized (MockConnectorCoordinator.this) {
          throw new UnsupportedOperationException();
      }
    }
  }
}
