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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An API for storing and retrieving snapshots.
 *
 * @since 2.8
 */
public class SnapshotStore {
  private static final Logger LOG =
      Logger.getLogger(SnapshotStore.class.getName());

  private static File getSnapshotFile(File snapshotDir, long snapshotNumber) {
    String name = String.format("snap.%d", snapshotNumber);
    return new File(snapshotDir, name);
  }

  private static SnapshotWriter getSnapshotWriter(File snapshotFile)
      throws IOException, SnapshotWriterException {
    FileOutputStream os = new FileOutputStream(snapshotFile);
    Writer w = new OutputStreamWriter(os, Charsets.UTF_8);
    return new SnapshotWriter(w, os.getFD(), snapshotFile.getAbsolutePath());
  }

  private static final Pattern SNAPSHOT_PATTERN =
      Pattern.compile("snap.([0-9]*)");
  private final File snapshotDir;
  private final DocumentSnapshotFactory documentSnapshotFactory;

  // Whether there is a current writer or not.
  private boolean aWriterIsActive = false;

  protected volatile long oldestSnapshotToKeep;

  /**
   * @param snapshotDirectory the directory in which to store the snapshots.
   *        Must be non-{@code null}. If it does not exist, it will be created.
   * @param documentSnapshotFactory factory for creating DocumentSnapshots
   * @throws SnapshotStoreException if the snapshot directory does not exist and
   *         cannot be created
   */
  public SnapshotStore(File snapshotDirectory,
      DocumentSnapshotFactory documentSnapshotFactory)
      throws SnapshotStoreException {
    Preconditions.checkNotNull(snapshotDirectory);
    if (!snapshotDirectory.exists()) {
      if (!snapshotDirectory.mkdirs()) {
        throw new SnapshotStoreException("failed to create snapshot directory: "
            + snapshotDirectory.getAbsolutePath());
      }
    }
    this.snapshotDir = snapshotDirectory;
    this.documentSnapshotFactory = documentSnapshotFactory;
    this.oldestSnapshotToKeep = 0;
  }

  /**
   * @return a writer for the next snapshot
   * @throws SnapshotStoreException
   */
  public SnapshotWriter openNewSnapshotWriter() throws SnapshotStoreException {
    if (aWriterIsActive) {
      throw new IllegalStateException("There is already an active writer.");
    }
    SortedSet<Long> snapshots = getExistingSnapshots();
    long nextIndex = (snapshots.isEmpty()) ? 1 : snapshots.first() + 1;
    File out = getSnapshotFile(snapshotDir, nextIndex);
    try {
      SnapshotWriter writer = getSnapshotWriter(out);
      aWriterIsActive = true;
      return writer;
    } catch (IOException e) {
      throw new SnapshotStoreException("failed to open snapshot: " + out.getAbsolutePath(), e);
    }
  }

  /**
   * @return the most recent snapshot. If no snapshot is available, return an
   *         empty snapshot.
   * @throws SnapshotStoreException
   */
  public SnapshotReader openMostRecentSnapshot() throws SnapshotStoreException {
    SnapshotReader result;
    for (long snapshotNumber : getExistingSnapshots()) {
      try {
        result = openSnapshot(snapshotDir, snapshotNumber,
            documentSnapshotFactory);
        LOG.fine("opened snapshot: " + snapshotNumber);
        return result;
      } catch (SnapshotReaderException e) {
        // TODO: Account for these failures letting code below run.
        LOG.log(Level.WARNING, "failed to open snapshot file: "
            + getSnapshotFile(snapshotDir, snapshotNumber), e);
      }
    }

    // Create a snapshot that has no records at all.
    LOG.info("starting with empty snapshot");
    File out = getSnapshotFile(snapshotDir, 0);
    try {
      SnapshotWriter writer = getSnapshotWriter(out);
      writer.close();
    } catch (IOException e) {
      throw new SnapshotStoreException("failed to open snapshot: " + out.getAbsolutePath(), e);
    }

    return openMostRecentSnapshot();
  }

  /**
   * @return sorted set of all available snapshots
   */
  private static SortedSet<Long> getExistingSnapshots(File snapshotDirectory) {
    TreeSet<Long> result =
        new TreeSet<Long>(Ordering.<Long>natural().reverse());
    File[] files = snapshotDirectory.listFiles();
    if (files != null) {
      for (File f : files) {
        Matcher m = SNAPSHOT_PATTERN.matcher(f.getName());
        if (m.matches()) {
          result.add(Long.valueOf(m.group(1)));
        }
      }
    }
    return result;
  }

  private SortedSet<Long> getExistingSnapshots() {
    return getExistingSnapshots(snapshotDir);
  }

  @VisibleForTesting
  public void deleteOldSnapshots() {
    // Leave at least two snapshot files, even if oldestSnapshotToKeep
    // is too high.
    for (long k : Iterables.skip(getExistingSnapshots(), 2)) {
      if (k < oldestSnapshotToKeep) {
        File x = getSnapshotFile(snapshotDir, k);
        if (x.delete()) {
          LOG.fine("deleting snapshot file " + x.getAbsolutePath());
        } else {
          LOG.warning("failed to delete snapshot file " + x.getAbsolutePath());
        }
      }
    }
  }

  /**
   * Internal method.
   *
   * @deprecated This method will be removed in a future release
   */
  @Deprecated
  public long getOldestSnapsotToKeep() {
    return oldestSnapshotToKeep;
  }

  void close(SnapshotReader reader, SnapshotWriter writer)
      throws IOException, SnapshotStoreException, SnapshotWriterException {
    try {
      if (reader != null) {
        reader.close();
        reader = null;
      }
    } finally {  // Make sure to try to close writer too.
      if (aWriterIsActive) {
        if (writer != null) {
          writer.close();
          writer = null;
        }
        aWriterIsActive = false;
      }
    }
  }

  /**
   * @param number
   * @return a snapshot reader for snapshot {@code number}
   * @throws SnapshotStoreException
   */
  private static SnapshotReader openSnapshot(File snapshotDir, long number,
      DocumentSnapshotFactory documentSnapshotFactory)
      throws SnapshotStoreException {
    File input = getSnapshotFile(snapshotDir, number);
    try {
      InputStream is = new FileInputStream(input);
      Reader r = new InputStreamReader(is, Charsets.UTF_8);
      return new SnapshotReader(new BufferedReader(r), input.getAbsolutePath(),
          number, documentSnapshotFactory);
    } catch (FileNotFoundException e) {
      throw new SnapshotStoreException("failed to open snapshot: " + number);
    }
  }

  void acceptGuarantee(MonitorCheckpoint cp) {
    long readSnapshotNumber = cp.getSnapshotNumber();
    if (readSnapshotNumber < 0) {
      throw new IllegalArgumentException("Received invalid snapshot in: " + cp);
    }
    if (oldestSnapshotToKeep > readSnapshotNumber) {
      LOG.warning("Received an older snapshot than " + oldestSnapshotToKeep + ": " + cp);
    } else {
      oldestSnapshotToKeep = readSnapshotNumber;
    }
  }

  private static void handleInterrupt() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
  }

  @VisibleForTesting
  public static void stitch(File snapshotDir, MonitorCheckpoint checkpoint,
      DocumentSnapshotFactory documentSnapshotFactory)
      throws IOException, SnapshotStoreException, InterruptedException {
    long readSnapshotIndex = checkpoint.getSnapshotNumber();
    long writeSnapshotIndex = readSnapshotIndex + 1;
    boolean listSnapshotDir = false;
    for (long snapshotIndex : getExistingSnapshots(snapshotDir)) {
      handleInterrupt();
      if (snapshotIndex > writeSnapshotIndex) {
        File snapshotFile = getSnapshotFile(snapshotDir, snapshotIndex); 
        if (snapshotFile.delete()) {
          LOG.info("Deleted snapshot # " + snapshotIndex + ".");
        } else {
          //TODO : find a better solution for scenarios where connector can't 
          // delete the snapshot file.
          LOG.severe("Couldn't delete: " + snapshotFile);
          listSnapshotDir = true;
        }
      }
    }
    // If connector can't delete the old snapshot files,
    // then following information is logged to get more information.
    if (listSnapshotDir) {
      LOG.info("Connector couldn't delete one or more old snapshot files; listing snapshot directory for more information");
      logSnapshotDirectoryDetails(snapshotDir);
    }
    
    long recoveryFileIndex = checkpoint.getSnapshotNumber() + 2;
    File out = getSnapshotFile(snapshotDir, recoveryFileIndex);
    boolean iMadeIt = false;
    SnapshotWriter writer = getSnapshotWriter(out);
      try {
      SnapshotReader part1 = openSnapshot(snapshotDir,
          checkpoint.getSnapshotNumber() + 1, documentSnapshotFactory);
      try {
        for (long k = 0; k < checkpoint.getOffset2(); ++k) {
          handleInterrupt();
          DocumentSnapshot rec = part1.read();
          if (rec == null) {
            break;
          }
          writer.write(rec);
        }
      } finally {
        part1.close();
      }
      SnapshotReader part2 = openSnapshot(snapshotDir,
          checkpoint.getSnapshotNumber(), documentSnapshotFactory);
      try {
        part2.skipRecords(checkpoint.getOffset1());
        DocumentSnapshot rec = part2.read();
        while (rec != null) {
          handleInterrupt();
          writer.write(rec);
          rec = part2.read();
        }
      } finally {
        part2.close();
      }
      iMadeIt = true;
    } finally {
      writer.close();
    }
  }

  /**
   * Logs the details of snapshot directory 
   * @param snapshotDir snapshot directory
   */
  private static void logSnapshotDirectoryDetails(File snapshotDir) {
    if (!snapshotDir.exists()) {
      LOG.severe("Snapshot directory does not exist : " + snapshotDir.getPath());
    } else {
      LOG.info("Trying to list the contents of snapshot directory: " + snapshotDir);
      File [] fileArray = snapshotDir.listFiles();
      if (fileArray == null) {
        LOG.severe("Connector couldn't list the files in the snapshot directory : " + snapshotDir.getPath());
      } else {
        LOG.info("Number of files present in snapshot directory is: " + fileArray.length);
        LOG.info("Files present in the snapshot directory: " + Arrays.asList(fileArray));
      }
    }
  }

  File getDirectory() {
    return snapshotDir;
  }
}
