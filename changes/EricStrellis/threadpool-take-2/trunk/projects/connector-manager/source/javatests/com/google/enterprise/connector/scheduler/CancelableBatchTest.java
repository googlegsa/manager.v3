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

package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
import com.google.enterprise.connector.traversal.Traverser;

import junit.framework.TestCase;

public class CancelableBatchTest extends TestCase {

  public void testRunBatchPositiveCount() throws Exception {
    runBatch(3, 3, 4, TraversalDelayPolicy.IMMEDIATE);
  }

  public void testRunBatchErrorWait() throws Exception {
    runBatch(Traverser.ERROR_WAIT, 0, 4, TraversalDelayPolicy.ERROR);
  }

  public void testRunBatchNoWait() throws Exception {
    runBatch(Traverser.POLLING_WAIT, 0, 4, TraversalDelayPolicy.POLL);
  }

  private void runBatch(final int legacyResult, final int expectCount,
      final int batchHint, final TraversalDelayPolicy expectDelayPolicy)
      throws Exception {
    MockTraverser traverser = new MockTraverser(batchHint, legacyResult);
    MockBatchResultRecorder recorder = new MockBatchResultRecorder();
    CancelableBatch batch =
        new CancelableBatch(traverser, "connector1", recorder, batchHint);
    batch.run();
    BatchResult batchResult = recorder.getBatchResult();
    assertEquals(new BatchResult(expectDelayPolicy, expectCount), batchResult);
    // TODO(strellis): Validate retryDelayMillis or remove it from
    // BtchRecorder.
  }

  private static class MockBatchResultRecorder implements BatchResultRecorder {
    private BatchResult myResult;

    public void recordResult(BatchResult result) {
      if (myResult != null) {
        throw new IllegalStateException("Cant set batch result twice");
      }
      myResult = result;
    }

    BatchResult getBatchResult() {
      return myResult;
    }
  }

  private static class MockTraverser implements Traverser {
    private final int expectBatchHint;
    private final int legacyResult;

    MockTraverser(int expectBatchHint, int legacyResult) {
      this.expectBatchHint = expectBatchHint;
      this.legacyResult = legacyResult;
    }

    public void cancelBatch() {
      throw new UnsupportedOperationException();
    }

    public int runBatch(int batchHint) {
      assertEquals(expectBatchHint, batchHint);
      return legacyResult;
    }
  }
}
