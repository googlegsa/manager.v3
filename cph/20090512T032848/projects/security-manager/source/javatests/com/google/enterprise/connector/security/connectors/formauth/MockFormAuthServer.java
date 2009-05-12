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

package com.google.enterprise.connector.security.connectors.formauth;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.common.GettableHttpServlet;
import com.google.enterprise.connector.common.PostableHttpServlet;
import com.google.enterprise.connector.saml.common.SecurityManagerServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class MockFormAuthServer extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {
  private static final long serialVersionUID = 1L;

  protected final String usernameKey;
  protected final String passwordKey;
  protected final Map<String, String> inputs;
  protected final Map<String, String> passwordMap;
  protected final Map<String, Cookie> cookieMap;

  MockFormAuthServer(String usernameKey, String passwordKey) {
    this.usernameKey = usernameKey;
    this.passwordKey = passwordKey;
    inputs = new ImmutableMap.Builder<String, String>()
      .put(usernameKey, "text")
      .put(passwordKey, "password")
      .build();
    passwordMap = new HashMap<String, String>();
    cookieMap = new HashMap<String, Cookie>();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    PrintWriter writer = initNormalResponse(response);
    writer.write(
        "<http><head><title>Just another form from LA</title><body>\n"
        + "<h1>Please login</h1>\n"
        + "<form method=\"post\" action=\"" + getAction(request) + "\">\n");
    for (Map.Entry<String, String> entry: inputs.entrySet()) {
      writer.write("<input name=\"" + entry.getKey() + "\""
                   + " type=\"" + entry.getValue() + "\"><br>\n");
    }
    writer.write("</form></body></http>\n");
    writer.close();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String username = request.getParameter(usernameKey);
    String password = request.getParameter(passwordKey);
    if (username == null || username.length() == 0 ||
        password == null || password.length() == 0 ||
        !password.equals(passwordMap.get(username))) {
      initErrorResponse(response, HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    Cookie cookie = cookieMap.get(username);
    if (cookie != null) {
      response.addCookie(cookie);
    }
    PrintWriter writer = initNormalResponse(response);
    writer.write(
        "<http><head><title>Just another site from LA</title><body>\n"
        + "<h1>Just another site from LA</h1>\n"
        + "<p>Welcome to the machine!</p>\n"
        + "</body></http>\n");
    writer.close();
  }

  private static String getAction(HttpServletRequest request) {
    String url = request.getRequestURL().toString();
    int q = url.indexOf("?");
    return (q < 0) ? url : url.substring(0, q);
  }
}
