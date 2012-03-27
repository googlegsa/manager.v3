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
import com.google.enterprise.connector.mock.MockRepositoryDateTime;
import com.google.enterprise.connector.mock.MockRepositoryEventList;

import junit.framework.TestCase;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * Unit tests for Mock JCR repository.
 */
public class MockJcrRepositoryTest extends TestCase {

  /**
   * Basic sanity test
   */
  public void testSimpleRepository() throws LoginException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    assertTrue
        (r.getCurrentTime().compareTo(new MockRepositoryDateTime(60)) == 0);

    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());

    Session session = repo.login(creds);
    assertTrue(session != null);
  }

  /**
   * Test session with "users" document and "acl" properties.
   */
  public void testAuthnRepository() throws LoginException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog2.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());
    Session session = repo.login(creds);
    assertTrue(session != null);
    // Test non-admin user
    creds = new SimpleCredentials("fred", "fred".toCharArray());
    session = repo.login(creds);
    assertTrue(session != null);
  }

  /**
   * Test for Issue 3.  Bad login should result in a LoginException.
   */
  public final void testIssue3() {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog7.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);

    // Test good credentials.
    Credentials creds = new SimpleCredentials("joe", "joe".toCharArray());
    Session session = null;
    try {
      session = repo.login(creds);
    } catch (LoginException e) {
      fail("Caught unexpected exception: " + e.getMessage());
    }
    assertNotNull(session);

    // Test bad login.
    session = null;
    creds = new SimpleCredentials("joe", "joey".toCharArray());
    try {
      session = repo.login(creds);
      fail("Expected exception not thrown.");
    } catch (LoginException expected) {
      assertEquals("Given credentials not valid.", expected.getMessage());
    }
    assertNull(session);

    // Test bad user.
    session = null;
    creds = new SimpleCredentials("rat", "rat".toCharArray());
    try {
      session = repo.login(creds);
      fail("Expected exception not thrown.");
    } catch (LoginException expected) {
      assertEquals("Given credentials not valid.", expected.getMessage());
    }
    assertNull(session);
  }
}
