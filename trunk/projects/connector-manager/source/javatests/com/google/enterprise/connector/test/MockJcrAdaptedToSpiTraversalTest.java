package com.google.enterprise.connector.test;

import com.google.enterprise.connector.jcradaptor.SpiRepositoryFromJcr;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrRepository;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

import junit.framework.TestCase;

public class MockJcrAdaptedToSpiTraversalTest extends TestCase {

  public void testTraversal() throws RepositoryLoginException, RepositoryException {
    MockRepositoryEventList mrel = 
      new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    javax.jcr.Repository jcrRepo = new MockJcrRepository(r);
    Connector repo = new SpiRepositoryFromJcr(jcrRepo);
    Session session = repo.login();
    TraversalManager qtm = session.getTraversalManager();
    QueryTraversalUtil.runTraversal(qtm, 2);
  }
  
}