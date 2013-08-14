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
 * Tests for the FileSizeLimitInfo class.
 */
public class FileSizeLimitInfoTest extends TestCase {
  private FileSizeLimitInfo fsli;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    fsli = new FileSizeLimitInfo();
  }

  /** Test maxDocumentSize too small. */
  public void testInvalidMaxDocumentSize() throws Exception {
    try {
      fsli.setMaxDocumentSize(0L);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }
  }

  /** Test maxDocumentSize too big. */
  public void testBigMaxDocumentSize() throws Exception {
    fsli.setMaxDocumentSize(Long.MAX_VALUE);
    assertTrue(fsli.maxDocumentSize() > 0L);
    assertFalse(fsli.maxDocumentSize() == Long.MAX_VALUE);
  }

  /** Test maxDocumentSize. */
  public void testMaxDocumentSize() throws Exception {
    assertTrue(fsli.maxDocumentSize() > 0L);
    fsli.setMaxDocumentSize(1000L);
    assertEquals(1000L, fsli.maxDocumentSize());
  }

  /** Test maxFeedSize too small. */
  public void testInvalidMaxFeedSize() throws Exception {
    try {
      fsli.setMaxFeedSize(0L);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }
  }

  /** Test maxFeedSize too big. */
  public void testBigMaxFeedSize() throws Exception {
    fsli.setMaxFeedSize(Long.MAX_VALUE);
    assertTrue(fsli.maxFeedSize() > 0L);
    assertFalse(fsli.maxFeedSize() == Long.MAX_VALUE);
  }

  /** Test maxFeedSize. */
  public void testMaxFeedSize() throws Exception {
    assertTrue(fsli.maxFeedSize() > 0L);
    fsli.setMaxFeedSize(1000L);
    assertEquals(1000L, fsli.maxFeedSize());
  }

  /** Test toString(). */
  public void testToString() throws Exception {
    fsli.setMaxDocumentSize(100L);
    fsli.setMaxFeedSize(200L);
    String fsliStr = fsli.toString();
    assertTrue(fsliStr.contains("100"));
    assertTrue(fsliStr.contains("200"));
  }
}
