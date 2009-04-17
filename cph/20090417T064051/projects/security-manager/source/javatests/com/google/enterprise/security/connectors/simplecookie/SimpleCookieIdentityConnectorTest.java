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

package com.google.enterprise.security.connectors.simplecookie;

import com.google.enterprise.common.SecurityManagerTestCase;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.security.identity.DomainCredentials;

import javax.servlet.http.Cookie;

/**
 * Tests for the {@link SimpleCookieIdentityConnector} class.
 */
public class SimpleCookieIdentityConnectorTest extends SecurityManagerTestCase {

  public void testAuthenticate() {
    String cookieName = "arglebargle";
    String idCookieName = "xyzzy";
    String pattern = "username=(.*)";
    CookieIdentityExtractor c = new RegexCookieIdentityExtractor(pattern);
    SimpleCookieIdentityConnector s =
        new SimpleCookieIdentityConnector(cookieName, idCookieName, c);
    runOneAuthenticationTest(cookieName, idCookieName, s, "username=fred", "fred");
    runOneAuthenticationTest(cookieName, idCookieName, s, "randomness", null);
  }

  private void runOneAuthenticationTest(String cookieName, String idCookieName,
      SimpleCookieIdentityConnector s, String cookieValue, String expectedIdentity) {
    SecAuthnIdentity id = DomainCredentials.dummy();
    id.addCookie(new Cookie(cookieName, cookieValue));
    AuthenticationResponse r = s.authenticate(id);
    if (expectedIdentity != null) {
    assertTrue(r.isValid());
    Cookie c = id.getCookieNamed(idCookieName);
    assertNotNull(c);
    assertEquals(expectedIdentity, c.getValue());
    } else {
      assertFalse(r.isValid());
    }
  }
}
