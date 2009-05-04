// Copyright 2009 Google Inc.  All Rights Reserved.
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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link ThreadPool}
 * @author EricStrellis@gmail.com (Eric Strellis)
 *
 */
public class ThreadPoolTest extends TestCase {
  public void testRunOne() throws Exception {
    ThreadPool threadPool = new ThreadPool(ThreadPool.DEFAULT_MAXIMUM_TASK_LIFE_MILLIS);
    BlockingQueue<Object> q = new ArrayBlockingQueue<Object>(10);
    CancelableTask task =  new BlockingQueueCancelable(q);
    final String key = "k";
    threadPool.submit(key, task);
    assertTrue(threadPool.isRunning(key));
    take(1, q, 1000);
    verifyAllTasksCompleted(threadPool);
    assert(0 == task.getCancelCount());
  }

  public void testRunMany() throws Exception {
    final int count = 103;
    ThreadPool threadPool = new ThreadPool(ThreadPool.DEFAULT_MAXIMUM_TASK_LIFE_MILLIS);
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(count);
    List<CancelableTask> tasks = new ArrayList<CancelableTask>();
    for (int ix = 0; ix < count; ix++) {
      CancelableTask task = new BlockingQueueCancelable(taskRunningQ);
      tasks.add(task);
      threadPool.submit(makeKey(ix), task);
    }

    take(tasks.size(), taskRunningQ, 1000);
    verifyAllTasksCompleted(threadPool);
    assertCancelCount(0, tasks);
  }

  public void testRunTwoForOneKey() throws Exception {
    ThreadPool threadPool = new ThreadPool(ThreadPool.DEFAULT_MAXIMUM_TASK_LIFE_MILLIS);
    CancelableTask task1 = new WaitForInterruptCancelable();
    CancelableTask task2 = new WaitForInterruptCancelable();
    threadPool.submit("k", task1);
    try {
      threadPool.submit("k", task2);
      fail("should have failed to add two tasks with the same key");
    } catch (IllegalStateException ise) {
      // Expected
    }
    assertTrue(1 == threadPool.countCurrentTasks());
    assertEquals(0, task1.getCancelCount());
    assertEquals(0, task2.getCancelCount());
  }

  public void testCancel() throws Exception{
    final int count = 103;
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(count);
    BlockingQueue<Object> taskCanceledQ = new ArrayBlockingQueue<Object>(count);
    ThreadPool threadPool = new ThreadPool(ThreadPool.DEFAULT_MAXIMUM_TASK_LIFE_MILLIS);
    List<CancelableTask> tasks = new ArrayList<CancelableTask>();
    for (int ix = 0; ix < count; ix++) {
      CancelableTask task = new VerifyInterruptedCancelable(taskRunningQ, taskCanceledQ);
      tasks.add(task);
      threadPool.submit(makeKey(ix), task);
    }

    assertEquals(tasks.size(), threadPool.countCurrentTasks());
    take(tasks.size(), taskRunningQ, 1000);
    //Verify no task unblocks without being canceled.
    assertEquals(tasks.size(), threadPool.countCurrentTasks());
    assertEquals(0, taskCanceledQ.size());
    for (int ix = 0; ix < tasks.size(); ix++) {
      threadPool.cancel(makeKey(ix));
    }
    take(tasks.size(), taskCanceledQ, 1000);
    verifyAllTasksCompleted(threadPool);
    assertCancelCount(1, tasks);
  }

  public void testCancelHung() throws Exception{
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(10);
    ThreadPool threadPool = new ThreadPool(ThreadPool.DEFAULT_MAXIMUM_TASK_LIFE_MILLIS);
    final String KEY = makeKey(0);
    HangingCancelable task = new HangingCancelable(taskRunningQ);
    threadPool.submit(KEY, task);
    take(1, taskRunningQ, 1000);
    assertEquals(1, threadPool.countCurrentTasks());
    assertTrue(threadPool.isRunning(KEY));
    threadPool.cancel(KEY);
    verifyAllTasksCompleted(threadPool);
    assertEquals(1, task.getCancelCount());
    assertFalse(threadPool.isRunning(KEY));
  }

  private final long SHORT_TASK_LIFE_MILLIS = 100;
  public void testTimeToLiveWithSlowBatch() throws Exception {
    final int count = 13;
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(count);
    BlockingQueue<Object> taskCanceledQ = new ArrayBlockingQueue<Object>(count);
    ThreadPool threadPool = new ThreadPool(SHORT_TASK_LIFE_MILLIS);
    List<CancelableTask> tasks = new ArrayList<CancelableTask>();
    for (int ix = 0; ix < count; ix++) {
      CancelableTask task = new VerifyInterruptedCancelable(taskRunningQ, taskCanceledQ);
      tasks.add(task);
      threadPool.submit(makeKey(ix), task);
    }
    assertEquals(tasks.size(), threadPool.countCurrentTasks());
    assertCancelCount(0, tasks);
    take(tasks.size(), taskRunningQ, 1000);
    assertIsRunning(true, tasks.size(), threadPool);
    take(tasks.size(), taskCanceledQ, 1000);
    verifyAllTasksCompleted(threadPool);
    assertCancelCount(1, tasks);
    assertIsRunning(false, tasks.size(), threadPool);
  }

  public void testTimeToLiveWithHungBatch() throws Exception {
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(10);
    ThreadPool threadPool = new ThreadPool(SHORT_TASK_LIFE_MILLIS);
    HangingCancelable task = new HangingCancelable(taskRunningQ);
    String key = makeKey(0);
    threadPool.submit(key, task);
    take(1, taskRunningQ, 1000);
    assertTrue(1 == threadPool.countCurrentTasks());
    verifyAllTasksCompleted(threadPool);
    assertEquals(1, task.getCancelCount());
    assertFalse(threadPool.isRunning(key));
  }

  public void testHungTaskResume() throws Exception {
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(10);
    BlockingQueue<Object> resumeAfterInterruptQ = new ArrayBlockingQueue<Object>(10);
    BlockingQueue<Object> completedQ = new ArrayBlockingQueue<Object>(10);
    ThreadPool threadPool = new ThreadPool(SHORT_TASK_LIFE_MILLIS);
    final String KEY = makeKey(0);
    ResumeAfterHangCancelable task =
      new ResumeAfterHangCancelable(taskRunningQ, resumeAfterInterruptQ, completedQ);
    threadPool.submit(KEY, task);
    take(1, taskRunningQ, 1000);
    assertTrue(threadPool.isRunning(KEY));
    assertEquals(1, threadPool.countCurrentTasks());
    //
    //Wait for ThreadPool to abandon the hanging task.
    verifyAllTasksCompleted(threadPool);
    //
    //Our cancel counts as a completion so onCompletion gets run. This is
    //odd because in this test task1 is really hung and will resume to a
    //second completion.
    take(1, completedQ, 1000);

    //verify the task has been abandoned.
    assertFalse(threadPool.isRunning(KEY));
    //verify the abandoned task is still running.
    assertTrue(task.isRunning());
    CancelableTask task2 = new ResumeAfterHangCancelable(taskRunningQ, resumeAfterInterruptQ, completedQ);
    //submit a new task with the same key as the abandoned one.
    threadPool.submit(KEY, task2);
    take(1, taskRunningQ, 1000);
    assertTrue(threadPool.isRunning(KEY));

    assertEquals(1, threadPool.countCurrentTasks());

    //resume the abandoned task
    resumeAfterInterruptQ.add(this);
    //wait for the abandoned task to exit and verify task 1 is still considered running
    take(1, completedQ, 1000);
    assertTrue(threadPool.isRunning(KEY));

    verifyAllTasksCompleted(threadPool);
    assertFalse(threadPool.isRunning(KEY));
    Thread.sleep(100);
  }

  public void testShutdown() throws Exception {
    final int count = 9;
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(count);
    BlockingQueue<Object> taskCanceledQ = new ArrayBlockingQueue<Object>(count);
    ThreadPool threadPool = new ThreadPool(ThreadPool.DEFAULT_MAXIMUM_TASK_LIFE_MILLIS);

    List<CancelableTask> tasks = new ArrayList<CancelableTask>();
    for (int ix = 0; ix < count; ix++) {
      CancelableTask task = new VerifyInterruptedCancelable(taskRunningQ, taskCanceledQ);
      tasks.add(task);
      threadPool.submit(makeKey(ix), task);
    }
    assertEquals(tasks.size(), threadPool.countCurrentTasks());
    take(tasks.size(), taskRunningQ, 1000);
    assertIsRunning(true, tasks.size(), threadPool);
    assertTrue(threadPool.shutdown(true, 1000));
    take(tasks.size(), taskCanceledQ, 1000);
    verifyAllTasksCompleted(threadPool);
    assertIsRunning(false, tasks.size(), threadPool);
    assertCancelCount(0, tasks);
  }

  public void testShutdownWithHung() throws Exception {
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(10);
    ThreadPool threadPool = new ThreadPool(ThreadPool.DEFAULT_MAXIMUM_TASK_LIFE_MILLIS);
    final String KEY = makeKey(0);
    CancelableTask task = new HangingCancelable(taskRunningQ);
    threadPool.submit(KEY, task);
    take(1, taskRunningQ, 1000);
    assertTrue(1 == threadPool.countCurrentTasks());
    assertFalse(threadPool.shutdown(true, 100));
    verifyAllTasksCompleted(threadPool);
    assertEquals(0, task.getCancelCount());
  }

  public void testSubmitAfterShutdown() throws Exception {
    ThreadPool threadPool = new ThreadPool(ThreadPool.DEFAULT_MAXIMUM_TASK_LIFE_MILLIS);
    threadPool.shutdown(true, 10);
  }


  private String makeKey(int ix) {
    return "k" + ix;
  }

  private void verifyAllTasksCompleted(ThreadPool threadPool) throws Exception {
    long timeToGiveUp = System.currentTimeMillis() + 3000;
    while (System.currentTimeMillis() < timeToGiveUp) {
      if (threadPool.countCurrentTasks() == 0) {
        return;
      }
      Thread.sleep(10);
    }
    fail("Some background tasks did not complete");
  }

  private void take(int count, BlockingQueue<?> q, int timeoutMillis) throws InterruptedException {
    for (int ix = 0; ix < count; ix++) {
      Object result = q.poll(timeoutMillis, TimeUnit.MILLISECONDS);
      if (result == null) {
        fail("Expected object not written to queue - this means a backgound task hung or failed");
      }
    }
  }

  private void assertIsRunning(boolean isRunning, int count, ThreadPool threadPool) {
    for(int ix = 0; ix < count; ix++) {
      assertEquals(isRunning, threadPool.isRunning(makeKey(ix)));
    }
  }

  private void assertCancelCount(int expectCount, List<CancelableTask> tasks) {
    for (CancelableTask task : tasks) {
      assertEquals(expectCount, task.getCancelCount());
    }
  }

  private static class BlockingQueueCancelable extends CancelableTask{
    private final BlockingQueue<Object> q;

    BlockingQueueCancelable(BlockingQueue<Object> q) {
      this.q = q;
    }
    @Override
    public void run() {
      try {
        q.put(this);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static class WaitForInterruptCancelable extends CancelableTask {
    @Override
    public void run() {
      synchronized (this) {
          try {
            wait();
          } catch (InterruptedException ie) {
            //Ignored
          }
      }
    }
  }

  private static class HangingCancelable extends CancelableTask {
    //Cancelable writes to this Q so test can block Cancelable is running.
    private final BlockingQueue<Object> taskRunningQ;

    HangingCancelable(BlockingQueue<Object> taskRunningQ) {
      this.taskRunningQ = taskRunningQ;
    }
    @Override
    public void run() {
     taskRunningQ.add(this);
       synchronized (this) {
        while (true) {
          try {
            wait();
          } catch (InterruptedException ie) {
            //Ignored so I hang.
          }
        }
      }
    }
  }

  private static class VerifyInterruptedCancelable extends CancelableTask {
    //Cancelable writes to this Q so test can block Cancelable is running.
    private final BlockingQueue<Object> taskRunningQ;
    //Cancelable writes to this after interrupt so test can verify interrupt occurred
    private final BlockingQueue<Object> taskCanceledQ;

    VerifyInterruptedCancelable(BlockingQueue<Object> taskRunningQ,
        BlockingQueue<Object> taskCanceledQ) {
      this.taskRunningQ = taskRunningQ;
      this.taskCanceledQ = taskCanceledQ;
    }

    @Override
    public void run() {
      try {
        taskRunningQ.add(this);
        Thread.sleep(10000);
      } catch (InterruptedException ie) {
        // Expected
      }
      taskCanceledQ.add(this);
    }
  }

  private static class ResumeAfterHangCancelable extends CancelableTask  implements Completable {
    private volatile boolean isRunning;

    // Cancelable writes to this Q so test can block Cancelable is running.
    private final BlockingQueue<Object> taskRunningQ;

    // Cancelable reads this Q to block until test is ready for Cancelable to
    // complete
    private final BlockingQueue<Object> resumeAfterInterruptQ;

    //
    // Cancelable writes this Q when done so test can read it.
    private final BlockingQueue<Object> completedQ;


    ResumeAfterHangCancelable(BlockingQueue<Object> taskRunningQ,
        BlockingQueue<Object> resumeAfterInterruptQ,
        BlockingQueue<Object> completedQ) {
      this.taskRunningQ = taskRunningQ;
      this.resumeAfterInterruptQ = resumeAfterInterruptQ;
      this.completedQ = completedQ;
    }

    boolean isRunning() {
      return isRunning;
    }

    void setIsRunning(boolean isRunning) {
      this.isRunning = isRunning;
    }

    @Override
    public void run() {
      setIsRunning(true);
      try {
        try {
          taskRunningQ.add(this);
          synchronized (this) {
            wait();
          }
        } catch (InterruptedException ie) {
          // Expected
        }

        try {
          resumeAfterInterruptQ.poll(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }

      } finally {
        setIsRunning(false);
      }
    }

    @Override
    public void onCompletion() {
      completedQ.add(this);
    }
  }

  private abstract static class CancelableTask implements Cancelable {
    private int cancelCount;

    public int getCancelCount() {
      return cancelCount;
    }

    @Override
    public void cancel() {
      cancelCount++;
    }
  }
}
