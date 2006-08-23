package com.google.enterprise.connector.test;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class TimeUtilsTest extends TestCase {

  public void testCalendarToIso8601() {
    Calendar c = Calendar.getInstance();
    Date d = new Date(999);
    c.setTime(d);
    String s = TimeUtils.calendarToIso8601(c);
    Assert.assertEquals(s, "1970-01-01T00:00:00.999Z");
  }

  public void testIso8601ToCalendar() throws ParseException {
    {
      String s = "1970-01-01T00:00:00.999Z";
      Calendar c = TimeUtils.iso8601ToCalendar(s);
      Date d = c.getTime();
      long millis = d.getTime();
      Assert.assertEquals(999, millis);
    }
    {
      String s = "1970-01-01T00:00:50Z";
      Calendar c = TimeUtils.iso8601ToCalendar(s);
      Date d = c.getTime();
      long millis = d.getTime();
      Assert.assertEquals(50000, millis);
    }
  }

}
