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
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.pusher.PushException;
import com.google.enterprise.connector.pusher.Pusher.PusherStatus;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.TraversalContextAware;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.util.database.JdbcDatabase;
import com.google.enterprise.connector.util.database.testing.TestJdbcDatabase;
import com.google.enterprise.connector.util.testing.AdjustableClock;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.jcr.query.QueryManager;

/**
 * Tests for {@link com.google.enterprise.connector.traversal.QueryTraverser}.
 */
public class QueryTraverserTest extends TestCase {
  protected static final File RESOURCE_DIR = new File("source/resources/");

  // Common objects used by many tests.
  AdjustableClock clock;
  ThreadPool threadPool;
  MockInstantiator instantiator;
  ValidatingPusher pusher;
  TraversalStateStore stateStore;
  ProductionTraversalContext traversalContext;
  String connectorName;

  @Override
  protected void setUp() throws Exception {
    connectorName = getName();
    clock = new AdjustableClock();
    threadPool = new ThreadPool(5, clock);
    instantiator = new MockInstantiator(threadPool);
    pusher = new ValidatingPusher();
    traversalContext = new ProductionTraversalContext();
    traversalContext.setTraversalTimeLimitSeconds(1);
    stateStore = new RecordingTraversalStateStore();
  }

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

  private void runTestBatches(int batchHint) {
    try {
      // Reset traversal state from previous tests.
      stateStore.storeTraversalState(null);

      MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
      Traverser traverser = createTraverser(mrel);

      System.out.println();
      BatchSize batchSize = new BatchSize(batchHint);
      System.out.println("Running batch test batchsize " + batchHint);

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
  private Traverser createTraverser(MockRepositoryEventList mrel) {
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);
    Traverser traverser = new QueryTraverser(new MockPusher(System.out), qtm,
        stateStore, connectorName, traversalContext, clock);
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
      for (int i = 0; i < 100000; i++) {
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
    try {
      MockRepositoryEventList mrel =
          new MockRepositoryEventList("MockRepositoryEventLogLargeFile.txt");
      Traverser traverser = createTraverser(mrel);
      int docsProcessed = 0;
      BatchSize batchSize = new BatchSize(1);
      do {
        docsProcessed = traverser.runBatch(batchSize).getCountProcessed();
      } while (docsProcessed > 0);
    } finally {
      instantiator.shutdown(true, 5000);
    }
  }

  private void checkResult(long documentCount, BatchResult result) {
    assertEquals(documentCount, result.getCountProcessed());
    assertEquals(Long.toString(documentCount), stateStore.getTraversalState());
    assertEquals(documentCount, pusher.getPushCount());
  }

  public void testNullStateStore() {
    NeverEndingDocumentlistTraversalManager traversalManager =
        new NeverEndingDocumentlistTraversalManager(100);

    QueryTraverser queryTraverser = new QueryTraverser(pusher, traversalManager,
        null, connectorName, traversalContext, clock, null);
    BatchResult result = queryTraverser.runBatch(new BatchSize(100));
    assertEquals(0, result.getCountProcessed());
    assertEquals(TraversalDelayPolicy.ERROR, result.getDelayPolicy());
  }

  public void testTimeout() {
    NeverEndingDocumentlistTraversalManager traversalManager =
        new NeverEndingDocumentlistTraversalManager(100);
    QueryTraverser queryTraverser = new QueryTraverser(pusher, traversalManager,
        stateStore, connectorName, traversalContext, clock, null);

    BatchResult result = queryTraverser.runBatch(new BatchSize(100));
    assertTrue(result.getCountProcessed() > 0);
    checkResult(traversalManager.getDocumentCount(), result);
  }

  public void testBatchSize() {
    LargeDocumentlistTraversalManager traversalManager =
        new LargeDocumentlistTraversalManager(10);
    QueryTraverser queryTraverser = new QueryTraverser(pusher, traversalManager,
        stateStore, connectorName, traversalContext, clock, null);

    BatchResult result = queryTraverser.runBatch(new BatchSize(10));
    assertTrue(result.getCountProcessed() > 10);
    assertTrue(result.getCountProcessed() <= 20);
    checkResult(traversalManager.getDocumentCount(), result);
  }

  private void checkExceptionHandling(Exception exception, Where where,
                                      long documentCount) {
    ExceptionalTraversalManager traversalManager =
        new ExceptionalTraversalManager(exception, where);
    QueryTraverser queryTraverser = new QueryTraverser(pusher, traversalManager,
        stateStore, connectorName, traversalContext, clock);
    BatchResult result = queryTraverser.runBatch(new BatchSize(10));
    assertEquals(documentCount, result.getCountProcessed());
  }

  public void testBatchSizeException() {
    checkExceptionHandling(new RepositoryException("BatchSizeException"),
         Where.SET_BATCH_HINT, 2);
  }

  public void testStartTraversalRepositoryException() {
    checkExceptionHandling(new RepositoryException("StartTraversalException"),
         Where.START_TRAVERSAL, 0);
  }

  public void testStartTraversalRuntimeException() {
    checkExceptionHandling(new RuntimeException("StartTraversalException"),
         Where.START_TRAVERSAL, 0);
  }

  public void testResumeTraversalRepositoryException() {
    // Create a checkpoint to force a resume traversal.
    stateStore.storeTraversalState("2");
    checkExceptionHandling(new RepositoryException("ResumeTraversalException"),
         Where.RESUME_TRAVERSAL, 0);
  }

  public void testResumeTraversalRuntimeException() {
    // Create a checkpoint to force a resume traversal.
    stateStore.storeTraversalState("2");
    checkExceptionHandling(new RuntimeException("ResumeTraversalException"),
         Where.RESUME_TRAVERSAL, 0);
  }

  public void testFirstDocumentRepositoryException() {
    checkExceptionHandling(new RepositoryException("FirstDocumentException"),
         Where.FIRST_DOCUMENT, 0);
  }

  public void testFirstDocumentRepositoryDocumentException() {
    checkExceptionHandling(
         new RepositoryDocumentException("FirstDocumentRepositoryDocumentException"),
         Where.FIRST_DOCUMENT, 1);
  }

  public void testFirstDocumentRuntimeException() {
    checkExceptionHandling(new RuntimeException("FirstDocumentException"),
         Where.FIRST_DOCUMENT, 0);
  }

  public void testNextDocumentRepositoryException() {
    checkExceptionHandling(new RepositoryException("NextDocumentException"),
         Where.NEXT_DOCUMENT, 0);
  }

  public void testNextDocumentRepositoryDocumentException() {
    checkExceptionHandling(
         new RepositoryDocumentException("NextDocumentRepositoryDocumentException"),
         Where.NEXT_DOCUMENT, 1);
  }

  public void testNextDocumentRuntimeException() {
    checkExceptionHandling(new RuntimeException("NextDocumentException"),
         Where.NEXT_DOCUMENT, 1);
  }

  public void testCheckpointRepositoryException() {
    checkExceptionHandling(new RepositoryException("CheckpointException"),
         Where.CHECKPOINT, 0);
  }

  public void testCheckpointRuntimeException() {
    checkExceptionHandling(new RuntimeException("CheckpointException"),
         Where.CHECKPOINT, 0);
  }

  public void testDocidRepositoryException() {
    checkExceptionHandling(
         new RepositoryException("DocidRepositoryException"),
         Where.DOCUMENT_DOCID, 0);
  }

  public void testDocidRuntimeException() {
    checkExceptionHandling(
         new IllegalArgumentException("DocidRuntimeException"),
         Where.DOCUMENT_DOCID, 0);
  }

  public void testDocumentRepositoryException() {
    checkExceptionHandling(
         new RepositoryException("DocumentRepositoryException"),
         Where.DOCUMENT_CONTENT, 0);
  }

  public void testRepositoryDocumentException() {
    checkExceptionHandling(
         new RepositoryDocumentException("RepositoryDocumentException"),
         Where.DOCUMENT_CONTENT, 0);
  }

  public void testSkipDocumentException() {
    checkExceptionHandling(
         new SkippedDocumentException("SkippedDocumentException"),
         Where.DOCUMENT_CONTENT, 0);
  }

  public void testDocumentRuntimeException() {
    checkExceptionHandling(new RuntimeException("DocumentException"),
         Where.DOCUMENT_CONTENT, 0);
  }

  /**
   * A {@link TraversalManager} for a {@link NeverEndingDocumentList}.
   */
  private class NeverEndingDocumentlistTraversalManager implements
        TraversalManager, TraversalContextAware {
    private final int docMillis;
    private long documentCount;

    public NeverEndingDocumentlistTraversalManager(int docMillis) {
      this.docMillis = docMillis;
    }

    public void setTraversalContext(TraversalContext traversalContext) {
      throw new UnsupportedOperationException();
    }

    public void setBatchHint(int batchHint) {
      // Ignored.
    }

    public DocumentList startTraversal() {
      return new NeverEndingDocumentList(this);
    }

    public DocumentList resumeTraversal(String checkPoint) {
      throw new UnsupportedOperationException();
    }

    // Return a new document every {@code docMillis} milliseconds.
    synchronized Document newDocument() {
      clock.adjustTime(docMillis);
      String id = Long.toString(documentCount++);
      return ConnectorTestUtils.createSimpleDocument(id);
    }

    synchronized long getDocumentCount() {
      return documentCount;
    }
  }

  /**
   * {@link DocumentList} that never runs out of documents -
   * returning new documents until interrupted.
   */
  private class NeverEndingDocumentList implements DocumentList {
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
     */
    public Document nextDocument() throws RepositoryException {
      return traversalManager.newDocument();
    }
  }

  /**
   * A {@link TraversalManager} for a {@link LargeDocumentList}.
   */
  private class LargeDocumentlistTraversalManager extends
      NeverEndingDocumentlistTraversalManager {
    private int batchHint;

    public LargeDocumentlistTraversalManager(int docMillis) {
      super(docMillis);
    }

    @Override
    public void setBatchHint(int batchHint) {
      this.batchHint = batchHint;
    }

    synchronized int getBatchHint() {
      return batchHint;
    }

    @Override
    public DocumentList startTraversal() {
      return new LargeDocumentList(this);
    }
  }

  /**
   * {@link DocumentList} that returns twice the batchHint
   * number of documents.
   */
  private class LargeDocumentList extends NeverEndingDocumentList {
    private final LargeDocumentlistTraversalManager traversalManager;

    public LargeDocumentList(
          LargeDocumentlistTraversalManager traversalManager) {
      super(traversalManager);
      this.traversalManager = traversalManager;
    }

    @Override
    public Document nextDocument() throws RepositoryException {
      if (traversalManager.getDocumentCount() <
          2 * traversalManager.getBatchHint()) {
        return super.nextDocument();
      } else {
        return null;
      }
    }
  }


  /**
   * Locations from where ExceptionalTraversalManager will throw
   * its exceptions.
   */
  private static enum Where {
      NONE,
      SET_BATCH_HINT, START_TRAVERSAL, RESUME_TRAVERSAL, // TraversalManager
      FIRST_DOCUMENT, NEXT_DOCUMENT, CHECKPOINT, // DocumentList
      DOCUMENT_DOCID, DOCUMENT_CONTENT // Document
  }

  /** Throws either a RuntimeException or a RepostioryException. */
  private  void throwException(Exception exception)
      throws RepositoryException {
    if (exception instanceof RepositoryDocumentException) {
      pusher.skipDocument();
      throw (RepositoryDocumentException) exception;
    } else if (exception instanceof RepositoryException) {
      throw (RepositoryException) exception;
    } else if (exception instanceof RuntimeException) {
      // RuntimeExceptions don't need to be declared.
      throw (RuntimeException) exception;
    }
  }

  /**
   * A {@link TraversalManager} that throws configured Exceptions.
   */
  private class ExceptionalTraversalManager implements TraversalManager {
    protected final Where where;
    protected final Exception exception;

    public ExceptionalTraversalManager(Exception exception, Where where) {
      this.exception = exception;
      this.where = where;
    }

    /* @Override */
    public void setBatchHint(int batchHint) throws RepositoryException {
      if (where == Where.SET_BATCH_HINT) {
        throwException(exception);
      }
    }

    /* @Override */
    public DocumentList startTraversal() throws RepositoryException {
      if (where == Where.START_TRAVERSAL) {
        throwException(exception);
      }
      return new ExceptionalDocumentList(0, exception, where);
    }

    /* @Override */
    public DocumentList resumeTraversal(String checkpoint)
        throws RepositoryException {
      if (where == Where.RESUME_TRAVERSAL) {
        throwException(exception);
      }
      int docNum = Integer.parseInt(checkpoint);
      if (docNum > 3) {
        return null;  // Only return two batches.
      }
      return new ExceptionalDocumentList(docNum, exception, where);
    }
  }

  /**
   * A {@link DocumentList} that throws configured Exceptions.
   */
  private class ExceptionalDocumentList implements DocumentList {
    protected final Where where;
    protected final Exception exception;
    protected final int startNum;
    protected int docNum;

    public ExceptionalDocumentList(int docNum, Exception exception,
                                   Where where) {
      this.startNum = docNum;
      this.docNum = docNum;
      this.exception = exception;
      this.where = where;
    }

    /* @Override */
    public Document nextDocument() throws RepositoryException {
      // Return no more than two documents per batch.
      if (docNum > startNum + 1) {
        return null;
      }
      int doc = docNum++;
      // This knows we are returning 2-document batches, so although
      // docNum keeps going up, even numbered docs are the first
      // in the batch, and odd numbered docs are the next.
      switch (where) {
        case FIRST_DOCUMENT:
          if ((doc & 1) == 0) throwException(exception);
          break;
        case NEXT_DOCUMENT:
          if ((doc & 1) == 1) throwException(exception);
          break;
      }
      return new ExceptionalDocument(doc, exception, where);
    }

    /* @Override */
    public String checkpoint() throws RepositoryException {
      if (where == Where.CHECKPOINT) {
        throwException(exception);
      }
      return String.valueOf(docNum);
    }
  }

  /**
   * A {@link Document} that throws configured Exceptions.
   */
  private class ExceptionalDocument implements Document {
    protected final Where where;
    protected final Exception exception;
    protected final int docNum;
    protected final Document doc;

    public ExceptionalDocument(int docNum, Exception exception,
                               Where where) {
      this.exception = exception;
      this.where = where;
      this.docNum = docNum;
      this.doc =
          ConnectorTestUtils.createSimpleDocument(String.valueOf(docNum));
    }

    /* @Override */
    public Set<String> getPropertyNames() throws RepositoryException {
      return doc.getPropertyNames();
    }

    /* @Override */
    public Property findProperty(String name) throws RepositoryException {
      if (SpiConstants.PROPNAME_CONTENT.equals(name) &&
          where == Where.DOCUMENT_CONTENT && (docNum & 1 ) == 0) {
        throwException(exception);
      } if (SpiConstants.PROPNAME_DOCID.equals(name) &&
            where == Where.DOCUMENT_DOCID && (docNum & 1) == 1) {
        throwException(exception);
      }
      return doc.findProperty(name);
    }
  }

  /**
   * A {@link Pusher} that performs validations
   * @see ValidatingPusher#take(Document) for details.
   */
  private static class ValidatingPusher implements Pusher, PusherFactory {
    private String connectorName = null;
    private volatile long pushCount = 0;
    private volatile long expectedId = 0;

    /**
     * Performs the following validations:
     * <OL>
     * <LI>connectorName matches the connector name passed to
     * {@link ValidatingPusher#ValidatingPusher(String)}.
     * </OL>
     */
    /* @Override */
    public Pusher newPusher(String connectorName) {
      if (this.connectorName == null) {
        this.connectorName = connectorName;
      } else {
        assertEquals(this.connectorName, connectorName);
      }
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
    /* @Override */
    public synchronized PusherStatus take(Document document)
        throws RepositoryException, PushException {
      String gotId =
          Value.getSingleValueString(document, SpiConstants.PROPNAME_DOCID);
      assertEquals(Long.toString(expectedId), gotId);
      try {
        Value.getSingleValue(document, SpiConstants.PROPNAME_CONTENT);
        expectedId++;
        pushCount++;
      } catch (RepositoryDocumentException rde) {
        expectedId++;
        throw rde;
      } catch (Throwable t) {
        throw new PushException(t);
      }
      return PusherStatus.OK;
    }

    synchronized void skipDocument() {
      expectedId++;
    }

    /* @Override */
    public void flush() {
    }

    /* @Override */
    public synchronized void cancel() {
      pushCount = 0;
      expectedId = 0;
    }

    /* @Override */
    public PusherStatus getPusherStatus() {
      return PusherStatus.OK;
    }

    /**
     * Returns the number of documents that have been pushed.
     */
    synchronized long getPushCount() {
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
