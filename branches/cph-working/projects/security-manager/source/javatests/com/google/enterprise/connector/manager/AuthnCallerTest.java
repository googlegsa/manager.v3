// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.manager;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * Tests for the {@link AuthnCaller} class.
 */
public class AuthnCallerTest extends TestCase {

  public void testNullAuthenticate() {
    AuthenticationManager authenticationManager = new AuthenticationManager() {
      public AuthenticationResponse authenticate(AuthenticationIdentity id) {
        AuthenticationResponse result = null;
        return result;
      }
    };
    runBasicTest(authenticationManager, "fred", "xyzzy", false);
  }

  public void testSimpleAuthenticate() {
    AuthenticationManager authenticationManager = new AuthenticationManager() {
      public AuthenticationResponse authenticate(AuthenticationIdentity id) {
        if (id.getUsername() == "fred" && id.getPassword() == "xyzzy") {
          return new AuthenticationResponse(true, null);
        }
        return new AuthenticationResponse(false, null);
      }
    };
    runBasicTest(authenticationManager, "fred", "xyzzy", true);
    runBasicTest(authenticationManager, "fred", "foo", false);
  }

  private void runBasicTest(AuthenticationManager authenticationManager, String username,
      String password, boolean expected) {
    AuthenticationIdentity identity = new UserPassIdentity(username, password);
    runIdentityTest(authenticationManager, expected, identity, null);
  }

  private void runIdentityTest(AuthenticationManager authenticationManager, boolean expected,
      AuthenticationIdentity identity, List<Cookie> cookies) {
    AuthnCaller authnCaller = new AuthnCaller(authenticationManager, identity, cookies);
    AuthenticationResponse response = authnCaller.authenticate();
    boolean result = ((response != null) && response.isValid());
    assertEquals(expected, result);
  }

  public void testAuthenticateWithContext() {
    AuthenticationManager authenticationManager = new AuthenticationManager() {
      public AuthenticationResponse authenticate(AuthenticationIdentity id) {
        id.setCookie("ilikecandy", "true");
        if (id.getUsername() == "fred" && id.getPassword() == "xyzzy") {
          return new AuthenticationResponse(true, null);
        }
        if (id.getCookie("joe_is_ok") != null && id.getUsername() == "joe") {
          return new AuthenticationResponse(true, null);
        }
        return new AuthenticationResponse(false, null);
      }
    };
    runBasicTest(authenticationManager, "fred", "xyzzy", true);
    runBasicTest(authenticationManager, "joe", "foo", false);
    AuthenticationIdentity identity;
    List<Cookie> cookies;

    // show that the authnmanager sees the cookies and sets one
    identity = new UserPassIdentity("joe", "foo");
    assertNull(identity.getCookie("ilikecandy"));
    cookies = new ArrayList<Cookie>();
    cookies.add(new Cookie("joe_is_ok", "xxx"));
    runIdentityTest(authenticationManager, true, identity, cookies);
    assertEquals("true", cookieValue("ilikecandy", cookies));

    // show that the manager setting a cookie has no effect if it's already set
    identity = new UserPassIdentity("joe", "foo");
    cookies = new ArrayList<Cookie>();
    cookies.add(new Cookie("ilikecandy", "false"));
    cookies.add(new Cookie("joe_is_ok", "xxx"));
    runIdentityTest(authenticationManager, true, identity, cookies);
    assertEquals("false", cookieValue("ilikecandy", cookies));
  }

  private static String cookieValue(String name, List<Cookie> cookies) {
    for (Cookie c: cookies) {
      if (c.getName().equals(name)) {
        return c.getValue();
      }
    }
    return null;
  }
}
