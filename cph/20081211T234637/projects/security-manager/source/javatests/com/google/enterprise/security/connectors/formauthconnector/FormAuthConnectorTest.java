// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.security.connectors.formauthconnector;

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.server.AuthSite;
import com.google.enterprise.saml.server.UserIdentity;
import com.google.enterprise.security.connectors.formauth.FormAuthConnector;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;

public class FormAuthConnectorTest extends TestCase {

  private final List<AuthSite> sites;

  public FormAuthConnectorTest(String name) throws IOException {
    super(name);
    Context.getInstance().setStandaloneContext(
        "source/webdocs/test/applicationContext.xml",
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    sites = AuthSite.getSites("testdata/mocktestdata/AuthSites.conf");
  }

  public void test1() {
    UserIdentity id = new UserIdentity("joe", "plumber", sites.get(0));
    FormAuthConnector connector = new FormAuthConnector("foo");
    AuthenticationResponse response = connector.authenticate(id);
    assertNotNull("Null response from authenticate()", response);
    assertTrue("Invalid response from authenticate()", response.isValid());
  }

}
