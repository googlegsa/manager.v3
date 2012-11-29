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

package com.google.enterprise.connector.mock;

import com.google.enterprise.connector.mock.MockRepositoryEvent.EventType;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.logging.Logger;

/**
 * Unit tests for Mock Document Store
 */
public class MockRepositoryDocumentStoreTest extends TestCase {
  private static final Logger logger =
    Logger.getLogger(MockRepositoryDocumentStoreTest.class.getName());
/**
 * Adds and deletes a few documents, checking integrity, size and content
 * along the way
 */
  public void testIntegrity() {
    int expectedSize = 0;

    MockRepositoryDocumentStore s = new MockRepositoryDocumentStore();

    Assert.assertTrue("Store should have size " + expectedSize
      + " actual size: " + s.size(), s.size() == expectedSize);

    MockRepositoryEvent e1 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc1",
                              "now is the time",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(1));
    doDocumentTest(s, "doc1", "now is the time", 1, e1);

    MockRepositoryEvent e2 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc2",
                              "now was the time",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(2));
    doDocumentTest(s, "doc2", "now was the time", 2, e2);

    MockRepositoryEvent e3 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc2",
                              "the time is now",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(3));
    doDocumentTest(s, "doc2", "the time is now", 2, e3);

    MockRepositoryEvent e4 =
      new MockRepositoryEvent(EventType.DELETE,
                              "doc2",
                              null,
                              null,
                              new MockRepositoryDateTime(4));
    doDocumentTest(s, "doc1", "now is the time", 1, e4);
  }

/**
 * Runs a series of tests given a single event and some expected values.
 * First, the event is applied.  The store itself checks its own integrity.
 * Next, we make sure that the store has the size we expect, make sure we
 * can find a supplied docid and make sure its content is as expected
 * @param s The store being tested
 * @param docid
 * @param expectedContent
 * @param expectedSize
 * @param e1 event to apply
 */
  private void doDocumentTest(MockRepositoryDocumentStore s,
                              String docid,
                              String expectedContent,
                              int expectedSize,
                              MockRepositoryEvent e1) {
    MockRepositoryDocument d;
    s.applyEvent(e1);
    Assert.assertTrue("Store should have size " + expectedSize
      + " actual size: " + s.size(), (s.size() == expectedSize));
    d = s.getDocByID(docid);
    Assert.assertTrue("Document " + docid + " not found in repository!",
      (d != null));
    Assert.assertTrue("Document " + docid + " has unexpected content!",
      (d.getContent().equals(expectedContent)));
  }

  public void testQuery() {
    MockRepositoryDocumentStore s = new MockRepositoryDocumentStore();

    MockRepositoryEvent e1 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc1",
                              "now is the time",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(1));
    doDocumentTest(s, "doc1", "now is the time", 1, e1);

    MockRepositoryEvent e2 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc2",
                              "now was the time",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(2));
    doDocumentTest(s, "doc2", "now was the time", 2, e2);

    MockRepositoryEvent e3 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc3",
                              "the time is now",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(3));
    doDocumentTest(s, "doc3", "the time is now", 3, e3);

    MockRepositoryEvent e4 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc4",
                              "the time is now",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(4));
    doDocumentTest(s, "doc4", "the time is now", 4, e4);

    MockRepositoryEvent e5 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc5",
                              "the time is now",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(5));
    doDocumentTest(s, "doc5", "the time is now", 5, e5);

    String[] expectedResults = {"doc3", "doc4", "doc5"};
    doQueryTest(s,
                new MockRepositoryDateTime(3),
                new MockRepositoryDateTime(6),
                expectedResults);

    String[] expectedResults2 = {"doc2", "doc3"};
    doQueryTest(s,
                new MockRepositoryDateTime(2),
                new MockRepositoryDateTime(4),
                expectedResults2);
  }

  private void doQueryTest(MockRepositoryDocumentStore s,
                           MockRepositoryDateTime from,
                           MockRepositoryDateTime to,
                           String[] expectedResults) {
    int count = 0;
    boolean allMatch = true;
    boolean overflow = false;
    for (MockRepositoryDocument d : s.dateRange(from, to)) {
      if (count < expectedResults.length) {
        if (!d.getDocID().equals(expectedResults[count])) {
          logger.info("Query result " + count + " docid " + d.getDocID()
            + " expected " + expectedResults[count]);
          allMatch = false;
        }
      } else {
        overflow = true;
      }
      count++;
    }
    assertFalse("query results overflow: found " + count + " expected "
      + expectedResults.length, overflow);
    assertTrue("query results underflow: found " + count + " expected "
      + expectedResults.length, count == expectedResults.length);
    assertTrue("query results don't match expectations", allMatch);
  }

  public void testSimultaneousEvents() {
    MockRepositoryDocumentStore s = new MockRepositoryDocumentStore();

    doDocumentTest(s,
                   "doc1",
                   "now is the time",
                   1,
                   new MockRepositoryEvent(EventType.SAVE,
                                           "doc1",
                                           "now is the time",
                                           new MockRepositoryPropertyList(),
                                           new MockRepositoryDateTime(1)));

    doDocumentTest(s,
                   "doc2",
                   "now was the time",
                   2,
                   new MockRepositoryEvent(EventType.SAVE,
                                           "doc2",
                                           "now was the time",
                                           new MockRepositoryPropertyList(),
                                           new MockRepositoryDateTime(1)));

    doDocumentTest(s,
                   "doc3",
                   "now was the time",
                   3, new MockRepositoryEvent(EventType.SAVE,
                                              "doc3",
                                              "now was the time",
                                              new MockRepositoryPropertyList(),
                                              new MockRepositoryDateTime(1)));
  }
}
