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
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * The {@link Lister} mechanism might be
 * used as an alternative to the {@link TraversalManager} as a
 * convenient interface for feeding documents by the connector.
 * Listers may also choose to implement the
 * {@link com.google.enterprise.connector.spi.TraversalContextAware TraversalContextAware}
 * and/or
 * {@link com.google.enterprise.connector.spi.TraversalScheduleAware TraversalScheduleAware}
 * interfaces.
 *
 * @since 3.0
 */
public interface Lister {
  /**
   * Supplies the {@link DocumentAcceptor} that the {@link Lister} may use
   * to supply {@link Document Documents} to the feed.
   *
   * @param documentAcceptor a DocumentAcceptor
   */
  // XXX: Giving the Listor a DocumentAcceptorFactory would allow it to
  // generate several feeds concurrently in multiple threads.
  public void setDocumentAcceptor(DocumentAcceptor documentAcceptor)
      throws RepositoryException;

  /**
   * Starts the {@link Lister}.  It may commence sending documents to the
   * {@link DocumentAcceptor}.
   */
  public void start() throws RepositoryException;

  /**
   * Shuts down the {@link Lister}.  It should cease sending documents to the
   * {@link DocumentAcceptor}.
   */
  public void shutdown() throws RepositoryException;
}
