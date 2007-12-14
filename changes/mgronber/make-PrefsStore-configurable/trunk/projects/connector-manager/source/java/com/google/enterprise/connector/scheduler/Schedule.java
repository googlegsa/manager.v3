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
  private int retryDelayMillis; // maximum of ~24 days
  private List timeIntervals;

  public Schedule(String connectorName, int load, int retryDelayMillis,
      List timeIntervals) {
    if ((null == timeIntervals) || (timeIntervals.isEmpty())) {
      throw new IllegalArgumentException();
    }
    this.connectorName = connectorName;
    this.load = load;
    this.retryDelayMillis = retryDelayMillis;
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
   * Utility method to parse out the delay field.  Currently only used when
   * sending the schedule string back to a GSA that has not been updated to
   * parse the delay field from the schedule.
   * 
   * Done in a safe manner so it won't mangle a given legacy schedule.
   * 
   * @param schedule a schedule string that contains a delay field.
   * @return a schedule string without the delay field.
   */
  public static String toLegacyString(String schedule) {
    String[] fields = schedule.split(":");
    try {
      if (fields[2].indexOf('-') < 0) {
        // It's a delay, get rid of it
        StringBuffer result = new StringBuffer();
        result.append(fields[0]);
        result.append(":").append(fields[1]);

        if (fields.length == 3) {
          throw new IllegalArgumentException();
        } else {
          for (int i = 3; i < fields.length; i++) {
            result.append(":").append(fields[i]);
          }
        }
        return result.toString();
      }
      // else, it's already legacy
      return schedule;
    } catch(ArrayIndexOutOfBoundsException aioobe) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Populate a schedule.
   * 
   * @param schedule String of the form:
   *    <connectorName>:<load>:<retryDelayMillis>:<timeIntervals>
   *    OR
   *    <connectorName>:<load>:<timeIntervals>
   * e.g. "connector1:60:86400000:1-2:3-5", "connector1:60:1-2:3-5" 
   *  
   */
  public void readString(String schedule) {
    String exceptionReason = "Invalid schedule string format: " + schedule;
    try {
      String[] strs = schedule.split(":");
      connectorName = strs[0];
      load = Integer.parseInt(strs[1]);
      int intervalsStart = -1;
      if(strs[2].indexOf('-') < 0) {
        retryDelayMillis = Integer.parseInt(strs[2]);
        intervalsStart = 3;
      } else {
        intervalsStart = 2;
      }
      timeIntervals = new ArrayList(); 
          
      for (int i = intervalsStart; i < strs.length; i++) {
        String[] strs2 = strs[i].split("-");
        String startTime = strs2[0];
        String endTime = strs2[1];
        ScheduleTime t1 = new ScheduleTime(Integer.parseInt(startTime));
        ScheduleTime t2 = new ScheduleTime(Integer.parseInt(endTime));
        ScheduleTimeInterval interval = new ScheduleTimeInterval(t1, t2);
        timeIntervals.add(interval);
      }
      if (timeIntervals.size() < 1) {
        throw new IllegalArgumentException(exceptionReason);
      }
    } catch(ArrayIndexOutOfBoundsException aioobe) {
      throw new IllegalArgumentException(exceptionReason);
    }
  }

  /**
   * @return String of the form: e.g. "connector1:1-2:3-5"
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(connectorName);
    buf.append(":" + load);
    buf.append(":" + retryDelayMillis);
    buf.append(":" + getTimeIntervalsAsString());
    return buf.toString();
  }

  public String getConnectorName() {
    return connectorName;
  }

  public int getLoad() {
    return load;
  }
  
  public int getRetryDelayMillis() {
    return retryDelayMillis;
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
