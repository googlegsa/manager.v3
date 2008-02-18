package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepositoryEventList;

import junit.framework.TestCase;

import java.util.logging.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

public class MockJcrEventIteratorTest extends TestCase {
  private static final Logger logger =
    Logger.getLogger(MockJcrEventIteratorTest.class.getName());

  /**
   * Sanity test
   * @throws RepositoryException 
   */
  public void testSimpleIterator() throws RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");

    // create an event iterator over the entire list
    EventIterator ei = new MockJcrEventIterator(mrel.getEventList().iterator());
    Event e;
    int count = 0;
    while (ei.hasNext()) {
      e = ei.nextEvent();
      count++;
      logger.info("Event: path=" + e.getPath() + ", type=" + e.getType());
    }
    assertEquals(6, count);
  }
}
