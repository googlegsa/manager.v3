// Copyright 2006 Google Inc.  All Rights Reserved.
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
 * A mock query traverser that runs forever.
 */
public class NeverEndingQueryTraverser implements Traverser {

  // This is an instance variable to avoid findbugs noticing the infinite loop.
  public boolean breakLoop = true;

  public BatchResult runBatch(BatchSize batchSize) {
    // infinite loop
    while (breakLoop) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ie) {
        // do nothing
      }
    }
    return new BatchResult(TraversalDelayPolicy.IMMEDIATE, batchSize.getHint());
  }

  public void cancelBatch() {
    // do nothing
  }
}
