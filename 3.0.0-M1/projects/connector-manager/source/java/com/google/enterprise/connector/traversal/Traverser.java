// Copyright (C) 2006-2009 Google Inc.
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

/**
 * Interface presented by a Traverser.  Used by the Scheduler.
 */
public interface Traverser {

  /**
   * Interval to wait after a transient error before retrying a traversal.
   */
  public static final int ERROR_WAIT_MILLIS = 15 * 60 * 1000;

  /**
   * Runs a batch of documents. The Traversal method may be hard (impossible?)
   * to interrupt while it is executing runBatch(). It is expected that a
   * thread loop running a traversal method would call runBatch(), then check
   * for InterruptedException, then decide whether it wants to stop of itself,
   * for scheduling reasons, or for a clean shutdown. It could then re-adjust
   * the batch hint if desired, then repeat.
   *
   * @param  batchSize A {@link BatchSize} instructs the traversal method to
   *         process approximately {@code batchSize.getHint()}, but no more
   *         than {@code batchSize.getMaximum()} number of documents in this
   *         batch.
   * @return A {@link BatchResult} containing the actual number of documents
   *         from this batch given to the feed and a possible policy to delay
   *         before requesting another batch.
   */
  public BatchResult runBatch(BatchSize batchSize);

  /**
   * Cancel the Batch in progress.  Discard the batch.  This might be called
   * when the workItem times out, connector deletion or reconfiguration, or
   * during shutdown.
   */
  public void cancelBatch();
}
