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

import com.google.enterprise.connector.database.DocumentStore;
import com.google.enterprise.connector.pusher.FeedException;
import com.google.enterprise.connector.pusher.Pusher;
import com.google.enterprise.connector.pusher.PushException;
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
  private final Pusher pusher;
  private final DocumentStore documentStore;

  public DocumentAcceptorImpl(String connectorName, Pusher pusher,
      DocumentStore documentStore)
      throws DocumentAcceptorException, RepositoryException {
    this.connectorName = connectorName;
    this.pusher = pusher;
    this.documentStore = documentStore;
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
      pusher.take(document, documentStore);
    } catch (PushException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor catches PushException.", e);
      throw new DocumentAcceptorException("Failed to take document", e);
    } catch (FeedException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor catches FeedException.", e);
      throw new DocumentAcceptorException("Failed to take document", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor catches RepositoryException.",
                 e);
      throw e;
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Uncaught Exception", t);
      throw new DocumentAcceptorException("Failed to take document", t);
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
  public void flush() throws DocumentAcceptorException, RepositoryException {
    try {
      pusher.flush();
    } catch (PushException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor catches PushException.", e);
      throw new DocumentAcceptorException("Failed to flush feed", e);
    } catch (FeedException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor catches FeedException.", e);
      throw new DocumentAcceptorException("Failed to flush feed", e);
    } catch (RepositoryException e) {
      LOGGER.log(Level.SEVERE, "DocumentAcceptor catches RepositoryException.",
                 e);
      throw e;
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Uncaught Exception", t);
      throw new DocumentAcceptorException("Failed to flush feed", t);
    }
  }

  /**
   * Cancels a feed.  Discard any accumulated feed data. Note that some
   * documents submitted to this DocumentAcceptor may have already been
   * sent on to the GSA.
   */
  public void cancel() {
    pusher.cancel();
  }
}