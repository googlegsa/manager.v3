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
   * The GSA's response when there was an internal error.
   */
  public static final String INTERNAL_ERROR_RESPONSE = "Internal Error";

  // multipart/form-data uploads require a boundary to delimit controls.
  // Since we XML-escape or base64-encode all data provided by the connector,
  // the feed XML will never contain "<<".
  private static final String BOUNDARY = "<<";

  private URL url = null;

  private static final Logger LOGGER =
      Logger.getLogger(GsaFeedConnection.class.getName());

  public GsaFeedConnection(String host, int port) throws MalformedURLException {
    this.setFeedHostAndPort(host, port);
  }

  public synchronized void setFeedHostAndPort(String host, int port)
      throws MalformedURLException {
    url = new URL("http", host, port, "/xmlfeed");
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

  public String sendData(String dataSource, FeedData feedData)
      throws FeedException, RepositoryException {
    String feedType = ((GsaFeedData)feedData).getFeedType();
    InputStream data = ((GsaFeedData)feedData).getData();
    OutputStream outputStream;
    URLConnection uc;
    try {
      synchronized (this) {
        uc = url.openConnection();
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
    StringBuffer buf = new StringBuffer();
    try {
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
        byte[] bytebuf = new byte[2048];
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
      }
    }
    return buf.toString();
  }
}
