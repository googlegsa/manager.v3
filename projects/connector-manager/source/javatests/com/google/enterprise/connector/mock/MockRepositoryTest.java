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

import junit.framework.TestCase;

/**
 * Unit tests for Mock Repository
 */
public class MockRepositoryTest extends TestCase {
  
  /**
   * Simple creation sanity test
   */  
  public void testSimpleRepository() {
    MockRepositoryEventList mrel = new MockRepositoryEventList(
      "MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel);
    MockRepositoryDateTime dateTime = new MockRepositoryDateTime(60);
    assertTrue(r.getCurrentTime().compareTo(dateTime) == 0);
  }
  
  /**
   * Test advancing repository time
   */
  public void testRepositoryTimes() {
    // TODO(ziff): change this file access to use TestUtil
    MockRepositoryEventList mrel = 
      new MockRepositoryEventList("MockRepositoryEventLog1.txt");
    MockRepository r = new MockRepository(mrel, 
      new MockRepositoryDateTime(0));
 
    assertEquals(0,r.getStore().size());
    
    r.setTime(new MockRepositoryDateTime(20));
    assertEquals(2, r.getStore().size());
    
    r.setTime(new MockRepositoryDateTime(39));
    assertEquals(3, r.getStore().size());
    
    r.setTime(new MockRepositoryDateTime(40));
    assertEquals(2, r.getStore().size());
    
    r.setTime(new MockRepositoryDateTime(41));
    assertEquals(2, r.getStore().size());
    
    r.setTime(new MockRepositoryDateTime(100));
    assertEquals(4, r.getStore().size());
  }
}
