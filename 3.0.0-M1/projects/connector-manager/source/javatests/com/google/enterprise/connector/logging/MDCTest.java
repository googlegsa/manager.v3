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
 * Unit Test for Mapped Diagnostic Context (MDC).
 */
public class MDCTest extends TestCase {
  private static final String key1 = "key1";
  private static final String value1 = "value1";
  private static final String key2 = "key2";
  private static final String value2 = "value2";
  private static final String key3 = "key3";
  private static final String value3 = "value3";

  @Override
  protected void tearDown() throws Exception {
    MDC.remove();
  }

  /** Test put/get and clear. */
  public void testPutGetClear() {
    String value;
    MDC.put(key1, value1);
    value = MDC.get(key1);
    assertEquals(value, value1);
    MDC.clear();
    value = MDC.get(key1);
    assertEquals(value, "");
  }

  /** Test put/get/re-put/get. */
  public void testPutGetPutGet() {
    MDC.clear();
    MDC.put(key1, value1);
    assertEquals(value1, MDC.get(key1));
    MDC.put(key1, value2);
    assertEquals(value2, MDC.get(key1));
  }

  /** Test put/get multiple keys/values. */
  public void testPutGetX2() {
    MDC.clear();
    MDC.put(key1, value1);
    assertEquals(value1, MDC.get(key1));
    MDC.put(key2, value2);
    assertEquals(value2, MDC.get(key2));
    // Make sure adding key2/value2 didn't mangle key1/value1.
    assertEquals(value1, MDC.get(key1));
    MDC.clear();
    assertEquals("", MDC.get(key1));
    assertEquals("", MDC.get(key2));
  }

  /** Test remove. */
  public void testRemove() {
    MDC.clear();
    MDC.put(key1, value1);
    assertEquals(value1, MDC.get(key1));
    MDC.put(key2, value2);
    assertEquals(value2, MDC.get(key2));

    MDC.remove(key1);
    assertEquals("", MDC.get(key1));
    assertEquals(value2, MDC.get(key2));

    MDC.remove(key2);
    assertEquals("", MDC.get(key2));
  }

  /** Test remove a key/value pair that don't exist. */
  public void testRemoveNotExists() {
    MDC.clear();
    assertEquals("", MDC.get(key3));
    MDC.remove(key3);
    assertEquals("", MDC.get(key3));
  }

  /** Test MDC values are different between threads. */
  public void testThreadLocal() {
    MDC.clear();
    MDC.put(key1, value1);
    assertEquals(value1, MDC.get(key1));

    Thread t = new OtherThread("MDCChildThread");
    t.start();
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {}
    assertEquals(value1, MDC.get(key1));
    MDC.put(key1, value3);
    assertEquals(value3, MDC.get(key1));

    // Make sure there is no cross-over.
    assertEquals("", MDC.get(key2));

    // Wait for child thread to exit.
    try {
      t.join();
    } catch (InterruptedException e) {}

    // Make sure our context is unmolested.
    assertEquals(value3, MDC.get(key1));
  }

  private class OtherThread extends Thread {
    public OtherThread(String name) {
      super(name);
    }

    @Override
    public void run() {
      // Make sure context is not inherited.
      assertEquals("", MDC.get(key1));

      MDC.put(key1, value2);
      assertEquals(value2, MDC.get(key1));

      MDC.put(key2, value2);
      assertEquals(value2, MDC.get(key2));

      try {
        Thread.sleep(75);
      } catch (InterruptedException e) {}
      assertEquals(value2, MDC.get(key1));
      assertEquals(value2, MDC.get(key2));

      MDC.remove();
    }
  }
}
