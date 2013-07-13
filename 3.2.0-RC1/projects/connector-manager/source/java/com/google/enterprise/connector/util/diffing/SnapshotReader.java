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
 * Reader for a {@link SnapshotStore}.
 *
 * @since 2.8
 */
public class SnapshotReader {

  private static final char START_JSON_CHAR = '{';
  
  private final String inputPath;
  private final BufferedReader in;
  private final long snapshotNumber;
  private final DocumentSnapshotFactory documentSnapshotFactory;
  private long recordNumber;
  private boolean done;
  private RecordReader recordReader;
  
  /**
   * Reads the snapshot file one record at a time. 
   */
  public interface RecordReader {
    
    /**
     * Reads one record at a time. If there are no records left, it returns null.
     * @return String form of the record
     * @throws SnapshotReaderException
     */
    String readRecord() throws SnapshotReaderException;
  }
  
  /**
   * Reads the records based on the length attribute present at the start of
   * each record.
   */
  private class LengthBasedRecordReader implements RecordReader {
    /* @Override */
    public String readRecord() throws SnapshotReaderException {
      String stringForm = null;
      try {
        int length = readLength();
        if (length > 0) {
          stringForm = readString(length);
          readRecordDelimiter(stringForm);
        }
      } catch (IOException ioe) {
        throw new SnapshotReaderException(
            String.format("failed to read snapshot record (%s, record %d)",
                inputPath, recordNumber + 1), ioe);
      } 
      return stringForm;
    }
    
    private int readLength() throws SnapshotReaderException, IOException {
      StringBuilder sb = new StringBuilder();
      int c;
      while ((c=in.read()) > 0 && c != SnapshotWriter.LENGTH_DELIMITER) {
        sb.append((char)c);
      }
      if (sb.length() == 0) {
        if (c == SnapshotWriter.LENGTH_DELIMITER) {
          throw new SnapshotReaderException(String.format(
              "failed to read snapshot record with missing length (%s, record %d)",
              inputPath, recordNumber));
        }
        return -1;
      } else {
        try {
          if (c != SnapshotWriter.LENGTH_DELIMITER) {
            throw new SnapshotReaderException(String.format(
                "failed to read snapshot record with missing length delimiter "
                + "(%s, record %d)", inputPath, recordNumber));
          }
          return Integer.parseInt(sb.toString());
        } catch (NumberFormatException nfe) {
          throw new SnapshotReaderException(String.format(
              "failed to read snapshot record with invalid length "
              + "(%s, record %d, length %s)",
              inputPath, recordNumber, sb.toString()));
        }
      }
    }

    private String readString(int length) throws IOException, SnapshotReaderException{
      CharBuffer cb = CharBuffer.allocate(length);
      while (in.read(cb) >= 0 && cb.hasRemaining()) {
        continue;
      }
      if (cb.hasRemaining()) {
        throw new SnapshotReaderException(String.format(
            "failed to read snapshot record with incomplete record "
            + "(%s, record %d, partial record %s)",
            inputPath, recordNumber, cb.flip().toString()));
      }
      return cb.flip().toString();
    }

    private void readRecordDelimiter(String stringForm)
      throws SnapshotReaderException, IOException {
         int delim = in.read();
         if (delim != SnapshotWriter.RECORD_DELIMITER) {
           throw new SnapshotReaderException(String.format(
               "failed to read snapshot record missing record delimiter "
               + "(%s, record %d, stringForm %s)",
               inputPath, recordNumber, stringForm));
         }
    }

  }
  
  /**
   * Reads the records assuming one line corresponds to one record. 
   */
  private class LineBasedRecordReader implements RecordReader {

    /* @Override */
    public String readRecord() throws SnapshotReaderException {
      String line = null;
      try {
        line = in.readLine();
      } catch (IOException e) {
        throw new SnapshotReaderException(String.format(
            "failed to read snapshot record (%s, record %d)", inputPath, recordNumber), e);
      }
      return line;
    }
  }

  /**
   * Constructs a SnapshotReader.
   *
   * @param in input for the reader
   * @param inputPath path to the snapshot
   * @param snapshotNumber the number of the snapshot being read
   * @throws SnapshotReaderException
   */
  public SnapshotReader(BufferedReader in, String inputPath,
      long snapshotNumber,
      DocumentSnapshotFactory documentSnapshotFactory)
      throws SnapshotReaderException {
    this.in = in;
    /* Initial version of File System connector (2.6) used JSON.
    When diffing library was extracted (2.8) the requirement was loosened to
    allow for arbitrary Strings.  We have two
    parser to support existing File System connector
    installations. */
    try {
      in.mark(2);
      int firstChar = in.read();
      in.reset();
      if (firstChar == START_JSON_CHAR) {
        recordReader = new LineBasedRecordReader();
      } else {
        recordReader = new LengthBasedRecordReader();
      }
    } catch (IOException ioe) {
      throw new SnapshotReaderException(
          String.format("failed to decide which record reader to use", ioe));
    }
    this.inputPath = inputPath;
    this.recordNumber = 0;  //1 based.
    this.snapshotNumber = snapshotNumber;
    this.documentSnapshotFactory = documentSnapshotFactory;
  }

  /**
   * @return the next record in this snapshot, or {@code null} if we have
   *         reached the end of the snapshot
   * @throws SnapshotReaderException
   */
  public DocumentSnapshot read() throws SnapshotReaderException {
    if (done) {
      throw new IllegalStateException();
    }
    String stringForm = null;
    try {
      stringForm = recordReader.readRecord();
    } finally {
      recordNumber++;
      if (stringForm == null) {
        done = true;
      }
    }
    return parseDocumentSnapshot(stringForm);
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
   * @return path to the input file, for logging purposes
   */
  public String getPath() {
    return inputPath;
  }

  /**
   * @return the number of the most recently returned record
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
      if (recordReader.readRecord() == null) {
        throw new SnapshotReaderException(String.format(
            "failed to skip %d records; snapshot contains only %d",
            number, recordNumber));
        }
      }
  }

  /**
   * @return the number of the snapshot this reader is reading from
   */
  public long getSnapshotNumber() {
    return snapshotNumber;
  }

  /**
   * Closes the underlying input stream.
   */
  public void close() throws IOException {
    in.close();
  }
}