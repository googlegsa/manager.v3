// Copyright 2008 Google Inc. All Rights Reserved.
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

import com.google.enterprise.connector.mock.MockRepositoryDateTime;
import com.google.enterprise.connector.mock.MockRepositoryEvent;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;
import com.google.enterprise.connector.mock.MockRepositoryEvent.EventType;

import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

/**
 *
 */
public class MockJcrEventTest extends TestCase {

  /**
   * Simple test
   * @throws RepositoryException 
   */
  public void testSimpleCase() throws RepositoryException {
    MockRepositoryEvent e1 =
      new MockRepositoryEvent(EventType.SAVE,
                              "doc1",
                              "now is the time",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(1));
    MockJcrEvent je1 = new MockJcrEvent(e1);
    assertEquals(e1.getDocID(), je1.getPath());
    assertEquals(Event.NODE_ADDED, je1.getType());

    MockRepositoryEvent e2 =
      new MockRepositoryEvent(EventType.DELETE,
                              "doc2",
                              null,
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(4));
    MockJcrEvent je2 = new MockJcrEvent(e2);
    assertEquals(e2.getDocID(), je2.getPath());
    assertEquals(Event.NODE_REMOVED, je2.getType());

    MockRepositoryEvent e3 =
      new MockRepositoryEvent(EventType.METADATA_ONLY_SAVE,
                              "doc1",
                              "now is the time",
                              new MockRepositoryPropertyList(),
                              new MockRepositoryDateTime(1));
    MockJcrEvent je3 = new MockJcrEvent(e3);
    assertEquals(e3.getDocID(), je3.getPath());
    assertEquals(Event.PROPERTY_CHANGED, je3.getType());
  }
}
