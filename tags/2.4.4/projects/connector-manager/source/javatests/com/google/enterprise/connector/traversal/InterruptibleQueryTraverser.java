// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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

import java.util.Date;

/**
 * A Traverser that runs forever but can be interrupted once/second
 */
public class InterruptibleQueryTraverser implements Traverser {

  public BatchResult runBatch(BatchSize ignored) {
    int counter = 0;
    boolean breakLoop = true;
    boolean interrupted = false;
    // infinite loop
    while (breakLoop) {
      long startTime = new Date().getTime();
      long now = startTime;
      while ((now - startTime) < (2 * 1000)) {
        // this inner loop simulates a call to a CMS that takes a while to
        // return and can't be interrupted
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ie) {
          // Here be dragons: the fact that an InterruptedException was thrown
          // does NOT mean that if you then call Thread.interrupted() it returns
          // true! That's why we have to remember that we were interrupted in
          // order to exit the outer loop at the break below.
          interrupted = true;
        }
        now = new Date().getTime();
      }
      counter++;
      if (interrupted || Thread.interrupted()) {
        break;
      }
    }
    return new BatchResult(TraversalDelayPolicy.IMMEDIATE, counter);
  }

  public void cancelBatch() {
    throw new UnsupportedOperationException("Should not get cancelled");
  }
}
