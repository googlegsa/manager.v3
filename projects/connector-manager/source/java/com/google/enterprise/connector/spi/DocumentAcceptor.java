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

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Interface for a DocumentAcceptor - something that takes spi Documents
 * and sends them along on their way.
 *
 * @since 3.0
 */
// NOTE: This looks A LOT like Pusher.
public interface DocumentAcceptor {
  /**
   * Takes an spi Document and pushes it along, presumably to the GSA Feed.
   *
   * @param document A Document
   * @throws RepositoryException if transient error accessing the Repository
   * @throws RepositoryDocumentException if fatal error accessing the Document
   * @throws DocumentAcceptorException if a transient error occurs in the
   *         DocumentAcceptor
   */
  public void take(Document document)
      throws DocumentAcceptorException, RepositoryException;

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
  public void flush() throws DocumentAcceptorException, RepositoryException;

  /**
   * Cancels a feed.  Discard any accumulated feed data. Note that some
   * documents submitted to this DocumentAcceptor may have already been
   * sent on to the GSA.
   */
  public void cancel() throws DocumentAcceptorException, RepositoryException;
}
