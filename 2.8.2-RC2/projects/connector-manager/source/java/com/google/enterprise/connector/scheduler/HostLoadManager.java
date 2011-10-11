// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.pusher.FeedConnection;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.FileSizeLimitInfo;
import com.google.enterprise.connector.util.Clock;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Keeps track of the load for each connector instance as well as supplies
 *  batchHint to indicate how many docs to allow to be traversed by traverser.
 */
/* @NotThreadSafe */
/* @GuardedBy("ConnectorCoordinatorImpl") */
public class HostLoadManager implements LoadManager {
  private static final Logger LOGGER =
      Logger.getLogger(HostLoadManager.class.getName());

  private static final long MINUTE_IN_MILLIS = 60 * 1000L;

  /**
   * Cache containing the last few {@link BatchResult} traversal results for
   * the connector.  The results returned by recent traversals are used to
   * calculate the optimum size for the next traversal batch to maintain the
   * configured host load.
   */
  private final LinkedList<BatchResult> batchResults = new LinkedList<BatchResult>();

  /**
   * The optimal number of documents for each Traversal to return.
   * Small batches (<100) incur significant per batch overhead.
   * Large batches may consume excessive local and Repository resources.
   */
  private int batchSize = 500;

  /**
   * Number of milliseconds used to measure the feed rate.
   * In particular, we try to constrain our feed rate to
   * loadFromSchedule docs per periodInMillis.  Several
   * periods worth of results may be used to keep the load
   * on target.
   *
   * By default, the HostLoadManager will use a one minute period
   * for calculating the batchSize.
   */
  private long periodInMillis = MINUTE_IN_MILLIS;

  /**
   * The load is the target number of documents per period to process.
   */
  private int load = 1000;

  /**
   * Used for timing throughput.
   */
  private final Clock clock;

  /**
   * Used for determining feed backlog status.
   */
  private final FeedConnection feedConnection;

  /**
   * Used when calculating low-memory conditions.
   */
  private final FileSizeLimitInfo fileSizeLimit;
  private boolean gotLowMemory = false;
  private long lastLowMemMessage = 0L;

  /**
   * Constructor used by {@link HostLoadManagerFactory} to create a
   * {@link LoadManager} for a a connector instance.
   *
   * @param feedConnection a {@link FeedConnection}.
   * @param fileSizeLimit a {@link FileSizeLimitInfo}.
   * @param clock a {@link Clock}.
   */
  public HostLoadManager(FeedConnection feedConnection,
      FileSizeLimitInfo fileSizeLimit, Clock clock) {
    this.feedConnection = feedConnection;
    this.fileSizeLimit = fileSizeLimit;
    this.clock = clock;
  }

  /**
   * Sets the target load in documents per period.
   *
   * @param load target load in documents per period.
   */
  /* @Override */
  public void setLoad(int load) {
    this.load = load;
  }

  /**
   * Sets the measurement period in seconds.
   *
   * @param periodInSeconds measurement period in seconds.
   */
  /* @Override */
  public void setPeriod(int periodInSeconds) {
    periodInMillis = periodInSeconds * 1000L;
  }

  /**
   * @param batchSize the target batchSize to set.
   */
  /* @Override */
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  /**
   * Lets HostLoadManager know how many documents have been traversed so that
   * it can properly enforce the host load.
   *
   * @param batchResult a traversal BatchResult
   */
  /* @Override */
  public void recordResult(BatchResult batchResult) {
    if (batchResult.getCountProcessed() > 0) {
      batchResults.add(0, batchResult);
    }
  }

  /**
   * Determine the approximate number of documents processed in the supplied
   * batch during the specified period.
   *
   * @param r a BatchResult
   * @param periodStart the start of the time period in question.
   * @return number of documents traversed in the minute.
   */
  private int getNumDocsTraversedInPeriod(BatchResult r, long periodStart) {
    long periodEnd = periodStart + periodInMillis;
    if (r.getEndTime() <= periodStart || r.getStartTime() >= periodEnd) {
      return 0;
    }
    long start = (r.getStartTime() < periodStart) ? periodStart : r.getStartTime();
    long end = (r.getEndTime() > periodEnd) ? periodEnd : r.getEndTime();
    return (int)(r.getCountProcessed() * (end - start)) / r.getElapsedTime();
  }

  /**
   * Small struct holding the number of docs processed in each
   * of the previous two periods.
   */
  private class RecentDocs {
    // Number of documents processed during the current minute.
    public final int docsThisPeriod;

    // Number of documents processed during the previous minute.
    public final int docsPrevPeriod;

    public RecentDocs(int docsThisPeriod, int docsPrevPeriod) {
      this.docsThisPeriod = docsThisPeriod;
      this.docsPrevPeriod = docsPrevPeriod;
    }
  }

  /**
   * Determine the number of documents traversed since the start
   * of the current traversal period (minute) and during the previous
   * traversal period.
   *
   * @return number of documents traversed in this minute and the previous minute
   */
  private RecentDocs getNumDocsTraversedRecently() {
    int numDocs = 0;
    int prevNumDocs = 0;
    long thisPeriod = (clock.getTimeMillis() / periodInMillis) * periodInMillis;
    long prevPeriod = thisPeriod - periodInMillis;
    if (batchResults.size() > 0) {
      ListIterator<BatchResult> iter = batchResults.listIterator();
      while (iter.hasNext()) {
        BatchResult r = iter.next();
        numDocs += getNumDocsTraversedInPeriod(r, thisPeriod);
        prevNumDocs += getNumDocsTraversedInPeriod(r, prevPeriod);
        if (r.getEndTime() < prevPeriod) {
          // This is an old result.  We don't need it any more.
          iter.remove();
        }
      }
    }
    return new RecentDocs(numDocs, prevNumDocs);
  }

  /**
   * Determine how many documents to be recommended to be traversed.  This
   * number is based on the max feed rate for the connector instance as well
   * as the load determined based on recently recorded results.
   *
   * @return BatchSize hint and constraint to the number of documents traverser
   *         should traverse
   */
  /* @Override */
  public BatchSize determineBatchSize() {
    int maxDocsPerPeriod = load;
    RecentDocs traversed  = getNumDocsTraversedRecently();
    int remainingDocsToTraverse = maxDocsPerPeriod - traversed.docsThisPeriod;

    // If the connector grossly exceeded the target load during the last period,
    // try to balance it out with a reduced batch size this period.
    if (traversed.docsPrevPeriod > (maxDocsPerPeriod + maxDocsPerPeriod/10)) {
      remainingDocsToTraverse -= traversed.docsPrevPeriod - maxDocsPerPeriod;
    }

    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("maxDocsPerPeriod = " + maxDocsPerPeriod
          + "  docsTraversedThisPeriod = " + traversed.docsThisPeriod
          + "  docsTraversedPreviousPeriod = " + traversed.docsPrevPeriod
          + "  remainingDocsToTraverse = " + remainingDocsToTraverse);
    }

    if (remainingDocsToTraverse > 0) {
      int hint = Math.min(batchSize, remainingDocsToTraverse);
      // Allow the connector to return up to twice as much as we
      // ask for, even if it exceeds the load target.  However,
      // connectors that grossly exceed the batchSize, may be
      // penalized next time around to maintain an average load.
      int max =  Math.max(hint * 2, remainingDocsToTraverse);
      return new BatchSize(hint, max);
    } else {
      return new BatchSize();
    }
  }

  /**
   * Return true if this connector instance should not be scheduled
   * for traversal at this time.
   *
   * @return true if the connector should not run at this time
   */
  /* @Override */
  public boolean shouldDelay() {
    // Has the connector exceeded its maximum number of documents per minute?
    int maxDocsPerPeriod = load;
    RecentDocs traversed  = getNumDocsTraversedRecently();
    int remainingDocsToTraverse = maxDocsPerPeriod - traversed.docsThisPeriod;

    // If the connector grossly exceeded the target load during the last period,
    // try to balance it out with a reduced batch size this period.
    if (traversed.docsPrevPeriod > (maxDocsPerPeriod + maxDocsPerPeriod/10)) {
      remainingDocsToTraverse -= traversed.docsPrevPeriod - maxDocsPerPeriod;
    }

    // Avoid asking for tiny batches if we are near the load limit.
    int min = Math.min((maxDocsPerPeriod / 10), 20);
    if (remainingDocsToTraverse <= min) {
      return true;
    }

    // If the process is running low on memory, don't traverse.
    if (fileSizeLimit != null) {
      Runtime rt = Runtime.getRuntime();
      long available = rt.maxMemory() - (rt.totalMemory() - rt.freeMemory());
      if (available < fileSizeLimit.maxFeedSize()) {
        Level level = (gotLowMemory) ? Level.FINE : Level.WARNING;
        gotLowMemory = true;
        long now = clock.getTimeMillis();
        // Log message no more than once every minute.
        if (now > (lastLowMemMessage + (60 * 1000))) {
          lastLowMemMessage = now;
          LOGGER.log(level, "Delaying traversal due to low memory condition: "
                     + available / (1024 * 1024L) + " MB available");
        }
        return true;
      } else if (gotLowMemory) {
          gotLowMemory = false;
          lastLowMemMessage = 0L;
          LOGGER.info("Resuming traversal after low memory condition abates: "
                      + available / (1024 * 1024L) + " MB available");
      }
    }

    // If the GSA this connector is feeding is backlogged handling feeds,
    // don't traverse.
    if ((feedConnection != null) && feedConnection.isBacklogged()) {
      return true;
    }

    return false;
  }
}
