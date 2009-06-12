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

package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.instantiator.ConnectorCoordinatorImpl;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.logging.Logger;

/**
 * A {@link Cancelable} for running a {@link Connector} batch using
 * a {@link Traverser}
 */
public class CancelableBatch implements Cancelable {
  private static final Logger LOGGER =
    Logger.getLogger(CancelableBatch.class.getName());

  final Traverser traverser;
  final String traverserName;
  final BatchResultRecorder batchResultRecorder;
  final int batchHint;
  // TODO(strellis): Add batch key support.
  final Object batchKey = null;
  final ConnectorCoordinatorImpl connectorInstance = null;

  /**
   * Construct a {@link CancelableBatch}.
   *
   * @param traverser {@link Traverser} for running the batch.
   * @param traverserName traverser name for logging purposes.
   * @param batchResultRecorder {@link BatchResultRecorder} for recording the
   *        result of running the batch.
   * @param batchHint hint as to the number of documents to process in the
   *        batch.
   */
  public CancelableBatch(Traverser traverser, String traverserName,
      BatchResultRecorder batchResultRecorder, int batchHint) {
    this.traverser = traverser;
    this.traverserName = traverserName;
    this.batchResultRecorder = batchResultRecorder;
    this.batchHint = batchHint;
  }

  public void cancel() {
   traverser.cancelBatch();
  }

  public void timeOut() {
    // TODO(strellis): Enhance ThreadPool to call this.
    connectorInstance.cancelBatch(batchKey);
  }

  public void run() {
    NDC.push("Traverse " + traverserName);
    try {
      LOGGER.fine("Begin runBatch; traverserName = " + traverserName
          + "batchHint = " + batchHint);
      int legacyBatchResult = traverser.runBatch(batchHint);
      BatchResult batchResult =
          BatchResult.newBatchResultFromLegacyBatchResult(legacyBatchResult);
      LOGGER.fine("Traverser " + traverserName + " batchDone with result = "
          + batchResult);
      // TODO(strellis): Employ batch key to assure the completion is only
      // recorded if the current batch is still running? One problem with
      // doing this is that work for a canceled batch will not be
      // applied to the host load calculation.
      batchResultRecorder.recordResult(batchResult);
    } finally {
      NDC.remove();
    }
  }

  @Override
  public String toString() {
    return "CancelableBatch traverser: "
        + traverser
        + " batchHint: "
        + batchHint;
  }
}
