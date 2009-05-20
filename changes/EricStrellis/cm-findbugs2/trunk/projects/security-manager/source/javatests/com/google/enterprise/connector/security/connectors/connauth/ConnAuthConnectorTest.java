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

package com.google.enterprise.connector.security.connectors.connauth;

import com.google.enterprise.connector.common.MockHttpClient;
import com.google.enterprise.connector.common.MockHttpTransport;
import com.google.enterprise.connector.common.SecurityManagerTestCase;
import com.google.enterprise.connector.common.SecurityManagerUtil;
import com.google.enterprise.connector.security.identity.AuthnDomainGroup;
import com.google.enterprise.connector.security.identity.CredentialsGroup;
import com.google.enterprise.connector.security.identity.CsvConfig;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;

import org.springframework.mock.web.MockHttpSession;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

public class ConnAuthConnectorTest extends SecurityManagerTestCase {

  private final List<CredentialsGroup> cgs;

  public ConnAuthConnectorTest(String name) throws IOException, ServletException {
    super(name);
    List<AuthnDomainGroup> adgs = CsvConfig.readConfigFile("AuthSites.conf");
    cgs = CredentialsGroup.newGroups(adgs, new MockHttpSession());
    MockHttpTransport transport = new MockHttpTransport();
    transport.registerServlet(cgs.get(0).getElements().get(0).getSampleUrl(),
                              new MockCMAuthServer());
    SecurityManagerUtil.setHttpClient(new MockHttpClient(transport));
  }

  public void testGood() throws RepositoryException {
    assertTrue("Invalid response", tryCreds("joe", "plumber"));
  }

  public void testBadPassword() throws RepositoryException {
    assertFalse("Valid response", tryCreds("joe", "biden"));
  }

  public void testBadUsername() throws RepositoryException {
    assertFalse("Valid response", tryCreds("jim", "plumber"));
  }

  private boolean tryCreds(String username, String password) throws RepositoryException {
    cgs.get(0).setUsername(username);
    cgs.get(0).setPassword(password);
    ConnAuthConnector connector = new ConnAuthConnector("foo");
    AuthenticationResponse response = connector.authenticate(cgs.get(0).getElements().get(0));
    assertNotNull("Null response from authenticate()", response);
    return response.isValid();
  }

}
