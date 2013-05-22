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

import com.google.enterprise.connector.util.diffing.DocumentSnapshot;
import com.google.enterprise.connector.util.diffing.SnapshotReader;
import com.google.enterprise.connector.util.diffing.SnapshotStoreException;
import com.google.enterprise.connector.util.diffing.SnapshotWriter;
import com.google.enterprise.connector.util.diffing.SnapshotWriterException;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * This relies on SnapshotReader to check results.
 *
 */
public class SnapshotWriterTest extends TestCase {
  private StringWriter sw;
  private SnapshotWriter writer;

  @Override
  public void setUp() throws Exception {
    sw = new StringWriter();
    writer = new SnapshotWriter(sw, null, "string");
  }

  public void testGetPath() {
    assertEquals("string", writer.getPath());
  }

  public void testOneRecord() throws SnapshotStoreException {
    MockDocumentSnapshot before = new MockDocumentSnapshot("0", "0.extra");
    writeAndClose(writer, before);

    SnapshotReader reader =
        new SnapshotReader(new BufferedReader(new StringReader(sw.toString())),
            "test", 8, new MockDocumentSnapshotFactory());
    DocumentSnapshot after = reader.read();
    assertEquals(before, after);
    assertNull(reader.read());
  }

  private void writeAndClose(SnapshotWriter snapshotWriter, DocumentSnapshot dss)
      throws SnapshotStoreException {
    boolean iMadeIt = false;
    try {
      snapshotWriter.write(dss);
      iMadeIt = true;
    } finally {
      snapshotWriter.close();
    }
  }

  public void testManyRecords() throws SnapshotStoreException {
    MockDocumentSnapshot[] before = new MockDocumentSnapshot[100];
    boolean iMadeIt = false;
    try {
      for (int k = 0; k < 100; ++k) {
        before[k] = new MockDocumentSnapshot(Integer.toString(k), "extra." + k);
        writer.write(before[k]);
      }
      iMadeIt = true;
    } finally {
      writer.close();
    }

    SnapshotReader reader =
        new SnapshotReader(new BufferedReader(new StringReader(sw.toString())),
            "test", 2, new MockDocumentSnapshotFactory());
    for (int k = 0; k < 100; ++k) {
      DocumentSnapshot dss = reader.read();
      assertEquals(before[k], dss);
    }
  }

  public void testProblemWriting() throws SnapshotStoreException {
    class FailingWriter extends FilterWriter {
      FailingWriter() {
        super(new StringWriter());
      }

      @Override
      public void write(String s) throws IOException {
        throw new IOException();
      }

      @Override
      public void write(int c) throws IOException {
        throw new IOException();
      }

      @Override
      public void write(char[] cbuf, int off, int len)
        throws IOException {
        throw new IOException();
      }

      @Override
      public void write(String str, int off, int len)
          throws IOException {
        throw new IOException();
      }

    }
    writer = new SnapshotWriter(new FailingWriter(), null, "string");

    MockDocumentSnapshot before = new MockDocumentSnapshot("0", "extra.0");
    try {
      writeAndClose(writer, before);
      fail("write worked!?");
    } catch (SnapshotWriterException expected) {
      // ignore
    }
  }

  public void testCount() throws SnapshotStoreException {
    try {
      for (int k = 0; k < 100; ++k) {
        DocumentSnapshot dss = new MockDocumentSnapshot(Integer.toString(k),
            "extra." + k);
        assertEquals(k, writer.getRecordCount());
        writer.write(dss);
        assertEquals(k + 1, writer.getRecordCount());
      }
    } finally {
      writer.close();
    }
    assertEquals(100, writer.getRecordCount());
  }
}
