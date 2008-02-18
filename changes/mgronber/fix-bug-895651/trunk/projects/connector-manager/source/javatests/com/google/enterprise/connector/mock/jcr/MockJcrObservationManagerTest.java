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

import com.google.enterprise.connector.mock.MockRepository;
import com.google.enterprise.connector.mock.MockRepositoryDateTime;
import com.google.enterprise.connector.mock.MockRepositoryEvent;
import com.google.enterprise.connector.mock.MockRepositoryEventList;
import com.google.enterprise.connector.mock.MockRepositoryPropertyList;
import com.google.enterprise.connector.mock.MockRepositoryEvent.EventType;

import junit.framework.TestCase;

import java.util.List;
import java.util.logging.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

/**
 * Unit tests for MockJcrObservationManager
 */
public class MockJcrObservationManagerTest extends TestCase {
  private static final Logger logger = Logger
      .getLogger(MockJcrObservationManagerTest.class.getName());
  
  private class TestListener implements EventListener {
    private MockRepositoryEvent expectedEvent;
    private String name;

    public TestListener(String name) {
      this.name = name;
    }

    public void setExpectedEvent(MockRepositoryEvent event) {
      this.expectedEvent = event;
    }

    public void onEvent(EventIterator eventIter) {
      int count = 0;
      while (eventIter.hasNext()) {
        Event event = (Event) eventIter.next();
        count++;
        logger.info(name + " received event:  " + event);
        try {
          assertEquals(expectedEvent.getDocID(), event.getPath());
          assertTypeEquals(expectedEvent.getType(), event.getType());
        } catch (RepositoryException e) {
          fail(e.getMessage());
        }
      }
      assertEquals(1, count);
    }

    private void assertTypeEquals(EventType mockType, int jcrType) {
      if (mockType == EventType.SAVE) {
        assertEquals(Event.NODE_ADDED, jcrType);
      } else if (mockType == EventType.DELETE) {
        assertEquals(Event.NODE_REMOVED, jcrType);
      } else if (mockType == EventType.METADATA_ONLY_SAVE) {
        assertEquals(Event.PROPERTY_CHANGED, jcrType);
      } else {
        fail("Unknown mock event type: " + mockType);
      }
    }
  }

  /**
   * Attaches an event listener to the ObservationManager and adds and deletes
   * a few documents, checking to make sure the listener is notified along the
   * way.
   * @throws RepositoryException 
   */  
  public void testListeners() throws RepositoryException {
    MockRepositoryEventList mrel = 
        new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    List eventList = mrel.getEventList();
    MockRepository r = new MockRepository(mrel, new MockRepositoryDateTime(0));
    ObservationManager om = new MockJcrObservationManager(r);
    TestListener listenerOne = new TestListener("listenerOne");
    TestListener listenerTwo = new TestListener("listenerTwo");

    // Add listeners
    om.addEventListener(listenerOne, 0, null, true, null, null, false);
    om.addEventListener(listenerTwo, 0, null, true, null, null, false);

    // Send events
    MockRepositoryEvent dummy =
        new MockRepositoryEvent(EventType.SAVE, 
                                "docX", 
                                "dummy",
                                new MockRepositoryPropertyList(), 
                                new MockRepositoryDateTime(0));
    MockRepositoryEvent event = (MockRepositoryEvent) eventList.get(0);
    listenerOne.setExpectedEvent(event);
    listenerTwo.setExpectedEvent(event);
    r.getStore().applyEvent(event);

    // Remove listeners
    om.removeEventListener(listenerTwo);

    // Send events
    event = (MockRepositoryEvent) eventList.get(1);
    listenerOne.setExpectedEvent(event);
    listenerTwo.setExpectedEvent(dummy);
    r.getStore().applyEvent(event);

    // Change listeners
    om.removeEventListener(listenerOne);
    om.addEventListener(listenerTwo, 0, null, true, null, null, false);

    // Send events
    event = (MockRepositoryEvent) eventList.get(2);
    listenerOne.setExpectedEvent(dummy);
    listenerTwo.setExpectedEvent(event);
    r.getStore().applyEvent(event);

    // Change listeners again
    om.addEventListener(listenerOne, 0, null, true, null, null, false);

    // Send events
    event = (MockRepositoryEvent) eventList.get(3);
    listenerOne.setExpectedEvent(event);
    listenerTwo.setExpectedEvent(event);
    r.getStore().applyEvent(event);

    event = (MockRepositoryEvent) eventList.get(4);
    listenerOne.setExpectedEvent(event);
    listenerTwo.setExpectedEvent(event);
    r.getStore().applyEvent(event);

    event = (MockRepositoryEvent) eventList.get(5);
    listenerOne.setExpectedEvent(event);
    listenerTwo.setExpectedEvent(event);
    r.getStore().applyEvent(event);
  }
}
