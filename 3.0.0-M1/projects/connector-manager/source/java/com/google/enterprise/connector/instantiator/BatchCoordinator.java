// Copyright (C) 2009 Google Inc.
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

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchResultRecorder;
import com.google.enterprise.connector.traversal.BatchTimeout;
import com.google.enterprise.connector.traversal.TraversalStateStore;

import java.util.logging.Logger;

/**
 * Coordinate operations that apply to a running batch with other changes that
 * affect this [@link {@link ConnectorCoordinatorImpl}.
 * <p>
 * The {@link ConnectorCoordinatorImpl} monitor is used to guard batch
 * operations.
 * <p>
 * To avoid long held locks the {@link ConnectorCoordinatorImpl} monitor is
 * not held while a batch runs or even between the time a batch is canceled
 * and the time its background processing completes. Therefore, a lingering
 * batch may attempt to record completion information, modify the checkpoint
 * or timeout after the lingering batch has been canceled. These operations
 * may even occur after a new batch has started. To avoid corrupting the
 * {@link ConnectorCoordinatorImpl} state this class employs the batchKey
 * protocol to disable completion operations that are performed on behalf of
 * lingering batches. Here is how the protocol works.
 * <OL>
 * <LI>To start a batch starts while holding the
 * {@link ConnectorCoordinatorImpl} monitor assign the batch a unique key.
 * Store the key in ConnectorCoordinator.this.currentBatchKey. Also create a
 * {@link BatchCoordinator} with BatchCoordinator.requiredBatchKey set to the
 * key for the batch.
 * <LI>To cancel a batch while holding the ConnectorCoordinatorImpl monitor,
 * null out ConnectorCoordinator.this.currentBatchKey.
 * <LI>The {@link BatchCoordinator} performs all completion operations for a
 * batch and prevents operations on behalf of non current batches. To check
 * while holding the {@link ConnectorCoordinatorImpl} monitor it
 * verifies that
 * BatchCoordinator.requiredBatchKey equals
 * ConnectorCoordinator.this.currentBatchKey.
 * </OL>
 */
class BatchCoordinator implements TraversalStateStore,
    BatchResultRecorder, BatchTimeout {

  private static final Logger LOGGER =
      Logger.getLogger(BatchCoordinator.class.getName());

  private String cachedState;
  private final Object requiredBatchKey;
  private final ConnectorCoordinatorImpl connectorCoordinator;

  /**
   * Creates a BatchCoordinator
   */
  BatchCoordinator(ConnectorCoordinatorImpl connectorCoordinator)
      throws ConnectorNotFoundException {
    this.requiredBatchKey = connectorCoordinator.currentBatchKey;
    this.cachedState = connectorCoordinator.getConnectorState();
    this.connectorCoordinator = connectorCoordinator;
  }

  public String getTraversalState() {
    synchronized (connectorCoordinator) {
      if (connectorCoordinator.currentBatchKey == requiredBatchKey) {
        return cachedState;
      } else {
        throw new BatchCompletedException();
      }
    }
  }

  public void storeTraversalState(String state) {
    synchronized (connectorCoordinator) {
      // Make sure our batch is still valid and that nobody has modified
      // the checkpoint while we were away.
      try {
        if ((connectorCoordinator.currentBatchKey == requiredBatchKey) &&
            isCheckpointUnmodified()) {
          connectorCoordinator.setConnectorState(state);
          cachedState = state;
        } else {
          throw new BatchCompletedException();
        }
      } catch (ConnectorNotFoundException cnfe) {
        // Connector disappeared while we were away.
        // Don't try to store results.
        throw new BatchCompletedException();
      }
    }
  }

  public void recordResult(BatchResult result) {
    synchronized (connectorCoordinator) {
      if (connectorCoordinator.currentBatchKey == requiredBatchKey) {
        connectorCoordinator.recordResult(result);
      } else {
        LOGGER.fine("Ignoring a BatchResult returned from a "
            + "prevously canceled traversal batch.  Connector = "
            + connectorCoordinator.getConnectorName()
            + "  result = " + result + "  batchKey = " + requiredBatchKey);
      }
    }
  }

  public void timeout() {
    synchronized (connectorCoordinator) {
      if (connectorCoordinator.currentBatchKey == requiredBatchKey) {
        connectorCoordinator.resetBatch();
      } else {
        LOGGER.warning("Ignoring Timeout for previously prevously canceled"
            + " or completed traversal batch.  Connector = "
            + connectorCoordinator.getConnectorName()
            + "  batchKey = "+ requiredBatchKey);
      }
    }
  }

  // Returns true if the stored traversal state has not been modified since
  // we started, false if the persisted state does not match our cache.
  private boolean isCheckpointUnmodified() throws ConnectorNotFoundException {
    String currentState = connectorCoordinator.getConnectorState();
    if (currentState == cachedState) {
      return true;
    } else {
      return (cachedState != null) && cachedState.equals(currentState);
    }
  }

  // TODO(strellis): Add this Exception to throws for BatchRecorder,
  //     TraversalStateStore, BatchTimeout interfaces and catch this
  //     specific exception rather than IllegalStateException.
  private static class BatchCompletedException extends IllegalStateException {
  }
}