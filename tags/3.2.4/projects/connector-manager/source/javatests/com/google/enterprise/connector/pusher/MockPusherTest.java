// Copyright 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.jcr.JcrTraversalManager;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.TraversalManager;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.jcr.query.QueryManager;

public class MockPusherTest extends TestCase {
  public void testSimple() throws Exception {
    MockRepositoryEventList mrel = new MockRepositoryEventList(
        "MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new JcrTraversalManager(qm);
    MockPusher pusher = new MockPusher(System.out);
    DocumentList documentList = qtm.startTraversal();
    int counter = 0;
    Document document = null;
    while ((document = documentList.nextDocument()) != null) {
      pusher.take(document);
      counter++;
    }
    Assert.assertEquals(4, counter);
    Assert.assertEquals(4, pusher.getTotalDocs());
    pusher.cancel();
    Assert.assertEquals(0, pusher.getTotalDocs());
  }
}
