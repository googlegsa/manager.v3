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

import java.util.TimerTask;

/**
 * Extends {@link TimerTask} to include the desired schedule. Note
 * that unlike {@link java.util.Timer} schedules, the schedule here is
 * specified in seconds for consistency with other time specifications
 * in the connector manager.
 */
public abstract class ScheduledTimerTask extends TimerTask {
  /** Gets the delay in seconds before the task is to be executed. */
  public abstract long getDelay();

  /** Gets the time in seconds between successive task executions. */
  public abstract long getPeriod();
}
