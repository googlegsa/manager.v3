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

import com.google.enterprise.connector.util.diffing.ChangeQueue.DefaultCrawlActivityLogger;

import junit.framework.TestCase;

/**
 * Test for {@link ChangeQueue}
 */
public class ChangeQueueTest extends TestCase {
  private static final MonitorCheckpoint MCP = new MonitorCheckpoint("foo", 0, 1L, 2L);

  ChangeQueue queue;
  DocumentSnapshotRepositoryMonitor.Callback callback;

  @Override
  public void setUp() {
    this.queue = new ChangeQueue(10, 0L, new DefaultCrawlActivityLogger());
    this.callback = queue.newCallback();
  }

  public void testNewDocument() throws Exception {
    MockDocumentHandle mdh = new MockDocumentHandle("id", "extra");
    callback.newDocument(mdh, MCP);
    Change c = queue.getNextChange();
    assertEquals(Change.FactoryType.CLIENT.name(),
        c.getJson().get(Change.Field.FACTORY_TYPE.name()));
    assertEquals(mdh, c.getDocumentHandle());
    assertEquals(MCP, c.getMonitorCheckpoint());
  }

  public void testDelete() throws Exception {
    DeleteDocumentHandle ddh = new DeleteDocumentHandle("id");
    callback.deletedDocument(ddh, MCP);
    Change c = queue.getNextChange();
    assertEquals(Change.FactoryType.INTERNAL.name(),
        c.getJson().get(Change.Field.FACTORY_TYPE.name()));
    assertEquals(ddh, c.getDocumentHandle());
    assertEquals(MCP, c.getMonitorCheckpoint());
  }

  public void testChange() throws Exception {
    MockDocumentHandle mdh = new MockDocumentHandle("id", "extra");
    callback.changedDocument(mdh, MCP);
    Change c = queue.getNextChange();
    assertEquals(Change.FactoryType.CLIENT.name(),
        c.getJson().get(Change.Field.FACTORY_TYPE.name()));
    assertEquals(mdh, c.getDocumentHandle());
    assertEquals(MCP, c.getMonitorCheckpoint());
  }

  public void testEmptyQueue() throws Exception {
    MockDocumentHandle mdhFoo = new MockDocumentHandle("foo", "extra");
    MockDocumentHandle mdhBar = new MockDocumentHandle("foo", "extra");

    callback.newDocument(mdhFoo, MCP);
    callback.newDocument(mdhBar, MCP);
    assertEquals(mdhFoo, queue.getNextChange().getDocumentHandle());
    assertEquals(mdhBar, queue.getNextChange().getDocumentHandle());
    assertNull(queue.getNextChange());
    assertNull(queue.getNextChange());
  }

  public void testSynchronization() {
    // Set up a thread to provide a nearly infinite stream of changes.
    Thread adder = new Thread() {
      @Override
      public void run() {
        for (int k = 0; k < Integer.MAX_VALUE && !isInterrupted(); ++k) {
          try {
            callback.newDocument(new MockDocumentHandle(
                String.format("/root/%d", k), ""), MCP);
          } catch (InterruptedException e) {
            return;
          }
        }
      }
    };
    adder.start();

    // Take the first 1000 changes. Make sure the queue is FIFO.
    int count = 0;
    for (int k = 0; k < 1000; ++k) {
      Change c = queue.getNextChange();
      if (c != null) {
        assertEquals(String.format("/root/%d", count),
            c.getDocumentHandle().getDocumentId());
        ++count;
      }
    }
    // Interrupt the thread.
    adder.interrupt();
  }
}
