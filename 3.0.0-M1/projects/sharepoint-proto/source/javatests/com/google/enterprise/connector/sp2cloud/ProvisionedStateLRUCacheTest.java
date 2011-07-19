// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sp2cloud;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit test for {@link ProvisionedStateLRUCache}.
 */
public class ProvisionedStateLRUCacheTest extends TestCase {
  private final int SIZE = 3;
  private ProvisionedStateLRUCache stateCache;
  private final static List<String> IDS = Collections.unmodifiableList(
      Arrays.asList("id0", "id1", "id2", "id3", "id4", "id5", "id6"));
  
  @Override
  public void setUp() {
    stateCache = new ProvisionedStateLRUCache(SIZE);
  }
  
  public void testGet_stateUnnown(){
    Boolean state = stateCache.getProvisionedState("i do not think or exist.");
    assertNull(state);
    assertEquals(1, stateCache.getMisses());
    assertEquals(0, stateCache.getHits());
  }
  
  public void testGet_provisioned(){
    stateCache.setProvisionedState(IDS.get(0), true);
    for (int ix = 0; ix < 4; ix++) {
      Boolean state = stateCache.getProvisionedState(IDS.get(0));
      assertNotNull(state);
      assertTrue(state);
      assertEquals(0, stateCache.getMisses());
      assertEquals(1 + ix, stateCache.getHits());
    }
  }
  
  public void testGet_notProvisioned() {
    stateCache.setProvisionedState(IDS.get(0), false);
    for (int ix = 0; ix < 4; ix++) {
      Boolean state = stateCache.getProvisionedState(IDS.get(0));
      assertNotNull(state);
      assertFalse(state);
      assertEquals(0, stateCache.getMisses());
      assertEquals(1 + ix, stateCache.getHits());
    }    
  }
  
  public void testGet_replace() {
    for (int ix = 0; ix < IDS.size(); ix++) {
      stateCache.setProvisionedState(IDS.get(ix), true);
    }
    for (int ix = 0; ix < IDS.size(); ix++) {
      Boolean state = stateCache.getProvisionedState(IDS.get(ix));
      if (ix + SIZE >= IDS.size()) {
        assertNotNull(state);
        assertTrue(state);
      } else {
        assertNull(state);
       }
    }
  }
  
  public void testGet_replaceWithAccess() {
    for (int ix = 0; ix < IDS.size(); ix++) {
      stateCache.setProvisionedState(IDS.get(ix), true);
      Boolean r = stateCache.getProvisionedState(IDS.get(0));
      assertNotNull(r);
      assertTrue(r);
    }
    for (int ix = 0; ix < IDS.size(); ix++) {
      Boolean state = stateCache.getProvisionedState(IDS.get(ix));
      if (ix == 0 || ix + SIZE - 1 >= IDS.size()) {
        assertNotNull(state);
        assertTrue(state);
      } else {
        assertNull(state);
       }
    }
  }
}
