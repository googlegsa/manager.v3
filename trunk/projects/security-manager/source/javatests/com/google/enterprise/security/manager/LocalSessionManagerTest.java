// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.security.manager;

import com.google.enterprise.security.manager.LocalSessionManager;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import junit.framework.TestCase;

/**
 * Tests to exercise the LocalSessionManager and LocalSession classes
 *
 */
public class LocalSessionManagerTest extends TestCase {

  public void testBasicFunctionality() {
    SessionManagerInterface sm = new LocalSessionManager();
    String sessionId = sm.createSession();
    assertNotNull(sessionId);
    assertTrue(sm.sessionExists(sessionId));
    String key = "foo";
    assertFalse(sm.keyExists(sessionId, key));
    String newValue = "bar";
    sm.setValue(sessionId, key, newValue);
    assertTrue(sm.keyExists(sessionId, key));
    assertEquals(newValue, sm.getValue(sessionId, key));
    sm.deleteSession(sessionId);
    assertFalse(sm.sessionExists(sessionId));
    try {
      sm.setValue(sessionId, key, newValue);
      fail();
    } catch (IndexOutOfBoundsException e) { // expected
    }
  }

}
