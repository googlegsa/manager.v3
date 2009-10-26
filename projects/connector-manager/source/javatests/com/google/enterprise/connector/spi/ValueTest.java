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
import java.util.Locale;

/**
 * Unit tests for the {@link SimpleValue} class.
 */
public class ValueTest extends TestCase {

  @Override
  public void setUp() {
    // RFC 822 is English-only, and ISO 8601 isn't locale-sensitive,
    // so change the locale but expect the same results.
    Locale.setDefault(Locale.FRENCH);
  }

  public void testCalendarToIso8601() {
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
    Calendar c = Calendar.getInstance();
    Date d = new Date(999);
    c.setTime(d);
    String s = Value.calendarToRfc822(c);
    Assert.assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", s);
  }

}
