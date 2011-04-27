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

import java.util.Iterator;

/**
 * Interface for accessing a sequence of {@link DocumentSnapshot}
 * objects from a repository. The returned objects are are sorted
 * by {@link DocumentSnapshot#getDocumentId()} values.
 *
 * @since 2.8
 */
public interface  SnapshotRepository<T extends DocumentSnapshot> extends
    Iterable<T> {
  /**
   * Returns the name for this {@link SnapshotRepository}. The returned
   * name must be unique for the containing Connector.
   */
  String getName();

  /**
   * Returns an {@link Iterator} to access the collection
   * of {@link DocumentSnapshot} objects from the repository.
   * The returned {@link Iterator} must return {@link DocumentSnapshot}
   * objects in {@link DocumentSnapshot#getDocumentId()} order.
   * <p/>
   * The returned {@link Iterator} may throw the unchecked
   * {@link SnapshotRepositoryRuntimeException} if
   * {@link Iterator#hasNext()} or {@link Iterator#next()} fails. The returned
   * {@link Iterator} need not implement {@link Iterator#remove()}.
   */
  /* @Override */
  Iterator<T> iterator()
      throws SnapshotRepositoryRuntimeException;
}
