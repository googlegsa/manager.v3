// Copyright 2007-2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.spiimpl.ValueImpl;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Wrapper class for all data items from a repository. Connector implementors
 * create instances of this class by calling the appropriate static factory
 * method. The factory methods are named to reflect the type data they carry,
 * thus:
 * <ul>
 * <li> <code>getStringValue</code> creates an object carrying a string
 * <li> <code>getBinaryValue</code> creates an object carrying binary data
 * (stream or byte array)
 * <li> <code>getLongValue</code> creates an object carrying an integer
 * <li> <code>getDoubleValue</code> creates an object carrying a
 * floating-point value
 * <li> <code>getDateValue</code> creates an object carrying a date
 * <li> <code>getBooleanValue</code> creates an object carrying a boolean
 * </ul>
 * In addition, some of the factory methods are overloaded for the convenience
 * of the connector developer. The implementations attempt to convert from the
 * parameter type to the base type indicated in the factory method's name.
 */
public abstract class Value {

  /**
   * Creates a value carrying a String.
   *
   * @param stringValue The String value
   * @return a Value instance carrying this value
   */
  public static Value getStringValue(String stringValue) {
    return ValueImpl.getStringValue(stringValue);
  }

  /**
   * Creates a value carrying binary data.
   *
   * @param inputStreamValue An <code>InputStream</code> containing the data
   * @return a Value instance carrying this data
   */
  public static Value getBinaryValue(InputStream inputStreamValue) {
    return ValueImpl.getBinaryValue(inputStreamValue);
  }

  /**
   * Creates a value carrying binary data.
   *
   * @param byteArrayValue An <code>byte</code> array containing the data
   * @return a Value instance carrying this data
   */
  public static Value getBinaryValue(byte[] byteArrayValue) {
    return ValueImpl.getBinaryValue(byteArrayValue);
  }

  /**
   * Creates a value carrying an integer.
   *
   * @param longValue A <code>long</code> containing the data
   * @return a Value instance carrying this data
   */
  public static Value getLongValue(long longValue) {
    return ValueImpl.getLongValue(longValue);
  }

  /**
   * Creates a value carrying an integer.
   *
   * @param doubleValue A <code>double</code> containing the data
   * @return a Value instance carrying this data
   */
  public static Value getDoubleValue(double doubleValue) {
    return ValueImpl.getDoubleValue(doubleValue);
  }

  /**
   * Creates a value carrying a date.
   *
   * @param calendarValue A <code>Calendar</code> object containing the data
   * @return a Value instance carrying this data
   */
  public static Value getDateValue(Calendar calendarValue) {
    return ValueImpl.getDateValue(calendarValue);
  }

  /**
   * Creates a value carrying a boolean.
   *
   * @param booleanValue A <code>boolean</code> containing the data
   * @return a Value instance carrying this data
   */
  public static Value getBooleanValue(boolean booleanValue) {
    return ValueImpl.getBooleanValue(booleanValue);
  }

  /**
   * Creates a value carrying a boolean.
   *
   * @param stringValue A <code>String</code> containing the data. The String
   *        is converted as follows:
   *        <ul>
   *        <li> Any case variant of the strings "t" and "true" return
   *        <code>true</code>
   *        <li> All other strings (including <code>null</code> and the empty
   *        string) return <code>false<code>
   *        </ul>
   * @return a Value instance carrying this data.
   */
  public static Value getBooleanValue(String stringValue) {
    return ValueImpl.getBooleanValue(stringValue);
  }

  /**
   * Convenience function for access to a single named value from a DocumentList
   *
   * @param document the Document List from which to extract the Value
   * @param propertyName the name of the Property
   * @return The first Value of that named property, if there is one -
   *         <code>null</code> otherwise
   * @throws RepositoryException
   */
  public static Value getSingleValue(Document document,
      String propertyName) throws RepositoryException {
    Property p = document.findProperty(propertyName);
    if (p == null) {
      return null;
    }
    return p.nextValue();
  }

  /**
   * Convenience function for access to a single string value from a DocumentList
   *
   * @param document the Document List from which to extract the Value
   * @param propertyName the name of the Property
   * @return The string Value of that named property, if there is one -
   *         <code>null</code> otherwise
   * @throws RepositoryException
   */
  public static String getSingleValueString(Document document,
      String propertyName) throws RepositoryException {
    Value v = getSingleValue(document, propertyName);
    if (v == null) {
      return null;
    }
    return v.toString();
  }

  /**
   * Returns a string representation of the Value. Connector developers may
   * count on this for debugging.
   *
   * @return a string representation of the Value.
   */
  @Override
  public abstract String toString();

  private static final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT+0");
  private static final Calendar GMT_CALENDAR =
      Calendar.getInstance(TIME_ZONE_GMT);
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_MINS =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_DATE =
      new SimpleDateFormat("yyyy-MM-dd");
  public static final SimpleDateFormat RFC822_DATE_FORMAT =
      new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss z");

  static {
    ISO8601_DATE_FORMAT_MILLIS.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_MILLIS.setLenient(true);
    ISO8601_DATE_FORMAT_SECS.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_SECS.setLenient(true);
    ISO8601_DATE_FORMAT_MINS.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_MINS.setLenient(true);
    ISO8601_DATE_FORMAT_DATE.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_DATE.setLenient(true);
    RFC822_DATE_FORMAT.setCalendar(GMT_CALENDAR);
    RFC822_DATE_FORMAT.setLenient(true);
    RFC822_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  /**
   * Formats a calendar object for the Feeds Protocol, using the
   * ISO-8601 format for just the date portion.  See
   * <a href="http://code.google.com/apis/searchappliance/documentation/50/feedsguide.html">
   * Feeds Protocol Developer's Guide</a>
   *
   * @param c
   * @return a String in ISO-8601 date format
   */
  public static synchronized String calendarToFeedXml(Calendar c) {
    Date d = c.getTime();
    String isoString = ISO8601_DATE_FORMAT_DATE.format(d);
    return isoString;
  }

  /**
   * Formats a calendar object as RFC 822.
   *
   * @param c
   * @return a String in RFC 822 format - always in GMT zone
   */
  public static synchronized String calendarToRfc822(Calendar c) {
    Date d = c.getTime();
    String isoString = RFC822_DATE_FORMAT.format(d);
    return isoString;
  }

  /**
   * Formats a calendar object as ISO-8601.
   *
   * @param c
   * @return a String in ISO-8601 format - always in GMT zone
   */
  public static synchronized String calendarToIso8601(Calendar c) {
    Date d = c.getTime();
    String isoString;
    if (c.isSet(Calendar.MILLISECOND)) {
      isoString = ISO8601_DATE_FORMAT_MILLIS.format(d);
    } else if (c.isSet(Calendar.SECOND)) {
      isoString = ISO8601_DATE_FORMAT_SECS.format(d);
    } else if (c.isSet(Calendar.MINUTE)) {
      isoString = ISO8601_DATE_FORMAT_MINS.format(d);
    } else {
      isoString = ISO8601_DATE_FORMAT_DATE.format(d);
    }
    return isoString;
  }

  private static synchronized Date iso8601ToDate(String s)
      throws ParseException {
    Date d = null;
    try {
      d = ISO8601_DATE_FORMAT_MILLIS.parse(s);
    } catch (ParseException e1) {
      try {
        d = ISO8601_DATE_FORMAT_SECS.parse(s);
      } catch (ParseException e2) {
        try {
          d = ISO8601_DATE_FORMAT_MINS.parse(s);
        } catch (ParseException e3) {
          d = ISO8601_DATE_FORMAT_DATE.parse(s);
        }
      }
    }
    return d;
  }

  /**
   * Parses a String in ISO-8601 format (GMT zone) and returns an equivalent
   * java.util.Calendar object.
   *
   * @param s
   * @return a Calendar object
   * @throws ParseException if the the String can not be parsed
   */
  public static synchronized Calendar iso8601ToCalendar(String s)
      throws ParseException {
    Date d = iso8601ToDate(s);
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    return c;
  }

}
