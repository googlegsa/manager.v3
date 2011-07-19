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

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of {@link DocumentList} for the {@link DiffingConnector}.
 *
 * @since 2.8
 */
public class DiffingConnectorDocumentList implements DocumentList {
  private final Iterator<CheckpointAndChange> checkpointAndChangeIterator;
  private String checkpoint;

  /**
   * Creates a document list that returns a batch of documents from the provided
   * {@link CheckpointAndChangeQueue}.
   *
   * @param queue a CheckpointAndChangeQueue containing document changes
   * @param checkpoint point into the change queue after which to start
   *        returning documents
   * @throws IOException if persisting fails
   */
  public DiffingConnectorDocumentList(CheckpointAndChangeQueue queue,
      String checkpoint) throws IOException {
    List<CheckpointAndChange> guaranteedChanges = queue.resume(checkpoint);
    checkpointAndChangeIterator = guaranteedChanges.iterator();
    this.checkpoint = checkpoint;
  }

  /* @Override */
  public String checkpoint() {
    return checkpoint;
  }

  /* @Override */
  public Document nextDocument() throws RepositoryException {
    if (checkpointAndChangeIterator.hasNext()) {
      CheckpointAndChange checkpointAndChange =
        checkpointAndChangeIterator.next();
      checkpoint = checkpointAndChange.getCheckpoint().toString();
      return checkpointAndChange.getChange().getDocumentHandle().getDocument();
    } else {
      return null;
    }
  }
}
