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
import com.google.enterprise.connector.spi.HasTimeout;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.PropertyMapList;

import java.util.Iterator;

/**
 * Traverser for a repository implemented using a TraversalManager
 */
public class QueryTraverser implements Traverser {

  private Pusher pusher;
  private TraversalManager queryTraversalManager;
  private ConnectorStateStore connectorStateStore;
  private String connectorName;
  private int timeout;
  
  private static final int TRAVERSAL_TIMEOUT = 5000;

  public QueryTraverser(Pusher p, TraversalManager q,
      ConnectorStateStore c, String n) {
    this.pusher = p;
    this.queryTraversalManager = q;
    this.connectorStateStore = c;
    this.connectorName = n;
    if (this.queryTraversalManager instanceof HasTimeout) {
    	int requestedTimeout = ((HasTimeout) queryTraversalManager).getTimeoutMillis();
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
    PropertyMap pm = null;
    String connectorState =
        connectorStateStore.getConnectorState(connectorName);
    PropertyMapList resultSet = null;
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
    
    Iterator iter = null;
    try {
      iter = resultSet.iterator();
    } catch (RepositoryException e) {
      // TODO:ziff Auto-generated catch block
      e.printStackTrace();
    }

    try {
      while (iter.hasNext()) {
        if (Thread.interrupted()) {
          break;
        }
        pm = (PropertyMap) iter.next();
        pusher.take(pm, connectorName);
        counter++;
        if (counter == batchHint) {
          break;
        }
      }
    } finally {
      // in case we have an unexpected exception such as a RuntimeException,
      // we ensure that we checkpoint what we've done
      if (counter != 0) {
        checkpointAndSave(pm);
      }
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
  
  public int getTimeoutMillis() {
	return timeout;
  }
}
