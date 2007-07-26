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
  public void testSerialization() {
    final String str = "connector1:60:0:1-2:3-5";
    final String strNoDelay = "connector1:60:1-2:3-5";
    
    List intervals = new ArrayList();
    ScheduleTimeInterval interval1 = 
      new ScheduleTimeInterval(new ScheduleTime(1), new ScheduleTime(2));
    ScheduleTimeInterval interval2 = 
      new ScheduleTimeInterval(new ScheduleTime(3), new ScheduleTime(5));
    intervals.add(interval1);
    intervals.add(interval2);
    Schedule schedule = new Schedule("connector1", 60, 0, intervals);
    Assert.assertEquals(str, schedule.toString());
    
    Schedule schedule2 = new Schedule("whatever", 30, 42, 
        Collections.EMPTY_LIST);
    schedule2.readString(str);
    Assert.assertEquals(str, schedule2.toString());
    
    Schedule schedule3 = new Schedule(str);
    Assert.assertEquals(str, schedule3.toString());
    
    Schedule schedule4 = new Schedule(strNoDelay);
    Assert.assertEquals(str,schedule3.toString()); // missing delay becomes 0
  }
}
