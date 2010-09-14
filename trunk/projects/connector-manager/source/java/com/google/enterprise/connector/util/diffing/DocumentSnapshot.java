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
 */
public interface DocumentSnapshot {
  /**
   * Returns the id for the document.
   */
  String getDocumentId();

  /**
   * Returns a {@link DocumentHandle} for updating the referenced
   * document on the GSA or null if the document on the GSA
   * does not need updating.
   * <p>
   * The diffing framework will call this before calling
   * {@link #toString()} to persist this {@link DocumentSnapshot}.
   * @throws RepositoryException
   */
  DocumentHandle getUpdate(DocumentSnapshot onGsa)
      throws RepositoryException;

  /**
   * Returns a serialized {@link String} representation of this
   * {@link DocumentSnapshot} suitable deserialization with
   * {@link DocumentSnapshotFactory#fromString(
   * String)}. The returned value must not be null.
   */
  @Override
  String toString();
}
