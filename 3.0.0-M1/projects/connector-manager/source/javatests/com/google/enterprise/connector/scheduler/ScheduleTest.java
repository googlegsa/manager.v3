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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Schedule class.
 */
public class ScheduleTest extends TestCase {

  private static final String strWithDelay = "connector1:60:0:1-2:3-5";
  private static final String strWithDefaultDelay =
      "connector1:60:300000:1-2:3-5";
  private static final String strNoDelay = "connector1:60:1-2:3-5";
  private static final String strIntervals = "1-2:3-5";
  private static final String strWithDisabled = "#connector1:60:0:1-2:3-5";
  private static final String strWithDelayNoIntervals = "connector2:60:0:";
  private static final String strWithDefaultDelayNoIntervals =
      "connector2:60:300000:";
  private static final String strNoDelayNoIntervals = "connector2:60:";
  private static final String[] illegalSchedules =
      {"connector1", "connector2:60", "connector2:60:0", "connector1:60:0:1",
          "connector1:xyx:0:1", "connector1:60:0:xyzzy",
          "connector1:60:0:1-2:3", "connector1:60:0:1-2:3:"};

  public void testSerialization() {
    List<ScheduleTimeInterval> intervals =
        new ArrayList<ScheduleTimeInterval>();
    ScheduleTimeInterval interval1 =
        new ScheduleTimeInterval(new ScheduleTime(1), new ScheduleTime(2));
    ScheduleTimeInterval interval2 =
        new ScheduleTimeInterval(new ScheduleTime(3), new ScheduleTime(5));
    intervals.add(interval1);
    intervals.add(interval2);
    Schedule schedule = new Schedule("connector1", false, 60, 0, intervals);
    assertEquals(strWithDelay, schedule.toString());

    Schedule schedule1 = new Schedule("connector1", false, 60, 0, strIntervals);
    assertEquals(strWithDelay, schedule1.toString());

    Schedule schedule2 = new Schedule("whatever", false, 30, 42, intervals);
    schedule2.readString(strWithDelay);
    assertEquals(strWithDelay, schedule2.toString());
    schedule2.readString(strNoDelay);
    assertEquals(strWithDefaultDelay, schedule2.toString());

    Schedule schedule3 = new Schedule(strWithDelay);
    assertEquals(strWithDelay, schedule3.toString());

    // Missing delay becomes Default.
    Schedule schedule4 = new Schedule(strNoDelay);
    assertEquals(strWithDefaultDelay, schedule4.toString());

    // Empty Schedules are now permitted.
    Schedule schedule5 = new Schedule("connector2", false, 60, 0,
                                      (List<ScheduleTimeInterval>) null);
    assertNotNull(schedule5.getTimeIntervals());
    assertEquals(0, schedule5.getTimeIntervals().size());
    assertEquals(strWithDelayNoIntervals, schedule5.toString());

    Schedule schedule5a = new Schedule("connector2", false, 60, 0,
                                       new ArrayList<ScheduleTimeInterval>(0));
    assertNotNull(schedule5a.getTimeIntervals());
    assertEquals(0, schedule5a.getTimeIntervals().size());
    assertEquals(strWithDelayNoIntervals, schedule5a.toString());

    Schedule schedule5b = new Schedule("connector2", false, 60, 0, "");
    assertNotNull(schedule5b.getTimeIntervals());
    assertEquals(0, schedule5b.getTimeIntervals().size());
    assertEquals(strWithDelayNoIntervals, schedule5b.toString());

    Schedule schedule5c = new Schedule(strWithDelayNoIntervals);
    assertEquals(strWithDelayNoIntervals, schedule5c.toString());

    Schedule schedule5d = new Schedule(strNoDelayNoIntervals);
    assertEquals(strWithDefaultDelayNoIntervals, schedule5d.toString());

    // Test Disabled Schedules.
    Schedule schedule9 = new Schedule("connector1", true, 60, 0, intervals);
    assertEquals(strWithDisabled, schedule9.toString());

    Schedule schedule10 = new Schedule(strWithDisabled);
    assertEquals(strWithDisabled, schedule10.toString());

    for (String badSched : illegalSchedules) {
      try {
        new Schedule(badSched);
        fail("IllegalArgumentException expected for invalid schedule \""
             + badSched + "\"");
      } catch (IllegalArgumentException e) {
        // Expected exception occurred.
        assertEquals("Invalid schedule string format: \"" + badSched + "\"",
                     e.getMessage());
      }
    }
  }

  public void testToLegacyString() {
    // Identity should work.
    assertEquals(strNoDelay, Schedule.toLegacyString(strNoDelay));

    // Legacy format doesn't include delay.
    assertEquals(strNoDelay, Schedule.toLegacyString(strWithDelay));

    // Legacy format doesn't include disabled.
    assertEquals(strNoDelay, Schedule.toLegacyString(strWithDisabled));

    // Legacy format does support no time intervals.
    assertEquals(strNoDelayNoIntervals,
        Schedule.toLegacyString(strWithDelayNoIntervals));
    assertEquals(strNoDelayNoIntervals,
        Schedule.toLegacyString(strNoDelayNoIntervals));

    // Invalid schedule strings should throw IllegalArgumentException.
    for (String badSched : illegalSchedules) {
      try {
        Schedule.toLegacyString(badSched);
        fail("IllegalArgumentException expected for invalid schedule \""
             + badSched + "\"");
      } catch (IllegalArgumentException e) {
        // Expected exception occurred.
        assertEquals("Invalid schedule string format: \"" + badSched + "\"",
                     e.getMessage());
      }
    }
  }
}
