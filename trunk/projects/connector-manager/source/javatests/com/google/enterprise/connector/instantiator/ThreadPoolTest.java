// Copyright 2009 Google Inc. 
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

import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.SystemClock;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link ThreadPool}
 */
public class ThreadPoolTest extends TestCase {
  /**
   * A suggested default amount of time to let tasks run before automatic
   * cancellation.
   */
  public static final int DEFAULT_TASK_LIFE_SECS = 60;

  private Clock clock = new SystemClock(); // TODO: use a mock clock?

  // TODO(strellis): Add test of cancel timer popping during submit - after the
  // timer is running and before the task is running.
  public void testRunOne() throws Exception {
    ThreadPool threadPool = new ThreadPool(DEFAULT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);
    BlockingQueue<Object> runningQ = new ArrayBlockingQueue<Object>(10);
    BlockingQueue<Object> stoppingQ = new ArrayBlockingQueue<Object>(10);
    CancelableTask task = new BlockingQueueCancelable(runningQ, stoppingQ);
    TaskHandle taskHandle = threadPool.submit(task);
    take(1, runningQ);
    assertTrue(!taskHandle.isDone());
    stoppingQ.put(this);
    verifyCompleted(taskHandle);
    assert (0 == task.getCancelCount());
    assertEquals(0, task.getTimeoutCount());

  }

  public void testRunMany() throws Exception {
    final int count = 103;
    ThreadPool threadPool = new ThreadPool(DEFAULT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(count);
    BlockingQueue<Object> taskStoppingQ = new ArrayBlockingQueue<Object>(count);
    List<CancelableTask> tasks = new ArrayList<CancelableTask>();
    List<TaskHandle> taskHandles = new ArrayList<TaskHandle>();
    for (int ix = 0; ix < count; ix++) {
      CancelableTask task =
          new BlockingQueueCancelable(taskRunningQ, taskStoppingQ);
      tasks.add(task);
      taskHandles.add(threadPool.submit(task));
    }
    take(tasks.size(), taskRunningQ);
    verifyRunning(taskHandles);
    put(tasks.size(), taskStoppingQ);
    verifyCompleted(taskHandles);
    assertCancelCount(0, tasks);
    assertTimeoutCount(0, tasks);
  }

  public void testCancel() throws Exception {
    final int count = 103;
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(count);
    BlockingQueue<Object> taskCanceledQ = new ArrayBlockingQueue<Object>(count);
    ThreadPool threadPool = new ThreadPool(DEFAULT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);
    List<CancelableTask> tasks = new ArrayList<CancelableTask>();
    List<TaskHandle> handles = new ArrayList<TaskHandle>();
    for (int ix = 0; ix < count; ix++) {
      CancelableTask task =
          new VerifyInterruptedCancelable(taskRunningQ, taskCanceledQ);
      tasks.add(task);
      handles.add(threadPool.submit(task));
    }

    take(tasks.size(), taskRunningQ);
    // Verify no task unblocks without being canceled.
    verifyRunning(handles);
    assertEquals(0, taskCanceledQ.size());
    for (TaskHandle handle : handles) {
      handle.cancel();
    }
    take(tasks.size(), taskCanceledQ);
    verifyCompleted(handles);
    assertCancelCount(1, tasks);
    assertTimeoutCount(0, tasks);
  }

  public void testTimeoutHung() throws Exception {
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(10);
    ThreadPool threadPool = new ThreadPool(DEFAULT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);
    HangingCancelable task = new HangingCancelable(taskRunningQ);
    TaskHandle handle = threadPool.submit(task);
    take(1, taskRunningQ);
    assertFalse(handle.isDone());
    assertFalse(task.isExiting());
    handle.cancel();
    verifyCompleted(handle);
    assertEquals(1, task.getCancelCount());
    assertFalse(task.isExiting());
  }

  public void testShutdown() throws Exception {
    final int count = 9;
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(count);
    BlockingQueue<Object> taskCanceledQ = new ArrayBlockingQueue<Object>(count);
    ThreadPool threadPool = new ThreadPool(DEFAULT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);

    List<CancelableTask> tasks = new ArrayList<CancelableTask>();
    List<TaskHandle> handles = new ArrayList<TaskHandle>();
    for (int ix = 0; ix < count; ix++) {
      CancelableTask task =
          new VerifyInterruptedCancelable(taskRunningQ, taskCanceledQ);
      tasks.add(task);
      handles.add(threadPool.submit(task));
    }
    take(tasks.size(), taskRunningQ);
    verifyRunning(handles);
    assertTrue(threadPool.shutdown(true, 1000));
    take(tasks.size(), taskCanceledQ);
    verifyCompleted(handles);
    assertCancelCount(0, tasks);
    assertTimeoutCount(0, tasks);
  }

  public void testShutdownWithHung() throws Exception {
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(10);
    ThreadPool threadPool = new ThreadPool(DEFAULT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);
    HangingCancelable task = new HangingCancelable(taskRunningQ);
    TaskHandle handle = threadPool.submit(task);
    take(1, taskRunningQ);
    assertFalse(handle.isDone());
    assertFalse(threadPool.shutdown(true, 100));
    // TODO(strellis): Shutdown seems to interrupt the task but not set its
    // isDone state. Since the task is hung, perhaps this is OK. If it causes
    // a problem we will need to explicitly cancel the task during shutdown.
    assertFalse(task.isExiting());
    assertEquals(0, task.getCancelCount());
    assertEquals(0, task.getTimeoutCount());
  }

  public void testSubmitAfterShutdown() throws Exception {
    ThreadPool threadPool = new ThreadPool(DEFAULT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);
    threadPool.shutdown(true, 10);
    BlockingQueue<Object> runningQ = new ArrayBlockingQueue<Object>(10);
    BlockingQueue<Object> stoppingQ = new ArrayBlockingQueue<Object>(10);
    CancelableTask task = new BlockingQueueCancelable(runningQ, stoppingQ);
    TaskHandle handle = threadPool.submit(task);
    assertNull(handle);
  }

  private final static int SHORT_TASK_LIFE_SECS = 1;

  public void testTimeToLiveWithHungBatch() throws Exception {
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(10);
    ThreadPool threadPool = new ThreadPool(SHORT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);
    HangingCancelable task = new HangingCancelable(taskRunningQ);
    TaskHandle taskHandel = threadPool.submit(task);
    take(1, taskRunningQ);
    verifyCompleted(taskHandel);
    assertFalse(task.isExiting());
    assertEquals(1, task.getCancelCount());
    assertEquals(1, task.getTimeoutCount());
  }

  public void testTimeToLiveWithSlowBatch() throws Exception {
    final int count = 2;
    BlockingQueue<Object> taskRunningQ = new ArrayBlockingQueue<Object>(count);
    BlockingQueue<Object> taskCanceledQ = new ArrayBlockingQueue<Object>(count);
    ThreadPool threadPool = new ThreadPool(SHORT_TASK_LIFE_SECS,
        clock /* TODO: Use mock clock? */);

    List<VerifyInterruptedCancelable> tasks =
        new ArrayList<VerifyInterruptedCancelable>();
    List<TaskHandle> handles = new ArrayList<TaskHandle>();
    for (int ix = 0; ix < count; ix++) {
      VerifyInterruptedCancelable task =
          new VerifyInterruptedCancelable(taskRunningQ, taskCanceledQ);
      tasks.add(task);
      handles.add(threadPool.submit(task));
    }
    take(tasks.size(), taskRunningQ);
    take(tasks.size(), taskCanceledQ);
    verifyCompleted(handles);
    assertCancelCount(1, tasks);
    assertTimeoutCount(1, tasks);
    assertIsExiting(true, tasks);
  }

  private void assertIsExiting(boolean expect,
      List<VerifyInterruptedCancelable> tasks) throws InterruptedException{
    for (VerifyInterruptedCancelable task : tasks) {
      long timeToGiveUp = clock.getTimeMillis() + 3000;
      while (clock.getTimeMillis() < timeToGiveUp) {
        if (task.isExiting() == expect) {
          return;
        }
        Thread.sleep(10);
      }
      assertEquals(expect, task.isExiting());
    }
  }

  private void verifyCompleted(TaskHandle taskHandle)
      throws InterruptedException {
    long timeToGiveUp = clock.getTimeMillis() + 3000;
    while (clock.getTimeMillis() < timeToGiveUp) {
      if (taskHandle.isDone()) {
        return;
      }
      Thread.sleep(10);
    }
    fail("Some background tasks did not complete");
  }

  private void verifyCompleted(List<TaskHandle> tasks)
      throws InterruptedException {
    for (TaskHandle task : tasks) {
      verifyCompleted(task);
    }
  }

  private void verifyRunning(List<TaskHandle> tasks) {
    for (TaskHandle task : tasks) {
      assertTrue(!task.isDone());
    }
  }

  /**
   * Take a value from a queue. For debugging purposes the timeout can be set
   * with the system property "TAKE_WAIT_MILLIS".
   * @param count
   * @param q
   * @throws InterruptedException
   */
  private void take(int count, BlockingQueue<?> q)
      throws InterruptedException {
    long timeoutMillis = Long.getLong("TAKE_WAIT_MILLIS", 2000);
    for (int ix = 0; ix < count; ix++) {
      Object result = q.poll(timeoutMillis, TimeUnit.MILLISECONDS);
      if (result == null) {
        fail("Expected object not written to queue "
            + "- this means a backgound task hung or failed");
      }
    }
  }

  private void put(int count, BlockingQueue<Object> q)
      throws InterruptedException {
    for (int ix = 0; ix < count; ix++) {
      q.put(this);
    }
  }

  private void assertCancelCount(int expectCount,
      List<? extends CancelableTask> tasks) {
    for (CancelableTask task : tasks) {
      assertEquals(expectCount, task.getCancelCount());
    }
  }

  private void assertTimeoutCount(int expectCount,
      List<? extends CancelableTask> tasks) {
    for (CancelableTask task : tasks) {
      assertEquals(expectCount, task.getTimeoutCount());
    }
  }

  private static class BlockingQueueCancelable extends CancelableTask {
    // Written by me.
    private final BlockingQueue<Object> runningQ;
    // Written by the test.
    private final BlockingQueue<Object> stoppingQ;

    BlockingQueueCancelable(BlockingQueue<Object> runningQ,
        BlockingQueue<Object> stoppingQ) {
      this.runningQ = runningQ;
      this.stoppingQ = stoppingQ;
    }

    public void run() {
      try {
        runningQ.put(this);
        stoppingQ.take();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static class HangingCancelable extends CancelableTask {
    // Cancelable writes to this Q so test can block Cancelable is running.
    private final BlockingQueue<Object> taskRunningQ;
    private volatile boolean isExiting = false;
    // This is an instance variable so findbugs wont issue a diagnostic for
    // hang.
    public boolean dontChangeMe = true;

    HangingCancelable(BlockingQueue<Object> taskRunningQ) {
      this.taskRunningQ = taskRunningQ;
    }

    public void run() {
      try {
        taskRunningQ.add(this);
        synchronized (this) {

          while (dontChangeMe) {
            try {
              wait();
            } catch (InterruptedException ie) {
              // Ignored so I hang.
            }
          }
        }
      } finally {
        isExiting = true;
      }
    }

    boolean isExiting() {
      return isExiting;
    }
  }

  private static class VerifyInterruptedCancelable extends CancelableTask {
    // Cancelable writes to this Q so test can block Cancelable is running.
    private final BlockingQueue<Object> taskRunningQ;
    // Cancelable writes to this after interrupt so test can verify interrupt
    // occurred
    private final BlockingQueue<Object> taskCanceledQ;
    private volatile boolean isExiting = false;

    VerifyInterruptedCancelable(BlockingQueue<Object> taskRunningQ,
        BlockingQueue<Object> taskCanceledQ) {
      this.taskRunningQ = taskRunningQ;
      this.taskCanceledQ = taskCanceledQ;
    }

    public void run() {
      try {
        taskRunningQ.add(this);
        while (true) {
          Thread.sleep(100000);
        }
      } catch (InterruptedException ie) {
        // Expected
      } finally {
        taskCanceledQ.add(this);
        isExiting = true;
      }
    }

    boolean isExiting() {
      return isExiting;
    }
  }

  private abstract static class CancelableTask implements TimedCancelable {
    private volatile int cancelCount;
    private volatile int timeoutCount;

    public int getCancelCount() {
      return cancelCount;
    }

    public void cancel() {
      cancelCount++;
    }

    public int getTimeoutCount() {
      return timeoutCount;
    }

    public void timeout(TaskHandle taskHandle) {
      timeoutCount++;
      taskHandle.cancel();
    }
  }
}
