// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.util.diffing;

import com.google.enterprise.connector.spi.TraversalSchedule;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Tests for {@link DocumentSnapshotRepositoryMonitorManagerImpl}.
 */
public class DocumentSnapshotRepositoryMonitorManagerImplTest extends TestCase {

  // Borrowed from filesystem connector's  MockTraversalSchedule.java
  public class MockTraversalSchedule implements TraversalSchedule {
    private final int load;
    private final int retryDelay;
    private final boolean isDisabled;
    private final boolean inInterval;
  
    public MockTraversalSchedule() {
      this(500, -1, false, true);
    }
  
    public MockTraversalSchedule(int load, int retryDelay, boolean isDisabled,
                                 boolean inInterval) {
      this.load = load;
      this.retryDelay = retryDelay;
      this.isDisabled = isDisabled;
      this.inInterval = inInterval;
    }
  
    /** Returns the target traversal rate in documents per minute. */
    @Override
    public int getTraversalRate() {
      return load;
    }
  
    /** Returns the number of seconds to delay after finding no new content. */
    @Override
    public int getRetryDelay() {
      return retryDelay;
    }
  
    /** Returns {@code true} if the traversal schedule is disabled. */
    @Override
    public boolean isDisabled() {
      return isDisabled;
    }
  
    /**
     * Returns {@code true} if the current time is within a scheduled traversal
     * interval.
     */
    @Override
    public boolean inScheduledInterval() {
      return inInterval;
    }
  
    @Override
    public int nextScheduledInterval() {
      return -1;
    }

    /**
     * Returns {@code true} if traversals could run at this time,
     * equivalent to <pre>!isDisabled() && inScheduledInterval()</pre>.
     */
    @Override
    public boolean shouldRun() {
      return !isDisabled() && inScheduledInterval();
    }
  }

  /**
   * This test verifies that the monitor manager can change sleepInterval
   * in {@link ChangeQueue} through setTraversalSchedule.
   */
  public void testSetTraversalSchedule() {
    ChangeQueue queue;
    queue = new ChangeQueue(10, 0L, null);

    DocumentSnapshotRepositoryMonitorManagerImpl manager =
        new DocumentSnapshotRepositoryMonitorManagerImpl(
            new ArrayList<SnapshotRepository<? extends DocumentSnapshot>>(),
            null, null, null, queue, null);

    assertEquals(0, queue.getSleepInterval());
    manager.setTraversalSchedule(new MockTraversalSchedule(500, 8074,
        false, false));
    assertEquals(8074000, queue.getSleepInterval());
  }
}
