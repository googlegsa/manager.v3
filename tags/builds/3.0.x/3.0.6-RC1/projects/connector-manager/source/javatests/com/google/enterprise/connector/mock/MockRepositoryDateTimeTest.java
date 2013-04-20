// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.mock;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Unit tests for Mock time object.
 */
public class MockRepositoryDateTimeTest extends TestCase {
  /**
   * Tests the comparator
   */
  public void testOrdering() {
    MockRepositoryDateTime d1 = new MockRepositoryDateTime(10);
    MockRepositoryDateTime d2 = new MockRepositoryDateTime(20);
    MockRepositoryDateTime d3 = new MockRepositoryDateTime(30);
    Assert.assertTrue(d1.compareTo(d2) < 0);
    Assert.assertTrue(d2.compareTo(d2) == 0);
    Assert.assertTrue(d1.compareTo(d3) < 0);
    Assert.assertTrue(d3.compareTo(d2) > 0);
  }

  /**
   * Tests equals and hashCode
   */
  public void testEquals() {
    MockRepositoryDateTime d1 = new MockRepositoryDateTime(10);
    MockRepositoryDateTime d2 = new MockRepositoryDateTime(10);
    Assert.assertEquals(d1, d2);
    Assert.assertFalse(d1 == d2);
    Assert.assertEquals(d1.hashCode(), d2.hashCode());
  }
}
