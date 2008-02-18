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

import junit.framework.TestCase;

import java.util.Iterator;

/**
 * Unit tests for Mock Repository
 */
public class MockRepositoryTest extends TestCase {

  /**
   * Simple creation sanity test
   */
  public void testSimpleRepository() {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    MockRepositoryDateTime dateTime = new MockRepositoryDateTime(60);
    assertTrue(r.getCurrentTime().compareTo(dateTime) == 0);
  }

  /**
   * Test advancing repository time
   */
  public void testRepositoryTimes() {
    // TODO(ziff): change this file access to use TestUtil
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel, new MockRepositoryDateTime(0));

    assertEquals(0, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(20));
    assertEquals(2, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(39));
    assertEquals(3, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(40));
    assertEquals(2, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(41));
    assertEquals(2, r.getStore().size());

    r.setTime(new MockRepositoryDateTime(100));
    assertEquals(4, r.getStore().size());
  }

  /**
   * Test pause.
   */
  public void testRepositoryPause() {
    // TODO(ziff): change this file access to use TestUtil
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog9.txt");
    MockRepository r = new MockRepository(mrel, new MockRepositoryDateTime(0));
    MockRepositoryDateTime targetTime = new MockRepositoryDateTime(60);

    assertEquals(0, r.getStore().size());

    // Edge case
    r.setTimeToTarget();
    assertEquals(0, r.getStore().size());

    r.setTime(targetTime);
    assertEquals(3, r.getStore().size());
    assertEquals(new MockRepositoryDateTime(35), r.getCurrentTime());

    r.setTimeToTarget();
    assertEquals(2, r.getStore().size());
    assertEquals(new MockRepositoryDateTime(45), r.getCurrentTime());

    r.setTimeToTarget();
    assertEquals(4, r.getStore().size());
    assertEquals(targetTime, r.getCurrentTime());

    // Test constructor that doesn't not specify the time
    r = new MockRepository(mrel);
    assertEquals(3, r.getStore().size());
    assertEquals(new MockRepositoryDateTime(35), r.getCurrentTime());

    r.setTimeToTarget();
    assertEquals(2, r.getStore().size());
    assertEquals(new MockRepositoryDateTime(45), r.getCurrentTime());

    r.setTimeToTarget();
    assertEquals(4, r.getStore().size());
    assertEquals(targetTime, r.getCurrentTime());
}

  /**
   * Make sure documents have exactly the attributes they should
   */
  public void testDocumentIntegrity() {
    MockRepositoryEventList mrel =
        new MockRepositoryEventList("MockRepositoryEventLog3.txt");
    MockRepository r = new MockRepository(mrel);
    MockRepositoryDocument doc = r.getStore().getDocByID("doc1");

    System.out.println();
    MockRepositoryPropertyList proplist = doc.getProplist();
    int counter = 0;
    for (Iterator i = proplist.iterator(); i.hasNext();) {
      MockRepositoryProperty property = (MockRepositoryProperty) i.next();
      System.out.print(property.toString());
      System.out.println();
      counter++;
    }
    assertEquals(2, counter);
  }


}
