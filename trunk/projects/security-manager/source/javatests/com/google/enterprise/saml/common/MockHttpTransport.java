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

package com.google.enterprise.saml.common;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.enterprise.saml.common.SamlTestUtil.servletRequestToString;
import static com.google.enterprise.saml.common.SamlTestUtil.servletResponseToString;

/**
 * A trivial HttpTransport that just maps request URLs to registered servlets.
 */
public final class MockHttpTransport implements HttpTransport {
  private static final Logger LOGGER = Logger.getLogger(MockHttpTransport.class.getName());

  private final Map<String, GettableHttpServlet> getMap;
  private final Map<String, PostableHttpServlet> postMap;

  public MockHttpTransport() {
    getMap = new HashMap<String, GettableHttpServlet>();
    postMap = new HashMap<String, PostableHttpServlet>();
  }

  public void registerServlet(String url, HttpServlet servlet) throws ServletException {
    servlet.init(new MockServletConfig());
    if (servlet instanceof GettableHttpServlet) {
      getMap.put(url, (GettableHttpServlet) servlet);
    }
    if (servlet instanceof PostableHttpServlet) {
      postMap.put(url, (PostableHttpServlet) servlet);
    }
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
    String url = requestUrl(request);
    GettableHttpServlet servlet = getMap.get(url);
    if (servlet == null) {
      throw new ServletException("Unknown request URL: " + url);
    }
    logRequest(request, servlet.getClass().getName());
    servlet.doGet(request, response);
    logResponse(response, servlet.getClass().getName());
  }

  private void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String url = requestUrl(request);
    PostableHttpServlet servlet = postMap.get(url);
    if (servlet == null) {
      throw new ServletException("Unknown request URL: " + url);
    }
    logRequest(request, servlet.getClass().getName());
    servlet.doPost(request, response);
    logResponse(response, servlet.getClass().getName());
  }

  private String requestUrl(HttpServletRequest request) {
    String url = request.getRequestURL().toString();
    int q = url.indexOf("?");
    return (q < 0) ? url : url.substring(0, q);
  }

  private void logRequest(HttpServletRequest request, String tag) throws IOException {
    LOGGER.log(Level.INFO, "Request: " + servletRequestToString(request, tag));
  }

  private void logResponse(HttpServletResponse response, String tag) throws IOException {
    LOGGER.log(Level.INFO,
               "Response: " + servletResponseToString((MockHttpServletResponse) response, tag));
  }
}
