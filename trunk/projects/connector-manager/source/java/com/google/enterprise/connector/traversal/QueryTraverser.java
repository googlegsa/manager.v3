// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

import java.util.Iterator;

/**
 * Traverser for a repository implemented using a QueryTraversalManager
 */
public class QueryTraverser implements Traverser {

  private Pusher pusher;
  private QueryTraversalManager queryTraversalManager;
  private ConnectorStateStore connectorStateStore;
  private String connectorName;

  public QueryTraverser(Pusher p, QueryTraversalManager q,
      ConnectorStateStore c, String n) {
    this.pusher = p;
    this.queryTraversalManager = q;
    this.connectorStateStore = c;
    this.connectorName = n;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.traversal.Traverser#runBatch(int)
   */
  public synchronized int runBatch(int batchHint) {
    int counter = 0;
    PropertyMap pm = null;
    String connectorState =
        connectorStateStore.getConnectorState(connectorName);
    ResultSet resultSet = null;
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

    Iterator iter = null;
    try {
      iter = resultSet.iterator();
    } catch (RepositoryException e) {
      // TODO:ziff Auto-generated catch block
      e.printStackTrace();
    }

    while (iter.hasNext()) {
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
      pm = (PropertyMap) iter.next();
      pusher.take(pm, connectorName);
      counter++;
      if (counter == batchHint) {
        break;
      }
    }

    if (counter != 0) {
      checkpointAndSave(pm);
    }

    return counter;
  }

  private void checkpointAndSave(PropertyMap pm) {
    String connectorState = null;
    try {
      connectorState = queryTraversalManager.checkpoint(pm);
    } catch (RepositoryException e) {
      // TODO:ziff Auto-generated catch block
      e.printStackTrace();
    }
    connectorStateStore.storeConnectorState(connectorName, connectorState);
  }
}
