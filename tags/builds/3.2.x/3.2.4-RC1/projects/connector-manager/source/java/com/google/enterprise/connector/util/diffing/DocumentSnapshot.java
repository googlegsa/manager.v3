// Copyright 2010 Google Inc.
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

import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Interface for a local copy of a document that is stored on the GSA.
 * This represents both objects in the repository as well as
 * serialized entries in the snapshot files (the implementations need
 * not be the same concrete, although doing so is often convenient).
 * <p>
 * The diffing package depends on the ordering of snapshots, which
 * must match the order of their appearance in the
 * {@link SnapshotRepository}. By default the ordering of snapshots is
 * determined by the lexicographic ordering of their document IDs (the
 * natural order of Java {@code String} values).
 * <p>
 * Since version 3.0.6, the {@code DocumentSnapshot} implementation
 * class can also implement {@link Comparable}. The diffing package
 * will check for the {@code Comparable} implementation before
 * comparing the document IDs. Reasons to implement {@code Comparable}
 * to define the details of the ordering include the lexicographic
 * ordering of the document IDs, and the encoding required to make the
 * document IDs safe to use in URLs, which might not be an
 * order-preserving encoding (e.g., Base64 is not order-preserving).
 *
 * @since 2.8
 */
public interface DocumentSnapshot {
  /**
   * Returns the unique ID for the document.
   */
  String getDocumentId();

  /**
   * Returns a {@link DocumentHandle} for updating the referenced
   * document on the GSA or {@code null} if the document on the GSA
   * does not need updating.
   * <p/>
   * The diffing framework will call this before calling
   * {@link #toString()} to persist this {@link DocumentSnapshot}.
   *
   * @param onGsa a {@link DocumentSnapshot} representing the GSA's version
   *        of the document
   * @return a {@link DocumentHandle} representing the change required by
   *        the GSA to bring the document up-to-date; or {@code null}
   *        if the GSA's knowledge of the document is up-to-date
   * @throws RepositoryException
   */
  DocumentHandle getUpdate(DocumentSnapshot onGsa)
      throws RepositoryException;

  /**
   * Returns a serialized {@link String} representation of this
   * {@link DocumentSnapshot} suitable for deserialization with
   * {@link DocumentSnapshotFactory#fromString(String)}.
   * The returned value must not be {@code null}.
   */
  @Override
  String toString();
}
