package com.google.enterprise.connector.scheduler;

import java.util.concurrent.Future;

public class TaskHandle {
  /**
   * The Cancelable operation that is run by this task. Filled in
   * by the constructor.
   */
  final Cancelable cancelable;

  /*
   * The Future for running the timeout task.
   */
  final Future<?> timeoutFuture;

  /*
   * The Future for the running task. Filled in after the task
   * starts.
   */
  final Future<?> taskFuture;
  /*
   * The time the task starts. Filled in after the task starts.
   */
  final long startTime;


  TaskHandle(Cancelable cancelable, Future<?> timeoutFuture, Future<?> taskFuture, long startTime) {
    this.cancelable = cancelable;
    this.timeoutFuture = timeoutFuture;
    this.taskFuture = taskFuture;
    this.startTime = startTime;
  }

  synchronized public void cancel() {
    cancelable.cancel();
    timeoutFuture.cancel(true);
    taskFuture.cancel(true);
  }

  public boolean isDone() {
    return taskFuture.isDone();
  }
}

