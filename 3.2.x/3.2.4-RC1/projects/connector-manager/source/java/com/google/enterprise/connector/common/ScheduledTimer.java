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

import com.google.common.annotations.VisibleForTesting;

import java.util.Timer;

/**
 * A timer for {@link ScheduledTimerTask}s. This class does not start
 * a timer thread until a task is scheduled to be executed in the
 * future.
 */
/*
 * In order to not create a thread during construction, this class
 * must not extend Timer.
*/
public class ScheduledTimer {
  @VisibleForTesting
  static final String THREAD_NAME = "ScheduledTimer";

  private Timer timer;

  /**
   * Schedules the task to run. If a delay of zero is given, it will
   * be run immediately in the calling thread, rather than running in
   * the timer thread.
   */
  public void schedule(ScheduledTimerTask task) {
    long delay;
    if (task.getDelay() == 0L) {
      task.run();
      delay = task.getPeriod();
    } else {
      delay = task.getDelay();
    }

    // Only schedule the task in the timer if it needs to be executed
    // in the future. N.B.: Do not test delay here instead of
    // task.getDelay.
    if (task.getDelay() > 0L || task.getPeriod() > 0L) {
      synchronized (this) {
        if (timer == null) {
          // Create a timer with a named thread.
          timer = new Timer(THREAD_NAME);
        }
      }

      // Timer requires milliseconds, rather than seconds.
      if (task.getPeriod() > 0L) {
        timer.schedule(task, delay * 1000L, task.getPeriod() * 1000L);
      } else {
        timer.schedule(task, delay * 1000L);
      }
    }
  }

  public void cancel() {
    if (timer != null) {
      timer.cancel();
    }
  }
}
