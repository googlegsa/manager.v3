// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.listener;

import com.google.enterprise.connector.manager.Context;

import junit.framework.TestCase;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

/**
 * Tests attaching an {@link ApplicationListener} to the {@link Context}.
 */
public class TestListenerTest extends TestCase {
  private static final String TEST_DIR = "testdata/contextTests/listener/";
  private static final String APPLICATION_CONTEXT = "applicationContext.xml";
  private static final String LISTENER_BEAN_NAME = "TestEventListener";

  private Context context;
  private TestListener testListener;

  protected void setUp() throws Exception {
    // Setup a Context to point to stand alone XML file with just the needed
    // beans.
    Context.refresh();
    context = Context.getInstance();
    context.setStandaloneContext(TEST_DIR + APPLICATION_CONTEXT, null);

    // Get the TestLister bean.
    testListener =
      (TestListener) getBean(LISTENER_BEAN_NAME, TestListener.class);
  }

  protected void tearDown() throws Exception {
    context = null;
    Context.refresh();
  }

  /**
   * Tests to make sure the name is set via the bean.
   */
  public void testGetListenerName() {
    assertEquals("getListenerName",
        "AppContextTestListener", testListener.getListenerName());
  }

  /**
   * Tests that listener is notified when appContext is initialized and when
   * specific event sent.
   */
  public void testOnApplicationEvent() {
    // Get the events from the listener.
    List eventQueue = testListener.pullEventsFromQueue();
    assertTrue("One message in queue", eventQueue.size() == 1);
    assertTrue("Message of type ContextRefreshedEvent",
        (eventQueue.get(0) instanceof ContextRefreshedEvent));

    // Make sure the queue is empty now - kind of testing the test.
    eventQueue = testListener.pullEventsFromQueue();
    assertTrue("No messages in queue", eventQueue.size() == 0);

    // Create a new message and send it.
    TestSource source = new TestSource("one name", "one value");
    TestEvent event = new TestEvent(source);
    context.publishEvent(event);
    // Then get events from the listener and check.
    eventQueue = testListener.pullEventsFromQueue();
    assertTrue("One message in queue", eventQueue.size() == 1);
    assertTrue("Message of type TestEvent",
        (eventQueue.get(0) instanceof TestEvent));
    assertEventContainsSource("Event contains source", source, event);
  }

  private void assertEventContainsSource(String message, TestSource source,
      TestEvent event) {
    assertTrue(message, (event.getSource() instanceof TestSource));
    TestSource eventSource = (TestSource) event.getSource();
    assertEquals(message, source.name, eventSource.name);
    assertEquals(message, source.value, eventSource.value);
  }

  private Object getBean(String name, Class requiredType) {
    return context.getRequiredBean(name, requiredType);
  }

  private class TestSource {
    String name;
    String value;

    public TestSource(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  private class TestEvent extends ApplicationEvent {
    public TestEvent(TestSource source) {
      super(source);
    }
  }
}
