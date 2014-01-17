// Copyright 2011 Google Inc.
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

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

/** Tests for {@link GsaFeedConnection} URLs. */
public class GsaFeedConnectionTest extends TestCase {
  private void assertFeedUrl(String protocol,
      String host, int port, GsaFeedConnection feeder) {
    URL url = feeder.getFeedUrl();
    assertEquals(url.toString(), protocol, url.getProtocol());
    assertEquals(url.toString(), host, url.getHost());
    assertEquals(url.toString(), port, url.getPort());
  }

  public void testNullPort() throws MalformedURLException {
    assertFeedUrl("http", "myhost", 19900,
        new GsaFeedConnection(null, "myhost", 19900, -1));
  }

  public void testNullSecurePort() throws MalformedURLException {
    assertFeedUrl("https", "myhost", 19902,
        new GsaFeedConnection(null, "myhost", -1, 19902));
  }

  public void testNullBoth() throws MalformedURLException {
    assertFeedUrl("https", "myhost", 19902,
        new GsaFeedConnection(null, "myhost", 19900, 19902));
  }

  public void testEmptyPort() throws MalformedURLException {
    assertFeedUrl("http", "myhost", 19900,
        new GsaFeedConnection("", "myhost", 19900, -1));
  }

  public void testEmptySecurePort() throws MalformedURLException {
    assertFeedUrl("https", "myhost", 19902,
        new GsaFeedConnection("", "myhost", -1, 19902));
  }

  public void testEmptyBoth() throws MalformedURLException {
    assertFeedUrl("https", "myhost", 19902,
        new GsaFeedConnection("", "myhost", 19900, 19902));
  }

  public void testHttpPort() throws MalformedURLException {
    assertFeedUrl("http", "myhost", 19900,
        new GsaFeedConnection("http", "myhost", 19900, -1));
  }

  public void testHttpSecurePort() throws MalformedURLException {
    assertFeedUrl("http", "myhost", -1,
        new GsaFeedConnection("http", "myhost", -1, 19902));
  }

  public void testHttpBoth() throws MalformedURLException {
    assertFeedUrl("http", "myhost", 19900,
        new GsaFeedConnection("http", "myhost", 19900, 19902));
  }

  public void testHttpsPort() throws MalformedURLException {
    assertFeedUrl("https", "myhost", -1,
        new GsaFeedConnection("https", "myhost", 19900, -1));
  }

  public void testHttpsSecurePort() throws MalformedURLException {
    assertFeedUrl("https", "myhost", 19902,
        new GsaFeedConnection("https", "myhost", -1, 19902));
  }

  public void testHttpsBoth() throws MalformedURLException {
    assertFeedUrl("https", "myhost", 19902,
        new GsaFeedConnection("https", "myhost", 19900, 19902));
  }
}
