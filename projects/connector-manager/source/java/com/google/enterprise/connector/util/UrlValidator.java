// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

/**
 * Validates URLs by making an HTTP request.
 *
 * @since 2.6.6
 */
/*
 * TODO: We might want to merge XmlFeed#validateSearchUrl into this class.
 *
 * TODO: With an URLStreamHandler, we might be able to do some unit
 * testing of this class.
 */
public class UrlValidator {
  /** The logger for this class. */
  private static final Logger LOGGER =
      Logger.getLogger(UrlValidator.class.getName());

  /** The connect timeout. */
  private volatile int connectTimeout = 60 * 1000;

  /** The read timeout. */
  private volatile int readTimeout = 60 * 1000;

  /** The HTTP request method. */
  private volatile String requestMethod = "HEAD";

  /** Whether redirects should be followed or returned as the response. */
  private volatile boolean followRedirects = false;

  /** Whether fully qualified host names must be specified. */
  private volatile boolean requireFullyQualifiedHostNames = false;

  /** Constructs an instance using the default parameter values. */
  public UrlValidator() {
  }

  /**
   * Sets the HTTP request method. The default value is "HEAD".
   *
   * @param requestMethod should be either "GET" or "HEAD"
   * @see HttpURLConnection#setRequestMethod
   */
  public void setRequestMethod(String requestMethod) {
    this.requestMethod = requestMethod;
  }

  /**
   * Sets whether to follow HTTP redirects, or return them as the
   * response. The default is {@code false}, which returns the
   * redirect as the HTTP response.
   *
   * @param followRedirects {@code true} to follow HTTP
   *     redirects, or {@code false} to return them as the HTTP
   *     response
   * @see HttpURLConnection#setInstanceFollowRedirects
   */
  public void setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
  }

  /**
   * Sets whether fully qualified host names are required in the URL.
   * IP addresses are still OK, but host names must be fully qualified.
   * The default is {@code false}, which allows non-fully qualified
   * host names, even thought the GSA requires one in most cases.
   *
   * @param requireFullyQualifiedHostNames {@code true} if host
   *        names must be fully qualified, {@code false} if not
   */
  public void setRequireFullyQualifiedHostNames(
      boolean requireFullyQualifiedHostNames) {
    this.requireFullyQualifiedHostNames = requireFullyQualifiedHostNames;
  }

  /**
   * Sets the connect timeout. The default value is 60000 milliseconds.
   *
   * @param connectTimeout the connect timeout in milliseconds
   * @see URLConnection#setConnectTimeout
   */
  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  /**
   * Sets the read timeout. The default value is 60000 milliseconds.
   *
   * @param readTimeout the read timeout in milliseconds
   * @see URLConnection#setReadTimeout
   */
  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  /**
   * Attempts to validate the given URL by making an HTTP request. In
   * this case, we're mostly trying to catch typos, so "valid" means:
   * <ol>
   * <li>The URL syntax is valid.</li>
   * <li>If fully qualified host names are required, check that the
   *     host name looks fully qualified (contains a '.').</li>
   * <li>If the URL uses HTTP or HTTPS:
   *   <ol>
   *   <li>A connection can be made and the response read.</li>
   *   <li>The response code was not 404,
   *   or any of the following related but less common errors:
   *   400, 405, 410, or 414.</li>
   *   </ol>
   * </li>
   * </ol>
   * <p>
   * The 405 (Method Not Allowed) is related because the Sun Java
   * System Web Server, and possibly Apache, return this code rather
   * than a 404 if you attempt to access a CGI program in an unknown
   * directory.
   * <p>
   * When testing an HTTPS URL, we override server certificate
   * validation to skip trying to verify the server's certificate,
   * and we accept hostname mismatches. In this case, all we care
   * about is that the configured URL can be reached; it's up to the
   * connector administrator to enter the right URL.
   *
   * @param urlString the URL to test
   * @throws GeneralSecurityException if there is an error configuring
   *         the HTTPS connection
   * @throws IOException if the URL is malformed, or if there is an
   *         error connecting or reading the response
   * @throws UrlValidatorException if the HTTP status code was invalid
   */
  /*
   * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4912484
   * The above Sun bug report documents that openConnection
   * doesn't try to connect.
   *
   * This method returns the HTTP response code so that it can be
   * unit tested. A return value of 0 is arbitrary and unused by the
   * tests.
   */
  public int validate(String urlString)
      throws GeneralSecurityException, IOException, UrlValidatorException {
    if (urlString == null || urlString.trim().length() == 0) {
      return 0;
    }

    URL url = new URL(urlString);

    if (requireFullyQualifiedHostNames) {
      // The GSA requires fully qualified host names for most hosts.
      // This non-rigorous test simply looks for '.' in hostname.
      // Conveniently, IPv4 addresses also pass this test (but not
      // IPv6 addresses).
      String host = url.getHost();
      if ((host.charAt(0) != '[') && (host.indexOf('.') < 0)) {
        // FIXME: This string should be translated, either locally,
        // which might be troubling, or by throwing a more specific
        // exception so that the connector can provide a localized
        // message.
        LOGGER.severe("Fully qualified host name is required: " + host);
        throw new UrlValidatorException(HttpURLConnection.HTTP_PRECON_FAILED,
            "Fully qualified host name is required: " + host);
      }
    }

    URLConnection conn = url.openConnection();

    if (!(conn instanceof HttpURLConnection)) {
      // If the URL is not an HTTP or HTTPS URL, which is
      // incredibly unlikely, we don't check anything beyond
      // the URL syntax.
      return 0;
    }

    HttpURLConnection httpConn = (HttpURLConnection) conn;
    if (httpConn instanceof HttpsURLConnection) {
      SslUtil.setTrustingHttpsOptions((HttpsURLConnection) httpConn);
    }
    setTimeouts(conn);
    httpConn.setRequestMethod(requestMethod);
    httpConn.setInstanceFollowRedirects(followRedirects);

    httpConn.connect();
    try {
      int responseCode = httpConn.getResponseCode();
      String responseMessage = httpConn.getResponseMessage();

      switch (responseCode) {
        case HttpURLConnection.HTTP_BAD_REQUEST:
        case HttpURLConnection.HTTP_NOT_FOUND:
        case HttpURLConnection.HTTP_BAD_METHOD:
        case HttpURLConnection.HTTP_GONE:
        case HttpURLConnection.HTTP_REQ_TOO_LONG:
          if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.severe("Validate URL HTTP response: "
                + responseCode + " " + responseMessage);
          }
          throw new UrlValidatorException(responseCode, responseMessage);

        default:
          if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.config("Validate URL HTTP response: "
                + responseCode + " " + responseMessage);
          }
          break;
      }
      return responseCode;
    } finally {
      httpConn.disconnect();
    }
  }

  /**
   * Sets the connect and read timeouts of the given {@code URLConnection}.
   *
   * @param conn the URL connection
   */
   private void setTimeouts(URLConnection conn) {
     conn.setConnectTimeout(connectTimeout);
     conn.setReadTimeout(readTimeout);
  }
}
