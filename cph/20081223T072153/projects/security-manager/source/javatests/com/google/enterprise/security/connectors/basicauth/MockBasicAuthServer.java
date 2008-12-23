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

package com.google.enterprise.security.connectors.basicauth;

import com.google.enterprise.common.Base64;
import com.google.enterprise.common.Base64DecoderException;
import com.google.enterprise.common.GettableHttpServlet;
import com.google.enterprise.common.PostableHttpServlet;
import com.google.enterprise.common.StringPair;
import com.google.enterprise.saml.common.SecurityManagerServlet;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class MockBasicAuthServer extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER =
    Logger.getLogger(MockBasicAuthServer.class.getName());

  private final String realm;
  protected final Map<String, String> passwordMap;

  protected MockBasicAuthServer(String realm) {
    this.realm = realm;
    passwordMap = new HashMap<String, String>();
  }

  public static class Server1 extends MockBasicAuthServer {
    private static final long serialVersionUID = 1L;
    public Server1() {
      super("Server1");
      passwordMap.put("joe", "plumber");
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (!goodAuthCredential(request)) {
      response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
      initErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    Writer w = initNormalResponse(response);
    w.write("<html><head><title>You've won!!!</title></head><body>\n");
    w.write("<p>You are the lucky winner of our content!!!</p>\n");
    w.write("</body></html>\n");
    w.close();
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    doGet(request, response);
  }

  public boolean goodAuthCredential(HttpServletRequest request) {
    List<StringPair> credentials = getAuthCredentials(request);
    for (StringPair credential: credentials) {
      LOGGER.info("BasicAuth credential: " + credential.getName() +
                  ":" + credential.getValue());
      if (credential.getValue().equals(passwordMap.get(credential.getName()))) {
        return true;
      }
    }
    return false;
  }

  private static List<StringPair> getAuthCredentials(HttpServletRequest request) {
    @SuppressWarnings("unchecked")
        Enumeration<String> headers = request.getHeaders("Authorize");
    List<StringPair> result = new ArrayList<StringPair>();
    while (headers.hasMoreElements()) {
      String value = headers.nextElement();
      List<StringPair> credentials = parseAuthorizationHeader(value);
      if (credentials != null) {
        result.addAll(credentials);
      }
    }
    return result;
  }

  private static List<StringPair> parseAuthorizationHeader(String header) {
    String h = header.trim();
    if (!h.regionMatches(true, 0, "BASIC ", 0, 6)) {
      return null;
    }
    LOGGER.info("BasicAuth header: " + h);
    List<StringPair> credentials = new ArrayList<StringPair>();
    for (String param: h.substring(6).trim().split("[, \t]*,[, \t]*")) {
      try {
        String decoded = new String(Base64.decode(param));
        int colon = decoded.indexOf(':');
        if (colon < 0) {
          return null;
        }
        credentials.add(new StringPair(decoded.substring(0, colon),
                                       decoded.substring(colon + 1)));
      } catch (Base64DecoderException e) {
        return null;
      }
    }
    return credentials;
  }
}
