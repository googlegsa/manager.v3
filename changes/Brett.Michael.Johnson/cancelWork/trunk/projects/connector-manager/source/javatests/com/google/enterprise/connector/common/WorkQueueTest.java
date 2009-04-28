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

package com.google.enterprise.connector.common;

import junit.framework.TestCase;

/**
 * Test for WorkQueue class.
 */
public class WorkQueueTest extends TestCase {
  private static class PrintRunnable extends WorkQueueItem {
    private String str;
    public PrintRunnable(String str) {
      this.str = str;
    }
    @Override
    public void doWork() {
      System.out.println(str);
    }
    @Override
    public void cancelWork() {
      System.out.println("Cancelled: " + str);
    }
  }

  /**
   * Wait until the queue is empty (i.e. all work done).
   * @param queue the WorkQueue
   */
  private static void waitForEmptyQueue(WorkQueue queue) {
    while (queue.getWorkCount() > 0) {
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        fail("waitForEmptyQueue interrupted");
      }
    }
  }

  public void testAddWork() {
    WorkQueue queue = new WorkQueue(2);
    queue.init();
    for (int i = 0; i < 10; i++) {
      queue.addWork(new PrintRunnable("work started: " + i));
    }

    waitForEmptyQueue(queue);
    queue.shutdown(false);
  }

  public void testInitShutdown() {
    WorkQueue queue = new WorkQueue(2);
    queue.init();
    for (int i = 0; i < 10; i++) {
      queue.addWork(new PrintRunnable("work started: " + i));
    }
    queue.shutdown(false);
    queue.init();
    for (int i = 0; i < 10; i++) {
      queue.addWork(new PrintRunnable("work started again: " + i));
    }

    waitForEmptyQueue(queue);
    queue.shutdown(false);
  }

  public void testInterruptorFinish() {
    WorkQueue queue = new WorkQueue(2, 1000, 1000);
    queue.init();
    SlowWorkQueueItem workItem =
        new SlowWorkQueueItem("testInterruptorFinish", 500);
    queue.addWork(workItem);
    // Wait on work queue item
    workItem.waitForWorkToBeDone();
    assertTrue("Work was done", workItem.isDone);
    assertEquals("Work was not interrupted", 0, workItem.workInterruptedCount);
    queue.shutdown(false);
  }

  public void testInterruptorInterrupt() {
    WorkQueue queue = new WorkQueue(2, 3000, 1000);
    queue.init();
    SlowWorkQueueItem workItem =
        new SlowWorkQueueItem("testInterruptorInterrupt", 2000);
    queue.addWork(workItem);
    // Wait on work queue item
    workItem.waitForWorkToBeDone();
    assertTrue("Work was done", workItem.isDone);
    assertEquals("Work was interrupted once", 1, workItem.workInterruptedCount);
    assertTrue("Work was not cancelled", !workItem.isCancelled);
    queue.shutdown(false);
  }

  public void testInterruptorKill() {
    WorkQueue queue = new WorkQueue(2, 1000, 1000);
    queue.init();
    SlowWorkQueueItem workItem =
        new SlowWorkQueueItem("testInterrupterKill", 3000);
    queue.addWork(workItem);
    // Wait on work queue item
    workItem.waitForWorkToBeDone();
    assertTrue("Work was done", workItem.isDone);
    assertEquals("Work was interrupted twice", 2,
        workItem.workInterruptedCount);
    assertTrue("Work was cancelled", workItem.isCancelled);
    queue.shutdown(false);
  }

  private static class SlowWorkQueueItem extends WorkQueueItem {
    private String str;
    private long workDuration;
    private boolean isDone;
    private boolean isCancelled;
    private int workInterruptedCount;

    public SlowWorkQueueItem(String str, long workDurationMillis) {
      this.str = str;
      this.workDuration = workDurationMillis;
      this.isDone = false;
      this.isCancelled = false;
      this.workInterruptedCount = 0;
    }

    @Override
    public void doWork() {
      long start = System.currentTimeMillis();
      try {
        System.out.println(str + ": Starting work...");
        Thread.sleep(workDuration);
        System.out.println(str + ": ...finishing work.");
      } catch (InterruptedException e) {
        System.err.println(str + ": ...work has been interrupted...");
        workInterruptedCount++;
        long now = System.currentTimeMillis();
        if ((now - start) < workDuration) {
          System.out.println(str + ": ...continuing work...");
          try {
            Thread.sleep(workDuration - (now - start));
          } catch (InterruptedException e1) {
            System.err.println(str + ": ...work has been interrupted again...");
            workInterruptedCount++;
          }
        }
        Thread.currentThread().interrupt();
      } finally {
        isDone = true;
      }
    }

    @Override
    public void cancelWork() {
      isCancelled = true;
      System.err.println(str + ": ...work has been cancelled...");
    }

    public void waitForWorkToBeDone() {
      while (!isDone) {
        try {
          Thread.sleep(5);
        } catch (InterruptedException e) {
          fail("Unexpected interruption while waiting for work to be done.");
        }
      }
    }
  }
}
