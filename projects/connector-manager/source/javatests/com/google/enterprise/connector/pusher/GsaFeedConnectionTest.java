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

import static com.google.common.base.Charsets.UTF_8;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import junit.framework.TestCase;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.OutputStream;

/** Tests for {@link GsaFeedConnection} URLs. */
public class GsaFeedConnectionTest extends TestCase {
  private HttpServer server;
  private DtdHandler handler;
  private GsaFeedConnection feedConnection;

  public void setUp() throws IOException {
    handler = new DtdHandler();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/", handler);
    server.start();
    int port = server.getAddress().getPort();
    feedConnection = new GsaFeedConnection("http", "localhost", port, -1);
  }

  public void tearDown() {
    server.stop(0);
  }

  static class DtdHandler implements HttpHandler {
    private String content;

    void setContent(String content) {
      this.content = content;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      byte[] response = content.getBytes(UTF_8);
      exchange.sendResponseHeaders(200, response.length);
      OutputStream body = exchange.getResponseBody();
      body.write(response);
      exchange.close();
    }
  }

  public void testSupportsInheritedAcls_true() throws IOException {
    handler.setContent("<!ELEMENT acl (principal*)>");
    assertEquals(true, feedConnection.supportsInheritedAcls());
  }

  public void testSupportsInheritedAcls_false() throws IOException {
    handler.setContent("<!ELEMENT metadata (meta*)>");
    assertEquals(false, feedConnection.supportsInheritedAcls());
  }

  public void testSupportsInheritedAcls_error() throws IOException {
    server.removeContext("/");
    assertEquals(false, feedConnection.supportsInheritedAcls());
  }

  public void testSupportsInheritedAcls_cached() throws IOException {
    handler.setContent("<!ELEMENT acl (principal*)>");
    assertEquals(true, feedConnection.supportsInheritedAcls());

    server.removeContext("/");
    assertEquals(true, feedConnection.supportsInheritedAcls());
  }

  /** Tests that the content encodings do not rely on the DTD. */
  public void testGetContentEncodings() throws IOException {
    server.removeContext("/");
    assertEquals("base64compressed,base64binary",
        feedConnection.getContentEncodings());
  }

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
