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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A traversal schedule.
 * 
 */
public class Schedule {
  private String connectorName;
  private List timeIntervals;

  public Schedule(String connectorName, List timeIntervals) {
    this.connectorName = connectorName;
    this.timeIntervals = timeIntervals;
  }

  /**
   * Create a schedule object.
   * 
   * @param scheduleProto String readable by readString() method
   */
  public Schedule(String scheduleProto) {
    readString(scheduleProto);
  }

  /**
   * Populate a schedule.
   * 
   * @param schedule String of the form: e.g. "connector1:1-2:3-5"
   * 
   */
  public void readString(String schedule) {
    String[] strs = schedule.split(":");
    if (strs.length > 0) {
      connectorName = strs[0];
      timeIntervals = new ArrayList();
      for (int i = 1; i < strs.length; i++) {
        String[] strs2 = strs[i].split("-");
        String startTime = strs2[0];
        String endTime = strs2[1];
        ScheduleTime t1 = new ScheduleTime(Integer.parseInt(startTime));
        ScheduleTime t2 = new ScheduleTime(Integer.parseInt(endTime));
        ScheduleTimeInterval interval = new ScheduleTimeInterval(t1, t2);
        timeIntervals.add(interval);
      }
    }
  }

  /**
   * @return String of the form: e.g. "connector1:1-2:3-5"
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(connectorName);
    Iterator iter = timeIntervals.iterator();
    while (iter.hasNext()) {
      ScheduleTimeInterval interval = (ScheduleTimeInterval) iter.next();
      ScheduleTime startTime = interval.getStartTime();
      ScheduleTime endTime = interval.getEndTime();
      buf.append(":");
      buf.append(startTime.getHour());
      buf.append("-");
      buf.append(endTime.getHour());
    }
    return buf.toString();
  }

  public String getConnectorName() {
    return connectorName;
  }

  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }

  public List getTimeIntervals() {
    return timeIntervals;
  }

  public void setTimeIntervals(List timeIntervals) {
    this.timeIntervals = timeIntervals;
  }
}
