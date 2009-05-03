// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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
import java.util.List;

/**
 * A traversal schedule.
 */
public class Schedule {

  private static int defaultRetryDelayMillis = (5 * 60 * 1000);

  private String connectorName;
  private boolean disabled;
  private int load;
  private int retryDelayMillis; // maximum of ~24 days
  private List<ScheduleTimeInterval> timeIntervals;

  /**
   * Signal to the Traverser that it should traverse the ECM repository
   * until there is not new content, then stop.
   */
  public static final int POLLING_DISABLED = -1;

  /**
   * Construct an empty, disabled Schedule.
   */
  public Schedule() {
    this(null, true, 0, -1, "1-1");
  }


  /**
   * Construct a Schedule for a given Connector.
   *
   * @param connectorName
   * @param disabled true if this schedule is currently disabled
   * @param load The hostload (in docs per minute) as an integer
   * @param retryDelayMillis Time to wait before next traversal (milliseconds)
   * @param timeIntervals Time intervals string in the format of "1-2:3-8"
   */
  public Schedule(String connectorName, boolean disabled, int load,
      int retryDelayMillis, String timeIntervals) {
    this(connectorName, disabled, load, retryDelayMillis,
         parseTimeIntervals(timeIntervals));
  }

  /**
   * Set schedule for a given Connector.
   *
   * @param connectorName
   * @param disabled true if this schedule is currently disabled
   * @param load The hostload (in docs per minute) as an integer
   * @param retryDelayMillis Time to wait before next traversal (milliseconds)
   * @param timeIntervals Time intervals in the format of {1-2,3-8
   */
  public Schedule(String connectorName, boolean disabled, int load,
      int retryDelayMillis, List<ScheduleTimeInterval> timeIntervals) {
    if ((null == timeIntervals) || (timeIntervals.isEmpty())) {
      throw new IllegalArgumentException();
    }
    this.connectorName = connectorName;
    this.load = load;
    this.disabled = disabled;
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
   * Set the default RetryDelayMillisecs.
   *
   * @param defaultValue default value for retryDelay in seconds.
   */
  public static void setDefaultRetryDelaySecs(int defaultValue) {
    defaultRetryDelayMillis = defaultValue * 1000;
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
      if (fields[2].indexOf('-') <= 0) {
        // It's a delay, get rid of it
        StringBuilder result = new StringBuilder();
        if (fields[0].charAt(0) == '#') {
          // Legacy doesn't support disabled either.
          result.append(fields[0].substring(1));
        } else {
          result.append(fields[0]);
        }
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
   * Return the default retryDelayMillis value.
   * This can be defined in the Context by specifying
   * TraversalDelaySecondsDefault value.
   */
  public static int defaultRetryDelayMillis() {
    return defaultRetryDelayMillis;
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
      String[] strs = schedule.trim().split(":", 4);
      if (strs[0].charAt(0) == '#') {
        disabled = true;
        connectorName = strs[0].substring(1);
      } else {
        connectorName = strs[0];
      }
      load = Integer.parseInt(strs[1]);
      String intervals;
      if (strs[2].indexOf('-') <= 0) {
        retryDelayMillis = Integer.parseInt(strs[2]);
        intervals = strs[3];
      } else {
        // This is a legacy string without the retryDelay.  Resplit.
        retryDelayMillis = defaultRetryDelayMillis;
        strs = schedule.trim().split(":", 3);
        intervals = strs[2];
      }
      timeIntervals = parseTimeIntervals(intervals);
      if (timeIntervals.size() < 1) {
        throw new IllegalArgumentException(exceptionReason);
      }
    } catch(ArrayIndexOutOfBoundsException aioobe) {
      throw new IllegalArgumentException(exceptionReason);
    }
  }

  /**
   * Parse a string of time intervals.
   *
   * @param intervals String of the form: "1-2:3-5:14-18" etc.
   * @return List of ScheduleTimeInterval objects
   */
  private static List<ScheduleTimeInterval> parseTimeIntervals(
      String intervals) {
    String[] strs = intervals.trim().split(":");
    List<ScheduleTimeInterval> timeIntervals =
      new ArrayList<ScheduleTimeInterval> (strs.length);
    for (int i = 0; i < strs.length; i++) {
      String[] strs2 = strs[i].split("-");
      String startTime = strs2[0];
      String endTime = strs2[1];
      ScheduleTime t1 = new ScheduleTime(Integer.parseInt(startTime));
      ScheduleTime t2 = new ScheduleTime(Integer.parseInt(endTime));
      ScheduleTimeInterval interval = new ScheduleTimeInterval(t1, t2);
      timeIntervals.add(interval);
    }
    return timeIntervals;
  }

  /**
   * @return String of the form: e.g. "connector1:1-2:3-5"
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    if (disabled) {
      buf.append('#');
    }
    buf.append(connectorName);
    buf.append(":" + load);
    buf.append(":" + retryDelayMillis);
    buf.append(":" + getTimeIntervalsAsString());
    return buf.toString();
  }

  public String getConnectorName() {
    return connectorName;
  }

  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }

  public int getLoad() {
    return load;
  }

  public void setLoad(int load) {
    this.load = load;
  }

  public int getRetryDelayMillis() {
    return retryDelayMillis;
  }

  public void setRetryDelayMillis(int retryDelayMillis) {
    this.retryDelayMillis = retryDelayMillis;
  }

  public List<ScheduleTimeInterval> getTimeIntervals() {
    return timeIntervals;
  }

  public void setTimeIntervals(List<ScheduleTimeInterval> timeIntervals) {
    this.timeIntervals = timeIntervals;
  }

  public void setTimeIntervals(String timeIntervals) {
    this.timeIntervals = parseTimeIntervals(timeIntervals);
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  /**
   * @return String of the form: e.g. "1-2:3-5"
   */
  public String getTimeIntervalsAsString() {
    StringBuilder buf = new StringBuilder();
    for (ScheduleTimeInterval interval : timeIntervals) {
      ScheduleTime startTime = interval.getStartTime();
      ScheduleTime endTime = interval.getEndTime();
      if (buf.length() > 0) {
        buf.append(":");
      }
      buf.append(startTime.getHour());
      buf.append("-");
      buf.append(endTime.getHour());
    }
    return buf.toString();
  }
}
