// Copyright 2013 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.util.diffing;

import java.util.Comparator;

/**
 * A comparator for {@link DocumentSnapshot} objects that supports both
 * snapshots that implement the {@link Comparable} interface and legacy
 * comparisons using the document IDs. This implementation does not
 * support null snapshot values.
 *
 * @since 3.0.8
 */
public class DocumentSnapshotComparator
    implements Comparator<DocumentSnapshot> {
  /** An instance of this comparator for convenience. */
  public static final DocumentSnapshotComparator COMPARATOR =
      new DocumentSnapshotComparator();

  /** @throws NullPointerException if {@code left} or {@code right} is null */
  public int compare(DocumentSnapshot left, DocumentSnapshot right) {
    if (left instanceof Comparable) {
      return compareSnapshots(left, right);
    } else if (right instanceof Comparable) {
      return -compareSnapshots(right, left);
    } else {
      return compareDocumentIds(left, right);
    }
  }

  @SuppressWarnings("unchecked")
  private int compareSnapshots(DocumentSnapshot left, DocumentSnapshot right) {
    if (right == null) {
      throw new NullPointerException();
    }
    // We can't test at runtime for Comparable<? extends DocumentSnapshot>,
    // without resorting to reflection, so a compareTo method that isn't
    // compatible with a DocumentSnapshot argument will throw a
    // ClassCastException here.
    try {
      return ((Comparable) left).compareTo(right);
    } catch (ClassCastException e) {
      return compareDocumentIds(left, right);
    }
  }

  private int compareDocumentIds(DocumentSnapshot left,
      DocumentSnapshot right) {
    return left.getDocumentId().compareTo(right.getDocumentId());
  }
}
