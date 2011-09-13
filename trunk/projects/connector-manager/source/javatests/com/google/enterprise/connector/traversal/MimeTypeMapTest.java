// Copyright 2007 Google Inc.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for the MimeTypeMap class.
 */
public class MimeTypeMapTest extends TestCase {

  private String[] mimeTypes = {"foo", "bar/baz"};
  Set<String> mimeTypesSet = ArrayAsSet(mimeTypes);
  private MimeTypeMap testMap;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testMap = new MimeTypeMap();
  }

  /** Test empty map returns the default unknown level for all. */
  public void testEmptyMimeTypeMap() {
    // Empty map should have all lookups return UnknownMimeType level.
    assertEquals(1, testMap.mimeTypeSupportLevel("foo"));
    assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    assertEquals(1, testMap.mimeTypeSupportLevel(null));
  }

  /** Test setUnknownMimeTypeSupportLevel(). */
  public void testSetUnknownMimeTypeSupportLevel() {
    assertEquals(1, testMap.mimeTypeSupportLevel("foo"));
    testMap.setUnknownMimeTypeSupportLevel(2);
    assertEquals(2, testMap.mimeTypeSupportLevel("foo"));
  }

  /** Test initMap with null mime types set. */
  public void testNullMimeTypesSet() {
    // Adding a null set of mime types to empty map results in empty map.
    // Empty map should have all lookups return UnknownMimeType level.
    testMap.initMimeTypes(null, 5);
    assertEquals(1, testMap.mimeTypeSupportLevel("foo"));
  }

  /** Test initMap with empty mime types set. */
  public void testEmptyMimeTypesSet() {
    // Adding an empty set of mime types to empty map results in empty map.
    // Empty map should have all lookups return UnknownMimeType level.
    testMap.initMimeTypes(new HashSet<String>(), 5);
    assertEquals(1, testMap.mimeTypeSupportLevel("foo"));
  }

  /** Test setting a support level < 3 gets bumpted to 3. */
  public void testSmallLevel() {
    // Make sure lowest value does not drop to <= 0, as
    // these are reserved for unsupported and excluded types.
    // Attempts to generate a set of values anchored at 1 or 2
    // should bump the anchor up to 3.
    testMap.initMimeTypes(mimeTypesSet, 2);
    assertEquals(3, testMap.mimeTypeSupportLevel("bar/baz"));
  }

  /** Test that all unsupported mimetypes return a rank of 0. */
  public void testUnsupportedMimeTypes() {
    testMap.setUnsupportedMimeTypes(mimeTypesSet);
    assertEquals(0, testMap.mimeTypeSupportLevel("foo"));
    assertEquals(0, testMap.mimeTypeSupportLevel("bar/baz"));
    assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    assertEquals(1, testMap.mimeTypeSupportLevel(null));
  }

  /** Test that all unsupported mimetypes return a rank of -1. */
  public void testExcludedMimeTypes() {
    testMap.setExcludedMimeTypes(mimeTypesSet);
    assertEquals(-1, testMap.mimeTypeSupportLevel("foo"));
    assertEquals(-1, testMap.mimeTypeSupportLevel("bar/baz"));
    assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    assertEquals(1, testMap.mimeTypeSupportLevel(null));
  }

  /** Test different support levels of supported mime types. */
  public void testSupportedMimeTypes() {
    // default for unspecified mime types is supported (1)
    testMap.setSupportedMimeTypes(mimeTypesSet);
    assertEquals(2, testMap.mimeTypeSupportLevel("foo"));
    assertEquals(4, testMap.mimeTypeSupportLevel("bar/baz"));
    assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    assertEquals(1, testMap.mimeTypeSupportLevel(null));
    testMap.setUnknownMimeTypeSupportLevel(0);
    // new the default is unsupported(0)
    assertTrue(testMap.mimeTypeSupportLevel("foo") > 1);
    assertTrue(testMap.mimeTypeSupportLevel("ibblefrix") <= 0);
    assertTrue(testMap.mimeTypeSupportLevel(null) <= 0);
  }

  /** Test vnd mime types are preferred over experimental. */
  public void testVndExp() {
    String[] mimeTypes = { "application/foo", "application/vnd.foo",
                           "x-foo/bar", "application/x-foo" };
    testMap.setSupportedMimeTypes(ArrayAsSet(mimeTypes));
    assertTrue(testMap.mimeTypeSupportLevel("application/foo") >
               testMap.mimeTypeSupportLevel("application/x-foo"));
    assertTrue(testMap.mimeTypeSupportLevel("application/foo") >
               testMap.mimeTypeSupportLevel("x-foo/bar"));
    assertTrue(testMap.mimeTypeSupportLevel("application/vnd.foo") >
               testMap.mimeTypeSupportLevel("application/foo"));
    assertEquals("application/vnd.foo",
                 testMap.preferredMimeType(ArrayAsSet(mimeTypes)));
  }

  /** Test ranking for all the categories. */
  public void testMixedSupportMimeTypes() {
    MimeTypeMap testMap = new MimeTypeMap();
    String[] preferredMimeTypes = {"foo/bar"};
    String[] supportedMimeTypes = {"foo/baz", "bar/baz"};
    String[] unsupportedMimeTypes = {"foo", "bar/cat"};
    String[] excludedMimeTypes = {"ignore", "bar/bar"};
    testMap.setPreferredMimeTypes(ArrayAsSet(preferredMimeTypes));
    testMap.setSupportedMimeTypes(ArrayAsSet(supportedMimeTypes));
    testMap.setUnsupportedMimeTypes(ArrayAsSet(unsupportedMimeTypes));
    testMap.setExcludedMimeTypes(ArrayAsSet(excludedMimeTypes));
    assertEquals(8, testMap.mimeTypeSupportLevel("foo/bar"));
    assertEquals(4, testMap.mimeTypeSupportLevel("foo/baz"));
    assertEquals(0, testMap.mimeTypeSupportLevel("foo/rat"));
    assertEquals(4, testMap.mimeTypeSupportLevel("bar/baz"));
    assertEquals(1, testMap.mimeTypeSupportLevel("bar/zoo"));
    assertEquals(0, testMap.mimeTypeSupportLevel("bar/cat"));
    assertEquals(-1, testMap.mimeTypeSupportLevel("bar/bar"));
    assertEquals(-1, testMap.mimeTypeSupportLevel("ignore/bar"));

    String[] testMimeTypes = {"foo/bar", "bar/baz", "bar/bar"};
    assertEquals("foo/bar",
                 testMap.preferredMimeType(ArrayAsSet(testMimeTypes)));
  }

  /** Test null or emptySet to preferredMimeType(). */
  public void testNullOrEmptyPreferredMimeTypeSet() {
    assertNull(testMap.preferredMimeType(null));
    assertNull(testMap.preferredMimeType(new HashSet<String>()));
  }

  private static Set<String> ArrayAsSet(String[] a) {
    return new HashSet<String>(Arrays.asList(a));
  }
}
