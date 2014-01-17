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

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalContextAware;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.TraversalSchedule;
import com.google.enterprise.connector.spi.TraversalScheduleAware;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Implementation of {@link TraversalManager} for the {@link DiffingConnector}.
 *
 * @since 2.8
 */
public class DiffingConnectorTraversalManager implements TraversalManager,
    TraversalContextAware, TraversalScheduleAware {
  private static final Logger LOG = Logger.getLogger(
      DiffingConnectorTraversalManager.class.getName());
  private final DocumentSnapshotRepositoryMonitorManager
      snapshotRepositoryMonitorManager;
  private final TraversalContextManager traversalContextManager;
  /**
   * Boolean to mark TraversalManager as invalid.
   * It's possible for Connector Manager to keep a reference to
   * an outdated TraversalManager (after a new one has been given
   * previous TraversalManagers are invalid to use).
   */
  private boolean isActive = true;

  /**
   * Creates a {@link DiffingConnectorTraversalManager}.
   *
   * @param snapshotRepositoryMonitorManager the
   *        {@link DocumentSnapshotRepositoryMonitorManager}
   *        for use accessing a {@link ChangeSource}
   * @param traversalContextManager {@link TraversalContextManager}
   *        that holds the current {@link TraversalContext}
   */
  public DiffingConnectorTraversalManager(
      DocumentSnapshotRepositoryMonitorManager snapshotRepositoryMonitorManager,
      TraversalContextManager traversalContextManager) {
    this.snapshotRepositoryMonitorManager = snapshotRepositoryMonitorManager;
    this.traversalContextManager = traversalContextManager;
  }

  private DocumentList newDocumentList(String checkpoint)
      throws RepositoryException {

    CheckpointAndChangeQueue checkpointAndChangeQueue =
        snapshotRepositoryMonitorManager.getCheckpointAndChangeQueue();

    try {
      DiffingConnectorDocumentList documentList = new DiffingConnectorDocumentList(
          checkpointAndChangeQueue,
          CheckpointAndChangeQueue.initializeCheckpointStringIfNull(
              checkpoint));

      Map<String, MonitorCheckpoint> guaranteesMade =
          checkpointAndChangeQueue.getMonitorRestartPoints();

      snapshotRepositoryMonitorManager.acceptGuarantees(guaranteesMade);

      return new ConfirmActiveDocumentList(documentList);
    } catch (IOException e) {
      throw new RepositoryException("Failure when making DocumentList.", e);
    }
  }

  @Override
  public synchronized void setBatchHint(int batchHint) {
    if (isActive()) {
      snapshotRepositoryMonitorManager.getCheckpointAndChangeQueue()
          .setMaximumQueueSize(batchHint);
    }
  }

  /** Start document crawling and piping as if from beginning. */
  @Override
  public synchronized DocumentList startTraversal() throws RepositoryException {
    if (isActive()) {
      // Entirely reset connector's state.
      snapshotRepositoryMonitorManager.stop();
      snapshotRepositoryMonitorManager.clean();
      // With no state issue crawl command from null (beginning) checkpoint.
      return resumeTraversal(null);
    } else {
      throw new RepositoryException(
          "Inactive FileTraversalManager referanced.");
    }
  }

  @Override
  public synchronized DocumentList resumeTraversal(String checkpoint)
      throws RepositoryException {
    /* Exhaustive list of method's use:
     resumeTraversal(null) from startTraversal:
       monitors get started from null
     resumeTraversal(null) from Connector Manager sometime after startTraversal:
       monitors already started from previous resumeTraversal call
     resumeTraversal(cp) from Connector Manager without a startTraversal:
       means there was a shutdown or turn off
       monitors get started from cp; should use state
     resumeTraversal(cp) from Connector Manager sometime after some uses:
       is most common case; roll
    */
    if (isActive()) {
      if (!snapshotRepositoryMonitorManager.isRunning()) {
        snapshotRepositoryMonitorManager.start(checkpoint);
      }
      return newDocumentList(checkpoint);
    } else {
      throw new RepositoryException(
          "Inactive FileTraversalManager referanced.");
    }
  }

  @Override
  public synchronized void setTraversalContext(
      TraversalContext traversalContext) {
    if (isActive()) {
      this.traversalContextManager.setTraversalContext(traversalContext);
    }
  }

  @Override
  public synchronized void setTraversalSchedule(TraversalSchedule
      traversalSchedule) {
    snapshotRepositoryMonitorManager.setTraversalSchedule(traversalSchedule);
  }

  public synchronized void deactivate() {
    isActive = false;
    snapshotRepositoryMonitorManager.stop();
  }

  /** Public for testing. */
  public synchronized boolean isActive() {
    if (!isActive) {
      LOG.info("Inactive FileTraversalManager referanced.");
    }
    return isActive;
  }

  /**
   * A delegating {@link DocumentList} that throws an {@link Exception}
   * if called when {@link DiffingConnectorTraversalManager#isActive()} returns
   * false.
   */
  private class ConfirmActiveDocumentList implements DocumentList{
    DocumentList delegate;
    private ConfirmActiveDocumentList(DocumentList delegate) {
      this.delegate = delegate;
    }

    /**
     * @throws RepositoryException if {@link DiffingConnectorTraversalManager#isActive()}
     *         returns false.
     */
    @Override
    public String checkpoint() throws RepositoryException {
      synchronized (DiffingConnectorTraversalManager.this) {
        if (isActive()) {
          return delegate.checkpoint();
        } else {
          throw new RepositoryException(
              "Inactive FileTraversalManager referanced.");
        }
      }
    }

    /**
     * @throws RepositoryException if {@link DiffingConnectorTraversalManager#isActive()}
     *         returns false.
     */
    @Override
    public Document nextDocument() throws RepositoryException {
      synchronized (DiffingConnectorTraversalManager.this) {
        if (isActive()) {
          return delegate.nextDocument();
        } else {
          throw new RepositoryException(
              "Inactive FileTraversalManager referanced.");
        }
      }
    }
  }
}
