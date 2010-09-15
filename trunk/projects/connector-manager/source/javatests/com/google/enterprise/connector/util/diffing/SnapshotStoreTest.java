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

import com.google.enterprise.connector.util.diffing.DocumentSnapshot;
import com.google.enterprise.connector.util.diffing.MonitorCheckpoint;
import com.google.enterprise.connector.util.diffing.SnapshotReader;
import com.google.enterprise.connector.util.diffing.SnapshotReaderException;
import com.google.enterprise.connector.util.diffing.SnapshotStore;
import com.google.enterprise.connector.util.diffing.SnapshotStoreException;
import com.google.enterprise.connector.util.diffing.SnapshotWriter;
import com.google.enterprise.connector.util.diffing.SnapshotWriterException;
import com.google.enterprise.connector.util.diffing.testing.TestDirectoryManager;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 */
public class SnapshotStoreTest extends TestCase {
  private File snapshotDir;
  private SnapshotStore store;

  @Override
  public void setUp() throws Exception {
    TestDirectoryManager testDirectoryManager = new TestDirectoryManager(this);
    File rootDir = testDirectoryManager.makeDirectory("rootDir");
    snapshotDir = new File(rootDir, "snapshots");
    store = new SnapshotStore(new File(rootDir, "snapshots"),
        new MockDocumentSnapshotFactory());
  }

  /**
   * Make sure that if the store contains no snapshots, an initial empty
   * snapshot is returned.
   *
   * @throws SnapshotReaderException
   */
  public void testEmptyDir() throws SnapshotStoreException {
    SnapshotReader in = store.openMostRecentSnapshot();
    assertNull(in.read());
  }

  /**
   * Make sure that if we write a snapshot and immediately read it, the contents
   * are correct.
   *
   * @throws SnapshotStoreException
   */
  public void testWriteRead() throws Exception {
    SnapshotWriter out = store.openNewSnapshotWriter();

    MockDocumentSnapshot before = new MockDocumentSnapshot("0", "extra.0");
    out.write(before);

    SnapshotReader in = store.openMostRecentSnapshot();
    DocumentSnapshot after = in.read();
    assertEquals(before, after);
    assertNull(in.read());
    store.close(in, out);
  }

  /**
   * Make sure that openMostRecentSnapshot always opens the most recent
   * snapshot.
   *
   * @throws SnapshotWriterException
   * @throws SnapshotReaderException
   */
  public void testSnapshotSorting() throws Exception {
    for (int k = 0; k < 10; ++k) {
      SnapshotWriter out = store.openNewSnapshotWriter();
      String path = String.format("/foo/bar/%d", k);
      DocumentSnapshot before = new MockDocumentSnapshot(Integer.toString(k),
          "extra." + k);
      out.write(before);

      SnapshotReader in = store.openMostRecentSnapshot();
      DocumentSnapshot after = in.read();
      assertEquals(before, after);
      store.close(in, out);
    }
  }

  /**
   * Make sure that after a bunch of snapshots are created, only the last three
   * remain.
   *
   * @throws SnapshotWriterException
   */
  public void testGarbageCollection() throws Exception {
    for (int k = 0; k < 10; ++k) {
      SnapshotWriter out = store.openNewSnapshotWriter();
      long readSnapshotNum = Math.max(0, k - 1);
      MonitorCheckpoint cp = new MonitorCheckpoint("foo", readSnapshotNum, 2, 1);
      store.acceptGuarantee(cp);
      store.close(null, out);
      store.deleteOldSnapshots();
    }
    File[] contents = snapshotDir.listFiles();
    for (File f : contents) {
      if (f.isHidden()) {
        // Special ".isTestDir" marker file; ignore
        continue;
      }
      assertTrue(f.getName(), f.getName().matches("snap\\.(8|9|10)"));
    }
  }

  /**
   * Make sure that a new SnapshotStore recovers correctly from checkpoints.
   *
   * @throws IOException
   * @throws SnapshotWriterException
   * @throws SnapshotReaderException
   */
  // TODO: add more recovery tests.
  public void testRecoveryBasics() throws IOException, SnapshotStoreException,
      InterruptedException {
    // Create the first snapshot with modification time 12345.
    SnapshotWriter ss1 = store.openNewSnapshotWriter();
    writeRecords(ss1, "12345");
    store.close(null, ss1);

    // Create a second snapshot with the same files, but modification time
    // 23456.
    SnapshotWriter ss2 = store.openNewSnapshotWriter();
    writeRecords(ss2, "23456");
    store.close(null, ss2);

    // Now pretend that the file-system monitor has scanned the first 7 records
    // from each snapshot and emitted changes for them. I.e., create a
    // checkpoint
    // as if the first 7 changes have been sent to the GSA.
    MonitorCheckpoint cp = new MonitorCheckpoint("foo", 1, 7, 7);

    SnapshotStore.stitch(snapshotDir, cp, new MockDocumentSnapshotFactory());
    SnapshotStore after = new SnapshotStore(snapshotDir,
        new MockDocumentSnapshotFactory());
    SnapshotReader reader = after.openMostRecentSnapshot();
    assertEquals(3, reader.getSnapshotNumber());

    // Snapshot should contain the first 7 records from snapshot 2 and the rest
    // from snapshot 1.
    for (int k = 0; k < 100; ++k) {
      DocumentSnapshot rec = reader.read();
      assertNotNull(rec);
      String suffix = (k < 7) ? "23456" : "12345";
      assertTrue(rec.getDocumentId().endsWith(suffix));
    }
    store.close(reader, null);
  }
  public void testStitchWithIntterupt() throws Exception {
    // Create the first snapshot with modification time 12345.
    SnapshotWriter ss1 = store.openNewSnapshotWriter();
    writeRecords(ss1, "12345");
    store.close(null, ss1);

    // Create a second snapshot with the same files, but modification time
    // 23456.
    SnapshotWriter ss2 = store.openNewSnapshotWriter();
    writeRecords(ss2, "23456");
    store.close(null, ss2);

    // Now pretend that the file-system monitor has scanned the first 7 records
    // from each snapshot and emitted changes for them. I.e., create a
    // checkpoint
    // as if the first 7 changes have been sent to the GSA.
    MonitorCheckpoint cp = new MonitorCheckpoint("foo", 1, 7, 7);
    try {
      Thread.currentThread().interrupt();
      SnapshotStore.stitch(snapshotDir, cp,
          new MockDocumentSnapshotFactory());
      fail();
    } catch (InterruptedException ie) {
      // Expected.
    } finally {
      assertFalse(Thread.interrupted());
    }

    SnapshotStore after = new SnapshotStore(snapshotDir,
        new MockDocumentSnapshotFactory());
    // Verify stitch did not create a new snapshot.
    SnapshotReader reader = after.openMostRecentSnapshot();
    assertEquals(2, reader.getSnapshotNumber());
  }


  /**
   * Write 100 records to {@code writer} with the specified {@code lastModified}
   * time.
   *
   * @param writer
   * @param suffix
   * @throws SnapshotWriterException
   */
  private void writeRecords(SnapshotWriter writer, String suffix)
      throws SnapshotWriterException {
    for (int k = 0; k < 100; ++k) {
      String path = String.format("/foo/bar/%d", k);
      DocumentSnapshot rec = new MockDocumentSnapshot(k + "."
          + suffix, "extra." + "k");
      writer.write(rec);
    }
  }

  public void testTwoWriters() throws SnapshotStoreException {
    store.openNewSnapshotWriter();
    try {
      store.openNewSnapshotWriter();
      fail("opened second writer");
    } catch (IllegalStateException expected) {
      assertEquals(expected.getMessage(), "There is already an active writer.");
    }
  }
}
