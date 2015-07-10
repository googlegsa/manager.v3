// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.util;

import static com.google.common.base.Charsets.UTF_8;
import static org.junit.Assert.assertEquals;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class UrlValidatorTest {
  private HttpServer server;
  private MockHandler handler;
  private String url;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    handler = new MockHandler();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/", handler);
    server.start();
    int port = server.getAddress().getPort();
    url = "http://localhost:" + port;
  }

  public void tearDown() {
    server.stop(0);
  }

  static class MockHandler implements HttpHandler {
    private IOException thrown = null;
    private int statusCode = 200;

    public void setException(IOException thrown) {
      this.thrown = thrown;
    }

    public void setResponseCode(int statusCode) {
      this.statusCode = statusCode;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (thrown != null) {
        throw thrown;
      }
      byte[] response = "hello, world".getBytes(UTF_8);
      exchange.sendResponseHeaders(statusCode, response.length);
      OutputStream body = exchange.getResponseBody();
      body.write(response);
      exchange.close();
    }
  }

  @Test
  public void testValidate_200() throws Exception {
    handler.setResponseCode(200);
    UrlValidator out = new UrlValidator();
    int statusCode = out.validate(url);
    assertEquals(200, statusCode);
  }

  @Test
  public void testValidate_401() throws Exception {
    handler.setResponseCode(401);
    UrlValidator out = new UrlValidator();
    int statusCode = out.validate(url);
    assertEquals(401, statusCode);
  }

  @Test
  public void testValidate_404() throws Exception {
    handler.setResponseCode(404);
    UrlValidator out = new UrlValidator();
    thrown.expect(UrlValidatorException.class);
    out.validate(url);
  }

  @Test
  public void testValidate_invalidUrl() throws Exception {
    UrlValidator out = new UrlValidator();
    thrown.expect(IllegalArgumentException.class);
    out.validate("http:what-is;this");
  }

  @Test
  public void testValidate_malformedUrl() throws Exception {
    UrlValidator out = new UrlValidator();
    thrown.expect(MalformedURLException.class);
    out.validate(url.substring(1));
  }

  @Test
  public void testValidate_unknownHost() throws Exception {
    handler.setException(new UnknownHostException());
    UrlValidator out = new UrlValidator();
    thrown.expect(UnknownHostException.class);
    // TODO(jlacey): non-FQHNs do not throw this exception. It would
    // be nice to find a way to not hit external DNS for this.
    out.validate("http://google-non-existent.google.com/");
  }
}
