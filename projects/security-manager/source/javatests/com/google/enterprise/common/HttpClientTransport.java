// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.common;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.enterprise.common.ServletTestUtil.servletRequestToString;
import static com.google.enterprise.common.ServletTestUtil.servletResponseToString;

public final class HttpClientTransport implements HttpTransport {
  private static final Logger LOGGER = Logger.getLogger(HttpClientTransport.class.getName());

  private final HttpClient userAgent;

  public HttpClientTransport() {
    userAgent = new HttpClient();
  }

  public void exchange(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String method = request.getMethod();
    if ("GET".equals(method)) {
      doGet(request, response);
    } else if ("POST".equals(method)) {
      doPost(request, response);
    } else {
      throw new ServletException("Unknown request method: " + method);
    }
  }

  private void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    GetMethod method = new GetMethod(requestUrl(request));
    method.setFollowRedirects(true);
    doExchange(request, response, method);
  }

  private void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PostMethod method = new PostMethod(requestUrl(request));
    method.setRequestEntity(
        new InputStreamRequestEntity(request.getInputStream(),
                                     request.getContentType()));
    doExchange(request, response, method);
  }

  private String requestUrl(HttpServletRequest request) {
    return request.getRequestURL().toString();
  }

  private void doExchange(HttpServletRequest request, HttpServletResponse response,
                          HttpMethodBase method)
      throws ServletException, IOException {
    LOGGER.log(Level.INFO, servletRequestToString(request, "Request"));
    try {
      @SuppressWarnings("unchecked") Enumeration<String> names = request.getHeaderNames();
      while (names.hasMoreElements()) {
        String name = names.nextElement();
        String value = request.getHeader(name);
        method.addRequestHeader(name, value);
      }
      response.setStatus(userAgent.executeMethod(method));
      for (Header h: method.getResponseHeaders()) {
        response.addHeader(h.getName(), h.getValue());
      }
      InputStream in = method.getResponseBodyAsStream();
      OutputStream out = response.getOutputStream();
      byte[] buffer = new byte[0x1000];
      while(true) {
        int nRead = in.read(buffer);
        if (nRead < 0) {
          break;
        }
        out.write(buffer, 0, nRead);
      }
      in.close();
      out.flush();
      out.close();
    } catch (HttpException e) {
      throw new ServletException(e);
    } finally {
      method.releaseConnection();
    }
    LOGGER.log(Level.INFO,
               servletResponseToString((MockHttpServletResponse) response, "Response"));
  }
}
