package com.google.enterprise.connector.scheduler;

import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadPool {
  private static final Logger LOGGER = Logger.getLogger(ThreadPool.class.getName());

  /**
   * The default amount of time to let a task run before automatic
   * Cancellation.
   */
  public static final long DEFAULT_MAXIMUM_TASK_LIFE_MILLIS = 30 * 60 * 1000;
  /**
   * The name for the timer thread. This thread is used for
   * automatic cancel of long running tasks.
   */
  private final long maximumTaskLifeMillis;
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final CompletionService<?> completionService = new ExecutorCompletionService<Object>(executor);

  private final ExecutorService completionExecutor = Executors.newSingleThreadExecutor();

  private final ScheduledExecutorService timerService = Executors.newSingleThreadScheduledExecutor();

  ThreadPool(long maximumLifeMillis) {
    this.maximumTaskLifeMillis = maximumLifeMillis;
    //TODO(strellis): Avoid threads in constructor?
    completionExecutor.execute(new CompletionTask());
  }

  boolean shutdown(boolean interrupt, long waitMillis) throws InterruptedException {
    if (interrupt) {
      executor.shutdownNow();
    } else {
      executor.shutdown();
    }

    try {
      return executor.awaitTermination(waitMillis, TimeUnit.MILLISECONDS);
    } finally {
      completionExecutor.shutdownNow();
    }
  }

  TaskHandle submit(Cancelable cancelable) {
    //
    //Resolve: submit after shutdown - use drop on floor executor policy
    // rather than extra synchronization? Also verify corner case where timer expires
    // and cancels task before it is submitted.
    TimeoutTask timeoutTask = new TimeoutTask();
    FutureTask<?> timeoutFuture = new FutureTask<Object>(timeoutTask, null);
    FutureTask<?> taskFuture = new TimeLimitedFutureTask(cancelable, timeoutFuture);
    TaskHandle handle = new TaskHandle(cancelable, timeoutFuture, taskFuture, System.currentTimeMillis());
    timeoutTask.setHandle(handle);
    timerService.schedule(timeoutFuture, maximumTaskLifeMillis, TimeUnit.MILLISECONDS);
    try {
      //TODO(strellis): test/handle timer pop/cancel before submit. In production
      //                with a 30 minute timeout this should never happen.
      completionService.submit(taskFuture, null);
    } catch (RejectedExecutionException re) {
      if (!executor.isShutdown()) {
        LOGGER.log(Level.SEVERE, "Unable to execute task", re);
      }
      handle = null;

    }
    return handle;
  }

  private class TimeLimitedFutureTask extends FutureTask<Object> {
    private final Future<?> timeoutFuture;
    TimeLimitedFutureTask(Cancelable cancelable, Future<?> timeoutFuture) {
      super(cancelable, null);
      this.timeoutFuture = timeoutFuture;
    }

    @Override
    public Object get() throws InterruptedException ,ExecutionException {
      //TODO(strellis) clean up canceled task from schedule pool?
      timeoutFuture.cancel(true);
      return get();
    }
  }

  private class TimeoutTask implements Runnable {
    volatile TaskHandle handle;
    void setHandle(TaskHandle handle) {
      this.handle = handle;
    }
    @Override
    public void run() {
      if (handle == null) {
        throw new IllegalStateException("Must set handle before run()");
      }
      handle.cancel();
    }
  }

  private class CompletionTask implements Runnable {
    private void completeTask() throws InterruptedException {
      Future<?> future = completionService.take();
        try {
          Object o = future.get();
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();
          //TODO(strellis): Should we call cancelable.cancel() if we get an exception?
          if(cause instanceof InterruptedException) {
            LOGGER.log(Level.INFO, "Batch failed due to interrupt.", cause);
          } else {
            LOGGER.log(Level.SEVERE, "Batch failed with unhandled exception", cause);
          }
        }
    }

    @Override
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
}
