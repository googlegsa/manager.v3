// Copyright (C) 2010 Google Inc.
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

import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchResultRecorder;
import com.google.enterprise.connector.traversal.BatchSize;

/**
 * Interface for a {@link LoadManager} implementations.
 */
public interface LoadManager extends BatchResultRecorder {
  /**
   * Sets the target load value.
   *
   * @param load
   */
  public void setLoad(int load);

  /**
   * Sets the measurement period in seconds.
   *
   * @param period in seconds
   */
  public void setPeriod(int period);

  /**
   * Sets the target batch size - the optimal number of documents to
   * return in an unconstrained traversal batch.
   *
   * @param batchSize the target batchSize to set.
   */
  public void setBatchSize(int batchSize);

  /**
   * Determine how many documents to be recommended to be traversed.  This
   * number is based on the max feed rate for the connector instance as well
   * as the load determined based on recently recorded results.
   *
   * @return BatchSize hint and constraint to the number of documents traverser
   *         should traverse
   */
  public BatchSize determineBatchSize();

  /**
   * Records the supplied {@link BatchResult} to be used against the load.
   *
   * @param batchResult a BatchResult
   */
  public void recordResult(BatchResult batchResult);

  /**
   * Returns true the the caller should delay processing in order to
   * maintain the target load management.
   */
  public boolean shouldDelay();
}
