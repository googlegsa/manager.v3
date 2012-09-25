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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.scheduler.HostLoadManager;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchResultRecorder;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.BatchTimeout;
import com.google.enterprise.connector.traversal.TraversalStateStore;
import com.google.enterprise.connector.traversal.Traverser;
import com.google.enterprise.connector.util.SystemClock;

import java.util.Locale;
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
  private final HostLoadManager hostLoadManager;

  private final TraversalStateStore stateStore;
  private final PersistentStore persistentStore;

  private final StoreContext storeContext;
  private final ThreadPool threadPool;
  private String typeName;

  // Batch context
  TaskHandle taskHandle;

  MockConnectorCoordinator(String name,
      ConnectorInterfaces connectorInterfaces, Traverser traverser,
      PersistentStore persistentStore, StoreContext storeContext,
      ThreadPool threadPool) {
    this.name = name;
    this.interfaces = connectorInterfaces;
    this.traverser = traverser;
    this.hostLoadManager = new HostLoadManager(null, null, new SystemClock());
    this.stateStore =
        new MockTraversalStateStore(persistentStore, storeContext);
    this.persistentStore = persistentStore;
    this.storeContext = storeContext;
    this.threadPool = threadPool;
  }

   private void cancelBatch() {
     throw new UnsupportedOperationException();
  }

  public boolean exists() {
    return true;
  }

  public String getConnectorName() {
    return name;
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

   public synchronized Configuration getConnectorConfiguration() {
    return persistentStore.getConnectorConfiguration(storeContext);
  }

  public synchronized Schedule getConnectorSchedule() {
    return persistentStore.getConnectorSchedule(storeContext);
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
    persistentStore.removeConnectorSchedule(storeContext);
    persistentStore.removeConnectorConfiguration(storeContext);
  }

  public synchronized void restartConnectorTraversal() {
    cancelBatch();
    stateStore.storeTraversalState(null);
  }

  public ConfigureResponse setConnectorConfiguration(TypeInfo newTypeInfo,
      Configuration configuration, Locale locale, boolean update) {
    persistentStore.storeConnectorConfiguration(storeContext, configuration);
    return null;
  }

  public void setGDataConfig() {
    // Do nothing.
  }

  public synchronized void setConnectorSchedule(Schedule schedule) {
    persistentStore.storeConnectorSchedule(storeContext, schedule);
    if (schedule != null) {
      hostLoadManager.setLoad(schedule.getLoad());
    }
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

  public synchronized boolean startBatch() {

    if (taskHandle != null && !taskHandle.isDone()) {
      return false;
    }
    taskHandle = null;

    BatchSize batchSize = hostLoadManager.determineBatchSize();
    if (batchSize.getMaximum() == 0) {
      return false;
    }

    MockBatchCoordinator batchResultProcessor =
        new MockBatchCoordinator(stateStore, hostLoadManager);
    TimedCancelable batch =
        new CancelableBatch(traverser, name, batchResultProcessor,
            batchResultProcessor, batchSize);
    taskHandle = threadPool.submit(batch);
    return true;
  }

  public String getConnectorState() {
    return stateStore.getTraversalState();
  }

  public synchronized void setConnectorState(String state) {
    stateStore.storeTraversalState(state);
  }

  private class MockBatchCoordinator implements TraversalStateStore,
      BatchResultRecorder, BatchTimeout {
    private final TraversalStateStore traversalStateStore;
    private final BatchResultRecorder batchResultRecorder;

    MockBatchCoordinator(TraversalStateStore traversalStateStore,
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
