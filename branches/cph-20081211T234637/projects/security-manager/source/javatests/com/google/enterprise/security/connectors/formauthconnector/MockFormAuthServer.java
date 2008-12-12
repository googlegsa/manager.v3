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

package com.google.enterprise.security.connectors.formauthconnector;

import com.google.enterprise.saml.common.GettableHttpServlet;
import com.google.enterprise.saml.common.PostableHttpServlet;
import com.google.enterprise.saml.common.SecurityManagerServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockFormAuthServer extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {

  private static final long serialVersionUID = 1L;

  private final String usernameKey;
  private final String passwordKey;
  private final String submitUrl;
  private final Cookie cookie;
  private final List<Map.Entry<String, String>> inputs;
  private final Map<String, String> passwordMap;

  protected MockFormAuthServer(
      String usernameKey, String passwordKey, String submitUrl, Cookie cookie,
      List<Map.Entry<String, String>> inputs, Map<String, String> passwordMap) {
    this.usernameKey = usernameKey;
    this.passwordKey = passwordKey;
    this.submitUrl = submitUrl;
    this.cookie = cookie;
    this.inputs = inputs;
    this.passwordMap = passwordMap;
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    PrintWriter writer = initNormalResponse(response);
    writer.write(
        "<http><head><title>Just another form from LA</title><body>\n"
        + "<h1>Please login</h1>\n"
        + "<form method=\"post\" action=\"" + submitUrl + "\">\n");
    for (Map.Entry<String, String> entry: inputs) {
      writer.write("<input name=\"" + entry.getKey() + "\""
                   + " type=\"" + entry.getValue() + "\"><br>\n");
    }
    writer.write("</form></body></http>\n");
  }

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
    PrintWriter writer = initNormalResponse(response);
    if (cookie != null) {
      response.addCookie(cookie);
    }
    writer.write(
        "<http><head><title>Just another site from LA</title><body>\n"
        + "<h1>Just another site from LA</h1>\n"
        + "<p>Welcome to the machine!</p>\n"
        + "</body></http>\n");
  }
}
