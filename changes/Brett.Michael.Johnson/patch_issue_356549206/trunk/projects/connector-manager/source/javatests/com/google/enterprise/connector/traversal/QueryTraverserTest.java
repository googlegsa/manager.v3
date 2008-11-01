// Copyright (C) 2006-2008 Google Inc.
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

package com.google.enterprise.connector.traversal;

import com.google.enterprise.connector.jcr.JcrTraversalManager;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.spi.TraversalManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.jcr.query.QueryManager;

/**
 * Tests for {@link com.google.enterprise.connector.traversal.QueryTraverser}.
 */
public class QueryTraverserTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.traversal.QueryTraverser
   * #runBatch(int)}.
   * @throws InterruptedException 
   */
  public final void testRunBatch() throws InterruptedException {
    runTestBatches(0);
    runTestBatches(1);
    runTestBatches(2);
    runTestBatches(3);
    runTestBatches(4);
    runTestBatches(5);
    
  }

  private void runTestBatches(int batchSize) throws InterruptedException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    String connectorName = "foo";
    ConnectorStateStore connectorStateStore = new MockConnectorStateStore();
    Traverser traverser = 
      createTraverser(mrel, connectorName, connectorStateStore);

    System.out.println();
    System.out.println("Running batch test batchsize " + batchSize);
    
    int docsProcessed = -1;
    int totalDocsProcessed = 0;
    int batchNumber = 0;
    while (docsProcessed != 0) {
      boolean exceptionThrown = false;
      try {
        docsProcessed = traverser.runBatch(batchSize);
      } catch (IllegalArgumentException e) {
        exceptionThrown = true;
        Assert.assertTrue("Batch size = " + batchSize + "; " + e,
            batchSize <= 0);
        return;
      } finally {
        if (!exceptionThrown) {
          Assert.assertTrue(batchSize > 0);
        }
      }
      totalDocsProcessed += docsProcessed;
      System.out.println("Batch# " + batchNumber + " docs " + docsProcessed +
          " checkpoint " + connectorStateStore.getConnectorState(connectorName));
      batchNumber++;
    }
    Assert.assertEquals(4,totalDocsProcessed);
  }

  /**
   * Create a Traverser.
   * @param mrel
   * @param connectorName
   * @param connectorStateStore
   * @return
   */
  private Traverser createTraverser(MockRepositoryEventList mrel,
      String connectorName, ConnectorStateStore connectorStateStore) {
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());

    TraversalManager qtm = new JcrTraversalManager(qm);
    MockPusher pusher = new MockPusher(System.out);

    Traverser traverser =
        new QueryTraverser(pusher, qtm, connectorStateStore, connectorName);
    return traverser;
  }
  
  /**
   * Initialize a large file used for tests.  This is to avoid
   * having giant files checked into the source code repository.
   *
   * @param fname the name of the large file to create (if it
   * doesn't already exist).
   * @throws IOException if creating the large file fails.
   */
  private void makeLargeFile(String fname) throws IOException {
    File largeFile = new File(fname);
    if (!largeFile.exists()) {
      byte[] text = "abcdefghijklmnopqrstuvwxyz\n".getBytes();
      FileOutputStream os = new FileOutputStream(largeFile);
      for (int i = 0; i < 1000000; i++) {
        os.write(text);
      }
      os.close();
    }
  }

  /**
   * Test that we are indeed streaming the file.
   */
  public final void testLargeFileStream() {
    try {
      // This has internal knowledge of the contents of 
      // MockRepositoryEventLogLargeFile.txt used below.
      makeLargeFile("testdata/tmp/largefile.txt");
    } catch (IOException e) {
      Assert.fail("Unable to initialize largefile.txt: " + e.toString());
    }

    MockRepositoryEventList mrel =
      new MockRepositoryEventList("MockRepositoryEventLogLargeFile.txt");
    String connectorName = "foo";
    ConnectorStateStore connectorStateStore = new MockConnectorStateStore();
    Traverser traverser = 
      createTraverser(mrel, connectorName, connectorStateStore);
    int docsProcessed = -1;
    while (docsProcessed != 0) {
      docsProcessed = traverser.runBatch(1);
    }

  }
}
