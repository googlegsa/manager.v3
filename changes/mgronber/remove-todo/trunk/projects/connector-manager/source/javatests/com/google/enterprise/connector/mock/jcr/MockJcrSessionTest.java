// Copyright (C) 2006-2008 Google Inc.
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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.jcr.Credentials;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class MockJcrSessionTest extends TestCase {

  public final void testGetNodeByUUID() throws LoginException,
      RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog2.txt");
    MockRepository r = new MockRepository(mrel);

    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());

    Session session = repo.login(creds);
    Assert.assertTrue(session != null);

    Assert.assertEquals("admin", session.getUserID());

    testAccess(session, "joe", "doc1", true);
    testAccess(session, "bill", "doc1", false);
    testAccess(session, "joe", "doc2", true);
    testAccess(session, "fred", "doc2", false);
    testAccess(session, "fred", "doc3", true);
    testAccess(session, "bill", "doc4", true);
  }

  public final void testGetNodeByUUIDNewFormat()
      throws LoginException, RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLogAcl.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());

    Session session = repo.login(creds);
    Assert.assertTrue(session != null);
    Assert.assertEquals("admin", session.getUserID());

    testAccess(session, "admin", "no_acl", true);
    testAccess(session, "joe", "no_acl", true);
    testAccess(session, "mary", "no_acl", true);
    testAccess(session, "murgatroyd", "no_acl", true);

    testAccess(session, "admin", "user_group_role_acl", true);
    testAccess(session, "joe", "user_group_role_acl", true);
    testAccess(session, "mary", "user_group_role_acl", true);
    testAccess(session, "eng", "user_group_role_acl", false);
    testAccess(session, "murgatroyd", "user_group_role_acl", false);

    testAccess(session, "admin", "user_scoped_owner_acl", true);
    testAccess(session, "joe", "user_scoped_owner_acl", true);
    testAccess(session, "mary", "user_scoped_owner_acl", false);
  }

  private void testAccess(Session session, String username, String uuid,
      boolean expectedAccess) throws LoginException, RepositoryException {
    SimpleCredentials userCreds =
        new SimpleCredentials(username, new char[] {});
    Session userSession = session.impersonate(userCreds);
    Assert.assertEquals(username, userSession.getUserID());
    boolean found = false;
    try {
      userSession.getNodeByUUID(uuid);
      found = true;
    } catch (ItemNotFoundException e) {
      found = false;
    }
    userSession.logout();
    Assert.assertEquals(username
        + (expectedAccess ? " should " : " should not ") + "be able to see "
        + uuid, expectedAccess, found);
  }

}
