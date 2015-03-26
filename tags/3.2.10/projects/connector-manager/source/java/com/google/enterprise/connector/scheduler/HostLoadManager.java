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
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
import com.google.enterprise.connector.util.Clock;

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

  // TODO(bmj): Raise this to 1000 when the GSA moves there.
  public static final int DEFAULT_HOST_LOAD = 500;

  /**
   * The batch size as calculated by the last call to determineBatchSize().
   */
  private int lastBatchSize;

  /**
   * The last recorded {@link BatchResult} traversal result for the connector.
   * The result returned by the previous traversal is used to calculate the
   * optimum size for the next traversal batch to maintain the configured
   * host load.
   */
  private BatchResult lastBatchResult;

  /**
   * The optimal number of documents for each Traversal to return.
   * Small batches (<100) incur significant per batch overhead, and may
   * backlog feed processing on the GSA.
   * Large batches may consume excessive local and Repository resources.
   */
  private int batchSize = 1000;

  /** The smallest allowed batch size. */
  private int minBatchSize;

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
  private int load = DEFAULT_HOST_LOAD;

  /**
   * The target traversal rate, based upon the load and the period.
   */
  private float rate;

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
    seedLoad();
  }

  /**
   * Sets the target load in documents per period.
   *
   * @param load target load in documents per period.
   */
  @Override
  public void setLoad(int load) {
    if (load >= 0) {
      LOGGER.fine("Setting host load to " + load);
      this.load = load;
      seedLoad();
    } else {
      LOGGER.warning("Invalid host load: " + load);
    }
  }

  /**
   * Sets the measurement period in seconds.
   *
   * @param periodInSeconds measurement period in seconds.
   */
  @Override
  public void setPeriod(int periodInSeconds) {
    if (periodInSeconds > 0) {
      LOGGER.fine("Setting load measurement period to " + periodInSeconds
                  + " seconds");
      periodInMillis = periodInSeconds * 1000L;
      seedLoad();
    } else {
      LOGGER.warning("Invalid load measurement period: " + periodInSeconds);
    }
  }

  /**
   * @param batchSize the target batchSize to set.
   */
  @Override
  public void setBatchSize(int batchSize) {
    if (batchSize >= 0) {
      this.batchSize = batchSize;
      LOGGER.fine("Setting the maximum batch size to " + batchSize);
      seedLoad();
    } else {
      LOGGER.warning("Invalid batch size: " + batchSize);
    }
  }

  /**
   * Sets the target traversal rate, based upon the configured load and period,
   * and seeds the lastBatchRequest().
   */
  private void seedLoad() {
    minBatchSize = Math.min(load, batchSize);
    LOGGER.fine("Setting the minimum batch size to " + batchSize);

    rate = ((float) load) / periodInMillis;
    lastBatchSize = Math.min(load, batchSize);
    lastBatchResult = new BatchResult(TraversalDelayPolicy.IMMEDIATE,
                                      lastBatchSize, 0L, periodInMillis);
  }

  /**
   * Lets HostLoadManager know how many documents have been traversed so that
   * it can properly enforce the host load.
   *
   * @param batchResult a traversal BatchResult
   */
  @Override
  public void recordResult(BatchResult batchResult) {
    if (batchResult.getCountProcessed() > 0) {
      lastBatchResult = batchResult;
    }
  }

  /**
   * Determine how many documents to be recommended to be traversed.  This
   * number is based on the max feed rate for the connector instance as well
   * as the load determined based on recently recorded results.
   *
   * @return BatchSize hint to the number of documents the traverser
   *         should traverse
   */
  @Override
  public BatchSize determineBatchSize() {
    BatchRequest batchReq = getBatchRequest();
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest(batchReq.toString());
    }
    // If the delay time hasn't expired, batch size is 0.
    // However, if there is less that 100ms left, just let it go.
    if ((batchReq.delay == 0) ||
        (lastBatchResult.getEndTime() + batchReq.delay <
         clock.getTimeMillis() + 100)) {
      lastBatchSize = batchReq.batchSize;
      return new BatchSize(batchReq.batchSize);
    }
    return new BatchSize();
  }

  /**
   * Return true if this connector instance should not be scheduled
   * for traversal at this time.
   *
   * @return true if the connector should not run at this time
   */
  @Override
  public boolean shouldDelay() {
    BatchRequest batchReq = getBatchRequest();

    // If the delay time hasn't expired, continue delay.
    // However, if there is less that 100ms left, just let it go.
    if ((lastBatchResult.getEndTime() + batchReq.delay) >=
        clock.getTimeMillis() + 100) {
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

  /**
   * Calculate the batch size for the next traversal batch.
   * This uses the throughput of the previous traversal batch and
   * the previously determined batch size to determine a batch
   * size and a delay that will keep the traversal rate at or
   * below the configured load.
   */
  private BatchRequest getBatchRequest() {
    int count = lastBatchResult.getCountProcessed();
    int time = lastBatchResult.getElapsedTime();
    float lastRate = ((float) count) / time;
    int newBatchSize = (int)(lastBatchSize * rate / lastRate);
    if (lastRate < 0.85F * rate) {
      return new BatchRequest(0, Math.min(batchSize, newBatchSize));
    } else if (lastRate > 1.15F * rate) {
      int delay = (int) (count * periodInMillis / load - time);
      return new BatchRequest(delay, Math.max(minBatchSize, newBatchSize));
    } else {
      return new BatchRequest(0, lastBatchSize);
    }
  }

  private static class BatchRequest {
    public final int delay;
    public final int batchSize;

    BatchRequest(int delay, int batchSize) {
      this.delay = delay;
      this.batchSize = batchSize;
    }

    public String toString() {
      return ((delay == 0) ? "no delay" : "delay = " + delay)
          + ", batchSize = " + batchSize;
    }
  }
}
