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

package com.google.enterprise.security.connectors.basicauthconnector;

import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.server.UserIdentity;
import com.google.enterprise.security.connectors.basicauth.BasicAuthConnector;
import com.google.enterprise.sessionmanager.AuthnDomain;
import com.google.enterprise.sessionmanager.AuthnMechanism;

import junit.framework.TestCase;

/* 
 * Tests for the {@link BasicAuthConnector} class.
 * Maybe should use a mock Idp...
 */
public class BasicAuthConnectorTest extends TestCase {
  
  public void testAuthenticate() {
    BasicAuthConnector conn;
    AuthnDomain domain;
    UserIdentity id;

    // HTTP Basic Auth
    domain = AuthnDomain.compatAuthSite("http://leiz.mtv.corp.google.com",
                                        "/basic/", AuthnMechanism.BASIC_AUTH, null);
    id = UserIdentity.compatNew("basic", "test", domain);
    conn = new BasicAuthConnector(domain.getLoginUrl());
    AuthenticationResponse result = conn.authenticate(id);
    assertTrue(result.isValid());
    
    // HTTPS Basic Auth
    domain = AuthnDomain.compatAuthSite("https://entconcx100-testbed.corp.google.com",
                                        "/sslsecure/test1/", AuthnMechanism.BASIC_AUTH, null);
    id = UserIdentity.compatNew("ruth_test1", "test1", domain);
    conn = new BasicAuthConnector(domain.getLoginUrl());
    result = conn.authenticate(id);
    assertFalse(result.isValid());  // TODO SSL problem, make this work
  }
}
