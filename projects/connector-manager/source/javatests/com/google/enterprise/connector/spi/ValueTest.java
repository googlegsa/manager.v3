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
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.util.InputStreamFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Unit tests for the {@link Value} class.
 */
public class ValueTest extends TestCase {
  @Override
  public void setUp() {
    // RFC 822 is English-only, and ISO 8601 isn't locale-sensitive,
    // so change the locale but expect the same results.
    Locale.setDefault(Locale.FRENCH);
  }

  @Override
  public void tearDown() {
    // Reset the default time zone.
    Value.setFeedTimeZone("");
  }

  public void testCalendarToIso8601() {
    // We're comparing date strings here, so we need a fixed time zone.
    Value.setFeedTimeZone("GMT");

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
    // We're comparing date strings here, so we need a fixed time zone.
    Value.setFeedTimeZone("GMT");

    Calendar c = Calendar.getInstance();
    Date d = new Date(999);
    c.setTime(d);
    String s = Value.calendarToRfc822(c);
    Assert.assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", s);
  }

  /**
   * Represents the prefix and suffix of formatted timestamp strings
   * for a specific date in both the RFC 822 and ISO 8601 formats.
   */
  private static class DateFragments {
    public final String rfc822Date;
    public final String rfc822TimeZone;
    public final String iso8601Date;
    public final String iso8601TimeZone;

    public DateFragments(String rfc822Date, String rfc822TimeZone,
        String iso8601Date, String iso8601TimeZone) {
      this.rfc822Date = rfc822Date;
      this.rfc822TimeZone = rfc822TimeZone;
      this.iso8601Date = iso8601Date;
      this.iso8601TimeZone = iso8601TimeZone;
    }
  }

  /** Tests the local time zone. */
  public void testDefaultTimeZone() {
    Value.setFeedTimeZone("");
    Calendar timestamp = Calendar.getInstance();
    // We have to compute the offset in this case, because we do not
    // know the time zone. Due to Daylight Savings Time, which may
    // alter the UTC offset of any given time zone, we need to use the
    // date under test, rather than the current date, to get the right
    // offset. The date used here should match the dates in
    // testTimeZoneOFfset.
    // TODO(jlacey): Find a way to isolate this from test data changes.
    timestamp.set(2000, 11 /* zero-based */, 31);
    int offset = timestamp.get(Calendar.ZONE_OFFSET)
        + timestamp.get(Calendar.DST_OFFSET);
    int minutes = offset / 60 / 1000;
    String timezone = String.format("%+03d%02d", minutes / 60,
        Math.abs(minutes % 60));
    testTimeZoneOffset(timestamp, offset, timezone);
  }

  /**
   * Tests the given time zone.
   *
   * @param offset the offset does not have to match the
   * <code>timezone</code> value, it just needs to have the right sign
   * @param timezone the UTC offset as a timezone string, e.g., "-0800"
   */
  private void testFixedTimeZone(int offset, String timezone) {
    Value.setFeedTimeZone("GMT" + timezone);
    Calendar timestamp = Calendar.getInstance();
    timestamp.setTimeZone(TimeZone.getTimeZone("GMT" + timezone));
    testTimeZoneOffset(timestamp, offset, timezone);
  }

  /** Tests a time zone west of UTC. The details do not matter. */
  public void testWestTimeZone() {
    testFixedTimeZone(-1, "-0500");
  }

  /** Tests a time zone east of UTC. The details do not matter. */
  public void testEastTimeZone() {
    testFixedTimeZone(+1, "+1100");
  }

  /** Tests the UTC time zone. */
  public void testUtcTimeZone() {
    testFixedTimeZone(0, "+0000");
  }

  /**
   * Tests converting a timestamp with the default local time zone,
   * setting the time zone, and converting the same timestamp again.
   *
   * NOTE: We want all of the Value methods to be called in this same
   * test method, because the Java date-time classes do some strange
   * cloning behind the scenes, and we need to make sure that
   * Value.setFeedTimeZone correctly affects all of the SimpleDateFormat
   * instances in the Value class.
   *
   * @param timestamp a calendar with the time zone set as desired
   * @param offset the offset does not have to match the
   * <code>timezone</code> value, it just needs to have the right sign
   * @param timezone the UTC offset as a timezone string, e.g., "-0800"
   */
  private void testTimeZoneOffset(Calendar timestamp, int offset,
      String timezone) {
    timestamp.clear();
    DateFragments local;
    DateFragments other;
    String otherId;
    // The test and local dates here should be less than an hour from
    // midnight, so that the date in the other time zone is different.
    // If the dates used here change, especially across a DST bounday,
    // you need to change the date used in testDefaultTimeZone.
    if (offset < 0) {
      // West of prime meridian.
      timestamp.set(2000, 11 /* zero-based */, 31, 23, 59, 01);
      local = new DateFragments("Sun, 31 Dec 2000", timezone,
          "2000-12-31", timezone);
      other = new DateFragments("Mon, 01 Jan 2001", "GMT", "2001-01-01", "Z");
      otherId = "GMT";
    } else if (offset > 0) {
      // East of prime meridian.
      timestamp.set(2001, 0 /* zero-based */, 1, 00, 00, 59);
      local = new DateFragments("Mon, 01 Jan 2001", timezone,
          "2001-01-01", timezone);
      other = new DateFragments("Sun, 31 Dec 2000", "GMT", "2000-12-31", "Z");
      otherId = "GMT";
    } else {
      // UTC
      timestamp.set(2001, 0 /* zero-based */, 1, 00, 00, 59);
      local = new DateFragments("Mon, 01 Jan 2001", "GMT", "2001-01-01", "Z");
      // Do not use timezone in this case, which is "+0000".
      other = new DateFragments("Sun, 31 Dec 2000", "-0800",
          "2000-12-31", "-0800");
      otherId = "GMT-0800";
    }

    testTimeZoneFragments(timestamp, local);
    Value.setFeedTimeZone(otherId);
    testTimeZoneFragments(timestamp, other);
  }

  /**
   * Calls all three formatting methods in <code>Value</code> and
   * compares the results to the given fragments.
   *
   * @param timestamp a calendar with the time and time zone set as desired
   * @param expected the expected date string fragments
   */
  private void testTimeZoneFragments(Calendar timestamp,
      DateFragments expected) {
    String s;
    s = Value.calendarToRfc822(timestamp);
    assertTrue(s, s.startsWith(expected.rfc822Date));
    assertTrue(s, s.endsWith(expected.rfc822TimeZone));
    s = Value.calendarToIso8601(timestamp);
    assertTrue(s, s.startsWith(expected.iso8601Date));
    assertTrue(s, s.endsWith(expected.iso8601TimeZone));
    s = Value.calendarToFeedXml(timestamp);
    assertEquals(expected.iso8601Date, s);
  }

  private static final String TEST_DIR = "testdata/contextTests/value/";

  /** Tests the default Spring configuration. */
  public void testDefaultConfig() throws Exception {
    testConfig(TEST_DIR + "default.xml", TimeZone.getDefault().getID());
  }

  /**
   * Tests the Spring configuration and an explicit time zone. This
   * test uses an offset, because those are unlikely to be the default
   * time zone, due to Daylight Savings Time. As an added but probably
   * unnecessary protection, we use GMT+0100, which is almost empty
   * during standard time or DST.
   */
  public void testOffsetConfig() throws Exception {
    testConfig(TEST_DIR + "offset.xml", "GMT+01:00");
  }

  private void testConfig(String applicationContext, String expectedId) {
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(applicationContext, null);

    assertEquals(expectedId, Value.getFeedTimeZone());
  }

  private static final String CONTENTS = "Test Content";
  private static final byte[] CONTENTS_BYTES = StringUtils.getBytes(CONTENTS);

  /** Tests BinaryValue constructors - byte array. */
  public void testBinaryValueByteArray() throws Exception {
    testBinaryValue(Value.getBinaryValue(CONTENTS_BYTES));
  }

  /** Tests BinaryValue constructors - InputStream. */
  public void testBinaryValueInputStream() throws Exception {
    testBinaryValue(Value.getBinaryValue(
        new ByteArrayInputStream(CONTENTS_BYTES)));
  }

  /** Tests BinaryValue constructors - InputStreamFactory. */
  public void testBinaryValueInputStreamFactory() throws Exception {
    testBinaryValue(Value.getBinaryValue(
        new InputStreamFactory() {
          public InputStream getInputStream() {
            return new ByteArrayInputStream(CONTENTS_BYTES);
          }
        }));
  }

  /** Tests BinaryValue constructors - InputStreamFactory. */
  public void testBinaryValueInputStreamFactoryError() throws Exception {
    BinaryValue value = (BinaryValue) Value.getBinaryValue(
        new InputStreamFactory() {
          public InputStream getInputStream() throws IOException {
            throw new IOException("Test Exception");
          }
        });

    try {
      value.getInputStream();
      fail("Expected RepositoryDocumentException, but got none.");
    } catch (RepositoryDocumentException expected) {
      // Expected.
    }
  }

  /** Tests reading a BinaryValue. */
  public void testBinaryValue(Value value) throws Exception {
    assertTrue(value instanceof BinaryValue);
    InputStream is = ((BinaryValue) value).getInputStream();
    assertNotNull(is);
    assertEquals(CONTENTS, StringUtils.streamToStringAndThrow(is));
  }

  /** Asserts that the Value represents a boolean of the expected value. */
  private void assertValueEquals(boolean expected, Value value) {
    assertEquals(value.toString(), BooleanValue.class, value.getClass());
    assertEquals(value.toString(),
        expected, ((BooleanValue) value).toBoolean());
  }

  public void testBooleanValue_f() {
    assertValueEquals(false, Value.getBooleanValue("f"));
  }

  public void testBooleanValue_false() {
    assertValueEquals(false, Value.getBooleanValue("false"));
  }

  public void testBooleanValue_FaLsE() {
    assertValueEquals(false, Value.getBooleanValue("FaLsE"));
  }

  public void testBooleanValue_t() {
    assertValueEquals(true, Value.getBooleanValue("t"));
  }

  public void testBooleanValue_true() {
    assertValueEquals(true, Value.getBooleanValue("true"));
  }

  public void testBooleanValue_TRUE() {
    assertValueEquals(true, Value.getBooleanValue("TRUE"));
  }

  public void testBooleanValue_helloworld() {
    assertValueEquals(true, Value.getBooleanValue("hello, world"));
  }

  public void testBooleanValue_empty() {
    assertValueEquals(true, Value.getBooleanValue(""));
  }

  public void testBooleanValue_null() {
    assertValueEquals(true, Value.getBooleanValue(null));
  }
}
