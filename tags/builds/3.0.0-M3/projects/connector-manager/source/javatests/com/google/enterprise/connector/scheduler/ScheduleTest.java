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

import java.util.Calendar;

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
    Schedule schedule1 = new Schedule("connector1", false, 60, 0, strIntervals);
    assertEquals(strWithDelay, schedule1.toString());
    assertFalse(schedule1.isDisabled());
    assertEquals(strIntervals, schedule1.getTimeIntervals());
    assertEquals("connector1", schedule1.getConnectorName());
    assertEquals(60, schedule1.getTraversalRate());
    assertEquals(60, schedule1.getLoad());
    assertEquals(0, schedule1.getRetryDelay());

    Schedule schedule2 = new Schedule("whatever", false, 30, 42, strIntervals);
    schedule2.readString(strWithDelay);
    assertEquals(strWithDelay, schedule2.toString());
    schedule2.readString(strNoDelay);
    assertEquals(strWithDefaultDelay, schedule2.toString());

    Schedule schedule3 = new Schedule(strWithDelay);
    assertEquals(strWithDelay, schedule3.toString());
    assertFalse(schedule3.isDisabled());
    assertEquals(strIntervals, schedule3.getTimeIntervals());
    assertEquals("connector1", schedule3.getConnectorName());
    assertEquals(60, schedule3.getTraversalRate());
    assertEquals(60, schedule3.getLoad());
    assertEquals(0, schedule3.getRetryDelay());

    // Missing delay becomes Default.
    Schedule schedule4 = new Schedule(strNoDelay);
    assertEquals(strWithDefaultDelay, schedule4.toString());
    assertEquals(300, schedule4.getRetryDelay());

    // Empty Schedules are now permitted.
    Schedule schedule5 = new Schedule("connector2", false, 60, 0, null);
    assertNotNull(schedule5.getTimeIntervals());
    assertEquals("", schedule5.getTimeIntervals());
    assertEquals(strWithDelayNoIntervals, schedule5.toString());

    Schedule schedule5b = new Schedule("connector2", false, 60, 0, "");
    assertNotNull(schedule5b.getTimeIntervals());
    assertEquals("", schedule5b.getTimeIntervals());
    assertEquals(strWithDelayNoIntervals, schedule5b.toString());

    Schedule schedule5c = new Schedule(strWithDelayNoIntervals);
    assertEquals(strWithDelayNoIntervals, schedule5c.toString());

    Schedule schedule5d = new Schedule(strNoDelayNoIntervals);
    assertEquals(strWithDefaultDelayNoIntervals, schedule5d.toString());

    // Test Disabled Schedules.
    Schedule schedule9 = new Schedule("connector1", true, 60, 0, strIntervals);
    assertEquals(strWithDisabled, schedule9.toString());
    assertTrue(schedule9.isDisabled());
    assertFalse(schedule9.shouldRun());

    Schedule schedule10 = new Schedule(strWithDisabled);
    assertEquals(strWithDisabled, schedule10.toString());
    assertTrue(schedule10.isDisabled());
    assertFalse(schedule10.shouldRun());

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

  public void testGettersAndSetters() throws Exception {
    Schedule schedule = new Schedule();

    schedule.setConnectorName("connector1");
    assertEquals("connector1", schedule.getConnectorName());

    schedule.setDisabled(true);
    assertTrue(schedule.isDisabled());
    schedule.setDisabled(false);
    assertFalse(schedule.isDisabled());

    schedule.setLoad(2000);
    assertEquals(2000, schedule.getLoad());
    assertEquals(2000, schedule.getTraversalRate());

    schedule.setDefaultRetryDelaySecs(20);
    assertEquals(20000, schedule.defaultRetryDelayMillis());

    schedule.setRetryDelayMillis(30000);
    assertEquals(30000, schedule.getRetryDelayMillis());
    assertEquals(30, schedule.getRetryDelay());

    schedule.setTimeIntervals(null);
    assertEquals("", schedule.getTimeIntervals());
    schedule.setTimeIntervals("");
    assertEquals("", schedule.getTimeIntervals());
    schedule.setTimeIntervals("0-0");
    assertEquals("0-0", schedule.getTimeIntervals());
    schedule.setTimeIntervals("0-4:8-14");
    assertEquals("0-4:8-14", schedule.getTimeIntervals());

    assertEquals("connector1:2000:30000:0-4:8-14", schedule.toString());
  }

  public void testNoIntervals() {
    Schedule schedule = new Schedule(strWithDelayNoIntervals);
    assertFalse(schedule.shouldRun());
    assertFalse(schedule.inScheduledInterval());
    assertEquals(-1, schedule.nextScheduledInterval());
    assertEquals("", schedule.getTimeIntervals());
  }

  public void testAlwaysRun() {
    Schedule schedule = new Schedule(strWithDelayNoIntervals + "0-0");
    assertTrue(schedule.shouldRun());
    assertTrue(schedule.inScheduledInterval());
    assertEquals(0, schedule.nextScheduledInterval());
    assertEquals("0-0", schedule.getTimeIntervals());
  }

  public void testNextScheduleInterval() throws Exception {
    // No Schedule intervals.
    testIntervals("", new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 });

    // Legacy disabled schedule.
    testIntervals("1-1", new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
         -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 });

    // Always run.
    testIntervals("0-0", new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

    // Always run.
    testIntervals("0-24", new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

    // Only in the AM.
    testIntervals("0-12", new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 });

    // Only in the PM.
    testIntervals("12-24", new int[] { 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

    // Two 4 hour intervals.
    int [] expected = new int[] { 4, 3, 2, 1, 0, 0, 0, 0, 8, 7, 6, 5,
                                  4, 3, 2, 1, 0, 0, 0, 0, 8, 7, 6, 5 };
    testIntervals("4-8:16-20", expected);

    // Out of order intervals should have no impact.
    testIntervals("16-20:4-8", expected);

    // Increasing gaps between intervals.
    testIntervals("0-1:2-3:5-6:9-10:14-15:20-21", new int[] { 0, 1, 0, 2, 1, 0,
        3, 2, 1, 0, 4, 3, 2, 1, 0, 5, 4, 3, 2, 1, 0, 3, 2, 1 });

    // Schedule interval wraps midnight.
    testIntervals("18-8", new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 10, 9, 8, 7, 6,
        5, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0 });

    // Schedule interval wraps midnight, and one that does not.
    testIntervals("18-8:12-13", new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 4, 3, 2, 1,
        0, 5, 4, 3, 2, 1, 0, 0, 0, 0, 0, 0 });
  }

  /**
   * Tests number of hours until next traversal interval for each hour of the day.
   */
  private void testIntervals(String intervals, int[] nextIntervalHours) {
    Schedule schedule = new Schedule("name", false, 500, 60, intervals);
    Calendar calendar = Calendar.getInstance();
    // We only care about hours and minutes. Ignore date, seconds, millis, etc.
    calendar.set(Calendar.MINUTE, 0);
    for (int i = 0; i < 24; i++) {
      calendar.set(Calendar.HOUR_OF_DAY, i);
      int nextInterval = (nextIntervalHours[i] < 0) ? nextIntervalHours[i]
                         : nextIntervalHours[i] * 3600;
      assertEquals("Hour = " + i, nextInterval,
                   schedule.nextScheduledInterval(calendar));
    }
  }

  /** Test Fractions of an hour remaining until next traversal interval. */
  public void testIntervalMinutes() {
    Schedule schedule = new Schedule("name", false, 500, 60, "2-3");
    Calendar calendar = Calendar.getInstance();

    calendar.set(Calendar.HOUR_OF_DAY, 1);
    calendar.set(Calendar.MINUTE, 40);
    assertEquals(20 * 60, schedule.nextScheduledInterval(calendar));

    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 15);
    assertEquals((60 + 45) * 60, schedule.nextScheduledInterval(calendar));

    calendar.set(Calendar.HOUR_OF_DAY, 2);
    calendar.set(Calendar.MINUTE, 50);
    assertEquals(0, schedule.nextScheduledInterval(calendar));

    calendar.set(Calendar.HOUR_OF_DAY, 20);
    calendar.set(Calendar.MINUTE, 20);
    assertEquals((((5 * 60) + 40) * 60),
                 schedule.nextScheduledInterval(calendar));
  }
}
