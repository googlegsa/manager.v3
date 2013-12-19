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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import junit.framework.TestCase;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.IAnswer;

import java.util.List;

public class OrderedSnapshotWriterTest extends TestCase {
  /* A mockable, comparable snapshot interface. */
  private interface ComparableDocumentSnapshot
      extends DocumentSnapshot, Comparable<DocumentSnapshot> {
  }

  /** Creates a mock snapshot that expects only calls to getDocumentId. */
  /* TODO(jlacey): This method is copied in DocumentSnapshotComparatorTest. */
  private <T extends DocumentSnapshot> T getDocumentId(Class<T> snapshotClass,
      String docid) {
    T snapshot = createMock(snapshotClass);
    expect(snapshot.getDocumentId())
        .andReturn(docid)
        .anyTimes();
    replay(snapshot);
    return snapshot;
  }

  /** Creates a mock comparable snapshot that compares numeric document IDs. */
  private DocumentSnapshot numericCompareTo(final String docid) {
    ComparableDocumentSnapshot snapshot =
        createMock(ComparableDocumentSnapshot.class);
    expect(snapshot.compareTo(isA(DocumentSnapshot.class)))
        .andAnswer(new IAnswer<Integer>() {
              public Integer answer() throws Throwable {
                return Integer.valueOf(docid).compareTo(
                    Integer.valueOf(
                        ((DocumentSnapshot) getCurrentArguments()[0])
                        .getDocumentId()));
              }
            })
        .anyTimes();
    expect(snapshot.getDocumentId())
        .andReturn(docid)
        .anyTimes();
    replay(snapshot);
    return snapshot;
  }

  /**
   * Tests that writing the given snapshots to the writer actually
   * writes the snapshots with the given document IDs.
   */
  private void testOrderedSnapshotWriter(List<DocumentSnapshot> snapshots,
      List<String> docids) throws SnapshotWriterException {
    SnapshotWriter writer = createMock(SnapshotWriter.class);
    final Capture<DocumentSnapshot> captures =
        new Capture<DocumentSnapshot>(CaptureType.ALL);
    writer.write(capture(captures));
    expectLastCall().anyTimes();
    expect(writer.getRecordCount())
        .andAnswer(new IAnswer<Long>() {
              public Long answer() throws Throwable {
                return new Long(captures.getValues().size());
              }
            });
    replay(writer);

    OrderedSnapshotWriter ordered = new OrderedSnapshotWriter(writer);
    for (DocumentSnapshot snapshot : snapshots) {
      ordered.write(snapshot);
    }

    assertEquals(docids.size(), ordered.getRecordCount());
    assertEquals(docids, getDocumentIds(captures.getValues()));
  }

  /** Transforms a list of snapshots to a list of their document IDs. */
  private List<String> getDocumentIds(List<DocumentSnapshot> snapshots) {
    List<String> docids = Lists.newArrayList();
    for (DocumentSnapshot snapshot : snapshots) {
      docids.add(snapshot.getDocumentId());
    }
    return docids;
  }

  /** Transforms an array of docids to a list of mock snapshots. */
  private List<DocumentSnapshot> docidOrdering(String... docids) {
    List<DocumentSnapshot> snapshots = Lists.newArrayList();
    for (String docid : docids) {
      snapshots.add(getDocumentId(DocumentSnapshot.class, docid));
    }
    return snapshots;
  }

  /**
   * Transforms an array of docids to a list of mock comparable
   * snapshots that compare numeric document IDs.
   */
  private List<DocumentSnapshot> numericOrdering(String... docids) {
    List<DocumentSnapshot> snapshots = Lists.newArrayList();
    for (String docid : docids) {
      snapshots.add(numericCompareTo(docid));
    }
    return snapshots;
  }

  public void testUniqueDocids() throws SnapshotWriterException {
    testOrderedSnapshotWriter(
        docidOrdering("1", "2", "3", "4", "5"),
        ImmutableList.of("1", "2", "3", "4", "5"));
  }

  public void testDuplicateDocids() throws SnapshotWriterException {
    testOrderedSnapshotWriter(
        docidOrdering("1", "2", "3", "4", "2", "4", "5"),
        ImmutableList.of("1", "2", "3", "4", "5"));
  }

  public void testUnpaddedNumericDocids() throws SnapshotWriterException {
    testOrderedSnapshotWriter(
        docidOrdering(
            "1", "5", "10", "15", "50", "100", "200", "500"),
        ImmutableList.of(
            "1", "5", "50", "500"));
  }

  public void testPaddedNumericDocids() throws SnapshotWriterException {
    testOrderedSnapshotWriter(
        docidOrdering(
            "001", "005", "010", "015", "050", "100", "200", "500"),
        ImmutableList.of(
            "001", "005", "010", "015", "050", "100", "200", "500"));
  }

  /** Finally, the piece de resistance. */
  public void testNumericComparator() throws SnapshotWriterException {
    testOrderedSnapshotWriter(
        numericOrdering(
            "1", "5", "10", "15", "50", "100", "200", "500"),
        ImmutableList.of(
            "1", "5", "10", "15", "50", "100", "200", "500"));
  }

  public void testClose() throws SnapshotWriterException {
    SnapshotWriter writer = createMock(SnapshotWriter.class);
    writer.close();
    replay(writer);

    OrderedSnapshotWriter ordered = new OrderedSnapshotWriter(writer);
    ordered.close();
    verify(writer);
  }

  public void testGetPath() throws SnapshotWriterException {
    SnapshotWriter writer = createMock(SnapshotWriter.class);
    expect(writer.getPath())
        .andReturn("/something/else");
    replay(writer);

    OrderedSnapshotWriter ordered = new OrderedSnapshotWriter(writer);
    assertEquals("/something/else", ordered.getPath());
  }

  public void testGetRecordCount() throws SnapshotWriterException {
    SnapshotWriter writer = createMock(SnapshotWriter.class);
    expect(writer.getRecordCount())
        .andReturn(42L);
    replay(writer);

    OrderedSnapshotWriter ordered = new OrderedSnapshotWriter(writer);
    assertEquals(42L, ordered.getRecordCount());
  }
}
