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

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.Writer;

/**
 * Write snapshot records in CSV format.
 *
 * @since 2.8
 */
public class SnapshotWriter {
  static final char LENGTH_DELIMITER = '#';
  static final char RECORD_DELIMITER = '\n';
  protected String path;
  protected Writer output;
  protected long count;
  protected FileDescriptor fileDescriptor;

  /**
   * Creates a SnapshotWriter that appends to {@code output}.
   *
   * @param output CSV writer that is being wrapped
   * @param fileDescriptor if non-{code null}, this will be flushed after
   *        each record is written to disk
   * @param path name of output, for logging purposes
   * @throws SnapshotWriterException on any error
   */
  public SnapshotWriter(Writer output, FileDescriptor fileDescriptor,
      String path) throws SnapshotWriterException {
    this.output = new BufferedWriter(output);
    this.fileDescriptor = fileDescriptor;
    this.path = path;
    this.count = 0;
  }

  /**
   * Appends a record to the output stream.
   *
   * @param snapshot record to write
   * @throws SnapshotWriterException
   * @throws IllegalArgumentException
   */
  public void write(DocumentSnapshot snapshot) throws SnapshotWriterException,
      IllegalArgumentException {
    try {
      write(snapshot, output);
      if (fileDescriptor != null) {
        fileDescriptor.sync();
      }
      ++count;
    } catch (IOException e) {
      throw new SnapshotWriterException("failed to write snapshot record", e);
    }
  }

  @VisibleForTesting
  static void write(DocumentSnapshot snapshot, Writer writer)
      throws IOException, IllegalArgumentException {
    String stringForm = snapshot.toString();
    if (stringForm == null) {
      throw new IllegalArgumentException(
          "DocumentSnapshot.toString returned null.");
    }
    String length = Integer.toString(stringForm.length());
    writer.write(length);
    writer.write(LENGTH_DELIMITER);
    writer.write(stringForm);
    writer.write(RECORD_DELIMITER);
    writer.flush();
  }

  /**
   * Closes the underlying output stream.
   *
   * @throws SnapshotWriterException
   */
  public void close() throws SnapshotWriterException {
    try {
      output.close();
    } catch (IOException e) {
      throw new SnapshotWriterException("failed to close snapshot", e);
    }
  }

  /**
   * @return a path to the file this writer writes to
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the number of records successfully written.
   */
  public long getRecordCount() {
    return count;
  }

  @VisibleForTesting
  public Writer getOutput() {
    return output;
  }

  @VisibleForTesting
  public FileDescriptor getFileDescriptor() {
    return fileDescriptor;
  }
 }
