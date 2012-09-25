// Copyright 2006 Google Inc.  All Rights Reserved.
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

/**
 * An interval of time used for schedules.
 */
public class ScheduleTimeInterval {
  private ScheduleTime startTime;
  private ScheduleTime endTime;

  public ScheduleTimeInterval(ScheduleTime startTime, ScheduleTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public ScheduleTime getEndTime() {
    return endTime;
  }

  public void setEndTime(ScheduleTime endTime) {
    this.endTime = endTime;
  }

  public ScheduleTime getStartTime() {
    return startTime;
  }

  public void setStartTime(ScheduleTime startTime) {
    this.startTime = startTime;
  }
}
