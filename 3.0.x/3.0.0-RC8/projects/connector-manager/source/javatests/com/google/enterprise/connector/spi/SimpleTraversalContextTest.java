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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for the {@link SimpleTraversalContext} class.
 * <p>
 * Note that we are always passing immutable sets to the
 * {@code TraversalContext}, so we are implicitly testing to make sure
 * that {@code SimpleTraversalContext} does not modify the collections
 * it receives.
 */
public class SimpleTraversalContextTest extends TestCase {
  private SimpleTraversalContext context;

  @Override
  protected void setUp() {
    context = new SimpleTraversalContext();
  }

  public void testMaxDocumentSize() {
    long testSize = 1024;
    assertFalse(testSize == context.maxDocumentSize());
    context.setMaxDocumentSize(testSize);
    assertEquals(testSize, context.maxDocumentSize());
  }

  public void testNoMimeTypes() {
    String testType = "text/plain";
    assertEquals(1, context.mimeTypeSupportLevel(testType));
  }

  public void testEmptyMimeTypes() {
    String testType = "text/plain";
    context.setMimeTypeSet(Collections.<String>emptySet());
    assertEquals(0, context.mimeTypeSupportLevel(testType));
  }

  public void testSupportedMimeType() {
    String testType = "text/plain";
    context.setMimeTypeSet(Collections.singleton(testType));
    assertEquals(1, context.mimeTypeSupportLevel(testType));
  }

  public void testUnsupportedMimeType() {
    String testType = "text/plain";
    String supportedType = "text/html";
    context.setMimeTypeSet(Collections.singleton(supportedType));
    assertEquals(0, context.mimeTypeSupportLevel(testType));
  }

  public void testNullPreferredMimeTypes() {
    String testType = "text/plain";
    assertEquals(testType,
        context.preferredMimeType(Collections.singleton(testType)));
  }

  public void testEmptyPreferredMimeTypes() {
    String testType = "text/plain";
    context.setMimeTypeSet(Collections.<String>emptySet());
    assertNull(context.preferredMimeType(Collections.singleton(testType)));
  }

  public void testDisjointPreferredMimeTypes() {
    String testType = "text/plain";
    String supportedType = "text/html";
    context.setMimeTypeSet(Collections.singleton(supportedType));
    assertNull(context.preferredMimeType(Collections.singleton(testType)));
  }

  public void testPreferredMimeTypes() {
    String testType = "text/plain";
    String[] supportedTypes = { testType, "test/html" };
    String[] availableTypes = { "application/pdf", testType };
    context.setMimeTypeSet(asSet(supportedTypes));
    assertEquals(testType, context.preferredMimeType(asSet(availableTypes)));
  }

  /**
   * Constructs an immutable set from the elements of the given array.
   */
  private Set<String> asSet(String[] values) {
    Set<String> valueSet = new HashSet<String>();
    for (String value : values) {
      valueSet.add(value);
    }
    return Collections.unmodifiableSet(valueSet);
  }
}
