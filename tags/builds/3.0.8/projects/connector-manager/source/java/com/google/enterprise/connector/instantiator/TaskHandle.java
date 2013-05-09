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

import java.util.concurrent.Future;

/**
 * Handle for the management of a {@link Cancelable} primary task.
 */
public class TaskHandle {
  /**
   * The primary {@link Cancelable} that is run by this task to
   * perform some useful work.
   */
  final Cancelable cancelable;

  /*
   * The {@link future} for the primary task.
   */
  final Future<?> taskFuture;

  /*
   * The time the task starts.
   */
  final long startTime;

  /**
   * Create a TaskHandle.
   *
   * @param cancelable {@link Cancelable} for the primary task.
   * @param taskFuture {@link Future} for the primary task.
   * @param startTime startTime for the primary task.
   */
  TaskHandle(Cancelable cancelable, Future<?> taskFuture, long startTime) {
    this.cancelable = cancelable;
    this.taskFuture = taskFuture;
    this.startTime = startTime;
  }

  /**
   * Cancel the primary task and the time out task.
   */
  public void cancel() {
    cancelable.cancel();
    taskFuture.cancel(true);
  }

  /**
   * Return true if the primary task has completed.
   */
  public boolean isDone() {
    return taskFuture.isDone();
  }
}

