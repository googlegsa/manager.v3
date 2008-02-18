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
import com.google.enterprise.connector.mock.MockRepositoryEvent;
import com.google.enterprise.connector.mock.MockRepositoryEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

/**
 * This class implements the corresponding JCR interface.
 */
public class MockJcrObservationManager implements ObservationManager {

  private MockRepository repo;
  private Map subscriptions = Collections.synchronizedMap(new HashMap());
  
  /**
   * This class is a wrapper used to translate between the MockRepository events
   * and the JCR domain.
   */
  private class MockJcrEventListener implements MockRepositoryEventListener {

    private EventListener jcrListener;

    public MockJcrEventListener(EventListener jcrListener) {
      this.jcrListener = jcrListener;
    }

    /*
     * Translates the given MockRepositoryEvent into an iterator and calls the
     * JCR event listener associated with this instance.
     */
    public void onEvent(MockRepositoryEvent event) {
      List eventList = new LinkedList();
      eventList.add(event);
      EventIterator eventIter = new MockJcrEventIterator(eventList.iterator());
      jcrListener.onEvent(eventIter);
    }
  }


  /**
   * Creates a MockJcrObservationManager from a MockRepository.
   */
  public MockJcrObservationManager(MockRepository repo) {
    this.repo = repo;
  }

  /**
   * Partially implements the addEventListener() interface.  Registers the given
   * listener, however, ignores all the parameters related to filtering.
   */
  public void addEventListener(EventListener listener, int eventTypes,
      String absPath, boolean isDeep, String[] uuid, String[] nodeTypeName,
      boolean noLocal) throws RepositoryException {
    MockRepositoryEventListener mockListener = 
        new MockJcrEventListener(listener);
    subscriptions.put(listener, mockListener);
    repo.getStore().addEventListener(mockListener);
  }

  /**
   * Deregisters the given listener.
   */
  public void removeEventListener(EventListener listener)
      throws RepositoryException {
    MockRepositoryEventListener mockListener = 
        (MockRepositoryEventListener) subscriptions.get(listener);
    if (mockListener != null) {
      repo.getStore().removeEventListener(mockListener);
      subscriptions.remove(listener);
    }
  }

  // The following methods are JCR level 1 - but we do not anticipate using them

  /**
   * Throws UnsupportedOperationException
   */
  public EventListenerIterator getRegisteredEventListeners()
      throws RepositoryException {
    throw new UnsupportedOperationException();
  }

}
