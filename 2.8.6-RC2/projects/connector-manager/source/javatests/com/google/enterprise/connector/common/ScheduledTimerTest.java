// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.common;

import junit.framework.TestCase;

public class ScheduledTimerTest extends TestCase {
  private ScheduledTimer timer;

  @Override
  protected void setUp() {
    // Try to wait for the timer thread to shutdown, but don't take
    // too long. A test failure is better than a hanging test.
    for (int i = 0; isThreadRunning() && i < 10; i++) {
      try { Thread.sleep(20); } catch (InterruptedException e) {}
    }

    assertFalse(isThreadRunning());
    timer = new ScheduledTimer();
  }

  @Override
  protected void tearDown() {
    timer.cancel();
  }

  private boolean isThreadRunning() {
    Thread[] threads = new Thread[Thread.activeCount() * 2];
    int count = Thread.enumerate(threads);
    assertTrue("Missing threads", count < threads.length);
    for (Thread t : threads) {
      if (t != null && t.getName().equals(ScheduledTimer.THREAD_NAME)) {
          return true;
      }
    }
    return false;
  }

  /** Helper method to schedule a task and check the timer thread. */
  private void testTask(long delay, long period, boolean isTriggered,
      boolean isThreadRunning) {
    MockScheduledTimerTask task =
        new MockScheduledTimerTask(delay, period);
    timer.schedule(task);
    assertEquals(isTriggered, task.isTriggered());
    assertEquals(isThreadRunning, isThreadRunning());
  }

  /** Tests that there is no timer thread unless there are scheduled tasks. */
  public void testNoScheduledTasks() {
    assertFalse(isThreadRunning());
  }

  /**
   * Tests that a task with no delay will be run immediately and not
   * be scheduled in the underlying Timer.
   */
  public void testNoFutureTasks() {
    testTask(0L, 0L, true, false);
  }

  /**
   * Tests a task that will execute immediately and be scheduled for
   * the future.
   */
  public void testOnceAndFutureTask() {
    testTask(0L, 60L, true, true);
  }

  /**
   * Tests that a task scheduled to execute in the future will start
   * the timer thread.
   */
  public void testFutureTask() {
    testTask(60L, 60L, false, true);
  }

  /**
   * Tests a period of zero. ScheduledTimer uses the
   * Timer.schedule(TimerTask, long delay) overload in that case.
   */
  public void testZeroPeriodTask() {
    testTask(60L, 0L, false, true);
  }

  /**
   * Tests a negative period. ScheduledTimer uses the
   * Timer.schedule(TimerTask, long delay) overload in that case.
   */
  public void testNegativePeriodTask() {
    testTask(60L, -60L, false, true);
  }
}
