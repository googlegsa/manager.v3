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

/**
 * Unit test for GSASessionAdapter.
 */
public class GSASessionAdapterTest extends TestCase {

  private LocalSessionManager sm;
  private GSASessionAdapter adapter;

  @Override
  public void setUp() {
    sm = new LocalSessionManager();
    adapter = new GSASessionAdapter(sm);
  }

  public void testGettersAndSetters() {
    String sid = sm.createSession();
    String username = "user";
    String password = "pass";
    String domain = "dome main";
    String verifiedId = "verified eye dee";
    String cookies = "coooooookies";
    String groups = "groups!";

    adapter.setUsername(sid, username);
    assertEquals(username, adapter.getUsername(sid));

    adapter.setPassword(sid, password);
    assertEquals(password, adapter.getPassword(sid));

    adapter.setDomain(sid, domain);
    assertEquals(domain, adapter.getDomain(sid));

    adapter.setVerifiedId(sid, verifiedId);
    assertEquals(verifiedId, adapter.getVerifiedUserId(sid));

    adapter.setGroups(sid, groups);
    assertEquals(groups, adapter.getGroups(sid));

    adapter.setCookies(sid, cookies);
    assertEquals(cookies, adapter.getCookies(sid));
  }
}
