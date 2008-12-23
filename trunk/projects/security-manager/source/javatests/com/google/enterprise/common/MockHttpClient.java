// Copyright (C) 2008 Google Inc.
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

  public HttpExchange getExchange(URL url) {
    MockHttpServletRequest request =
        ServletTestUtil.makeMockHttpGet(null, url.toString());
    return new MockExchange(transport, request);
  }

  public HttpExchange postExchange(URL url, List<StringPair> parameters) {
    MockHttpServletRequest request =
        ServletTestUtil.makeMockHttpPost(null, url.toString());
    if (parameters != null) {
      for (StringPair p: parameters) {
        request.addParameter(p.getName(), p.getValue());
      }
      try {
        ServletTestUtil.generatePostContent(request);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
    return new MockExchange(transport, request);
  }

  private static class MockExchange implements HttpExchange {

    private final HttpTransport transport;
    private final MockHttpServletRequest request;
    private final MockHttpServletResponse response;
    private String credentials;

    public MockExchange(HttpTransport transport, MockHttpServletRequest request) {
      this.transport = transport;
      this.request = request;
      response = new MockHttpServletResponse();
      credentials = null;
    }

      public void setProxy(String proxy) {
    }

    public void setBasicAuthCredentials(String username, String password) {
      credentials = "Basic " + Base64.encode((username + ":" + password).getBytes());
    }

      public void setRequestHeader(String name, String value) {
      request.addHeader(name, value);
    }

      public int exchange() throws IOException {
      if (credentials != null) {
        request.addHeader("Authorize", credentials);
      }
      try {
        transport.exchange(request, response);
      } catch (ServletException e) {
        throw new IOException(e);
      }
      return response.getStatus();
    }

      public String getResponseEntityAsString() throws IOException {
      return response.getContentAsString();
    }

      public String getResponseHeaderValue(String name) {
      return String.class.cast(response.getHeader(name));
    }

      public List<String> getResponseHeaderValues(String name) {
      List<String> result = new ArrayList<String>();
      for (Object value: response.getHeaders(name)) {
        result.add(String.class.cast(value));
      }
      return result;
    }

      public int getStatusCode() {
      return response.getStatus();
    }

    public void setRequestBody(byte[] requestContent) {
      request.setContent(requestContent);
    }
    
      public void close() {
    }

  }
}
