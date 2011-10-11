// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchResultRecorder;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.BatchTimeout;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.logging.Logger;

/**
 * A {@link TimedCancelable} for running a {@link Connector} batch using
 * a {@link Traverser}
 */
class CancelableBatch implements TimedCancelable {
  private static final Logger LOGGER =
    Logger.getLogger(CancelableBatch.class.getName());

  final Traverser traverser;
  final String traverserName;
  final BatchResultRecorder batchResultRecorder;
  final BatchTimeout batchTimeout;
  final BatchSize batchSize;

  /**
   * Construct a {@link CancelableBatch}.
   *
   * @param traverser {@link Traverser} for running the batch.
   * @param traverserName traverser name for logging purposes.
   * @param batchResultRecorder {@link BatchResultRecorder} for recording
   *        the result of running the batch.
   * @param batchSize hint and constraints as to the number of documents
   *        to process in the batch.
   */
  public CancelableBatch(Traverser traverser, String traverserName,
      BatchResultRecorder batchResultRecorder, BatchTimeout batchTimeout,
      BatchSize batchSize) {
    this.traverser = traverser;
    this.traverserName = traverserName;
    this.batchResultRecorder = batchResultRecorder;
    this.batchSize = batchSize;
    this.batchTimeout = batchTimeout;
  }

  public void cancel() {
   traverser.cancelBatch();
  }

  public void timeout(TaskHandle taskHandle) {
     batchTimeout.timeout();
  }

  public void run() {
    NDC.push("Traverse " + traverserName);
    try {
      LOGGER.fine("Begin runBatch; traverserName = " + traverserName
          + "  " + batchSize);
      BatchResult batchResult = traverser.runBatch(batchSize);
      LOGGER.fine("Traverser " + traverserName + " batchDone with result = "
          + batchResult);
      batchResultRecorder.recordResult(batchResult);
    } finally {
      NDC.remove();
    }
  }

  @Override
  public String toString() {
    return "CancelableBatch traverser: " + traverser + "  " + batchSize;
  }
}
