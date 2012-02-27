// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util.diffing;

import com.google.enterprise.connector.util.diffing.Change;
import com.google.enterprise.connector.util.diffing.ChangeSource;
import com.google.enterprise.connector.util.diffing.CheckpointAndChange;
import com.google.enterprise.connector.util.diffing.CheckpointAndChangeQueue;
import com.google.enterprise.connector.util.diffing.DeleteDocumentHandleFactory;
import com.google.enterprise.connector.util.diffing.DiffingConnectorCheckpoint;
import com.google.enterprise.connector.util.diffing.MonitorCheckpoint;
import com.google.enterprise.connector.util.diffing.testing.TestDirectoryManager;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Test for {@Link CheckpointAndChangeQueue}.
 */
public class CheckpointAndChangeQueueTest extends TestCase {
  private File persistDir;
  private TestDirectoryManager testDirectoryManager;
  private DeleteDocumentHandleFactory internalFactory;
  private MockDocumentHandleFactory clientFactory;

  @Override
  public void setUp() throws IOException {
    testDirectoryManager = new TestDirectoryManager(this);
    persistDir = testDirectoryManager.makeDirectory("queue");
    internalFactory = new DeleteDocumentHandleFactory();
    clientFactory = new MockDocumentHandleFactory();

    deleteDir(persistDir);
    assertTrue(persistDir.mkdir());
  }

  @Override
  public void tearDown() {
    assertTrue(deleteDir(persistDir));
  }

  private static class MockChangeSource implements ChangeSource {
    Collection<Change> original = new ArrayList<Change>();
    LinkedList<Change> pending = new LinkedList<Change>();
    private static final String PREFIX = "/xx/yy.";

    MockChangeSource(int count) {
      for (int ix = 0; ix < count; ix++) {
        original.add(newChange(ix, PREFIX));
      }
      pending.addAll(original);
    }

    MockChangeSource(Collection<Change> changes) {
      original.addAll(changes);
      pending.addAll(changes);
    }

    static Change newChange(int ix, String monitorName) {
      //TODO add some deletes
      MockDocumentHandle mdh = new MockDocumentHandle(PREFIX + ix,
          "extra_" + monitorName);
      return new Change(Change.FactoryType.CLIENT, mdh,
          new MonitorCheckpoint(monitorName, ix, ix, ix));
    }

    static Change newChange(int ix) {
      return newChange(ix, PREFIX);
    }

    /* @Override */
    public Change getNextChange() {
      return pending.poll();
    }

    static void validateChange(int expected, Change c) {
      String documentId = c.getDocumentHandle().getDocumentId();
      int got = Integer.parseInt(documentId.substring(PREFIX.length()));
      assertEquals(expected, got);
    }
  }

  private static Change newChange(int ix, String monitorName) {
    return MockChangeSource.newChange(ix, monitorName);
  }

  private boolean deleteDir(File dir) {
    if (dir.exists() && dir.isDirectory()) {
      for (File f : dir.listFiles()) {
        if (f.isFile()) {
          f.delete();
        } else {
          deleteDir(f);
        }
      }
      return dir.delete();
    }
    return false;
  }

  /**
   * Tests the normal operation where a sequence of batches are processed to
   * completion in order. The first batch begins with a null checkpoint.
   */
  public void testStartResumeTraversal() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(null);
    String checkpoint = null;
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 2);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 2, 2);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 4, 2);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 0);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 0);
    assertFalse(q.resume(checkpoint).iterator().hasNext());
   }

  /**
   * Tests replaying the first batch which has a null checkpoint and then
   * resuming.
   */
  public void testRetryStartThenResume() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(null);
    String checkpoint = null;
    checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 2);
    // Replay the first batch.
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 2);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 2, 2);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 4, 2);
    assertFalse(q.resume(checkpoint).iterator().hasNext());
  }

  /**
   * Tests retrying a batch after the first one.
   */
  public void testRetryThenResume() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(null);
    String checkpoint = null;
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 2);
    // We call q.resume but do not set checkpoint forward to reflect the
    // changes from the batch.
    checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 2, 2);
    // Since we have not advanced checkpoint we replay the batch.
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 2, 2);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 4, 2);
    assertFalse(q.resume(checkpoint).iterator().hasNext());
  }

  /**
   * Tests resuming from a checkpoint that is half way through the first batch.
   */
  public void testHalfStartBatch() throws IOException {
    ChangeSource changeSource = new MockChangeSource(8);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(3);
    q.start(null);
    Iterator<CheckpointAndChange> it = q.resume(null).iterator();
    CheckpointAndChange checkpointAndChange = it.next();
    MockChangeSource.validateChange(0, checkpointAndChange.getChange());
    assertTrue(it.hasNext());
    checkpointAndChange = it.next();
    MockChangeSource.validateChange(1, checkpointAndChange.getChange());
    assertTrue(it.hasNext());
    String checkpoint = checkChangesAndReturnLastCheckpoint(
        q.resume(checkpointAndChange.getCheckpoint().toString()), 2, 3);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 5, 3);
    assertFalse(q.resume(checkpoint).iterator().hasNext());
  }

  /**
   * Tests resume for a null checkpoint when the {@link ChangeSource} is empty.
   */
  public void testStartWithEmptyChangeSource() throws IOException {
    ChangeSource changeSource = new MockChangeSource(0);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(3);
    q.start(null);
    Iterator<CheckpointAndChange> it = q.resume(null).iterator();
    assertFalse(it.hasNext());
  }

  /**
   * Tests resume for a second checkpoint when the {@link ChangeSource} is empty.
   */
  public void testResumeWithEmptyChangeSource() throws IOException {
    ChangeSource changeSource = new MockChangeSource(2);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(null);
    String checkpoint = null;
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 2);
    Iterator<CheckpointAndChange> it = q.resume(checkpoint).iterator();
    assertFalse(it.hasNext());
  }

  /**
   * Tests resume for a null checkpoint when the {@link ChangeSource} has half
   * the requested changes.
   */
  public void testStartWithPartialChangeSource() throws IOException {
    ChangeSource changeSource = new MockChangeSource(2);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(3);
    q.start(null);
    String checkpoint = null;
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 2);
    Iterator<CheckpointAndChange> it = q.resume(checkpoint).iterator();
    assertFalse(it.hasNext());
  }

  /**
   * Tests resume for a second checkpoint when the {@link ChangeSource} has half
   * the requested changes.
   */
  public void testResumeWithPartialChangeSource() throws IOException {
    ChangeSource changeSource = new MockChangeSource(5);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(3);
    q.start(null);
    String checkpoint = null;
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 3);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 3, 2);
    assertFalse(q.resume(checkpoint).iterator().hasNext());
  }

  public void testRefillChangeSource() throws IOException {
    List<Change> changes =
        Arrays.asList(MockChangeSource.newChange(0),
            MockChangeSource.newChange(1), null,
            MockChangeSource.newChange(2), null, null,
            MockChangeSource.newChange(3),
            MockChangeSource.newChange(4));
    ChangeSource changeSource = new MockChangeSource(changes);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(5);
    q.start(null);
    String checkpoint = null;
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 0, 2);
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 2, 1);
    Iterator<CheckpointAndChange> it = q.resume(checkpoint).iterator();
    assertFalse(it.hasNext());
    checkpoint = checkChangesAndReturnLastCheckpoint(q.resume(checkpoint), 3, 2);
    it = q.resume(checkpoint).iterator();
    assertFalse(it.hasNext());
  }

  private String checkChangesAndReturnLastCheckpoint(List<CheckpointAndChange> list,
      int start, int count) {
    Iterator<CheckpointAndChange> it = list.iterator();
    String result = null;
    for (int ix = 0; ix < count; ix++) {
      CheckpointAndChange checkpointAndChange = it.next();
      result = checkpointAndChange.getCheckpoint().toString();
      MockChangeSource.validateChange(start + ix, checkpointAndChange.getChange());
    }
    assertFalse(it.hasNext());
    return result;
  }

  public void testRecovery() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(null);
    List<CheckpointAndChange> firstBatch = q.resume(null);
    String checkpoint = firstBatch.get(1).getCheckpoint().toString();
    List<CheckpointAndChange> secondBatch = q.resume(checkpoint);

    CheckpointAndChangeQueue q2 = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q2.setMaximumQueueSize(2);
    q2.start(checkpoint);
    List<CheckpointAndChange> secondBatchAgain = q2.resume(checkpoint);
    assertEquals(secondBatch, secondBatchAgain);
  }

  public void testRepeatedRecoveryAtSameCheckpoint() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(null);
    List<CheckpointAndChange> firstBatch = q.resume(null);
    String checkpoint = firstBatch.get(1).getCheckpoint().toString();
    List<CheckpointAndChange> secondBatch = q.resume(checkpoint);

    CheckpointAndChangeQueue q2 = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q2.setMaximumQueueSize(2);
    q2.start(checkpoint);
    List<CheckpointAndChange> secondBatchAgain = q2.resume(checkpoint);
    assertEquals(secondBatch, secondBatchAgain);

    CheckpointAndChangeQueue q3 = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory,clientFactory);
    q3.setMaximumQueueSize(2);
    q3.start(checkpoint);
    List<CheckpointAndChange> secondBatchThrice = q3.resume(checkpoint);
    assertEquals(secondBatch, secondBatchThrice);

    CheckpointAndChangeQueue q4 = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q4.setMaximumQueueSize(2);
    q4.start(checkpoint);
    List<CheckpointAndChange> secondBatchFourthTime = q4.resume(checkpoint);
    assertEquals(secondBatch, secondBatchFourthTime);
  }

  public void testRepeatedResumeAtSameCheckpointOfSameQueue() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(null);
    List<CheckpointAndChange> firstBatch = q.resume(null);
    String checkpoint = firstBatch.get(1).getCheckpoint().toString();
    List<CheckpointAndChange> secondBatch = q.resume(checkpoint);

    CheckpointAndChangeQueue q2 = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q2.setMaximumQueueSize(2);
    q2.start(checkpoint);
    List<CheckpointAndChange> secondBatchAgain = q2.resume(checkpoint);
    assertEquals(secondBatch, secondBatchAgain);

    List<CheckpointAndChange> secondBatchThrice = q2.resume(checkpoint);
    assertEquals(secondBatch, secondBatchThrice);
  }

  public void testPartialRecovery() throws IOException {
    ChangeSource changeSource = new MockChangeSource(10);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(4);
    q.start(null);
    List<CheckpointAndChange> firstBatch = q.resume(null);
    String checkpoint = firstBatch.get(3).getCheckpoint().toString();
    List<CheckpointAndChange> secondBatch = q.resume(checkpoint);
    checkpoint = secondBatch.get(1).getCheckpoint().toString();

    CheckpointAndChangeQueue q2 = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q2.setMaximumQueueSize(4);
    q2.start(checkpoint);
    List<CheckpointAndChange> secondBatchRedoEnd = q2.resume(checkpoint);
    assertEquals(secondBatch.get(2), secondBatchRedoEnd.get(0));
    assertEquals(secondBatch.get(3), secondBatchRedoEnd.get(1));
  }

  public void testRecoveryStateCleanup() throws IOException {
    final int NUM_RESUME_CALLS = 20;
    ChangeSource changeSource = new MockChangeSource(NUM_RESUME_CALLS * 3);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    String checkpoint = null;
    q.start(checkpoint);
    for (int i = 0; i < NUM_RESUME_CALLS; i++) {
       List<CheckpointAndChange> batch = q.resume(checkpoint);
       checkpoint = batch.get(1).getCheckpoint().toString();
       assertTrue(1 >= persistDir.listFiles().length);
    }
    assertTrue(1 == persistDir.listFiles().length);
  }

  public void testTooManyRecoveryFiles() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    {
      File persistFile = new File(persistDir, "recovery.1234");
      FileWriter writer = new FileWriter(persistFile);
      writer.write("omonee-harmony");
      writer.close();
    }
    {
      File persistFile = new File(persistDir, "recovery.90");
      FileWriter writer = new FileWriter(persistFile);
      writer.write("kwami.fitzpatrik");
      writer.close();
    }
    {
      File persistFile = new File(persistDir, "recovery.-123");
      FileWriter writer = new FileWriter(persistFile);
      writer.write("funny\nfarm\nman");
      writer.close();
    }
    q.setMaximumQueueSize(2);
    try {
      q.start(DiffingConnectorCheckpoint.newFirst().toString());
      fail("Should have failed on too many recovery files.");
    } catch(IOException e) {
      assertTrue(-1 != e.getMessage().indexOf("Found too many recovery files: "));
    }
  }

  public void testInvalidRecoveryFilename() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    File persistFile = new File(persistDir, "recovery.sugar-n-spice");
    FileWriter writer = new FileWriter(persistFile);
    writer.write("oh so not relevent");
    writer.close();
    q.setMaximumQueueSize(2);
    try {
      q.start(DiffingConnectorCheckpoint.newFirst().toString());
      fail("Should have failed on invalid recovery filename.");
    } catch(IOException e) {
      assertTrue(-1 != e.getMessage().indexOf("Invalid recovery filename: "));
    }
  }

  public void testRecoveryFromTwoCompleteFiles() throws IOException {
    File persistDirA = testDirectoryManager.makeDirectory("queue-A");
    File persistDirB = testDirectoryManager.makeDirectory("queue-B");

    { /* Make recovery file that finishes 2nd batch. */
      ChangeSource changeSource = new MockChangeSource(6);
      CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
          persistDirA, internalFactory, clientFactory);
      q.setMaximumQueueSize(2);
      q.start(null);
      List<CheckpointAndChange> firstBatch = q.resume(null);
      String checkpoint = firstBatch.get(1).getCheckpoint().toString();
      List<CheckpointAndChange> secondBatch = q.resume(checkpoint);
      checkpoint = secondBatch.get(1).getCheckpoint().toString();
      q.resume(checkpoint);
      File recoveryFile = persistDirA.listFiles()[0];
      recoveryFile.renameTo(new File(persistDir, recoveryFile.getName()));
    }

    List<CheckpointAndChange> secondBatch;
    String checkpoint;
    { /* Make recovery file that finishes 1st batch. */
      ChangeSource changeSource = new MockChangeSource(6);
      CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
          persistDirB, internalFactory, clientFactory);
      q.setMaximumQueueSize(2);
      q.start(null);
      List<CheckpointAndChange> firstBatch = q.resume(null);
      checkpoint = firstBatch.get(1).getCheckpoint().toString();
      secondBatch = q.resume(checkpoint);
      File recoveryFile = persistDirB.listFiles()[0];
      recoveryFile.renameTo(new File(persistDir, recoveryFile.getName()));
    }

    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(checkpoint);
    List<CheckpointAndChange> secondBatchAgain = q.resume(checkpoint);
    assertEquals(secondBatch, secondBatchAgain);
    assertTrue(deleteDir(persistDirA));
    assertTrue(deleteDir(persistDirB));
  }

  public void testRecoveryFromOneCompleteAndOneIncompleteFile() throws IOException {
    File persistDirAux = testDirectoryManager.makeDirectory("queue-aux");

    String checkpoint;
    List<CheckpointAndChange> secondBatch;
    {
      ChangeSource changeSource = new MockChangeSource(6);
      CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
          persistDirAux, internalFactory, clientFactory);
      q.setMaximumQueueSize(2);
      q.start(null);
      List<CheckpointAndChange> firstBatch = q.resume(null);
      checkpoint = firstBatch.get(1).getCheckpoint().toString();
      secondBatch = q.resume(checkpoint);
      File recoveryFile = persistDirAux.listFiles()[0];
      recoveryFile.renameTo(new File(persistDir, recoveryFile.getName()));
    }

    File persistFile = new File(persistDir, "recovery." + System.nanoTime());
    FileWriter writer = new FileWriter(persistFile);
    writer.write("i iZ brokens\nyar\t\t\n?");
    writer.close();

    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(checkpoint);
    List<CheckpointAndChange> secondBatchAgain = q.resume(checkpoint);
    assertEquals(secondBatch, secondBatchAgain);

    assertTrue(deleteDir(persistDirAux));
  }

  public void testRecoveryFromOneIncompleteFileOnly() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    File persistFile = new File(persistDir, "recovery.1234");
    FileWriter writer = new FileWriter(persistFile);
    writer.write("omonee-harmony");
    writer.close();
    q.setMaximumQueueSize(2);
    try {
      q.start(DiffingConnectorCheckpoint.newFirst().toString());
      fail("Should have failed on sole faulty recovery file.");
    } catch(IOException e) {
      assertTrue(-1 != e.getMessage().indexOf("Found incomplete recovery file: "));
    }
  }

  public void testReStart() throws IOException {
    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(null);
    List<CheckpointAndChange> firstBatch = q.resume(null);
    String checkpoint = firstBatch.get(1).getCheckpoint().toString();
    List<CheckpointAndChange> secondBatch = q.resume(checkpoint);
    q.start(null);
    assertTrue(0 == persistDir.listFiles().length);
    List<CheckpointAndChange> firstBatchAgain = q.resume(null);
    assertEquals(firstBatch, firstBatchAgain);
  }

  public void testWithMoreResumeCallsThanFileDescriptors() throws IOException {
    final int NUM_RESUME_CALLS = 1000;
    ChangeSource changeSource = new MockChangeSource(NUM_RESUME_CALLS * 3);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    String checkpoint = null;
    q.start(checkpoint);
    for (int i = 0; i < NUM_RESUME_CALLS; i++) {
       List<CheckpointAndChange> batch = q.resume(checkpoint);
       checkpoint = batch.get(1).getCheckpoint().toString();
       assertTrue(1 >= persistDir.listFiles().length);
    }
    assertTrue(1 == persistDir.listFiles().length);
  }

  public void testTrackingMonitorState() throws IOException {
    final String MON_A = "A Monitor";
    final String MON_B = "I am mon B";
    ChangeSource changeSource = new MockChangeSource(Arrays.asList(new Change[] {
        newChange(0, MON_A), newChange(0, MON_B), newChange(1, MON_A),
        newChange(1, MON_B), newChange(2, MON_B), newChange(2, MON_A)
    }));
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    String checkpoint = null;
    q.start(checkpoint);

    List<CheckpointAndChange> batch = q.resume(checkpoint);
    Map<String, MonitorCheckpoint> monPoints = q.getMonitorRestartPoints();
    assertEquals(2, monPoints.size());
    assertTrue(monPoints.containsKey(MON_A));
    assertTrue(monPoints.containsKey(MON_B));
    assertEquals(0, monPoints.get(MON_A).getOffset1());
    assertEquals(0, monPoints.get(MON_B).getOffset1());
    assertEquals(0, monPoints.get(MON_A).getOffset2());
    assertEquals(0, monPoints.get(MON_B).getOffset2());
    checkpoint = batch.get(0).getCheckpoint().toString();
    batch = q.resume(checkpoint);
    monPoints = q.getMonitorRestartPoints();
    assertEquals(2, monPoints.size());
    assertTrue(monPoints.containsKey(MON_A));
    assertTrue(monPoints.containsKey(MON_B));
    assertEquals(1, monPoints.get(MON_A).getOffset1());
    assertEquals(0, monPoints.get(MON_B).getOffset1());
    assertEquals(1, monPoints.get(MON_A).getOffset2());
    assertEquals(0, monPoints.get(MON_B).getOffset2());
    // Can do it again.
    batch = q.resume(checkpoint);
    monPoints = q.getMonitorRestartPoints();
    assertEquals(2, monPoints.size());
    assertTrue(monPoints.containsKey(MON_A));
    assertTrue(monPoints.containsKey(MON_B));
    assertEquals(1, monPoints.get(MON_A).getOffset1());
    assertEquals(0, monPoints.get(MON_B).getOffset1());
    assertEquals(1, monPoints.get(MON_A).getOffset2());
    assertEquals(0, monPoints.get(MON_B).getOffset2());
  }

  public void testTrackingMoreMonitorStates() throws IOException {
    final String MON_A = "A Monitor";
    final String MON_B = "I am mon B";
    final String MON_C = "C me";
    final String MON_D = "D is for diploma";
    final String MON_E = "Um.....eeee";
    ChangeSource changeSource = new MockChangeSource(Arrays.asList(new Change[] {
        newChange(0, MON_A), newChange(0, MON_B), newChange(1, MON_A),
        newChange(0, MON_C), newChange(0, MON_D), newChange(0, MON_E),
        newChange(1, MON_C), newChange(1, MON_D), newChange(1, MON_E),
        newChange(1, MON_B), newChange(2, MON_B), newChange(2, MON_A),
        newChange(2, MON_E), newChange(2, MON_C), newChange(2, MON_D),
    }));
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(15);
    String checkpoint = null;
    q.start(checkpoint);
    List<CheckpointAndChange> batch = q.resume(checkpoint);
    Map<String, MonitorCheckpoint> monPoints = q.getMonitorRestartPoints();
    assertEquals(5, monPoints.size());
    assertTrue(monPoints.containsKey(MON_A));
    assertTrue(monPoints.containsKey(MON_B));
    assertTrue(monPoints.containsKey(MON_C));
    assertTrue(monPoints.containsKey(MON_D));
    assertTrue(monPoints.containsKey(MON_E));
  }

  public void testRecoveryOfMonitorState() throws IOException {
    File persistDirSeed = testDirectoryManager.makeDirectory("queue-to-stop");
    Map<String, MonitorCheckpoint> monPoints = null;

    String checkpoint = null;
    { /* Make recovery file that finishes 2nd batch. */
      ChangeSource changeSource = new MockChangeSource(6);
      CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
          persistDirSeed, internalFactory, clientFactory);
      q.setMaximumQueueSize(2);
      q.start(checkpoint);
      List<CheckpointAndChange> firstBatch = q.resume(checkpoint);
      checkpoint = firstBatch.get(1).getCheckpoint().toString();
      List<CheckpointAndChange> secondBatch = q.resume(checkpoint);
      checkpoint = secondBatch.get(1).getCheckpoint().toString();
      q.resume(checkpoint);
      monPoints = q.getMonitorRestartPoints();
      File recoveryFiles[] = persistDirSeed.listFiles();
      assertEquals(1, recoveryFiles.length);
      File recoveryFile = recoveryFiles[0];
      recoveryFile.renameTo(new File(persistDir, recoveryFile.getName()));
    }

    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(checkpoint);
    // Note: It's important that q.resume(checkpoint) is not called.
    // That is start(checkpoint) loads the persisted monitor state.
    assertEquals(monPoints, q.getMonitorRestartPoints());
  }

  public void testRecoveryOfMonitorStateInPartialResume() throws IOException {
    File persistDirSeed = testDirectoryManager.makeDirectory("queue-to-stop");
    Map<String, MonitorCheckpoint> monPoints = null;

    String checkpoint = null;
    { /* Make recovery file that finishes 2nd batch. */
      ChangeSource changeSource = new MockChangeSource(6);
      CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
          persistDirSeed, internalFactory, clientFactory);
      q.setMaximumQueueSize(2);
      q.start(checkpoint);
      List<CheckpointAndChange> firstBatch = q.resume(checkpoint);
      checkpoint = firstBatch.get(1).getCheckpoint().toString();
      List<CheckpointAndChange> secondBatch = q.resume(checkpoint);
      checkpoint = secondBatch.get(0).getCheckpoint().toString();
      q.resume(checkpoint);
      monPoints = q.getMonitorRestartPoints();
      File recoveryFiles[] = persistDirSeed.listFiles();
      assertEquals(1, recoveryFiles.length);
      File recoveryFile = recoveryFiles[0];
      recoveryFile.renameTo(new File(persistDir, recoveryFile.getName()));
    }

    ChangeSource changeSource = new MockChangeSource(6);
    CheckpointAndChangeQueue q = new CheckpointAndChangeQueue(changeSource,
        persistDir, internalFactory, clientFactory);
    q.setMaximumQueueSize(2);
    q.start(checkpoint);
    // Note: It's important that q.resume(checkpoint) is not called.
    // That is start(checkpoint) loads the persisted monitor state.
    assertEquals(monPoints, q.getMonitorRestartPoints());
  }

  public void testCompareRecoveryFilesWithoutMillis() throws IOException {
    CheckpointAndChangeQueue.RecoveryFile rfA
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.123");
    CheckpointAndChangeQueue.RecoveryFile rfB 
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.231");
    CheckpointAndChangeQueue.RecoveryFile rfC 
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.312");
    assertTrue(rfA.isOlder(rfB));
    assertTrue(rfB.isOlder(rfC));
    assertFalse(rfC.isOlder(rfA));
  }

  public void testCompareRecoveryFilesWithMillis() throws IOException {
    CheckpointAndChangeQueue.RecoveryFile rfA
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.123_50");
    CheckpointAndChangeQueue.RecoveryFile rfB 
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.231_20");
    CheckpointAndChangeQueue.RecoveryFile rfC 
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.123_60");
    assertTrue(rfA.isOlder(rfB));
    assertFalse(rfB.isOlder(rfC));
    assertFalse(rfC.isOlder(rfA));
    assertTrue(rfA.isOlder(rfC));
  }

  public void testCompareRecoveryFilesXorMillis() throws IOException {
    CheckpointAndChangeQueue.RecoveryFile rfA
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.123_50");
    CheckpointAndChangeQueue.RecoveryFile rfB 
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.231_20");
    CheckpointAndChangeQueue.RecoveryFile rfC 
        = new CheckpointAndChangeQueue.RecoveryFile("recovery.321");
    assertTrue(rfC.isOlder(rfA));
    assertTrue(rfC.isOlder(rfB));
    assertFalse(rfA.isOlder(rfC));
    assertFalse(rfB.isOlder(rfC));
  }
}
