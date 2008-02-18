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

import java.util.Iterator;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

/**
 * MockJcrEventIterator implements the corresponding JCR interface, with
 * these limitations:
 *
 * <ul>
 * <li> This is a "level 1" (read-only) implementation. All level 2
 * (side-effecting) calls throw UnsupportedOperation exceptions. These are
 * grouped at the bottom of the class implementation.
 * <li> Some level 1 calls are not implemented because they will never be used
 * by our connector infrastructure.  In this implementation, they also throw
 * UnsupportedOperation exceptions. These are grouped above the level 2 calls.
 * </ul>
 */
public class MockJcrEventIterator implements EventIterator {
  Iterator iter;
  Iterator internalIterator;

  public MockJcrEventIterator(Iterator iter) {
    internalIterator = iter;
    this.iter = new Iterator() {

      public boolean hasNext() {
        return internalIterator.hasNext();
      }

      public Object next() {
        return new MockJcrEvent((MockRepositoryEvent) internalIterator.next());
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
      
    };
  }

  public Event nextEvent() {
    return (Event) iter.next();
  }

  public boolean hasNext() {
    return iter.hasNext();
  }

  public Object next() {
    return iter.next();
  }

  // The following methods are JCR level 1 - but we do not anticipate using them

  public long getPosition() {
    throw new UnsupportedOperationException();
  }

  public long getSize() {
    throw new UnsupportedOperationException();
  }

  public void skip(long arg0) {
    throw new UnsupportedOperationException();
  }

  // The following methods are JCR level 2 - these would never be needed

  public void remove() {
    throw new UnsupportedOperationException();
  }

}
