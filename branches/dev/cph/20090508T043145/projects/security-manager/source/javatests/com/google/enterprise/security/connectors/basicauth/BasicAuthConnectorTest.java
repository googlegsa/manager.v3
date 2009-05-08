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

import com.google.enterprise.common.MockHttpClient;
import com.google.enterprise.common.MockHttpTransport;
import com.google.enterprise.common.SecurityManagerTestCase;
import com.google.enterprise.common.SecurityManagerUtil;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.security.identity.AuthnDomain;
import com.google.enterprise.security.identity.AuthnDomainGroup;
import com.google.enterprise.security.identity.AuthnMechanism;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.DomainCredentials;

import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;

/*
 * Tests for the {@link BasicAuthConnector} class.
 */
public class BasicAuthConnectorTest extends SecurityManagerTestCase {

  private List<CredentialsGroup> cgs;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    List<AuthnDomainGroup> adgs = new ArrayList<AuthnDomainGroup>();
    adgs.add(new AuthnDomainGroup("ADG1"));
    new AuthnDomain(
        "BasicDomain", AuthnMechanism.BASIC_AUTH,
        "http://localhost:8973/basic/", adgs.get(0));
    cgs = CredentialsGroup.newGroups(adgs, new MockHttpSession());
    MockHttpTransport transport = new MockHttpTransport();
    transport.registerServlet(cgs.get(0).getElements().get(0).getSampleUrl(),
                              new MockBasicAuthServer.Server1());
    SecurityManagerUtil.setHttpClient(new MockHttpClient(transport));
  }

  public void testHttpAuthenticate() throws RepositoryException {
    assertTrue(tryCredentials("joe", "plumber").isValid());
    assertFalse(tryCredentials("joe", "biden").isValid());
  }

  private AuthenticationResponse tryCredentials(String username, String password)
      throws RepositoryException {
    cgs.get(0).setUsername(username);
    cgs.get(0).setPassword(password);
    DomainCredentials dc = cgs.get(0).getElements().get(0);
    BasicAuthConnector conn = new BasicAuthConnector(dc.getSampleUrl());
    return conn.authenticate(dc);
  }
}
