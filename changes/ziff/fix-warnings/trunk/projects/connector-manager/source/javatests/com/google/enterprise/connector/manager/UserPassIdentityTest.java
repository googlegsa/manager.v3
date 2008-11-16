// Copyright 2008 Google Inc.
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

package com.google.enterprise.connector.manager;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Set;

/**
 * Tests for the {@link UserPassIdentity} class.
 */
public class UserPassIdentityTest extends TestCase {

  public void testGetUserPass() {
    String username = "foo";
    String password = "bar";
    UserPassIdentity i = new UserPassIdentity(username, password);
    assertEquals(username, i.getUsername());
    assertEquals(password, i.getPassword());
  }

  public void testCookies() {
    String username = "argle-bargle";
    String password = "xyzzy";
    UserPassIdentity i = new UserPassIdentity(username, password);
    try {
      i.setCookie(null, "foo");
      fail();
    } catch (IllegalArgumentException e) { // expected
    }
    i.setCookie("foo", "bazfaz");
    assertEquals("bazfaz", i.getCookie("foo"));
    Set cookieNames = i.getCookieNames();
    assertEquals(1, cookieNames.size());
    Iterator iter = cookieNames.iterator();
    while (iter.hasNext()) {
      String cookie = (String) iter.next();
      assertEquals(cookie,"foo");
    }
    assertEquals("bazfaz", i.setCookie("foo", null));
    assertNull(i.getCookie("foo"));
  }
}
