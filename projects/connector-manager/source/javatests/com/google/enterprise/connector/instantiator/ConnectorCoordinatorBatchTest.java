// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.LoadManager;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.BatchSize;
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
import com.google.enterprise.connector.util.SystemClock;
import com.google.enterprise.connector.database.DocumentStore;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Tests batch support in {@link ConnectorCoordinatorImpl}.
 */
public class ConnectorCoordinatorBatchTest extends TestCase {
  private static final Locale locale = Locale.ENGLISH;

  private static final int SHORT_TRAVERSAL_TIME_LIMIT_SECS = 1;

  ConnectorCoordinatorImpl coordinator;
  RecordingPusher recordingPusher;
  RecordingLoadManager recordingLoadManager;
  TypeInfo typeInfo;

  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/ConnectorCoordinatorBatchTest.xml";

  private static final String TEST_DIR_NAME =
      "testdata/tmp/ConnectorCoordinatorBatchTests";
  private final File baseDirectory  = new File(TEST_DIR_NAME);


  @Override
  protected void setUp() throws Exception {
    assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
    // Then recreate it empty.
    assertTrue(baseDirectory.mkdirs());

    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    SpringInstantiator si = (SpringInstantiator) context.getRequiredBean(
        "Instantiator", SpringInstantiator.class);
    si.init();

    typeInfo = getTypeMap().getTypeInfo("TestConnectorA");
    Assert.assertNotNull(typeInfo);

    SyncingConnector.reset();
  }

  @Override
  protected void tearDown() {
    try {
      if (coordinator != null) {
        coordinator.removeConnector();
      }
    } finally {
      coordinator = null;
      assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
    }
  }

  /** Retrieve the TypeMap from the Spring Context. */
  private TypeMap getTypeMap() {
    return (TypeMap) Context.getInstance().getRequiredBean(
        "TypeMap", TypeMap.class);
  }

  /** Retrieve the ConnectorCoordinatorMap from the Spring Context. */
  private ConnectorCoordinatorMap getCoordinatorMap() {
    return (ConnectorCoordinatorMap) Context.getInstance().getRequiredBean(
        "ConnectorCoordinatorMap", ConnectorCoordinatorMap.class);
  }

  /** Retrieve the ConnectorCoordinatorFactory from the Spring Context. */
  private ConnectorCoordinatorImplFactory getCoordinatorFactory() {
    return (ConnectorCoordinatorImplFactory) Context.getInstance().getRequiredBean(
        "ConnectorCoordinatorFactory", ConnectorCoordinatorImplFactory.class);
  }

  /** Retrieve the PusherFactory from the Spring Context. */
  private RecordingPusherFactory getPusherFactory() {
    return (RecordingPusherFactory) Context.getInstance().getRequiredBean(
        "PusherFactory", RecordingPusherFactory.class);
  }

  /** Retrieve the LoadManagerFactory from the Spring Context. */
  private RecordingLoadManagerFactory getLoadManagerFactory() {
    return (RecordingLoadManagerFactory) Context.getInstance().getRequiredBean(
        "LoadManagerFactory", RecordingLoadManagerFactory.class);
  }

  private void createPusherAndCoordinator() throws Exception {
    coordinator = (ConnectorCoordinatorImpl) getCoordinatorMap().getOrAdd("c1");
    recordingPusher = (RecordingPusher) getPusherFactory().newPusher("c1");
    recordingPusher.reset();
    recordingLoadManager =
        (RecordingLoadManager) getLoadManagerFactory().newLoadManager("c1");
    recordingLoadManager.reset();
    Configuration config = new Configuration(
        typeInfo.getConnectorTypeName(), new HashMap<String, String>(), null);
    coordinator.setConnectorConfiguration(typeInfo, config, locale, false);
    coordinator.setConnectorSchedule(new Schedule("c1:1000:0:0-0"));
  }

  public void testCreateRunRemoveLoop() throws Exception {
    for (int ix = 0; ix < 100; ix++) {
      createPusherAndCoordinator();
      runBatch(1 + ix, 1 + ix, 0);
      coordinator.removeConnector();
      coordinator = null;
    }
  }

  public void testStartThenResumeTraversal() throws Exception {
    createPusherAndCoordinator();
    runBatch(1, 1, 0);

    // Run a second batch for the same connector coordinator to confirm
    // resumeTraversal rather than startTraversal is called.
    runBatch(1, 1, 1);
  }

  private void runBatch(int expectTraversalManagerCount,
      int expectStartTraversalCount, int expectResumeTraversalCount)
      throws RepositoryException, ConnectorNotFoundException,
      InterruptedException {
    List<SimpleDocument> expectList =
        SyncingConnector.createaAndQueueDocumentList();
    String expectId =
        Value.getSingleValueString(expectList.get(0),
            SpiConstants.PROPNAME_DOCID);
    SyncingConnector.Tracker tracker =
        SyncingConnector.getTracker();
    startBatch();
    PushedDocument got = recordingPusher.poll();
    assertNotNull(tracker.toString(), got);
    String gotId =
        Value.getSingleValueString(got.getDocument(),
            SpiConstants.PROPNAME_DOCID);
    assertEquals(expectId, gotId);
    assertEquals(tracker.toString(), expectTraversalManagerCount,
                 tracker.getTraversalManagerCount());
    assertEquals(tracker.toString(), expectStartTraversalCount, tracker
        .getStartTraversalCount());
    assertEquals(tracker.toString(), expectResumeTraversalCount, tracker
        .getResumeTraversalCount());
    BatchResult batchResult = recordingLoadManager.getBatchResult();
    assertNotNull(batchResult);
    assertEquals(tracker.toString(), 1, batchResult.getCountProcessed());
    assertEquals(tracker.toString(), TraversalDelayPolicy.IMMEDIATE,
        batchResult.getDelayPolicy());
  }

  public void testManyBatches() throws Exception {
    createPusherAndCoordinator();
    for (int ix = 0; ix < 10; ix++) {
      runBatch(1, 1, ix);
    }
  }

  /**
   * Starts a batch.
   * <p>
   * These tests wait for completion of activity that occurs in a batch such as
   * the {@link SyncingConnector} registering an interrupt. There is a
   * little interval between the time such activity occurs and the time the
   * batch fully completes. Since {@link ConnectorCoordinatorImpl#startBatch()}
   * will not start a batch while one is running this function includes a retry
   * loop.
   *
   * @throws ConnectorNotFoundException
   * @throws InterruptedException
   */
  private void startBatch()
      throws ConnectorNotFoundException, InterruptedException {
    for (int iy = 0; iy < 100; iy++) {
      if (coordinator.startBatch()) {
        return;
      } else {
        Thread.sleep(20);
      }
    }
    fail("Failed to start batch - probably a batch is not ending properly.");
  }

  public void testDisabledTraversal() throws Exception {
    createPusherAndCoordinator();
    // Disable traversal schedule.  No batch should run.
    coordinator.setConnectorSchedule(new Schedule("#c1:1000:0:0-0"));
    assertFalse(coordinator.startBatch());
    assertNull(recordingLoadManager.getBatchResult());
  }

  public void testLegacyDisabledTraversal() throws Exception {
    createPusherAndCoordinator();
    // Legacy disabled traversal schedule was interval of 1-1.
    // No batch should run.
    coordinator.setConnectorSchedule(new Schedule("c1:1000:0:1-1"));
    assertFalse(coordinator.startBatch());
    assertNull(recordingLoadManager.getBatchResult());
  }

  public void testNoTraversalIntervals() throws Exception {
    createPusherAndCoordinator();
    // With no traversal intervals, no batch should run.
    coordinator.setConnectorSchedule(new Schedule("c1:1000:0:"));
    assertFalse(coordinator.startBatch());
    assertNull(recordingLoadManager.getBatchResult());
  }

  public void testOutsideTraversalIntervals() throws Exception {
    createPusherAndCoordinator();
    // If current time is outside traversal intervals, no batch should run.
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    String intervals;
    if (hour < 2 ) {
      intervals = (hour + 2) + "-24";
    } else if (hour >= 22) {
      intervals = "0-" + hour;
    } else {
      intervals = "0-" + hour + ":" + (hour + 2) + "-24";
    }
    Schedule schedule = new Schedule("c1:1000:0:" + intervals);
    coordinator.setConnectorSchedule(schedule);
    assertFalse(coordinator.startBatch());
    assertNull(recordingLoadManager.getBatchResult());
  }

  public void testOutsideWrappedTraversalIntervals() throws Exception {
    createPusherAndCoordinator();
    // If current time is outside a traversal interval that wraps around
    // midnight, no batch should run.
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);

    // Can't test this if we are too close to midnight.
    // TODO: Fix this when we have a Mockable Clock.
    if (hour > 0 && hour < 22) {
      String interval = (hour + 2) + "-" + hour;
      Schedule schedule = new Schedule("c1:1000:0:" + interval);
      coordinator.setConnectorSchedule(schedule);
      assertFalse(coordinator.startBatch());
      assertNull(recordingLoadManager.getBatchResult());
    }
  }

  public void testTraversalIntervals() throws Exception {
    createPusherAndCoordinator();
    // If current time is inside traversal intervals, a batch should run.
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    String intervals;
    if (hour <= 2 ) {
      intervals = hour + "-" + (hour + 2) + ":12-23";
    } else if (hour >= 22) {
      intervals = "0-20:" + hour + "-24";
    } else {
      intervals = "0-" + (hour - 2) + ":" + hour + "-" + (hour + 2);
    }
    Schedule schedule = new Schedule("c1:1000:0:" + intervals);
    coordinator.setConnectorSchedule(schedule);
    assertTrue(coordinator.startBatch());
  }

  public void testWrappedTraversalIntervals() throws Exception {
    createPusherAndCoordinator();
    // If current time is inside a traversal interval that wraps
    // around midnight, a batch should run. Regression test for Issue 217.
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    String interval;
    if (hour >= 20) {
      interval = "20-1";
    } else {
      interval = (hour + 3) + "-" + (hour + 1);
    }
    Schedule schedule = new Schedule("c1:1000:0:" + interval);
    coordinator.setConnectorSchedule(schedule);
    assertTrue(coordinator.startBatch());
  }

  public void testTraversalDelayPolicy1() throws Exception {
    createPusherAndCoordinator();
    // Force a POLLING wait.
    coordinator.setConnectorSchedule(new Schedule("c1:1000:250:0-0"));
    coordinator.delayTraversal(TraversalDelayPolicy.POLL);
    assertFalse(coordinator.startBatch());
    assertNull(recordingLoadManager.getBatchResult());

    // Wait until delay interval is up.  Should be able to run.
    try {
      Thread.sleep(300);
    } catch (InterruptedException ie) {}
    assertTrue(coordinator.startBatch());
  }

  public void testTraversalDelayPolicy2() throws Exception {
    createPusherAndCoordinator();
    // Force a POLLING wait.
    coordinator.setConnectorSchedule(new Schedule("c1:1000:500:0-0"));
    coordinator.delayTraversal(TraversalDelayPolicy.POLL);
    assertFalse(coordinator.startBatch());
    assertNull(recordingLoadManager.getBatchResult());
    // IMMEDIATE should cancel out any current delay policy.
    coordinator.delayTraversal(TraversalDelayPolicy.IMMEDIATE);
    assertTrue(coordinator.startBatch());
  }

  public void testTraversalDelayPolicy3() throws Exception {
    createPusherAndCoordinator();
    // Force a ERROR wait.
    coordinator.delayTraversal(TraversalDelayPolicy.ERROR);
    assertFalse(coordinator.startBatch());
    assertNull(recordingLoadManager.getBatchResult());
    // IMMEDIATE should cancel out any current delay policy.
    coordinator.delayTraversal(TraversalDelayPolicy.IMMEDIATE);
    assertTrue(coordinator.startBatch());
  }

  public void testCancelBatch() throws Exception {
    createPusherAndCoordinator();
    coordinator.startBatch();
    SyncingConnector.Tracker tracker =
        SyncingConnector.getTracker();
    tracker.blockUntilTraversing();
    assertEquals(1, tracker.getStartTraversalCount());
    coordinator.restartConnectorTraversal();
    tracker.blockUntilTraversingInterrupted();
    assertEquals(1, tracker.getTraversalManagerCount());
    assertEquals(1, tracker.getInterruptedCount());
    assertEquals(1, tracker.getStartTraversalCount());

    // Run a second batch to confirm we create a new connector
    // and call start traversal after the cancel.
    runBatch(2, 2, 0);
  }

  public void testSetConnectorConfig() throws Exception {
    createPusherAndCoordinator();
    coordinator.startBatch();
    SyncingConnector.Tracker tracker =
        SyncingConnector.getTracker();
    tracker.blockUntilTraversing();
    assertEquals(1, tracker.getStartTraversalCount());

    Configuration config = coordinator.getConnectorConfiguration();
    config.getMap().put("hi", "mom");
    coordinator.setConnectorConfiguration(typeInfo, config, locale, true);
    tracker.blockUntilTraversingInterrupted();
    assertEquals(1, tracker.getTraversalManagerCount());
    assertEquals(1, tracker.getInterruptedCount());
    assertEquals(1, tracker.getStartTraversalCount());

    // Run a second batch to confirm we create a new connector
    // and call start traversal after the cancel.
    runBatch(2, 2, 0);
  }

  public void testTimeoutBatch() throws Exception {
    // Override the default ThreadPool timeout with a much shorter timeout.
    getCoordinatorFactory().setThreadPool(
        new ThreadPool(SHORT_TRAVERSAL_TIME_LIMIT_SECS,
        new SystemClock() /* TODO: Use a mock clock */));
    createPusherAndCoordinator();
    coordinator.startBatch();
    SyncingConnector.Tracker tracker =
        SyncingConnector.getTracker();
    tracker.blockUntilTraversingInterrupted();

    assertEquals(tracker.toString(), 1, tracker.getTraversalManagerCount());
    assertEquals(tracker.toString(), 1, tracker.getStartTraversalCount());
    assertEquals(tracker.toString(), 1, tracker.getInterruptedCount());

    // Run a second batch to confirm we create a new connector
    // and call start traversal after the cancel.
    runBatch(2, 2, 0);
  }

  private static class PushedDocument {
    private final Document document;
    private final String connectorName;

    PushedDocument(Document document, String connectorName) {
      this.document = document;
      this.connectorName = connectorName;
    }

    public Document getDocument() {
      return document;
    }

    @Override
    public String toString() {
      return "PushedDocument connectorName = " + connectorName + " document = "
          + document;
    }
  }

  public static class RecordingPusherFactory implements PusherFactory {
    private RecordingPusher pusher;
    private String connectorName;

    /* @Override */
    public Pusher newPusher(String connectorName) {
      if (pusher == null) {
        pusher = new RecordingPusher(connectorName);
        this.connectorName = connectorName;
      } else {
        assertEquals(this.connectorName, connectorName);
      }
      return pusher;
    }
  }

  private static class RecordingPusher implements Pusher {
    private final BlockingQueue<PushedDocument> pushedDocuments =
        new ArrayBlockingQueue<PushedDocument>(100);
    private final String connectorName;

    RecordingPusher(String connectorName) {
      this.connectorName = connectorName;
    }

    /* @Override */
    public boolean take(Document document, DocumentStore ignored) {
      pushedDocuments.add(new PushedDocument(document, connectorName));
      return true;
    }

    /* @Override */
    public void flush() {
    }

    /* @Override */
    public void cancel() {
    }

    PushedDocument poll() throws InterruptedException {
      return pushedDocuments.poll(
          SyncingConnector.POLL_TIME_LIMIT_MILLIS, TimeUnit.MILLISECONDS);
    }

    void reset() {
      pushedDocuments.clear();
    }

    @Override
    public String toString() {
      return "Pusher pushed = " + pushedDocuments;
    }
  }

  public static class RecordingLoadManagerFactory implements LoadManagerFactory {
    private RecordingLoadManager loadManager;

    /* @Override */
    public LoadManager newLoadManager(String ignored) {
      if (loadManager == null) {
        loadManager = new RecordingLoadManager();
      }
      return loadManager;
    }
  }

  private static class RecordingLoadManager implements LoadManager {
    int load = 200;
    int batchSize = 3;

    private final BlockingQueue<BatchResult> resultQueue =
        new ArrayBlockingQueue<BatchResult>(10);

    /* @Override */
    public void recordResult(BatchResult batchResult) {
      resultQueue.add(batchResult);
    }

    BatchResult getBatchResult() throws InterruptedException {
      return resultQueue.poll(SyncingConnector.POLL_TIME_LIMIT_MILLIS,
          TimeUnit.MILLISECONDS);
    }

    void reset() {
      resultQueue.clear();
    }

    /* @Override */
    public void setLoad(int load) {
      this.load = load;
    }

    /* @Override */
    public void setPeriod(int period) {
    }

    /* @Override */
    public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
    }

    /* @Override */
    public BatchSize determineBatchSize() {
      int size = Math.min(load, batchSize);
      return new BatchSize(size, size);
    }

    /* @Override */
    public boolean shouldDelay() {
      return false;
    }
  }
}
