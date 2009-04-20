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

package com.google.enterprise.saml.server;

import com.google.enterprise.common.SecurityManagerTestCase;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;
import com.google.enterprise.security.connectors.formauth.CookieUtil;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.CredentialsGroupConfig;
import com.google.enterprise.security.identity.IdentityElementConfig;
import com.google.enterprise.security.manager.LocalSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.Cookie;

/**
 * Unit test for BackEndImpl.
 */
public class BackEndImplTest extends SecurityManagerTestCase {

  private final List<CredentialsGroupConfig> groupConfigs;
  private LocalSessionManager sm;
  private BackEndImpl backend;

  public BackEndImplTest() {
    groupConfigs = new ArrayList<CredentialsGroupConfig>();
    groupConfigs.add(new CredentialsGroupConfig("group1"));
    groupConfigs.add(new CredentialsGroupConfig("group2"));
    groupConfigs.add(new CredentialsGroupConfig("group3"));

    new IdentityElementConfig(AuthNMechanism.BASIC_AUTH, "basic_loginurl", groupConfigs.get(0));
    new IdentityElementConfig(AuthNMechanism.FORMS_AUTH, "forms_loginurl", groupConfigs.get(1));
    new IdentityElementConfig(AuthNMechanism.CONNECTORS, "connector_loginurl", groupConfigs.get(2));
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    sm = new LocalSessionManager();
    backend = new BackEndImpl(sm, new AuthzResponderImpl());
  }

  public void testUpdateSessionManager() {
    List<CredentialsGroup> groups = CredentialsGroup.newGroups(groupConfigs);
    CredentialsGroup basicCG = groups.get(0);
    CredentialsGroup formsCG = groups.get(1);
    // CredentialsGroup connectorCG = groups.get(2);

    basicCG.getElements().get(0).setUsername("user");
    basicCG.getElements().get(0).setPassword("password");

    String sid = sm.createSession();
    List<CredentialsGroup> cgList = new ArrayList<CredentialsGroup>();
    cgList.add(basicCG);
    backend.updateSessionManager(sid, cgList);
    assertEquals("user", backend.adapter.getUsername(sid));
    assertEquals("password", backend.adapter.getPassword(sid));

    formsCG.getElements().get(0).setUsername("joe");
    formsCG.getElements().get(0).setPassword("bob");
    formsCG.addCookie(new Cookie("cookieOne", "cookieOneVal"));

    sid = sm.createSession();
    cgList.add(formsCG);
    backend.updateSessionManager(sid, cgList);
    assertEquals("user", backend.adapter.getUsername(sid));
    assertEquals("password", backend.adapter.getPassword(sid));
    Vector<Cookie> cookies = CookieUtil.deserializeCookies(backend.adapter.getCookies(sid));
    assertEquals(1, cookies.size());
    assertEquals("cookieOne", cookies.get(0).getName());
    assertEquals("cookieOneVal", cookies.get(0).getValue());
  }

}
