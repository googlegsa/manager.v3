// Copyright (C) 2006-2009 Google Inc.
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

import java.util.List;
import java.util.ListIterator;

/**
 * MockRepository is the parent of a set of classes that implement an in-memory
 * Content Management System for testing.
 * <p>
 * The work-horse is the MockRepositoryDocumentStore class.  This class wraps
 * a document store with a list of events and it knows what time it is.  One
 * can move time along by calling appropriate methods.
 */
public class MockRepository {
  MockRepositoryDocumentStore store;
  MockRepositoryDateTime currentTime;
  MockRepositoryEventList eventList;
  ListIterator<MockRepositoryEvent> internalIterator;

  private void init() {
    store = new MockRepositoryDocumentStore();
    this.currentTime = new MockRepositoryDateTime(0);
    this.internalIterator = this.eventList.getEventList().listIterator();
  }

  /**
   * Creates a repository from a supplied event list and sets the time as
   * specified.  Typically, this would probably be zero - so the repository
   * would be empty, but you could advance time programmatically.
   * @param eventList Should be in increasing time order
   * @param time
   */
  public MockRepository(MockRepositoryEventList eventList,
      MockRepositoryDateTime time) {
    this.eventList = eventList;
    init();
    setTime(time);
  }

  /**
   * Creates a repository from a suppiled event list and sets the time to the
   * time of the last event.
   * @param eventList Should be in increasing time order
   */
  public MockRepository(MockRepositoryEventList eventList) {
    this.eventList = eventList;
    init();
    // set the time to be the time of the last event supplied
    List<MockRepositoryEvent> l = eventList.getEventList();
    MockRepositoryEvent lastEvent = l.get(l.size() - 1);
    setTime(lastEvent.getTimeStamp());
  }

  /**
   * resets the repository to empty and resets the time to zero
   */
  public void reinit() {
    init();
  }

  /**
   * Sets the time and applies all events that have timestamp less than or
   * equal to the supplied time
   * @param newTime
   */
  public void setTime(MockRepositoryDateTime newTime) {
    if (newTime.compareTo(currentTime) > 0) {
      while (internalIterator.hasNext()) {
        MockRepositoryEvent e = internalIterator.next();
        if (e.getTimeStamp().compareTo(newTime) > 0) {
          internalIterator.previous();
          break;
        }
        store.applyEvent(e);
      }
      currentTime = newTime;
    }
  }

  /**
   * @return The current time of this repository
   */
  public MockRepositoryDateTime getCurrentTime() {
    return currentTime;
  }

  /**
   * @return The underlying MockRepositoryStore
   */
  public MockRepositoryDocumentStore getStore() {
    return store;
  }

}
