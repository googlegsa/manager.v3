// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.servlet.ServletUtil;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Opens a connection to a url and sends data to it.
 */
public class GsaFeedConnection implements FeedConnection {

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

  // multipart/form-data uploads require a boundary to delimit controls.
  // Since we XML-escape or base64-encode all data provided by the connector,
  // the feed XML will never contain "<<".
  private static final String BOUNDARY = "<<";

  // All current GSAs only support legacy Schedule formats.
  private int scheduleFormat = 1;

  // Content encodings supported by GSA.
  // TODO: Get compressed encoding support from server.
  private String contentEncodings = "base64binary";

  // True if we recently got a feed error of some sort.
  private boolean gotFeedError = false;

  // XmlFeed URL
  private URL feedUrl = null;

  // BacklogCount URL
  private URL backlogUrl = null;

  // BacklogCount Ceiling. Throttle back feed if backlog exceeds the ceiling.
  private int backlogCeiling = 50000;

  // BacklogCount Floor. Stop throttling feed if backlog drops below floor.
  private int backlogFloor = 15000;

  // True if the feed is throttled back due to excessive backlog.
  private boolean isBacklogged = false;

  // Time of last backlog check.
  private long lastBacklogCheck;

  // How often to check for backlog (in milliseconds).
  private long backlogCheckInterval = 15 * 60 * 1000L;

  private static final Logger LOGGER =
      Logger.getLogger(GsaFeedConnection.class.getName());

  public GsaFeedConnection(String host, int port) throws MalformedURLException {
    this.setFeedHostAndPort(host, port);
  }

  public synchronized void setFeedHostAndPort(String host, int port)
      throws MalformedURLException {
    feedUrl = new URL("http", host, port, "/xmlfeed");
    backlogUrl = new URL("http", host, port, "/getbacklogcount");
    lastBacklogCheck = 0L;
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

  public void setScheduleFormat(int scheduleFormatVersion) {
    this.scheduleFormat = scheduleFormatVersion;
  }

  public void setContentEncodings(String contentEncodings) {
    this.contentEncodings = contentEncodings;
  }

  private static final void writeMultipartControlHeader(
      OutputStream outputStream,
      String name,
      String mimetype)
      throws IOException {
    outputStream.write(("--" + BOUNDARY + "\n").getBytes());
    outputStream.write(("Content-Disposition: form-data;").getBytes());
    outputStream.write((" name=\"" + name + "\"\n").getBytes());
    outputStream.write(("Content-Type: " + mimetype + "\n").getBytes());
    outputStream.write("\n".getBytes());
  }

  //@Override
  public String sendData(String dataSource, FeedData feedData)
      throws FeedException, RepositoryException {
    try {
      String response = sendFeedData(dataSource, feedData);
      gotFeedError = !response.equalsIgnoreCase(SUCCESS_RESPONSE);
      return response;
    } catch (FeedException fe) {
      gotFeedError = true;
      throw fe;
    }
  }

  private String sendFeedData(String dataSource, FeedData feedData)
      throws FeedException, RepositoryException {
    String feedType = ((GsaFeedData)feedData).getFeedType();
    InputStream data = ((GsaFeedData)feedData).getData();
    OutputStream outputStream;
    URLConnection uc;
    try {
      LOGGER.finest("Opening feed connection.");
      synchronized (this) {
        uc = feedUrl.openConnection();
      }
      uc.setDoInput(true);
      uc.setDoOutput(true);
      uc.setRequestProperty("Content-Type", "multipart/form-data; boundary="
          + BOUNDARY);
      outputStream = uc.getOutputStream();
    } catch (IOException ioe) {
      throw new FeedException(ioe);
    }

    boolean isThrowing = false;
    StringBuilder buf = new StringBuilder();
    try {
      LOGGER.finest("Writing to feed connection.");
      // If there is an exception during this read/write, we do our
      // best to close the url connection and read the result.
      try {
        writeMultipartControlHeader(outputStream,
                                    "datasource",
                                    ServletUtil.MIMETYPE_TEXT_PLAIN);
        outputStream.write((dataSource + "\n").getBytes());

        writeMultipartControlHeader(outputStream,
                                    "feedtype",
                                    ServletUtil.MIMETYPE_TEXT_PLAIN);
        outputStream.write((feedType + "\n").getBytes());

        writeMultipartControlHeader(outputStream,
                                    "data",
                                    ServletUtil.MIMETYPE_XML);
        byte[] bytebuf = new byte[32768];
        int val;
        while (true) {
          // Handle input exceptions differently than output exceptions.
          try {
            if ((val = data.read(bytebuf)) == -1) {
              break;
            }
          } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE,
                       "IOException while reading: skipping", ioe);
            Throwable t = ioe.getCause();
            isThrowing = true;
            if (t != null && (t instanceof RepositoryException)) {
              throw (RepositoryException) t;
            } else {
              throw new RepositoryDocumentException(ioe);
            }
          }
          outputStream.write(bytebuf, 0, val);
        }
        outputStream.write(("\n--" + BOUNDARY + "--\n").getBytes());
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
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Received response from feed connection: "
                        + buf.toString());
        }
      }
    }
    return buf.toString();
  }

  //@Override
  public synchronized boolean isBacklogged() {
    if (lastBacklogCheck != Long.MAX_VALUE) {
      long now = System.currentTimeMillis();
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
                LOGGER.fine("Resuming traversal after feed backlog clears.");
              }
            } else if (backlogCount > backlogCeiling) {
              // If the backlogcount exceeds the ceiling value,
              // then we are definitely backlogged.
              isBacklogged = true;
              LOGGER.fine("Pausing traversal due to excessive feed backlog.");
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
    HttpURLConnection conn = null;
    BufferedReader br = null;
    String str = null;
    StringBuilder buf = new StringBuilder();
    try {
      LOGGER.finest("Opening backlogcount connection.");
      synchronized (this) {
        conn = (HttpURLConnection)backlogUrl.openConnection();
      }
      conn.connect();
      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        br = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                                                      "UTF8"));
        while ((str = br.readLine()) != null) {
          buf.append(str);
        }
        str = buf.toString();
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Received backlogcount: " + str);
        }
        return Integer.parseInt(str.trim());
      } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
        throw new UnsupportedOperationException(
            "Older GSA lacks backlogcount support.");
      }
    } catch (IOException ioe) {
      LOGGER.finest("Error while reading backlogcount: " + ioe.getMessage());
    } catch (NumberFormatException ignored) {
      // Got a non-integer backlog count - probably an error message,
      // which we have already logged (at Finest).  Simply return -1,
      // indicating that the backlogcount is not currently available.
    } finally {
      try {
        if (br != null) {
          br.close();
        }
        if (conn != null) {
          conn.disconnect();
        }
      } catch (IOException e) {
        LOGGER.finest("Error after reading backlogcount: " + e.getMessage());
      }
    }
    // If we get here something bad happened.  It is not the case that the
    // GSA doesn't support getbacklogcount, but we still failed to retrieve it.
    return -1;
  }

  /**
   * Tests for feed error conditions such as insufficient disk space,
   * unauthorized clients, etc.  If the /xmlfeed command is sent with no
   * arguments the server will return an error message and a 200 response
   * code if it can't accept feeds.  If it can continue to accept feeds, then
   * it will return a 400 bad request since it's missing required parameters.
   *
   * @return True if feed host is likely to accept a feed request.
   */
  private boolean isFeedAvailable() {
    HttpURLConnection conn = null;
    BufferedReader br = null;
    String str = null;
    StringBuilder buf = new StringBuilder();
    try {
      LOGGER.finest("Opening xmlfeed connection.");
      synchronized (this) {
        conn = (HttpURLConnection)feedUrl.openConnection();
      }
      conn.connect();
      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        br = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                                                      "UTF8"));
        while ((str = br.readLine()) != null) {
          buf.append(str);
        }
        str = buf.toString();
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Received response: " + str);
        }
        return str.contains(SUCCESS_RESPONSE);
      } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
        // The expected responseCode if no error conditions are present.
        LOGGER.finest("Xmlfeed connection seems to be accepting new feeds.");
        return true;
      }
    } catch (IOException ioe) {
      LOGGER.finest("Error while reading feed status: " + ioe.getMessage());
    } finally {
      try {
        if (br != null) {
          br.close();
        }
        if (conn != null) {
          conn.disconnect();
        }
      } catch (IOException e) {
        LOGGER.finest("Error after reading feed status: " + e.getMessage());
      }
    }
    return false;
  }

  //@Override
  public int getScheduleFormat() {
    return scheduleFormat;
  }

  //@Override
  public String getContentEncodings() {
    return contentEncodings;
  }
}
