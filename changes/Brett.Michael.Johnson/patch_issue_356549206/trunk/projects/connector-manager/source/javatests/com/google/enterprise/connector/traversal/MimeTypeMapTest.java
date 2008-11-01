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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    Assert.assertEquals(0, testMap.mimeTypeSupportLevel("foo"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel(null));
  }

  public void testSupportedMimeTypes() {
    MimeTypeMap testMap = new MimeTypeMap();
    String[] mimeTypes = {"foo", "bar"};
    // default for unspecified mime types is supported (1)
    testMap.setSupportedMimeTypes(ArrayAsSet(mimeTypes));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("foo"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("ibblefrix"));
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel(null));
    testMap.setUnknownMimeTypeSupportLevel(0);
    // new the default is unsupported(0)
    Assert.assertEquals(1, testMap.mimeTypeSupportLevel("foo"));
    Assert.assertEquals(0, testMap.mimeTypeSupportLevel("ibblefrix"));
    Assert.assertEquals(0, testMap.mimeTypeSupportLevel(null));   
  }

  private static Set ArrayAsSet(String[] a) {
    List l = Arrays.asList(a);
    Set result = new HashSet();
    result.addAll(l);
    return result;
  }

}
