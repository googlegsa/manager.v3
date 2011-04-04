// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.instantiator;

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.database.ConnectorPersistentStoreFactory;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.LoadManager;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.scheduler.ScheduleTimeInterval;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.ConnectorPersistentStoreAware;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchResultRecorder;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
import com.google.enterprise.connector.traversal.Traverser;
import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.database.DocumentStore;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConnectorCoordinator that supports Spring based connector instantiation and
 * persistent storage of connector configuration, schedule and traversal state.
 */
class ConnectorCoordinatorImpl implements
     ConnectorCoordinator, ChangeHandler, BatchResultRecorder {

  private static final Logger LOGGER =
      Logger.getLogger(ConnectorCoordinatorImpl.class.getName());

  /** An empty, disabled Schedule. */
  private static final Schedule EMPTY_SCHEDULE = new Schedule();

  /**
   * Invariant context.
   */
  private final String name;
  private final PusherFactory pusherFactory;
  private final ConnectorPersistentStoreFactory connectorPersistentStoreFactory;
  private final ThreadPool threadPool;
  private final ChangeDetector changeDetector;
  private final Clock clock;

  /**
   * Context set when an instance is created or configured and cleared when the
   * instance is removed. It is an invariant that either both of these are null
   * or neither is.
   */
  private TypeInfo typeInfo;
  private InstanceInfo instanceInfo;

  /**
   * Context that is filled in on first use. Requires instanceInfo.
   */
  private ConnectorInterfaces interfaces;

  /**
   * LoadManager controls throughput to avoid overtaxing the Repository
   * or the GSA.
   */
  private final LoadManager loadManager;

  /**
   * The current traversal Schedule.
   */
  private Schedule traversalSchedule;

  /**
   * The finish time for delay of next traversal.  Used to postpone
   * starting another traversal for a short period of time, as dictated
   * by a {@link TraversalDelayPolicy}.
   */
  private long traversalDelayEnd;

  /**
   * Context set when a batch is run. This must be cleared and any
   * running batch must be canceled when interfaces is reset.
   */
  private TaskHandle taskHandle;
  Object currentBatchKey;

  /**
   * DocumentStore for the Pusher.
   */
  private DocumentStore documentStore;

  /**
   * Constructs a ConnectorCoordinator for the named {@link Connector}.
   * The {@code Connector} may not yet have a concrete instance.
   *
   * @param name The name of the Connector.
   * @param pusherFactory creates instances of
   *        {@link com.google.enterprise.connector.pusher.Pusher Pusher}
   *        for pushing documents to the GSA.
   * @param loadManagerFactory  creates instances of
   *        {@link LoadManager} for controlling the feed rate.
   * @param connectorPersistentStoreFactory creates instances of
   *        {@link ConnectorPersistentStore} for Connectors that request
   *        database access.
   * @param threadPool the {@link ThreadPool} for running traversals.
   * @param changeDetector used to invoke the ChangeHandlers for changes
   *        originiting within this Manager instance (or from the Servlets).
   */
  ConnectorCoordinatorImpl(String name, PusherFactory pusherFactory,
      LoadManagerFactory loadManagerFactory,
      ConnectorPersistentStoreFactory connectorPersistentStoreFactory,
      ThreadPool threadPool, ChangeDetector changeDetector, Clock clock) {
    this.name = name;
    this.threadPool = threadPool;
    this.clock = clock;
    this.changeDetector = changeDetector;
    this.pusherFactory = pusherFactory;
    this.loadManager = loadManagerFactory.newLoadManager(name);
    this.connectorPersistentStoreFactory = connectorPersistentStoreFactory;
    this.documentStore = null;
  }

  /**
   * Returns the name of this {@link Connector}.
   *
   * @return The name of this Connector.
   */
  /* @Override */
  public String getConnectorName() {
    return name;
  }

  /**
   * Returns {@code true} if an instance of this {@link Connector} exists.
   */
  /* @Override */
  public synchronized boolean exists() {
    return (instanceInfo != null);
  }

  /**
   * Removes this {@link Connector} instance.  Halts traversals,
   * removes the Connector instance from the known connectors,
   * and removes the Connector's on-disk representation.
   */
  /* @Override */
  public void removeConnector() {
    synchronized(this) {
      resetBatch();
      instanceInfo.removeConnector();
    }
    // This must not be called while holding the lock.
    changeDetector.detect();
  }

  /**
   * Removes this {@link Connector} instance.  Halts traversals,
   * removes the Connector instance from the known connectors,
   * and removes the Connector's on-disk representation.
   */
  /* @Override */
  public synchronized void connectorRemoved() {
    LOGGER.info("Dropping connector: " + name);
    try {
      resetBatch();
      if (instanceInfo != null) {
        File connectorDir = instanceInfo.getConnectorDir();
        shutdownConnector(true);
        removeConnectorDirectory(connectorDir);
      }

      // Discard all content from the LocalDocumentStore for this connector.
      if (documentStore != null) {
        documentStore.delete();
      }
    } finally {
      instanceInfo = null;
      typeInfo = null;
      traversalSchedule = null;
      traversalDelayEnd = 0;
    }
  }

  /**
   * Returns the {@link AuthenticationManager} for the {@link Connector}
   * instance.
   *
   * @return an AuthenticationManager
   * @throws InstantiatorException
   */
  /* @Override */
  public synchronized AuthenticationManager getAuthenticationManager()
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorInterfaces().getAuthenticationManager();
  }

  /**
   * Returns the {@link AuthorizationManager} for the {@link Connector}
   * instance.
   *
   * @return an AuthorizationManager
   * @throws InstantiatorException
   */
  /* @Override */
  public synchronized AuthorizationManager getAuthorizationManager()
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorInterfaces().getAuthorizationManager();
  }

  /**
   * Returns the {@link TraversalManager} for the {@link Connector}
   * instance.
   *
   * @return a TraversalManager
   * @throws InstantiatorException
   */
  /* @Override */
  public synchronized TraversalManager getTraversalManager()
      throws ConnectorNotFoundException, InstantiatorException {
    return getConnectorInterfaces().getTraversalManager();
  }

  /**
   * Get populated configuration form snippet for the {@link Connector}
   * instance.
   *
   * @param locale A java.util.Locale which the implementation may use to
   *        produce appropriate descriptions and messages
   * @return a ConfigureResponse object. The form must be prepopulated with the
   *         supplied data in the map.
   * @see ConnectorType#getPopulatedConfigForm(Map, Locale)
   */
  /* @Override */
  public synchronized ConfigureResponse getConfigForm(Locale locale)
      throws ConnectorNotFoundException, InstantiatorException {
    Configuration config = getInstanceInfo().getConnectorConfiguration();
    Map<String, String> configMap = (config == null) ? null : config.getMap();
    ConnectorType connectorType = typeInfo.getConnectorType();
    try {
      return connectorType.getPopulatedConfigForm(configMap, locale);
    } catch (Exception e) {
      throw new InstantiatorException("Failed to get configuration form", e);
    }
  }

  /**
   * Retraverses the {@link Connector}'s content from scratch.
   * Halts any traversal in progress and removes any saved traversal state,
   * forcing the Connector to retraverse the Repository from its start.
   */
  /* @Override */
  public void restartConnectorTraversal() throws ConnectorNotFoundException {
    // To avoid deadlock, this method calls InstanceInfo's getters and setters,
    // rather than the local ones.
    synchronized(this) {
      resetBatch();                               // Halt any traversal.
      getInstanceInfo().setConnectorState(null);  // Discard the checkpoint.

      // If Schedule was 'run-once', re-enable it to run again.  But watch out -
      // empty disabled Schedules could look a bit like a run-once Schedule.
      Schedule schedule = getInstanceInfo().getConnectorSchedule();
      if (schedule != null && schedule.isDisabled() &&
            schedule.getRetryDelayMillis() == -1 &&
            !schedule.getTimeIntervals().isEmpty()) {
          schedule.setDisabled(false);
          getInstanceInfo().setConnectorSchedule(schedule);
      }
    }
    // This must not be called while holding the lock.
    changeDetector.detect();
  }

  /**
   * Returns a traversal {@link Schedule} for the {@link Connector} instance.
   */
  private synchronized Schedule getSchedule() {
    if (traversalSchedule == null) {
      try {
        traversalSchedule = getInstanceInfo().getConnectorSchedule();
        if (traversalSchedule == null) {
          return EMPTY_SCHEDULE;
        }
      } catch (ConnectorNotFoundException e) {
        return EMPTY_SCHEDULE;
      }
    }
    return traversalSchedule;
  }

  /**
   * Sets the traversal {@link Schedule} for the {@link Connector}.
   *
   * @param connectorSchedule Schedule to store or null to unset any existing
   *        Schedule.
   * @throws ConnectorNotFoundException if the connector is not found
   */
  /* @Override */
  public void setConnectorSchedule(Schedule connectorSchedule)
      throws ConnectorNotFoundException {
    synchronized(this) {
      // Persistently store the new schedule.
      getInstanceInfo().setConnectorSchedule(connectorSchedule);
    }
    // This must not be called while holding the lock.
    changeDetector.detect();
  }

  /**
   * Handles a change to the traversal {@link Schedule} for the
   * {@link Connector}.
   *
   * @param schedule new Connector Schedule
   */
  // TODO: What happens on create connector if setConfig servlet goes to
  // some other CM, then I get the setSchedule?
  /* @Override */
  public synchronized void connectorScheduleChanged(Schedule schedule) {
    // Refresh the cached Schedule.
    traversalSchedule = schedule;

    // Update the LoadManager with the new load.
    loadManager.setLoad(
        (schedule == null) ? EMPTY_SCHEDULE.getLoad() : schedule.getLoad());

    // New Schedule may alter DelayPolicy.
    delayTraversal(TraversalDelayPolicy.IMMEDIATE);
  }

  /**
   * Fetches the traversal {@link Schedule} for the {@link Connector}.
   *
   * @return the Schedule, or null if there is no stored Schedule
   *         for this connector.
   * @throws ConnectorNotFoundException if the connector is not found
   */
  /* @Override */
  public synchronized Schedule getConnectorSchedule()
      throws ConnectorNotFoundException {
    // Fetch the Schedule and Update the cache while we're at it.
    traversalSchedule = getInstanceInfo().getConnectorSchedule();
    return traversalSchedule;
  }

  /**
   * Set the Connector's traversal state.
   *
   * @param state a String representation of the state to store.
   *        If null, any previous stored state is discarded.
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  /* @Override */
  public synchronized void setConnectorState(String state)
      throws ConnectorNotFoundException {
    getInstanceInfo().setConnectorState(state);
    // Must not call ChangeDetector, as this is called from a synchronized
    // block in BatchCoordinator.
  }

  /**
   * Handle a change to the Connector's traversal state.  The only change
   * that matters is a change from non-null to null.  This indicates that
   * the Repository should be retraversed from the beginning.
   *
   * @param checkpoint a String representation of the traversal state.
   */
  /* @Override */
  public void connectorCheckpointChanged(String checkpoint) {
    // TODO: actually detect transition from non-null to null?
    // If checkpoint has been nulled, then traverse the repository from scratch.
    if (checkpoint == null) {
      synchronized(this) {
        // Halt any traversal in progress.
        resetBatch();

        // Discard all content from the LocalDocumentStore for this connector.
        if (documentStore != null) {
          documentStore.delete();
        }

        // Kick off a restart immediately.
        delayTraversal(TraversalDelayPolicy.IMMEDIATE);
      }
      LOGGER.info("Restarting traversal from beginning for connector " + name);
    }
  }

  /**
   * Returns the Connector's traversal state.
   *
   * @return String representation of the stored state, or
   *         null if no state is stored.
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   */
  /* @Override */
  public synchronized String getConnectorState()
      throws ConnectorNotFoundException {
    return getInstanceInfo().getConnectorState();
  }

  /**
   * Returns the name of the {@link ConnectorType} for this {@link Connector}
   * instance.
   */
  /* @Override */
  public synchronized String getConnectorTypeName()
      throws ConnectorNotFoundException {
    return getInstanceInfo().getTypeInfo().getConnectorTypeName();
  }

  /**
   * Sets the {@link Configuration} for this {@link ConnectorCoordinator}.
   * If this {@link ConnectorCoordinator} supports persistence this will
   * persist the new Configuration.
   */
  /* @Override */
  public ConfigureResponse setConnectorConfiguration(TypeInfo newTypeInfo,
      Configuration configuration, Locale locale, boolean update)
      throws ConnectorNotFoundException, ConnectorExistsException,
      InstantiatorException {
    LOGGER.info("Configuring connector " + name);
    ConfigureResponse response = null;
    synchronized(this) {
      resetBatch();
      if (instanceInfo != null) {
        if (!update) {
          throw new ConnectorExistsException();
        }
        if (newTypeInfo.getConnectorTypeName().equals(
            typeInfo.getConnectorTypeName())) {
          File connectorDir = instanceInfo.getConnectorDir();
          response = resetConfig(connectorDir, typeInfo, configuration, locale);
        } else {
          // An existing connector is being given a new type - drop then add.
          removeConnector();
          response = createNewConnector(newTypeInfo, configuration, locale);
          if (response != null) {
            // TODO: We need to restore original Connector config. This is
            // necessary once we allow update a Connector with new ConnectorType.
            LOGGER.severe("Failed to update Connector configuration."
                + " Restoring original Connector configuration.");
          }
        }
      } else {
        if (update) {
          throw new ConnectorNotFoundException();
        }
        response = createNewConnector(newTypeInfo, configuration, locale);
      }
    }
    if (response == null) {
      // This must not be called while holding the lock.
      changeDetector.detect();
    }
    return response;
  }

  /* @Override */
  public synchronized Configuration getConnectorConfiguration()
      throws ConnectorNotFoundException {
    return getInstanceInfo().getConnectorConfiguration();
  }

  /**
   * Delay future traversals for a short period of time, as dictated by the
   * {@link TraversalDelayPolicy}.
   *
   * @param delayPolicy a TraversalDelayPolicy
   */
  @VisibleForTesting
  synchronized void delayTraversal(TraversalDelayPolicy delayPolicy) {
    switch (delayPolicy) {
      case IMMEDIATE:
        traversalDelayEnd = 0;  // No delay.
        break;

      case POLL:
        try {
          Schedule schedule = getSchedule();
          int retryDelayMillis = schedule.getRetryDelayMillis();
          if (retryDelayMillis == Schedule.POLLING_DISABLED) {
            if (!schedule.isDisabled()) {
              // We reached then end of the repository, but aren't allowed
              // to poll looking for new content to arrive.  Disable the
              // traversal schedule.
              traversalDelayEnd = 0;
              schedule.setDisabled(true);
              // To avoid deadlock, this method calls InstanceInfo's setter,
              // rather than the local one.
              traversalSchedule = schedule; // Update local cache.
              getInstanceInfo().setConnectorSchedule(schedule);
              LOGGER.info("Traversal complete. Automatically pausing "
                  + "traversal for connector " + name);
            }
          } else if (retryDelayMillis > 0) {
            traversalDelayEnd = clock.getTimeMillis() + retryDelayMillis;
          }
        } catch (ConnectorNotFoundException cnfe) {
          // Connector was deleted while processing the batch.  Don't take any
          // action at the moment, as we may be in the middle of a reconfig.
        }
        break;

      case ERROR:
        traversalDelayEnd =
            clock.getTimeMillis() + Traverser.ERROR_WAIT_MILLIS;
        break;
    }
  }

  /**
   * Returns {@code true} if it is OK to start a traversal,
   * {@code false} otherwise.
   */
  @VisibleForTesting
  synchronized boolean shouldRun() {
    // If we do not have a traversing instance, don't run.
    if (instanceInfo == null) {   // TODO: handle setSchedule before connectorAdded
      return false;
    }

    // Are we already running? If so, we shouldn't run again.
    if (taskHandle != null && !taskHandle.isDone()) {
      return false;
    }

    // Don't run if we have postponed traversals.
    if (clock.getTimeMillis() < traversalDelayEnd) {
      return false;
    }

    Schedule schedule = getSchedule();

    // Don't run if traversals are disabled.
    if (schedule.isDisabled()) {
      return false;
    }

    // Don't run if we have exceeded our configured host load.
    if (loadManager.shouldDelay()) {
      return false;
    }

    // OK to run if we are within one of the Schedule's traversal intervals.
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    for (ScheduleTimeInterval interval : schedule.getTimeIntervals()) {
      int startHour = interval.getStartTime().getHour();
      int endHour = interval.getEndTime().getHour();
      if (0 == endHour) {
        endHour = 24;
      }
      if (endHour < startHour) {
        // The traversal interval straddles midnight.
        if ((hour >= startHour) || (hour < endHour)) {
          return true;
        }
      } else {
        // The traversal interval falls wholly within the day.
        if ((hour >= startHour) && (hour < endHour)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Starts running a batch for this {@link ConnectorCoordinator} if a batch is
   * not already running.
   *
   * @return true if this call started a batch
   */
  /* @Override */
  public synchronized boolean startBatch() {
    if (!shouldRun()) {
      return false;
    }

    BatchSize batchSize = loadManager.determineBatchSize();
    if (batchSize.getMaximum() == 0) {
      return false;
    }

    try {
      TraversalManager traversalManager =
          getConnectorInterfaces().getTraversalManager();
      if (traversalManager == null) { // TODO: handle setSchedule before connectorAdded
        return false;
      }
      currentBatchKey = new Object();
      BatchCoordinator batchCoordinator = new BatchCoordinator(this);
      Traverser traverser = new QueryTraverser(pusherFactory,
          traversalManager, batchCoordinator, name,
          Context.getInstance().getTraversalContext(), clock, documentStore);
      TimedCancelable batch =  new CancelableBatch(traverser, name,
          batchCoordinator, batchCoordinator, batchSize);
      taskHandle = threadPool.submit(batch);
      return true;
    } catch (ConnectorNotFoundException cnfe) {
      LOGGER.log(Level.WARNING, "Connector not found - this is normal if you "
          + " recently reconfigured your connector instance: " + cnfe);
    } catch (InstantiatorException ie) {
      LOGGER.log(Level.WARNING,
          "Failed to perform connector content traversal.", ie);
      delayTraversal(TraversalDelayPolicy.ERROR);
    }
    return false;
  }

  /**
   * Records the supplied traversal batch results.  Updates the
   * {@link LoadManager} with number of documents traversed,
   * and implements the requested {@link TraversalDelayPolicy}.
   *
   * @param result a BatchResult
   */
  /* @Override */
  public synchronized void recordResult(BatchResult result) {
    loadManager.recordResult(result);
    delayTraversal(result.getDelayPolicy());
  }

  /**
   * Shuts down this {@link Connector} instance.  Halts any in-progress
   * traversals, instructs the Connector that it is being shut down,
   * and discards the Connector instance.  Any on-disk representation of
   * the connector remains.
   */
  /* @Override */
  public synchronized void shutdown() {
    resetBatch();
    shutdownConnector(false);
    instanceInfo = null;
  }

  /**
   * Halts any in-progess traversals for this {@link Connector} instance.
   * Some or all of the information collected during the current traversal
   * may be discarded.
   */
  synchronized void resetBatch() {
    if (taskHandle != null) {
      taskHandle.cancel();
    }
    taskHandle = null;
    currentBatchKey = null;
    interfaces = null;
  }

  /**
   * Informs the Connector instance that it will be shut down
   * and possibly deleted.
   *
   * @param delete {@code true} if the {@code Connector} will be deleted.
   */
  private void shutdownConnector(boolean delete) {
    if (instanceInfo != null
        && instanceInfo.getConnector() instanceof ConnectorShutdownAware) {
      ConnectorShutdownAware csa =
          (ConnectorShutdownAware)(instanceInfo.getConnector());
      try {
        LOGGER.fine("Shutting down connector " + name);
        csa.shutdown();
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Problem shutting down connector " + name
            + " during configuration update.", e);
      }

      if (delete) {
        try {
          LOGGER.fine("Removing connector " + name);
          csa.delete();
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Failed to remove connector " + name, e);
        }
      }
    }
  }

  /**
   * Returns the {@link InstanceInfo} representing the associated
   * {@link Connector} instance.
   *
   * @throws ConnectorNotFoundException if there is no associated Connector
   *         instance.
   */
  @VisibleForTesting
  InstanceInfo getInstanceInfo() throws ConnectorNotFoundException {
    verifyConnectorInstanceAvailable();
    return instanceInfo;
  }

  /**
   * Checks if this {@code ConnectorCoordinator} is associated
   * with an active {@link Connector} instance.
   *
   * @throws ConnectorNotFoundException if there is no associated Connector
   *         instance.
   */
  private void verifyConnectorInstanceAvailable()
      throws ConnectorNotFoundException {
    if (instanceInfo == null) {
      throw new ConnectorNotFoundException("Connector instance " + name
          + " not available.");
    }
  }

  /**
   * Returns a {@link ConnectorInterfaces} object that exposes the public
   * interfaces of the associated {@link Connector} instance.
   *
   * @throws ConnectorNotFoundException if there is no associated Connector
   *         instance.
   */
  private ConnectorInterfaces getConnectorInterfaces()
      throws ConnectorNotFoundException {
    if (interfaces == null) {
      InstanceInfo info = getInstanceInfo();
      interfaces = new ConnectorInterfaces(name, info.getConnector());
    }
    return interfaces;
  }

  private ConfigureResponse createNewConnector(TypeInfo newTypeInfo,
      Configuration config, Locale locale) throws InstantiatorException {
    if (newTypeInfo == null) {
      throw new IllegalStateException(
          "Create new connector with no type specified.");
    }
    if (instanceInfo != null) {
      throw new IllegalStateException(
          "Create new connector when one already exists.");
    }
    File connectorDir = makeConnectorDirectory(name, newTypeInfo);
    String configXml = (config.getXml() != null) ? config.getXml() :
        getConnectorInstancePrototype(name, newTypeInfo);
    Configuration configuration = new Configuration(
        newTypeInfo.getConnectorTypeName(), config.getMap(), configXml);
    try {
      ConfigureResponse result =
          resetConfig(connectorDir, newTypeInfo, configuration, locale);
      if (result != null && result.getMessage() != null) {
        removeConnectorDirectory(connectorDir);
      }
      return result;
    } catch (InstantiatorException ie) {
      removeConnectorDirectory(connectorDir);
      throw (ie);
    }
  }

  /* @Override */
  public void connectorAdded(TypeInfo newTypeInfo, Configuration configuration)
      throws InstantiatorException {
    if (instanceInfo != null) {
      throw new IllegalStateException(
          "Create new connector when one already exists.");
    }
    File connectorDir = new File(newTypeInfo.getConnectorTypeDir(), name);
    boolean didMakeConnectorDir = false;
    if (!connectorDir.exists()) {
      connectorDir = makeConnectorDirectory(name, newTypeInfo);
      didMakeConnectorDir = true;
    }
    try {
      connectorConfigurationChanged(newTypeInfo, configuration);
    } catch (InstantiatorException ie) {
      if (didMakeConnectorDir) {
        removeConnectorDirectory(connectorDir);
      }
      throw (ie);
    }
  }

  private ConfigureResponse resetConfig(File connectorDir,
      TypeInfo newTypeInfo, Configuration config, Locale locale)
      throws InstantiatorException {
    // Copy the configuration map, adding a couple of additional
    // context properties. validateConfig() may also alter this map.
    Map<String, String> newConfig = new HashMap<String, String>();
    newConfig.putAll(config.getMap());
    newConfig.put(PropertiesUtils.GOOGLE_CONNECTOR_NAME, name);
    newConfig.put(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR, connectorDir
        .getPath());
    newConfig.put(PropertiesUtils.GOOGLE_WORK_DIR, Context.getInstance()
        .getCommonDirPath());

    Configuration newConfiguration =
        new Configuration(config.getTypeName(), newConfig, config.getXml());

    // Validate the configuration.
    ConfigureResponse response =
        validateConfig(name, connectorDir, newTypeInfo, newConfiguration, locale);
    if (response != null) {
      // If validateConfig() returns a non-null response with an error message.
      // or populated config form, then consider it an invalid config that
      // needs to be corrected. Return the response so that the config form
      // may be redisplayed.
      if ((response.getMessage() != null)
          || (response.getFormSnippet() != null)) {
        LOGGER.warning("A rejected configuration for connector " + name
            + " was returned.");
        return response;
      }

      // If validateConfig() returns a response with no message or formSnippet,
      // but does include a configuration Map; then consider it a valid,
      // but possibly altered configuration and use it.
      if (response.getConfigData() != null) {
        LOGGER.config("A modified configuration for connector " + name
            + " was returned.");
        newConfiguration = new Configuration(config.getTypeName(),
            response.getConfigData(), config.getXml());
      }
    }

    // We have an apparently valid configuration. Create a connector instance
    // with that configuration.
    // TODO: try to avoid instantiating the connector 3 times.
    InstanceInfo newInstanceInfo = new InstanceInfo(name, connectorDir,
        newTypeInfo, newConfiguration);

    // Set up connector database access.
    setDatabaseAccess(newInstanceInfo);

    // Only after validateConfig and instantiation succeeds do we
    // save the new configuration to persistent store.
    newInstanceInfo.setConnectorConfiguration(newConfiguration);

    return null;
  }

  /**
   * Handles a change to a Connector's Configuration.  Shuts down any
   * current instance of the Connector and starts up a new instance with
   * the new Configuration.
   *
   * @param newTypeInfo the {@link TypeInfo} for this this Connector.
   * @param config a new {@link Configuration} for this Connector.
   */
  /* @Override */
  public void connectorConfigurationChanged(TypeInfo newTypeInfo,
      Configuration config) throws InstantiatorException {
    // We have an apparently valid configuration. Create a connector instance
    // with that configuration.
    String connectorWorkDir =
        config.getMap().get(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR);
    InstanceInfo newInstanceInfo = new InstanceInfo(name,
        new File(connectorWorkDir), newTypeInfo, config);

    // Tell old connector instance to shut down, as it is being replaced.
    resetBatch();
    shutdownConnector(false);

    setDatabaseAccess(newInstanceInfo);
    instanceInfo = newInstanceInfo;
    typeInfo = newTypeInfo;

    // The load value in a Schedule is docs/minute.
    loadManager.setLoad(getSchedule().getLoad());

    // Allow newly modified connector to resume traversals immediately.
    delayTraversal(TraversalDelayPolicy.IMMEDIATE);
  }

  @SuppressWarnings("unchecked")
  private void setDatabaseAccess(InstanceInfo instanceInfo) {
    if (connectorPersistentStoreFactory != null) {
      Connector connector = instanceInfo.getConnector();
      if (connector instanceof ConnectorPersistentStoreAware) {
        ConnectorPersistentStore pstore =
            connectorPersistentStoreFactory.newConnectorPersistentStore(
               instanceInfo.getName(),
               instanceInfo.getTypeInfo().getConnectorTypeName(),
               instanceInfo.getTypeInfo().getConnectorType());
        documentStore = (DocumentStore) pstore.getLocalDocumentStore();
        ((ConnectorPersistentStoreAware) connector).setDatabaseAccess(pstore);
      }
    }
  }

  private static ConfigureResponse validateConfig(String name,
      File connectorDir, TypeInfo typeInfo, Configuration config,
      Locale locale) throws InstantiatorException {
    ConnectorInstanceFactory factory =
        new ConnectorInstanceFactory(name, typeInfo, config);
    try {
      return typeInfo.getConnectorType()
          .validateConfig(config.getMap(), locale, factory);
    } catch (Exception e) {
      throw new InstantiatorException("Unexpected validateConfig failure.", e);
    } finally {
      factory.shutdown();
    }
  }

  // Extract connectorInstance.xml from the Connector's jar file.
  private static String getConnectorInstancePrototype(String name,
      TypeInfo typeInfo) {
    try {
      return StringUtils.streamToStringAndThrow(
          typeInfo.getConnectorInstancePrototype().getInputStream());
    } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "Failed to extract connectorInstance.xml "
            + " for connector " + name, ioe);
    }
    return null;
  }

  private static File makeConnectorDirectory(String name, TypeInfo typeInfo)
      throws InstantiatorException {
    File connectorDir = new File(typeInfo.getConnectorTypeDir(), name);
    if (connectorDir.exists()) {
      if (connectorDir.isDirectory()) {
        // we don't know why this directory already exists, but we're ok with it
        LOGGER.warning("Connector directory " + connectorDir.getAbsolutePath()
            + "; already exists for connector " + name);
      } else {
        throw new InstantiatorException("Existing file blocks creation of "
            + "connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name);
      }
    } else {
      if (!connectorDir.mkdirs()) {
        throw new InstantiatorException("Can not create "
            + "connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name);
      }
    }
    return connectorDir;
  }

  /**
   * Remove the on-disk {@link Connector} representation.  This removes
   * many or all files in the {@code Connector}'s directory.
   */
  // TODO: Issue 87: Should we force the removal of files created by the
  // Connector implementation? ConnectorShutdownAware.delete() gives the
  // Connector an opportunity to delete these files in a cleaner fashion.
  private static void removeConnectorDirectory(File connectorDir) {
    if (connectorDir.exists()) {
      if (!connectorDir.delete()) {
        LOGGER.warning("Failed to delete connector directory "
            + connectorDir.getPath()
            + "; this connector may be difficult to delete.");
      }
    }
  }
}
