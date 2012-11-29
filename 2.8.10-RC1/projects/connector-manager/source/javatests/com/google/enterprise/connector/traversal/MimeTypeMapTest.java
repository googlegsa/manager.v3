// Copyright 2007-2009 Google Inc.
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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for the MimeTypeMap class.
 */
public class MimeTypeMapTest extends TestCase {

  public void testEmptyMimeTypeMap() {
    MimeTypeMap testMap = new MimeTypeMap();
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("foo"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel(null));
  }

  public void testUnsupportedMimeTypes() {
    MimeTypeMap testMap = new MimeTypeMap();
    String[] mimeTypes = {"foo", "bar"};
    testMap.setUnsupportedMimeTypes(ArrayAsSet(mimeTypes));
    Assert.assertTrue(testMap.mimeTypeSupportLevel("foo") == 0);
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel(null));
  }

  public void testExcludedMimeTypes() {
    MimeTypeMap testMap = new MimeTypeMap();
    String[] mimeTypes = {"foo", "bar"};
    testMap.setExcludedMimeTypes(ArrayAsSet(mimeTypes));
    Assert.assertTrue(testMap.mimeTypeSupportLevel("foo") < 0);
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel(null));
  }

  public void testSupportedMimeTypes() {
    MimeTypeMap testMap = new MimeTypeMap();
    String[] mimeTypes = {"foo", "bar"};
    // default for unspecified mime types is supported (1)
    testMap.setSupportedMimeTypes(ArrayAsSet(mimeTypes));
    Assert.assertTrue(testMap.mimeTypeSupportLevel("foo") > 1);
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel(null));
    testMap.setUnknownMimeTypeSupportLevel(0);
    // new the default is unsupported(0)
    Assert.assertTrue(testMap.mimeTypeSupportLevel("foo") > 1);
    Assert.assertTrue(testMap.mimeTypeSupportLevel("ibblefrix") <= 0);
    Assert.assertTrue(testMap.mimeTypeSupportLevel(null) <= 0);
  }

  public void testMixedSupportMimeTypes() {
    MimeTypeMap testMap = new MimeTypeMap();
    String[] supportedMimeTypes = {"foo/baz", "bar/baz"};
    String[] unsupportedMimeTypes = {"foo", "bar/cat"};
    String[] excludedMimeTypes = {"ignore", "bar/bar"};
    testMap.setSupportedMimeTypes(ArrayAsSet(supportedMimeTypes));
    testMap.setUnsupportedMimeTypes(ArrayAsSet(unsupportedMimeTypes));
    testMap.setExcludedMimeTypes(ArrayAsSet(excludedMimeTypes));
    Assert.assertEquals(4, testMap.mimeTypeSupportLevel("foo/baz"));
    Assert.assertEquals(0, testMap.mimeTypeSupportLevel("foo/rat"));
    Assert.assertEquals(4, testMap.mimeTypeSupportLevel("bar/baz"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("bar/zoo"));
    Assert.assertEquals(0, testMap.mimeTypeSupportLevel("bar/cat"));
    Assert.assertTrue((testMap.mimeTypeSupportLevel("bar/bar") < 0));
    Assert.assertTrue((testMap.mimeTypeSupportLevel("ignore/bar") < 0));
  }

  private static Set<String> ArrayAsSet(String[] a) {
    return new HashSet<String>(Arrays.asList(a));
  }
}
