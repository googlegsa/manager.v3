// Copyright 2009 Google Inc. All Rights Reserved.
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

/**
 * Holder for the result of running {@link Traverser#runBatch(BatchSize)}
 */
public class BatchResult {
  private final TraversalDelayPolicy delayPolicy;
  private final int countProcessed;
  private final long startTime;
  private final long endTime;

  /**
   * Construct a new {@link BatchResult}, with no documents processed and
   * no time spent processing.
   *
   * @param delayPolicy TraversalDelayPolicy to follow after this result.
   */
  public BatchResult(TraversalDelayPolicy delayPolicy) {
    this(delayPolicy, 0, 0L, 0L);
  }

  /**
   * Construct a new {@link BatchResult}, with number of documents processed
   * and no time spent processing.
   *
   * @param delayPolicy TraversalDelayPolicy to follow after this result.
   * @param countProcessed number of items processed in this batch.
   */
  // Legacy constructor used by Unit Tests.
  public BatchResult(TraversalDelayPolicy delayPolicy, int countProcessed) {
    this(delayPolicy, countProcessed, 0L, 0L);
  }

  /**
   * Construct a new {@link BatchResult}.
   *
   * @param delayPolicy TraversalDelayPolicy to follow after this result.
   * @param countProcessed number of items processed in this batch.
   * @param startTime the time (in milliseconds) when this batch started.
   * @param endTime the time (in milliseconds) when this batch finished.
   */
  public BatchResult(TraversalDelayPolicy delayPolicy, int countProcessed,
      long startTime, long endTime) {
    if (delayPolicy == null) {
      throw new IllegalArgumentException("Missing TraversalDelayPolicy");
    }
    this.delayPolicy = delayPolicy;
    this.countProcessed = countProcessed;
    this.startTime = startTime;
    // Avoid divide by 0 later.
    this.endTime = (startTime >= endTime) ? startTime + 1 : endTime;
  }

  /**
   * Returns the TraversalDelayPolicy that should be applied following
   * this Batch.
   */
  public TraversalDelayPolicy getDelayPolicy() {
    return delayPolicy;
  }

  /**
   * Returns the number of items processed in this Batch.
   */
  public int getCountProcessed() {
    return countProcessed;
  }

  /**
   * Returns the time (in milliseconds since 1970) when this Batch
   * started processing.
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * Returns the time (in milliseconds since 1970) when this Batch
   * finished processing.
   */
  public long getEndTime() {
    return endTime;
  }

  /**
   * Returns the elapsed time (in milliseconds) that this Batch
   * required for processing.
   */
  public int getElapsedTime() {
    return (int)(endTime - startTime);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + countProcessed;
    result = prime * result + delayPolicy.hashCode();
    result = prime * result + (int)(startTime);
    result = prime * result + (int)(endTime);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BatchResult other = (BatchResult) obj;
    if (countProcessed != other.countProcessed) {
      return false;
    }
    if (!delayPolicy.equals(other.delayPolicy)) {
      return false;
    }
    if (startTime != other.startTime) {
      return false;
    }
    if (endTime != other.endTime) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "BatchResult: delayPolicy = " + delayPolicy + " countProcessed = "
        + countProcessed + " elapsed time = " + (endTime - startTime)/1000
        + " seconds";
  }
}
