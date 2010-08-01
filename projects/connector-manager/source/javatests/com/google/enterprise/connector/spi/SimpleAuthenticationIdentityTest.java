// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.spi;

import junit.framework.TestCase;

/**
 * Tests for the {@link SimpleAuthenticationIdentity} class.
 */
public class SimpleAuthenticationIdentityTest extends TestCase {

  public void testGetUserPass() {
    String username = "foo";
    String password = "bar";
    SimpleAuthenticationIdentity i =
        new SimpleAuthenticationIdentity(username, password);
    assertEquals(username, i.getUsername());
    assertEquals(password, i.getPassword());
    assertNull(i.getDomain());
  }

  public void testGetDomain() {
    String username = "foo";
    String password = "bar";
    String domain = "baz";
    SimpleAuthenticationIdentity i =
        new SimpleAuthenticationIdentity(username, password, domain);
    assertEquals(domain, i.getDomain());
    assertEquals(username, i.getUsername());
    assertEquals(password, i.getPassword());
  }
}
