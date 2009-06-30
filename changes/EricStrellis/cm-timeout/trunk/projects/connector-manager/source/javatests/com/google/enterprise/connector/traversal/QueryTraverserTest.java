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

package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.instantiator.MockInstantiator;
import com.google.enterprise.connector.instantiator.ThreadPool;
import com.google.enterprise.connector.jcr.JcrTraversalManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.jcr.query.QueryManager;

/**
 * Tests for {@link com.google.enterprise.connector.traversal.QueryTraverser}.
 */
public class QueryTraverserTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.traversal.QueryTraverser
   * #runBatch(int)}.
   */
  public final void testRunBatch() {
    runTestBatches(0);
    runTestBatches(1);
    runTestBatches(2);
    runTestBatches(3);
    runTestBatches(4);
    runTestBatches(5);
  }

  private void runTestBatches(int batchSize) {
    ThreadPool threadPool =
      ThreadPool.newThreadPoolWithMaximumTaskLifeMillis(5000);
    MockInstantiator instantiator = new MockInstantiator(threadPool);
    try {
      MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
      String connectorName = "foo";
      Traverser traverser = createTraverser(mrel, connectorName, instantiator);

      System.out.println();
      System.out.println("Running batch test batchsize " + batchSize);

      int totalDocsProcessed = 0;
      int batchNumber = 0;
      while (true) {
        int docsProcessed = 0;
        boolean exceptionThrown = false;
        try {
          docsProcessed = traverser.runBatch(batchSize);
          if (docsProcessed <= 0) {
            break;
          }
        } catch (IllegalArgumentException e) {
          exceptionThrown = true;
          assertTrue("Batch size = " + batchSize + "; " + e, batchSize <= 0);
          return;
        } finally {
          if (!exceptionThrown) {
            assertTrue(batchSize > 0);
          }
        }
        totalDocsProcessed += docsProcessed;
        String state = "";
        try {
          state = instantiator.getConnectorState(connectorName);
        } catch (ConnectorNotFoundException e) {
          fail("Connector " + connectorName + " Not Found: " + e.toString());
        }
        System.out.println("Batch# " + batchNumber + " docs " + docsProcessed
            + " checkpoint " + state);
        batchNumber++;
      }
      assertEquals(4, totalDocsProcessed);
    } finally {
      instantiator.shutdown(true, 5000);
    }
  }

  /**
   * Create a Traverser.
   * @param mrel
   * @param connectorName
   * @param instantiator
   * @return a Traverser instance
   */
  private Traverser createTraverser(MockRepositoryEventList mrel,
      String connectorName, MockInstantiator instantiator) {
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());

    TraversalManager qtm = new JcrTraversalManager(qm);
    MockPusher pusher = new MockPusher(System.out);

    Traverser traverser = new QueryTraverser(pusher, qtm,
        instantiator.getTraversalStateStore(connectorName), connectorName,
        Context.getInstance().getTraversalContext());

    instantiator.setupTraverser(connectorName, traverser);
    return traverser;
  }

  /**
   * Initialize a large file used for tests.  This is to avoid
   * having giant files checked into the source code repository.
   *
   * @param fname the name of the large file to create (if it
   * doesn't already exist).
   * @throws IOException if creating the large file fails.
   */
  private void makeLargeFile(String fname) throws IOException {
    File largeFile = new File(fname);
    if (!largeFile.exists()) {
      byte[] text = "abcdefghijklmnopqrstuvwxyz\n".getBytes();
      FileOutputStream os = new FileOutputStream(largeFile);
      for (int i = 0; i < 1000000; i++) {
        os.write(text);
      }
      os.close();
    }
  }

  /**
   * Test that we are indeed streaming the file.
   */
  public void testLargeFileStream() {
    try {
      // This has internal knowledge of the contents of
      // MockRepositoryEventLogLargeFile.txt used below.
      makeLargeFile("testdata/tmp/largefile.txt");
    } catch (IOException e) {
      fail("Unable to initialize largefile.txt: " + e.toString());
    }

    ThreadPool threadPool =
        ThreadPool.newThreadPoolWithMaximumTaskLifeMillis(5000);
    MockInstantiator instantiator = new MockInstantiator(threadPool);
    try {
      MockRepositoryEventList mrel =
          new MockRepositoryEventList("MockRepositoryEventLogLargeFile.txt");
      String connectorName = "foo";
      Traverser traverser = createTraverser(mrel, connectorName, instantiator);
      int docsProcessed = 0;
      do {
        docsProcessed = traverser.runBatch(1);
      } while (docsProcessed > 0);
    } finally {
      instantiator.shutdown(true, 5000);
    }
  }

  public void testTimeout() {
    final String CONNECTOR_NAME = "fred flinstone";
    ValidatingPusher pusher = new ValidatingPusher(CONNECTOR_NAME);
    NeverEndingDocumentlistTraversalManager traversalManager =
      new NeverEndingDocumentlistTraversalManager();
    TraversalStateStore stateStore = new RecordingTraversalStateStore();
    ProductionTraversalContext context = new ProductionTraversalContext();
    context.setTraversalTimeLimitSeconds(1);
    QueryTraverser queryTraverser = new QueryTraverser(pusher, traversalManager,
        stateStore, CONNECTOR_NAME, context);
    int result = queryTraverser.runBatch(100);
    assertTrue(result > 0);
    assertEquals(traversalManager.getDocumentCount(), result);
    assertEquals(Long.toString(traversalManager.getDocumentCount()),
        stateStore.getTraversalState());
    assertEquals(traversalManager.getDocumentCount(), pusher.getPushCount());
  }

  /**
   * A {@link TraversalManager} for a {@link NeverEndingDocumentList}.
   */
  private static class NeverEndingDocumentlistTraversalManager implements
      TraversalManager {
    private long documentCount;

    public DocumentList resumeTraversal(String checkPoint) {
      throw new UnsupportedOperationException();
    }

    public void setBatchHint(int batchHint) {
      // Ignored.
    }

    public DocumentList startTraversal() {
      return new NeverEndingDocumentList(this);
    }

    synchronized Document newDocument() {
      String id = Long.toString(documentCount++);
      return ConnectorTestUtils.createSimpleDocument(id);
    }

    synchronized long getDocumentCount() {
      return documentCount;
    }
  }

  /**
   * {@link DocumentList} that returns a new document every 100
   * milliseconds until interrupted.
   */
  private static class NeverEndingDocumentList implements DocumentList {
    private final NeverEndingDocumentlistTraversalManager traversalManager;

    public NeverEndingDocumentList(
        NeverEndingDocumentlistTraversalManager traversalManager) {
      this.traversalManager = traversalManager;
    }

    public String checkpoint() {
      return Long.toString(traversalManager.getDocumentCount());
    }

    /**
     * Returns a new {@link Document} with an
     * SpiConstants.PROPNAME_DOCID property set to the number
     * previously returned.
     *
     * @throws RepositoryException interrupted while sleeping.
     */
    public Document nextDocument() throws RepositoryException {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {
        throw new RepositoryException("Unexpected interrupt", ie);
      }
      return traversalManager.newDocument();
    }
  }

  /**
   * A {@link Pusher} that performs validations
   * @see ValidatingPusher#take(Document, String) for details.
   */
  private static class ValidatingPusher implements Pusher {
    private final String connectorName;
    private volatile long pushCount;

    ValidatingPusher(String connectorName) {
      this.connectorName = connectorName;
    }

    /**
     * Performs the following validations and increments the count
     * of pushed documents if all the validations pass.
     * <OL>
     * <LI>SpiConstants.PROPNAME_DOCID property of {@link Document}
     * matches the number of documents pushed (formatted as a {@link String}).
     * <LI>connectorName matches the connector name passed to
     * {@link ValidatingPusher#ValidatingPusher(String)}.
     * </OL>
     */
    public void take(Document document, String connectorName)
        throws RepositoryException{
      String expectId = Long.toString(pushCount);
      String gotId =
        Value.getSingleValueString(document, SpiConstants.PROPNAME_DOCID);
      assertEquals(expectId, gotId);
      assertEquals(this.connectorName, connectorName);
      pushCount++;
    }

    /**
     * Returns the number of documents that have been pushed.
     */
    public long getPushCount() {
      return pushCount;
    }
  }

  /**
   * A {@link TraversalStateStore} that remembers the last saved state in
   * memory for testing purposes.
   */
  private static class RecordingTraversalStateStore
      implements TraversalStateStore {
    private String state;

    public String getTraversalState() {
      return state;
    }

    public void storeTraversalState(String state) {
      this.state = state;
    }

  }
}
