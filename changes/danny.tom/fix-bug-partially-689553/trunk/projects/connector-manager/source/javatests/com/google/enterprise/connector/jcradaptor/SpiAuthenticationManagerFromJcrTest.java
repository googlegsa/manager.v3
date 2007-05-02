package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.spi.AuthenticationManager;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

public class SpiAuthenticationManagerFromJcrTest extends TestCase {

  public final void testAuthenticate() throws LoginException,
      RepositoryException, com.google.enterprise.connector.spi.LoginException,
      com.google.enterprise.connector.spi.RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog2.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());
    Session session = repo.login(creds);
    AuthenticationManager authenticationManager =
        new SpiAuthenticationManagerFromJcr(session);

    Assert.assertFalse(authenticationManager.authenticate("jimbo", "jimbo"));
    Assert.assertFalse(authenticationManager.authenticate("admin", "admin1"));
    Assert.assertFalse(authenticationManager.authenticate("joe", "password"));
    Assert.assertFalse(authenticationManager.authenticate("jimbo", null));

    // in this repository, the superuser account is open with any password
    Assert.assertTrue(authenticationManager.authenticate(null, "jimbo"));
    Assert.assertTrue(authenticationManager.authenticate(null, null));

    Assert.assertTrue(authenticationManager.authenticate("admin", "admin"));
    Assert.assertTrue(authenticationManager.authenticate("joe", "joe"));
    Assert.assertTrue(authenticationManager.authenticate("mary", "mary"));
  }

}
