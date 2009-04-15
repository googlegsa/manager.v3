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

package com.google.enterprise.connector.mock.jcr;

import com.google.enterprise.connector.mock.MockRepositoryDocumentStore;
import com.google.enterprise.connector.mock.MockRepositoryEvent;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.spi.Value;

import junit.framework.TestCase;

import java.util.Calendar;
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
  private static final String JCR_LAST_MODIFIED = "jcr:lastModified";
  private static final String JCR_UUID = "jcr:uuid";
  private static final Logger logger =
      Logger.getLogger(MockJcrNodeIteratorTest.class.getName());

  /**
   * Sanity test
   * @throws RepositoryException
   */
  public void testSimpleIterator() throws RepositoryException {
    MockRepositoryDocumentStore mrd = new MockRepositoryDocumentStore();
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    for (MockRepositoryEvent event : mrel.getEventList()) {
      mrd.applyEvent(event);
    }

    // create an node iterator over the entire store
    NodeIterator ni = new MockJcrNodeIterator(mrd.iterator());

    Node n;
    while (ni.hasNext()) {
      n = ni.nextNode();
      logger.info(JCR_UUID + " " + n.getProperty(JCR_UUID).getString());

      Property lastModifiedProperty = n.getProperty(JCR_LAST_MODIFIED);
      Calendar lastModifiedCalendar = lastModifiedProperty.getDate();
      String lastModifiedDateISO8601 =
        Value.calendarToIso8601(lastModifiedCalendar);
      logger.info(JCR_LAST_MODIFIED + " " + lastModifiedDateISO8601);

      Property p;
      PropertyIterator pi = n.getProperties();
      String indent = "  ";
      while (pi.hasNext()) {
        p = pi.nextProperty();
        String name = p.getName();
        if (JCR_UUID.equals(name) || JCR_LAST_MODIFIED.equals(name)) {
          continue;
        }
        logger.info(indent + p.getName() + " " + p.getString());
      }
    }
  }
}
