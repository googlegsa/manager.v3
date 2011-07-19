// Copyright 2006 Google Inc.
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
/*
 * TODO: Implement equals and hashCode, rather than forcing string
 * comparisons in the tests?
 */
public class Schedule {

  private static int defaultRetryDelayMillis = (5 * 60 * 1000);

  private String connectorName;
  private boolean disabled;
  private int load;
  private int retryDelayMillis; // maximum of ~24 days
  private List<ScheduleTimeInterval> timeIntervals;

  /*
   * TODO: Either formalize the versions of serialized {@code Schedule} strings,
   * or convert the serialized format to XML (or both).
   * The current Schedule versions are:
   *  <ul>
   *  <li>0 - Unknown</li>
   *  <li>1 - <code>connectorName:hostLoad:timeIntervals...</code></li>
   *  <li>2 - <code>connectorName:hostLoad:retryDelayMillis:timeIntervals...</code>
   *          adds retryDelayMillis.</li>
   *  <li>3 - <code>#connectorName:hostLoad:retryDelayMillis:timeIntervals...</code>
   *          where leading '#' indicates disabled schedule, and a
   *          retryDelayMillis value of -1 indicates traverse to until
   *          no new content, then automatically disable.</li>
   *  </ul>
   */
  public static final int CURRENT_VERSION = 3;

  /**
   * Signal to the Traverser that it should traverse the ECM repository
   * until there is not new content, then stop.
   */
  public static final int POLLING_DISABLED = -1;

  /**
   * Construct an empty, disabled Schedule.
   */
  public Schedule() {
    this(null, true, 0, -1, (List<ScheduleTimeInterval>) null);
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
   * @param timeIntervals Time intervals in the format of {1-2,3-8}
   */
  public Schedule(String connectorName, boolean disabled, int load,
      int retryDelayMillis, List<ScheduleTimeInterval> timeIntervals) {
    this.connectorName = connectorName;
    this.load = load;
    this.disabled = disabled;
    this.retryDelayMillis = retryDelayMillis;
    setTimeIntervals(timeIntervals);
  }

  /**
   * Create a schedule object.
   *
   * @param scheduleProto String readable by readString() method
   */
  public Schedule(String scheduleProto) {
    if (scheduleProto == null || scheduleProto.trim().length() == 0) {
      scheduleProto = "#:0:-1:";
    }
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
   * Return a legacy representation of the supplied schedule.
   * Legacy schedules do not have a delay field or disabled flag.
   * Only sent to a GSA that does not understand the delay field.
   *
   * @param scheduleStr a schedule string.
   * @return a schedule string without the delay field or disabled flag.
   */
  public static String toLegacyString(String scheduleStr) {
    Schedule schedule = new Schedule(scheduleStr);
    return (schedule.connectorName + ":" + schedule.load + ":"
            + schedule.getTimeIntervalsAsString());
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
   */
  public void readString(String schedule) {
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
      if ((strs.length > 3) && (strs[2].indexOf('-') <= 0)) {
        retryDelayMillis = Integer.parseInt(strs[2]);
        intervals = strs[3];
      } else {
        // This is a legacy string without the retryDelay.  Resplit.
        retryDelayMillis = defaultRetryDelayMillis;
        strs = schedule.trim().split(":", 3);
        intervals = strs[2];
      }
      setTimeIntervals(parseTimeIntervals(intervals));
    } catch(Exception e) {
      throw new IllegalArgumentException("Invalid schedule string format: \""
                                         + schedule + "\"");
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
    if (intervals == null || (intervals.trim().length() == 0)) {
      return null;
    }
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
    // If we have a null List, make it an empty one.
    if (timeIntervals == null) {
      this.timeIntervals = new ArrayList<ScheduleTimeInterval>(0);
    } else {
      this.timeIntervals = timeIntervals;
    }
  }

  public void setTimeIntervals(String timeIntervals) {
    setTimeIntervals(parseTimeIntervals(timeIntervals));
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  /**
   * @return String of the form: e.g. "1-2:3-5",
   *         or empty string if Schedule has no timeIntervals.
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
