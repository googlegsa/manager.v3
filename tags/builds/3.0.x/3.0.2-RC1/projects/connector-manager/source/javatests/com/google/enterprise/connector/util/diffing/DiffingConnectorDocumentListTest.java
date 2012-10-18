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
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.util.diffing.testing.TestDirectoryManager;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Test for {@link DiffingConnectorDocumentList}
 */
public class DiffingConnectorDocumentListTest extends TestCase {
  private File persistDir;

  private static class MockChangeSource implements ChangeSource {
    List<Change> original;
    LinkedList<Change> pending;

    /* @Override */
    public Change getNextChange() {
      return pending.poll();
    }

    /**
     * @param count
     * @return a list of {@code count} changes. The files in the changes have
     *         names "file.0", "file.1", etc. Files ending in even numbers are
     *         added; files ending in odd numbers are deleted.
     */
    private static List<Change> createChanges(int count) {
      List<Change> result = new ArrayList<Change>();
      for (int k = 0; k < count; ++k) {
        String id = String.format("file.%d", k);

        DocumentHandle dh = isEven(k)
            ? new MockDocumentHandle(id, String.format("contents of %s", id))
            : new DeleteDocumentHandle(id);
        Change.FactoryType factoryType = isEven(k)
            ? Change.FactoryType.CLIENT
            : Change.FactoryType.INTERNAL;

        MonitorCheckpoint mcp = new MonitorCheckpoint("foo", k, k + 1, k + 2);
        Change change = new Change(factoryType, dh, mcp);
        result.add(change);
      }
      return result;
    }

    MockChangeSource(int count) {
      original = createChanges(count);
      pending = new LinkedList<Change>();
      pending.addAll(original);
    }
  }

  private static boolean isEven(int ix) {
    return ix % 2 == 0;
  }

  @Override
  public void setUp() throws IOException {
    TestDirectoryManager testDirectoryManager = new TestDirectoryManager(this);
    persistDir = testDirectoryManager.makeDirectory("queue");
    assertTrue("Directory " + persistDir + " is not empty",
        persistDir.listFiles().length == 0);
  }

  public void testBasics() throws RepositoryException, IOException {
    MockChangeSource changeSource = new MockChangeSource(100);
    CheckpointAndChangeQueue checkpointAndChangeQueue =
        new CheckpointAndChangeQueue(changeSource, persistDir,
        new DeleteDocumentHandleFactory(), new MockDocumentHandleFactory());
    checkpointAndChangeQueue.setMaximumQueueSize(100);
    checkpointAndChangeQueue.start(null);
    DiffingConnectorDocumentList docs =
      new DiffingConnectorDocumentList(checkpointAndChangeQueue, null /* checkpoint */);
    for (int ix = 0; ix < changeSource.original.size(); ix++) {
      Change change = changeSource.original.get(ix);
      Document doc = docs.nextDocument();
      String docId =
        Value.getSingleValueString(doc, SpiConstants.PROPNAME_DOCID);
      assertEquals(change.getDocumentHandle().getDocumentId(),
          DocIdUtil.idToPath(docId));
      String expectAction = isEven(ix)
          ? SpiConstants.ActionType.ADD.toString()
          : SpiConstants.ActionType.DELETE.toString();
       assertEquals(expectAction,
          Value.getSingleValueString(doc, SpiConstants.PROPNAME_ACTION));
    }
    assertNull(docs.nextDocument());
  }

  public void testShortSource() throws RepositoryException, IOException {
    MockChangeSource changeSource = new MockChangeSource(50);
    CheckpointAndChangeQueue checkpointAndChangeQueue =
      new CheckpointAndChangeQueue(changeSource, persistDir,
      new DeleteDocumentHandleFactory(), new MockDocumentHandleFactory());
    checkpointAndChangeQueue.setMaximumQueueSize(50);
    checkpointAndChangeQueue.start(null);
    DiffingConnectorDocumentList docs =
        new DiffingConnectorDocumentList(checkpointAndChangeQueue, null /* checkpoint */);
    for (int k = 0; k < 50; ++k) {
      assertNotNull(docs.nextDocument());
    }
    assertNull(docs.nextDocument());
  }

  public void testEmptySource() throws RepositoryException, IOException {
    MockChangeSource changeSource = new MockChangeSource(0);
    CheckpointAndChangeQueue checkpointAndChangeQueue =
      new CheckpointAndChangeQueue(changeSource, persistDir,
      new DeleteDocumentHandleFactory(), new MockDocumentHandleFactory());
    checkpointAndChangeQueue.setMaximumQueueSize(0);
    checkpointAndChangeQueue.start(null);
    DiffingConnectorDocumentList docs =
        new DiffingConnectorDocumentList(checkpointAndChangeQueue, null /* checkpoint */);
    assertNull(docs.nextDocument());
  }
}
