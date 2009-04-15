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

import com.google.enterprise.common.SecurityManagerTestCase;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.DomainCredentials;

import javax.servlet.http.Cookie;

/**
 * Tests for the {@link AuthnCaller} class.
 */
public class AuthnCallerTest extends SecurityManagerTestCase {

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
      public AuthenticationResponse authenticate(AuthenticationIdentity raw) {
        SecAuthnIdentity id = SecAuthnIdentity.class.cast(raw);
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
    SecAuthnIdentity identity = newIdentity(username, password);
    runIdentityTest(authenticationManager, expected, identity, null);
  }

  private void runIdentityTest(AuthenticationManager authenticationManager, boolean expected,
      SecAuthnIdentity identity, SecAuthnContext context) {
    AuthnCaller authnCaller = new AuthnCaller(authenticationManager, identity, context);
    AuthenticationResponse response = authnCaller.authenticate();
    boolean result = ((response != null) && response.isValid());
    assertEquals(expected, result);
  }

  public void testAuthenticateWithContext() {
    AuthenticationManager authenticationManager = new AuthenticationManager() {
      public AuthenticationResponse authenticate(AuthenticationIdentity raw) {
        SecAuthnIdentity id = SecAuthnIdentity.class.cast(raw);
        id.addCookie(new Cookie("ilikecandy", "true"));
        if (id.getUsername() == "fred" && id.getPassword() == "xyzzy") {
          return new AuthenticationResponse(true, null);
        }
        if (id.getCookieNamed("joe_is_ok") != null && id.getUsername() == "joe") {
          return new AuthenticationResponse(true, null);
        }
        return new AuthenticationResponse(false, null);
      }
    };
    runBasicTest(authenticationManager, "fred", "xyzzy", true);
    runBasicTest(authenticationManager, "joe", "foo", false);
    SecAuthnIdentity identity;
    SecAuthnContext context;
    Cookie cookie;
    // show that the authnmanager sees the cookies and sets one
    identity = newIdentity("joe", "foo");
    context = new SecAuthnContext();
    assertNull(identity.getCookieNamed("ilikecandy"));
    context.addCookie(new Cookie("joe_is_ok", "xxx"));
    runIdentityTest(authenticationManager, true, identity, context);
    cookie = context.getCookieNamed("ilikecandy");
    assertNotNull(cookie);
    assertEquals("true", cookie.getValue());
    // show that the authenticationManager setting a cookie has no effect if it's already set
    identity = newIdentity("joe", "foo");
    context = new SecAuthnContext();
    context.addCookie(new Cookie("ilikecandy", "false"));
    context.addCookie(new Cookie("joe_is_ok", "xxx"));
    runIdentityTest(authenticationManager, true, identity, context);
    cookie = context.getCookieNamed("ilikecandy");
    assertNotNull(cookie);
    assertEquals("false", cookie.getValue());
  }

  private SecAuthnIdentity newIdentity(String username, String password) {
    CredentialsGroup group = new CredentialsGroup(null);
    group.setUsername(username);
    group.setPassword(password);
    return new DomainCredentials(null, group);
  }
}
