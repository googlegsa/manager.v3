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

package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentAcceptor;
import com.google.enterprise.connector.spi.DocumentAcceptorException;
import com.google.enterprise.connector.spi.Lister;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalContext;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mock implementation for {@link Lister}.
 */
public class MockLister implements Lister {
  private static final Logger LOGGER =
      Logger.getLogger(MockLister.class.getName());

  protected DocumentAcceptor documentAcceptor;
  protected Timer timer;
  protected final long delayMillis;
  protected final long maxDocs;
  protected long documentCount;
  protected boolean isShutdown;

  /**
   * Creates a Lister that feeds a new document with the given delay between
   * documents until maxDocs is reached or shutdown is called.  If delayMillis
   * is non-zero, documents are fed in a separate thread.
   */
  public MockLister(long maxDocs, long delayMillis) {
    this.maxDocs = maxDocs;
    this.delayMillis = delayMillis;
    isShutdown = false;
    documentCount = 0;
  }

  /* @Override */
  public void setDocumentAcceptor(DocumentAcceptor documentAcceptor)
        throws RepositoryException {
    this.documentAcceptor = documentAcceptor;
  }

  /**
   * {@inheritDoc}
   * If delayMillis is non-zero, feed documents in a separate thread,
   * otherwise feed them from here.
   */
  /* @Override */
  public void start() throws RepositoryException {
    isShutdown = false;
    documentCount = 0;
    // If no interdoc delay, run in this thread for simplicity.
    if (delayMillis == 0L) {
      try {
        while (documentCount < maxDocs) {
          nextDocument();
        }
      } catch (DocumentAcceptorException e) {
        LOGGER.log(Level.WARNING, "Caught DocumentAcceptorException", e);
      }
    } else {
      synchronized (this) {
        // Create a timer with a named thread.
        timer = new Timer("MockLister");
        timer.schedule(new ListerTask(), delayMillis, delayMillis);
      }
    }
  }

  /**
   * Shuts down the {@link Lister}.  It should cease sending documents to the
   * {@link DocumentAcceptor}.
   */
  /* @Override */
  public void shutdown() throws RepositoryException {
    LOGGER.config("Shutdown Lister");
    isShutdown = true;
    synchronized(this) {
      if (timer != null) {
        timer.cancel();
        timer = null;
      }
    }
    try {
      documentAcceptor.flush();
    } catch (DocumentAcceptorException e) {
      LOGGER.log(Level.WARNING, "Caught DocumentAcceptorException", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Caught RepositoryException", e);
    }
    LOGGER.finer("Fed " + documentCount + " documents.");
  }

  /** Send the document to the documentAcceptor. */
  protected void feedDocument(Document doc)
      throws DocumentAcceptorException, RepositoryException {
    try {
      LOGGER.finer("Feeding document "
          + Value.getSingleValueString(doc, SpiConstants.PROPNAME_DOCID));
      documentAcceptor.take(doc);
    } catch (DocumentAcceptorException e) {
      LOGGER.log(Level.WARNING, "Caught DocumentAcceptorException", e);
      documentAcceptor.cancel();
      throw e;
    } catch (RepositoryException e) {
      LOGGER.log(Level.WARNING, "Caught RepositoryException", e);
      throw e;
    }
  }

  /** Return a new document. */
  protected synchronized Document newDocument() {
    String id = Long.toString(documentCount++);
    return ConnectorTestUtils.createSimpleDocument(id);
  }

  /** Return the number of documents fed. */
  protected synchronized long getDocumentCount() {
    return documentCount;
  }

  /** Send the next document to the documentAcceptor. */
  protected synchronized void nextDocument()
      throws DocumentAcceptorException, RepositoryException {
    if (!isShutdown) {
      feedDocument(newDocument());
      if (documentCount >= maxDocs) {
        shutdown();
      }
    }
  }

  protected class ListerTask extends TimerTask {
    @Override
    public void run() {
      try {
        nextDocument();
      } catch (DocumentAcceptorException e) {
        LOGGER.log(Level.WARNING, "Uncaught DocumentAcceptorException", e);
      } catch (RepositoryException e) {
        LOGGER.log(Level.WARNING, "Uncaught RepositoryException", e);
      }
    }
  }
}
