// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.traversal;

import junit.framework.TestCase;

/**
 * Tests for the BatchResult class.
 */
public class BatchResultTest extends TestCase {

  /** Test null delayPolicy. */
  public void testNullDelayPolicy() throws Exception {
    try {
      BatchResult result = new BatchResult(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }
  }

  /** Test the getters. */
  public void testGetters() throws Exception {
    BatchResult result =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 400L, 1000L);
    assertEquals(TraversalDelayPolicy.IMMEDIATE, result.getDelayPolicy());
    assertEquals(100, result.getCountProcessed());
    assertEquals(400L, result.getStartTime());
    assertEquals(1000L, result.getEndTime());
    assertEquals(600, result.getElapsedTime());
  }

  /** Test endTime less than or equal to startTime. Avoid divide by zero. */
  public void testBadEndTime() throws Exception {
    BatchResult result = new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100);
    assertEquals(0L, result.getStartTime());
    assertFalse(0L == result.getEndTime());
    assertTrue(0L < result.getElapsedTime());

    result = new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 1000L, 0L);
    assertEquals(1000L, result.getStartTime());
    assertTrue(result.getStartTime() < result.getEndTime());
    assertTrue(0L < result.getElapsedTime());
  }

  /** Test hashCode(). */
  public void testHashCode() throws Exception {
    BatchResult result0 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 400L, 1000L);
    BatchResult result1 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 400L, 1000L);
    BatchResult result2 =
        new BatchResult(TraversalDelayPolicy.POLL, 100, 400L, 1000L);
    BatchResult result3 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 200, 400L, 1000L);
    BatchResult result4 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 600L, 1000L);
    BatchResult result5 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 400L, 2000L);

    assertEquals(result0.hashCode(), result1.hashCode());
    assertFalse(result0.hashCode() == result2.hashCode());
    assertFalse(result0.hashCode() == result3.hashCode());
    assertFalse(result0.hashCode() == result4.hashCode());
    assertFalse(result0.hashCode() == result5.hashCode());
  }

  /** Test equals(). */
  public void testEquals() throws Exception {
    BatchResult result0 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 400L, 1000L);
    BatchResult result1 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 400L, 1000L);
    BatchResult result2 =
        new BatchResult(TraversalDelayPolicy.POLL, 100, 400L, 1000L);
    BatchResult result3 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 200, 400L, 1000L);
    BatchResult result4 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 600L, 1000L);
    BatchResult result5 =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 400L, 2000L);

    assertTrue(result0.equals(result0));
    assertTrue(result0.equals(result1));
    assertFalse(result0.equals(null));
    assertFalse(result0.equals(new String()));
    assertFalse(result0.equals(result2));
    assertFalse(result0.equals(result3));
    assertFalse(result0.equals(result4));
    assertFalse(result0.equals(result5));
  }

  /** Test toString(). */
  public void testToString() throws Exception {
    BatchResult result =
        new BatchResult(TraversalDelayPolicy.IMMEDIATE, 100, 0L, 3000L);
    String resultStr = result.toString();
    assertTrue(resultStr.contains("IMMEDIATE"));
    assertTrue(resultStr.contains("100"));
    assertTrue(resultStr.contains("3"));
  }
}
