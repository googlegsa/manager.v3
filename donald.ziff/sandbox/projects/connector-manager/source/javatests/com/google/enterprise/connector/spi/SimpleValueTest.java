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

import com.google.enterprise.connector.common.StringUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Unit tests for SimpleValue class
 */
public class SimpleValueTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleValue#getBoolean()}.
   * 
   * @throws RepositoryException
   * @throws IllegalArgumentException
   */
  public final void testGetBoolean() throws IllegalArgumentException,
      RepositoryException {
    Assert.assertEquals(true, new SimpleValue(ValueType.BOOLEAN, "true")
        .getBoolean());
    Assert.assertEquals(true, new SimpleValue(ValueType.BOOLEAN, "T")
        .getBoolean());
    Assert.assertEquals(true, new SimpleValue(ValueType.BOOLEAN, "TrUe")
        .getBoolean());
    Assert.assertEquals(false, new SimpleValue(ValueType.BOOLEAN,
        "true for you maybe").getBoolean());
    Assert.assertEquals(false, new SimpleValue(ValueType.BOOLEAN, "false")
        .getBoolean());
    Assert.assertEquals(false, new SimpleValue(ValueType.BOOLEAN, "F")
        .getBoolean());
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleValue#getDate()}.
   * 
   * @throws RepositoryException
   * @throws IllegalArgumentException
   */
  public final void testGetDate() throws IllegalArgumentException,
      RepositoryException {
    Assert.assertEquals(999, new SimpleValue(ValueType.DATE,
        "1970-01-01T00:00:00.999Z").getDate().getTimeInMillis());
    Assert.assertEquals(50000, new SimpleValue(ValueType.DATE,
        "1970-01-01T00:00:50Z").getDate().getTimeInMillis());
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleValue#getDouble()}.
   * 
   * @throws RepositoryException
   * @throws IllegalArgumentException
   */
  public final void testGetDouble() throws IllegalArgumentException,
      RepositoryException {
    Assert.assertEquals(0.0,
        new SimpleValue(ValueType.DOUBLE, "0").getDouble(), 0.00001);
    Assert.assertEquals(100.0, new SimpleValue(ValueType.DOUBLE, "100.000")
        .getDouble(), 0.00001);
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleValue#getLong()}.
   * 
   * @throws RepositoryException
   * @throws IllegalArgumentException
   */
  public final void testGetLong() throws IllegalArgumentException,
      RepositoryException {
    Assert.assertEquals(0, new SimpleValue(ValueType.DOUBLE, "0").getLong());
    Assert.assertEquals(-100, new SimpleValue(ValueType.DOUBLE, "-100")
        .getLong());
    try {
      Assert.assertEquals(12345, new SimpleValue(ValueType.DOUBLE, "12345.0")
          .getLong());
      fail("shouldn't get here");
    } catch (IllegalArgumentException e) {
      // this exception is expected
    } catch (Throwable t) {
      fail("unexpected throwable " + t.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleValue#getStream()}.
   * 
   * @throws RepositoryException
   * @throws IllegalStateException
   * @throws IllegalArgumentException
   */
  public final void testGetStream() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    {
      String testString = "Now is the time";
      SimpleValue simpleValue =
          new SimpleValue(ValueType.BINARY, testString.getBytes());
      String resultString =
          StringUtils.streamToString(simpleValue.getStream());
      Assert.assertEquals(testString, resultString);
    }
    {
      String testString = "abcdefghijklmnopqrstuvwxyz";
      SimpleValue simpleValue =
          new SimpleValue(ValueType.BINARY, testString.getBytes());
      String resultString =
          StringUtils.streamToString(simpleValue.getStream());
      Assert.assertEquals(testString, resultString);
    }
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleValue#getString()}.
   * 
   * @throws RepositoryException
   * @throws IllegalArgumentException
   */
  public final void testGetString() throws IllegalArgumentException,
      RepositoryException {
    Assert
        .assertEquals("0", new SimpleValue(ValueType.STRING, "0").getString());
    Assert.assertEquals("foo", new SimpleValue(ValueType.STRING, "foo")
        .getString());
    Assert.assertEquals("true", new SimpleValue(ValueType.STRING, "true")
        .getString());
    Assert.assertEquals("100.000", new SimpleValue(ValueType.STRING, "100.000")
        .getString());
    Assert.assertEquals("12345.0", new SimpleValue(ValueType.STRING, "12345.0")
        .getString());
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleValue#toString()}.
   * 
   * @throws RepositoryException
   * @throws IllegalArgumentException
   */
  public final void testToString() throws IllegalArgumentException,
      RepositoryException {
    Assert
        .assertEquals("0", new SimpleValue(ValueType.STRING, "0").toString());
    Assert.assertEquals("foo", new SimpleValue(ValueType.STRING, "foo")
        .toString());
    Assert.assertEquals("true", new SimpleValue(ValueType.STRING, "true")
        .toString());
    Assert.assertEquals("100.000", new SimpleValue(ValueType.STRING, "100.000")
        .toString());
    Assert.assertEquals("12345.0", new SimpleValue(ValueType.STRING, "12345.0")
    .toString());
    String str = new TestSimpleValue(ValueType.STRING, "12345.0")
    .toString();
    Assert.assertNotNull(str);
    Assert.assertTrue("Actual value: " + str, str.indexOf("RepositoryException") >= 0); 
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.spi.SimpleValue#getType()}.
   * 
   * @throws RepositoryException
   * @throws IllegalArgumentException
   */
  public final void testGetType() throws IllegalArgumentException,
      RepositoryException {
    Assert.assertEquals(ValueType.STRING,
        new SimpleValue(ValueType.STRING, "0").getType());
    Assert.assertEquals(ValueType.DOUBLE, new SimpleValue(ValueType.DOUBLE,
        "foo").getType());
    Assert.assertEquals(ValueType.BOOLEAN, new SimpleValue(ValueType.BOOLEAN,
        "true").getType());
  }

  public void testCalendarToIso8601() {
    Calendar c = Calendar.getInstance();
    Date d = new Date(999);
    c.setTime(d);
    String s = SimpleValue.calendarToIso8601(c);
    Assert.assertEquals("1970-01-01T00:00:00.999Z", s);
  }

  public void testIso8601ToCalendar() throws ParseException {
    {
      String s = "1970-01-01T00:00:00.999Z";
      Calendar c = SimpleValue.iso8601ToCalendar(s);
      Date d = c.getTime();
      long millis = d.getTime();
      Assert.assertEquals(999, millis);
    }
    {
      String s = "1970-01-01T00:00:50Z";
      Calendar c = SimpleValue.iso8601ToCalendar(s);
      Date d = c.getTime();
      long millis = d.getTime();
      Assert.assertEquals(50000, millis);
    }
  }

  public void testCalendarToRfc822() {
    Calendar c = Calendar.getInstance();
    Date d = new Date(999);
    c.setTime(d);
    String s = SimpleValue.calendarToRfc822(c);
    Assert.assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", s);
  }

  class TestSimpleValue extends SimpleValue {
	  public TestSimpleValue(ValueType t, String v) {
		    super(t,v);
	  }
	  
	  public String getString() throws IllegalArgumentException,
	      RepositoryException {
	    throw new RepositoryException("just testing");
	  }
 }
  
}
