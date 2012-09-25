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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchResultRecorder;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.BatchTimeout;
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
import com.google.enterprise.connector.traversal.Traverser;

import junit.framework.TestCase;

public class CancelableBatchTest extends TestCase {

  public void testRunBatchPositiveCount() throws Exception {
    runBatch(3, 4, TraversalDelayPolicy.IMMEDIATE);
  }

  public void testRunBatchErrorWait() throws Exception {
    runBatch(0, 4, TraversalDelayPolicy.ERROR);
  }

  public void testRunBatchNoWait() throws Exception {
    runBatch(0, 4, TraversalDelayPolicy.POLL);
  }

  private void runBatch(final int expectCount, final int batchHint,
      final TraversalDelayPolicy expectDelayPolicy) throws Exception {
    BatchSize batchSize = new BatchSize(batchHint, batchHint);
    BatchResult expectResult = new BatchResult(expectDelayPolicy, expectCount);
    MockTraverser traverser = new MockTraverser(batchSize, expectResult);
    MockBatchResultRecorder recorder = new MockBatchResultRecorder();
    MockBatchTimeout batchTimeout = new MockBatchTimeout();
    CancelableBatch batch =
        new CancelableBatch(traverser, "connector1", recorder, batchTimeout,
            batchSize);
    batch.run();
    BatchResult batchResult = recorder.getBatchResult();
    assertEquals(expectResult, batchResult);
    // TODO(strellis): Validate retryDelayMillis or remove it from
    // BatchRecorder.
    // TODO(bjohnson): Create tests where batchHint != batchMaximum, esp.
    // where returned batch has batchMaximum and greater than batchMaximum.
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
    private final BatchSize expectBatchSize;
    private final BatchResult batchResult;

    MockTraverser(BatchSize batchSize, BatchResult batchResult) {
      this.expectBatchSize = batchSize;
      this.batchResult = batchResult;
    }

    public void cancelBatch() {
      throw new UnsupportedOperationException();
    }

    public BatchResult runBatch(BatchSize batchSize) {
      assertEquals(expectBatchSize, batchSize);
      return batchResult;
    }
  }

  private static class MockBatchTimeout implements BatchTimeout {
    public void timeout() {
     throw new UnsupportedOperationException();
    }
  }
}
