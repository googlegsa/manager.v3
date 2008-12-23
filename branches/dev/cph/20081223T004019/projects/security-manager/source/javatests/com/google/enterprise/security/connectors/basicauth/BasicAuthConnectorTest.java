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

import com.google.enterprise.common.HttpClientInterface;
import com.google.enterprise.common.MockHttpClient;
import com.google.enterprise.common.MockHttpTransport;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;
import com.google.enterprise.saml.server.AuthSite;
import com.google.enterprise.saml.server.UserIdentity;

import junit.framework.TestCase;

import javax.servlet.ServletException;

/**
 * Tests for {@link BasicAuthConnector}.
 */
public class BasicAuthConnectorTest extends TestCase {

  private final AuthSite site;
  private final HttpClientInterface httpClient;

  public BasicAuthConnectorTest(String name) throws ServletException {
    super (name);
    site = new AuthSite("http://localhost:8973", "/basic", AuthNMechanism.BASIC_AUTH, null);
    MockHttpTransport transport = new MockHttpTransport();
    transport.registerServlet(site.getLoginUri(), new MockBasicAuthServer.Server1());
    httpClient = new MockHttpClient(transport);
  }
  
  public void testBasicAuth() throws RepositoryException {
    assertTrue(tryCredentials("joe", "plumber").isValid());
    assertFalse(tryCredentials("joe", "biden").isValid());
  }

  private AuthenticationResponse tryCredentials(String username, String password)
      throws RepositoryException {
    BasicAuthConnector conn = new BasicAuthConnector(httpClient, site.getLoginUri());
    AuthenticationIdentity id = new UserIdentity(username, password, site);
    return conn.authenticate(id);
  }
}
