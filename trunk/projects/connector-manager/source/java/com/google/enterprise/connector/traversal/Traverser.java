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
   * Signal to the TraversalManager that it should wait before calling
   * {@link #runBatch(int)} again for the Connector.
   */
  public static final int FORCE_WAIT = -1;

  /**
   * Signal to the TraversalManager that it need not wait before calling
   * {@link #runBatch(int)} again for the Connector, even if the Connector
   * returned no Documents in the previous batch.
   */
  public static final int NO_WAIT = 0;

  /**
   * Runs a batch of documents. The Traversal method may be hard (impossible?)
   * to interrupt while it is executing runBatch(). It is expected that a thread
   * loop running a traversal method would call runBatch(), then check for
   * InterruptedException, then decide whether it wants to stop of itself, for
   * scheduling reasons, or for a clean shutdown. It could then re-adjust the
   * batch hint if desired, then repeat.
   *
   * @param  batchHint A positive integer. This requests that the traversal
   *         method process no more than that number of documents in this batch.
   *
   * @return The actual number of documents given to the feed (may not be the
   *         same as the batch hint), with 0 and -1 having special meaning:
   *         A return value of -1 indicates that no new documents are available
   *         to index at this time - wait a while and try again.
   *         A return value of 0 indicates that while no documents were indexed
   *         in this batch, there are still potential candidates to consider.
   *         (Perhaps no documents passed in this batch because there were
   *         document errors or qualified documents are sparsely distributed in
   *         the repository).  Try another batch as soon as possible.
   *         A return value greater than 0 represents the actual number of
   *         documents traversed and pushed into the feed.  There are likely
   *         more documents available for traversal, so try another batch as
   *         soon as possible.
   *
   * @throws IllegalArgumentException if a non-positive batchHint is supplied.
   */
  /* TODO: Perhaps we should return a more complicated structure that allows
   *       for more interesting monitoring: number of successful docs,
   *       number failed, average size, etc.
   */
  public int runBatch(int batchHint);

  /**
   * Cancel the Batch in progress.  Discard the batch.  This might be called
   * when the workItem times out, connector deletion or reconfiguration, or
   * during shutdown.
   */
  public void cancelBatch();
}
