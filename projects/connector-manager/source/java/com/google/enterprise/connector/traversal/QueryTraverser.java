// Copyright (C) 2006 Google Inc.
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
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traverser for a repository implemented using a TraversalManager
 */
public class QueryTraverser implements Traverser {
  private static final Logger LOGGER = Logger.getLogger(QueryTraverser.class
      .getName());

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
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    int counter = 0;
    String connectorState = connectorStateStore
        .getConnectorState(connectorName);
    DocumentList resultSet = null;
    if (connectorState == null) {
      try {
        resultSet = queryTraversalManager.startTraversal();
      } catch (RepositoryException e) {
        // TODO:ziff Auto-generated catch block
        e.printStackTrace();
      }
    } else {
      try {
        resultSet = queryTraversalManager.resumeTraversal(connectorState);
      } catch (RepositoryException e) {
        // TODO:ziff Auto-generated catch block
        e.printStackTrace();
      }
    }

    if (resultSet == null) {
      return 0;
    }

    Document nextDocument = null;
    boolean forceCheckpoint = false;
    try {
       while (true) {
        try {
          nextDocument = resultSet.nextDocument();
        } catch (RepositoryException e) {
          LOGGER.log(Level.SEVERE, "Repository Exception during traversal.", e);
        }
        if (nextDocument == null) {
          break;
        }
        if (Thread.interrupted()) {
          break;
        }
        pusher.take(nextDocument, connectorName);
        counter++;
        if (counter == batchHint) {
          break;
        }
      }
    } catch (OutOfMemoryError e) {
      forceCheckpoint = true;
      String docid = null;
      try {
        docid = Value.getSingleValueString(nextDocument, SpiConstants.PROPNAME_DOCID);
      } catch (IllegalArgumentException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (RepositoryException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      LOGGER.warning("Out of JVM Heap Space.  Most likely document (" + docid
          + ") is too large.  To fix, increase heap space or reduce size "
          + "of document.");
    } catch (PushException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      e.printStackTrace();
    } finally {
      // checkpoint completed work as well as skip past troublesome documents
      // (e.g. documents that are too large and will always fail)
      if (forceCheckpoint || (counter != 0)) {
        if (null != resultSet) {
          checkpointAndSave(resultSet);
        }
      }
    }

    return counter;
  }

  private void checkpointAndSave(DocumentList pm) {
    String connectorState = null;
    try {
      connectorState = pm.checkpoint();
    } catch (RepositoryException e) {
      // TODO:ziff Auto-generated catch block
      e.printStackTrace();
    }
    if (connectorState != null) {
      connectorStateStore.storeConnectorState(connectorName, connectorState);
    }
  }

  public int getTimeoutMillis() {
    return timeout;
  }
}
