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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test Schedule class.
 */
public class ScheduleTest extends TestCase {

  final String strWithDelay = "connector1:60:0:1-2:3-5";
  final String strWithDefaultDelay = "connector1:60:300000:1-2:3-5";
  final String strNoDelay = "connector1:60:1-2:3-5";
  final String strIntervals = "1-2:3-5";
  final String strWithDisabled = "#connector1:60:0:1-2:3-5";
  final String illegalStrWithDelay = "connector2:60:0:";
  final String illegalStrNoDelay = "connector2:60:";

  public void testSerialization() {
    List intervals = new ArrayList();
    ScheduleTimeInterval interval1 =
      new ScheduleTimeInterval(new ScheduleTime(1), new ScheduleTime(2));
    ScheduleTimeInterval interval2 =
      new ScheduleTimeInterval(new ScheduleTime(3), new ScheduleTime(5));
    intervals.add(interval1);
    intervals.add(interval2);
    Schedule schedule = new Schedule("connector1", false, 60, 0, intervals);
    Assert.assertEquals(strWithDelay, schedule.toString());

    Schedule schedule1 = new Schedule("connector1", false, 60, 0, strIntervals);
    Assert.assertEquals(strWithDelay, schedule1.toString());

    Schedule schedule2 = new Schedule("whatever", false, 30, 42, intervals);
    schedule2.readString(strWithDelay);
    Assert.assertEquals(strWithDelay, schedule2.toString());
    schedule2.readString(strNoDelay);
    Assert.assertEquals(strWithDefaultDelay, schedule2.toString());

    Schedule schedule3 = new Schedule(strWithDelay);
    Assert.assertEquals(strWithDelay, schedule3.toString());

    // Missing delay becomes Default.
    Schedule schedule4 = new Schedule(strNoDelay);
    Assert.assertEquals(strWithDefaultDelay, schedule4.toString());

    try {
      new Schedule("connector2", false, 60, 0, Collections.EMPTY_LIST);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // Expected exception occurred.
    }

    try {
      Schedule schedule6 = new Schedule("whatever", false, 30, 42, intervals);
      schedule6.readString(illegalStrWithDelay);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // Expected exception occurred.
    }

    try {
      new Schedule(illegalStrWithDelay);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // Expected exception occurred.
    }

    try {
      new Schedule(illegalStrNoDelay);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // Expected exception occurred.
    }

    Schedule schedule9 = new Schedule("connector1", true, 60, 0, intervals);
    Assert.assertEquals(strWithDisabled, schedule9.toString());

    Schedule schedule10 = new Schedule(strWithDisabled);
    Assert.assertEquals(strWithDisabled, schedule10.toString());
  }

  public void testToLegacyString() {
    Assert.assertEquals(strNoDelay, Schedule.toLegacyString(strWithDelay));
    Assert.assertEquals(strNoDelay, Schedule.toLegacyString(strNoDelay));
    try {
      Schedule.toLegacyString(illegalStrWithDelay);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // Expected exception occurred.
    }
    try {
      Schedule.toLegacyString(illegalStrNoDelay);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // Expected exception occurred.
    }
  }
}
