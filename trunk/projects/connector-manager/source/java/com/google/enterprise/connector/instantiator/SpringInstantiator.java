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

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.common.ScheduledTimer;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.Retriever;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Instantiator} that supports Spring-based connector instantiation and
 * persistent storage of connector configuration, schedule and traversal state.
 */
public class SpringInstantiator implements Instantiator {
  private static final Logger LOGGER =
      Logger.getLogger(SpringInstantiator.class.getName());

  // State that is filled in by setters from Spring.
  private ConnectorCoordinatorMap coordinatorMap;
  private ThreadPool threadPool;
  private TypeMap typeMap;
  private ChangeDetectorTask changeDetectorTask;

  private final ScheduledTimer timer = new ScheduledTimer();

  /**
   * Normal constructor.
   */
  public SpringInstantiator() {
    // NOTE: we can't call init() here because then there would be a
    // circular dependency on the Context, which hasn't been constructed yet
  }

  /**
   * Sets the {@link ConnectorCoordinatorMap} instance used to manage the
   * instances of {@link ConnectorCoordinator}.
   *
   * @param coordinatorMap a {@link ConnectorCoordinatorMap} instance
   */
  public void setConnectorCoordinatorMap(
      ConnectorCoordinatorMap coordinatorMap) {
    this.coordinatorMap = coordinatorMap;
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
   * Sets the {@link TypeMap} of installed {@link ConnectorType}s.
   *
   * @param typeMap a {@link TypeMap}.
   */
  public void setTypeMap(TypeMap typeMap) {
    this.typeMap = typeMap;
  }

  /**
   * Sets the {@link ChangeDetectorTask}.
   *
   * @param changeDetectorTask a {@code ChangeDetector} task
   */
  public void setChangeDetectorTask(ChangeDetectorTask changeDetectorTask) {
    this.changeDetectorTask = changeDetectorTask;
  }

  /**
   * Initializes the Context, post bean construction.
   */
  public synchronized void init() {
    LOGGER.info("Initializing instantiator");
    // typeMap must be initialized before the ChangeDetector task is run.
    typeMap.init();

    // Run the ChangeDetector periodically to update the internal
    // state. The initial execution will create connector instances
    // from the persistent store.
    timer.schedule(changeDetectorTask);
  }

  /**
   * Shutdown all connector instances.
   */
  @Override
  public void shutdown(boolean interrupt, long timeoutMillis) {
    timer.cancel();
    coordinatorMap.shutdown();
    try {
      if (threadPool != null) {
        threadPool.shutdown(interrupt, timeoutMillis);
      }
    } catch (InterruptedException ie) {
      LOGGER.log(Level.SEVERE, "TraversalScheduler shutdown interrupted: ", ie);
    }
  }

  @Override
  public void removeConnector(String connectorName) {
    LOGGER.info("Dropping connector: " + connectorName);
    ConnectorCoordinator existing = coordinatorMap.get(connectorName);
    if (existing != null) {
      existing.removeConnector();
    }
  }

  @Override
  public AuthenticationManager getAuthenticationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthenticationManager();
  }

  @Override
  public void startBatch(String connectorName)
      throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).startBatch();
  }

  @VisibleForTesting
  ConnectorCoordinator getConnectorCoordinator(String connectorName)
      throws ConnectorNotFoundException {
    ConnectorCoordinator connectorCoordinator =
        coordinatorMap.get(connectorName);
    if (connectorCoordinator == null) {
      // If we are clustered, perhaps another CM created a new connector
      // instance for this connector and we haven't detected it yet.
      changeDetectorTask.run();
      connectorCoordinator = coordinatorMap.get(connectorName);
      if (connectorCoordinator == null) {
        throw new ConnectorNotFoundException();
      }
    }
    return connectorCoordinator;
  }

  private ConnectorCoordinator getOrAddConnectorCoordinator(
      String connectorName) {
    if (typeMap == null) {
      throw new IllegalStateException(
          "Init must be called before accessing connectors.");
    }
    ConnectorCoordinator connectorCoordinator =
        coordinatorMap.get(connectorName);
    if (connectorCoordinator == null) {
      // If we are clustered, perhaps another CM created a new connector
      // instance for this connector and we haven't detected it yet.
      changeDetectorTask.run();
      connectorCoordinator = coordinatorMap.getOrAdd(connectorName);
    }
    return connectorCoordinator;
  }

  @Override
  public AuthorizationManager getAuthorizationManager(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getAuthorizationManager();
  }

  @Override
  public Retriever getRetriever(String connectorName)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getRetriever();
  }

  @Override
  public ConfigureResponse getConfigFormForConnector(String connectorName,
      String connectorTypeName, Locale locale)
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorCoordinator(connectorName).getConfigForm(locale);
  }

  @Override
  public String getConnectorInstancePrototype(String connectorTypeName)
      throws ConnectorTypeNotFoundException {
    Resource resource = typeMap.getTypeInfo(connectorTypeName)
                        .getConnectorInstancePrototype();
    try {
      return StringUtils.streamToStringAndThrow(resource.getInputStream());
    } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "Failed to extract connectorInstance.xml "
            + " for connector " + connectorTypeName, ioe);
    }
    return null;
  }

  @Override
  public synchronized ConnectorType getConnectorType(String typeName)
      throws ConnectorTypeNotFoundException {
    return typeMap.getTypeInfo(typeName).getConnectorType();
  }

  @Override
  public synchronized Set<String> getConnectorTypeNames() {
    return typeMap.getConnectorTypeNames();
  }

  @Override
  public void restartConnectorTraversal(String connectorName)
      throws ConnectorNotFoundException {
    LOGGER.info("Restarting traversal for Connector: " + connectorName);
    getConnectorCoordinator(connectorName).restartConnectorTraversal();
  }

  @Override
  public Set<String> getConnectorNames() {
    return coordinatorMap.getConnectorNames();
  }

  @Override
  public String getConnectorTypeName(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorTypeName();
  }

  @Override
  public ConfigureResponse setConnectorConfiguration(String connectorName,
      Configuration configuration, Locale locale, boolean update)
      throws ConnectorNotFoundException, ConnectorExistsException,
      InstantiatorException {
    LOGGER.info("Configuring connector: " + connectorName);
    TypeInfo typeInfo;
    try {
      typeInfo = typeMap.getTypeInfo(configuration.getTypeName());
    } catch (ConnectorTypeNotFoundException ctnf) {
      throw new ConnectorNotFoundException("Incorrect type", ctnf);
    }
    ConnectorCoordinator ci = getOrAddConnectorCoordinator(connectorName);
    return ci.setConnectorConfiguration(typeInfo, configuration, locale, update);
  }

  @Override
  public Configuration getConnectorConfiguration(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorConfiguration();
  }

  @Override
  public void setConnectorSchedule(String connectorName, Schedule schedule)
      throws ConnectorNotFoundException {
    getConnectorCoordinator(connectorName).setConnectorSchedule(schedule);
  }

  @Override
  public Schedule getConnectorSchedule(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getConnectorSchedule();
  }

  @Override
  public DocumentFilterFactory getDocumentFilterFactory(String connectorName)
      throws ConnectorNotFoundException {
    return getConnectorCoordinator(connectorName).getDocumentFilterFactory();
  }

  @Override
  public void setGDataConfig() {
    for (String name : getConnectorNames()) {
      try {
        getConnectorCoordinator(name).setGDataConfig();
      } catch (ConnectorNotFoundException cnfe) {
        // Shouldn't happen, but if it does, skip it.
      } catch (InstantiatorException ie) {
        LOGGER.log(Level.WARNING, "", ie);
      }
    }
  }
}
