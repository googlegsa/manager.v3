// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.Iterator;
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
  private List eventQueue = new ArrayList();

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
  public List pullEventsFromQueue() {
    List result = new ArrayList();
    synchronized (eventQueue) {
      for (Iterator iter = eventQueue.iterator(); iter.hasNext(); ) {
        result.add(iter.next());
      }
      eventQueue.clear();
    }
    return result;
  }
}
