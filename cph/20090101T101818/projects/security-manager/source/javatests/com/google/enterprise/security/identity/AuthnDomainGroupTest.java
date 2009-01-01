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

package com.google.enterprise.security.identity;

import junit.framework.TestCase;

import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: con Date: Dec 18, 2008 Time: 5:54:54 PM To
 * change this template use File | Settings | File Templates.
 */
public class AuthnDomainGroupTest extends TestCase {

  public void testAuthnDomainGroupParser() throws IOException {
    // TODO: change this test when new AuthnDomainGroup parser is implemented
    // to handle multiple domains in the same Group

    String config = "http://leiz.mtv.corp.google.com,/basic/,BASIC_AUTH,\n"
                    + "http://gama.corp.google.com,/secured/,FORMS_AUTH,\n"
                    + "http://mooglegoogle.com,/moogle/,FORMS_AUTH,http://loginurl.com/login";
    File  f = new File("foo");
    BufferedWriter writer = new BufferedWriter(new FileWriter(f));
    writer.write(config);
    writer.close();
    List<AuthnDomainGroup> adgs = AuthnDomainGroup.getAuthnDomainGroups(f.getAbsolutePath());

    assertEquals(3, adgs.size());

    AuthnDomainGroup adg1 = adgs.get(0);
    assertEquals(1, adg1.getDomains().size());
    AuthnDomain leizBasicDomain = adg1.getDomains().get(0);
    assertEquals("http://leiz.mtv.corp.google.com/basic/", leizBasicDomain.getName());
    assertEquals(AuthNMechanism.BASIC_AUTH, leizBasicDomain.getMechanism());
    assertEquals("http://leiz.mtv.corp.google.com/basic/", leizBasicDomain.getLoginUrl());

    assertEquals(1, adgs.get(2).getDomains().size());
    AuthnDomain moogleDomain = adgs.get(2).getDomains().get(0);
    assertEquals("http://mooglegoogle.com/moogle/", moogleDomain.getName());
    assertEquals("http://loginurl.com/login", moogleDomain.getLoginUrl());

  }
}
