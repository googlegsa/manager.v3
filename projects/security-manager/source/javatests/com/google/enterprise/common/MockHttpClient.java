// Copyright (C) 2008, 2009 Google Inc.
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
import org.springframework.mock.web.MockHttpSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import static com.google.enterprise.common.ServletTestUtil.generatePostContent;
import static com.google.enterprise.common.ServletTestUtil.makeMockHttpGet;
import static com.google.enterprise.common.ServletTestUtil.makeMockHttpPost;

/**
 * A mock instance of HttpClientInterface, using HttpTransport for transport.
 */
public class MockHttpClient implements HttpClientInterface {

  private final HttpTransport transport;
  private final MockHttpSession session;
  private String referrer;

  public MockHttpClient(HttpTransport transport) {
    this.transport = transport;
    session = new MockHttpSession();
    referrer = null;
  }

  public HttpExchange getExchange(URL url) {
    MockHttpServletRequest request = makeMockHttpGet(null, url.toString());
    return new MockExchange(request);
  }

  public HttpExchange postExchange(URL url, List<StringPair> parameters) {
    MockHttpServletRequest request = makeMockHttpPost(null, url.toString());
    if (parameters != null) {
      for (StringPair p: parameters) {
        request.addParameter(p.getName(), p.getValue());
      }
    }
    return new MockExchange(request);
  }

  private class MockExchange implements HttpExchange {

    private final MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private String credentials;
    private boolean followRedirects;

    public MockExchange(MockHttpServletRequest request) {
      this.request = request;
      credentials = null;
      followRedirects = false;
    }

    public void setProxy(String proxy) {
    }

    public void setBasicAuthCredentials(String username, String password) {
      credentials = "Basic " + Base64.encode((username + ":" + password).getBytes());
    }

    public void setFollowRedirects(boolean followRedirects) {
      this.followRedirects = followRedirects;
    }

    public void addParameter(String name, String value) {
      request.addParameter(name, value);
    }

    public void setRequestHeader(String name, String value) {
      request.addHeader(name, value);
    }

    public int exchange() throws IOException {
      if (request.getMethod().equalsIgnoreCase("POST")
          && request.getParameterNames().hasMoreElements()) {
        generatePostContent(request);
      }
      MockHttpServletResponse response = exchange1(request);
      if (followRedirects) {
        while (isRedirect(response)) {
          response = exchange1(makeMockHttpGet(null, getRedirectUrl(response)));
        }
      }
      this.response = response;
      return response.getStatus();
    }

    private boolean isRedirect(MockHttpServletResponse response) {
      int status = response.getStatus();
      return (status == 303) || (status == 302);
    }

    private String getRedirectUrl(MockHttpServletResponse response) {
      return String.class.cast(response.getHeader("Location"));
    }

    private MockHttpServletResponse exchange1(MockHttpServletRequest request)
        throws IOException {
      MockHttpServletResponse response = new MockHttpServletResponse();
      if (referrer != null) {
        request.addHeader("Referer", referrer);
      }
      if (credentials != null) {
        request.addHeader("Authorize", credentials);
      }
      request.setSession(session);
      try {
        transport.exchange(request, response);
      } catch (ServletException e) {
        IOException ee = new IOException();
        ee.initCause(e);
        throw ee;
      }
      referrer = getReferrer(request);
      return response;
    }

    private String getReferrer(MockHttpServletRequest request) {
      String referrer = request.getRequestURL().toString();
      int q = referrer.indexOf("?");
      if (q >= 0) {
        referrer = referrer.substring(0, q);
      }
      return referrer;
    }

    public String getResponseEntityAsString() throws IOException {
      return response.getContentAsString();
    }

    public InputStream getResponseEntityAsStream() {
      return new ByteArrayInputStream(response.getContentAsByteArray());
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
