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

import com.google.enterprise.connector.mock.MockRepositoryEvent;
import com.google.enterprise.connector.mock.MockRepositoryEvent.EventType;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

/**
 * MockJcrNode implements the corresponding JCR interface, with the following
 * exceptions:
 * 
 * <ul>
 * <li>The <code>getPath()</code> method returns the document id or uuid rather
 * than an absolute path of the item associated with this event.
 * <li>Some level 1 calls are not implemented because they will never be used
 * by our connector infrastructure.  In this implementation, they also throw
 * UnsupportedOperation exceptions.
 * </ul>
 */
public class MockJcrEvent implements Event {
  MockRepositoryEvent event;

  public MockJcrEvent(MockRepositoryEvent event) {
    this.event = event;
  }

  public String getPath() throws RepositoryException {
    return event.getDocID();
  }

  public int getType() {
    if (event.getType() == EventType.SAVE) {
      return Event.NODE_ADDED;
    } else if (event.getType() == EventType.DELETE) {
      return Event.NODE_REMOVED;
    } else if (event.getType() == EventType.METADATA_ONLY_SAVE) {
      return Event.PROPERTY_CHANGED;
    } else {
      throw new IllegalArgumentException("Unknown event type: " + 
          event.getType());
    }
  }

  // The following methods may be needed later but are temporarily
  // unimplemented

  public String getUserID() {
    throw new UnsupportedOperationException();
  }
}
