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

import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.pusher.PushException;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.HasTimeout;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
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

  private Pusher pusher;
  private TraversalManager queryTraversalManager;
  private ConnectorStateStore connectorStateStore;
  private String connectorName;
  private int timeout;

  private static final int TRAVERSAL_TIMEOUT = 5000;

  public QueryTraverser(Pusher p, TraversalManager q, ConnectorStateStore c,
      String n) {
    this.pusher = p;
    this.queryTraversalManager = q;
    this.connectorStateStore = c;
    this.connectorName = n;
    if (this.queryTraversalManager instanceof HasTimeout) {
      int requestedTimeout = ((HasTimeout) queryTraversalManager)
          .getTimeoutMillis();
      this.timeout = Math.max(requestedTimeout, TRAVERSAL_TIMEOUT);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.enterprise.connector.traversal.Traverser#runBatch(int)
   */
  public synchronized int runBatch(int batchHint) {
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
      connectorState = connectorStateStore.getConnectorState(connectorName);
    } catch (IllegalStateException ise) {
      // We get here if the ConnectorStateStore for connector is disabled.
      // That happens if the connector was deleted while we were asleep.
      // Our connector seems to have been deleted.  Don't process a batch.
      LOGGER.finer("Halting traversal...");
      return Traverser.FORCE_WAIT;
    }

    if (connectorState == null) {
      try {
        LOGGER.finer("Starting traversal...");
        resultSet = queryTraversalManager.startTraversal();
      } catch (RepositoryException e) {
        LOGGER.log(Level.WARNING, "startTraversal threw exception: ", e);
      }
    } else {
      try {
        LOGGER.finer("Resuming traversal...");
        resultSet = queryTraversalManager.resumeTraversal(connectorState);
      } catch (RepositoryException e) {
        LOGGER.log(Level.WARNING, "resumeTraversal threw exception: ", e);
      }
    }

    // If the traversal returns null, that means that the repository has
    // no new content to traverse.
    if (resultSet == null) {
      LOGGER.finer("Result set is NULL, no documents returned for traversal");
      return Traverser.FORCE_WAIT;
    }

    try {
      while (true) {
        if (Thread.currentThread().isInterrupted()) {
          LOGGER.finest(
              "Thread has been interrupted...breaking out of batch run.");
          break;
        }

        Document nextDocument = null;
        try {
          LOGGER.finer("Pulling next document from connector " + connectorName);
          nextDocument = resultSet.nextDocument();
          if (nextDocument == null) {
            break;
          }
          LOGGER.finer("Sending document (" + nextDocument + ") from connector "
                       + connectorName + " to Pusher");
          pusher.take(nextDocument, connectorName);
          counter++;
          if (counter == batchHint) {
            break;
          }
        } catch (RepositoryDocumentException e) {
          // Skip individual documents that fail.  Proceed on to the next one.
          LOGGER.log(Level.WARNING, "Skipping document from connector "
                     + connectorName, e);
        } catch (OutOfMemoryError e) {
          String docid = null;
          try {
            if (nextDocument != null) {
              docid = Value.getSingleValueString(nextDocument,
                                                 SpiConstants.PROPNAME_DOCID);
            }
          } catch (IllegalArgumentException e1) {
            try {
              LOGGER.fine("Unable to get document id for document ("
                           + nextDocument + "): " + e1.getMessage());
            } catch (Throwable t) {}
          } catch (RepositoryException e1) {
            try {
              LOGGER.fine("Unable to get document id for document ("
                          + nextDocument + "): " + e1.getMessage());
            } catch (Throwable t) {}
          }
          LOGGER.warning("Out of JVM Heap Space.  Most likely document ("
                         + docid + ") is too large.  To fix, increase heap "
                         + "space or reduce size of document.");
          LOGGER.log(Level.FINEST, e.getMessage(), e);
        }
      }
    } catch (RepositoryException e) {
      LOGGER.log(Level.SEVERE, "Repository Exception during traversal.", e);
      if (counter == 0) {
        // If we blew up on the first document, it may be an indication that
        // there is a systemic Connector problem (for instance, loss of
        // connectivity to its repository).  Wait a while, then try again.
        counter = Traverser.FORCE_WAIT;
      }
    } catch (PushException e) {
      LOGGER.log(Level.SEVERE, "Push Exception during traversal.", e);
      // Drop the entire batch on the floor.  Do not call checkpoint
      // (as there is a discrepency between what the Connector thinks
      // it has fed, and what actually has been pushed).
      resultSet = null;
      counter = Traverser.FORCE_WAIT;
    } finally {
      // Checkpoint completed work as well as skip past troublesome documents
      // (e.g. documents that are too large and will always fail).
      if ((resultSet != null) && (checkpointAndSave(resultSet) == null)) {
        // Unable to get a checkpoint, so wait a while, then retry batch.
        counter = Traverser.FORCE_WAIT;
      }
    }
    return counter;
  }

  private String checkpointAndSave(DocumentList pm) {
    String connectorState = null;
    try {
      LOGGER.finest("Checkpointing for connector " + connectorName + " ...");
      connectorState = pm.checkpoint();
      if (connectorState != null) {
        connectorStateStore.storeConnectorState(connectorName, connectorState);
        LOGGER.finest("...checkpoint " + connectorState + " created.");
      }
      return connectorState;
    } catch (IllegalStateException ise) {
      // We get here if the ConnectorStateStore for connector is disabled.
      // That happens if the connector was deleted while we were working.
      // Our connector seems to have been deleted.  Don't save a checkpoint.
      LOGGER.finest("...checkpoint " + connectorState + " discarded.");
      return null;
    } catch (RepositoryException e) {
      // checkpoint throwing RepositoryException means there is no new
      // checkpoint.
      return null;
    }
  }

  public int getTimeoutMillis() {
    return timeout;
  }
}
