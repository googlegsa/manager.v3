// Copyright (C) 2006-2008 Google Inc.
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

package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.pusher.FeedException;
import com.google.enterprise.connector.pusher.PushException;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalContextAware;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traverser for a repository implemented using a TraversalManager
 */
public class QueryTraverser implements Traverser {
  private static final Logger LOGGER =
      Logger.getLogger(QueryTraverser.class.getName());

  private final Pusher pusher;
  private final TraversalManager queryTraversalManager;
  private TraversalStateStore stateStore;
  private final String connectorName;

  // Synchronize access to cancelWork.
  private final Object cancelLock = new Object();
  private boolean cancelWork = false;

  public QueryTraverser(Pusher pusher, TraversalManager traversalManager,
                        TraversalStateStore stateStore, String connectorName) {
    this.pusher = pusher;
    this.queryTraversalManager = traversalManager;
    this.stateStore = stateStore;
    this.connectorName = connectorName;
    if (queryTraversalManager instanceof TraversalContextAware) {
      TraversalContextAware contextAware =
          (TraversalContextAware)queryTraversalManager;
      try {
        TraversalContext tc = Context.getInstance().getTraversalContext();
        contextAware.setTraversalContext(tc);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Unable to set TraversalContext", e);
      }
    }
  }

  public void cancelBatch() {
    synchronized(cancelLock) {
      cancelWork = true;
      stateStore = null;
    }
    LOGGER.fine("Cancelling traversal for connector " + connectorName);
  }

  public boolean isCancelled() {
    synchronized(cancelLock) {
      return cancelWork;
    }
  }

  public synchronized int runBatch(int batchHint) {
    if (isCancelled()) {
        LOGGER.warning("Attempting to run a cancelled QueryTraverser");
        return Traverser.ERROR_WAIT;
    }
    if (batchHint <= 0) {
      throw new IllegalArgumentException("batchHint must be a positive int");
    }
    try {
      queryTraversalManager.setBatchHint(batchHint);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Unable to set batch hint", e);
    }

    int counter = 0;
    DocumentList resultSet = null;
    String connectorState;
    try {
      if (stateStore != null) {
        connectorState = stateStore.getTraversalState();
      } else {
        throw new IllegalStateException("null TraversalStateStore");
      }
    } catch (IllegalStateException ise) {
      // We get here if the ConnectorStateStore for connector is disabled.
      // That happens if the connector was deleted while we were asleep.
      // Our connector seems to have been deleted.  Don't process a batch.
      LOGGER.finer("Halting traversal..." + ise.getMessage());
      return Traverser.ERROR_WAIT;
    }

    if (connectorState == null) {
      try {
        LOGGER.finer("Starting traversal...");
        resultSet = queryTraversalManager.startTraversal();
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "startTraversal threw exception: ", e);
      }
    } else {
      try {
        LOGGER.finer("Resuming traversal...");
        resultSet = queryTraversalManager.resumeTraversal(connectorState);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "resumeTraversal threw exception: ", e);
      }
    }

    // If the traversal returns null, that means that the repository has
    // no new content to traverse.
    if (resultSet == null) {
      LOGGER.finer("Result set is NULL, no documents returned for traversal.");
      return Traverser.POLLING_WAIT;
    }

    try {
      while (true) {
        if (Thread.currentThread().isInterrupted() || isCancelled()) {
          LOGGER.fine("Traversal for connector " + connectorName
                      + " has been interrupted...breaking out of batch run.");
          break;
        }

        Document nextDocument = null;
        String docid = null;
        try {
          LOGGER.finer("Pulling next document from connector " + connectorName);
          nextDocument = resultSet.nextDocument();
          if (nextDocument == null) {
            break;
          } else {
            // Fetch DocId to use in messages.
            try {
              docid = Value.getSingleValueString(nextDocument,
                                                 SpiConstants.PROPNAME_DOCID);
            } catch (IllegalArgumentException e1) {
                LOGGER.fine("Unable to get document id for document ("
                            + nextDocument + "): " + e1.getMessage());
            } catch (RepositoryException e1) {
                LOGGER.fine("Unable to get document id for document ("
                            + nextDocument + "): " + e1.getMessage());
            }
          }
          LOGGER.finer("Sending document (" + docid + ") from connector "
              + connectorName + " to Pusher");
          pusher.take(nextDocument, connectorName);
          counter++;
          if (counter == batchHint) {
            break;
          }
        } catch (RepositoryDocumentException e) {
          // Skip individual documents that fail.  Proceed on to the next one.
          LOGGER.log(Level.WARNING, "Skipping document (" + docid
              + ") from connector " + connectorName, e);
        } catch (OutOfMemoryError e) {
          System.runFinalization();
          System.gc();
          try {
            LOGGER.warning("Out of JVM Heap Space.  Most likely document ("
                           + docid + ") is too large.  To fix, increase heap "
                           + "space or reduce size of document.");
            LOGGER.log(Level.FINEST, e.getMessage(), e);
          } catch (Throwable t) {
            // OutOfMemory state may prevent us from logging the error.
            // Don't make matters worse by rethrowing something meaningless.
          }
        } catch (RuntimeException e) {
          // Skip individual documents that fail.  Proceed on to the next one.
          LOGGER.log(Level.WARNING, "Skipping document (" + docid
              + ") from connector " + connectorName, e);
        }
      }
    } catch (RepositoryException e) {
      LOGGER.log(Level.SEVERE, "Repository Exception during traversal.", e);
      if (counter == 0) {
        // If we blew up on the first document, it may be an indication that
        // there is a systemic Connector problem (for instance, loss of
        // connectivity to its repository).  Wait a while, then try again.
        counter = Traverser.ERROR_WAIT;
      }
    } catch (PushException e) {
      LOGGER.log(Level.SEVERE, "Push Exception during traversal.", e);
      // Drop the entire batch on the floor.  Do not call checkpoint
      // (as there is a discrepancy between what the Connector thinks
      // it has fed, and what actually has been pushed).
      resultSet = null;
      counter = Traverser.ERROR_WAIT;
    } catch (FeedException e) {
      LOGGER.log(Level.SEVERE, "Feed Exception during traversal.", e);
      // Drop the entire batch on the floor.  Do not call checkpoint
      // (as there is a discrepancy between what the Connector thinks
      // it has fed, and what actually has been pushed).
      resultSet = null;
      counter = Traverser.ERROR_WAIT;
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Uncaught Exception during traversal.", t);
      // Drop the entire batch on the floor.  Do not call checkpoint
      // (as there is a discrepancy between what the Connector thinks
      // it has fed, and what actually has been pushed).
      resultSet = null;
      counter = Traverser.ERROR_WAIT;
    } finally {
      // If we have cancelled the work, abandon the batch.
      if (isCancelled()) {
        resultSet = null;
        counter = Traverser.ERROR_WAIT;
      }

      // Checkpoint completed work as well as skip past troublesome documents
      // (e.g. documents that are too large and will always fail).
      if ((resultSet != null) && (checkpointAndSave(resultSet) == null)) {
        // Unable to get a checkpoint, so wait a while, then retry batch.
        counter = Traverser.ERROR_WAIT;
      }
    }
    return counter;
  }

  private String checkpointAndSave(DocumentList pm) {
    String connectorState = null;
    LOGGER.finest("Checkpointing for connector " + connectorName + " ...");
    try {
      connectorState = pm.checkpoint();
    } catch (RepositoryException re) {
      // If checkpoint() throws RepositoryException, it means there is no
      // new checkpoint.
      return null;
    } catch (Exception e) {
      // If checkpoint() throws some general Exception, it is probably
      // an older connector that doesn't understand the newer empty
      // DocumentList and Exception handling from runBatch() model.
      return null;
    }
    try {
      if (connectorState != null) {
        if (stateStore != null) {
          stateStore.storeTraversalState(connectorState);
        } else {
          throw new IllegalStateException("null TraversalStateStore");
        }
        LOGGER.finest("...checkpoint " + connectorState + " created.");
      }
      return connectorState;
    } catch (IllegalStateException ise) {
      // We get here if the ConnectorStateStore for connector is disabled.
      // That happens if the connector was deleted while we were working.
      // Our connector seems to have been deleted.  Don't save a checkpoint.
      LOGGER.finest("...checkpoint " + connectorState + " discarded.");
    }
    return null;
  }
}
