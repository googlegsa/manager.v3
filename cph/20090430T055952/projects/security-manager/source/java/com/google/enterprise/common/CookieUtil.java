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

package com.google.enterprise.common;

import com.google.enterprise.connector.common.Base64;
import com.google.enterprise.connector.common.Base64DecoderException;
import com.google.enterprise.saml.common.GsaConstants;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

/**
 * A class with some static methods to be used for Forms Authentication
 * feature.
 */
public final class CookieUtil {
  // RFC 2109, sect 10.1.2, says: Wdy, DD-Mon-YY HH:MM:SS GMT
  private static final DateFormat DATE_FORMAT_RFC2109 =
      new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
  // ..and to be flexible we accept a variation.
  private static final DateFormat DATE_FORMAT_ALT =
      new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
  static {
    DATE_FORMAT_RFC2109.setTimeZone(TimeZone.getTimeZone("GMT"));
    DATE_FORMAT_ALT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
  private static final Logger LOG =
      Logger.getLogger(CookieUtil.class.getName());

  private CookieUtil() {
  }

  /** Fetch a page, using POST or GET, and do not follow redirect.
   *  @param exchange The HTTP exchange object
   *  @param url The URL to fetch
   *  @param proxy The proxy String of form host:port if so desired
   *  @param userAgent The string to send in the user agent header
   *  @param cookies A collection of existing cookie to be sent, newly
   *                 received cookies will be stored in here as well.
   *  @param bodyBuffer A StringBuffer to store response body. Ignored if null
   *  @param redirectBuffer A StringBuffer to store the redirect URL
   *                        if so exists; may be null
   *  @param passwordFields A Set of password field names of which values
   *                        should not be logged.
   *  @param logger A Logger to log HTTP communication.  Ignored if null.
   *  @return The HTTP status code of the request.
   *
   *  TODO - passwordFields is not a good approach;  we cannot predict
   *  password field names.  It would be much better to scan the html field
   *  TYPES for a type of "password."  The enum of types is set by the html
   *  standard, the range of possible password field names is unlimited.
   *  I'd make that change now, except we're in a 5.0 push, and mgmt asked for
   *  minimal changes necessary to resolve bug 858157.
   */
  public static int fetchPage(HttpExchange exchange,
                              URL url,
                              String proxy,
                              String userAgent,
                              Collection<Cookie> cookies,
                              StringBuffer bodyBuffer,
                              StringBuffer redirectBuffer,
                              Set<String> passwordFields,
                              Logger logger)
      throws IOException {

    if (bodyBuffer != null) {
      bodyBuffer.setLength(0);
    }

    if (redirectBuffer != null) {
      redirectBuffer.setLength(0);
    }

    exchange.setProxy(proxy);

    // Boilerplate headers.
    exchange.setRequestHeader("Date", ServletBase.httpDateString());
    exchange.setRequestHeader(
        "Accept", "text/html, text/xhtml;q=0.9, text/plain;q=0.5, text/*;q=0.1");
    exchange.setRequestHeader("Accept-Charset", "us-ascii, iso-8859-1, utf-8");
    exchange.setRequestHeader("Accept-Encoding", "identity");
    exchange.setRequestHeader("Accept-Language", "en-us, en;q=0.9");

    if (!undefined(userAgent)) {
      exchange.setRequestHeader("User-Agent", userAgent);
    }

    String cookieStr = filterCookieToSend(cookies, url);

    if (!undefined(cookieStr)) {
      exchange.setRequestHeader("Cookie", cookieStr);
    }

    int status = exchange.exchange();

    if (bodyBuffer != null) {
      bodyBuffer.append(exchange.getResponseEntityAsString());
    }
    if (redirectBuffer != null) {
      String redirect = getRedirectUrl(exchange, url);
      if (redirect != null)
        redirectBuffer.append(redirect);
    }

    if (cookies != null) {
      List<SetCookie> newCookies = parseHttpResponseCookies(exchange);

      // removing duplicate cookies in old Vector. We do it inefficient way
      // O(n^2) here for small Vectors
      List<Cookie> cookiesToRemove = new ArrayList<Cookie>();
      Set<String> expiredCookieNames = new HashSet<String>();
      Set<SetCookie> expiredCookies = new HashSet<SetCookie>();

      // Get the current time so we can check for expired cookies.
      Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
      long curTimeGmt = cal.getTimeInMillis();

      for (SetCookie setCookie : newCookies) {
        String name = setCookie.getName();
        String expiresAt = setCookie.getExpires();
        LOG.info("name=" + name + " age=" + setCookie.getMaxAge() +
                 " exp=" + expiresAt);
        if (setCookie.getMaxAge() == 0) {
          // Max age is non-negative (RFC2109) and negative means
          // attribute not present, so we just check for 0.
          LOG.info("This cookie has expired based on max-age: " + name);
          expiredCookieNames.add(name);
          expiredCookies.add(setCookie);
        } else if (expiresAt != null) {
          long expMillis = -1;
          // We try 2 styles of date formats but only want to complain if
          // both fail, so we store the last exception here.
          ParseException lastParsingProblem = null;
          try {
            expMillis = DATE_FORMAT_RFC2109.parse(expiresAt).getTime();
          } catch (ParseException pe) {
            lastParsingProblem = pe;
          }

          if (lastParsingProblem != null) { // Parsing failed, try other fmt
            try {
              expMillis = DATE_FORMAT_ALT.parse(expiresAt).getTime();
            } catch (ParseException pe) {
              lastParsingProblem = pe;
            }
          }

          if (expMillis >= 0 && expMillis <= curTimeGmt) {
            LOG.info("This cookie has expired based on time: " + name);
            expiredCookieNames.add(name);
            expiredCookies.add(setCookie);
          } else if (expMillis == -1 && lastParsingProblem != null) {
            // If we never parsed the time and we have an exception recorded
            // then complain.
            LOG.log(Level.WARNING,
                    "Can't parse cookie date \"" + expiresAt
                    + "\" for cookie " + name, lastParsingProblem);
          }
        }

        // TODO what to do with cookies that have no "domain" attribute??

        // Now delete all cookies marked for deletion.
        for (Cookie cookie : cookies) {
          String thisName = cookie.getName();
          if (name.equals(thisName) ||
              expiredCookieNames.contains(thisName)) {
            cookiesToRemove.add(cookie);
          }
        }
      }
      newCookies.removeAll(expiredCookies);
      cookies.removeAll(cookiesToRemove);
      cookies.addAll(newCookies);
    }

    exchange.close();
    return status;
  }

  /**
   * Get the URL to redirect to after executing an HTTP request if so exists
   *
   * @param exchange The HTTP exchange object.
   * @param url The base URL object.
   * @return The absolute URL from a <code>Refresh</code> or
   *         <code>Location</code> header. Return null if none exists
   */
  private static String getRedirectUrl(HttpExchange exchange, URL url) {
    int status = exchange.getStatusCode();
    if (status == 200) {
      return getRefreshUrl(exchange);
    }
    if (status >= 300 && status < 400) {
      return exchange.getResponseHeaderValue("Location");
    }
    return null;
  }

  /**
   * Get the relative URL string in Refresh header if exists.
   * @param exchange The HTTP exchange object.
   * @return The relative URL string of Refresh header or null
   *   if none exists
   */
  private static String getRefreshUrl(HttpExchange exchange) {
    String refresh = exchange.getResponseHeaderValue("Refresh");
    if (refresh != null) {
      int pos = refresh.indexOf(';');
      if (pos != -1) {
        // found a semicolon
        String timeToRefresh = refresh.substring(0, pos);
        if ("0".equals(timeToRefresh)) {
          // only follow this if its an immediate refresh (0 seconds)
          pos = refresh.indexOf('=');
          if (pos != -1 && (pos + 1) < refresh.length()) {
            return refresh.substring(pos+1);
          }
        }
      }
    }
    return null;
  }

  /**
   *  Build a String representation out of a Vector of Cookie that
   *  applies for a given URL.
   *  @param cookies Vector of Cookie object
   *  @param url The target URL.
   *  @return A String to be used as value of a "Cookie" header
   */
  private static String filterCookieToSend(Collection<Cookie> cookies, URL url) {
    if (cookies == null || cookies.size() == 0)
      return null;
    StringBuilder buffer = new StringBuilder();
    for (Cookie cookie: cookies) {
      if (isCookieGoodFor(cookie, url)) {
        if (buffer.length() > 0) {
          buffer.append("; ");
        }
        buffer.append(cookie.getName());
        buffer.append('=');
        buffer.append(cookie.getValue());
      }
    }
    return buffer.toString();
  }

  /**
   * Check if a cookie is good for a given target URL.
   *
   * @param cookie The cookie to be filtered
   * @param url The URL object to be tested against
   * @return If the cookie is valid to be sent to the URL.
   */
  public static boolean isCookieGoodFor(Cookie cookie, URL url) {
    if (!undefined(cookie.getDomain()) &&
        !url.getHost().toLowerCase().endsWith(cookie.getDomain().toLowerCase())) {
      return false;
    }
    if (!undefined(cookie.getPath()) &&
        !url.getPath().startsWith(cookie.getPath())) {
      return false;
    }
    if (cookie.getSecure() && !"https".equalsIgnoreCase(url.getProtocol())) {
      return false;
    }
    return true;
  }

  /**
   * Convert a collection of cookies into a form suitable for use in a "Cookie:" header.
   *
   * @param cookies The cookies to convert.
   * @param showValues If false, replace each cookie's value with its hash code.
   *        This should be false when using the result for logging.
   * @return The string representation of the cookies.
   */
  public static String cookieHeaderValue(Collection<Cookie> cookies, boolean showValues) {
    return convertCookies(cookies, showValues, true);
  }

  /**
   * Convert a collection of cookies into a form suitable for use in a "Set-Cookie:" header.
   *
   * @param cookies The cookies to convert.
   * @param showValues If false, replace each cookie's value with its hash code.
   *        This should be false when using the result for logging.
   * @return The string representation of the cookies.
   */
  public static String setCookieHeaderValue(Collection<Cookie> cookies, boolean showValues) {
    return convertCookies(cookies, showValues, false);
  }

  private static String convertCookies(Collection<Cookie> cookies,
                                       boolean showValues, boolean shortForm) {
    if (cookies.size() == 0) {
      return null;
    }
    StringBuffer buffer = new StringBuffer();
    for (Cookie c: cookies) {
      if (buffer.length() > 0) {
        buffer.append(shortForm ? "; " : ", ");
      }
      buffer.append(c.getName());
      buffer.append("=");
      if (c.getValue() != null) {
        buffer.append(
            showValues
            ? c.getValue()
            : c.getValue().hashCode());
      }
      if (shortForm) {
        continue;
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
    return buffer.toString();
  }

  /**
   * Parse the Set-Cookie headers in an HTTP response.
   *
   * @param exchange The exchange containing the headers.
   * @return A list of parsed cookies.
   */
  public static List<SetCookie> parseHttpResponseCookies(HttpExchange exchange) {
    List<SetCookie> cookies = new ArrayList<SetCookie>();
    for (String value: exchange.getResponseHeaderValues("Set-Cookie")) {
      cookies.addAll(SetCookieParser.parse(value));
    }
    for (String value: exchange.getResponseHeaderValues("Set-Cookie2")) {
      cookies.addAll(SetCookieParser.parse(value));
    }
    return cookies;
  }

  /**
   * Serialize a cookie for storage in the GSA session manager.
   *
   * @param cookie The cookie to serialize.
   * @return The cookie's session-manager serialization.
   */
  public static String serializeCookie(Cookie cookie) {
    StringBuilder str = new StringBuilder(safeSerialize(cookie.getName()));
    str.append(GsaConstants.COOKIE_FIELD_SEPERATOR);
    str.append(safeSerialize(cookie.getValue()));
    str.append(GsaConstants.COOKIE_FIELD_SEPERATOR);
    str.append(safeSerialize(cookie.getPath()));
    str.append(GsaConstants.COOKIE_FIELD_SEPERATOR);
    str.append(safeSerialize(cookie.getDomain()));
    str.append(GsaConstants.COOKIE_FIELD_SEPERATOR);
    str.append(safeSerialize(String.valueOf(cookie.getMaxAge())));
    return str.toString();
  }

  /**
   * Serialize a collection of cookies for storage in the GSA session manager.
   *
   * @param cookies The cookies to serialize.
   * @return The cookies' session-manager serialization.
   */
  public static String serializeCookies(Collection<Cookie> cookies) {
    StringBuilder str = new StringBuilder();
    for (Cookie cookie : cookies) {
      str.append(serializeCookie(cookie));
      str.append(GsaConstants.COOKIE_RECORD_SEPARATOR);
    }
    return str.toString();
  }

  private static String safeSerialize(String str) {
    if (str == null) str = "";
    return Base64.encodeWebSafe(str.getBytes(), false);
  }

  /**
   * Deserialize a cookie from the GSA session manager.
   *
   * @param str The cookie's session-manager serialization.
   * @return The corresponding cookie.
   */
  public static Cookie deserializeCookie(String str) {
    String[] elements = str.split(GsaConstants.COOKIE_FIELD_SEPERATOR);
    Cookie cookie = new Cookie(safeDeserialize(elements[0]),
                               safeDeserialize(elements[1]));
    cookie.setPath(safeDeserialize(elements[2]));
    cookie.setDomain(safeDeserialize(elements[3]));
    Integer maxAge = new Integer(safeDeserialize(elements[4]));
    cookie.setMaxAge(maxAge);
    return cookie;
  }

  /**
   * Deserialize a collection of cookies from the GSA session manager.
   *
   * @param str The cookies' session-manager serialization.
   * @return A list of the corresponding cookies.
   */
  public static List<Cookie> deserializeCookies(String str) {
    List<Cookie> cookies = new ArrayList<Cookie>();
    String[] elements = str.split(GsaConstants.COOKIE_RECORD_SEPARATOR);
    for (String element : elements) {
      cookies.add(deserializeCookie(element));
    }
    return cookies;
  }

  private static String safeDeserialize(String str) {
     try {
       return new String(Base64.decode(str));
     } catch (Base64DecoderException e) {
       // Should not happen if the cookie was serialized by us
       LOG.warning("Error while deserializing. Original string = <" + str + ">.");
       return "";
     }
   }

  private static boolean undefined(String str) {
    return str == null || "".equals(str);
  }
}
