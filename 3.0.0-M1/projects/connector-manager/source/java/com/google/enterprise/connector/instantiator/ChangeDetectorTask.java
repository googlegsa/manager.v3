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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.common.ScheduledTimerTask;

public class ChangeDetectorTask extends ScheduledTimerTask {
  private final ChangeDetector changeDetector;
  private final long delay;
  private final long period;

  /**
   * Constructs a task with a schedule. Note that unlike
   * {@link java.util.Timer} schedules, the schedule here is specified
   * in seconds for consistency with other time specifications in the
   * connector manager.
   *
   * @param delay delay in seconds before task is to be executed
   * @param period time in seconds between successive task executions
   */
  public ChangeDetectorTask(ChangeDetector changeDetector, long delay,
      long period) {
    this.changeDetector = changeDetector;
    this.delay = delay;
    this.period = period;
  }

  @Override
  public long getDelay() {
    return delay;
  }

  @Override
  public long getPeriod() {
    return period;
  }

  @Override
  public void run() {
    changeDetector.detect();
  }
}
