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

import static com.google.enterprise.connector.util.diffing.DocumentSnapshotComparator.COMPARATOR;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

import junit.framework.TestCase;

public class DocumentSnapshotComparatorTest extends TestCase {
  /* A mockable, comparable snapshot interface. */
  private interface ComparableDocumentSnapshot
      extends DocumentSnapshot, Comparable<DocumentSnapshot> {
  }

  /*
   * A comparable snapshot interface that won't compare itself to a
   * generic DocumentSnapshot. A partial mock apparently bypasses the
   * Java runtime machinery that throws a ClassCastException here, and
   * EasyMock throws an AssertionError instead, so here's a manual
   * mock class.
   */
  private static class ComparableSubclassDocumentSnapshot
      extends MockDocumentSnapshot
      implements Comparable<ComparableSubclassDocumentSnapshot> {
    private final int comparison;

    public ComparableSubclassDocumentSnapshot(int comparison, String docid) {
      super(docid, "extra values");
      this.comparison = comparison;
    }

    public int compareTo(ComparableSubclassDocumentSnapshot other) {
      return comparison;
    }
  }

  /** Creates a mock snapshot that expects only calls to getDocumentId. */
  /* TODO(jlacey): This method is copied in OrderedSnapshotWriterTest. */
  private <T extends DocumentSnapshot> T getDocumentId(Class<T> snapshotClass,
      String docid) {
    T snapshot = createMock(snapshotClass);
    expect(snapshot.getDocumentId())
        .andReturn(docid)
        .anyTimes();
    replay(snapshot);
    return snapshot;
  }

  /** Creates a mock snapshot that expects only calls to compareTo. */
  private DocumentSnapshot compareToSnapshot(int comparison) {
    ComparableDocumentSnapshot snapshot =
        createMock(ComparableDocumentSnapshot.class);
    expect(snapshot.compareTo(isA(DocumentSnapshot.class)))
        .andReturn(comparison)
        .anyTimes();
    replay(snapshot);
    return snapshot;
  }

  /** Creates a mock snapshot that is only comparable to its own subclass. */
  private DocumentSnapshot compareToSubclass(int comparison, String docid) {
    return new ComparableSubclassDocumentSnapshot(comparison, docid);
  }

  private void testNpe(DocumentSnapshot left, DocumentSnapshot right) {
    try {
      COMPARATOR.compare(left, right);
      fail("Expected a NullPointerException");
    } catch (NullPointerException expected) {
      expected.printStackTrace();
    }
  }

  public void testNullBoth() {
    testNpe(null, null);
  }

  public void testNullLeft() {
    testNpe(null, getDocumentId(ComparableDocumentSnapshot.class, "1"));
  }

  public void testNullRight() {
    testNpe(getDocumentId(ComparableDocumentSnapshot.class, "1"), null);
  }

  public void testComparableLeft() {
    int result = COMPARATOR.compare(
        compareToSnapshot(42),
        createMock(DocumentSnapshot.class));
    assertEquals(42, result);
  }

  public void testComparableRight() {
    int result = COMPARATOR.compare(
        createMock(DocumentSnapshot.class),
        compareToSnapshot(42));
    assertEquals(-42, result);
  }

  public void testComparableBoth() {
    int result = COMPARATOR.compare(
        compareToSnapshot(42),
        createMock(ComparableDocumentSnapshot.class));
    assertEquals(42, result);
  }

  public void testSubclassLeft() {
    int result = COMPARATOR.compare(
        compareToSubclass(42, "1"),
        getDocumentId(DocumentSnapshot.class, "9"));
    assertEquals(-8, result);
  }

  public void testSubclassRight() {
    int result = COMPARATOR.compare(
        getDocumentId(DocumentSnapshot.class, "1"),
        compareToSubclass(42, "9"));
    assertEquals(-8, result);
  }

  public void testSubclassBoth() {
    int result = COMPARATOR.compare(
        compareToSubclass(42, "1"),
        compareToSubclass(-42, "9"));
    assertEquals(42, result);
  }
}
