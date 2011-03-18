// Copyright 2006 Google Inc.
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

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.pusher.FeedException;
import com.google.enterprise.connector.pusher.PushException;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalContextAware;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.database.DocumentStore;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traverser for a repository implemented using a TraversalManager
 */
public class QueryTraverser implements Traverser {
  private static final Logger LOGGER =
      Logger.getLogger(QueryTraverser.class.getName());

  private final PusherFactory pusherFactory;
  private final TraversalManager queryTraversalManager;
  private final TraversalStateStore stateStore;
  private final DocumentStore documentStore;
  private final String connectorName;
  private final TraversalContext traversalContext;
  private final Clock clock;

  // Synchronize access to cancelWork.
  private final Object cancelLock = new Object();
  private boolean cancelWork = false;

  public QueryTraverser(PusherFactory pusherFactory,
      TraversalManager traversalManager, TraversalStateStore stateStore,
      String connectorName, TraversalContext traversalContext, Clock clock,
      DocumentStore documentStore) {
    this.pusherFactory = pusherFactory;
    this.queryTraversalManager = traversalManager;
    this.stateStore = stateStore;
    this.connectorName = connectorName;
    this.traversalContext = traversalContext;
    this.clock = clock;
    this.documentStore = documentStore;
    if (queryTraversalManager instanceof TraversalContextAware) {
      TraversalContextAware contextAware =
          (TraversalContextAware)queryTraversalManager;
      try {
        contextAware.setTraversalContext(traversalContext);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Unable to set TraversalContext", e);
      }
    }
  }

  //@Override
  public void cancelBatch() {
    synchronized(cancelLock) {
      cancelWork = true;
    }
    LOGGER.fine("Cancelling traversal for connector " + connectorName);
  }

  public boolean isCancelled() {
    synchronized(cancelLock) {
      return cancelWork;
    }
  }

  //@Override
  public BatchResult runBatch(BatchSize batchSize) {
    final long startTime = clock.getTimeMillis();
    final long timeoutTime = startTime
      + traversalContext.traversalTimeLimitSeconds() * 1000;

    if (isCancelled()) {
        LOGGER.warning("Attempting to run a cancelled QueryTraverser");
      return new BatchResult(TraversalDelayPolicy.ERROR);
    }
    try {
      queryTraversalManager.setBatchHint(batchSize.getHint());
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Unable to set batch hint", e);
    }

    String connectorState;
    try {
      if (stateStore != null) {
        connectorState = stateStore.getTraversalState();
      } else {
        throw new IllegalStateException("null TraversalStateStore");
      }
    } catch (IllegalStateException ise) {
      // We get here if the store for the connector is disabled.
      // That happens if the connector was deleted while we were asleep.
      // Our connector seems to have been deleted.  Don't process a batch.
      LOGGER.finer("Halting traversal..." + ise.getMessage());
      return new BatchResult(TraversalDelayPolicy.ERROR);
    }

    DocumentList resultSet = null;
    if (connectorState == null) {
      try {
        LOGGER.finer("Starting traversal...");
        resultSet = queryTraversalManager.startTraversal();
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "startTraversal threw exception: ", e);
        return new BatchResult(TraversalDelayPolicy.ERROR);
      }
    } else {
      try {
        LOGGER.finer("Resuming traversal...");
        resultSet = queryTraversalManager.resumeTraversal(connectorState);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "resumeTraversal threw exception: ", e);
        return new BatchResult(TraversalDelayPolicy.ERROR);
      }
    }

    // If the traversal returns null, that means that the repository has
    // no new content to traverse.
    if (resultSet == null) {
      LOGGER.finer("Result set is NULL, no documents returned for traversal.");
      return new BatchResult(TraversalDelayPolicy.POLL, 0);
    }

    Pusher pusher = null;
    BatchResult result = null;
    int counter = 0;
    try {
      // Get a Pusher for feeding the returned Documents.
      pusher = pusherFactory.newPusher(connectorName);

      while (counter < batchSize.getMaximum()) {
        if (Thread.currentThread().isInterrupted() || isCancelled()) {
          LOGGER.fine("Traversal for connector " + connectorName
                      + " has been interrupted...breaking out of batch run.");
          break;
        }
        if (clock.getTimeMillis() >= timeoutTime) {
          LOGGER.fine("Traversal for connector " + connectorName
              + " is completing due to time limit.");
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
            // Since there are a couple of places below that could throw
            // exceptions but not exit the while loop, the counter should be
            // incremented here to insure it represents documents returned from
            // the list.  Note the call to nextDocument() could also throw a
            // RepositoryDocumentException signaling a skipped document in which
            // case the call will not be counted against the batch maximum.
            counter++;
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

          if (!pusher.take(nextDocument, documentStore)) {
            LOGGER.fine("Traversal for connector " + connectorName
                + " is completing at the request of the Pusher.");
            break;
          }

        } catch (SkippedDocumentException e) {
          /* TODO (bmj): This is a temporary solution and should be replaced.
           * It uses Exceptions for non-exceptional cases.
           */
          // Skip this document.  Proceed on to the next one.
          skipDocument(docid, nextDocument, e);
        } catch (RepositoryDocumentException e) {
          // Skip individual documents that fail.  Proceed on to the next one.
          skipDocument(docid, nextDocument, e);
        } catch (RuntimeException e) {
          // Skip individual documents that fail.  Proceed on to the next one.
          skipDocument(docid, nextDocument, e);
        }
      }
      // No more documents. Wrap up any accumulated feed data and send it off.
      if (!isCancelled()) {
        pusher.flush();
        if (documentStore != null) {
          documentStore.flush();
        }
      }
    } catch (OutOfMemoryError e) {
      pusher.cancel();
      if (documentStore != null) {
        documentStore.cancel();
      }
      System.runFinalization();
      System.gc();
      result = new BatchResult(TraversalDelayPolicy.ERROR);
      try {
        LOGGER.severe("Out of JVM Heap Space.  Will retry later.");
        LOGGER.log(Level.FINEST, e.getMessage(), e);
      } catch (Throwable t) {
        // OutOfMemory state may prevent us from logging the error.
        // Don't make matters worse by rethrowing something meaningless.
      }
    } catch (RepositoryException e) {
      // Drop the entire batch on the floor.  Do not call checkpoint
      // (as there is a discrepancy between what the Connector thinks
      // it has fed, and what actually has been pushed).
      LOGGER.log(Level.SEVERE, "Repository Exception during traversal.", e);
      result = new BatchResult(TraversalDelayPolicy.ERROR);
    } catch (PushException e) {
      LOGGER.log(Level.SEVERE, "Push Exception during traversal.", e);
      // Drop the entire batch on the floor.  Do not call checkpoint
      // (as there is a discrepancy between what the Connector thinks
      // it has fed, and what actually has been pushed).
      result = new BatchResult(TraversalDelayPolicy.ERROR);
    } catch (FeedException e) {
      LOGGER.log(Level.SEVERE, "Feed Exception during traversal.", e);
      // Drop the entire batch on the floor.  Do not call checkpoint
      // (as there is a discrepancy between what the Connector thinks
      // it has fed, and what actually has been pushed).
      result = new BatchResult(TraversalDelayPolicy.ERROR);
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Uncaught Exception during traversal.", t);
      // Drop the entire batch on the floor.  Do not call checkpoint
      // (as there is a discrepancy between what the Connector thinks
      // it has fed, and what actually has been pushed).
      result = new BatchResult(TraversalDelayPolicy.ERROR);
   } finally {
      // If we have cancelled the work, abandon the batch.
      if (isCancelled()) {
        result = new BatchResult(TraversalDelayPolicy.ERROR);
      }

      // Checkpoint completed work as well as skip past troublesome documents
      // (e.g. documents that are too large and will always fail).
      if ((result == null) && (checkpointAndSave(resultSet) == null)) {
        // Unable to get a checkpoint, so wait a while, then retry batch.
        result = new BatchResult(TraversalDelayPolicy.ERROR);
      }
    }
    if (result == null) {
      result = new BatchResult(TraversalDelayPolicy.IMMEDIATE, counter,
                               startTime, clock.getTimeMillis());
    } else if (pusher != null) {
      // We are returning an error from this batch. Cancel any feed that
      // might be in progress.
      pusher.cancel();
      if (documentStore != null) {
        documentStore.cancel();
      }
    }
    return result;
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
      // We get here if the store for the connector is disabled.
      // That happens if the connector was deleted while we were working.
      // Our connector seems to have been deleted.  Don't save a checkpoint.
      LOGGER.finest("...checkpoint " + connectorState + " discarded.");
    }
    return null;
  }

  private void skipDocument(String docid, Document document, Exception e) {
    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.log(Level.FINER, "Skipping document (" + docid
          + ") from connector " + connectorName + ": " + e.getMessage());
    }
    if (documentStore != null) {
      documentStore.storeDocument(new SkippedDocument(document, e));
    }
  }

  /**
   * Wraps the supplied {@link Document}, adding a
   * {@code SpiConstants.PROPNAME_MESSAGE} property to the set of
   * properties.
   */
  private class SkippedDocument implements Document {
    private Document document;
    private Exception exception;

    SkippedDocument(Document document, Exception exception) {
      this.document = document;
      this.exception = exception;
    }

    /* @Override */
    public Property findProperty(String name) throws RepositoryException {
      if (SpiConstants.PROPNAME_MESSAGE.equals(name)) {
        String message = Value.getSingleValueString(document, name);
        message = exception.getMessage() + ((message == null) ? "" : ": " + message);
        return new SimpleProperty(Value.getStringValue(message));
      } else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
        return new SimpleProperty(Value.getStringValue(
            SpiConstants.ActionType.SKIPPED.toString()));
      } else {
        return document.findProperty(name);
      }
    }

    /* @Override */
    public Set<String> getPropertyNames() throws RepositoryException {
      return new ImmutableSet.Builder<String>()
             .add(SpiConstants.PROPNAME_ACTION)
             .add(SpiConstants.PROPNAME_MESSAGE)
             .addAll(document.getPropertyNames())
             .build();
    }
  }
}
