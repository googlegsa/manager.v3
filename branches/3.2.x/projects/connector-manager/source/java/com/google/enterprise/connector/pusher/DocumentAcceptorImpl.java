// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.pusher.Pusher.PusherStatus;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentAcceptor;
import com.google.enterprise.connector.spi.DocumentAcceptorException;
import com.google.enterprise.connector.spi.Lister;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation for {@link DocumentAcceptor} - something that takes spi
 * Documents and sends them along on their way.
 */
// TODO: Impose Error delay where appropriate.
public class DocumentAcceptorImpl implements DocumentAcceptor {
  private static final Logger LOGGER =
      Logger.getLogger(DocumentAcceptorImpl.class.getName());

  private final String connectorName;
  private final PusherFactory pusherFactory;

  private Pusher pusher;

  // Sleep milliseconds when waiting for Pusher to resume OK status.
  private long shortSleep = 30 * 1000L;
  private long longSleep = 5 * 60 * 1000L;
  private int retryCount = 10;

  public DocumentAcceptorImpl(String connectorName, PusherFactory pusherFactory)
      throws DocumentAcceptorException, RepositoryException {
    this.connectorName = connectorName;
    this.pusherFactory = pusherFactory;
  }

  /* Used by tests to shorten sleep times. */
  void setSleepIntervals(long shortSleep, long longSleep, int retryCount) {
    this.shortSleep = shortSleep;
    this.longSleep = longSleep;
    this.retryCount = retryCount;
  }

  /**
   * Takes an spi Document and pushes it along, presumably to the GSA Feed.
   *
   * @param document A Document
   * @throws RepositoryException if transient error accessing the Repository
   * @throws RepositoryDocumentException if fatal error accessing the Document
   * @throws DocumentAcceptorException if a transient error occurs in the
   *         DocumentAcceptor
   */
  public synchronized void take(Document document)
      throws DocumentAcceptorException, RepositoryException {
    try {
      if (pusher.take(document) != PusherStatus.OK) {
        waitForOkStatus();
      }
    } catch (NullPointerException e) {
      // Ugly, but avoids checking for null Pusher on every call to take.
      if (pusher == null) {
        // Opps. We need to get a new Pusher.
        try {
          pusher = pusherFactory.newPusher(connectorName);
          this.take(document);
        } catch (PushException pe) {
          LOGGER.log(Level.SEVERE, "DocumentAcceptor failed to get Pusher", e);
          throw new DocumentAcceptorException("Failed to get Pusher", e);
        }
      } else {
        throw e;
      }
    } catch (PushException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor failed to take document", e);
      throw new DocumentAcceptorException("Failed to take document", e);
    } catch (FeedException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor failed to take document", e);
      throw new DocumentAcceptorException("Failed to take document", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "DocumentAcceptor failed to take document", e);
      throw e;
    } catch (InterruptedException e) {
      // Woke from sleep. Just return.
    }
  }

  /**
   * Wait for the PusherStatus to clear. But don't wait forever.
   */
  private void waitForOkStatus() throws PushException,
      FeedException, RepositoryException, InterruptedException {
    for (int retries = 0; retries < retryCount; retries++) {
      switch (pusher.getPusherStatus()) {
        case OK:
          return;
        case DISABLED:
          // This is not likely, but trigger getting a new Pusher.
          throw new NullPointerException();
        case LOW_MEMORY:
        case LOCAL_FEED_BACKLOG:
          Thread.sleep(shortSleep);
          break;
        case GSA_FEED_BACKLOG:
          Thread.sleep(longSleep);
          break;
      }
    }
  }

  /**
   * Finishes processing a document feed.  If the caller anticipates no
   * further calls to {@link #take(Document)} will be made, this method
   * should be called, so that the DocumentAcceptor may send a cached,
   * accumulated feed to the GSA.
   *
   * @throws RepositoryException if transient error accessing the Repository
   * @throws RepositoryDocumentException if fatal error accessing the Document
   * @throws DocumentAcceptorException if a transient error occurs in the
   *         DocumentAcceptor
   */
  public synchronized void flush()
      throws DocumentAcceptorException, RepositoryException {
    try {
      if (pusher != null) {
        pusher.flush();
        pusher = null;
      }
    } catch (PushException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor failed to flush feed.", e);
      throw new DocumentAcceptorException("Failed to flush feed", e);
    } catch (FeedException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor failed to flush feed.", e);
      throw new DocumentAcceptorException("Failed to flush feed", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "DocumentAcceptor failed to flush feed.", e);
      throw e;
    }
  }

  /**
   * Cancels a feed.  Discard any accumulated feed data. Note that some
   * documents submitted to this DocumentAcceptor may have already been
   * sent on to the GSA.
   */
  public synchronized void cancel() {
    if (pusher != null) {
      pusher.cancel();
      pusher = null;
    }
  }
}
