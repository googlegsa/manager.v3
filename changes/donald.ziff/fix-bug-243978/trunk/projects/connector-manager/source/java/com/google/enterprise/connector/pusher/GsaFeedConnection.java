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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Opens a connection to a url and sends data to it.
 */
public class GsaFeedConnection implements FeedConnection {
  
  private URL url = null;
  private String host;
  private int port;
  
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
  
  public String sendData(InputStream data) throws IOException {
    if (url == null) {
      url = getFeedUrl();
    }
    URLConnection uc = url.openConnection();
    uc.setDoInput(true);
    uc.setDoOutput(true);
    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    OutputStream outputStream = uc.getOutputStream();
    byte[] bytebuf = new byte[2048];
    int val;
    while (-1 != (val = data.read(bytebuf))) {
      outputStream.write(bytebuf, 0, val);
    }
    outputStream.flush();
    
    StringBuffer buf = new StringBuffer();
    InputStream inputStream = uc.getInputStream();
	BufferedReader br =
      new BufferedReader(new InputStreamReader(inputStream,"UTF8"));
    String line;
    while ((line = br.readLine()) != null) {
      buf.append(line);
    }
    outputStream.close();
    br.close();
    return buf.toString();
  }
  
  
}
