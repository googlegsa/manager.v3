// Copyright 2006 Google Inc.  All Rights Reserved.
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

  public synchronized void setFeedHostAndPort(String host, int port) throws MalformedURLException {
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

  public String sendData(String dataSource, String feedType, InputStream data)
      throws IOException {
    URLConnection uc;
    synchronized(this) {
      uc = url.openConnection();
    }
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setRequestProperty("Content-Type",
                          "multipart/form-data; boundary=" + BOUNDARY);
    OutputStream outputStream = uc.getOutputStream();

    byte[] bytebuf = new byte[32768];
    int val;

    try {
      LOGGER.finest("Writing to feed connection.");
      // if there is an exception during this read/write, we do our
      // best to close the url connection and read the result

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
      while (-1 != (val = data.read(bytebuf))) {
        outputStream.write(bytebuf, 0, val);
      }
      outputStream.write("\n".getBytes());

      outputStream.write(("--" + BOUNDARY + "--\n").getBytes());
      outputStream.flush();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "IOException while posting: continuing", e);
    } finally {
      try {
        outputStream.close();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE,
            "IOException while closing after post: continuing", e);
      }
    }

    LOGGER.finest("Waiting for response from feed connection.");
    StringBuffer buf = new StringBuffer();
    InputStream inputStream = uc.getInputStream();
    BufferedReader br =
        new BufferedReader(new InputStreamReader(inputStream,"UTF8"));
    String line;
    while ((line = br.readLine()) != null) {
      buf.append(line);
    }
    br.close();
    String response = buf.toString();
    LOGGER.finest("Received response from feed connection: " + response);
    return response;
  }

}
