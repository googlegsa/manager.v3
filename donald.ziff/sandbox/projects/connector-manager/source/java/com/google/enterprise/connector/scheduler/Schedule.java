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
  private int load;
  private List timeIntervals;

  public Schedule(String connectorName, int load, List timeIntervals) {
    if (null == timeIntervals) {
      throw new IllegalArgumentException();
    }
    this.connectorName = connectorName;
    this.load = load;
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
   * @param schedule String of the form: <connectorName>:<load>:<timeIntervals>
   * e.g. "connector1:60:1-2:3-5"
   * 
   */
  public void readString(String schedule) {
    String[] strs = schedule.split(":");
    if (strs.length >= 3) {  // must have at least one time interval
      connectorName = strs[0];
      load = Integer.parseInt(strs[1]);
      timeIntervals = new ArrayList();
      for (int i = 2; i < strs.length; i++) {
        String[] strs2 = strs[i].split("-");
        String startTime = strs2[0];
        String endTime = strs2[1];
        ScheduleTime t1 = new ScheduleTime(Integer.parseInt(startTime));
        ScheduleTime t2 = new ScheduleTime(Integer.parseInt(endTime));
        ScheduleTimeInterval interval = new ScheduleTimeInterval(t1, t2);
        timeIntervals.add(interval);
      }
    } else {
      throw new IllegalArgumentException("Schedule should have at least one " +
            "time interval: " + schedule);
    }
  }

  /**
   * @return String of the form: e.g. "connector1:1-2:3-5"
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(connectorName);
    buf.append(":" + load);
    buf.append(":" + getTimeIntervalsAsString());
    return buf.toString();
  }

  public String getConnectorName() {
    return connectorName;
  }

  public int getLoad() {
    return load;
  }

  public List getTimeIntervals() {
    return timeIntervals;
  }

  /**
   * @return String of the form: e.g. "1-2:3-5"
   */
  public String getTimeIntervalsAsString() {
    StringBuffer buf = new StringBuffer();
    Iterator iter = timeIntervals.iterator();
    for (int index = 0; iter.hasNext(); index++) {
      ScheduleTimeInterval interval = (ScheduleTimeInterval) iter.next();
      ScheduleTime startTime = interval.getStartTime();
      ScheduleTime endTime = interval.getEndTime();
      if (index != 0) {
        buf.append(":");
      }
      buf.append(startTime.getHour());
      buf.append("-");
      buf.append(endTime.getHour());
    }
    return buf.toString();
  }
}
