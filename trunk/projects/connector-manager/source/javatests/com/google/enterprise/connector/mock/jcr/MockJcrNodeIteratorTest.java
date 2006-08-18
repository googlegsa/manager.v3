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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepositoryDocumentStore;
import com.google.enterprise.connector.mock.MockRepositoryEvent;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.MockRepositoryPropertyTest;

import junit.framework.TestCase;

import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * Unit tests for MockJcrNodeIterator
 */
public class MockJcrNodeIteratorTest extends TestCase {
  private static final Logger logger = 
    Logger.getLogger(MockRepositoryPropertyTest.class.getName());
  
  /**
   * Sanity test
   * @throws RepositoryException
   */
  public void testSimpleIterator() throws RepositoryException {
    MockRepositoryDocumentStore mrd = new MockRepositoryDocumentStore();
    MockRepositoryEventList mrel = 
      new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    for (MockRepositoryEvent e: mrel.getEventList()) {
      mrd.applyEvent(e);
    }

    // create an node iterator over the entire store
    NodeIterator ni = new MockJcrNodeIterator(mrd);
    
    Node n;
    while (ni.hasNext()) {
      n = ni.nextNode();
      logger.info("docid " + n.getProperty("jcr:uuid").getString());
      
      Property p;
      PropertyIterator pi = n.getProperties();
      String indent = "  ";
      while (pi.hasNext()) {
        p = pi.nextProperty();
        logger.info(indent + p.getName() + " " + p.getString());
      }
    }
  }
}
