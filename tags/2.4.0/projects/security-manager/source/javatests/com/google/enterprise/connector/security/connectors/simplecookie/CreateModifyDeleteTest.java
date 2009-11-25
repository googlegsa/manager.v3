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

package com.google.enterprise.connector.security.connectors.simplecookie;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.common.SecurityManagerTestCase;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.connector.security.identity.DomainCredentials;

import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import javax.servlet.http.Cookie;

/**
 * Tests for creating, modifying and deleting a RegexCookieIdentityConnector
 */
public class CreateModifyDeleteTest extends SecurityManagerTestCase {

  public final void testBasicFunctionality() throws Exception {
    ConnectorManager connectorManager =
        ConnectorManager.class.cast(Context.getInstance().getManager());

    String connectorName = "CookieConnector1";
    String connectorTypeName = "regexCookieIdentityConnector";
    String language = "en";

    boolean connectorExists = testConnectorExists(connectorManager, connectorName);

    Map<String, String> configData;
    SecAuthnIdentity id;
    Cookie cookie;

    configData =
        ImmutableMap.of("CookieName", "in", "IdCookieName", "out", "Regex", "username=(.*)");
    connectorManager.setConnectorConfig(connectorName, connectorTypeName, configData, language,
        connectorExists);

    connectorExists = testConnectorExists(connectorManager, connectorName);
    assertTrue(connectorExists);

    id = newIdentity();
    id.addCookie(new Cookie("in", "username=fred"));
    connectorManager.authenticate(connectorName, id);
    cookie = id.getCookieNamed("out");
    assertNotNull(cookie);
    assertEquals("fred", cookie.getValue());

    configData =
        ImmutableMap.of("CookieName", "abc", "IdCookieName", "def", "Regex", "user=(.*)");
    connectorManager.setConnectorConfig(connectorName, connectorTypeName, configData, language,
        connectorExists);

    id = newIdentity();
    id.addCookie(new Cookie("abc", "user=joe"));
    connectorManager.authenticate(connectorName, id);
    cookie = id.getCookieNamed("def");
    assertNotNull(cookie);
    assertEquals("joe", cookie.getValue());

    connectorManager.removeConnector(connectorName);

    connectorExists = testConnectorExists(connectorManager, connectorName);
    assertFalse(connectorExists);
  }

  private boolean testConnectorExists(ConnectorManager connectorManager, String connectorName) {
    boolean connectorExists;
    try {
      connectorManager.getConnectorConfig(connectorName);
      connectorExists = true;
    } catch (ConnectorNotFoundException e) {
      connectorExists = false;
    }
    return connectorExists;
  }

  private SecAuthnIdentity newIdentity() {
    return DomainCredentials.dummy(new MockHttpSession());
  }
}
