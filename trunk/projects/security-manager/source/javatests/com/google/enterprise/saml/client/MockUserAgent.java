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

package com.google.enterprise.saml.client;

import com.google.enterprise.common.HttpTransport;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.io.IOException;

import javax.servlet.ServletException;

import static com.google.enterprise.common.ServletTestUtil.makeMockHttpGet;

/**
 * Implements a simple user agent that does HTTP servlet messaging over a given transport.
 */
public final class MockUserAgent {
  private final HttpTransport transport;
  private final MockHttpSession session;
  private String referrer;

  public MockUserAgent(HttpTransport transport) {
    this.transport = transport;
    session = new MockHttpSession();
    referrer = null;
  }

  public MockHttpServletResponse exchange(MockHttpServletRequest request)
      throws ServletException, IOException {
    request.setSession(session);
    if (referrer != null) {
      request.addHeader("Referer", referrer);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();
    transport.exchange(request, response);
    referrer = request.getRequestURL().toString();
    while (isRedirect(response)) {
      request = makeMockHttpGet(null, (String) response.getHeader("Location"));
      request.setSession(session);
      request.addHeader("Referer", referrer);
      response = new MockHttpServletResponse();
      transport.exchange(request, response);
      referrer = request.getRequestURL().toString();
    }
    return response;
  }

  private boolean isRedirect(MockHttpServletResponse response) {
    int status = response.getStatus();
    return (status == 303) || (status == 302);
  }
}
