package com.google.enterprise.connector.jcradaptor;

import com.google.enterprise.connector.manager.UserPassIdentity;
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
      RepositoryException, com.google.enterprise.connector.spi.RepositoryLoginException,
      com.google.enterprise.connector.spi.RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog2.txt");
    MockRepository r = new MockRepository(mrel);
    MockJcrRepository repo = new MockJcrRepository(r);
    Credentials creds = new SimpleCredentials("admin", "admin".toCharArray());
    Session session = repo.login(creds);
    AuthenticationManager authenticationManager =
        new SpiAuthenticationManagerFromJcr(session);

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
