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

  // multipart/form-data uploads require a boundary to delimit controls.
  // Since we XML-escape or base64-encode all data provided by the connector,
  // the feed XML will never contain "<<".
  private static final String BOUNDARY = "<<";

  private URL url = null;
  private String host;
  private int port;
  
  private static final Logger LOGGER =
      Logger.getLogger(GsaFeedConnection.class.getName());
  
  public GsaFeedConnection(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /*
   * Generates the feed url for a given GSA host.
   */
  private URL getFeedUrl() throws MalformedURLException{
    String feedUrl = "http://" + host + ":" + port + "/xmlfeed";
    URL url = new URL(feedUrl);
    return url;
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
    if (url == null) {
      url = getFeedUrl();
    }
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setRequestProperty("Content-Type",
                          "multipart/form-data; boundary=" + BOUNDARY);
    OutputStream outputStream = uc.getOutputStream();

    byte[] bytebuf = new byte[2048];
    int val;

    try {
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

    StringBuffer buf = new StringBuffer();
    InputStream inputStream = uc.getInputStream();
	BufferedReader br =
      new BufferedReader(new InputStreamReader(inputStream,"UTF8"));
    String line;
    while ((line = br.readLine()) != null) {
      buf.append(line);
    }
    br.close();
    return buf.toString();
  }

}
