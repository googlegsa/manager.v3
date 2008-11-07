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

import java.util.HashMap;
import java.util.Map;

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
      AuthenticationIdentity identity, Map<String,String> context) {
    AuthnCaller authnCaller = new AuthnCaller(authenticationManager, identity, context);
    boolean result = authnCaller.authenticate();
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
    Map<String,String> context;
    // show that the authnmanager sees the cookies and sets one
    identity = new UserPassIdentity("joe", "foo");
    context = new HashMap<String,String>();
    assertNull(identity.getCookie("ilikecandy"));
    context.put("joe_is_ok", "xxx");
    runIdentityTest(authenticationManager, true, identity, context);
    assertEquals("true",context.get("ilikecandy"));
    // show that the manager setting a cookie has no effect if it's already set
    identity = new UserPassIdentity("joe", "foo");
    context = new HashMap<String,String>();
    context.put("ilikecandy", "false");
    context.put("joe_is_ok", "xxx");
    runIdentityTest(authenticationManager, true, identity, context);
    assertEquals("false",context.get("ilikecandy"));
  }
}
