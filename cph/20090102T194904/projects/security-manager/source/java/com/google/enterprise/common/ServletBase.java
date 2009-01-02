// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

/**
 * Useful utilities for writing servlets.
 */
public abstract class ServletBase extends HttpServlet {

  private static final long serialVersionUID = 1L;

  protected static final DateTimeFormatter dtFormat =
      DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
  
  public static String httpDateString() {
    return dtFormat.print((new DateTime()).withZone(DateTimeZone.UTC));
  }

  public static PrintWriter initNormalResponse(HttpServletResponse response) throws IOException {
    initResponse(response);
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Content-Type", "text/html; charset=UTF-8");
    response.setBufferSize(0x1000);
    return response.getWriter();
  }

  public static void initErrorResponse(HttpServletResponse response, int code)
      throws IOException {
    initResponse(response);
    response.sendError(code);
  }

  public static void initResponse(HttpServletResponse response) {
    response.setHeader("Date", httpDateString());
  }

  public static String setCookieHeaderValue(Collection<Cookie> cookies) {
    if (cookies.size() == 0) {
      return null;
    }
    StringBuffer buffer = new StringBuffer();
    for (Cookie c: cookies) {
      if (buffer.length() > 0) {
        buffer.append(", ");
      }
      convertCookie(c, buffer);
    }
    return buffer.toString();
  }

  private static void convertCookie(Cookie c, StringBuffer buffer) {
    buffer.append(c.getName());
    buffer.append("=");
    if (c.getValue() != null) {
      buffer.append(c.getValue());
    }
    if (c.getComment() != null) {
      buffer.append("; comment=");
      buffer.append(c.getComment());
    }
    if (c.getDomain() != null) {
      buffer.append("; domain=");
      buffer.append(c.getDomain());
    }
    if (c.getMaxAge() > 0) {
      buffer.append("; Max-Age=");
      buffer.append(c.getMaxAge());
    }
    if (c.getPath() != null) {
      buffer.append("; path=");
      buffer.append(c.getPath());
    }
    if (c.getVersion() != -1) {
      buffer.append("; version=");
      buffer.append(String.valueOf(c.getVersion()));
    }
    if (c.getSecure()) {
      buffer.append("; secure");
    }
  }
}
