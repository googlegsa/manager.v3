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
import com.google.enterprise.connector.persist.GenerationalStateStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.scheduler.BatchResultRecorder;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.traversal.BatchResult;
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
  private final GenerationalStateStore stateStore;
  private final ConnectorConfigStore configStore;
  private final ConnectorScheduleStore scheduleStore;

  private final StoreContext storeContext;
  private String typeName;

  MockConnectorCoordinator(String name, ConnectorInterfaces connectorInterfaces,
      GenerationalStateStore stateStore, ConnectorConfigStore configStore,
      ConnectorScheduleStore scheduleStore, StoreContext storeContext) {
    this.name = name;
    this.interfaces = connectorInterfaces;
    this.stateStore = stateStore;
    this.configStore = configStore;
    this.scheduleStore = scheduleStore;
    this.storeContext = storeContext;
  }

   public void cancelBatch(Object batchKey) {
     // TODO(strellis): implement this
     throw new UnsupportedOperationException();
  }

  public void completeBatch(Object batchKey, String connectorSchedule,
      BatchResult batchResult) {
    // TODO(strellis): implement this
    throw new UnsupportedOperationException();
  }

  public boolean exists() {
    return true;
  }

  public AuthenticationManager getAuthenticationManager()
      throws InstantiatorException {
    return interfaces.getAuthenticationManager();
  }

  public AuthorizationManager getAuthorizationManager()
      throws InstantiatorException {
    return interfaces.getAuthorizationManager();
  }

   public ConfigureResponse getConfigForm(Locale locale) {
    throw new UnsupportedOperationException();
  }

   public Map<String, String> getConnectorConfig() {
    Properties props = configStore.getConnectorConfiguration(storeContext);
    if (props == null) {
      return new HashMap<String, String>();
    } else {
      return PropertiesUtils.toMap(props);
    }
  }

  public String getConnectorSchedule() {
    return scheduleStore.getConnectorSchedule(storeContext);
  }

   public String getConnectorTypeName() {
    return typeName;
  }

  public Traverser getTraverser() throws InstantiatorException {
    return interfaces.getTraverser();
  }

  public void removeConnector() {
    stateStore.removeConnectorState(storeContext);
    scheduleStore.removeConnectorSchedule(storeContext);
    configStore.removeConnectorConfiguration(storeContext);
  }

  public void restartConnectorTraversal() {
    stateStore.removeConnectorState(storeContext);
  }

  public ConfigureResponse setConnectorConfig(TypeInfo newTypeInfo,
      Map<String, String> configMap, Locale locale, boolean update) {
    configStore.storeConnectorConfiguration(
        storeContext, PropertiesUtils.fromMap(configMap));
    return null;
  }

  public void setConnectorSchedule(String connectorSchedule) {
    scheduleStore.storeConnectorSchedule(
        storeContext, connectorSchedule);
  }

  public void shutdown() {
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

  public void startBatch(BatchResultRecorder resultRecorder, int batchHint) {
    throw new UnsupportedOperationException();
  }

  public String getTraversalState() {
    return stateStore.getConnectorState(storeContext);
  }

  public void storeTraversalState(String state) {
    stateStore.storeConnectorState(storeContext, state);
  }
}
