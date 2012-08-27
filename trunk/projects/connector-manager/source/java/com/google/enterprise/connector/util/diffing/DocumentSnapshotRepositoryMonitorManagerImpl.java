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

import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.util.ChecksumGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A {@link DocumentSnapshotRepositoryMonitorManager} implementation.  There is
 * one instance of this class per {@link DiffingConnector} created by Spring.
 * That instance gets signals from {@link DiffingConnectorTraversalManager}
 * to start (go from "cold" to "warm") and does so from scratch or from recovery
 * state.  It creates the {@link SnapshotStore} instances and invokes
 * their recovery method.  It creates and manages the
 * {@link DocumentSnapshotRepositoryMonitor} instances and passes guaranteed
 * checkpoints to these monitors.
 *
 * @since 2.8
 */
public class DocumentSnapshotRepositoryMonitorManagerImpl
    implements DocumentSnapshotRepositoryMonitorManager {
  /** Maximum time to wait for background threads to terminate (in ms). */
  private static final long MAX_SHUTDOWN_MS = 5000;

  private static final DocumentSink DOCUMENT_SINK = new LoggingDocumentSink();

  private static final Logger LOG = Logger.getLogger(
      DocumentSnapshotRepositoryMonitorManagerImpl.class.getName());

  private String makeMonitorNameFromStartPath(String startPath) {
    String monitorName = checksumGenerator.getChecksum(startPath);
    return monitorName;
  }

  private final List<Thread> threads =
      Collections.synchronizedList(new ArrayList<Thread>());
  private final Map<String, DocumentSnapshotRepositoryMonitor> fileSystemMonitorsByName =
      Collections.synchronizedMap(new HashMap<String, DocumentSnapshotRepositoryMonitor>());
  private boolean isRunning = false;  // Monitor threads start in off state.
  private final List<? extends SnapshotRepository<? extends DocumentSnapshot>>
      repositories;

  private final File snapshotDir;
  private final ChecksumGenerator checksumGenerator;
  private final CheckpointAndChangeQueue checkpointAndChangeQueue;
  private final ChangeQueue changeQueue;

  private final DocumentSnapshotFactory documentSnapshotFactory;

  /**
   * Constructs {@link DocumentSnapshotRepositoryMonitorManagerImpl}
   * for the {@link DiffingConnector}.
   *
   * @param repositories a {@code List} of {@link SnapshotRepository
   *        SnapshotRepositorys}
   * @param documentSnapshotFactory a {@link DocumentSnapshotFactory}
   * @param snapshotDir directory to store {@link SnapshotRepository}
   * @param checksumGenerator a {@link ChecksumGenerator} used to
   *        detect changes in a document's content
   * @param changeQueue a {@link ChangeQueue}
   * @param checkpointAndChangeQueue a
   *        {@link CheckpointAndChangeQueue}
   */
  public DocumentSnapshotRepositoryMonitorManagerImpl(
      List<? extends SnapshotRepository<
          ? extends DocumentSnapshot>> repositories,
      DocumentSnapshotFactory documentSnapshotFactory,
      File snapshotDir, ChecksumGenerator checksumGenerator,
      ChangeQueue changeQueue,
      CheckpointAndChangeQueue checkpointAndChangeQueue) {
    this.repositories = repositories;
    this.documentSnapshotFactory = documentSnapshotFactory;
    this.snapshotDir = snapshotDir;
    this.checksumGenerator = checksumGenerator;
    this.changeQueue = changeQueue;
    this.checkpointAndChangeQueue = checkpointAndChangeQueue;
  }

  private void flagAllMonitorsToStop() {
    for (SnapshotRepository<? extends DocumentSnapshot> repository
        : repositories) {
      String monitorName = makeMonitorNameFromStartPath(repository.getName());
      DocumentSnapshotRepositoryMonitor
          monitor = fileSystemMonitorsByName.get(monitorName);
      if (null != monitor) {
        LOG.fine("going to stop " + monitorName + " : " + repository.getName()
            + " " + monitor);
        monitor.shutdown();
      }
      else {
        LOG.fine("trying to stop non existent monitor thread for "
            + monitorName);
      }
    }
  }

  /* @Override */
  public synchronized void stop() {
    for (Thread thread : threads) {
      thread.interrupt();
    }
    for (Thread thread : threads) {
      try {
        thread.join(MAX_SHUTDOWN_MS);
        if (thread.isAlive()) {
          LOG.warning("failed to stop background thread: " + thread.getName());
        }
      } catch (InterruptedException e) {
        // Mark this thread as interrupted so it can be dealt with later.
        Thread.currentThread().interrupt();
      }
    }
    threads.clear();

    /* in case thread.interrupt doesn't stop monitors */
    flagAllMonitorsToStop();

    fileSystemMonitorsByName.clear();
    changeQueue.clear();
    this.isRunning = false;
  }

  /* For each start path gets its monitor recovery files in state were monitor
   * can be started. */
  private Map<String, SnapshotStore> recoverSnapshotStores(
      String connectorManagerCheckpoint, Map<String,
      MonitorCheckpoint> monitorPoints)
      throws IOException, SnapshotStoreException, InterruptedException {
    Map<String, SnapshotStore> snapshotStores =
        new HashMap<String, SnapshotStore>();
    for (SnapshotRepository<? extends DocumentSnapshot> repository
        : repositories) {
      String monitorName = makeMonitorNameFromStartPath(repository.getName());
      File dir = new File(snapshotDir,  monitorName);

      boolean startEmpty = (connectorManagerCheckpoint == null)
          || (!monitorPoints.containsKey(monitorName));
      if (startEmpty) {
        LOG.info("Deleting " + repository.getName()
            + " global checkpoint=" + connectorManagerCheckpoint
            + " monitor checkpoint=" + monitorPoints.get(monitorName));
        delete(dir);
      } else {
        SnapshotStore.stitch(dir, monitorPoints.get(monitorName),
            documentSnapshotFactory);
      }

      SnapshotStore snapshotStore = new SnapshotStore(dir,
          documentSnapshotFactory);

      snapshotStores.put(monitorName, snapshotStore);
    }
    return snapshotStores;
  }

  /** Go from "cold" to "warm" including CheckpointAndChangeQueue. */
  public void start(String connectorManagerCheckpoint)
      throws RepositoryException {

    try {
      checkpointAndChangeQueue.start(connectorManagerCheckpoint);
    } catch (IOException e) {
      throw new RepositoryException("Failed starting CheckpointAndChangeQueue.",
          e);
    }

    Map<String, MonitorCheckpoint> monitorPoints
        = checkpointAndChangeQueue.getMonitorRestartPoints();

    Map<String, SnapshotStore> snapshotStores = null;

    try {
      snapshotStores =
          recoverSnapshotStores(connectorManagerCheckpoint, monitorPoints);
    } catch (SnapshotStoreException e) {
      throw new RepositoryException("Snapshot recovery failed.", e);
    } catch (IOException e) {
      throw new RepositoryException("Snapshot recovery failed.", e);
    } catch (InterruptedException e) {
      throw new RepositoryException("Snapshot recovery interrupted.", e);
    }

    startMonitorThreads(snapshotStores, monitorPoints);
    isRunning = true;
  }

  /* @Override */
  public synchronized void clean() {
    LOG.info("Cleaning snapshot directory: " + snapshotDir.getAbsolutePath());
    if (!delete(snapshotDir)) {
      LOG.warning("failed to delete snapshot directory: "
          + snapshotDir.getAbsolutePath());
    }
    checkpointAndChangeQueue.clean();
  }

  /* @Override */
  public int getThreadCount() {
    int result = 0;
    for (Thread t : threads) {
      if (t.isAlive()) {
        result++;
      }
    }
    return result;
  }

  /* @Override */
  public synchronized CheckpointAndChangeQueue getCheckpointAndChangeQueue() {
    return checkpointAndChangeQueue;
  }

  /**
   * Delete a file or directory.
   *
   * @param file
   * @return true if the file is deleted.
   */
  private boolean delete(File file) {
    if (file.isDirectory()) {
      for (File contents : file.listFiles()) {
        delete(contents);
      }
    }
    return file.delete();
  }

  /**
   * Creates a {@link DocumentSnapshotRepositoryMonitor} thread for the provided
   * folder.
   *
   * @throws RepositoryDocumentException if {@code startPath} is not readable,
   *         or if there is any problem reading or writing snapshots.
   */
  private Thread newMonitorThread(
      SnapshotRepository<? extends DocumentSnapshot> repository,
      SnapshotStore snapshotStore, MonitorCheckpoint startCp)
      throws RepositoryDocumentException {
    String monitorName = makeMonitorNameFromStartPath(repository.getName());
    DocumentSnapshotRepositoryMonitor monitor =
        new DocumentSnapshotRepositoryMonitor(monitorName, repository,
            snapshotStore, changeQueue.newCallback(), DOCUMENT_SINK, startCp,
            documentSnapshotFactory);
    LOG.fine("adding a new monitor for " + monitorName + " : " + monitor);
    fileSystemMonitorsByName.put(monitorName, monitor);
    return new Thread(monitor);
  }

  /**
   * Creates a {@link DocumentSnapshotRepositoryMonitor} thread for each
   * startPath.
   *
   * @throws RepositoryDocumentException if any of the threads cannot be
   *         started.
   */
  private void startMonitorThreads(Map<String, SnapshotStore> snapshotStores,
      Map<String, MonitorCheckpoint> monitorPoints)
      throws RepositoryDocumentException {

    for (SnapshotRepository<? extends DocumentSnapshot> repository
            : repositories) {
      String monitorName = makeMonitorNameFromStartPath(repository.getName());
      SnapshotStore snapshotStore = snapshotStores.get(monitorName);
      Thread monitorThread = newMonitorThread(repository, snapshotStore,
          monitorPoints.get(monitorName));
      threads.add(monitorThread);

      LOG.info("starting monitor for <" + repository.getName() + ">");
      monitorThread.setName(repository.getName());
      monitorThread.setDaemon(true);
      monitorThread.start();
    }
  }

  /* @Override */
  public synchronized boolean isRunning() {
    return isRunning;
  }

  /* @Override */
  public void acceptGuarantees(Map<String, MonitorCheckpoint> guarantees) {
    for (Map.Entry<String, MonitorCheckpoint> entry : guarantees.entrySet()) {
      String monitorName = entry.getKey();
      MonitorCheckpoint checkpoint = entry.getValue();
      DocumentSnapshotRepositoryMonitor monitor = fileSystemMonitorsByName.get(monitorName);
      if (monitor != null) {
        // Signal is asynch.  Let monitor figure out how to use.
        monitor.acceptGuarantee(checkpoint);
      }
    }
  }
}
