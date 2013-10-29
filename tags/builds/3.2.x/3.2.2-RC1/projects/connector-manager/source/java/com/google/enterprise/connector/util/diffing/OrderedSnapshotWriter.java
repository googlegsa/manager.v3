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

import com.google.common.io.NullOutputStream;

import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper on the SnapshotWriter that enforces an ordering on the
 * written snapshots, to prevent duplicates being written to the
 * store. This is slightly hacky because SnapshotWriter is a concrete
 * implementation rather than an interface.
 */
class OrderedSnapshotWriter extends SnapshotWriter {
  private static final Logger LOG = Logger.getLogger(
      OrderedSnapshotWriter.class.getName());

  private final SnapshotWriter delegate;

  /** The largest document snapshot we have seen. */
  private DocumentSnapshot maxWrittenSnapshot;

  public OrderedSnapshotWriter(SnapshotWriter delegate)
      throws SnapshotWriterException{
    // The superclass is not actually used, but the Writer argument
    // cannot be null. We use a null sink.
    super(new OutputStreamWriter(new NullOutputStream()), null, null);
    this.delegate = delegate;
    this.maxWrittenSnapshot = null;
  }

  @Override
  public void write(DocumentSnapshot snapshot)
      throws SnapshotWriterException, IllegalArgumentException {
    if (maxWrittenSnapshot == null
        || COMPARATOR.compare(snapshot, maxWrittenSnapshot) > 0) {
      maxWrittenSnapshot = snapshot;
      delegate.write(snapshot);
    } else {
      LOG.log(Level.WARNING, "Processed out of order document snapshot {0}, "
          + "not writing to store.", snapshot.getDocumentId());
    }
  }

  @Override
  public void close() throws SnapshotWriterException {
    delegate.close();
  }

  @Override
  public String getPath() {
    return delegate.getPath();
  }

  @Override
  public long getRecordCount() {
    return delegate.getRecordCount();
  }
}
