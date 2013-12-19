// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.pusher.ExceptionalPusher;
import com.google.enterprise.connector.pusher.ExceptionalPusher.Where;
import com.google.enterprise.connector.pusher.Pusher.PusherStatus;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentAcceptor;
import com.google.enterprise.connector.spi.DocumentAcceptorException;
import com.google.enterprise.connector.spi.Lister;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.traversal.MockLister;
import com.google.enterprise.connector.util.SystemClock;

import junit.framework.TestCase;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests for DocumentAcceptorImpl.
 */
public class DocumentAcceptorTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(DocumentAcceptorTest.class.getName());

  /** Test feeding a limited number of docs. */
  public void testFeedDocs() throws Exception {
    String connectorName = getName();
    MockPusher pusher = new MockPusher();
    DocumentAcceptorImpl documentAcceptor =
        new DocumentAcceptorImpl(connectorName, pusher);
    MockLister lister = new MockLister(10, 0);
    lister.setDocumentAcceptor(documentAcceptor);

    // With no inter-document delay, MockLister feeds all documents
    // from its start() method, then returns when done.
    lister.start();
    assertEquals(10, pusher.getTotalDocs());
  }

  /** Test feeding more docs after a flush. */
  public void testFeedAfterFlush() throws Exception {
    String connectorName = getName();
    MockPusher pusher = new MockPusher();
    DocumentAcceptorImpl documentAcceptor =
        new DocumentAcceptorImpl(connectorName, pusher);

    // Feed a couple of documents.
    assertEquals(0, pusher.getTotalDocs());
    documentAcceptor.take(ConnectorTestUtils.createSimpleDocument("foo"));
    documentAcceptor.take(ConnectorTestUtils.createSimpleDocument("bar"));

    // Flush the feed.  This shuts down the Pusher.
    assertEquals(PusherStatus.OK, pusher.getPusherStatus());
    documentAcceptor.flush();
    assertEquals(2, pusher.getTotalDocs());
    assertEquals(PusherStatus.DISABLED, pusher.getPusherStatus());

    // Feed another document. Should get a new Pusher.
    documentAcceptor.take(ConnectorTestUtils.createSimpleDocument("baz"));
    assertEquals(1, pusher.getTotalDocs());
    assertEquals(PusherStatus.OK, pusher.getPusherStatus());

    // Cancel that feed, without flushing.
    documentAcceptor.cancel();
    assertEquals(0, pusher.getTotalDocs());
    assertEquals(PusherStatus.DISABLED, pusher.getPusherStatus());
  }

  /**
   * Test that if Pusher appears to be backlogged transmitting feeds,
   * (feeds backed up on this end of the FeedConnection), the DocumentAcceptor
   * waits a short period of time for the backlog to clear.
   */
  public void testProximalFeedBacklog() throws Exception {
    testPusherStatus(PusherStatus.LOCAL_FEED_BACKLOG, 500L, 2000L, 1, 500L);
    testPusherStatus(PusherStatus.LOCAL_FEED_BACKLOG, 500L, 2000L, 2, 2 * 500L);
  }

  /**
   * Test that if GSA appears to be backlogged processing feeds, the
   * DocumentAcceptor waits a longer period of time for the backlog to clear.
   */
  public void testDistalFeedBacklog() throws Exception {
    testPusherStatus(PusherStatus.GSA_FEED_BACKLOG, 250L, 750L, 1, 750L);
    testPusherStatus(PusherStatus.GSA_FEED_BACKLOG, 250L, 750L, 2, 2 * 750L);
  }

  /**
   * Test that if runtime environment seems to be running low on memory, the
   * DocumentAcceptor waits a short period of time for the low memory condition
   * to clear.
   */
  public void testLowMemory() throws Exception {
    testPusherStatus(PusherStatus.LOW_MEMORY, 500L, 2000L, 1, 500L);
    testPusherStatus(PusherStatus.LOW_MEMORY, 500L, 2000L, 2, 2 * 500L);
  }

  private void testPusherStatus(PusherStatus status, long shortSleep,
      long longSleep, int retries, long expectedDelay) throws Exception {
    SystemClock clock = new SystemClock();
    String connectorName = getName();
    Document document = ConnectorTestUtils.createSimpleDocument("foo");
    MockPusher pusher = new MockPusher();
    DocumentAcceptorImpl documentAcceptor =
        new DocumentAcceptorImpl(connectorName, pusher);
    documentAcceptor.setSleepIntervals(shortSleep, longSleep, retries);

    // Initial document feed should go unimpeded.
    long startTime = clock.getTimeMillis();
    documentAcceptor.take(document);
    long stopTime = clock.getTimeMillis();
    assertEquals(1, pusher.getTotalDocs());
    assertEquals(PusherStatus.OK, pusher.getPusherStatus());
    assertTrue("Delay too long " + (stopTime - startTime),
               shortSleep > (stopTime - startTime));

    // If pusher returns not-ready status, documentAcceptor should sleep.
    pusher.setPusherStatus(status);
    startTime = clock.getTimeMillis();
    documentAcceptor.take(document);
    stopTime = clock.getTimeMillis();
    assertEquals(2, pusher.getTotalDocs());
    assertTrue("Delay " + (stopTime - startTime),
               expectedDelay <= (stopTime - startTime));

    // Once the Pusher is OK, there should be no delays.
    pusher.setPusherStatus(PusherStatus.OK);
    startTime = clock.getTimeMillis();
    documentAcceptor.take(document);
    stopTime = clock.getTimeMillis();
    assertEquals(3, pusher.getTotalDocs());
    assertTrue("Delay " + (stopTime - startTime),
               shortSleep > (stopTime - startTime));
  }

  /** Test PushException in take(). */
  public void testTakePushException() throws Exception {
    checkExceptionHandling(new PushException("TestPushException"), Where.TAKE);
  }

  /** Test FeedException in take(). */
  public void testTakeFeedException() throws Exception {
    checkExceptionHandling(new FeedException("TestFeedException"), Where.TAKE);
  }

  /** Test RepositoryException in take(). */
  public void testTakeRepositoryException() throws Exception {
    checkExceptionHandling(new RepositoryException("TestRepositoryException"),
                           Where.TAKE);
  }

  /** Test RuntimeException in take(). */
  public void testTakeRuntimeException() throws Exception {
    checkExceptionHandling(new RuntimeException("TestRuntimeException"),
                           Where.TAKE);
  }

  /** Test PushException in flush(). */
  public void testFlushPushException() throws Exception {
    checkExceptionHandling(new PushException("TestPushException"), Where.FLUSH);
  }

  /** Test FeedException in flush(). */
  public void testFlushFeedException() throws Exception {
    checkExceptionHandling(new FeedException("TestFeedException"), Where.FLUSH);
  }

  /** Test RepositoryException in flush(). */
  public void testFlushRepositoryException() throws Exception {
    checkExceptionHandling(new RepositoryException("TestRepositoryException"),
                           Where.FLUSH);
  }

  /** Test RuntimeException in flush(). */
  public void testFlushRuntimeException() throws Exception {
    checkExceptionHandling(new RuntimeException("TestRuntimeException"),
                           Where.FLUSH);
  }

  /** Test PushException in cancel(). */
  public void testCancelPushException() throws Exception {
    checkExceptionHandling(new PushException("TestPushException"),
                           Where.CANCEL);
  }

  /** Test FeedException in cancel(). */
  public void testCancelFeedException() throws Exception {
    checkExceptionHandling(new FeedException("TestFeedException"),
                           Where.CANCEL);
  }

  /** Test RepositoryException in cancel(). */
  public void testCancelRepositoryException() throws Exception {
    checkExceptionHandling(new RepositoryException("TestRepositoryException"),
                           Where.CANCEL);
  }

  /** Test RuntimeException in cancel(). */
  public void testCancelRuntimeException() throws Exception {
    checkExceptionHandling(new RuntimeException("TestRuntimeException"),
                           Where.CANCEL);
  }

  private void checkExceptionHandling(Exception exception, Where where)
      throws Exception {
    ExceptionalPusher pusher = new ExceptionalPusher(exception, where);
    String connectorName = getName();
    DocumentAcceptorImpl documentAcceptor =
        new DocumentAcceptorImpl(connectorName, pusher);

    // Test take().
    try {
      documentAcceptor.take(ConnectorTestUtils.createSimpleDocument("testDoc"));
      assertFalse("Expected Exception", (where == Where.TAKE));
    } catch (Exception e) {
      assertTrue("Unexpected Exception", (where == Where.TAKE));
      if (exception instanceof PushException) {
        assertEquals(DocumentAcceptorException.class, e.getClass());
        assertEquals(PushException.class, e.getCause().getClass());
      } else if (exception instanceof FeedException) {
        assertEquals(DocumentAcceptorException.class, e.getClass());
        assertEquals(FeedException.class, e.getCause().getClass());
      } else if (exception instanceof RuntimeException) {
        assertEquals(RuntimeException.class, e.getClass());
      } else {
        assertEquals(RepositoryException.class, e.getClass());
      }
    }

    // Test flush().
    try {
      documentAcceptor.flush();
      assertFalse("Expected Exception", (where == Where.FLUSH));
    } catch (Exception e) {
      assertTrue("Unexpected Exception", (where == Where.FLUSH));
      if (exception instanceof PushException) {
        assertEquals(DocumentAcceptorException.class, e.getClass());
        assertEquals(PushException.class, e.getCause().getClass());
      } else if (exception instanceof FeedException) {
        assertEquals(DocumentAcceptorException.class, e.getClass());
        assertEquals(FeedException.class, e.getCause().getClass());
      } else if (exception instanceof RuntimeException) {
        assertEquals(RuntimeException.class, e.getClass());
      } else {
        assertEquals(RepositoryException.class, e.getClass());
      }
    }

    // Test take() again after flush().
    try {
      documentAcceptor.take(ConnectorTestUtils.createSimpleDocument("testDoc"));
      assertFalse("Expected Exception", (where == Where.TAKE));
    } catch (Exception e) {
      assertTrue("Unexpected Exception", (where == Where.TAKE));
      if (exception instanceof PushException) {
        assertEquals(DocumentAcceptorException.class, e.getClass());
        assertEquals(PushException.class, e.getCause().getClass());
      } else if (exception instanceof FeedException) {
        assertEquals(DocumentAcceptorException.class, e.getClass());
        assertEquals(FeedException.class, e.getCause().getClass());
      } else if (exception instanceof RuntimeException) {
        assertEquals(RuntimeException.class, e.getClass());
      } else {
        assertEquals(RepositoryException.class, e.getClass());
      }
    }

    // Test cancel().
    try {
      documentAcceptor.cancel();
      assertFalse("Expected Exception", (where == Where.CANCEL));
    } catch (Exception e) {
      assertTrue("Unexpected Exception", (where == Where.CANCEL));
      assertEquals(RuntimeException.class, e.getClass());
    }
  }
}
