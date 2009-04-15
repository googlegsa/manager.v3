// Copyright 2008-2009 Google Inc. All Rights Reserved.
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
 * Test for WorkQueueThread class.
 */
public class WorkQueueThreadTest extends TestCase {
  private WorkQueue queue;

  @Override
  protected void setUp() throws Exception {
    queue = new WorkQueue(2);
    queue.init();
  }

  @Override
  protected void tearDown() throws Exception {
    queue.shutdown(false);
  }

  public void testIsWorking() {
    TestItem testItem = new TestItem("testIsWorking");
    queue.addWork(testItem);
    testItem.waitForWorkToBeStarted();
    WorkQueueThread workingThread = testItem.getWorkQueueThread();
    assertTrue("Thread is working", workingThread.isWorking());
    testItem.finishWork();
    testItem.waitForWorkToBeDone();
    assertTrue("Thread is not working", !workingThread.isWorking());
    assertTrue("Work was started", testItem.workIsStarted);
    assertTrue("Wark was not interrupted", !testItem.workIsInterrupted);
    assertTrue("Work was done", testItem.workIsDone);
  }

  public void testInterrupt() {
    TestItem testItem = new TestItem("testInterrupt");
    queue.addWork(testItem);
    testItem.waitForWorkToBeStarted();
    WorkQueueThread workingThread = testItem.getWorkQueueThread();
    workingThread.interrupt();
    testItem.waitForWorkToBeInterrupted();
    assertTrue("Thread is alive", workingThread.isAlive());
    assertTrue("Work was started", testItem.workIsStarted);
    assertTrue("Work was interrupted", testItem.workIsInterrupted);
    assertTrue("Work was not done", !testItem.workIsDone);
  }

  public void testInterruptAndKill() {
    TestItem testItem = new TestItem("testInterruptAndKill");
    queue.addWork(testItem);
    testItem.waitForWorkToBeStarted();
    WorkQueueThread workingThread = testItem.getWorkQueueThread();
    workingThread.interruptAndKill();
    testItem.waitForWorkToBeInterrupted();
    // Note, this is somewhat of a race before the LifeThread restarts.
    assertTrue("Thread is not alive", !workingThread.isAlive());
    assertTrue("Work was started", testItem.workIsStarted);
    assertTrue("Work was interrupted", testItem.workIsInterrupted);
    assertTrue("Work was not done", !testItem.workIsDone);
    assertTrue("Work was not cancelled", !testItem.workIsCancelled);
  }

  private static class TestItem extends WorkQueueItem {
    private String str;
    private boolean isFinished;
    private boolean workIsStarted;
    private boolean workIsDone;
    private boolean workIsInterrupted;
    private boolean workIsCancelled;

    public TestItem(String str) {
      this.str = str;
      isFinished = false;
      workIsStarted = false;
      workIsDone = false;
      workIsInterrupted = false;
      workIsCancelled = false;
    }

    public void finishWork() {
      this.isFinished = true;
      synchronized (this) {
        notifyAll();
      }
    }

    @Override
    public void doWork() {
      synchronized (this) {
        while (!isFinished) {
          try {
            workIsStarted = true;
            System.out.println(str + ": Starting work...");
            wait();
            System.out.println(str + ": ...finishing work");
            workIsDone = true;
          } catch (InterruptedException e) {
            System.out.println(str + ": ...work has been interrupted...");
            workIsInterrupted = true;
            // NOTE: Perpetuate the interrupted state so the WorkQueueThread
            // knows it's been interrupted and won't drop into wait state.
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    }

    @Override
    public void cancelWork() {
      workIsCancelled = true;
      System.out.println(str + ": ...work has been cancelled...");
    }

    public void waitForWorkToBeStarted() {
      while (!workIsStarted) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          fail("Unexpected interruption while waiting for work to be started.");
        }
      }
    }

    public void waitForWorkToBeDone() {
      while (!workIsDone) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          fail("Unexpected interruption while waiting for work to be done.");
        }
      }
    }

    public void waitForWorkToBeInterrupted() {
      while (!workIsInterrupted) {
        try {
          // Note, sleep time should be short enough to insure the LifeThread
          // won't have time to restart the WorkQueueThread before it's state
          // can be checked.
          Thread.sleep(5);
        } catch (InterruptedException e) {
          fail("Unexpected interruption while waiting for interrupt.");
        }
      }
    }
  }
}
