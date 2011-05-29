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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests CopyPropertyFilter
 */
public class CopyPropertyFilterTest extends DocumentFilterTest {

  /** Creates a CopyPropertyFilter. */
  protected static Document createFilter() {
    Map<String, String>aliases = new HashMap<String, String>();
    aliases.put("foo", PROP1);
    aliases.put("bar", PROP3);
    return createFilter(aliases);
  }

  protected static Document createFilter(Map<String, String>aliases) {
    CopyPropertyFilter factory = new CopyPropertyFilter();
    factory.setPropertyNameMap(aliases);
    return factory.newDocumentFilter(createDocument());
  }

  /** Tests the Factory constructor with illegal arguments. */
  public void testFactoryIllegalArgs() throws Exception {
    try {
      createFilter(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // Expected.
    }
  }

  /** Tests for non-existent property should return null. */
  public void testNonExistentProperty() throws Exception {
    Map<String, String>aliases = new HashMap<String, String>();
    aliases.put("foo", "bar");
    Document filter = createFilter(aliases);
    assertNull(filter.findProperty("foo"));
    assertNull(filter.findProperty("nonExistentProperty"));
  }

  /** Test aliases to existing properties are resolved at all. */
  public void testAliasesExist() throws Exception {
    Document filter = createFilter();
    assertNotNull(filter.findProperty("foo"));
    assertNotNull(filter.findProperty("bar"));
  }

  /** Test aliases show up in the property names. */
  public void testAliasesInPropertyNames() throws Exception {
    Document filter = createFilter();
    Set<String> names = filter.getPropertyNames();
    assertTrue(names.contains("foo"));
    assertTrue(names.contains("bar"));

    // Make sure all the real properties are there too.
    assertTrue(names.containsAll(createProperties().keySet()));
  }

  /** Test aliases should return all the values of the original property. */
  public void testAliases() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put("foo", expectedProps.get(PROP1));
    expectedProps.put("bar", expectedProps.get(PROP3));
    checkDocument(createFilter(), expectedProps);
  }
}
