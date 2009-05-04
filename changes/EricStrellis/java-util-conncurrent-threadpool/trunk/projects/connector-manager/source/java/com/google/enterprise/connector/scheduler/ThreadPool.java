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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Manager for a pool of threads for running {@link Cancelable} tasks.
 * Individual task are identified by a unique user supplied
 * {@link String} key. This facility currently runs tasks as
 * soon as they are submitted. In the future it may limit the number of
 * concurrently tasks running and queue additional tasks to remain within
 * the limit.
 *
 * <p>This facility provides the following features:
 * <OL>
 * <LI> Cancel running or queued (if supported) task identified by a key.
 * <LI> Query if task is running or queued by its key.
 * <LI> Enforcement of key uniqueness.
 * <LI> Automatic tracking of task completion and release of
 *      key's for completed tasks.
 * <LI> Automatic cancel of tasks that do not complete within
 *      a specified interval.
 * <LI> Management of <b>hung</b> tasks that do not complete or respond
 *      to interrupts by releasing their keys for reuse. Once the key is
 *      released a new task can run with the key. Subsequent completion
 *      by the hung task will not affect the state managed by this facility
 *      for the running task.
 * <LI> Graceful shutdown.
 * </OL>
 *
 * <p>Cancel support includes two aspects
 * <OL>
 * <LI> Send the task an interrupt if it is running.
 * <LI> Call the task's {@link Cancelable#cancel()} method. This application
 *     level call is useful in cases the task logic does not
 *     track interrupts. It will also provide a means to alert a queued
 *     task of a cancel.
 * </OL>
 *
 * Issues:
 * <OL>
 * <LI> Though WorkQueue supports an option to shutdown
 *      with or without sending interrupts to running
 *      batches we only really use the shutdown with
 *      interrupts. Should we remove the option
 *      to shutdown without interrupts from this
 *      facility?
 * <LI> I've added a facility for calling user code
 *     when a task completes for testing purposes.
 *     Should I remove, document or change this
 *     facility?
 * <LI> What logging is needed?
 * </OL>
 * @author EricStrellis@gmail.com (Eric Strellis)
 */
public class ThreadPool {
  private static final Logger LOGGER = Logger.getLogger(ThreadPool.class.getName());

  /**
   * The default amount of time to let a task run before automatic
   * Cancellation.
   */
  static final long DEFAULT_MAXIMUM_TASK_LIFE_MILLIS = 30 * 60 * 1000;
  /**
   * The name for the timer thread. This thread is used for
   * automatic cancel of long running tasks.
   */
  private static final String TIMER_NAME = "Thread Pool - Kill Hung Task Timer";
  /**
   * Flag to indicate if this {@link ThreadPool} as been shutdown. Code
   * must hold the {@link ThreadPool} monitor to access this flag.
   */
  private boolean isShutdown;
  /**
   * {@link Map} from user supplied
   * key values to {@link TaskHandle}
   * Object for managing an associated task. Code must hold the
   * {@link ThreadPool} monitor to access crrentTasks.
   */
  private final Map<String, TaskHandle> currentTasks = new HashMap<String, TaskHandle>();
  /**
   * Tasks that do not complete in this interval are subject to
   * automatic cancel.
   */
  private final long maximumTaskLifeMillis;
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final Timer cancelIfHungTimer = new Timer(TIMER_NAME, true);

  /**
   * Create a {@link ThreadPool} that will automatically cancel
   * tasks that do not complete in the supplied interval.
   */
  ThreadPool(long maximumTaskLifeMillis) {
    this.maximumTaskLifeMillis = maximumTaskLifeMillis;
  }

  /**
   * Shutdown this {@link ThreadPool}. After the shutdown no new
   * tasks will be accepted.
   * @param interrupt true means send an interrupt each running tasks
   *     and false means don't send one.
   * @param waitMillis the maximum length of time to wait for
   *     tasks to complete.
   * @return true means all the running tasks have completed and
   *     false means some have not completed.
   * @throws InterruptedException if callers thread is
   *     Interrupted while waiting for existing tasks to complete.
   */
  boolean shutdown(boolean interrupt, int waitMillis) throws InterruptedException {
    synchronized (this) {
      // TODO(strellis): Is this flag needed or should we move
      //   the executor.shudown* calls into our synchronized block
      //   and depend on executor.isShutdown?
      isShutdown = true;
      // TODO(strellis): Call ThreadPool.cancel for running tasks?
      currentTasks.clear();
    }

    if (interrupt) {
      executor.shutdownNow();
    } else {
      executor.shutdown();
    }

    /*
     * When we call executor.awaitTermination we must not hold
     * ThreadPool monitor. If we do then completing tasks will
     * block calling registerCompletion().
     */
    return executor.awaitTermination(waitMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Submit a task for execution in another thread. This may return
   * before the task completes. If this {@link ThreadPool} has
   * been shut down this call does nothing.
   * @param key a Unique key to identify the task.
   * @param task the task to submit.
   * @throws IllegalStateException if a task with the provided
   *     key is already running.
   */
  synchronized void submit(String key, Cancelable task) {
    /*
     * After a shutdown this throws away submitted work. This avoids
     * the RejectedExecutionException that ExecutorService throws.
     */
     if (isShutdown) {
      return;
    }

    if (isRunning(key)) {
      throw new IllegalStateException("Attempt to run task with key " + key
          + " failed due to conflict with currently running task");
    }

    TaskHandle taskHandle = new TaskHandle(key, task);
    taskHandle.startTask();
  }

  /**
   * Cancel the task identified by the provided key.
   * If no task identified by this key is running,
   * perhaps because the task already completed, this
   * operation does nothing.
   */
  synchronized void cancel(String key) {
    TaskHandle taskHandle = currentTasks.get(key);
    if (taskHandle != null) {
      taskHandle.cancel();
    }
  }

  /**
   * Register the completion of the task with the provided
   * {@link TaskHandle}. This function may be safely called more
   * than once for the same {@link TaskHandle}.
   */
  private synchronized void registerCompletion(TaskHandle taskHandle) {
    /*
     * This code has some extra checking to deal with
     * a special case.
     * <OL>
     * <LI> Task with key K1 submitted and added to currentTasks.
     * <LI> Task with key K1 exceeds time limit, is cancelled and
     *      removed from currentTasks but thread does not complete.
     * <LI> New task with key K1 submitted and added to currentTasks.
     * <LI> Original stalled task with key K1 completes and calls
     *      registerCompletion.
     * </OL>
     * After this sequence if registeCompletion blindly removed
     * the TaskHandle for key K1 from currentTasks it would remove
     * the TaskHandle for the new running task when the original
     * task completes. To avoid this issue we check that the
     * TaskHadle in currentTasks matches before removing it.
     */
    TaskHandle registeredTaskHandle = currentTasks.get(taskHandle.getKey());
    if (registeredTaskHandle == taskHandle) {
      currentTasks.remove(taskHandle.getKey());
    }
    //
    //TODO(strellis): Currently our test for proper handling of the completion
    // of a stalled task that resumes after a new task starts for the
    // same key depends on our running the completion logic for the stalled task
    // twice (once when we cancel the task and again when it resumes and
    // completes). One could argue that we should not be calling
    // taskHandle.completeTask() a second time and that we can detect
    // this is a second time if registeredTaskHandle != taskHandle above. The
    // question is how to fix the test without adding a confusing feature?
    taskHandle.completeTask();
  }

  /**
   * Return true iff the task identified by the provided key
   * is running.
   */
  synchronized boolean isRunning(String key) {
    TaskHandle taskHandle = currentTasks.get(key);
    // TODO(strellis): Is the taskHandle.isRunning check a
    //    good idea? Should countCurrentTasks do the same
    //    check?
    return taskHandle != null && taskHandle.isRunning();
  }

  /**
   * Return the number of tasks that are running.
   */
  synchronized int countCurrentTasks() {
    return currentTasks.size();
  }

  /**
   * {@link Runnable} for executing a {@link Cancelable}
   * task in a {@link ThreadPool} {@link Thread} and removing
   * the task from the {@link ThreadPool} when it completes.
   */
  private class SelfCleaningRunnable implements Runnable {
    private final TaskHandle taskHandle;

    SelfCleaningRunnable(TaskHandle taskHandle) {
      this.taskHandle = taskHandle;
    }

    @Override
    public void run() {
      try {
        taskHandle.getCancelable().run();
      } finally {
        /*
         * Note that registerCompletion is called here though
         * it may have already been called by
         * {@link TaskHandle#cancel}.
         */
        registerCompletion(taskHandle);
      }
    }
  }

  /**
   * Handle for the management of a running task.
   * <p> All of the state in the TaskHandle is filled in
   * by the creation thread when it calls {@link ThreadPool#submit(String,
   * Cancelable)}. This includes some state
   * that is not available until after the task is started:
   * <OL>
   * <LI> The {link Future} for the task.
   * <LI> The the {@link CancelIfHungTask} to cancel the task if it does
   *      not complete in the allowed interval. Though this object could
   *      be instantiated it can't be used without the future.
   * <LI> The time the task starts.
   * </OL>
   * Below are the uses of the {@link TaskHandle} elements that are filled
   * in after the task starts. Each use is accompanied by an explanation of
   * the synchronization to assure the needed elements are available:
   * <OL>
   * <LI> Completion in the task pool thread.
   *      {@link ThreadPool#submit(String, Cancelable)} holds
   *      the monitor on {@link ThreadPool} until the
   *      {@link TaskHandle} is fully filled in. During
   *      completion ThreadPool.registerCompletion blocks
   *      on this lock lock.
   * <LI> Cancel based on a key in a client thread. The
   *      {@link TaskHandle} is only registered in
   *      TaskHandle.currentTasks after the task handle
   *      is fully filled in so the client only has
   *      access when the TaskHandle is complete.
   * <LI> Cancel based on a {@link TaskHandle} in in a
   *      timer thread after the time limit elapses. The
   *      timer is not activated until after the task
   *      handle is fully filled in.
   * </OL>
   *
   * <p> Concurrency control on {@link Cancelable} is up to the calling
   * application. This facility may make the following accesses from
   * outside the pool thread running the task.
   * <OL>
   * <LI> {@link Cancelable#cancel()}
   * <LI> {@link Completable#onCompletion()}
   * </OL>
   * The calling application must manage any needed synchronization in
   * these cases. The {@link Cancelable} should not call
   * {@link ThreadPool} functions to control its execution
   * from its code that is executing in a {@link ThreadPool} thread. Of
   * course the code can control its execution by normal means such
   * as exiting, throwing Exceptions or executing code.
   */
  private class TaskHandle {
    /**
     * The key for this task. Filled in by the constructor.
     */
    final String key;
    /**
     * The Cancelable operation that is run by this task. Filled in
     * by the constructor.
     */
    final Cancelable cancelable;

    /*
     * The Future for the running task. Filled in after the task
     * starts.
     */
    Future<?> future;
    /*
     * The time the task starts. Filled in after the task starts.
     */
    long startTime;
    /*
     * The TimerTask for canceling this task if it does not complete
     * in the allowed interval. Filled in after the task starts.
     */
    CancelIfHungTask cancelIfHungTask;

    /**
     * Create a {@link TaskHandle} for a new task.
     */
    TaskHandle(String key, Cancelable cancelable) {
      this.key = key;
      this.cancelable = cancelable;
    }

    Cancelable getCancelable() {
      return cancelable;
    }

    String getKey() {
      return key;
    }

    /**
     * Start the task. The caller must hold the {@link ThreadPool}
     * monitor to call this.
     */
    void startTask() {
      if (future != null) {
        throw new IllegalStateException("Unsupported attempt to start a task twice");
      }
      Runnable selfCleaner = new SelfCleaningRunnable(this);
      cancelIfHungTask = new CancelIfHungTask(this);
      future = executor.submit(selfCleaner);
      currentTasks.put(key, this);
      cancelIfHungTimer.schedule(cancelIfHungTask, maximumTaskLifeMillis);
    }

    /**
     * Return true iff the task is running. The caller must
     * hold the {@link ThreadPool} monitor to call this.
     */
    boolean isRunning() {
      return future != null && !future.isDone();
    }

    /**
     * Return the start time for the task or 0 if the the task has not run.
     * The caller must hold the {@link ThreadPool} monitor to call this.
     */
    long getStartTime() {
      return startTime;
    }


    // Caller must hold the ThreadPool lock and startTask must have completed.
    /**
     * Complete this task after it has run or been canceled. The caller
     * must hold the {@link ThreadPool} monitor to call this and
     * {#link {@link #startTask()} must have completed.
     */
    void completeTask() {
      cancelIfHungTask.cancel();
      /**
       * Currently we only allow the caller to run some code after the task completes for
       * testing purposes so perhaps this feature should be removed.
       */
      if (cancelable instanceof Completable) {
        ((Completable) cancelable).onCompletion();
       }
//  Nice try but because this runs in the background so future.isDone() == false
//  and future.get() hangs. Is it worth doing these checks in another thread
//  to log errors and verify future.isDone == true?
//      if (!future.isDone()) {
//        LOGGER.log(Level.WARNING, "completeTask called for task that is not done.");
//      }
//      try {
//        Object ignored = future.get();
//      } catch (InterruptedException ie) {
//        LOGGER.log(Level.WARNING, "Unexpected interrupt in task " + key, ie);
//        Thread.currentThread().interrupt();
//      } catch (ExecutionException ee) {
//        LOGGER.log(Level.SEVERE, "Unexpected exception in task " + key, ee);
//      }
    }

    /**
     * Cancel this task. {@link #startTask} must complete before calling this.
     */
    void cancel() {
      future.cancel(true);
      cancelIfHungTask.cancel();
      cancelable.cancel();
      registerCompletion(this);
    }

    @Override
    public String toString() {
      return "TaskHandle key " + key + " startTime = " + startTime +" cancelable "
          + cancelable + "future = " + future + " cancelIfHungTask " + cancelIfHungTask;
    }
  }

  /**
   * {@link TimerTask} to cancel a task if it does not complete
   * in the allowed interval.
   */
  private class CancelIfHungTask extends TimerTask {
    private final TaskHandle taskHandle;

    CancelIfHungTask(TaskHandle taskHandle) {
      this.taskHandle = taskHandle;
    }

    @Override
    public void run() {
      taskHandle.cancel();
    }
  }
}
