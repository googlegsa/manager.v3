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


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Simple convenience implementation of the spi.SimpleValue interface. This
 * class is not part of the spi - it is provided for developers to assist in
 * implementations of the spi.
 */
public class SimpleValue implements Value {

  private final ValueType type;
  private final String stringValue;
  private final byte[] byteArrayValue;

  public SimpleValue(ValueType t, String v) {
    this.type = t;
    this.stringValue = v;
    this.byteArrayValue = new byte[] {};
  }

  public SimpleValue(ValueType t, byte[] v) {
    this.type = t;
    this.stringValue = null;
    this.byteArrayValue = new byte[v.length];
    for (int i=0; i<v.length; i++) {
      this.byteArrayValue[i] = v[i];
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Value#getBoolean()
   */
  public boolean getBoolean() throws IllegalArgumentException,
      RepositoryException {
    if (stringValue.equalsIgnoreCase("t")
        || stringValue.equalsIgnoreCase("true")
        || stringValue.equalsIgnoreCase("y")
        || stringValue.equalsIgnoreCase("yes")
        || stringValue.equalsIgnoreCase("ok")
        || stringValue.equals("1")) {
      return true;
    }
    if (stringValue.equalsIgnoreCase("f")
        || stringValue.equalsIgnoreCase("false")
        || stringValue.equalsIgnoreCase("n")
        || stringValue.equalsIgnoreCase("no")
        || stringValue.equals("0")) {
      return false;
    }

    throw new IllegalArgumentException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Value#getDate()
   */
  public Calendar getDate() throws IllegalArgumentException,
      RepositoryException {
    Calendar c;
    try {
      c = iso8601ToCalendar(stringValue);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Can't parse stringValue as date: "
          + e.getMessage());
    }
    return c;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Value#getDouble()
   */
  public double getDouble() throws IllegalArgumentException,
      RepositoryException {
    Double d;
    d = Double.valueOf(stringValue);
    return d.doubleValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Value#getLong()
   */
  public long getLong() throws IllegalArgumentException, RepositoryException {
    Long l;
    l = Long.valueOf(stringValue);
    return l.longValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Value#getStream()
   */
  public InputStream getStream() throws IllegalArgumentException,
      IllegalStateException, RepositoryException {
    if (byteArrayValue.length > 0) {
      return new ByteArrayInputStream(byteArrayValue);
    }
    return new ByteArrayInputStream(stringValue.getBytes());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Value#getString()
   */
  public String getString() throws IllegalArgumentException,
      RepositoryException {
    return stringValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Value#getType()
   */
  public ValueType getType() throws RepositoryException {
    return type;
  }

  private static final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT+0");
  private static final Calendar GMT_CALENDAR =
      Calendar.getInstance(TIME_ZONE_GMT);
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  public static final SimpleDateFormat RFC822_DATE_FORMAT =
      new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss z");

  static {
    ISO8601_DATE_FORMAT_MILLIS.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_MILLIS.setLenient(true);
    ISO8601_DATE_FORMAT_SECS.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_SECS.setLenient(true);
    RFC822_DATE_FORMAT.setCalendar(GMT_CALENDAR);
    RFC822_DATE_FORMAT.setLenient(true);
    RFC822_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  /**
   * Formats a calendar object as RFC 822.
   * 
   * @param c
   * @return a String in RFC 822 format - always in GMT zone
   */
  public static String calendarToRfc822(Calendar c) {
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
  public static String calendarToIso8601(Calendar c) {
    Date d = c.getTime();
    String isoString = ISO8601_DATE_FORMAT_MILLIS.format(d);
    return isoString;
  }

  private static Date iso8601ToDate(String s) throws ParseException {
    Date d = null;
    try {
      d = ISO8601_DATE_FORMAT_MILLIS.parse(s);
      return d;
    } catch (ParseException e) {
      // this is just here so we can try another format
    }
    d = ISO8601_DATE_FORMAT_SECS.parse(s);
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
  public static Calendar iso8601ToCalendar(String s) throws ParseException {
    Date d = iso8601ToDate(s);
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    return c;
  }

}
