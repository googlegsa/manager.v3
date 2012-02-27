// Copyright 2010 Google Inc. All Rights Reserved.
package com.google.enterprise.connector.sp2cloud;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU Cache to record if a user or group is provisioned.
 */
public class ProvisionedStateLRUCache {
  private final HashMap<String, Boolean> stateMap;
  private int hits;
  private int misses;
  ProvisionedStateLRUCache(final int size) {
    this.stateMap = new LinkedHashMap<String, Boolean>(16, .75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
         return size() > size;
     }
    };
  }

  /**
   * Returns {@link Boolean#TRUE} if the specified id has been provisioned,
   * {@link Boolean#FALSE} if the specified id has not been provisioned and
   * returns null if the provisioned state of the specified id is not currently
   * held in this cache.
   */
  synchronized Boolean getProvisionedState(String id) {
    Boolean result = stateMap.get(id);
    if (result == null) {
      misses++;
    } else {
      hits++;
    }
    return result;
  }

  synchronized void setProvisionedState(String id, boolean state) {
   stateMap.put(id, state);
  }

  synchronized int getHits() {
    return hits;
  }

  synchronized int getMisses() {
    return misses;
  }
}

