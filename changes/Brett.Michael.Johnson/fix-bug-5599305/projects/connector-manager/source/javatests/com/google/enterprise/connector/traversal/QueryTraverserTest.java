// Copyright 2006 Google Inc.
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
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.util.SystemClock;
import com.google.enterprise.connector.database.DocumentStore;

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
   * #runBatch(BatchSize)}.
   */
  public final void testRunBatch() {
    runTestBatches(1);
    runTestBatches(2);
    runTestBatches(3);
    runTestBatches(4);
    runTestBatches(5);
  }

  private void runTestBatches(int batchSize) {
    runTestBatches(batchSize, batchSize);
  }

  private void runTestBatches(int batchHint, int batchMax) {
    ThreadPool threadPool = new ThreadPool(5,
        new SystemClock() /* TODO: use mock clock? */);
    MockInstantiator instantiator = new MockInstantiator(threadPool);
    try {
      MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
      String connectorName = "foo";
      Traverser traverser = createTraverser(mrel, connectorName, instantiator);

      System.out.println();
      BatchSize batchSize = new BatchSize(batchHint, batchMax);
      System.out.println("Running batch test batchsize " + batchSize);

      int totalDocsProcessed = 0;
      int batchNumber = 0;
      while (true) {
        int docsProcessed = 0;
        docsProcessed = traverser.runBatch(batchSize).getCountProcessed();
        if (docsProcessed <= 0) {
          break;
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

    Traverser traverser = new QueryTraverser(new MockPusher(System.out), qtm,
        instantiator.getTraversalStateStore(connectorName), connectorName,
        Context.getInstance().getTraversalContext(),
        new SystemClock() /* TODO: use a mock clock? */, null);

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

    ThreadPool threadPool = new ThreadPool(5,
        new SystemClock() /* TODO: use mock clock? */);
    MockInstantiator instantiator = new MockInstantiator(threadPool);
    try {
      MockRepositoryEventList mrel =
          new MockRepositoryEventList("MockRepositoryEventLogLargeFile.txt");
      String connectorName = "foo";
      Traverser traverser = createTraverser(mrel, connectorName, instantiator);
      int docsProcessed = 0;
      BatchSize batchSize = new BatchSize(1, 1);
      do {
        docsProcessed = traverser.runBatch(batchSize).getCountProcessed();
      } while (docsProcessed > 0);
    } finally {
      instantiator.shutdown(true, 5000);
    }
  }

  public void testTimeout() {
    final String CONNECTOR_NAME = "fred_flinstone";
    ValidatingPusher pusher = new ValidatingPusher(CONNECTOR_NAME);
    NeverEndingDocumentlistTraversalManager traversalManager =
        new NeverEndingDocumentlistTraversalManager(100);
    TraversalStateStore stateStore = new RecordingTraversalStateStore();
    ProductionTraversalContext context = new ProductionTraversalContext();
    context.setTraversalTimeLimitSeconds(1);
    QueryTraverser queryTraverser = new QueryTraverser(pusher, traversalManager,
        stateStore, CONNECTOR_NAME, context,
        new SystemClock() /* TODO: use a mock clock */, null);

    BatchResult result = queryTraverser.runBatch(new BatchSize(100, 100));
    assertTrue(result.getCountProcessed() > 0);
    assertEquals(traversalManager.getDocumentCount(),
        result.getCountProcessed());
    assertEquals(Long.toString(traversalManager.getDocumentCount()),
        stateStore.getTraversalState());
    assertEquals(traversalManager.getDocumentCount(), pusher.getPushCount());
  }

  public void testBatchSize() {
    final String CONNECTOR_NAME = "barney_rubble";
    ValidatingPusher pusher = new ValidatingPusher(CONNECTOR_NAME);
    NeverEndingDocumentlistTraversalManager traversalManager =
        new NeverEndingDocumentlistTraversalManager(10);
    TraversalStateStore stateStore = new RecordingTraversalStateStore();
    ProductionTraversalContext context = new ProductionTraversalContext();
    context.setTraversalTimeLimitSeconds(1);
    QueryTraverser queryTraverser = new QueryTraverser(pusher, traversalManager,
        stateStore, CONNECTOR_NAME, context, new SystemClock(), null);

    BatchResult result = queryTraverser.runBatch(new BatchSize(10, 20));
    assertTrue(result.getCountProcessed() > 10);
    assertTrue(result.getCountProcessed() <= 20);

    assertEquals(traversalManager.getDocumentCount(),
        result.getCountProcessed());
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
    private final DocumentList documentList;

    public NeverEndingDocumentlistTraversalManager(int docMillis) {
      this.documentList = new NeverEndingDocumentList(docMillis, this);
    }

    public DocumentList resumeTraversal(String checkPoint) {
      throw new UnsupportedOperationException();
    }

    public void setBatchHint(int batchHint) {
      // Ignored.
    }

    public DocumentList startTraversal() {
      return documentList;
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
   * {@link DocumentList} that returns a new document every {@code docMillis}
   * milliseconds until interrupted.
   */
  private static class NeverEndingDocumentList implements DocumentList {
    private final NeverEndingDocumentlistTraversalManager traversalManager;
    private final int docMillis;

    public NeverEndingDocumentList(int docMillis,
        NeverEndingDocumentlistTraversalManager traversalManager) {
      this.docMillis = docMillis;
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
        Thread.sleep(docMillis);
      } catch (InterruptedException ie) {
        throw new RepositoryException("Unexpected interrupt", ie);
      }
      return traversalManager.newDocument();
    }
  }

  /**
   * A {@link Pusher} that performs validations
   * @see ValidatingPusher#take(Document, DocumentStore) for details.
   */
  private static class ValidatingPusher implements Pusher, PusherFactory {
    private final String connectorName;
    private volatile long pushCount;

    ValidatingPusher(String connectorName) {
      this.connectorName = connectorName;
    }

    /**
     * Performs the following validations:
     * <OL>
     * <LI>connectorName matches the connector name passed to
     * {@link ValidatingPusher#ValidatingPusher(String)}.
     * </OL>
     */
    public Pusher newPusher(String connectorName) {
      assertEquals(this.connectorName, connectorName);
      return this;
    }

    /**
     * Performs the following validations and increments the count
     * of pushed documents if all the validations pass.
     * <OL>
     * <LI>SpiConstants.PROPNAME_DOCID property of {@link Document}
     * matches the number of documents pushed (formatted as a {@link String}).
     * </OL>
     */
    public boolean take(Document document, DocumentStore ignored)
        throws RepositoryException {
      String expectId = Long.toString(pushCount);
      String gotId =
          Value.getSingleValueString(document, SpiConstants.PROPNAME_DOCID);
      assertEquals(expectId, gotId);
      pushCount++;
      return true;
    }

    public void flush() {
    }

    public void cancel() {
      pushCount = 0;
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
