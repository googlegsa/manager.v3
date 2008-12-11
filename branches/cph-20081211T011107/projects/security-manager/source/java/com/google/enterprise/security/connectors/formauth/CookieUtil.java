// Copyright 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.security.connectors.formauth;

import java.io.IOException;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;


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
  private static final int CONNECTION_TIMEOUT = 3000;
  private static final int IDLE_TIMEOUT = 3000;

  private static HttpConnectionManager connectionManager;

  static {
    // set up ConnectionManager to be used.
    connectionManager = new MultiThreadedHttpConnectionManager();

    HttpConnectionManagerParams params = new HttpConnectionManagerParams();
    params.setConnectionTimeout(CONNECTION_TIMEOUT);
    connectionManager.setParams(params);

    IdleConnectionTimeoutThread idleConnectionTimeoutThread
        = new IdleConnectionTimeoutThread();
    idleConnectionTimeoutThread.setTimeoutInterval(IDLE_TIMEOUT);
    idleConnectionTimeoutThread.start();
    idleConnectionTimeoutThread.addConnectionManager(connectionManager);
  }

  private CookieUtil() {
  }

  /** Fetch a page, using POST or GET, and do not follow redirect.
   *  @param method HTTP method, "GET" or "POST"
   *  @param url The URL to fetch
   *  @param proxy The proxy String of form host:port if so desired
   *  @param userAgent The string to send in the user agent header
   *  @param cookies A Vector of existing cookie to be sent, newly
   *                 received cookies will be stored in here as well.
   *  @param parameters Form data for a POST request
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
  public static int fetchPage(String method,
                              URL url,
                              String proxy,
                              String userAgent,
                              Vector<Cookie> cookies,
                              NameValuePair[] parameters,
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

    HttpClient httpClient = new HttpClient(connectionManager);
    setProxy(httpClient, proxy);
    httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    HttpMethod httpMethod = null;
    if ("post".equalsIgnoreCase(method)) {
      httpMethod = new PostMethod(url.toString());
      httpMethod.setRequestHeader("Content-type",
                                  "application/x-www-form-urlencoded");
      ((PostMethod) httpMethod).addParameters(parameters);
    } else if ("get".equalsIgnoreCase(method)) {
      httpMethod = new GetMethod(url.toString());
    } else {
      return 0;
    }

    httpMethod.setFollowRedirects(false);

    if (!undefined(userAgent)) {
      httpMethod.setRequestHeader("User-Agent", userAgent);
    }

    String cookieStr = filterCookieToSend(cookies, url);

    if (!undefined(cookieStr)) {
      httpMethod.setRequestHeader("Cookie", cookieStr);
    }

    httpMethod.setRequestHeader("Accept", "*/*");
    httpMethod.setRequestHeader("Host", url.getHost());
    httpMethod.setPath(url.getPath());

    if (!undefined(url.getQuery())) {
      httpMethod.setQueryString(url.getQuery());
    }

    int status = httpClient.executeMethod(httpMethod);

    if (logger != null) {  // log sending data
      StringBuffer logInfo = new StringBuffer();
      logInfo.append("\nRequest: " + httpMethod.getName() +
                     " " + url.toString());
      logInfo.append("\nHeaders:\n");
      for (NameValuePair nvpair : httpMethod.getRequestHeaders()) {
        logInfo.append(nvpair.toString());
      }

      if ("post".equalsIgnoreCase(method)) {
        logInfo.append("\nParameters:");
        for (NameValuePair nvpair : ((PostMethod)httpMethod).getParameters()) {
          if (passwordFields != null &&
              passwordFields.contains(nvpair.getName())) {
            logInfo.append("\nname=").append(nvpair.getName()).
              append(", value=******");
          } else {
            logInfo.append('\n').append(nvpair.toString());
          }
        }
        logInfo.append("\n");
      }
      logInfo.append("\n================================================\n");
      logger.info(logInfo.toString());
    }

    if (bodyBuffer != null) {
      bodyBuffer.append(httpMethod.getResponseBodyAsString());
    }
    if (redirectBuffer != null) {
      String redirect = getRedirectUrl(url, httpMethod);
      if (redirect != null)
        redirectBuffer.append(redirect);
    }

    if (cookies != null) {
      List<SetCookie> newCookies = new ArrayList<SetCookie>();
      for (Header header : httpMethod.getResponseHeaders("Set-Cookie")) {
        newCookies.addAll(SetCookieParser.parse(header.getValue()));
      }
      for (Header header : httpMethod.getResponseHeaders("Set-Cookie2")) {
        newCookies.addAll(SetCookieParser.parse(header.getValue()));
      }
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
          // attribtue not present, so we just check for 0.
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

    if (logger != null) {  // log receiving data
      StringBuilder logInfo = new StringBuilder();
      logInfo.append("\nResponse: status = " + httpMethod.getStatusLine());
      logInfo.append("\nHeaders:\n");
      for (NameValuePair nvpair : httpMethod.getResponseHeaders()) {
        logInfo.append(nvpair.toString());
      }
      logInfo.append("\n================================================\n");
      logger.info(logInfo.toString());
    }

    httpMethod.releaseConnection();
    return status;
  }

  /**
   * Get the URL to redirect to after executing an HTTP request if so exists
   *
   * @param method The HttpMethod object, after performing the request
   * @param url The base URL object.
   * @return The absolute URL from a <code>Refresh</code> or
   *         <code>Location</code> header. Return null if none exists
   */
  public static String getRedirectUrl(URL url, HttpMethod method) {
    String redirectUrl = null;
    int status = method.getStatusCode();
    if (status == 200) {
      redirectUrl = getRefreshUrl(method);
    } else if (status >= 300 && status < 400) {
      redirectUrl = getResponseHeader(method, "Location");
    }
    return redirectUrl;
  }

  /** Return the String value of a header with name @name
   *  @param method The HttpMethod object, after performing the request.
   *  @param name Header name
   *  @return Header value.
   */
  private static String getResponseHeader(HttpMethod method, String name) {
    Header header = method.getResponseHeader(name);
    return (header != null) ? header.getValue() : null;
  }

  /**
   * Get the relative URL string in Refresh header if exists.
   * @param method The HttpMethod object, after performing the request.
   * @return The relative URL string of Refresh header or null
   *   if none exists
   */
  private static String getRefreshUrl(HttpMethod method) {
    String refresh = getResponseHeader(method, "Refresh");
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
   * If necessary, set proxy information for an HTTP client connection.
   *
   * @param httpClient The client object to set proxy info for
   * @param proxy The proxy host:port to use to look up proxy information
   */
  public static void setProxy(HttpClient httpClient, String proxy) {
    if (null == proxy) {
      return;
    }

    String[] proxyInfo = proxy.split(":");
    if (proxyInfo.length != 2) {
      LOG.warning("Error parsing proxy config file entry.");
      return;
    }

    httpClient.getHostConfiguration().
      setProxy(proxyInfo[0], Integer.parseInt(proxyInfo[1]));
  }

  /**
   *  Build a String representation out of a Vector of Cookie that
   *  applies for a given URL.
   *  @param cookies Vector of Cookie object
   *  @param url The target URL.
   *  @return A String to be used as value of a "Cookie" header
   */
  private static String filterCookieToSend(Vector<Cookie> cookies,
                                           URL url) {
    if (cookies == null || cookies.size() == 0)
      return null;
    StringBuilder buffer = new StringBuilder();
    for (Cookie cookie: cookies) {
      if (isCookieGoodFor(cookie, url))
        buffer.append(cookie.getName()).append('=').
          append(cookie.getValue()).append(';');
    }

    return buffer.toString();
  }

  /** Check if a cookie is good for a given target URL.
   *  @param cookie The cookie to be filtered
   *  @param url The java.net.URL object to be tested against
   *  @return If the cookie is valid to be sent to the URL.
   */
  private static boolean isCookieGoodFor(Cookie cookie, java.net.URL url) {
    if (!undefined(cookie.getDomain()) &&
        !url.getHost().endsWith(cookie.getDomain())) {
      return false;
    }
    if (!undefined(cookie.getPath()) &&
        !url.getPath().startsWith(cookie.getPath())) {
      return false;
    }
    if (cookie.getSecure() && !"https".equals(url.getProtocol())) {
      return false;
    }
    return true;
  }

  /**
   *  Return a multi-line String of Set-Cookie header, ready to
   *  send to User-Agent.
   *  Since the Vector may contains Cookie from user-agent and Cookie from
   *  @param cookies Vector of Cookie
   *  @return A String value that can be used in a Set-Cookie header.
   */
  public static String toSetCookieString(Vector<Cookie> cookies) {
    StringBuilder strbuf = new StringBuilder();
    boolean first = true;
    for (Cookie cookie : cookies) {
      if (first) {
        first = false;
      } else {
        strbuf.append("\nSet-Cookie: ");
      }

      if (!(cookie instanceof SetCookie)) {
        SetCookie temp = new SetCookie(cookie.getName(), cookie.getValue());
        cookie = temp;
      }

      String cookieStr = ((SetCookie)cookie).toString();
      LOG.fine("Set-Cookie: " + cookieStr);
      strbuf.append(cookieStr);
    }
    return strbuf.toString();
  }

  private static boolean undefined(String str) {
    return str == null || "".equals(str);
  }
}
