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

package com.google.enterprise.security.connectors.formauth;

import com.google.enterprise.common.HttpClientInterface;
import com.google.enterprise.common.MockHttpClient;
import com.google.enterprise.common.MockHttpTransport;
import com.google.enterprise.common.SecurityManagerTestCase;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.security.identity.AuthnDomainGroup;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.CsvConfig;
import com.google.enterprise.security.identity.DomainCredentials;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

public class FormAuthConnectorTest extends SecurityManagerTestCase {

  private final HttpClientInterface httpClient;
  private final CredentialsGroup cg;

  public FormAuthConnectorTest(String name) throws IOException, ServletException {
    super(name);
    List<AuthnDomainGroup> adgs = CsvConfig.readConfigFile("AuthSites.conf");
    List<CredentialsGroup> cgs = CredentialsGroup.newGroups(adgs);
    cg = cgs.get(0);
    MockHttpTransport transport = new MockHttpTransport();
    transport.registerServlet(cg.getElements().get(0).getSampleUrl(),
                              new MockFormAuthServer1());
    httpClient = new MockHttpClient(transport);
  }

  public void testGood() {
    assertTrue(tryCreds("joe", "plumber"));
  }

  public void testBadPassword() {
    assertFalse(tryCreds("joe", "biden"));
  }

  public void testBadUsername() {
    assertFalse(tryCreds("jim", "plumber"));
  }

  public boolean tryCreds(String username, String password) {
    cg.setUsername(username);
    cg.setPassword(password);
    DomainCredentials dCred = cg.getElements().get(0);
    dCred.clearCookies();
    AuthenticationResponse response =
        (new FormAuthConnector(httpClient, "foo")).authenticate(dCred);
    assertNotNull("Null response from authenticate()", response);
    return response.isValid() && (dCred.getCookies().size() > 0);
  }
}
