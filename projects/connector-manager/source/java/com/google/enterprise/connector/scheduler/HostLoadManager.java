// Copyright 2006 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.scheduler;

import java.util.HashMap;
import java.util.Map;

/**
 *  Keeps track of the load for each connector instance as well as supplies
 *  batchHint to indicate how many docs to allow to be traversed by traverser. 
 */
public class HostLoadManager {
  // size of batches for traversal
  private static final int BATCH_SIZE = 100;
  
  private static final long MINUTE_IN_MILLIS = 60 * 1000;
  private long startTimeInMillis;
  private Map connectorNameToNumDocsTraversed;
  /**
   * Number of milliseconds before we ignore previously fed documents.  In
   * particular, we limit our feed rate during the duration
   * [startTimeInMillis, startTimeInMillis + periodInMillis].
   */
  private long periodInMillis;
  
  /* 
   * docs per second (TODO: we want to remove this and replace with 
   * configuration store and determine the right value on a per connector 
   * instance basis)  
   */
  private int maxFeedRate; 
  
  /**
   * TODO: constructor should take a configuration store object which will tell
   * us what the real maxFeedRate values are for each connector.
   * By default, the HostLoadManager will use a one minute period for
   * calculating the batchHint.
   * @param maxFeedRate
   */
  public HostLoadManager(int maxFeedRate) {
    this(MINUTE_IN_MILLIS, maxFeedRate);
  }
  
  /**
   * TODO: constructor should take a configuration store object which will tell
   * us what the real maxFeedRate values are for each connector.
   * @param periodInMillis time period in which we enforce the maxFeedRate
   * @param maxFeedRate
   */
  public HostLoadManager(long periodInMillis, int maxFeedRate) {
    this.periodInMillis = periodInMillis;
    this.maxFeedRate = maxFeedRate;
    
    startTimeInMillis = System.currentTimeMillis();
    connectorNameToNumDocsTraversed = new HashMap();
  }
  
  private int getMaxFeedRate(String connectorName) {
    return maxFeedRate;  // TODO: actually get value from configuration store
  }
  
  /**
   * Update startTimeInMillis and connectorNameToNumDocsFed based on current
   * time.
   */
  private void updateNumDocsTraversedData() {
    long now = System.currentTimeMillis();
    if (now > startTimeInMillis + periodInMillis) {
      startTimeInMillis = now;
      connectorNameToNumDocsTraversed.clear();
    }
  }
  
  /**
   * Determine the number of documents traversed since a given time.
   * @param connectorName name of the connector instance
   * @return number of documents traversed
   */
  private int getNumDocsTraversedThisPeriod(String connectorName) {
    updateNumDocsTraversedData();
    if (connectorNameToNumDocsTraversed.containsKey(connectorName)) {
      Integer numDocs = 
        (Integer) connectorNameToNumDocsTraversed.get(connectorName);
      return numDocs.intValue();
    }
    return 0;
  }
  
  /**
   * Let HostLoadManager know how many documents have been traversed so that
   * it can properly enforce the host load.
   * @param connectorName name of the connector instance
   * @param numDocsTraversed number of documents traversed
   */
  public void updateNumDocsTraversed(String connectorName, 
      int numDocsTraversed) {
    updateNumDocsTraversedData();
    Integer numDocs = 
      (Integer) connectorNameToNumDocsTraversed.get(connectorName);
    Integer updatedNumDocs;
    if (null == numDocs) {
      updatedNumDocs = new Integer(numDocsTraversed);
    } else {
      updatedNumDocs = new Integer(numDocs.intValue() + numDocsTraversed);
    }
    connectorNameToNumDocsTraversed.put(connectorName, updatedNumDocs);
  }
  
  /**
   * Determine how many documents to be recommended to be traversed.  This
   * number is based on the max feed rate for the connector instance as well
   * as the load determined based on calls to updateNumDocsTraversed(). 
   * @param connectorName name of the connector instance
   * @return hint to the number of documents traverser should traverse
   */
  public int determineBatchHint(String connectorName) {
    int maxDocsPerPeriod = 
      (int) ((periodInMillis / 1000f) * getMaxFeedRate(connectorName));
    int docsTraversed = getNumDocsTraversedThisPeriod(connectorName);
    int remainingDocsToTraverse = maxDocsPerPeriod - docsTraversed;
    if (remainingDocsToTraverse > BATCH_SIZE) {
      remainingDocsToTraverse = BATCH_SIZE;
    }
    if (remainingDocsToTraverse > 0) {
      return remainingDocsToTraverse;
    } else {
      return 0;
    }
  }
}
