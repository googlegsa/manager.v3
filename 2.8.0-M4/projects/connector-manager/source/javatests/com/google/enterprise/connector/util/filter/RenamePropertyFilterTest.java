// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.util.filter;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests RenamePropertyFilter.
 */
public class RenamePropertyFilterTest extends DocumentFilterTest {

  /** Creates a RenamePropertyFilter. */
  protected static Document createFilter() {
    Map<String, String>renames = new HashMap<String, String>();
    renames.put("foo", PROP1);
    renames.put("bar", PROP3);
    return createFilter(renames);
  }

  protected static Document createFilter(Map<String, String>renames) {
    RenamePropertyFilter factory = new RenamePropertyFilter();
    factory.setPropertyNameMap(renames);
    return factory.newDocumentFilter(createDocument());
  }

  /** Tests for non-existent property should return null. */
  public void testNonExistentProperty() throws Exception {
    Map<String, String>renames = new HashMap<String, String>();
    renames.put("foo", "bar");
    Document filter = createFilter(renames);
    assertNull(filter.findProperty("foo"));
    assertNull(filter.findProperty("nonExistentProperty"));
  }

  /** Test renames to existing properties are resolved at all. */
  public void testRenamedPropertiesExist() throws Exception {
    Document filter = createFilter();
    assertNotNull(filter.findProperty("foo"));
    assertNotNull(filter.findProperty("bar"));
  }

  /** Test renames show up in the property names and old names do not. */
  public void testRenamedInPropertyNames() throws Exception {
    Document filter = createFilter();
    Set<String> names = filter.getPropertyNames();
    assertTrue(names.contains("foo"));
    assertTrue(names.contains("bar"));
    assertFalse(names.contains(PROP1));
    assertFalse(names.contains(PROP3));

    // Make sure all the real properties are there too.
    Set<String> origNames = new HashSet<String>(createProperties().keySet());
    origNames.remove(PROP1);
    origNames.remove(PROP3);
    assertTrue(names.containsAll(origNames));
  }

  /** Test renames should return all the values of the original property. */
  public void testRenamedProperties() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put("foo", expectedProps.get(PROP1));
    expectedProps.put("bar", expectedProps.get(PROP3));
    expectedProps.remove(PROP1);
    expectedProps.remove(PROP3);
    checkDocument(createFilter(), expectedProps);
  }
}
