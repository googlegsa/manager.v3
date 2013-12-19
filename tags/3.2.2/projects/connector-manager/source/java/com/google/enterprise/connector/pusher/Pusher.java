// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Interface for a Pusher - something that takes spi Documents
 * and sends them along on their way.
 */
public interface Pusher {

  /**
   * Status indicating the readiness of the Pusher.
   */
  public static enum PusherStatus {
    OK, LOW_MEMORY, LOCAL_FEED_BACKLOG, GSA_FEED_BACKLOG, DISABLED;
  }

  /**
   * Takes an spi Document and pushes it along, presumably to the GSA Feed.
   *
   * @param document A Document
   * @return PusherStatus. If OK, Pusher may accept more documents.
   * @throws RepositoryException if transient error accessing the Repository
   * @throws RepositoryDocumentException if fatal error accessing the Document
   * @throws FeedException if a transient Feed error occurs in the Pusher
   * @throws PushException if a transient error occurs in the Pusher
   */
  public PusherStatus take(Document document)
      throws PushException, FeedException, RepositoryException;

  /**
   * Finishes processing a document feed.  If the caller anticipates no
   * further calls to {@link #take(Document)} will be
   * made, this method should be called, so that the Pusher may send a cached,
   * accumulated Feed to the feed processor.
   *
   * @throws RepositoryException if transient error accessing the Repository
   * @throws RepositoryDocumentException if fatal error accessing the Document
   * @throws FeedException if a transient Feed error occurs in the Pusher
   * @throws PushException if a transient error occurs in the Pusher
   */
  public void flush()
      throws PushException, FeedException, RepositoryException;

  /**
   * Cancels a feed.  Discard any accumulated feed data.
   */
  public void cancel();

  /**
   * Gets the current pusher status.
   *
   * @return the current PusherStatus
   * @throws RepositoryException if transient error accessing the Repository
   * @throws FeedException if a transient Feed error occurs in the Pusher
   * @throws PushException if a transient error occurs in the Pusher
   */
  public PusherStatus getPusherStatus()
      throws PushException, FeedException, RepositoryException;
}
