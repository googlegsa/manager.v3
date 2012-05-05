// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.pusher.MockFeedConnection;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.FileSizeLimitInfo;
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.SystemClock;

import junit.framework.TestCase;

/**
 * Test HostLoadManager class.
 */
public class HostLoadManagerTest extends TestCase {

  static Clock clock = new SystemClock(); // TODO: use a mock clock;

  // Wait until the current time is between the minimum and maximum
  // milliseconds of a second.  This can help us avoid running accross
  // 1-second boundaries and splitting results between two periods.
  private void alignTime(int min, int max) {
    while (true) {
      int now = (int)(clock.getTimeMillis() % 1000);
      if (now > min && now < max) {
        return;
      }
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        // Ignore.
      }
    }
  }

  private HostLoadManager newHostLoadManager(int load) {
    HostLoadManager hlm = new HostLoadManager(null, null, clock);
    hlm.setLoad(load);
    alignTime(50, 750);
    return hlm;
  }

  private BatchResult newBatchResult(int numDocs) {
    return newBatchResult(numDocs, 50);
  }

  private BatchResult newBatchResult(int numDocs, int duration) {
    long now = clock.getTimeMillis();
    return new BatchResult(TraversalDelayPolicy.IMMEDIATE, numDocs,
        now - duration, now);
  }

  public void testMaxFeedRateLimit() {
    HostLoadManager hostLoadManager = newHostLoadManager(60);
    assertEquals(60, hostLoadManager.determineBatchSize().getHint());
    hostLoadManager.recordResult(newBatchResult(60));
    assertEquals(0, hostLoadManager.determineBatchSize().getHint());
  }

  public void testMultipleUpdates() {
    HostLoadManager hostLoadManager = newHostLoadManager(60);
    BatchResult result = newBatchResult(10);
    hostLoadManager.recordResult(result);
    hostLoadManager.recordResult(result);
    hostLoadManager.recordResult(result);
    assertEquals(30, hostLoadManager.determineBatchSize().getHint());
  }

  public void testPeriod() {
    HostLoadManager hostLoadManager = newHostLoadManager(60);
    hostLoadManager.setPeriod(1);

    hostLoadManager.recordResult(newBatchResult(55));
    assertEquals(5, hostLoadManager.determineBatchSize().getHint());

    // sleep a period (and then some) so that batchHint is reset
    try {
      // extra time in ms in case sleeping the period is not long enough
      Thread.sleep(1200);
    } catch (InterruptedException e) {
      // Ignore.
    }

    assertEquals(60, hostLoadManager.determineBatchSize().getHint());
    hostLoadManager.recordResult(newBatchResult(15));
    assertEquals(45, hostLoadManager.determineBatchSize().getHint());
  }

  /**
   * Test that if we meet or exceed the host load limit, further
   * traversal should be delayed until the next traversal period.
   */
  public void testShouldDelay() {
    HostLoadManager hostLoadManager = newHostLoadManager(60);
    hostLoadManager.setPeriod(1);

    assertFalse(hostLoadManager.shouldDelay());
    hostLoadManager.recordResult(newBatchResult(60));
    assertTrue(hostLoadManager.shouldDelay());

    // Sleep more than 1 second, the LoadManager period, so that this
    // connector should be allowed to run again without delay.
    try {
      Thread.sleep(1250);
    } catch (InterruptedException e) {
      // Ignore.
    }
    assertFalse(hostLoadManager.shouldDelay());
  }

  /**
   * Test that if we have nearly reached the load level, we
   * won't OK a traversal requesting a tiny number of documents.
   */
  public void testShouldDelayTinyBatch() {
    HostLoadManager hostLoadManager = newHostLoadManager(60);
    hostLoadManager.setPeriod(1);

    assertFalse(hostLoadManager.shouldDelay());
    hostLoadManager.recordResult(newBatchResult(59));

    // We should delay rather than ask for a batch of 1.
    BatchSize batchSize = hostLoadManager.determineBatchSize();
    assertEquals(1, batchSize.getHint());
    assertTrue(hostLoadManager.shouldDelay());

    // Sleep more than 1 second, the LoadManager period, so that this
    // connector should be allowed to run again without delay.
    try {
      Thread.sleep(1250);
    } catch (InterruptedException e) {
      // Ignore.
    }
    assertFalse(hostLoadManager.shouldDelay());
  }

  /**
   * Test that if we returned a huge result that exceeds the load,
   * we will delay further traversals accordingly.
   * Regression for Issue 194.
   */
  public void testShouldDelayAfterHugeBatch() {
    HostLoadManager hostLoadManager = newHostLoadManager(50);
    hostLoadManager.setPeriod(1);

    assertFalse(hostLoadManager.shouldDelay());
    hostLoadManager.recordResult(newBatchResult(150));

    // We should delay, after grossly exceeding the load.
    assertTrue(hostLoadManager.shouldDelay());

    // Sleep more than 1 second, the LoadManager period, so that this
    // connector might be allowed to run again without delay.
    try {
      Thread.sleep(1250);
    } catch (InterruptedException e) {
      // Ignore.
    }

    // We should still delay, paying the penalty for grossly exceeding
    // the load previously.
    assertTrue(hostLoadManager.shouldDelay());

    // Sleep another couple of seconds, allowing the penalty to expire.
    // This will bring the average load over the elapsed traversal time
    // plus the penalty periods in line with the configured load.
    try {
      Thread.sleep(2500);
    } catch (InterruptedException e) {
      // Ignore.
    }

    // We should then be allowed to traverse again.
    assertFalse(hostLoadManager.shouldDelay());
  }

  /**
   * The new determineBatchSize logic treats both the hint and the load
   * as "suggestions", or "desired targets".  The traversal might
   * return more than the batchHint, and the number of docs processed in
   * a period might exceed the load.  This flexibility allows the connectors
   * to work more efficiently without expending a great deal of effort
   * trying to hit the batch size exactly.  However, poorly behaved
   * connectors could attempt to vastly exceed the recommended batch size,
   * so the BatchSize.maximum constraint puts a ceiling on the number of
   * documents that will be processed from the DocumentList.
   */
  public void testDetermineBatchSize() {
    HostLoadManager hostLoadManager = newHostLoadManager(60);
    BatchSize batchSize = hostLoadManager.determineBatchSize();
    assertEquals(60, batchSize.getHint());
    assertEquals(120, batchSize.getMaximum());
    hostLoadManager.setBatchSize(40);
    batchSize = hostLoadManager.determineBatchSize();
    assertEquals(40, batchSize.getHint());
    assertEquals(80, batchSize.getMaximum());
    hostLoadManager.setBatchSize(10);
    batchSize = hostLoadManager.determineBatchSize();
    assertEquals(10, batchSize.getHint());
    assertEquals(60, batchSize.getMaximum());
  }

  /**
   * Test that the throughput is spread out for long running batches.
   * For instance, if the period is 1 minute, and the load is 200;
   * then a traversal that took 4 minutes, but returned 200 documents
   * only averaged 50 docs per period.  That implies that only 50 docs
   * were returned in the current period, so the batch hint should be
   * 200 - 50 = 150.  Regression for Issue 189.
   */
  public void testAmortizeLongRunningBatch() {
    HostLoadManager hostLoadManager = newHostLoadManager(200);
    hostLoadManager.setPeriod(1);

    // We don't want to register the result for this test too close
    // to the beginning or end of a second.
    alignTime(450, 750);

    assertFalse(hostLoadManager.shouldDelay());
    hostLoadManager.recordResult(newBatchResult(200, 4000));

    // We should not delay, since we could still submit at least
    // another 150 docs in this period.
    assertFalse(hostLoadManager.shouldDelay());

    // The returned batch hint should be between 150 and 200,
    // depending upon the relation between aligned period and now.
    BatchSize batchSize = hostLoadManager.determineBatchSize();
    assertTrue((150 <= batchSize.getHint()) && (200 >= batchSize.getHint()));
  }

  /**
   * Test shouldDelay(void) with a low memory condition.
   */
  public void testShouldDelayLowMemory() {
    Runtime rt = Runtime.getRuntime();
    FileSizeLimitInfo fsli = new FileSizeLimitInfo();
    HostLoadManager hostLoadManager = new HostLoadManager(null, fsli, clock);

    // OK to start a traversal if there is plenty of memory for a new feed.
    rt.gc();
    fsli.setMaxFeedSize(rt.freeMemory()/100);
    assertFalse(hostLoadManager.shouldDelay());

    // Not OK to start a traversal if there is not enough memory for feeds.
    rt.gc();
    fsli.setMaxFeedSize(rt.maxMemory() - (rt.totalMemory() - rt.freeMemory()));
    assertTrue(hostLoadManager.shouldDelay());
  }

  /**
   * Test shouldDelay(void) with backlogged FeedConnection.
   */
  public void testShouldDelayFeedBacklogged() {
    BacklogFeedConnection feedConnection = new BacklogFeedConnection();
    HostLoadManager hostLoadManager = new HostLoadManager(feedConnection, null, clock);

    // OK to start a traversal if feedConnection is not backlogged.
    feedConnection.setBacklogged(false);
    assertFalse(hostLoadManager.shouldDelay());

    // Not OK to start a traversal if feedConnection is backlogged.
    feedConnection.setBacklogged(true);
    assertTrue(hostLoadManager.shouldDelay());
  }

  /**
   * A FeedConnection that can be backlogged.
   */
  private class BacklogFeedConnection extends MockFeedConnection {
    private boolean backlogged = false;

    public void setBacklogged(boolean backlogged) {
      this.backlogged = backlogged;
    }

    @Override
    public boolean isBacklogged() {
      return backlogged;
    }
  }
}
