// Copyright 2006 Google Inc.
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
import com.google.enterprise.connector.util.InputStreamFactory;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Wrapper class for all data items from a repository. Connector implementors
 * create instances of this class by calling the appropriate static factory
 * method. The factory methods are named to reflect the type data they carry,
 * thus:
 * <ul>
 * <li> {@code getStringValue} creates an object carrying a string</li>
 * <li> {@code getBinaryValue} creates an object carrying binary data
 * (stream or byte array)</li>
 * <li> {@code getLongValue} creates an object carrying an integer</li>
 * <li> {@code getDoubleValue} creates an object carrying a
 * floating-point value</li>
 * <li> {@code getDateValue} creates an object carrying a date</li>
 * <li> {@code getBooleanValue} creates an object carrying a boolean</li>
 * </ul>
 * In addition, some of the factory methods are overloaded for the convenience
 * of the connector developer. The implementations attempt to convert from the
 * parameter type to the base type indicated in the factory method's name.
 *
 * @since 1.0
 */
public abstract class Value {
  private static final Logger LOGGER = Logger.getLogger(Value.class.getName());

  /**
   * Creates a value carrying a String.
   *
   * @param stringValue the {@code String} value
   * @return a {@link Value} instance carrying this value
   */
  public static Value getStringValue(String stringValue) {
    return ValueImpl.getStringValue(stringValue);
  }

  /**
   * Creates a value carrying binary data.
   *
   * @param inputStreamValue an {@code InputStream} containing the data
   * @return a {@link Value} instance carrying this data
   */
  public static Value getBinaryValue(InputStream inputStreamValue) {
    return ValueImpl.getBinaryValue(inputStreamValue);
  }

  /**
   * Creates a value carrying binary data.
   *
   * @param inputStreamFactory an {@code InputStreamFactory}
   * @return a {@link Value} instance carrying this data
   */
  public static Value getBinaryValue(InputStreamFactory inputStreamFactory) {
    return ValueImpl.getBinaryValue(inputStreamFactory);
  }

  /**
   * Creates a value carrying binary data.
   *
   * @param byteArrayValue an {@code byte} array containing the data
   * @return a {@link Value} instance carrying this data
   */
  public static Value getBinaryValue(byte[] byteArrayValue) {
    return ValueImpl.getBinaryValue(byteArrayValue);
  }

  /**
   * Creates a value carrying an integer.
   *
   * @param longValue a {@code long} containing the data
   * @return a {@link Value} instance carrying this data
   */
  public static Value getLongValue(long longValue) {
    return ValueImpl.getLongValue(longValue);
  }

  /**
   * Creates a value carrying an integer.
   *
   * @param doubleValue a {@code double} containing the data
   * @return a {@link Value} instance carrying this data
   */
  public static Value getDoubleValue(double doubleValue) {
    return ValueImpl.getDoubleValue(doubleValue);
  }

  /**
   * Creates a value carrying a date.
   *
   * @param calendarValue a {@code Calendar} object containing the data
   * @return a {@link Value} instance carrying this data
   */
  public static Value getDateValue(Calendar calendarValue) {
    return ValueImpl.getDateValue(calendarValue);
  }

  /**
   * Creates a value carrying a boolean.
   *
   * @param booleanValue a {@code boolean} containing the data
   * @return a {@link Value} instance carrying this data
   */
  public static Value getBooleanValue(boolean booleanValue) {
    return ValueImpl.getBooleanValue(booleanValue);
  }

  /**
   * Creates a value carrying a boolean.
   *
   * @param stringValue A {@code String} containing the data. The String
   *        is converted as follows:
   *        <ul>
   *        <li> Any case variant of the strings "t" and "true" return
   *        {@code true}.</li>
   *        <li> All other strings (including {@code null} and the empty
   *        string) return {@code false}.</li>
   *        </ul>
   * @return a {@link Value} instance carrying this data
   */
  public static Value getBooleanValue(String stringValue) {
    return ValueImpl.getBooleanValue(stringValue);
  }

  /**
   * Creates a value carrying a principal.
   *
   * @param name a String representing the name of a principal.
   */
  public static Value getPrincipalValue(String name) {
    return ValueImpl.getPrincipalValue(new Principal(name));
  }

  /**
   * Creates a value carrying a principal.
   *
   * @param principal a Principal
   */
  public static Value getPrincipalValue(Principal principal) {
    return ValueImpl.getPrincipalValue(principal);
  }

  /**
   * Convenience function for access to a single named value from a
   * {@link Document}.
   *
   * @param document the {@link Document} from which to extract the
   *        {@link Value}
   * @param propertyName the name of the {@link Property}
   * @return the first {@link Value} of that named property, if there is one -
   *         {@code null} otherwise
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
   * Convenience function for access to a single string value from a
   * {@link Document}.
   *
   * @param document the {@link Document} from which to extract the
   *        {@link Value}
   * @param propertyName the name of the {@link Property}
   * @return the String {@link Value} of that named property, if there is one -
   *         {@code null} otherwise
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
   * Returns a string representation of the {@link Value}. Connector developers
   * may count on this for debugging.
   *
   * @return a string representation of the {@link Value}
   */
  @Override
  public abstract String toString();

  private static final Calendar CALENDAR = Calendar.getInstance();
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_MINS =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_DATE =
      new SimpleDateFormat("yyyy-MM-dd");
  private static final SimpleDateFormat RFC822_DATE_FORMAT =
      new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss Z",
          Locale.ENGLISH);

  static {
    ISO8601_DATE_FORMAT_MILLIS.setCalendar(CALENDAR);
    ISO8601_DATE_FORMAT_MILLIS.setLenient(true);
    ISO8601_DATE_FORMAT_SECS.setCalendar(CALENDAR);
    ISO8601_DATE_FORMAT_SECS.setLenient(true);
    ISO8601_DATE_FORMAT_MINS.setCalendar(CALENDAR);
    ISO8601_DATE_FORMAT_MINS.setLenient(true);
    ISO8601_DATE_FORMAT_DATE.setCalendar(CALENDAR);
    ISO8601_DATE_FORMAT_DATE.setLenient(true);
    RFC822_DATE_FORMAT.setCalendar(CALENDAR);
    RFC822_DATE_FORMAT.setLenient(true);
  }

  /**
   * Sets the time zone used to format date values for the feed to the
   * given time zone.
   *
   * @param id the time zone ID, or {@code null} or
   * {@code ""} (empty string) to specify the default time zone
   * @see TimeZone#getTimeZone
   * @see TimeZone#getDefault
   * @since 2.4.4
   */
  public static synchronized void setFeedTimeZone(String id) {
    TimeZone tz;
    if (id == null || id.length() == 0) {
      id = "default"; // For the log message.
      tz = TimeZone.getDefault();
    } else {
      tz = TimeZone.getTimeZone(id);
    }
    LOGGER.config("Setting feed time zone to " + id + " = " + tz.getID());
    CALENDAR.setTimeZone(tz);
  }

  /**
   * Gets the time zone ID for the unit tests.
   *
   * @since 2.4.4
   */
  static synchronized String getFeedTimeZone() {
    return CALENDAR.getTimeZone().getID();
  }

  /**
   * Formats a calendar object for the Feeds Protocol, using the
   * <a href="http://www.iso.org/iso/support/faqs/faqs_widely_used_standards/widely_used_standards_other/date_and_time_format.htm">
   * ISO-8601</a> format for just the date portion.  See
   * <a href="http://code.google.com/apis/searchappliance/documentation/feedsguide.html">
   * Feeds Protocol Developer's Guide</a>
   *
   * @param calendar a {@code Calendar}
   * @return a String in ISO-8601 date format
   */
  public static synchronized String calendarToFeedXml(Calendar calendar) {
    Date date = calendar.getTime();
    return ISO8601_DATE_FORMAT_DATE.format(date);
  }

  /**
   * Formats a calendar object according to the
   * <a href="http://www.w3.org/Protocols/rfc822/#z28">RFC 822</a>
   * specification.
   *
   * @param calendar a {@code Calendar}
   * @return a String in RFC 822 format
   */
  public static synchronized String calendarToRfc822(Calendar calendar) {
    Date date = calendar.getTime();
    // Fix UTC time zone marker. The SimpleDateFormat Z pattern letter
    // always produces an offset string, e.g., "-0800" or "+000". For
    // UTC, the use of "GMT" (RFC 822) or "Z" (ISO 8601) is preferred.
    return RFC822_DATE_FORMAT.format(date).replaceFirst("\\+0000$", "GMT");
  }

  /**
   * Formats a calendar object according to the
   * <a href="http://www.iso.org/iso/support/faqs/faqs_widely_used_standards/widely_used_standards_other/date_and_time_format.htm">
   * ISO-8601</a> specification.
   *
   * @param calendar a {@code Calendar}
   * @return a String in ISO-8601 format
   */
  public static synchronized String calendarToIso8601(Calendar calendar) {
    Date date = calendar.getTime();
    String isoString;
    if (calendar.isSet(Calendar.MILLISECOND)) {
      isoString = ISO8601_DATE_FORMAT_MILLIS.format(date);
    } else if (calendar.isSet(Calendar.SECOND)) {
      isoString = ISO8601_DATE_FORMAT_SECS.format(date);
    } else if (calendar.isSet(Calendar.MINUTE)) {
      isoString = ISO8601_DATE_FORMAT_MINS.format(date);
    } else {
      isoString = ISO8601_DATE_FORMAT_DATE.format(date);
    }
    // Fix UTC time zone marker.
    return isoString.replaceFirst("\\+0000$", "Z");
  }

  private static synchronized Date iso8601ToDate(String s)
      throws ParseException {
    Date date = null;
    try {
      date = ISO8601_DATE_FORMAT_MILLIS.parse(s);
    } catch (ParseException e1) {
      try {
        date = ISO8601_DATE_FORMAT_SECS.parse(s);
      } catch (ParseException e2) {
        try {
          date = ISO8601_DATE_FORMAT_MINS.parse(s);
        } catch (ParseException e3) {
          date = ISO8601_DATE_FORMAT_DATE.parse(s);
        }
      }
    }
    return date;
  }

  /**
   * Parses a String in
   * <a href="http://www.iso.org/iso/support/faqs/faqs_widely_used_standards/widely_used_standards_other/date_and_time_format.htm">
   * ISO-8601</a> format (GMT zone) and returns an equivalent
   * {@code java.util.Calendar} object.
   *
   * @param dateString the date string to parse
   * @return a Calendar object
   * @throws ParseException if the the String can not be parsed
   */
  public static synchronized Calendar iso8601ToCalendar(String dateString)
      throws ParseException {
    // Fix UTC time zone marker. For parsing, the Z pattern letter
    // does not accept "Z" for UTC.
    Date date = iso8601ToDate(dateString.replaceFirst("Z$", "+0000"));
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }
}
