// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.pusher.FeedConnection;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Keeps track of the load for each connector instance as well as supplies
 *  batchHint to indicate how many docs to allow to be traversed by traverser.
 */
public class HostLoadManager {
  private static final Logger LOGGER =
      Logger.getLogger(HostLoadManager.class.getName());

  private static final long MINUTE_IN_MILLIS = 60 * 1000;
  private long startTimeInMillis;
  private final Map<String, Integer> connectorNameToNumDocsTraversed;
  private final Map<String, Long> connectorNameToFinishTime;
  // Default batch size to something reasonable.
  private int batchSize = 500;

  /**
   * Number of milliseconds before we ignore previously fed documents.  In
   * particular, we limit our feed rate during the duration
   * [startTimeInMillis, startTimeInMillis + periodInMillis].
   */
  private final long periodInMillis;

  /**
   * Used for determining the loads of the schedules.
   */
  private final Instantiator instantiator;

  /**
   * Used for determining feed backlog status.
   */
  private final FeedConnection feedConnection;

  /**
   * By default, the HostLoadManager will use a one minute period for
   * calculating the batchHint.
   *
   * @param instantiator used to get Schedule for Connector instances
   * @param feedConnection used to get FeedConnection backlog status
   */
  public HostLoadManager(Instantiator instantiator,
                         FeedConnection feedConnection) {
    this(instantiator, feedConnection, MINUTE_IN_MILLIS);
  }

  /**
   * Constructor used by unit tests.
   *
   * @param instantiator used to get schedule for connector instances
   * @param feedConnection used to get FeedConnection backlog status
   * @param periodInMillis time period in which we enforce the maxFeedRate
   */
  public HostLoadManager(Instantiator instantiator,
                         FeedConnection feedConnection, long periodInMillis) {
    this.instantiator = instantiator;
    this.feedConnection = feedConnection;
    this.periodInMillis = periodInMillis;
    startTimeInMillis = System.currentTimeMillis();
    connectorNameToNumDocsTraversed =
        Collections.synchronizedMap(new HashMap<String, Integer>());
    connectorNameToFinishTime =
        Collections.synchronizedMap(new HashMap<String, Long>());
  }

  /*
   * Only used for testing.
   */
  int getBatchSize() {
    return batchSize;
  }

  /**
   * @param batchSize the batchSize to set
   */
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  private int getMaxLoad(String connectorName) {
    String scheduleStr = null;
    try {
      scheduleStr = instantiator.getConnectorSchedule(connectorName);
    } catch (ConnectorNotFoundException e) {
      // Connector seems to have been deleted.
    }
    return (scheduleStr == null) ? 0 : new Schedule(scheduleStr).getLoad();
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
   *
   * @param connectorName name of the connector instance
   * @return number of documents traversed
   */
  private int getNumDocsTraversedThisPeriod(String connectorName) {
    updateNumDocsTraversedData();
    Integer numDocs = connectorNameToNumDocsTraversed.get(connectorName);
    return (numDocs == null) ? 0 : numDocs.intValue();
  }

  /**
   * Let HostLoadManager know how many documents have been traversed so that
   * it can properly enforce the host load.
   *
   * @param connectorName name of the connector instance
   * @param numDocsTraversed number of documents traversed
   */
  public void updateNumDocsTraversed(String connectorName,
      int numDocsTraversed) {
    synchronized (connectorNameToNumDocsTraversed) {
      int numDocs = getNumDocsTraversedThisPeriod(connectorName);
      connectorNameToNumDocsTraversed.put(connectorName,
          Integer.valueOf(numDocs + numDocsTraversed));
    }
  }

  /**
   * Let HostLoadManager know that a connector has just completed a traversal,
   * (whether it was a failure or natural completion is irrelevant).
   *
   * @param connectorName name of the connector instance
   * @param retryDelayMillis number of milliseconds to wait until retrying
   *        traversal
   */
  public void connectorFinishedTraversal(String connectorName,
                                         int retryDelayMillis) {
    Long finishTime = Long.valueOf(((retryDelayMillis < 0) ? Long.MAX_VALUE :
        (System.currentTimeMillis() + retryDelayMillis)));
    connectorNameToFinishTime.put(connectorName, finishTime);
  }

  /**
   * Remove the connector's statistics from the caches.
   * This is called when a connector is deleted.
   *
   * @param connectorName name of the connector instance
   */
  public void removeConnector(String connectorName) {
    connectorNameToFinishTime.remove(connectorName);
    connectorNameToNumDocsTraversed.remove(connectorName);
  }

  /**
   * Determine how many documents to be recommended to be traversed.  This
   * number is based on the max feed rate for the connector instance as well
   * as the load determined based on calls to updateNumDocsTraversed().
   *
   * @param connectorName name of the connector instance
   * @return hint to the number of documents traverser should traverse
   */
  public int determineBatchHint(String connectorName) {
    int maxDocsPerPeriod =
        (int) ((periodInMillis / 1000f) * (getMaxLoad(connectorName) / 60f));
    int docsTraversed = getNumDocsTraversedThisPeriod(connectorName);
    int remainingDocsToTraverse = maxDocsPerPeriod - docsTraversed;
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("connectorName = " + connectorName
          + "  maxDocsPerPeriod = " + maxDocsPerPeriod
          + "  docsTraversed = " + docsTraversed
          + "  remainingDocsToTraverse = " + remainingDocsToTraverse);
    }
    if (remainingDocsToTraverse > batchSize) {
      remainingDocsToTraverse = batchSize;
    }
    return (remainingDocsToTraverse > 0) ? remainingDocsToTraverse : 0;
  }

  /**
   * Return true if this connector instance should not be scheduled
   * for traversal at this time.
   *
   * @param connectorName name of the connector instance
   * @return true if the connector should not run at this time
   */
  public boolean shouldDelay(String connectorName) {
    // Is the connector waiting after a finished traversal pass
    // or waiting for an error condition to clear?
    Object value = connectorNameToFinishTime.get(connectorName);
    if (value != null) {
      long finishTime = ((Long)value).longValue();
      if (System.currentTimeMillis() < finishTime) {
        return true;
      }
    }

    // Has the connector exceeded it maximum number of documents per minute?
    int maxDocsPerPeriod =
        (int) ((periodInMillis / 1000f) * (getMaxLoad(connectorName) / 60f));
    int docsTraversed = getNumDocsTraversedThisPeriod(connectorName);
    int remainingDocsToTraverse = maxDocsPerPeriod - docsTraversed;
    return (remainingDocsToTraverse <= 0);
  }

  /**
   * Return true if systemic conditions indicate that traversals should
   * not happen at this time.  Perhaps the GSA is backlogged  or the
   * feedergate has died or the Connector Manager is being reconfigured.
   *
   * @return true if traversals should not run at this time.
   */
  public boolean shouldDelay() {
    return (feedConnection != null) ? feedConnection.isBacklogged() : false;
  }
}
