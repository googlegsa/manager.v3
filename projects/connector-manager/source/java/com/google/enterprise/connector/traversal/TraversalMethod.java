// Copyright (C) 2006 Google Inc.
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
 * Interface presented by a TraversalMethod.  Used by the controller.
 */
public interface TraversalMethod {

  /**
   * Runs a batch of documents. The Traversal method may be hard (impossible?)
   * to interrupt while it is executing runBatch(). It is expected that a thread
   * loop running a traversal method would call runBatch(), then check for
   * InterruptedException, then decide whether it wants to stop of itself, for
   * scheduling reasons, or for a clean shutdown. It could then re-adjust the
   * batch hint if desired, then repeat.
   * 
   * @param batchHint Must be a positive integer. IllegalArgumentException is
   *        thrown for non-positive parameters. This requests that the traversal
   *        method process no more than that number of documents in this batch.
   * @return The actual number of documents processed (may not be the same as
   *         the batch hint). Perhaps we should return a more complicated
   *         structure that allows for more interesting monitoring: number of
   *         successful docs, number failed, average size, etc.
   * @throws InterruptedException If the traversal perceives that an interrupt
   *         was requested and it is able to interrupt itself.
   */
  public int runBatch(int batchHint) throws InterruptedException;

}
