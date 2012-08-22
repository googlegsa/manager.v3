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

import java.sql.Timestamp;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Bounded buffer of {@link Change} objects for buffering between the
 * {@link DocumentSnapshotRepositoryMonitor} and the
 * {@link CheckpointAndChangeQueue}.
 *
 * @since 2.8
 */
@VisibleForTesting
public class ChangeQueue implements ChangeSource {
  private final BlockingQueue<Change> pendingChanges;

  /** Milliseconds to sleep after a scan that finds no changes. */
  private final long sleepInterval;

  /** Logger that records crawl activities for each repository scan.*/
  private final CrawlActivityLogger activityLogger;
  
  /** 
   * Flag that decides whether to add delay after each scan or only after
   * scans with no changes found. 
   */
  private final boolean introduceDelayAfterEveryScan;

  /**
   * Interface to log the crawl activity for each crawl.
   */
  public static interface CrawlActivityLogger {

    /**Records the start time of the scan.
     * @param time timestamp of the start time
     */
    void scanBeginAt(Timestamp time);

    /**
     * Records the end time of the scan.
     * @param time timestamp of the end time
     */
    void scanEndAt(Timestamp time);

    /**
     * To record that crawling thread just received a document for
     * which either the content or the meta data or both have changed since the
     * last scan.
     * @param documentId Id for the changed document
     */
    void gotChangedDocument(String documentId);

    /**
     * To record that crawling thread just received a new document
     * which was not present in the last scan.
     * @param documentId Id of the newly added document
     */
    void gotNewDocument(String documentId);

    /**
     * To record that crawling thread just found out that the
     * previously existing document got deleted and is no longer present.
     * @param documentId id of the deleted document
     */
    void gotDeletedDocument(String documentId);

  }

  public static class DefaultCrawlActivityLogger implements CrawlActivityLogger {

    private int newDocumentCount, changedDocumentCount, deletedDocumentCount;
    private Timestamp startTime, endTime;
    private static final Logger LOG = Logger.getLogger(
        DefaultCrawlActivityLogger.class.getName());

    /* @Override */
    public void scanBeginAt(Timestamp time) {
      logCrawlStatistics();
      resetLogStatistics();
      startTime = time;
      LOG.info("Scan started at : " + time);
    }

    /* @Override */
    public void scanEndAt(Timestamp time) {
      endTime = time;
      LOG.info("Scan completed at : " + endTime);
      logCrawlStatistics();
      resetLogStatistics();
    }

    /**
     * This method resets all the variable that keep track of scans.
     */
    private void resetLogStatistics() {
      newDocumentCount = changedDocumentCount = deletedDocumentCount = 0;
      startTime = endTime = null;
    }

    /**
     * This method logs all the important information related to each scan of
     * the crawling thread. It logs following information for each scan
     * 1. Time taken to perform the complete scan. <br>
     * 2. No. of new documents found. <br>
     * 3. No. of changed documents found. <br>
     * 4. No. of deleted documents found.
     */
    private void logCrawlStatistics() {
      if (startTime != null) {
        LOG.info("Crawl statistics for this scan");
        if (endTime == null) {
          LOG.info("The scan failed to complete. The crawl statistics reflect the figures at the time of starting next scan");
          endTime = new Timestamp(System.currentTimeMillis());
        }
        String duration = (new Long((endTime.getTime() - startTime.getTime()) / 1000)).toString();
        LOG.info("Scan duration : " + duration + " seconds");
        LOG.info("# of new documents found : " + newDocumentCount);
        LOG.info("# of changed documents found : " + changedDocumentCount);
        LOG.info("# of deleted documents found : " + deletedDocumentCount);
      }
    }

    /* @Override */
    public void gotChangedDocument(String documentId) {
      ++changedDocumentCount;
      LOG.fine("Changed document found during the crawl; document id is : " + documentId);
    }

    /* @Override */
    public void gotDeletedDocument(String documentId) {
      ++deletedDocumentCount;
      LOG.fine("Deleted document found during the crawl; document id is : " + documentId);
    }

    /* @Override */
    public void gotNewDocument(String documentId) {
      ++newDocumentCount;
      LOG.fine("New document found during the crawl; document id is : " + documentId);
    }

  }

  /**
   * Adds {@link Change Changes} to this queue.
   */
  private class Callback implements DocumentSnapshotRepositoryMonitor.Callback {
    private int changeCount = 0;

    public void passBegin() {
      changeCount = 0;
      activityLogger.scanBeginAt(new Timestamp(System.currentTimeMillis()));
    }

    /* @Override */
    public void changedDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {
      ++changeCount;
      pendingChanges.put(new Change(Change.FactoryType.CLIENT, dh, mcp));
      activityLogger.gotChangedDocument(dh.getDocumentId());
    }

     /* @Override */
    public void deletedDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {
      ++changeCount;
      pendingChanges.put(new Change(Change.FactoryType.INTERNAL, dh, mcp));
      activityLogger.gotDeletedDocument(dh.getDocumentId());
    }

    /* @Override */
    public void newDocument(DocumentHandle dh, MonitorCheckpoint mcp)
        throws InterruptedException {
      ++changeCount;
      pendingChanges.put(new Change(Change.FactoryType.CLIENT, dh, mcp));
      activityLogger.gotNewDocument(dh.getDocumentId());
    }

    /* @Override */
    public void passComplete(MonitorCheckpoint mcp) throws InterruptedException {
      activityLogger.scanEndAt(new Timestamp(System.currentTimeMillis()));
      if (introduceDelayAfterEveryScan || changeCount == 0) {
        Thread.sleep(sleepInterval);
      }
    }

    public boolean hasEnqueuedAtLeastOneChangeThisPass() {
      return changeCount > 0;
    }
  }

  /**
   * Create a new ChangeQueue.
   *
   * @param size the queue size
   * @param sleepInterval how often to look for new changes, in milliseconds
   * @param activityLogger a CrawlActivityLogger
   */
  /* @VisibleForTesting */ 
  public ChangeQueue(int size, long sleepInterval, CrawlActivityLogger activityLogger) {
    this(size, sleepInterval, false, activityLogger);
  }
  
  private ChangeQueue(int size, long sleepInterval, 
      boolean introduceDelayAfterEachScan, CrawlActivityLogger activityLogger) {
    pendingChanges = new ArrayBlockingQueue<Change>(size);
    this.sleepInterval = sleepInterval;
    this.activityLogger = activityLogger;
    this.introduceDelayAfterEveryScan = introduceDelayAfterEachScan;
  }
  
  public ChangeQueue(QueuePropertyFetcher propertyFetcher,
      CrawlActivityLogger activityLogger) {
    this(propertyFetcher.getQueueSize(),
        propertyFetcher.getDelayBetweenTwoScansInMillis(), 
        propertyFetcher.isIntroduceDelayAfterEveryScan(), activityLogger);
  }

  /**
   * @return the monitor callback. This is a factory method for use by Spring,
   *         which needs a Callback to create a
   *         {@code DocumentSnapshotRepositoryMonitor}.
   */
  public DocumentSnapshotRepositoryMonitor.Callback newCallback() {
    return new Callback();
  }

  /**
   * Gets the next available change from the ChangeQueue.  Will wait up to
   * 1/4 second for a change to appear if none is immediately available.
   *
   * @return the next available change, or {@code null} if no changes are
   *         available
   */
  public Change getNextChange() {
    try {
      return pendingChanges.poll(250L, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ie) {
      return null;
    }
  }

  /** Empties the queue of all pending changes. */
  void clear() {
    pendingChanges.clear();
  }
  
  /**
   * Interface to retrieve the properties required for ChangeQueue. 
   */
  public static interface QueuePropertyFetcher { 
    /**
     * Gets the queue size. 
     */
    int getQueueSize();
    
    /**
     * Gets the delay to add between two scans.
     */
    long getDelayBetweenTwoScansInMillis();
    
    /**
     * Gets the flag to decide whether to sleep after each scan
     * or only after scans with no changes found. 
     */
    boolean isIntroduceDelayAfterEveryScan();
  }
}
