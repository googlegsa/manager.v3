// Copyright 2006 Google Inc.
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

package com.google.enterprise.common;

import com.google.enterprise.common.ServletTestUtil;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

/**
 * A mock instance of HttpClientInterface, using HttpTransport for transport.
 */
public class MockHttpClient implements HttpClientInterface {

  private final HttpTransport transport;

  public MockHttpClient(HttpTransport transport) {
    this.transport = transport;
  }

  /** {@inheritDoc} */
  public HttpExchange newExchange(String method, URL url, List<StringPair> parameters) {
    return new MockExchange(transport, method, url, parameters);
  }

  private static class MockExchange implements HttpExchange {

    private final HttpTransport transport;
    private final MockHttpServletRequest request;
    private final MockHttpServletResponse response;

    public MockExchange(HttpTransport transport, String method, URL url, List<StringPair> parameters) {
      this.transport = transport;
      this.request = ServletTestUtil.makeMockHttpRequest(method, null, url.toString());
      this.response = new MockHttpServletResponse();

      if ("POST".equalsIgnoreCase(method)) {
        setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
      }
    }

    /** {@inheritDoc} */
    public void setProxy(String proxy) {
    }

    /** {@inheritDoc} */
    public void setRequestHeader(String name, String value) {
      request.addHeader(name, value);
    }

    /** {@inheritDoc} */
    public int exchange() throws IOException {
      try {
        transport.exchange(request, response);
      } catch (ServletException e) {
        throw new IOException(e);
      }
      return response.getStatus();
    }

    /** {@inheritDoc} */
    public String getResponseEntityAsString() throws IOException {
      return response.getContentAsString();
    }

    /** {@inheritDoc} */
    public String getResponseHeaderValue(String name) {
      return String.class.cast(response.getHeader(name));
    }

    /** {@inheritDoc} */
    public List<String> getResponseHeaderValues(String name) {
      List<String> result = new ArrayList<String>();
      for (Object value: response.getHeaders(name)) {
        result.add(String.class.cast(value));
      }
      return result;
    }

    /** {@inheritDoc} */
    public int getStatusCode() {
      return response.getStatus();
    }

    /** {@inheritDoc} */
    public void close() {
    }
  }
}
