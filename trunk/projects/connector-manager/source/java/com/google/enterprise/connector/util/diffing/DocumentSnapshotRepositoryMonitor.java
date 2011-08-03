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
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service that monitors a {@link SnapshotRepository} and makes callbacks
 * when changes occur.
 * <p/>
 * This implementation works as follows. It repeatedly scans all the
 * {@link DocumentSnapshot} entries returned by
 * {@link SnapshotRepository#iterator()}. On each pass, it compares the current
 * contents of the repository to a record of what it saw on the previous pass.
 * The record is stored as a file in the local file system. Each discrepancy
 * is propagated to the client.
 * <p/>
 * Using a local snapshot of the file system has some serious flaws for
 * continuous crawl:
 * <ul>
 * <li>The local snapshot can diverge from the actual contents of the GSA. This
 * can lead to situations where discrepancies are not corrected.</li>
 * <li>If the local snapshot gets corrupted, there is no way to recover short of
 * deleting all on the GSA and starting again.</li>
 * </ul>
 * A much more robust solution is to obtain snapshots directly from the GSA
 * at least part of the time. (However, to save bandwidth, it may still be
 * useful to keep local snapshots and only get an "authoritative" snapshot
 * from the cloud occasionally. E.g., once a week or if the local snapshot
 * is corrupted.)
 * <p/>
 * When an API to do that is available, this implementation should be fixed
 * to use it.
 *
 * @since 2.8
 */
// TODO: Retrieve authoritative snapshots from GSA when appropriate.
public class DocumentSnapshotRepositoryMonitor implements Runnable {
  private static final Logger LOG = Logger.getLogger(
      DocumentSnapshotRepositoryMonitor.class.getName());

  /*
   * Gross hack uses Java Reflection to setup and teardown NDC logging context.
   * This avoids connector-spi.jar having a compile-time or run-time dependency
   * on connector-logging.jar.
   */
  private static Method ndcPush = null;
  private static Method ndcRemove = null;

  static {
    initNdcLogging();
  }

  /* Extracted from the above static block to suppress the unchecked warning. */
  @SuppressWarnings("unchecked")
  private static void initNdcLogging() {
    try {
      Class ndc = Class.forName("com.google.enterprise.connector.logging.NDC");
      ndcPush = ndc.getMethod("push", String.class);
      ndcRemove = ndc.getMethod("remove", (Class []) null);
    } catch (LinkageError ignored) {
    } catch (ClassNotFoundException ignored) {
    } catch (NoSuchMethodException ignored) {
    } catch (SecurityException ignored) {
    }
  }

  /* Call an NDC method via reflection, if possible. */
  private static void invoke(Method method, Object... args) {
    if (method != null) {
      try {
        method.invoke(null, args);
      } catch (LinkageError ignored) {
      } catch (IllegalAccessException ignored) {
      } catch (IllegalArgumentException ignored) {
      } catch (InvocationTargetException ignored) {
      }
    }
  }

  /**
   * The client provides an implementation of this interface to receive
   * notification of changes to the file system.
   */
  public static interface Callback {
    public void passBegin() throws InterruptedException;

    public void newDocument(DocumentHandle documentHandle,
        MonitorCheckpoint mcp) throws InterruptedException;

    public void deletedDocument(DocumentHandle documentHandle,
        MonitorCheckpoint mcp) throws InterruptedException;

    public void changedDocument(DocumentHandle documentHandle,
        MonitorCheckpoint mcp) throws InterruptedException;

    public void passComplete(MonitorCheckpoint mcp) throws InterruptedException;

    public boolean hasEnqueuedAtLeastOneChangeThisPass();
  }

  /** Directory that contains snapshots. */
  private final SnapshotStore snapshotStore;

  /** The root of the file system to monitor */
  private final SnapshotRepository<? extends DocumentSnapshot> query;

  /** Reader for the current snapshot. */
  private SnapshotReader snapshotReader;

  /** Callback to invoke when a change is detected. */
  private final Callback callback;

  /** Current record from the snapshot. */
  private DocumentSnapshot current;

  /** The snapshot we are currently writing */
  private SnapshotWriter snapshotWriter;

  private final String name;

  private final DocumentSnapshotFactory documentSnapshotFactory;

  private final DocumentSink documentSink;

  /* Contains a checkpoint confirmation from CM. */
  private MonitorCheckpoint guaranteeCheckpoint;

  /**
   * Creates a DocumentSnapshotRepositoryMonitor that monitors the
   * Repository rooted at {@code root}.
   *
   * @param name the name of this monitor (a hash of the start path)
   * @param query query for files
   * @param snapshotStore where snapshots are stored
   * @param callback client callback
   * @param documentSink destination for filtered out file info
   * @param initialCp checkpoint when system initiated, could be {@code null}
   * @param documentSnapshotFactory for un-serializing
   *        {@link DocumentSnapshot} objects.
   */
  public DocumentSnapshotRepositoryMonitor(String name,
      SnapshotRepository<? extends DocumentSnapshot> query,
      SnapshotStore snapshotStore, Callback callback,
      DocumentSink documentSink, MonitorCheckpoint initialCp,
      DocumentSnapshotFactory documentSnapshotFactory) {
    this.name = name;
    this.query = query;
    this.snapshotStore = snapshotStore;
    this.callback = callback;
    this.documentSnapshotFactory = documentSnapshotFactory;
    this.documentSink = documentSink;
    guaranteeCheckpoint = initialCp;
  }

  /**
   * @return a current checkpoint for this monitor.
   */
  private MonitorCheckpoint getCheckpoint(long readerDelta) {
    long snapNum = snapshotReader.getSnapshotNumber();
    long readRecNum = snapshotReader.getRecordNumber() + readerDelta;
    if (readRecNum < 0) {
      readRecNum = 0;
    }
    long writeRecNum = snapshotWriter.getRecordCount();
    return new MonitorCheckpoint(name, snapNum, readRecNum, writeRecNum);
  }

  private MonitorCheckpoint getCheckpoint() {
    return getCheckpoint(0);
  }

  /* @Override */
  public void run() {
    // Call NDC.push() via reflection, if possible.
    invoke(ndcPush, "Monitor " + name);
    try {
      while (true) {
        tryToRunForever();
        // TODO: Remove items from this monitor that are in queues.
        // Watch out for race conditions. The queues are potentially
        // giving docs to CM as bad things happen in monitor.
        // This TODO would be mitigated by a reconciliation with GSA.
        performExceptionRecovery();
      }
    } catch (InterruptedException ie) {
      LOG.info("Repository Monitor " + name + " received stop signal.");
    } finally {
      // Call NDC.remove() via reflection, if possible.
      invoke(ndcRemove);
    }
  }

  private void tryToRunForever() throws InterruptedException {
    try {
      while (true) {
        doOnePass();
      }
    } catch (SnapshotWriterException e) {
      String msg = "Failed to write to snapshot file: " + snapshotWriter.getPath();
      LOG.log(Level.SEVERE, msg, e);
    } catch (SnapshotReaderException e) {
      String msg = "Failed to read snapshot file: " + snapshotReader.getPath();
      LOG.log(Level.SEVERE, msg, e);
    } catch (SnapshotStoreException e) {
      String msg = "Problem with snapshot store.";
      LOG.log(Level.SEVERE, msg, e);
    } catch (SnapshotRepositoryRuntimeException e) {
      String msg = "Failed reading repository.";
      LOG.log(Level.SEVERE, msg, e);
    }
  }

  /**
   * Call in situations were DocumentSnapshotRepositoryMonitor runs were
   * interfered with and we wish to have the DocumentSnapshotRepositoryMonitor
   * continue running. Brings system into state where doOnePass can be invoked.
   * Failures in this method are considered fatal for the thread.
   *
   * @throws IllegalStateException if recovery fails.
   * @throws InterruptedException if the calling thread is interrupted.
   */
  private void performExceptionRecovery() throws InterruptedException,
      IllegalStateException {
    // Try to close potentially opened snapshot files.
    try {
      snapshotStore.close(snapshotReader, snapshotWriter);
      LOG.info("Repository Monitor " + name + " closed faulty reader and writer.");
    } catch (IOException e) {
      String msg = "Repository Monitor " + name + " failed clean up .";
      LOG.log(Level.SEVERE, msg, e);
      throw new IllegalStateException(msg, e);
    } catch (SnapshotStoreException e) {
      String msg = "Repository Monitor " + name + " failed clean up .";
      LOG.log(Level.SEVERE, msg, e);
      throw new IllegalStateException(msg, e);
    }

    if (null == guaranteeCheckpoint) {
      // This monitor was started without state; that is from scratch.
      // TODO: Consider deleting all snapshot state and starting again.
      String msg = "Repository Monitor " + name + " could not start correctly.";
      LOG.severe(msg);
      throw new IllegalStateException(msg);
    } else {
      try {
        SnapshotStore.stitch(snapshotStore.getDirectory(), guaranteeCheckpoint,
            documentSnapshotFactory);
        LOG.info("Repository Monitor " + name + " restiched snapshot.");
      } catch (IOException e) {
        String msg = "Repository Monitor " + name + " has failed and stopped.";
        LOG.log(Level.SEVERE, msg, e);
        throw new IllegalStateException(msg, e);
      } catch (SnapshotStoreException e) {
        String msg = "Repository Monitor " + name + " failed fixing store.";
        LOG.log(Level.SEVERE, msg, e);
        throw new IllegalStateException(msg, e);
      }
    }
  }

  /**
   * Makes one pass through the file system, notifying {@code visitor} of any
   * changes.
   *
   * @throws InterruptedException
   */
  private void doOnePass() throws SnapshotStoreException,
      InterruptedException {
    callback.passBegin();
    try {
      // Open the most recent snapshot and read the first record.
      this.snapshotReader = snapshotStore.openMostRecentSnapshot();
      current = snapshotReader.read();

      // Create an snapshot writer for this pass.
      this.snapshotWriter = snapshotStore.openNewSnapshotWriter();

      for(DocumentSnapshot ss : query) {
        if (Thread.currentThread().isInterrupted()) {
          throw new InterruptedException();
        }
        processDeletes(ss);
        safelyProcessDocumentSnapshot(ss);
      }
      // Take care of any trailing paths in the snapshot.
      processDeletes(null);

    } finally {
      try {
        snapshotStore.close(snapshotReader, snapshotWriter);
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Failed closing snapshot reader and writer.", e);
        // Try to proceed anyway.  Weird they are not closing.
      }
    }
    if (current != null) {
      throw new IllegalStateException(
          "Should not finish pass until entire read snapshot is consumed.");
    }
    callback.passComplete(getCheckpoint(-1));
    snapshotStore.deleteOldSnapshots();
    if (!callback.hasEnqueuedAtLeastOneChangeThisPass()) {
      // No monitor checkpoints from this pass went to queue because
      // there were no changes, so we can delete the snapshot we just wrote.
      new java.io.File(snapshotWriter.getPath()).delete();
      // TODO: Check return value; log trouble.
    }
    snapshotWriter = null;
    snapshotReader = null;
  }

  /**
   * Process snapshot entries as deletes until {@code current} catches up with
   * {@code documentSnapshot}. Or, if {@code documentSnapshot} is {@code null},
   * process all remaining snapshot entries as deletes.
   *
   * @param documentSnapshot where to stop
   * @throws SnapshotReaderException
   * @throws InterruptedException
   */
  private void processDeletes(DocumentSnapshot documentSnapshot)
      throws SnapshotReaderException, InterruptedException {
    while (current != null
        && (documentSnapshot == null
            || documentSnapshot.getDocumentId().compareTo(
                current.getDocumentId()) > 0)) {
      callback.deletedDocument(
          new DeleteDocumentHandle(current.getDocumentId()), getCheckpoint());
      current = snapshotReader.read();
    }
  }

  private void safelyProcessDocumentSnapshot(DocumentSnapshot snapshot)
      throws InterruptedException, SnapshotReaderException,
      SnapshotWriterException {
    try {
      processDocument(snapshot);
    } catch (RepositoryException re) {
      //TODO Log the exception or its message? in document sink perhaps.
      documentSink.add(snapshot.getDocumentId(), FilterReason.IO_EXCEPTION);
    }
  }

  /**
   * Processes a document found in the document repository.
   *
   * @param documentSnapshot
   * @throws RepositoryException
   * @throws InterruptedException
   * @throws SnapshotReaderException
   * @throws SnapshotWriterException
   */
  private void processDocument(DocumentSnapshot documentSnapshot)
      throws InterruptedException, RepositoryException, SnapshotReaderException,
          SnapshotWriterException {
    // At this point 'current' >= 'file', or possibly current == null if
    // we've processed the previous snapshot entirely.
    if (current != null
        && (documentSnapshot.getDocumentId().compareTo(
            current.getDocumentId()) == 0)) {
      processPossibleChange(documentSnapshot);
    } else {
      // This file didn't exist during the previous scan.
      DocumentHandle documentHandle  = documentSnapshot.getUpdate(null);
      snapshotWriter.write(documentSnapshot);

      // Null if filtered due to mime-type.
      if (documentHandle != null) {
        callback.newDocument(documentHandle, getCheckpoint(-1));
      }
    }
  }

  /**
   * Processes a document found in the document repository that also appeared
   * in the previous scan. Determines whether the document has changed,
   * propagates changes to the client and writes the snapshot record.
   *
   * @param documentSnapshot
   * @throws RepositoryException
   * @throws InterruptedException
   * @throws SnapshotWriterException
   * @throws SnapshotReaderException
   */
  private void processPossibleChange(DocumentSnapshot documentSnapshot)
      throws RepositoryException, InterruptedException, SnapshotWriterException,
             SnapshotReaderException {
    DocumentHandle documentHandle = documentSnapshot.getUpdate(current);
    snapshotWriter.write(documentSnapshot);
    if (documentHandle == null) {
      // No change.
    } else {
      // Normal change - send the gsa an update.
      callback.changedDocument(documentHandle, getCheckpoint());
    }
    current = snapshotReader.read();
  }

  // Public for DocumentSnapshotRepositoryMonitorTest
  @VisibleForTesting
  public void acceptGuarantee(MonitorCheckpoint cp) {
    snapshotStore.acceptGuarantee(cp);
    guaranteeCheckpoint = cp;
  }
}
