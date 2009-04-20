// Copyright (C) 2008, 2009 Google Inc.
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

package com.google.enterprise.security.connectors.connauth;

import com.google.enterprise.common.HttpClientInterface;
import com.google.enterprise.common.MockHttpClient;
import com.google.enterprise.common.MockHttpTransport;
import com.google.enterprise.common.SecurityManagerTestCase;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.CredentialsGroupConfig;
import com.google.enterprise.security.identity.CsvConfig;
import com.google.enterprise.security.identity.IdentityElement;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

public class ConnAuthConnectorTest extends SecurityManagerTestCase {

  private final List<CredentialsGroup> cgs;
  private final HttpClientInterface httpClient;

  public ConnAuthConnectorTest(String name) throws IOException, ServletException {
    super(name);
    List<CredentialsGroupConfig> cgcs = CsvConfig.readConfigFile("AuthSites.conf");
    cgs = CredentialsGroup.newGroups(cgcs);
    MockHttpTransport transport = new MockHttpTransport();
    transport.registerServlet(cgs.get(0).getElements().get(0).getSampleUrl(),
                              new MockCMAuthServer());
    httpClient = new MockHttpClient(transport);
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
    IdentityElement id = cgs.get(0).getElements().get(0);
    id.setUsername(username);
    id.setPassword(password);
    AuthenticationResponse response = (new ConnAuthConnector(httpClient, "foo")).authenticate(id);
    assertNotNull("Null response from authenticate()", response);
    return response.isValid();
  }
}
