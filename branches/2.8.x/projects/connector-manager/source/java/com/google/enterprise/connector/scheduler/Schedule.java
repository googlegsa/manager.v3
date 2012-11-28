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

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.spi.TraversalSchedule;

import java.util.Calendar;
import java.util.TreeSet;

/**
 * A traversal schedule.
 */
public class Schedule implements TraversalSchedule {

  private static int defaultRetryDelayMillis = (5 * 60 * 1000);

  private String connectorName;
  private boolean disabled;
  private int load;
  private int retryDelayMillis; // maximum of ~24 days
  private String timeIntervals;
  private ScheduleTimeInterval[] scheduleIntervals;

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
    this(null, true, 0, -1, null);
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
            + schedule.getTimeIntervals());
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
      setTimeIntervals(intervals);
    } catch(Exception e) {
      throw new IllegalArgumentException("Invalid schedule string format: \""
                                         + schedule + "\"");
    }
  }

  /**
   * Parse a string of time intervals.  The returned structure is designed for
   * fast processing by {@link nextScheduledInterval()}.
   *
   * @param intervals String of the form e.g. "1-2:3-5:14-18" etc.
   * @return a non-null array of ScheduleTimeInterval objects ordered by start
   *         time
   */
  private static ScheduleTimeInterval[] parseTimeIntervals(String intervals) {
    if (intervals.length() == 0) {
      return new ScheduleTimeInterval[0];
    }
    TreeSet<ScheduleTimeInterval> timeIntervals =
        new TreeSet<ScheduleTimeInterval> ();
    for (String interval : intervals.trim().split(":")) {
      String[] startEndTime = interval.split("-");
      int startTime = Integer.parseInt(startEndTime[0]);
      int endTime = Integer.parseInt(startEndTime[1]);
      if (endTime == 0) {
        endTime = 24;
      } else if (startTime == endTime) {
        // Legacy disabled schedule, e.g. "1-1".
        continue;
      }
      if (endTime < startTime) {
        // Interval wraps midnight, split it in two.
        timeIntervals.add(new ScheduleTimeInterval(0, endTime));
        timeIntervals.add(new ScheduleTimeInterval(startTime, 24));
      } else {
        timeIntervals.add(new ScheduleTimeInterval(startTime, endTime));
      }
    }
    if (!timeIntervals.isEmpty()) {
      // Add the first interval for tomorrow to the end of the list
      // for the benefit of nextScheduledInterval.
      ScheduleTimeInterval first = timeIntervals.first();
      timeIntervals.add(new ScheduleTimeInterval(first.startTime + 24,
                                                 first.endTime + 24));
    }
    return timeIntervals.toArray(new ScheduleTimeInterval[0]);
  }

  /**
   * @return String of the form: e.g. "connector1:500:30000:1-2:3-5"
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
    buf.append(":" + getTimeIntervals());
    return buf.toString();
  }

  public String getConnectorName() {
    return connectorName;
  }

  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }

  /* @Override */
  public int getTraversalRate() {
    return getLoad();
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

  /* @Override */
  public int getRetryDelay() {
    return retryDelayMillis / 1000;
  }

  public void setRetryDelayMillis(int retryDelayMillis) {
    this.retryDelayMillis = retryDelayMillis;
  }

  /* @Override */
  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public void setTimeIntervals(String timeIntervals) {
    this.timeIntervals = (timeIntervals == null) ? "" : timeIntervals.trim();
    this.scheduleIntervals = parseTimeIntervals(this.timeIntervals);
  }

  /**
   * @return String of the form: e.g. "1-2:3-5",
   *         or empty string if Schedule has no timeIntervals.
   */
  public String getTimeIntervals() {
    return timeIntervals;
  }

  /**
   * Return {@code true} if the current time is within a scheduled traversal
   * interval; {@code false} otherwise.
   */
  /* @Override */
  public boolean inScheduledInterval() {
    return nextScheduledInterval(Calendar.getInstance()) == 0;
  }

  /**
   * Returns the number of seconds until the next scheduled traversal interval.
   * A return value of 0 (zero) indicates the current time is within a scheduled
   * traversal interval.  A returned value of -1 indicates there is no next
   * traversal interval.
   */
  /* @Override */
  public int nextScheduledInterval() {
    return nextScheduledInterval(Calendar.getInstance());
  }

  @VisibleForTesting
  int nextScheduledInterval(Calendar now) {
    int hour = now.get(Calendar.HOUR_OF_DAY);
    for (ScheduleTimeInterval interval : scheduleIntervals) {
      if ((hour >= interval.startTime) && (hour < interval.endTime)) {
        return 0;
      } else if (hour < interval.startTime) {
        return
            (interval.startTime - hour) * 3600 - now.get(Calendar.MINUTE) * 60;
      }
    }
    return -1;
  }

  /**
   * Return {@code true} if this Schedule would allow traversals to run
   * at this time; {@code false} otherwise.
   */
  /* @Override */
  public boolean shouldRun() {
    return !isDisabled() && inScheduledInterval();
  }

  /**
   * Returns a hash code value for the object.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @return {@code true} if this object is the same as the {@code obj}
   *         argument; {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Schedule other = (Schedule) obj;
    return toString().equals(other.toString());
  }

  /**
   * An interval of time used for schedules.
   */
  private static class ScheduleTimeInterval
      implements Comparable<ScheduleTimeInterval> {
    public final int startTime;
    public final int endTime;

    public ScheduleTimeInterval(int startTime, int endTime) {
      this.startTime = startTime;
      this.endTime = endTime;
    }

    /* @Override */
    public int compareTo(ScheduleTimeInterval o) {
      if (o == null) {
        return 1;
      }
      int compare = startTime - o.startTime;
      if (compare == 0) {
        compare = endTime - o.endTime;
      }
      return compare;
    }
  }
}
