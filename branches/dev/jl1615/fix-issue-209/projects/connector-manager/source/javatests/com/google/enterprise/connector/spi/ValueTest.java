// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.spi;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Unit tests for SimpleValue class
 */
public class ValueTest extends TestCase {

  public void testCalendarToIso8601() {
    // We're comparing full strings here, so we need a fixed time zone.
    Value.setTimeZone("GMT");

    Calendar c = Calendar.getInstance();
    Date d = new Date(999);
    c.setTime(d);
    String s = Value.calendarToIso8601(c);
    Assert.assertEquals("1970-01-01T00:00:00.999Z", s);
  }

  public void testIso8601ToCalendar() throws ParseException {
    {
      String s = "1970-01-01T00:00:00.999Z";
      Calendar c = Value.iso8601ToCalendar(s);
      Date d = c.getTime();
      long millis = d.getTime();
      Assert.assertEquals(999, millis);
    }
    {
      String s = "1970-01-01T00:00:50Z";
      Calendar c = Value.iso8601ToCalendar(s);
      Date d = c.getTime();
      long millis = d.getTime();
      Assert.assertEquals(50000, millis);
    }
  }

  public void testCalendarToRfc822() {
    // We're comparing full strings here, so we need a fixed time zone.
    Value.setTimeZone("GMT");

    Calendar c = Calendar.getInstance();
    Date d = new Date(999);
    c.setTime(d);
    String s = Value.calendarToRfc822(c);
    Assert.assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", s);
  }

  /**
   * Tests converting a timestamp with the default local time zone,
   * setting the time zone, and converting the same timestamp again.
   * 
   * NOTE: We want all of the Value methods to be called in this same
   * test method, because the Java date-time classes do some strange
   * cloning behind the scenes, and we need to make sure that
   * Value.setTimeZone correctly affects all of the SimpleDateFormat
   * instances in the Value class.
   */
  public void testTimeZone() {
    Calendar timestamp = Calendar.getInstance();
    //Value.setTimeZone("GMT");
    //timestamp.setTimeZone(TimeZone.getTimeZone("GMT"));

    timestamp.clear();
    int offset = timestamp.get(Calendar.ZONE_OFFSET)
        + timestamp.get(Calendar.DST_OFFSET);
    int minutes = offset / 60 / 1000;
    String timezone = String.format("%+03d%02d", minutes / 60, minutes % 60);
    System.out.println("timezone = " + timezone);
    String localRfc822;
    String localIso8601;
    String localRfc822TimeZone;
    String localIso8601TimeZone;
    String utcRfc822;
    String utcIso8601;
    String utcRfc822TimeZone;
    String utcIso8601TimeZone;
    String utcId = "GMT";
    if (offset < 0) {
      System.out.println("West");
      timestamp.set(2000, 11 /* sic */, 31, 23, 59, 01);
      localRfc822 = "Sun, 31 Dec 2000";
      localIso8601 = "2000-12-31";
      localRfc822TimeZone = timezone;
      localIso8601TimeZone = timezone;
      utcRfc822 = "Mon, 01 Jan 2001";
      utcIso8601 = "2001-01-01";
      utcRfc822TimeZone = "GMT";
      utcIso8601TimeZone = "Z";
    } else if (offset > 0) {
      System.out.println("East");
      timestamp.set(2001, 0 /* sic */, 1, 00, 00, 59);
      localRfc822 = "Mon, 01 Jan 2001";
      localIso8601 = "2001-01-01";
      localRfc822TimeZone = timezone;
      localIso8601TimeZone = timezone;
      utcRfc822 = "Sun, 31 Dec 2000";
      utcIso8601 = "2000-12-31";
      utcRfc822TimeZone = "GMT";
      utcIso8601TimeZone = "Z";
    } else {
      System.out.println("Middle");
      timestamp.set(2001, 0 /* sic */, 1, 00, 00, 59);
      localRfc822 = "Mon, 01 Jan 2001";
      localIso8601 = "2001-01-01";
      localRfc822TimeZone = "GMT";
      localIso8601TimeZone = "Z";
      utcRfc822 = "Sun, 31 Dec 2000";
      utcIso8601 = "2000-12-31";
      utcRfc822TimeZone = "-0800"; // not timezone, which is "+0000".
      utcIso8601TimeZone = "-0800"; // ditto
      utcId = "GMT-0800";
    }

    //timestamp.setTimeZone(TimeZone.getTimeZone("GMT-0400"));
    String s;
    System.out.println(timestamp);
    
    s = Value.calendarToRfc822(timestamp);
    assertTrue(s, s.startsWith(localRfc822));
    assertTrue(s, s.endsWith(localRfc822TimeZone));
    s = Value.calendarToIso8601(timestamp);
    assertTrue(s, s.startsWith(localIso8601));
    assertTrue(s, s.endsWith(localIso8601TimeZone));
    s = Value.calendarToFeedXml(timestamp);
    assertEquals(localIso8601, s);

    Value.setTimeZone(utcId);

    s = Value.calendarToRfc822(timestamp);
    assertTrue(s, s.startsWith(utcRfc822));
    assertTrue(s, s.endsWith(utcRfc822TimeZone));
    s = Value.calendarToIso8601(timestamp);
    assertTrue(s, s.startsWith(utcIso8601));
    assertTrue(s, s.endsWith(utcIso8601TimeZone));
    s = Value.calendarToFeedXml(timestamp);
    assertEquals(utcIso8601, s);
  }
}
