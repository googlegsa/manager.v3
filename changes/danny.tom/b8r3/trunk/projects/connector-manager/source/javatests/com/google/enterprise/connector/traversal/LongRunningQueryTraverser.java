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
 * A mock query traverser that takes a long time to run.
 */
public class LongRunningQueryTraverser implements Traverser {

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.traversal.Traverser#runBatch(int)
   */
  public int runBatch(int batchHint, QueryTraverserMonitor monitor) {
    long sleepTime = 60 * 1000;
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return batchHint;
  }

}
