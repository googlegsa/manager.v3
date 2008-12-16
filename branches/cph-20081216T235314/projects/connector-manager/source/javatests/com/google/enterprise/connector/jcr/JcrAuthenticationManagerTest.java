// Copyright (C) 2006-2008 Google Inc.
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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.manager.UserPassIdentity;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.jcr.Credentials;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class JcrAuthenticationManagerTest extends TestCase {

  public final void testAuthenticate() throws RepositoryLoginException,
      RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog2.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());
    Session session = repo.login(creds);
    AuthenticationManager authenticationManager =
        new JcrAuthenticationManager(session);

    Assert.assertFalse(authenticationManager.authenticate(new UserPassIdentity("jimbo","jimbo")).isValid());
    Assert.assertFalse(authenticationManager.authenticate(new UserPassIdentity("admin","admin1")).isValid());
    Assert.assertFalse(authenticationManager.authenticate(new UserPassIdentity("joe","password")).isValid());
    Assert.assertFalse(authenticationManager.authenticate(new UserPassIdentity("jimbo",null)).isValid());

    // in this repository, the superuser account is open with any password
    Assert.assertTrue(authenticationManager.authenticate(new UserPassIdentity(null,"jimbo")).isValid());
    Assert.assertTrue(authenticationManager.authenticate(new UserPassIdentity(null,null)).isValid());

    Assert.assertTrue(authenticationManager.authenticate(new UserPassIdentity("admin","admin")).isValid());
    Assert.assertTrue(authenticationManager.authenticate(new UserPassIdentity("joe","joe")).isValid());
    Assert.assertTrue(authenticationManager.authenticate(new UserPassIdentity("mary","mary")).isValid());
  }

}
