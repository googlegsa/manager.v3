// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.persist;

import junit.framework.TestCase;

/**
 * Tests the equality, hash codes, and natural ordering of {@link
 * StoreContext} objects.
 */
public class StoreContextTest extends TestCase {
  /** Gets an array of test objects sorted by the natural ordering. */
  private static StoreContext[] getTestObjects() {
    return new StoreContext[] {
      new StoreContext("alice", "aliceType"),
      new StoreContext("alice", "bobType"),
      new StoreContext("bob", "aliceType"),
      new StoreContext("bob", "bobType"),
    };
  }

  private static final StoreContext[] LEFT = getTestObjects();
  private static final StoreContext[] RIGHT = getTestObjects();

  /**
   * Tests if the exception is thrown correctly when the connector name is null.
   */
  public void testNullConnectorName() {
    try {
      new StoreContext(null, "test");
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorName may not be null or empty.",
                   e.getMessage());
    }
  }

  /**
   * Tests if the exception is thrown correctly if the connector name is empty.
   */
  public void testEmptyConnectorName() {
    try {
      new StoreContext("", "test");
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorName may not be null or empty.",
                   e.getMessage());
    }
  }

  /**
   * Tests if the exception is thrown correctly when the type name is null.
   */
  public void testNullTypeName() {
    try {
      new StoreContext("test", null);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.typeName may not be null or empty.",
                   e.getMessage());
    }
  }

  /**
   * Tests if the exception is thrown correctly if the type name is empty.
   */
  public void testEmptyTypeName() {
    try {
      new StoreContext("test", "");
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.typeName may not be null or empty.",
                   e.getMessage());
    }
  }

  /**
   * Make sure our test arrays have distinct instances, so that an ==
   * comparison does not short-circuit the methods under test.
   */
  public void testTestData() {
    assertEquals(LEFT.length, RIGHT.length);
    for (int i = 0; i < LEFT.length; i++) {
      assertNotSame("Compare " + i, LEFT[i], RIGHT[i]);
    }
  }

  public void testEquals() {
    for (int i = 0; i < LEFT.length; i++) {
      for (int j = 0; j < RIGHT.length; j++) {
        assertEquals("Compare " + i + ", " + j,
            i == j, LEFT[i].equals(RIGHT[j]));
      }
    }
  }

  public void testHashCode() {
    for (int i = 0; i < LEFT.length; i++) {
      assertEquals("Compare " + i, LEFT[i].hashCode(), RIGHT[i].hashCode());
    }
  }

  public void testCompareTo() {
    for (int i = 0; i < LEFT.length; i++) {
      for (int j = 0; j < RIGHT.length; j++) {
        assertEquals("Compare " + i + ", " + j + " => "
            + LEFT[i].compareTo(RIGHT[j]),
            sgn(i - j), sgn(LEFT[i].compareTo(RIGHT[j])));
      }
    }
  }

  private int sgn(int n) {
    return n < 0 ? -1 : (n == 0 ? 0 : +1);
  }
}
