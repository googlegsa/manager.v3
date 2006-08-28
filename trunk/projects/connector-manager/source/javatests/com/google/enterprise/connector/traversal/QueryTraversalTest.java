// Copyright (C) 2006 Google Inc.
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

import com.google.enterprise.connector.jcradaptor.SpiQueryTraversalManagerFromJcr;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.persist.ConnectorStateStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.spi.QueryTraversalManager;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.jcr.query.QueryManager;

/**
 * @author ziff@google.com (Your Name Here)
 * 
 */
public class QueryTraversalTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.traversal.QueryTraversal
   * #runBatch(int)}.
   * @throws InterruptedException 
   */
  public final void testRunBatch() throws InterruptedException {
    
    runTestBatches(1);
    runTestBatches(2);
    runTestBatches(3);
    runTestBatches(4);
    runTestBatches(5);
    
  }

  private void runTestBatches(int batchSize) throws InterruptedException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());

    String connectorName = "foo";
    QueryTraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);
    MockPusher pusher = new MockPusher(System.out);
    ConnectorStateStore connectorStateStore = new MockConnectorStateStore();

    TraversalMethod traversalMethod =
        new QueryTraversal(pusher, qtm, connectorStateStore, connectorName);

    System.out.println();
    System.out.println("Running batch test batchsize " + batchSize);
    
    int docsProcessed = -1;
    int totalDocsProcessed = 0;
    int batchNumber = 0;
    while (docsProcessed != 0) {
      docsProcessed = traversalMethod.runBatch(batchSize);
      totalDocsProcessed += docsProcessed;
      System.out.println("Batch# " + batchNumber + " docs " + docsProcessed +
          " checkpoint " + connectorStateStore.getConnectorState(connectorName));
      batchNumber++;
    }
    Assert.assertEquals(4,totalDocsProcessed);
  }

}
