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

package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.pusher.DocumentAcceptorImpl;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.spi.DocumentAcceptor;
import com.google.enterprise.connector.spi.DocumentAcceptorException;
import com.google.enterprise.connector.spi.Lister;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests for MockLister.
 */
public class MockListerTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(MockListerTest.class.getName());

  // Common objects used by many tests.
  MockPusher pusher;
  DocumentAcceptorImpl documentAcceptor;
  String connectorName;

  @Override
  protected void setUp() throws Exception {
    connectorName = getName();
    pusher = new MockPusher();
    documentAcceptor = new DocumentAcceptorImpl(connectorName, pusher, null);
  }

  private MockLister getLister(long maxDocs, long delayMillis)
      throws Exception {
    MockLister lister = new MockLister(maxDocs, delayMillis);
    lister.setDocumentAcceptor(documentAcceptor);
    lister.start();
    return lister;
  }

  /** Test feeding a limited number of docs. */
  public void testFeedDocs() throws Exception {
    MockLister lister = getLister(10, 0);
    assertEquals(10, lister.getDocumentCount());
    assertEquals(10, pusher.getTotalDocs());
  }

  /** Test DocumentAcceptor, no documents. */
  public void testFeedNoDocs() throws Exception {
    MockLister lister = getLister(0, 0);
    assertEquals(0, lister.getDocumentCount());
    assertEquals(0, pusher.getTotalDocs());
  }

  /** Test interdocument delay. */
  public void testDocMillis() throws Exception {
    MockLister lister = getLister(3, 50);
    assertEquals(0, lister.getDocumentCount());
    try { Thread.sleep(75); } catch (InterruptedException ignored) {}
    assertEquals(1, lister.getDocumentCount());
    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
    assertEquals(2, lister.getDocumentCount());
    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
    assertEquals(3, lister.getDocumentCount());
    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
    assertEquals(3, lister.getDocumentCount());
    assertEquals(3, pusher.getTotalDocs());
  }

  /** Test shutdown. */
  public void testShutdown() throws Exception {
    MockLister lister = getLister(100, 50);
    assertEquals(0, lister.getDocumentCount());
    try { Thread.sleep(75); } catch (InterruptedException ignored) {}
    assertEquals(1, lister.getDocumentCount());
    lister.shutdown();
    assertTrue(lister.isShutdown);
    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
    assertEquals(1, lister.getDocumentCount());
    assertEquals(1, pusher.getTotalDocs());
  }
}