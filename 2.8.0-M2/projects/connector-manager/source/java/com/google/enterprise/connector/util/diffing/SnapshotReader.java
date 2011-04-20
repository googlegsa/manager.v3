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


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;

/**
 * Reader {@link SnapshotStore}.
 */
public class SnapshotReader {
  private final String inputPath;
  private final BufferedReader in;
  private final long snapshotNumber;
  private final DocumentSnapshotFactory documentSnapshotFactory;

  private long recordNumber;
  private boolean done;
  /**
   * @param in input for the reader
   * @param inputPath path to the snapshot
   * @param snapshotNumber the number of the snapshot being read
   * @throws SnapshotReaderException
   */
  public SnapshotReader(BufferedReader in, String inputPath, long snapshotNumber,
      DocumentSnapshotFactory documentSnapshotFactory)
      throws SnapshotReaderException {
    this.in = in;
    this.inputPath = inputPath;
    this.recordNumber = 0;  //1 based.
    this.snapshotNumber = snapshotNumber;
    this.documentSnapshotFactory = documentSnapshotFactory;
  }

  /**
   * @return the next record in this snapshot, or null if we have reached the
   *         end of the snapshot.
   * @throws SnapshotReaderException
   */
  public DocumentSnapshot read() throws SnapshotReaderException {
      String stringForm = readStringForm();
      return parseDocumentSnapshot(stringForm);
  }

  private String readStringForm() throws SnapshotReaderException {
    if (done) {
      throw new IllegalStateException();
    }
    String stringForm = null;
    try {
      int length = readLength();
      if (length > 0) {
        stringForm = readString(length);
        readRecordDeleimiter(stringForm);
      }
    } catch (IOException ioe) {
      throw new SnapshotReaderException(
          String.format("failed to read snapshot record (%s, line %d)",
              inputPath, recordNumber + 1), ioe);
    } finally {
      recordNumber++;
      if (stringForm == null) {
        done = true;
      }
    }
    return stringForm;
  }

  int readLength() throws SnapshotReaderException, IOException {
    StringBuilder sb = new StringBuilder();
    int c;
    while ((c=in.read()) > 0 && c != SnapshotWriter.LENGTH_DELIMITER) {
      sb.append((char)c);
    }
    if (sb.length() == 0) {
      if (c == SnapshotWriter.LENGTH_DELIMITER) {
        throw new SnapshotReaderException(String.format(
            "failed to read snapshot record with missing length (%s, line %d)",
            inputPath, recordNumber));
      }
      return -1;
    } else {
      try {
        if (c != SnapshotWriter.LENGTH_DELIMITER) {
          throw new SnapshotReaderException(String.format(
              "failed to read snapshot record with missing length delimiter "
              + "(%s, line %d)", inputPath, recordNumber));
        }
        return Integer.parseInt(sb.toString());
      } catch (NumberFormatException nfe) {
        throw new SnapshotReaderException(String.format(
            "failed to read snapshot record with invalid length "
            + "(%s, line %d, length %s)",
            inputPath, recordNumber, sb.toString()));
      }
    }
  }

  String readString(int length) throws IOException, SnapshotReaderException{
    CharBuffer cb = CharBuffer.allocate(length);
    while (in.read(cb) >= 0 && cb.hasRemaining()) {
      continue;
    }
    if (cb.hasRemaining()) {
      throw new SnapshotReaderException(String.format(
          "failed to read snapshot record with incomplete record "
          + "(%s, line %d, partial record %s)",
          inputPath, recordNumber, cb.flip().toString()));
    }
    return cb.flip().toString();
  }

  private void readRecordDeleimiter(String stringForm)
    throws SnapshotReaderException, IOException {
       int delim = in.read();
       if (delim != SnapshotWriter.RECORD_DELIMITER) {
         throw new SnapshotReaderException(String.format(
             "failed to read snapshot record missing record delmiter "
             + "(%s, line %d, stringForm %s)",
             inputPath, recordNumber, stringForm));
       }
  }

  private DocumentSnapshot parseDocumentSnapshot(String stringForm)
      throws SnapshotReaderException {
    if (stringForm == null) {
      return null;
    } else {
      try {
        return documentSnapshotFactory.fromString(stringForm);
      } catch (IllegalArgumentException iae) {
        throw new SnapshotReaderException(
            String.format("failed to parse snapshot (%s, line %d)",
                inputPath, recordNumber), iae);
      }
    }
  }

  /**
   * @return path to the input file, for logging purposes.
   */
  public String getPath() {
    return inputPath;
  }

  /**
   * @return the number of the most recently returned record.
   */
  public long getRecordNumber() {
    return recordNumber;
  }

  /**
   * Read and discard {@code number} records.
   *
   * @param number of records to skip.
   * @throws SnapshotReaderException on IO errors, or if there aren't enough
   *         records.
   * @throws InterruptedException it the calling thread is interrupted.
   */
  public void skipRecords(long number) throws SnapshotReaderException,
      InterruptedException {
    for (int k = 0; k < number; ++k) {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      if (readStringForm() == null) {
        throw new SnapshotReaderException(String.format(
            "failed to skip %d records; snapshot contains only %d",
            number, recordNumber));
        }
      }
  }

  /**
   * @return the number of the snapshot this reader is reading from.
   */
  public long getSnapshotNumber() {
    return snapshotNumber;
  }

  public void close() throws IOException {
    in.close();
  }
}
