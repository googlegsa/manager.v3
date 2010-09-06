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

import com.google.enterprise.connector.util.Clock;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pool for running {@link TimedCancelable}, time limited tasks.
 *
 * <p>
 * Users are provided a {@link TaskHandle} for each task. The {@link TaskHandle}
 * supports canceling the task and determining if the task is done running.
 *
 * <p>
 * The ThreadPool enforces a configurable maximum time interval for tasks. Each
 * task is guarded by a <b>time out task</b> that will cancel the primary task
 * if the primary task does not complete within the allowed interval.
 *
 * <p>
 * Task cancellation includes two actions that are visible for the task task's
 * {@link TimedCancelable}
 * <OL>
 * <LI>Calling {@link Future#cancel(boolean)} to send the task an interrupt and
 * mark it as done.
 * <LI>Calling {@link TimedCancelable#cancel()} to send the task a second signal
 * that it is being canceled. This signal has the benefit that it does not
 * depend on the tasks interrupt handling policy.
 * </OL>
 * Once a task has been canceled its {@link TaskHandle#isDone()} method will
 * immediately start returning true.
 *
 * <p>
 * {@link ThreadPool} performs the following processing when a task completes
 * <OL>
 * <LI>Cancel the <b>time out task</b> for the completed task.
 * <LI>Log exceptions that indicate the task did not complete normally.
 * </OL>
 */
public class ThreadPool {
  private static final Logger LOGGER =
      Logger.getLogger(ThreadPool.class.getName());

  /**
   * The default amount of time in to wait for tasks to complete during during
   * shutdown.
   */
  public static final int DEFAULT_SHUTDOWN_TIMEOUT_MILLIS = 10 * 1000;

  /**
   * Configured amount of time to let tasks run before automatic cancellation.
   */
  private final long maximumTaskLifeMillis;

  /**
   * Clock used to time out threads.
   */
  private final Clock clock;

  /**
   * ExecutorService for running submitted tasks. Tasks are only submitted
   * through completionService.
   */
  private final ExecutorService executor =
    Executors.newCachedThreadPool(
        new ThreadNamingThreadFactory("ThreadPoolExecutor"));

  /**
   * CompletionService for running submitted tasks. All tasks are submitted
   * through this CompletionService to provide blocking, queued access to
   * completion information.
   */
  private final CompletionService<?> completionService =
      new ExecutorCompletionService<Object>(executor);

  /**
   * Dedicated ExecutorService for running the CompletionTask. The completion
   * task is run in its own ExecutorService so that it can be shut down after
   * the executor for submitted tasks has been shut down and drained of running
   * tasks.
   */
  private final ExecutorService completionExecutor =
      Executors.newSingleThreadExecutor(
          new ThreadNamingThreadFactory("ThreadPoolCompletion"));

  /**
   * Dedicated ScheduledThreadPoolExecutor for running time out tasks. Each
   * primary task is guarded by a time out task that is scheduled to run when
   * the primary tasks maximum life time expires. When the time out task runs it
   * cancels the primary task.
   */
  private final ScheduledThreadPoolExecutor timeoutService =
      new ScheduledThreadPoolExecutor(1,
          new ThreadNamingThreadFactory("ThreadPoolTimeout"));

  /**
   * Create a {@link ThreadPool}.
   *
   * @param taskLifeSeconds minimum number of seconds to allow a task to run
   *        before automatic cancellation.
   */
  // TODO: This method, called from Spring, multiplies the supplied [soft]
  // timeout value by 2.  The actual value wants to be 2x or 1.5x of a user
  // configured soft value. However, Spring v2 does not provide a convenient
  // mechanism to do arithmetic on configuration properties. Once we move to
  // Spring v3, the calculation should be done in the Spring XML definition
  // file rather than here.
  public ThreadPool(int taskLifeSeconds, Clock clock) {
    this.maximumTaskLifeMillis = taskLifeSeconds * 2 * 1000L;
    this.clock = clock;
    completionExecutor.execute(new CompletionTask());
  }

  /**
   * Shut down the {@link ThreadPool}. After this returns
   * {@link ThreadPool#submit(TimedCancelable)} will return null.
   *
   * @param interrupt <tt>true</tt> if the threads executing tasks task should
   *        be interrupted; otherwise, in-progress tasks are allowed to complete
   *        normally.
   * @param waitMillis maximum amount of time to wait for tasks to complete.
   * @return <tt>true</tt> if this all the running tasks terminated and
   *         <tt>false</tt> if the some running task did not terminate.
   * @throws InterruptedException if interrupted while waiting.
   */
  boolean shutdown(boolean interrupt, long waitMillis)
      throws InterruptedException {
    if (interrupt) {
      executor.shutdownNow();
    } else {
      executor.shutdown();
    }
    timeoutService.shutdown();
    try {
      return executor.awaitTermination(waitMillis, TimeUnit.MILLISECONDS);
    } finally {
      completionExecutor.shutdownNow();
      timeoutService.shutdownNow();
    }
  }

  /**
   * Submit a {@link TimedCancelable} for execution and return a
   * {@link TaskHandle} for the running task or null if the task has not been
   * accepted. After {@link ThreadPool#shutdown(boolean, long)} returns this
   * will always return null.
   */
  public TaskHandle submit(TimedCancelable cancelable) {
    // When timeoutTask is run it will cancel 'cancelable'.
    TimeoutTask timeoutTask = new TimeoutTask(cancelable);
    // timeoutFuture will be used to cancel timeoutTask when 'cancelable'
    // completes.
    FutureTask<?> timeoutFuture = new FutureTask<Object>(timeoutTask, null);
    // cancelTimeoutRunnable runs 'cancelable'. When 'cancelable' completes
    // cancelTimeoutRunnable cancels 'timeoutTask'. This saves system
    // resources. In addition it prevents timeout task from running and
    // calling cancel after 'cancelable' completes successfully.
    CancelTimeoutRunnable cancelTimeoutRunnable =
        new CancelTimeoutRunnable(cancelable, timeoutFuture);
    // taskFuture is used to cancel 'cancelable' and to determine if
    // 'cancelable' is done.
    FutureTask<?> taskFuture =
        new FutureTask<Object>(cancelTimeoutRunnable, null);
    TaskHandle handle =
        new TaskHandle(cancelable, taskFuture, clock.getTimeMillis());
    timeoutTask.setTaskHandle(handle);
    try {
      // Schedule timeoutTask to run when 'cancelable's maximum run interval
      // has expired.
      timeoutService.schedule(timeoutFuture, maximumTaskLifeMillis,
          TimeUnit.MILLISECONDS);
      // TODO(strellis): test/handle timer pop/cancel before submit. In
      // production with a 30 minute timeout this should never happen.
      completionService.submit(taskFuture, null);
    } catch (RejectedExecutionException re) {
      if (!executor.isShutdown()) {
        LOGGER.log(Level.SEVERE, "Unable to execute task", re);
      }
      handle = null;
    }
    return handle;
  }

  /**
   * A {@link Runnable} for running {@link TimedCancelable} that has been
   * guarded by a timeout task. This will cancel the timeout task when the
   * {@link TimedCancelable} completes. If the timeout task has already run then
   * canceling it has no effect.
   */
  private class CancelTimeoutRunnable implements Runnable {
    private final Future<?> timeoutFuture;
    private final TimedCancelable cancelable;

    /**
     * Constructs a {@link CancelTimeoutRunnable}.
     *
     * @param cancelable the {@link TimedCancelable} this runs.
     * @param timeoutFuture the {@link Future} for canceling the timeout task.
     */
    CancelTimeoutRunnable(TimedCancelable cancelable, Future<?> timeoutFuture) {
      this.timeoutFuture = timeoutFuture;
      this.cancelable = cancelable;
    }

    public void run() {
      try {
        cancelable.run();
      } finally {
        timeoutFuture.cancel(true);
        timeoutService.purge();
      }
    }
  }

  /**
   * A task that cancels another task that is running a {@link TimedCancelable}.
   * The {@link TimeoutTask} should be scheduled to run when the interval for
   * the {@link TimedCancelable} to run expires.
   */
  private static class TimeoutTask implements Runnable {
    final TimedCancelable timedCancelable;
    private volatile TaskHandle taskHandle;

    TimeoutTask(TimedCancelable timedCancelable) {
      this.timedCancelable = timedCancelable;
    }

    public void run() {
      if (taskHandle == null) {
        throw new IllegalStateException(
            "Run TimeoutTask called with null taskHandle.");
      }
      timedCancelable.timeout(taskHandle);
    }

    void setTaskHandle(TaskHandle taskHandle) {
      this.taskHandle = taskHandle;
    }
  }

  /**
   * A task that gets completion information from all the tasks that run in a
   * {@link CompletionService} and logs uncaught exceptions that cause the tasks
   * to fail.
   */
  private class CompletionTask implements Runnable {
    private void completeTask() throws InterruptedException {
      Future<?> future = completionService.take();
      try {
        future.get();
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        // TODO(strellis): Should we call cancelable.cancel() if we get an
        // exception?
        if (cause instanceof InterruptedException) {
          LOGGER.log(Level.INFO, "Batch termiated due to an interrupt.", cause);
        } else {
          LOGGER.log(Level.SEVERE, "Batch failed with unhandled exception",
              cause);
        }
      }
    }

    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          completeTask();
        }
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
      LOGGER.info("Completion task shutdown.");
    }
  }

  /**
   * A {@link ThreadFactory} that adds a prefix to thread names assigned
   * by {@link Executors#defaultThreadFactory()} to provide diagnostic
   * context in stack traces.
   */
  private static class ThreadNamingThreadFactory implements ThreadFactory {
    private final ThreadFactory delegate = Executors.defaultThreadFactory();
    private final String namePrefix;
    ThreadNamingThreadFactory(String namePrefix) {
      this.namePrefix = namePrefix + "-";
    }
    public Thread newThread(Runnable r) {
      Thread t = delegate.newThread(r);
      t.setName(namePrefix + t.getName());
      return t;
    }
  }
}
