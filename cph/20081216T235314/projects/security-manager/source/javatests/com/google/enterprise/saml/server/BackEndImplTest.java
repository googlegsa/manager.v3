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

package com.google.enterprise.saml.server;

import junit.framework.TestCase;
import com.google.enterprise.security.manager.LocalSessionManager;
import com.google.enterprise.security.connectors.formauth.CookieUtil;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

import javax.servlet.http.Cookie;
import java.util.Vector;

/**
 * Unit test for BackEndImpl.
 */
public class BackEndImplTest extends TestCase {

  private BackEndImpl backend;
  private LocalSessionManager sm;
  private AuthSite basicSite = new AuthSite("host1", "realm1", AuthNMechanism.BASIC_AUTH, "uri1");
  private AuthSite formsSite = new AuthSite("host2", "realm2", AuthNMechanism.FORMS_AUTH, "uri2");
  //private AuthSite connectorSite = new AuthSite("host3", "realm3", AuthNMechanism.CONNECTORS, "uri3");

  public void setUp() {
    sm = new LocalSessionManager();
    backend = new BackEndImpl(sm,
        new AuthzResponderImpl(),
        "foo",  // loginConfigFile
        "bar",  // ssoUrl
        "baz"); // artifact resolution url

  }

  public void testUpdateSessionManager() {
    UserIdentity uid1 = new UserIdentity("user","password",basicSite);
    String sid = sm.createSession();
    backend.updateSessionManager(sid, new UserIdentity[]{uid1});
    assertEquals("user", backend.adapter.getUsername(sid));
    assertEquals("password", backend.adapter.getPassword(sid));

    UserIdentity uid2 = new UserIdentity("joe","bob",formsSite);
    uid2.setCookie("cookieOne", "cookieOneVal");
    UserIdentity uid3 = new UserIdentity("john","doe",formsSite);
    sid = sm.createSession();
    backend.updateSessionManager(sid, new UserIdentity[]{uid3,uid1,uid2});
    assertEquals("user", backend.adapter.getUsername(sid));
    assertEquals("password", backend.adapter.getPassword(sid));
    Vector<Cookie> cookies = CookieUtil.deserializeCookies(backend.adapter.getCookies(sid));
    assertEquals(1, cookies.size());
    assertEquals("cookieOne", cookies.get(0).getName());
    assertEquals("cookieOneVal", cookies.get(0).getValue());
  }

}
