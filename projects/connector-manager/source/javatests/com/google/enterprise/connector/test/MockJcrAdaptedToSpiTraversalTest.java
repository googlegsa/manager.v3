package com.google.enterprise.connector.test;

import com.google.enterprise.connector.jcradaptor.SpiRepositoryFromJcr;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.spi.LoginException;
import com.google.enterprise.connector.spi.QueryTraversalManager;
import com.google.enterprise.connector.spi.Repository;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class MockJcrAdaptedToSpiTraversalTest extends TestCase {

  public void testTraversal() throws LoginException, RepositoryException {
    MockRepositoryEventList mrel = 
      new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    javax.jcr.Repository jcrRepo = new MockJcrRepository(r);
    Repository repo = new SpiRepositoryFromJcr(jcrRepo);
    Session session = repo.login("admin", "admin");
    QueryTraversalManager qtm = session.getQueryTraversalManager();
    QueryTraversalTest.runTraversal(qtm, 2);
  }


}
