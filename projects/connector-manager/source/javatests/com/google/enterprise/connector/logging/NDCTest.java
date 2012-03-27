// Copyright 2009 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.logging;

import junit.framework.TestCase;

/**
 * Unit Test for Nested Diagnostic Context (NDC).
 */
public class NDCTest extends TestCase {
  private final String message1 = "message1";
  private final String message2 = "message2";
  private final String message3 = "message3";

  @Override
  protected void tearDown() throws Exception {
    NDC.remove();
  }

  /** Test clear(). */
  public void testClear() {
    NDC.clear();
    assertTrue(NDC.getDepth() == 0);
    assertEquals(NDC.peek(), "");

    NDC.push(message1);
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    NDC.clear();
    assertTrue(NDC.getDepth() == 0);
    assertEquals(NDC.peek(), "");
  }


  /** Test normal push/pop usage. */
  public void testPushPop() {
    NDC.clear();

    NDC.push(message1);
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    NDC.push(message2);
    assertEquals(message2, NDC.peek());
    assertTrue(NDC.getDepth() == 2);

    NDC.push(message3);
    assertEquals(message3, NDC.peek());
    assertTrue(NDC.getDepth() == 3);

    String value;

    value = NDC.pop();
    assertEquals(message3, value);
    assertEquals(message2, NDC.peek());
    assertTrue(NDC.getDepth() == 2);

    value = NDC.pop();
    assertEquals(message2, value);
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    value = NDC.pop();
    assertEquals(message1, value);
    assertEquals("", NDC.peek());
    assertTrue(NDC.getDepth() == 0);
  }

  /** Test pushAppend usage. */
  public void testPushAppend() {
    NDC.clear();

    NDC.pushAppend(message1);
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    NDC.pushAppend(message2);
    assertEquals(message1 + " " + message2, NDC.peek());
    assertTrue(NDC.getDepth() == 2);

    NDC.pushAppend(message3);
    assertEquals(message1 + " " + message2 + " " + message3, NDC.peek());
    assertTrue(NDC.getDepth() == 3);

    String value;

    NDC.pop();
    assertEquals(message1 + " " + message2, NDC.peek());
    assertTrue(NDC.getDepth() == 2);

    NDC.pop();
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    value = NDC.pop();
    assertEquals(message1, value);
    assertEquals("", NDC.peek());
    assertTrue(NDC.getDepth() == 0);
  }


  /** Test more pops than pushes. */
  public void testExtraPop() {
    NDC.clear();

    NDC.push(message1);
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    NDC.pop();
    assertEquals("", NDC.peek());
    assertTrue(NDC.getDepth() == 0);

    NDC.pop();
    assertEquals("", NDC.peek());
    assertTrue(NDC.getDepth() == 0);
  }

  /** Test NDC values are different between threads. */
  public void testThreadLocal() {
    NDC.clear();

    NDC.push(message1);
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    Thread t = new OtherThread("NDCChildThread");
    t.start();
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {}
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    NDC.push(message3);
    assertEquals(message3, NDC.peek());
    assertTrue(NDC.getDepth() == 2);

    // Wait for child thread to exit.
    try {
      t.join();
    } catch (InterruptedException e) {}

    // Make sure our context is unmolested.
    assertEquals(message3, NDC.peek());
    assertTrue(NDC.getDepth() == 2);
  }

  private class OtherThread extends Thread {
    public OtherThread(String name) {
      super(name);
    }

    @Override
    public void run() {
      assertTrue(NDC.getDepth() == 0);
      NDC.push(message2);
      assertEquals(message2, NDC.peek());
      assertTrue(NDC.getDepth() == 1);

      try {
        Thread.sleep(750);
      } catch (InterruptedException e) {}
      assertEquals(message2, NDC.peek());
      assertTrue(NDC.getDepth() == 1);

      NDC.remove();
    }
  }

  /** Test remove(). */
  public void testRemove() {
    NDC.clear();

    NDC.push(message1);
    assertEquals(message1, NDC.peek());
    assertTrue(NDC.getDepth() == 1);

    NDC.remove();
    // Bogus test, as these calls will recreate the NDC context stack.
    // But at least I can verify that the new context stack would be empty.
    assertTrue(NDC.getDepth() == 0);
    assertEquals(NDC.peek(), "");
  }
}
