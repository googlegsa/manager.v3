// Copyright 2009 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Used for testing adding an {@link ApplicationListener} to the
 * {@link ApplicationContext}.
 */
public class TestListener implements ApplicationListener {
  private static final Logger LOGGER =
      Logger.getLogger(TestListener.class.getName());

  private String listenerName = "TestListener";
  private List<ApplicationEvent> eventQueue =
      new ArrayList<ApplicationEvent>();

  public String getListenerName() {
    return listenerName;
  }

  public void setListenerName(String listenerName) {
    this.listenerName = listenerName;
  }

  /**
   * Logs the given event and stores it in an event queue.
   */
  public void onApplicationEvent(ApplicationEvent event) {
    LOGGER.info(listenerName + ": Entering onApplicationEvent...");
    LOGGER.info(listenerName + ": Event=" + event);
    synchronized (eventQueue) {
      eventQueue.add(event);
    }
    LOGGER.info(listenerName + ": ...exiting onApplicationEvent.");
  }

  /**
   * Pulls all the events out of the event queue and returns them in a list.
   * Has side effect of clearing out the current event queue.
   *
   * @return a List containing all the current events in the event queue.
   */
  public List<ApplicationEvent> pullEventsFromQueue() {
    List<ApplicationEvent> result = new ArrayList<ApplicationEvent>();
    synchronized (eventQueue) {
      for (ApplicationEvent event : eventQueue) {
        result.add(event);
      }
      eventQueue.clear();
    }
    return result;
  }
}
