// Copyright 2009 Google Inc.
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

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class SnapshotReaderTest extends TestCase {
  private MockDocumentSnapshot good1;
 private MockDocumentSnapshot good2;

  @Override
  public void setUp(){
    good1 = new MockDocumentSnapshot("good1", "good1.extra");
    good2 = new MockDocumentSnapshot("good1", "good2.extra");
  }

  public void testBasics() throws SnapshotReaderException, IOException {
    BufferedReader br = mkReader(good1, good2);
    SnapshotReader reader =
        new SnapshotReader(br, "test", 7, new MockDocumentSnapshotFactory());
    assertEquals(7, reader.getSnapshotNumber());
    DocumentSnapshot read = reader.read();
    assertNotNull(read);
    assertEquals(read, good1);
    read = reader.read();
    assertNotNull(read);
    assertEquals(read, good2);
    read = reader.read();
    assertNull(read);
  }

  public void testReaderBackwardCompatibility() throws IOException, SnapshotReaderException {
    BufferedReader br = mkReaderWithJsonTypeSnapshot(good1, good2);
    SnapshotReader reader =
        new SnapshotReader(br, "test", 7, new MockDocumentSnapshotFactory());
    assertEquals(7, reader.getSnapshotNumber());
    DocumentSnapshot read = reader.read();
    assertNotNull(read);
    assertEquals(read, good1);
    read = reader.read();
    assertNotNull(read);
    assertEquals(read, good2);
    read = reader.read();
    assertNull(read);
  }

  public void testMissingField() throws IOException {
    try {
      DocumentSnapshot bad = new MissingIdSnapshot("badId", "bad.extra");
      BufferedReader br = mkReader(bad);
      SnapshotReader reader = new SnapshotReader(br, "path", 9,
          new MockDocumentSnapshotFactory());
      reader.read();
      fail();
    } catch (SnapshotReaderException expected) {
      assertTrue(expected.getCause().getMessage().contains(
          MockDocumentSnapshot.Field.DOCUMENT_ID.toString()));
    }
  }

  public void testBadReader() {
    class FailingReader extends FilterReader {

      FailingReader() {
        super(new StringReader(""));
      }

      @Override
      public int read(char[] buf, int offset, int len) throws IOException {
        throw new IOException();
      }
    }

    try {
      SnapshotReader reader =
          new SnapshotReader(new BufferedReader(new FailingReader()), "string",
              4, new MockDocumentSnapshotFactory());
      reader.read();
      fail();
    } catch (SnapshotReaderException expected) {
      assertTrue(expected.getMessage().contains("failed to decide which record reader to use"));
    }
  }

  /**
   * Create a reader that contains {@code n} records. The last-modified time is
   * the number of the record.
   *
   * @param n
   * @return a mock reader.
   * @throws SnapshotReaderException
   */
  private SnapshotReader createMockInput(int n)
      throws SnapshotReaderException, IOException {
    List<MockDocumentSnapshot> snapshots = new ArrayList<MockDocumentSnapshot>();
    for (int ix = 0; ix < n; ix++) {
      snapshots.add(new MockDocumentSnapshot(Integer.toString(ix), "extra."+ix));
    }
    BufferedReader br = mkReader(snapshots.toArray(
        new MockDocumentSnapshot[snapshots.size()]));
    return new SnapshotReader(br, "testPath", 17,
        new MockDocumentSnapshotFactory());
  }

  public void testGetLineNumber()
      throws SnapshotReaderException, IOException {
    SnapshotReader reader = createMockInput(100);

    for (int k = 0; k < Integer.MAX_VALUE; ++k) {
      assertEquals(k, reader.getRecordNumber());
      DocumentSnapshot dss = reader.read();
      assertEquals(k + 1, reader.getRecordNumber());
      if (dss == null) {
        break;
      }
      assertEquals(k, Integer.parseInt(dss.getDocumentId()));
    }
  }

  public void testSkipRecords()
      throws SnapshotReaderException, IOException, InterruptedException {
    SnapshotReader reader = createMockInput(100);

    reader.skipRecords(0);
    DocumentSnapshot dss = reader.read();
    assertEquals("0", dss.getDocumentId());

    reader.skipRecords(7);
    assertEquals("8", reader.read().getDocumentId());

    try {
      reader.skipRecords(1000);
      fail("skipped too many records");
    } catch (SnapshotReaderException e) {
      assertTrue(e.getMessage().contains("snapshot contains only"));
    }
  }

  public void testMissingLength() throws Exception {
    String missingLength = SnapshotWriter.LENGTH_DELIMITER + "random padding";
    BufferedReader br = new BufferedReader(new StringReader(missingLength));
    SnapshotReader r = new SnapshotReader(br, "testPath", 17,
        new MockDocumentSnapshotFactory());
    try {
      r.read();
      fail();
    } catch (SnapshotReaderException sne) {
      assertTrue(sne.getMessage().contains(
          "failed to read snapshot record with missing length"));
    }
  }

  public void testOverflow() throws Exception {
    String overflowingLength = "1" + Integer.MAX_VALUE
        + SnapshotWriter.LENGTH_DELIMITER + "random padding";
    BufferedReader br = new BufferedReader(new StringReader(overflowingLength));
    SnapshotReader r = new SnapshotReader(br, "testPath", 17,
        new MockDocumentSnapshotFactory());
    try {
      r.read();
      fail();
    } catch (SnapshotReaderException sne) {
      assertTrue(sne.getMessage().contains(
          "failed to read snapshot record with invalid length"));
    }
  }

  public void testInvalidLength() throws SnapshotReaderException {
    String invalidLength = "max" + Integer.MAX_VALUE +
    SnapshotWriter.LENGTH_DELIMITER + "random padding";
    BufferedReader br = new BufferedReader(new StringReader(invalidLength));
    SnapshotReader r = new SnapshotReader(br, "testPath", 17,
        new MockDocumentSnapshotFactory());
    try {
      r.read();
      fail();
    } catch (SnapshotReaderException sne) {
      assertTrue(sne.getMessage().contains(
          "failed to read snapshot record with invalid length"));
    }
  }

  public void testMissingLengthDelim() throws SnapshotStoreException {
    String ssString = good1.toString();
    String invalidLength =  ssString.length() + ssString
        + SnapshotWriter.RECORD_DELIMITER;
    BufferedReader br = new BufferedReader(new StringReader(invalidLength));
    SnapshotReader r = new SnapshotReader(br, "testPath", 17,
        new MockDocumentSnapshotFactory());
    try {
      r.read();
      fail();
    } catch (SnapshotReaderException sne) {
      assertTrue(sne.getMessage().contains(
          "failed to read snapshot record with missing length delimiter"));
    }
  }

  public void testStringFormTooLong() throws SnapshotStoreException {
    String ssString = good1.toString();
    String invalidLength =  "" + (ssString.length() - 1)
         + SnapshotWriter.LENGTH_DELIMITER
         + ssString
         + SnapshotWriter.RECORD_DELIMITER;
    BufferedReader br = new BufferedReader(new StringReader(invalidLength));
    SnapshotReader r = new SnapshotReader(br, "testPath", 17,
        new MockDocumentSnapshotFactory());
    try {
      r.read();
      fail();
    } catch (SnapshotReaderException sne) {
      assertTrue(sne.getMessage().contains(
          "failed to read snapshot record missing record delimiter "));
    }
  }

  public void testStringFormTooShort() throws SnapshotReaderException {
    String ssString = good1.toString();
    String invalidLength =  "" + (ssString.length() + 2)
         + SnapshotWriter.LENGTH_DELIMITER
         + ssString
         + SnapshotWriter.RECORD_DELIMITER;
    BufferedReader br = new BufferedReader(new StringReader(invalidLength));
    SnapshotReader r = new SnapshotReader(br, "testPath", 17,
        new MockDocumentSnapshotFactory());
    try {
      r.read();
      fail();
    } catch (SnapshotReaderException sne) {
      assertTrue(sne.getMessage().contains(
          "failed to read snapshot record with incomplete record "));
    }
  }

  public void testMissingRecordDelim() throws SnapshotReaderException {
    String ssString = good1.toString();
    String noRecordDelim =  "" + ssString.length()
         + SnapshotWriter.LENGTH_DELIMITER
         + ssString;
    BufferedReader br = new BufferedReader(new StringReader(
        noRecordDelim + noRecordDelim));
    SnapshotReader r = new SnapshotReader(br, "testPath", 17,
        new MockDocumentSnapshotFactory());
    try {
      r.read();
      fail();
    } catch (SnapshotReaderException sne) {
      assertTrue(sne.getMessage().contains(
          "failed to read snapshot record missing record delimiter "));
    }
  }

  public void testNewLine() throws IOException, SnapshotReaderException{
    DocumentSnapshot nl1 =  new StringDocumentSnapshot("\nn\nl1\n");
    DocumentSnapshot nl2 =  new StringDocumentSnapshot("\n");
    BufferedReader br = mkReader(nl1, nl2);
    SnapshotReader reader =
        new SnapshotReader(br, "test", 7, new StringDocumentSnapshotFactory());
    assertEquals(7, reader.getSnapshotNumber());
    DocumentSnapshot read = reader.read();
    assertNotNull(read);
    assertEquals(read.getDocumentId(), nl1.getDocumentId());
    read = reader.read();
    assertNotNull(read);
    assertEquals(read.getDocumentId(), nl2.getDocumentId());
    read = reader.read();
    assertNull(read);
  }

  public void testSkipRecordsInterrupt()
      throws SnapshotStoreException, IOException {
    SnapshotReader reader = createMockInput(100);
    try {
      Thread.currentThread().interrupt();
      reader.skipRecords(25);
      fail();
    } catch (InterruptedException ie) {
      //Expected.
    } finally {
      assertFalse(Thread.interrupted());
    }
    DocumentSnapshot dss = reader.read();
    assertEquals("0", dss.getDocumentId());
  }

  private BufferedReader mkReader(DocumentSnapshot...  snapshots)
      throws IOException {
    Writer writer = new StringWriter();
    for (DocumentSnapshot snapshot : snapshots) {
      SnapshotWriter.write(snapshot, writer);
    }
    return new BufferedReader(new StringReader(writer.toString()));
  }

  /**
   * Creates a snapshot representation of previous version snapshot files.
   * @return BufferedReader
   * @throws IOException
   */
  private BufferedReader mkReaderWithJsonTypeSnapshot(DocumentSnapshot...snapshots) throws IOException {
    Writer writer = new StringWriter();
    for (DocumentSnapshot snapshot : snapshots) {
      String stringForm = snapshot.toString();
      if (stringForm == null) {
        throw new IllegalArgumentException(
            "DocumentSnapshot.toString returned null.");
      }
      writer.write(stringForm);
      writer.write(SnapshotWriter.RECORD_DELIMITER);
    }
    writer.flush();
    return new BufferedReader(new StringReader(writer.toString()));
  }

  public static class MissingIdSnapshot extends MockDocumentSnapshot {
    MissingIdSnapshot(String documentId, String extra) {
      super(documentId, extra);
    }

    @Override
    public String toString() {
      String correctResult = super.toString();
      return correctResult.replace(
          MockDocumentSnapshot.Field.DOCUMENT_ID.name(),
          MockDocumentSnapshot.Field.DOCUMENT_ID.name() + "_not");
    }
  }

  static class StringDocumentSnapshot implements DocumentSnapshot {
    private final String documentId;
    public StringDocumentSnapshot(String documentId) {
      this.documentId = documentId;
    }

    public String getDocumentId() {
      return documentId;
    }

    public DocumentHandle getUpdate(DocumentSnapshot onGsa) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return documentId;
    }
  }

  static class StringDocumentSnapshotFactory
      implements DocumentSnapshotFactory {

    public DocumentSnapshot fromString(String stringForm) {
      return new StringDocumentSnapshot(stringForm);
    }
  }
}