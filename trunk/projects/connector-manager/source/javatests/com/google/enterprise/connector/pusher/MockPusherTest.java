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

package com.google.enterprise.connector.pusher;

import com.google.enterprise.connector.jcradaptor.SpiQueryTraversalManagerFromJcr;
import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Iterator;

import javax.jcr.query.QueryManager;

public class MockPusherTest extends TestCase {
  public void testSimple() throws RepositoryException {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    QueryManager qm = new MockJcrQueryManager(r.getStore());
    TraversalManager qtm = new SpiQueryTraversalManagerFromJcr(qm);
    
    MockPusher pusher = new MockPusher(System.out);

    {
      ResultSet resultSet = qtm.startTraversal();

      int counter = 0;
      for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
        PropertyMap propertyMap = (PropertyMap) iter.next();
        pusher.take(propertyMap, "junit");
        counter++;
      }
      Assert.assertEquals(4, counter);
      Assert.assertEquals(4, pusher.getTotalDocs());
    }
  }
}
