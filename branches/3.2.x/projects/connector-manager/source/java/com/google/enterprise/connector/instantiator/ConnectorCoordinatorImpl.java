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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.SecurityUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.database.ConnectorPersistentStoreFactory;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.pusher.DocumentAcceptorImpl;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.HostLoadManager;
import com.google.enterprise.connector.scheduler.LoadManager;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.ConnectorPersistentStoreAware;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.Lister;
import com.google.enterprise.connector.spi.Retriever;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalContextAware;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.TraversalSchedule;
import com.google.enterprise.connector.spi.TraversalScheduleAware;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchResultRecorder;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.QueryTraverser;
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
import com.google.enterprise.connector.traversal.Traverser;
import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConnectorCoordinator that supports Spring based connector instantiation and
 * persistent storage of connector configuration, schedule and traversal state.
 */
// TODO (jlacey): Context and ConnectorCoordinatorImpl are dangerously close
// to encountering deadlock issues, calling each other from synchronized 
// methods.  The most likely scenerio for deadlock would probably be when
// registering the CM with a new GSA.  Be wary when adding addition
// synchronization to these classes.
class ConnectorCoordinatorImpl implements
     ConnectorCoordinator, ChangeHandler, BatchResultRecorder {

  private static final Logger LOGGER =
      Logger.getLogger(ConnectorCoordinatorImpl.class.getName());

  /** A default, disabled Schedule. */
  private static final Schedule DEFAULT_SCHEDULE = new Schedule();

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
   * The cached TraversalManager.
   */
  private TraversalManager traversalManager;
  private boolean traversalEnabled;

  /**
   * The cached Lister.
   */
  private Lister lister;

  /**
   * The running Lister TaskHandle.
   */
  private TaskHandle listerHandle;

  /**
   * The cached Retriever.
   */
  private Retriever retriever;

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
    this.traversalEnabled = true;
  }

  /**
   * Returns the name of this {@link Connector}.
   *
   * @return The name of this Connector.
   */
  @Override
  public String getConnectorName() {
    return name;
  }

  /**
   * Returns {@code true} if an instance of this {@link Connector} exists.
   */
  @Override
  public synchronized boolean exists() {
    return (instanceInfo != null);
  }

  /**
   * Removes this {@link Connector} instance.  Halts traversals,
   * removes the Connector instance from the known connectors,
   * and removes the Connector's on-disk representation.
   */
  @Override
  public void removeConnector() {
    synchronized(this) {
      resetBatch();
      if (instanceInfo != null) {
        instanceInfo.removeConnector();
      }
    }
    // This must not be called while holding the lock.
    changeDetector.detect();
  }

  /**
   * Removes this {@link Connector} instance.  Halts traversals,
   * removes the Connector instance from the known connectors,
   * and removes the Connector's on-disk representation.
   */
  @Override
  public synchronized void connectorRemoved() {
    LOGGER.info("Dropping connector: " + name);
    try {
      resetBatch();
      if (instanceInfo != null) {
        File connectorDir = instanceInfo.getConnectorDir();
        shutdownConnector(true);
        removeConnectorDirectory(connectorDir);
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
  @Override
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
  @Override
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
  @Override
  public synchronized TraversalManager getTraversalManager()
      throws ConnectorNotFoundException, InstantiatorException {
    if (traversalManager == null && traversalEnabled) {
      traversalManager = getConnectorInterfaces().getTraversalManager();
      if (traversalManager == null) {
        LOGGER.fine("Connector " + name + " has no TraversalManager.");
        traversalEnabled = false;
      } else {
        setTraversalContext(traversalManager);
        setTraversalSchedule(traversalManager, getSchedule());
      }
    }
    return traversalManager;
  }

  /** If target is TraversalContextAware, set its traversalContext. */
  private void setTraversalContext(Object target) {
    if (target != null && target instanceof TraversalContextAware) {
      TraversalContext traversalContext =
          Context.getInstance().getTraversalContext();
      try {
        ((TraversalContextAware) target).setTraversalContext(traversalContext);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Unable to set TraversalContext", e);
      }
    }
  }

  /** If target is TraversalScheduleAware, set its traversalSchedule. */
  private void setTraversalSchedule(Object target, Schedule schedule) {
    if (target != null && target instanceof TraversalScheduleAware) {
      try {
        ((TraversalScheduleAware) target).setTraversalSchedule(schedule);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Unable to set TraversalSchedule", e);
      }
    }
  }

  /**
   * Returns the {@link Lister} for the {@link Connector}
   * instance.
   *
   * @return a Lister
   * @throws InstantiatorException
   */
  public synchronized Lister getLister()
      throws ConnectorNotFoundException, InstantiatorException {
    if (lister == null) {
      lister = getConnectorInterfaces().getLister();
      setTraversalContext(lister);
      setTraversalSchedule(lister, getSchedule());
    }
    return lister;
  }

  /** Start up the Lister for the connector, if this CM allows feeding. */
  private synchronized void startLister() throws InstantiatorException {
    if (Context.getInstance().isFeeding()) {
      try {
        Lister lister = getLister();
        if (lister != null) {
          LOGGER.log(Level.FINE, "Starting Lister for connector {0}", name);
          lister.setDocumentAcceptor(new DocumentAcceptorImpl(
              name, pusherFactory));
          listerHandle = threadPool.submit(new CancelableLister(name, lister));
        }
      } catch (ConnectorNotFoundException e) {
        throw new InstantiatorException("Connector not found " + name, e);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to start Lister for connector "
                   + name, e);
      }
    }
  }

  /** Stop the Lister for the connector. */
  private synchronized void stopLister() {
    if (listerHandle != null && !listerHandle.isDone()) {
      LOGGER.log(Level.FINE, "Stopping Lister for connector {0}", name);
      listerHandle.cancel();
    } else if (lister != null) {
      // We check lister here rather than getLister() to also avoid
      // logging this if the lister exists but has never been started.
      LOGGER.log(Level.FINER, "Already stopped Lister for connector {0}", name);
    }
  }

  /**
   * Return a {@link Retriever} that may be used to access content for the
   * document identified by {@code docid}.  If the connector does not support
   * the {@link Retriever} interface, {@code null} is returned.
   *
   * @return a {@link Retriever}, or {@code null} if none is available
   * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
   *         does not exist.
   * @throws InstantiatorException if unable to instantiate the requested
   *         {@link Retriever}
   */
  @Override
  public Retriever getRetriever()
      throws ConnectorNotFoundException, InstantiatorException {
    if (retriever == null) {
      retriever = getConnectorInterfaces().getRetriever();
      setTraversalContext(retriever);
    }
    return retriever;
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
  @Override
  public synchronized ConfigureResponse getConfigForm(Locale locale)
      throws ConnectorNotFoundException, InstantiatorException {
    Configuration config = getConnectorConfiguration();
    ConnectorType connectorType = typeInfo.getConnectorType();
    try {
      ConfigureResponse response;
      // If config is null, the connector was deleted behind our back.
      // Treat this a new connector configuration.
      if (config == null) {
        response = connectorType.getConfigForm(locale);
        if (response != null) {
          return new ExtendedConfigureResponse(response,
              getConnectorInstancePrototype(name, typeInfo));
        }
      } else {
        if (LOGGER.isLoggable(Level.CONFIG)) {
          LOGGER.config("GET POPULATED CONFIG FORM: locale = " + locale
                        + ", configuration = "
                        + SecurityUtils.getMaskedMap(config.getMap()));
        }
        response =
            connectorType.getPopulatedConfigForm(config.getMap(), locale);
        if (response != null) {
          return new ExtendedConfigureResponse(response, config);
        }
      }
      return response;
    } catch (Exception e) {
      throw new InstantiatorException("Failed to get configuration form", e);
    }
  }

  @Override
  public DocumentFilterFactory getDocumentFilterFactory()
      throws ConnectorNotFoundException {
    return getInstanceInfo().getDocumentFilterFactory();
  }

  /**
   * Retraverses the {@link Connector}'s content from scratch.
   * Halts any traversal in progress and removes any saved traversal state,
   * forcing the Connector to retraverse the Repository from its start.
   */
  @Override
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
            schedule.nextScheduledInterval() != -1) {
          schedule.setDisabled(false);
          getInstanceInfo().setConnectorSchedule(schedule);
      }
    }

    // TODO: Remove this if we switch completely to JDBC PersistentStore.
    // FileStore doesn't notice the deletion of a file that did not exist.
    if (lister != null) {
      connectorCheckpointChanged(null);
    }

    // This must not be called while holding the lock.
    changeDetector.detect();
  }

  /**
   * Returns a traversal {@link Schedule} for the {@link Connector} instance,
   * or a default, disabled {@link Schedule} if the connector has no schedule.
   */
  private synchronized Schedule getSchedule() {
    if (traversalSchedule == null) {
      try {
        traversalSchedule = getInstanceInfo().getConnectorSchedule();
        if (traversalSchedule == null) {
          return DEFAULT_SCHEDULE;
        }
      } catch (ConnectorNotFoundException e) {
        return DEFAULT_SCHEDULE;
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
  @Override
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
  @Override
  public synchronized void connectorScheduleChanged(Schedule schedule) {
    LOGGER.config("Schedule changed for connector " + name + ": " + schedule);

    // Refresh the cached Schedule.
    traversalSchedule = schedule;

    // Update the LoadManager with the new load.
    loadManager.setLoad((schedule == null)
        ? HostLoadManager.DEFAULT_HOST_LOAD : schedule.getLoad());

    // Let the traversal manager know the schedule changed.
    setTraversalSchedule(traversalManager, schedule);

    // Let the lister know the schedule changed.
    setTraversalSchedule(lister, schedule);

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
  @Override
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
  @Override
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
  @Override
  public void connectorCheckpointChanged(String checkpoint) {
    // If checkpoint has been nulled, then traverse the repository from scratch.
    if (checkpoint == null) {
      synchronized(this) {
        // Halt any traversal in progress.
        resetBatch();

        // Shut down any Lister.
        stopLister();

        try {
          // Restart Lister.
          startLister();
        } catch (InstantiatorException e) {
          LOGGER.log(Level.WARNING, "Failed to restart Lister for connector "
                     + name, e);
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
  @Override
  public synchronized String getConnectorState()
      throws ConnectorNotFoundException {
    return getInstanceInfo().getConnectorState();
  }

  /**
   * Returns the name of the {@link ConnectorType} for this {@link Connector}
   * instance.
   */
  @Override
  public synchronized String getConnectorTypeName()
      throws ConnectorNotFoundException {
    return getInstanceInfo().getTypeInfo().getConnectorTypeName();
  }

  /**
   * Sets the {@link Configuration} for this {@link ConnectorCoordinator}.
   * If this {@link ConnectorCoordinator} supports persistence this will
   * persist the new Configuration.
   */
  @Override
  public ConfigureResponse setConnectorConfiguration(TypeInfo newTypeInfo,
      Configuration configuration, Locale locale, boolean update)
      throws ConnectorNotFoundException, ConnectorExistsException,
      InstantiatorException {
    LOGGER.info("Configuring connector " + name);
    String typeName = newTypeInfo.getConnectorTypeName();
    Preconditions.checkArgument(typeName.equals(configuration.getTypeName()),
        "TypeInfo must match Configuration type");
    ConfigureResponse response = null;
    synchronized(this) {
      resetBatch();
      if (instanceInfo != null) {
        if (!update) {
          throw new ConnectorExistsException();
        }
        if (typeName.equals(typeInfo.getConnectorTypeName())) {
          configuration =
              new Configuration(configuration, getConnectorConfiguration());
          response = resetConfig(instanceInfo.getConnectorDir(), typeInfo,
              configuration, locale);
        } else {
          // An existing connector is being given a new type - drop then add.
          // TODO: This shouldn't be called from within the synchronized block
          // because it will kick the change detector.
          removeConnector();
          response = createNewConnector(newTypeInfo, configuration, locale);
          if (response != null) {
            // TODO: We need to restore original Connector config. This is
            // necessary once we allow update a Connector with new ConnectorType.
            // However, when doing so consider: createNewConnector could have
            // thrown InstantiatorException as well.  Also, you need to kick
            // the changeDetector (but not in this synchronized block).
            LOGGER.severe("Failed to update Connector configuration.");
            //    + " Restoring original Connector configuration.");
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
    } else {
      return new ExtendedConfigureResponse(response, configuration.getXml());
    }
    return response;
  }

  @Override
  public synchronized Configuration getConnectorConfiguration()
      throws ConnectorNotFoundException {
    Configuration config = getInstanceInfo().getConnectorConfiguration();
    if (config != null) {
      // Strip any "google*" properties that were saved by previous versions.
      config = removeGoogleProperties(config);

      if (config.getXml() == null) {
        return new Configuration(config,
            getConnectorInstancePrototype(name, typeInfo));
      }
    }
    return config;
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
            LOGGER.fine("Delaying traversal for connector " + name + " "
                        + ((retryDelayMillis < (120 * 1000))
                            ? ((retryDelayMillis / 1000) + " seconds")
                            : ((retryDelayMillis / (60 * 1000)) + " minutes"))
                        + " after repository reveals no new content.");
          }
        } catch (ConnectorNotFoundException cnfe) {
          // Connector was deleted while processing the batch.  Don't take any
          // action at the moment, as we may be in the middle of a reconfig.
        }
        break;

      case ERROR:
        traversalDelayEnd =
            clock.getTimeMillis() + Traverser.ERROR_WAIT_MILLIS;
        LOGGER.info("Delaying traversal for connector " + name + " "
                    + (Traverser.ERROR_WAIT_MILLIS / (60 * 1000))
                    + " minutes after encountering an error.");
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
    if (instanceInfo == null) {
      return false;
    }

    // If traversals are disabled, don't run.
    if (!traversalEnabled) {
      return false;
    }

    // Are we already running? If so, we shouldn't run again.
    if (taskHandle != null && !taskHandle.isDone()) {
      return false;
    }

    // If the traversal schedule is disabled, don't run.
    if (getSchedule().isDisabled()) {
      return false;
    }

    // Don't run if we have postponed traversals.
    if (clock.getTimeMillis() < traversalDelayEnd) {
      return false;
    }

    // Don't run if we have exceeded our configured host load.
    if (loadManager.shouldDelay()) {
      return false;
    }

    // Run if we are within scheduled traversal interval.
    return getSchedule().inScheduledInterval();
  }

  /**
   * Starts running a batch for this {@link ConnectorCoordinator} if a batch is
   * not already running.
   *
   * @return true if this call started a batch
   */
  @Override
  public synchronized boolean startBatch() {
    if (!shouldRun()) {
      return false;
    }

    BatchSize batchSize = loadManager.determineBatchSize();
    if (batchSize.getHint() == 0) {
      return false;
    }

    try {
      TraversalManager traversalManager = getTraversalManager();
      if (traversalManager == null) {
        return false;
      }
      currentBatchKey = new Object();
      BatchCoordinator batchCoordinator = new BatchCoordinator(this);
      Traverser traverser = new QueryTraverser(pusherFactory,
          traversalManager, batchCoordinator, name,
          Context.getInstance().getTraversalContext(), clock);
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
  @Override
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
  @Override
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

    // Discard cached interface instances.
    traversalManager = null;
    retriever = null;
    traversalSchedule = null;
  }

  /**
   * Informs the Connector instance that it will be shut down
   * and possibly deleted.
   *
   * @param delete {@code true} if the {@code Connector} will be deleted.
   */
  private void shutdownConnector(boolean delete) {
    // Discard cached instances.
    traversalManager = null;
    retriever = null;
    traversalSchedule = null;

    // Shut down the Lister, if running.
    stopLister();
    lister = null;

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
      Configuration configuration, Locale locale) throws InstantiatorException {
    if (newTypeInfo == null) {
      throw new IllegalStateException(
          "Create new connector with no type specified.");
    }
    if (instanceInfo != null) {
      throw new IllegalStateException(
          "Create new connector when one already exists.");
    }
    File connectorDir = getConnectorDir(newTypeInfo);
    boolean didMakeConnectorDir = makeConnectorDirectory(connectorDir);

    // If there is no connectorInstance.xml in the config, look to see if
    // there is one stored.  If not, fetch the connectorInstancePrototype
    // from the connectorType.
    if (configuration.getXml() == null) {
      // Check to see if there is a pre-existing connectorInstance.xml.
      Configuration old = new InstanceInfo(name, connectorDir, newTypeInfo)
          .getConnectorConfiguration();
      if (old != null && old.getXml() != null) {
        configuration = new Configuration(configuration, old);
      } else {
        configuration = new Configuration(configuration,
            getConnectorInstancePrototype(name, newTypeInfo));
      }
    }

    try {
      ConfigureResponse result =
          resetConfig(connectorDir, newTypeInfo, configuration, locale);
      if (result != null && result.getMessage() != null
          && didMakeConnectorDir) {
        removeConnectorDirectory(connectorDir);
      }
      return result;
    } catch (InstantiatorException ie) {
      if (didMakeConnectorDir) {
        removeConnectorDirectory(connectorDir);
      }
      throw (ie);
    }
  }

  @Override
  public void connectorAdded(TypeInfo newTypeInfo, Configuration configuration)
      throws InstantiatorException {
    if (instanceInfo != null) {
      throw new IllegalStateException(
          "Create new connector when one already exists.");
    }
    File connectorDir = getConnectorDir(newTypeInfo);
    boolean didMakeConnectorDir = makeConnectorDirectory(connectorDir);
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
    Configuration newConfiguration = addGoogleProperties(config, connectorDir);

    // Validate the configuration.
    if (LOGGER.isLoggable(Level.CONFIG)) {
      LOGGER.config("VALIDATE CONFIG: Validating connector " + name
          + ": locale = " + locale + ", " + newConfiguration);
    }

    ConfigureResponse response = validateConfig(connectorDir, newTypeInfo,
                                                newConfiguration, locale);
    if (response != null) {
      // If validateConfig() returns a non-null response with an error message,
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
        if (LOGGER.isLoggable(Level.CONFIG)) {
          LOGGER.config("A modified configuration for connector " + name
                        + " was returned: "
                        + SecurityUtils.getMaskedMap(response.getConfigData()));
        }
        newConfiguration = new Configuration(response.getConfigData(), config);
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
    newInstanceInfo.setConnectorConfiguration(
        removeGoogleProperties(newConfiguration));

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
  @Override
  public void connectorConfigurationChanged(TypeInfo newTypeInfo,
      Configuration config) throws InstantiatorException {
    if (LOGGER.isLoggable(Level.CONFIG)) {
      LOGGER.config("New configuration for connector " + name + ": " + config);
    }

    File connectorDir = getConnectorDir(newTypeInfo);

    // We have an apparently valid configuration. Create a connector instance
    // with that configuration.
    InstanceInfo newInstanceInfo = new InstanceInfo(name, connectorDir,
        newTypeInfo, addGoogleProperties(config, connectorDir));

    // Tell old connector instance to shut down, as it is being replaced.
    resetBatch();
    shutdownConnector(false);

    setDatabaseAccess(newInstanceInfo);
    instanceInfo = newInstanceInfo;
    typeInfo = newTypeInfo;

    // Prefetch an AuthorizationManager to avoid AuthZ time-outs
    // when logging in to repository at search time.
    try {
      getAuthorizationManager();
    } catch (ConnectorNotFoundException cnfe) {
      // Not going to happen here, but even if it did, we don't care.
    } catch (InstantiatorException ie) {
      // Likely failed connector.login(). This attempt to cache AuthZMgr failed.
      // However it is not important yet, so log it and continue on.
      LOGGER.log(Level.WARNING,
          "Failed to get AuthorizationManager for connector " + name, ie);
    }

    // The load value in a Schedule is docs/minute.
    loadManager.setLoad(getSchedule().getLoad());

    // Start up a Lister, if the Connector supports one.
    startLister();

    // Allow newly modified connector to resume traversals immediately.
    delayTraversal(TraversalDelayPolicy.IMMEDIATE);
  }

  /**
   * Sets GData configuration for GData aware Connectors.
   */
  /* TODO: This should either set real GData configuration or we should supply
   * the connector with a GDataClientFactory.  Unfortunately full GData
   * configuration (protocol, addr, port, userId, userPwd) in the CM doesn't
   * work for on-board Connector Managers, as it isn't editable.  At this
   * point, we supply just the GSA Feed Host to the connector and leave the
   * rest of the GData configuration to the connector.
   * A GDataClientFactory runs into problems when the feed host changes.
   */
  /* TODO: This is not HA safe! (But no change to CM config is.) */
  public void setGDataConfig()
      throws ConnectorNotFoundException, InstantiatorException {
    Map<String, String> newConfig = Maps.newHashMap();
    newConfig.put(PropertiesUtils.GOOGLE_FEED_HOST,
                  Context.getInstance().getGsaFeedHost());
    getInstanceInfo().setGDataConfig(newConfig);
  }

  /**
   * Adds special "google" properties to the Configuration.
   */
  private Configuration addGoogleProperties(Configuration config,
       File connectorDir) {
    Map<String, String> newConfig = Maps.newHashMap(config.getMap());
    newConfig.put(PropertiesUtils.GOOGLE_CONNECTOR_NAME, name);
    newConfig.put(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR,
                  connectorDir.getPath());
    Context context = Context.getInstance();
    newConfig.put(PropertiesUtils.GOOGLE_WORK_DIR,
                  context.getCommonDirPath());
    // TODO: This should either set real GData configuration or supply the
    // connector with a GDataClientFactory. See comment on setGDataConfig().
    if (context.getGsaFeedHost() != null) {  // Because Properties hate nulls.
      newConfig.put(PropertiesUtils.GOOGLE_FEED_HOST, context.getGsaFeedHost());
    }
    return new Configuration(newConfig, config);
  }

  /**
   * Removes non-persistable "google" properties from the Configuration.
   */
  private Configuration removeGoogleProperties(Configuration config) {
    // Make a copy of the map with google* entries removed.
    Map<String, String> newConfig = Maps.newHashMap(
        Maps.filterKeys(config.getMap(), new Predicate<String>() {
            public boolean apply(String input) {
              return !PropertiesUtils.GOOGLE_NONPERSISTABLE_PROPERTIES
                      .contains(input);
            }
        }));

    return new Configuration(newConfig, config);
  }

  @SuppressWarnings("unchecked")
  private void setDatabaseAccess(InstanceInfo instanceInfo)
      throws InstantiatorException {
    try {
      if (connectorPersistentStoreFactory != null) {
        Connector connector = instanceInfo.getConnector();
        if (connector instanceof ConnectorPersistentStoreAware) {
          ConnectorPersistentStore pstore =
            connectorPersistentStoreFactory.newConnectorPersistentStore(
                 instanceInfo.getName(),
                 instanceInfo.getTypeInfo().getConnectorTypeName(),
                 instanceInfo.getTypeInfo().getConnectorType());
          LOGGER.config("Setting DatabasePersistentStore for connector " + name);
          ((ConnectorPersistentStoreAware) connector).setDatabaseAccess(pstore);
        }
      }
    } catch (SQLException e) {
      throw new InstantiatorException("Failed to set database access for "
          + "connector " + instanceInfo.getName(), e);
    }
  }

  private ConfigureResponse validateConfig(
      File connectorDir, TypeInfo typeInfo, Configuration config,
      Locale locale) throws InstantiatorException {
    ConnectorInstanceFactory factory =
        new ConnectorInstanceFactory(name, typeInfo, config,
                                     connectorPersistentStoreFactory);
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

  /** Manufactures the connector directory path from the TypeInfo and name. */
  private File getConnectorDir(TypeInfo typeInfo) {
    return new File(typeInfo.getConnectorTypeDir(), name);
  }

  /**
   * Make the on-disk {@link Connector} directory, if it doesn't already exist.
   *
   * @return true if directory was created, false otherwise.
   * @throws InstantiatorException if the directory could not be created.
   */
  private boolean makeConnectorDirectory(File connectorDir)
      throws InstantiatorException {
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
      LOGGER.finest("Making connector directory "
                    + connectorDir.getAbsolutePath());
      if (!connectorDir.mkdirs()) {
        throw new InstantiatorException("Can not create "
            + "connector directory at " + connectorDir.getAbsolutePath()
            + " for connector " + name);
      }
      return true;
    }
    return false;
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
      LOGGER.finest("Removing connector directory "
                    + connectorDir.getAbsolutePath());
      if (!connectorDir.delete()) {
        LOGGER.warning("Failed to delete connector directory "
            + connectorDir.getPath()
            + "; this connector may be difficult to delete.");
      }
    }
  }
}
