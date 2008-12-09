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

package com.google.enterprise.security.connectors.simplecookie;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.saml.server.UserIdentity;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

/**
 * Tests for creating, modifying and deleting a RegexCookieIdentityConnector
 */
public class CreateModifyDeleteTest extends TestCase {

  /**
   * We do Context.refresh() before and after so as not to interfere with other
   * tests that might use a Context
   * 
   * @throws Exception
   */
  public final void testBasicFunctionality() throws Exception {
    Context.refresh();
    Context.getInstance().setStandaloneContext(Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    Context.getInstance().setFeeding(false);
    Context.getInstance().start();

    ConnectorManager connectorManager = (ConnectorManager) Context.getInstance().getManager();

    String connectorName = "CookieConnector1";
    String connectorTypeName = "CookieConnector";
    String language = "en";

    boolean connectorExists = testConnectorExists(connectorManager, connectorName);

    Map<String, String> configData;
    UserIdentity id = UserIdentity.compatNew(null, null, null);
    
    configData =
        ImmutableMap.of("CookieName", "in", "IdCookieName", "out", "Regex", "username=(.*)");
    connectorManager.setConnectorConfig(connectorName, connectorTypeName, configData, language,
        connectorExists);
    
    connectorExists = testConnectorExists(connectorManager, connectorName);
    assertTrue(connectorExists);

    id = UserIdentity.compatNew(null, null, null);
    id.setCookie("in", "username=fred");
    connectorManager.authenticate(connectorName, id, null);
    assertEquals("fred", id.getCookie("out"));

    configData =
        ImmutableMap.of("CookieName", "abc", "IdCookieName", "def", "Regex", "user=(.*)");
    connectorManager.setConnectorConfig(connectorName, connectorTypeName, configData, language,
        connectorExists);

    id = UserIdentity.compatNew(null, null, null);
    id.setCookie("abc", "user=joe");
    connectorManager.authenticate(connectorName, id, null);
    assertEquals("joe", id.getCookie("def"));
    
    connectorManager.removeConnector(connectorName);
    
    connectorExists = testConnectorExists(connectorManager, connectorName);
    assertFalse(connectorExists);

    Context.getInstance().shutdown(true);
    Context.refresh();
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
}
