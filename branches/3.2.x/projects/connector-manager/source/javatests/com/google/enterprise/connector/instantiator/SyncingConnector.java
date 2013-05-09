// Copyright (C) 2009 Google Inc.
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

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorShutdownAware;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SimpleDocumentList;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A Test Connector that provides
 * <OL>
 * <LI> Synchronization between a test and the {@link TraversalManager} so the
 * test can coordinate test actions such as cancel during the running of a
 * batch.</LI>
 * <LI> Tracking of traversal related events for test validation purposes.</LI>
 * </OL>
 * This implementation supports repeated instantiation by Spring. Currently
 * tests interact with the {@link TraversalManager} through static singleton
 * Objects. Hence concurrent management of multiple active
 * {@link TraversalManager} instances is not well supported.
 */
public class SyncingConnector implements Connector, ConnectorShutdownAware {
  private static final Logger LOGGER =
      Logger.getLogger(SyncingConnector.class.getName());

  private volatile static Tracker tracker = new Tracker();

  private static BlockingQueue<DocumentList> traversalResults =
    new ArrayBlockingQueue<DocumentList>(100);

  /**
   * Milliseconds a traversal will block polling traversalResults for a
   * {@link DocumentList} before giving up.
   */
  private static long pollTimeOutMillis = 5000;

  /**
   * Sets the Poll timeout in milliseconds.
   */
  static synchronized void setPollTimeout(long millis) {
    pollTimeOutMillis = millis;
  }

  /**
   * Gets the Poll timeout in milliseconds.
   */
  static synchronized long getPollTimeout() {
    return pollTimeOutMillis;
  }

  /**
   * Creates a single document {@link DocumentList} and queues it for the
   * {@link TraversalManager} to return on an upcoming
   * {@link TraversalManager#startTraversal()} or
   * {@link TraversalManager#resumeTraversal(String)} call. Note these calls
   * block until a {@link DocumentList} has been queued or the
   * {@code timeOutMillis} milliseconds have elapsed.
   *
   * @return A {@link List} with the queued document.
   */
  static List<SimpleDocument> createaAndQueueDocumentList() {
    List<SimpleDocument> result = createDocumentList(tracker.nextDocId());
    traversalResults.add(new SimpleDocumentList(result));
    return result;
  }

  /**
   * Returns a {@link SyncingConnector.Tracker} for tracking and coordinating
   * with a {@link TraversalManager}
   */
  static synchronized Tracker getTracker() {
    return tracker;
  }

  public SyncingConnector() {
  }

  /* @Override */
  public Session login() {
    tracker.incrementLoginCount();
    return new SyncingConnectorSession();
  }

  public void delete() {
    tracker.incrementDeleteCount();
  }

  public void shutdown() {
    tracker.incrementShutdownCount();
  }

  /**
   * Resets static state so the calling test will not be affected by ealier
   * usage.
   */
  static void reset() {
    traversalResults.clear();
    tracker = new Tracker();
  }

  private class SyncingConnectorSession implements Session {
   public AuthenticationManager getAuthenticationManager() {
     return null;
    }

    public AuthorizationManager getAuthorizationManager() {
     return null;
    }

    public TraversalManager getTraversalManager() {
      tracker.incrementTraversalManagerCount();
      return new SyncingConnectorTraversalManager();
    }
  }

  private class SyncingConnectorTraversalManager implements
      TraversalManager {
    public DocumentList resumeTraversal(String checkPoint) {
      tracker.incrementResumeTraversalCount();
      tracker.traversingStarted();
      return poll();
    }

    public void setBatchHint(int batchHint) {
      // Ignored.
    }

    public DocumentList startTraversal() {
      tracker.incrementStartTraversalCount();
      tracker.traversingStarted();
      return poll();
    }

    private DocumentList poll() {
      try {
        DocumentList result = traversalResults.poll(getPollTimeout(),
            TimeUnit.MILLISECONDS);
        if(result == null) {
          LOGGER.warning("poll returned null document.");
        }
        return result;
      } catch (InterruptedException ie) {
        tracker.incrementInterruptedCount();
        tracker.traversingInterrupted();
        return null;
      }
    }
  }

  /**
   * Provides tracking and synchronization with a {@link TraversalManager}.
   */
  static class Tracker {
    // BlockingQueueConnectorTraversalManager writes to this queue in
    // start/resumeTraversal. Tests read from the queue to block until
    // the traversal is underway.
    private final BlockingQueue<Object> traversingQueue =
        new ArrayBlockingQueue<Object>(200);

    // BlockingQueueConnectorTraversalManager writes to this queue
    // when a poll is interrupted. Tests read from the queue to block until
    // the interrupt occurs.
    private final BlockingQueue<Object> traversingInterrupted =
        new ArrayBlockingQueue<Object>(200);

    private int loginCount;
    private int deleteCount;
    private int shutdownCount;
    private int interruptedCount;
    private int traversalManagerCount;
    private int startTraversalCount;
    private int resumeTraversalCount;
    private int nextDocId;

    public final void traversingStarted() {
      traversingQueue.add(new Object());
    }

    public final void blockUntilTraversing()
        throws InterruptedException {
      traversingQueue.poll(getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    public final void traversingInterrupted() {
      traversingInterrupted.add(new Object());
    }

    public void blockUntilTraversingInterrupted()
        throws InterruptedException {
      if (null == traversingInterrupted.poll(getPollTimeout(),
          TimeUnit.MILLISECONDS)) {
        throw new InterruptedException("poll timed out");
      }
    }

    public int getLoginCount() {
      return loginCount;
    }

    public synchronized void incrementLoginCount() {
      loginCount++;
    }

    public synchronized int getDeleteCount() {
      return deleteCount;
    }

    public synchronized void incrementDeleteCount() {
      deleteCount++;
    }

    public synchronized int getShutdownCount() {
      return shutdownCount;
    }

    public synchronized void incrementShutdownCount() {
      shutdownCount++;
    }

    public synchronized int getInterruptedCount() {
      return interruptedCount;
    }

    public void incrementInterruptedCount() {
      interruptedCount++;
    }

    public synchronized int getTraversalManagerCount() {
      return traversalManagerCount;
    }

    public synchronized void incrementTraversalManagerCount() {
      traversalManagerCount++;
    }

    public synchronized int getStartTraversalCount() {
      return startTraversalCount;
    }

    public synchronized void incrementStartTraversalCount() {
      startTraversalCount++;
    }

    public synchronized int getResumeTraversalCount() {
      return resumeTraversalCount;
    }

    public synchronized void incrementResumeTraversalCount() {
      resumeTraversalCount++;
    }

    public synchronized String nextDocId() {
      return Integer.toString(nextDocId++);
    }

    @Override
    public synchronized String toString() {
      return "Tracker"
          + " loginCount=" + loginCount
          + " deleteCount=" + deleteCount
          + " shutdownCount=" + shutdownCount
          + " interruptedCount=" + interruptedCount
          + " startTraversalCount=" + startTraversalCount
          + " resumeTraversalCount=" + resumeTraversalCount
          + " nextDocId=" + nextDocId;
    }
  }

  public static List<SimpleDocument>  createDocumentList(String docId) {
    SimpleDocument document = ConnectorTestUtils.createSimpleDocument(docId);
    List<SimpleDocument> docList = new LinkedList<SimpleDocument>();
    docList.add(document);
    return docList;
  }
}
