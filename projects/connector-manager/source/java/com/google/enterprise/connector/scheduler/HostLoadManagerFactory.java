// Copyright 2010 Google Inc.
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
import com.google.enterprise.connector.traversal.FileSizeLimitInfo;
import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.SystemClock;

/**
 * Interface for a factory that creates {@link HostLoadManager} instances
 * dedicated to the named connector instance.
 */
public class HostLoadManagerFactory implements LoadManagerFactory {
  /**
   * The optimal number of documents for each Traversal to return.
   * Small batches (<100) incur significant per batch overhead.
   * Large batches may consume excessive local and Repository resources.
   */
  private int batchSize = 500;

  /**
   * The load measurement period in seconds. The default period is 1 minute.
   */
  private int period = 60;

  /**
   * Used for determining feed backlog status.
   */
  private FeedConnection feedConnection;

  /**
   * Used when calculating low-memory conditions.
   */
  private FileSizeLimitInfo fileSizeLimit;

  /**
   * Clock used for timing througput.
   */
  private Clock clock = new SystemClock();

  /**
   * Sets the {@link FeedConnection} used to determine distal feed backlogs.
   *
   * @param feedConnection a {@link FeedConnection}.
   */
  // TODO: Support multiple sinks where different connector instances
  // might feed different sinks.
  public void setFeedConnection(FeedConnection feedConnection) {
    this.feedConnection = feedConnection;
  }

  /**
   * Sets the {@link FileSizeLimitInfo} used to determine low memory conditions.
   *
   * @param fileSizeLimitInfo a {@link FileSizeLimitInfo}
   */
  public void setFileSizeLimitInfo(FileSizeLimitInfo fileSizeLimitInfo) {
    this.fileSizeLimit = fileSizeLimitInfo;
  }

  /**
   * Sets the {@link Clock} used to measure time periods.
   *
   * @param clock a {@link Clock}
   */
  public void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Sets the measurement period in seconds.
   *
   * @param periodInSeconds measurement period in seconds.
   */
  public void setPeriod(int periodInSeconds) {
    this.period = periodInSeconds;
  }

  /**
   * @param batchSize the target batchSize to set.
   */
  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  /**
   * Create a new {@link HostLoadManager} instance appropriate for the named
   * connector instance.
   *
   * @param connectorName the name of a connector instance.
   *
   * @return a {@link HostLoadManager} or {@code null} if no HostLoadManager
   *         is assigned to the connectorInstance.
   */
  //@Override
  public LoadManager newLoadManager(String connectorName) {
    HostLoadManager hlm = new HostLoadManager(feedConnection, fileSizeLimit, clock);
    hlm.setPeriod(period);
    hlm.setBatchSize(batchSize);
    return hlm;
  }
}
