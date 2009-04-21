// Copyright (C) 2008 Google Inc.
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

import org.opensaml.util.URLBuilder;
import org.opensaml.xml.util.Pair;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Useful utilities for SAML testing.
 */
public final class ServletTestUtil {

  public static MockHttpServletRequest makeMockHttpGet(String clientUrl, String serverUrl) {
    return makeMockHttpRequest("GET", clientUrl, serverUrl);
  }

  public static MockHttpServletRequest makeMockHttpPost(String clientUrl, String serverUrl) {
    return makeMockHttpRequest("POST", clientUrl, serverUrl);
  }

  private static MockHttpServletRequest makeMockHttpRequest(String method, String client,
      String server) {
    URLBuilder serverUrl = new URLBuilder(server);
    // TODO(cph): figure out how to get servlet context from serverUrl.
    String uri = serverUrl.getPath();
    String query = serverUrl.buildQueryString();
    if (query != null) {
      uri += "?" + query;
    }
    MockHttpServletRequest request = new MockHttpServletRequest(null, method, uri);
    request.setScheme(serverUrl.getScheme());
    request.setServerName(serverUrl.getHost());
    request.setServerPort(serverUrl.getPort());
    for (Pair<String, String> binding: serverUrl.getQueryParams()) {
      request.addParameter(binding.getFirst(), binding.getSecond());
    }
    if (client != null) {
      URLBuilder clientUrl = new URLBuilder(client);
      request.setRemoteHost(clientUrl.getHost());
      request.setRemotePort(clientUrl.getPort());
    }
    {
      String host = serverUrl.getHost();
      int port = serverUrl.getPort();
      request.addHeader("Host", (port < 0) ? host : String.format("%s:%d", host, port));
    }
    request.addHeader("Date", ServletBase.httpDateString());
    request.addHeader("Accept", "text/html, text/xhtml;q=0.9, text/plain;q=0.5, text/*;q=0.1");
    request.addHeader("Accept-Charset", "us-ascii, iso-8859-1, utf-8");
    request.addHeader("Accept-Encoding", "identity");
    request.addHeader("Accept-Language", "en-us, en;q=0.9");
    return request;
  }

  public static String servletRequestToString(HttpServletRequest request, String tag)
      throws IOException {
    StringWriter out = new StringWriter();
    out.write(tag);
    out.write(":\n");
    writeServletRequest(request, out);
    String result = out.toString();
    out.close();
    return result;
  }

  public static void writeServletRequest(HttpServletRequest request, Writer out)
      throws IOException {
    writeRequestLine(request, out);
    writeRequestHeaders(request, out);
    copyText(request.getReader(), out);
  }

  private static void writeRequestLine(HttpServletRequest request, Writer out) throws IOException {
    out.write(request.getMethod());
    out.write(" ");
    out.write(request.getRequestURI());
    {
      String qs = request.getQueryString();
      if (qs != null) {
        out.write(qs);
      }
    }
    out.write(" ");
    out.write("HTTP/1.1");
    out.write("\n");
  }

  @SuppressWarnings("unchecked")
  private static void writeRequestHeaders(HttpServletRequest request, Writer out)
      throws IOException {
    for (Enumeration<String> names = request.getHeaderNames(); names.hasMoreElements();) {
      String name = names.nextElement();
      for (Enumeration<String> values = request.getHeaders(name); values.hasMoreElements();) {
        out.write(name);
        out.write(": ");
        out.write(values.nextElement());
        out.write("\n");
      }
    }
    out.write("\n");
  }

  /**
   * Finalize a mock servlet request prior to using it.
   *
   * Should be called immediately before the servlet is called.  Fills in some fields that
   * the mock isn't already taking care of; this wouldn't be needed if the mock was a
   * little more complete.
   *
   * @param request A servlet request that is (potentially) a mock.
   */
  public static void finalizeRequest(HttpServletRequest request) {
    if (request instanceof MockHttpServletRequest) {
      MockHttpServletRequest mr = (MockHttpServletRequest) request;
      String value = ServletBase.cookieHeaderValue(Arrays.asList(mr.getCookies()));
      if (value != null) {
        mr.addHeader("Cookie", value);
      }
    }
  }

  public static String servletResponseToString(MockHttpServletResponse response, String tag)
      throws IOException {
    StringWriter out = new StringWriter();
    out.write(tag);
    out.write(":\n");
    writeServletResponse(response, out);
    String result = out.toString();
    out.close();
    return result;
  }

  public static void writeServletResponse(MockHttpServletResponse response, Writer out)
      throws IOException {
    {
      String url = response.getRedirectedUrl();
      if (url != null) {
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", url);
      }
    }
    writeResponseLine(response, out);
    writeResponseHeaders(response, out);
    copyText(new InputStreamReader(new ByteArrayInputStream(response.getContentAsByteArray())),
             out);
  }

  private static void writeResponseLine(MockHttpServletResponse response, Writer out)
      throws IOException {
    out.write("HTTP/1.1");
    out.write(" ");
    out.write(String.format("%03d", response.getStatus()));
    out.write(" ");
    out.write("insert reason here");
    out.write("\n");
  }

  @SuppressWarnings("unchecked")
  private static void writeResponseHeaders(MockHttpServletResponse response, Writer out)
      throws IOException {
    for (String name : (Set<String>) response.getHeaderNames()) {
      for (String value : (List<String>) response.getHeaders(name)) {
        out.write(name);
        out.write(": ");
        out.write(value);
        out.write("\n");
      }
    }
    out.write("\n");
  }

  private static void copyText(Reader in, Writer out) throws IOException {
    if (in == null) {
      return;
    }
    char[] buffer = new char[0x1000];
    while (true) {
      int nRead = in.read(buffer);
      if (nRead < 1) {
        break;
      }
      out.write(buffer, 0, nRead);
    }
    in.close();
  }

  /**
   * Generate POST body and headers for a mock servlet request.
   *
   * Should be called immediately before the servlet is called.  Converts the mock's
   * parameters into the appropriate form for an HTTP message.  Wouldn't be needed if the
   * mock was more complete.
   */
  public static void generatePostContent(MockHttpServletRequest request) throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    Writer out = new OutputStreamWriter(bs);
    writePostParams(request, out);
    byte[] content = bs.toByteArray();
    out.close();
    request.setContent(content);
    request.setContentType("application/x-www-form-urlencoded");
    request.setCharacterEncoding("UTF-8");
    request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    request.addHeader("Content-Length", (new Integer(content.length)).toString());
  }

  private static void writePostParams(HttpServletRequest request, Writer out) throws IOException {
    @SuppressWarnings("unchecked") Enumeration<String> keys = request.getParameterNames();
    boolean atStart = true;
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      String[] values = request.getParameterValues(key);
      for (int i = 0; i < values.length; i += 1) {
        if (atStart) {
          atStart = false;
        } else {
          out.write("&");
        }
        out.write(key);
        out.write("=");
        out.write(values[i]);
      }
    }
    out.flush();
  }

  /**
   * Finalize a mock servlet response prior to using it.
   *
   * Should be called immediately after the servlet has returned.  Fills in some fields
   * that the mock isn't already taking care of; this wouldn't be needed if the mock was a
   * little more complete.
   *
   * @param response A servlet response that is (potentially) a mock.
   */
  public static void finalizeResponse(HttpServletResponse response) {
    if (response instanceof MockHttpServletResponse) {
      MockHttpServletResponse mr = (MockHttpServletResponse) response;
      int length = mr.getContentAsByteArray().length;
      mr.setContentLength(length);
      response.addHeader("Content-Length", String.valueOf(length));
      String value = ServletBase.setCookieHeaderValue(Arrays.asList(mr.getCookies()));
      if (value != null) {
        response.addHeader("Set-Cookie", value);
      }
    }
  }
}
