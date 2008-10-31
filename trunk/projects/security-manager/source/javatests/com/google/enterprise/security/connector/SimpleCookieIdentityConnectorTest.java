// Copyright 2008 Google Inc. All Rights Reserved.
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

package com.google.enterprise.security.connector;

import com.google.enterprise.connector.manager.UserPassIdentity;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationResponse;

import junit.framework.TestCase;

/**
 * Tests for the {@link SimpleCookieIdentityConnector} class.
 */
public class SimpleCookieIdentityConnectorTest extends TestCase {

  public void testAuthenticate() {
    String cookieName = "arglebargle";
    String idCookieName = "xyzzy";
    String pattern = "username=(.*)";
    CookieIdentityExtractor c = new RegexCookieIdentityExtractor(pattern);
    SimpleCookieIdentityConnector s = new SimpleCookieIdentityConnector(cookieName, idCookieName, c);
    runOneAuthenticationTest(cookieName, idCookieName, s, "username=fred", "fred");
    runOneAuthenticationTest(cookieName, idCookieName, s, "randomness", null);
  }

  private void runOneAuthenticationTest(String cookieName, String idCookieName,
      SimpleCookieIdentityConnector s, String cookieValue, String expectedIdentity) {
    AuthenticationIdentity id = new UserPassIdentity("", "");
    id.setCookie(cookieName, cookieValue);
    AuthenticationResponse r = s.authenticate(id);
    if (expectedIdentity != null) {
    assertTrue(r.isValid());
    assertEquals(expectedIdentity, id.getCookie(idCookieName));
    } else {
      assertFalse(r.isValid());
    }
  }
}
