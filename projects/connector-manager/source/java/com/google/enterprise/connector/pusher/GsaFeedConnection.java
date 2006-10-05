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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Opens a connection to a url and sends data to it.
 */
public class GsaFeedConnection implements FeedConnection {
  
  private URL url = null;;
  private String host;
  private int port;
  
  public GsaFeedConnection(String host, int port) {
    this.host = host;
    this.port = port;
  }
  
  /*
   * Urlencodes the xml string.
   */
  private String encode(String data) throws UnsupportedEncodingException {
    String encodedData =
        URLEncoder.encode(data, DocPusher.XML_DEFAULT_ENCODING);
    return encodedData;
  }

  /*
   * Generates the feed url for a given GSA host.
   */
  private URL getFeedUrl() throws MalformedURLException{
    String feedUrl = "http://" + host + ":" + port + "/xmlfeed";
    URL url = new URL(feedUrl);
    return url;
  }
  
  public String sendData(String data) throws IOException {
    if (url == null) {
      url = getFeedUrl();
    }
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    OutputStreamWriter osw = new OutputStreamWriter(uc.getOutputStream());
    osw.write(encode(data));
    osw.flush();
    
    StringBuffer buf = new StringBuffer();
    BufferedReader br =
      new BufferedReader(new InputStreamReader(uc.getInputStream()));
    String line;
    while ((line = br.readLine()) != null) {
      buf.append(line);
    }
    osw.close();
    br.close();
    return buf.toString();
  }
  
  
}
