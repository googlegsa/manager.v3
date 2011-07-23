// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.pusher;

import com.google.common.base.Strings;
import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.util.Clock;
import com.google.enterprise.connector.util.SslUtil;
import com.google.enterprise.connector.util.SystemClock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;

/**
 * Opens a connection to a url and sends data to it.
 */
public class GsaFeedConnection implements FeedConnection {
  private static final Logger LOGGER =
      Logger.getLogger(GsaFeedConnection.class.getName());

  /**
   * The GSA's response when it successfully receives a feed.
   */
  public static final String SUCCESS_RESPONSE = "Success";

  /**
   * The GSA's response when the client is not authorized to send feeds.
   */
  public static final String UNAUTHORIZED_RESPONSE =
      "Error - Unauthorized Request";

  /**
   * The GSA's response when it runs out of disk space.
   */
  public static final String DISKFULL_RESPONSE =
      "Feed not accepted due to insufficient disk space.";

  /**
   * The GSA's response when there was an internal error.
   */
  public static final String INTERNAL_ERROR_RESPONSE = "Internal Error";

  // Multipart/form-data uploads require a boundary to delimit controls.
  // Since we XML-escape or base64-encode all data provided by the connector,
  // the feed XML will never contain "<<".
  private static final String BOUNDARY = "<<";

  private static final String CRLF = "\r\n";

  // Content encodings supported by GSA.
  private String contentEncodings = null;

  // True if we recently got a feed error of some sort.
  private boolean gotFeedError = false;

  // XmlFeed URL
  private URL feedUrl = null;

  // XmlFeed DTD URL
  private URL dtdUrl = null;

  // BacklogCount URL
  private URL backlogUrl = null;

  // BacklogCount Ceiling. Throttle back feed if backlog exceeds the ceiling.
  private int backlogCeiling = 50000;

  // BacklogCount Floor. Stop throttling feed if backlog drops below floor.
  private int backlogFloor = 15000;

  // True if the feed is throttled back due to excessive backlog.
  private boolean isBacklogged = false;

  // Clock used for backlog checks.
  private Clock clock = new SystemClock();

  // Time of last backlog check.
  private long lastBacklogCheck;

  // How often to check for backlog (in milliseconds).
  private long backlogCheckInterval = 15 * 60 * 1000L;

  /** Whether HTTPS connections validate the server certificate. */
  private boolean validateCertificate = true;

  public GsaFeedConnection(String protocol, String host, int port,
      int securePort) throws MalformedURLException {
    if (Strings.isNullOrEmpty(protocol)) {
      protocol = (securePort < 0) ? "http" : "https";
  }
    this.setFeedHostAndPort(protocol, host, port, securePort);
  }

  @Override
  public String toString() {
    return "FeedConnection: feedUrl = " + feedUrl;
  }

  public synchronized void setFeedHostAndPort(String protocol, String host,
      int port, int securePort) throws MalformedURLException {
    setUrls(protocol, host, (protocol.equals("https")) ? securePort : port);
  }

  /**
   * Sets the URLs. This separate helper method ensures that only one
   * port value is available, to avoid grabbing the wrong port by
   * accident.
   */
  private void setUrls(String protocol, String host, int port)
      throws MalformedURLException {
    feedUrl = new URL(protocol, host, port, "/xmlfeed");
    dtdUrl = new URL(protocol, host, port, "/getdtd");
    contentEncodings = null;
    backlogUrl = new URL(protocol, host, port, "/getbacklogcount");
    lastBacklogCheck = 0L;
  }

  /** For the unit tests to verify the correct URLs. */
  public synchronized URL getFeedUrl() {
    return feedUrl;
  }

  public void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Set the backlog check parameters. The Feed connection can check to see
   * if the GSA is falling behind processing feeds by calling the GSA's
   * {@code getbacklogcount} servlet. If the number of outstanding feed
   * items exceeds the {@code ceiling}, then the GSA is considered
   * backlogged.  If the number of outstanding feed items then drops below
   * the {@code floor}, it may be considered no longer backlogged.
   *
   * @param floor backlog count floor value, below which the GSA is no
   *        longer considered backlogged.
   * @param ceiling backlog count ceiling value, above which the GSA is
   *        considered backlogged.
   * @param interval number of seconds to wait between backlog count checks.
   */
  public void setBacklogCheck(int floor, int ceiling, int interval) {
    backlogFloor = floor;
    backlogCeiling = ceiling;
    backlogCheckInterval = interval * 1000L;
  }

  public void setContentEncodings(String contentEncodings) {
    this.contentEncodings = contentEncodings;
  }

  /**
   * Sets whether HTTPS connections to the GSA validate the GSA certificate.
   */
  public void setValidateCertificate(boolean validateCertificate) {
    this.validateCertificate = validateCertificate;
  }

  /** For the unit tests. */
  public boolean getValidateCertificate() {
    return validateCertificate;
  }

  private static final void controlHeader(StringBuilder builder,
        String name, String mimetype) {
    builder.append("--").append(BOUNDARY).append(CRLF);
    builder.append("Content-Disposition: form-data;");
    builder.append(" name=\"").append(name).append("\"").append(CRLF);
    builder.append("Content-Type: ").append(mimetype).append(CRLF);
    builder.append(CRLF);
  }

  /* @Override */
  public String sendData(FeedData feedData)
      throws FeedException {
    try {
      String response = sendFeedData((XmlFeed)feedData);
      gotFeedError = !response.equalsIgnoreCase(SUCCESS_RESPONSE);
      return response;
    } catch (FeedException fe) {
      gotFeedError = true;
      throw fe;
    }
  }

  private String sendFeedData(XmlFeed feed)
      throws FeedException {
    String feedType = feed.getFeedType().toLegacyString();
    String dataSource = feed.getDataSource();
    OutputStream outputStream;
    HttpURLConnection uc;
    StringBuilder buf = new StringBuilder();
    byte[] prefix;
    byte[] suffix;
    try {
      // Build prefix.
      controlHeader(buf, "datasource", ServletUtil.MIMETYPE_TEXT_PLAIN);
      buf.append(dataSource).append(CRLF);
      controlHeader(buf, "feedtype", ServletUtil.MIMETYPE_TEXT_PLAIN);
      buf.append(feedType).append(CRLF);
      controlHeader(buf, "data", ServletUtil.MIMETYPE_XML);
      prefix = buf.toString().getBytes("UTF-8");

      // Build suffix.
      buf.setLength(0);
      buf.append(CRLF).append("--").append(BOUNDARY).append("--").append(CRLF);
      suffix = buf.toString().getBytes("UTF-8");

      LOGGER.finest("Opening feed connection.");
      synchronized (this) {
        uc = (HttpURLConnection) feedUrl.openConnection();
      }
      if (uc instanceof HttpsURLConnection && !validateCertificate) {
        SslUtil.setTrustingHttpsOptions((HttpsURLConnection) uc);
      }
      uc.setDoInput(true);
      uc.setDoOutput(true);
      uc.setFixedLengthStreamingMode(prefix.length + feed.size()
          + suffix.length);
      uc.setRequestProperty("Content-Type", "multipart/form-data; boundary="
          + BOUNDARY);
      outputStream = uc.getOutputStream();
    } catch (IOException ioe) {
      throw new FeedException(ioe);
    } catch (GeneralSecurityException e) {
      throw new FeedException(e);
    }

    boolean isThrowing = false;
    buf.setLength(0);
    try {
      LOGGER.finest("Writing feed data to feed connection.");
      // If there is an exception during this read/write, we do our
      // best to close the url connection and read the result.
      try {
        outputStream.write(prefix);
        feed.writeTo(outputStream);
        outputStream.write(suffix);
        outputStream.flush();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE,
            "IOException while posting: will retry later", e);
        isThrowing = true;
        throw new FeedException(e);
      } catch (RuntimeException e) {
        isThrowing = true;
        throw e;
      } catch (Error e) {
        isThrowing = true;
        throw e;
      } finally {
        try {
          outputStream.close();
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE,
              "IOException while closing after post: will retry later", e);
          if (!isThrowing) {
            isThrowing = true;
            throw new FeedException(e);
          }
        }
      }
    } finally {
      BufferedReader br = null;
      try {
        LOGGER.finest("Waiting for response from feed connection.");
        InputStream inputStream = uc.getInputStream();
        br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
        String line;
        while ((line = br.readLine()) != null) {
          buf.append(line);
        }
      } catch (IOException ioe) {
        if (!isThrowing) {
          throw new FeedException(ioe);
        }
      } finally {
        try {
          if (br != null) {
            br.close();
          }
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE,
                     "IOException while closing after post: continuing", e);
        }
        if (uc != null) {
          uc.disconnect();
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Received response from feed connection: "
                        + buf.toString());
        }
      }
    }
    return buf.toString();
  }

  /* @Override */
  public synchronized String getContentEncodings() {
    if (contentEncodings == null) {
      String dtd = getDtd();
      if (dtd == null) {
        // Failed to get a DTD. Assume the GSA only supports base64 encoded.
        contentEncodings = "base64binary";
      } else {
        // TODO: Extract the supported content encodings from the DTD.
        // As of GSA 6.2, returning a DTD at all also means compression
        // is supported.
        contentEncodings = "base64binary,base64compressed";
      }
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("GSA supports Content Encodings: " + contentEncodings);
      }
    }
    return contentEncodings;
  }

  /* @Override */
  public synchronized boolean isBacklogged() {
    if (lastBacklogCheck != Long.MAX_VALUE) {
      long now = clock.getTimeMillis();
      if ((now - lastBacklogCheck) > backlogCheckInterval) {
        lastBacklogCheck = now;
        // If we got a feed error and the feed is still down, delay.
        if (gotFeedError) {
          if (isFeedAvailable()) {
            gotFeedError = false;
          } else {
            // Feed is still unavailable.
            return true;
          }
        }
        try {
          int backlogCount = getBacklogCount();
          if (backlogCount >= 0) {
            if (isBacklogged) {
              // If we were backlogged, but have dropped below the
              // floor value, then we are no longer backlogged.
              if (backlogCount < backlogFloor) {
                isBacklogged = false;
                LOGGER.info("Resuming traversal after feed backlog clears.");
              }
            } else if (backlogCount > backlogCeiling) {
              // If the backlogcount exceeds the ceiling value,
              // then we are definitely backlogged.
              isBacklogged = true;
              LOGGER.info("Pausing traversal due to excessive feed backlog.");
            }
          }
        } catch (UnsupportedOperationException e) {
          // This older GSA does not support getbacklogcount.
          // Assume never backlogged and don't check again.
          isBacklogged = false;
          lastBacklogCheck = Long.MAX_VALUE;
          LOGGER.fine("Older GSA lacks backlogcount support.");
        }
      }
    }
    return isBacklogged;
  }

  /**
   * @return the current feed backlog count of the GSA,
   *         or -1 if the count is unavailable.
   * @throws UnsupportedOperationException if the GSA does
   *         not support getbacklogcount.
   */
  private int getBacklogCount() {
    try {
      HttpResponse response = doGet(backlogUrl, "backlogcount");
      if (response != null && response.content != null) {
        return Integer.parseInt(response.content);
      }
    } catch (NumberFormatException ignored) {
      // Got a non-integer backlog count - probably an error message,
      // which we have already logged (at Finest).  Simply return -1,
      // indicating that the backlogcount is not currently available.
    }
    // If we get here something bad happened.  It is not the case that the
    // GSA doesn't support getbacklogcount, but we still failed to retrieve it.
    return -1;
  }

  /**
   * Tests for feed error conditions such as insufficient disk space,
   * unauthorized clients, etc.  If the /xmlfeed command is sent with no
   * arguments, the server will return an error message and a 200 response
   * code if it can't accept feeds.  If it can continue to accept feeds, then
   * it will return a 400 bad request since it's missing required parameters.
   *
   * @return True if feed host is likely to accept a feed request.
   */
  private boolean isFeedAvailable() {
    try {
      HttpResponse response = doGet(feedUrl, "XmlFeed");
      if (response != null) {
        if (response.responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
          // The expected responseCode if no error conditions are present.
          LOGGER.finest("XmlFeed connection seems to be accepting new feeds.");
          return true;
        }
        if (response.content != null) {
          response.content.contains(SUCCESS_RESPONSE);
        }
      }
    } catch (UnsupportedOperationException ignored) {
      // This GSA does not support feeds?  Return false.
    }
    // If we get here something bad happened.
    return false;
  }

  /**
   * @return the current feed XML DTD for the GSA,
   *         or null if the DTD is unavailable.
   */
  private String getDtd() {
    try {
      HttpResponse response = doGet(dtdUrl, "DTD");
      if (response != null && response.content != null) {
        return response.content;
      }
    } catch (UnsupportedOperationException ignored) {
      // This older GSA does not support getdtd, so return null.
      LOGGER.fine("Older GSA lacks get DTD support.");
    }
    return null;
  }

  /**
   * Get the response to a URL request.  The response is returned
   * as an HttpResponse containing the HTTP ResponseCode and the
   * returned content as a String. The content String is only returned
   * if the response code was OK.
   *
   * @param url the URL to request
   * @param name the name of the feature requested (for logging)
   * @return HttpResponse representing response to an HTTP GET.
   *         or null if the GSA is unavailable.
   * @throws UnsupportedOperationException if the GSA does
   *         not support the requested feature.
   */
  private HttpResponse doGet(URL url, String name) {
    HttpURLConnection conn = null;
    BufferedReader br = null;
    String str = null;
    StringBuilder buf = new StringBuilder();
    try {
      if (LOGGER.isLoggable(Level.FINEST)) {
        LOGGER.finest("Opening " + name + " connection.");
      }
      conn = (HttpURLConnection)url.openConnection();
      if (conn instanceof HttpsURLConnection && !validateCertificate) {
        SslUtil.setTrustingHttpsOptions((HttpsURLConnection) conn);
      }
      conn.connect();
      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        br = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                                                      "UTF8"));
        while ((str = br.readLine()) != null) {
          buf.append(str);
        }
        str = buf.toString().trim();
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Received " + name + ": " + str);
        }
        return new HttpResponse(responseCode, str);
      } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
        throw new UnsupportedOperationException(
            "GSA lacks " + name + " support.");
      } else {
        return new HttpResponse(responseCode);
      }
    } catch (IOException ioe) {
      LOGGER.finest("Error while reading " + name + ": " + ioe.getMessage());
    } catch (GeneralSecurityException e) {
      LOGGER.finest("Error while reading " + name + ": " + e.getMessage());
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException e) {
        LOGGER.finest("Error after reading " + name + ": " + e.getMessage());
      }
      if (conn != null) {
        conn.disconnect();
      }
    }
    // If we get here something bad happened. It is not the case that the GSA
    // doesn't support the requested feature, but we failed to retrieve it.
    return null;
  }

  private static class HttpResponse {
    public int responseCode;  // The HTTP response code.
    public String content;    // The returned content as a String.

    public HttpResponse(int responseCode) {
      this(responseCode, null);
    }

    public HttpResponse(int responseCode, String content) {
      this.responseCode = responseCode;
      this.content = content;
    }
  }
}
