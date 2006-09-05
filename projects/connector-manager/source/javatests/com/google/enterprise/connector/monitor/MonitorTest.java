// Copyright 2006 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.monitor;

import junit.framework.TestCase;

import java.util.Map;
import java.util.TreeMap;

/**
 * Test of Monitor implementation.
 */
public class MonitorTest extends TestCase {
  public void testHashMapMonitor() {
    String key = "key";
    String value = "value";
    Map temp;
    Monitor monitor = new HashMapMonitor();

    assertTrue("Variables should be empty.", monitor.getVariables().isEmpty());

    temp = new TreeMap();
    temp.put(key, value);
    monitor.setVariables(temp);
    assertEquals("One variable should be set.", 
      1, monitor.getVariables().size());

    temp = monitor.getVariables();
    assertEquals("One variable should be set.", 
      1, temp.size());
    assertTrue("Key should be in variables.", temp.containsKey(key));
    assertTrue("Value should be in variables.", temp.containsValue(value));
  }
}
