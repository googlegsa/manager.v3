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
import com.google.enterprise.connector.spi.Value;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests CopyPropertyFilter
 */
public class CopyPropertyFilterTest extends DocumentFilterTest {

  /** Creates a CopyPropertyFilter. */
  protected Document createFilter() throws Exception {
    Map<String, String> aliases = new HashMap<String, String>();
    aliases.put(PROP1, "foo");
    aliases.put(PROP3, "bar");
    return createFilter(aliases, false);
  }

  protected Document createFilter(Map<String, String>aliases,
      boolean overwrite) throws Exception {
    CopyPropertyFilter factory = new CopyPropertyFilter();
    factory.setPropertyNameMap(aliases);
    factory.setOverwrite(overwrite);
    return factory.newDocumentFilter(createDocument());
  }

  /** Tests the Factory constructor with illegal arguments. */
  public void testFactoryIllegalArgs() throws Exception {
    try {
      createFilter(null, false);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // Expected.
    }
  }

  /** Tests illegal state if configuration setters are not called. */
  public void testFactoryIllegalState() throws Exception {
    checkIllegalState(new CopyPropertyFilter());
  }

  /** Tests for non-existent property should return null. */
  public void testNonExistentProperty() throws Exception {
    Map<String, String>aliases = new HashMap<String, String>();
    aliases.put("foo", "bar");
    Document filter = createFilter(aliases, false);
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

  /** Test toString(). */
  public void testToString() {
    // We need to preserve the insertion order for the string comparison.
    Map<String, String> aliases = new LinkedHashMap<String, String>();
    aliases.put(PROP1, "foo");
    aliases.put(PROP3, "bar");
    CopyPropertyFilter factory = new CopyPropertyFilter();
    factory.setPropertyNameMap(aliases);
    factory.setOverwrite(true);
    assertEquals("CopyPropertyFilter: ({foo=property1, bar=property3} , true)",
        factory.toString());
  }

  /** Test copy to existing property with no overwrite should augment the
      destination property values with those of the source property. */
  public void testDestinationExistsWithNoOverwrite() throws Exception {
    // Move PROP7 to PROP2, no overwrite.
    Map<String, String>copies = new HashMap<String, String>();
    copies.put(PROP7, PROP2);

    // PROP2 + PROP7 = PROP4
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP2, expectedProps.get(PROP4));
    checkDocument(createFilter(copies, false), expectedProps);
  }

  /** Test copy to existing property with overwrite should replace the
      destination property values with those of the source property. */
  public void testDestinationExistsWithOverwrite() throws Exception {
    // Copy PROP2 to PROP1, with overwrite.
    Map<String, String>copies = new HashMap<String, String>();
    copies.put(PROP2, PROP1);

    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP1, expectedProps.get(PROP2));
    checkDocument(createFilter(copies, true), expectedProps);
  }
}
