// Copyright 2009 Google Inc.
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
import com.google.enterprise.connector.util.diffing.DiffingConnectorCheckpoint;
import com.google.enterprise.connector.util.diffing.DocumentSnapshotRepositoryMonitor;

import junit.framework.TestCase;

/**
 * Tests for {@link DocumentSnapshotRepositoryMonitor}.
 */
public class DocumentSnapshotRepositoryMonitorTest extends TestCase {

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
    /* @Override */
    public int getTraversalRate() {
      return load;
    }
  
    /** Returns the number of seconds to delay after finding no new content. */
    /* @Override */
    public int getRetryDelay() {
      return retryDelay;
    }
  
    /** Returns {@code true} if the traversal schedule is disabled. */
    /* @Override */
    public boolean isDisabled() {
      return isDisabled;
    }
  
    /**
     * Returns {@code true} if the current time is within a scheduled travesal
     * interval.
     */
    /* @Override */
    public boolean inScheduledInterval() {
      return inInterval;
    }
  
    /* @Override */
    public int nextScheduledInterval() {
      return -1;
    }

    /**
     * Returns {@code true} if traversals could run at this time,
     * equivalent to <pre>!isDisabled() && inScheduledInterval()</pre>.
     */
    /* @Override */
    public boolean shouldRun() {
      return !isDisabled() && inScheduledInterval();
    }
  }

  // This mock callback throws InterruptedException to catch off traversal pause.
  private class MockOffTraversalCallback
      implements DocumentSnapshotRepositoryMonitor.Callback {
    public void passBegin() {}

    /* @Override */
    public void changedDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {}

     /* @Override */
    public void deletedDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {}

    /* @Override */
    public void newDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {}

    /* @Override */
    public void passComplete(MonitorCheckpoint mcp)
        throws InterruptedException {}

    public boolean hasEnqueuedAtLeastOneChangeThisPass() {
      return true;
    }

    public void passPausing(int sleepms) throws InterruptedException {
      throw new InterruptedException();
    }
  }

  private void startTestTraversal(TraversalSchedule traversalSchedule,
      DocumentSnapshotRepositoryMonitor.Callback callback)
      throws InterruptedException, NullPointerException {

    DocumentSnapshotRepositoryMonitor monitor =
        new DocumentSnapshotRepositoryMonitor("nashi", null,
            null, callback, null, null, null);

    monitor.setTraversalSchedule(traversalSchedule);
    monitor.testTraversalSchedule();
  }

  public void testTraversalWithinSchedule() {
    // Testing shouldRun() case:
    // Referencing callback.passBegin should throw a NullPointerException.
    try {
      startTestTraversal(new MockTraversalSchedule(), null);
      fail("Expected NullPointerException, but got none.");
    } catch (InterruptedException e) {
      fail("Expected NullPointerException, but got InterruptedException instead.");
    } catch (NullPointerException e) {
      // Expected
    }
  }

  public void testTraversalOutOfSchedule() {
    // Testing !shouldRun() case:
    // Callback.passPausing() should throw an InterruptedException.
    try {
      startTestTraversal(new MockTraversalSchedule(500, -1, false, false),
          new MockOffTraversalCallback());
      fail("Expected InterruptedException, but got not.");
    } catch (InterruptedException e) {
      // Expected
    } catch (NullPointerException e) {
      fail("Expected InterruptedException, " +
          "but got NullPointerException instead.");
    }
  }
}
