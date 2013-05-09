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

package com.google.enterprise.connector.spi;

import junit.framework.TestCase;

/**
 * Tests the expected root causes and messages of exceptions
 * constructed in various ways.
 */
public class RepositoryExceptionTest extends TestCase {
  public void testCause() {
    Throwable cause = new Exception("root cause");
    Throwable t = new RepositoryException(cause);
    assertSame(cause, t.getCause());
    assertEquals("root cause", t.getMessage());
  }

  public void testMessage() {
    Throwable t = new RepositoryException("repository exception");
    assertNull(t.getCause());
    assertEquals("repository exception", t.getMessage());
  }

  public void testMessageCause() {
    Throwable cause = new Exception("root cause");
    Throwable t = new RepositoryException("repository exception", cause);
    assertSame(cause, t.getCause());
    assertEquals("repository exception: root cause", t.getMessage());
  }

  public void testInitCause() {
    Throwable cause = new Exception("root cause");
    Throwable t = new RepositoryException();
    t.initCause(cause);
    assertSame(cause, t.getCause());
    assertEquals("root cause", t.getMessage());
  }

  public void testMessageInitCause() {
    Throwable cause = new Exception("root cause");
    Throwable t = new RepositoryException("repository exception");
    t.initCause(cause);
    assertSame(cause, t.getCause());
    assertEquals("repository exception: root cause", t.getMessage());
  }
}
