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

package com.google.enterprise.connector.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

  private TimeUtils() {
    // prevents instantiation
  }

  private static TimeZone timeZoneGmt = TimeZone.getTimeZone("GMT+0");
  private static Calendar gmtCalendar = Calendar.getInstance(timeZoneGmt);
  private static SimpleDateFormat iso8601DateFormatMillis = 
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static SimpleDateFormat iso8601DateFormatSecs = 
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  
  static {
    iso8601DateFormatMillis.setCalendar(gmtCalendar);
    iso8601DateFormatMillis.setLenient(true);
    iso8601DateFormatSecs.setCalendar(gmtCalendar);
    iso8601DateFormatSecs.setLenient(true);
  }
  
  public static String calendarToIso8601(Calendar c) {
    Date d = c.getTime();
    String isoString = iso8601DateFormatMillis.format(d);
    return isoString;
  }

  private static Date iso8601ToDate(String s) throws ParseException {
    Date d = null;
    try {
      d = iso8601DateFormatMillis.parse(s);
      return d;
    } catch (ParseException e) {
      // this is just here so we can try another format
    }
      d = iso8601DateFormatSecs.parse(s);
   return d;
  }
  
  public static Calendar iso8601ToCalendar(String s) throws ParseException { 
    Date d = iso8601ToDate(s);
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    return c;
  }

}
