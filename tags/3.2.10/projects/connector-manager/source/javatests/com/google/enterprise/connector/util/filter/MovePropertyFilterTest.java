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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests MovePropertyFilter.
 */
public class MovePropertyFilterTest extends DocumentFilterTest {

  /** Creates a MovePropertyFilter. */
  protected Document createFilter() throws Exception {
    Map<String, String>renames = new HashMap<String, String>();
    renames.put(PROP1, "foo");
    renames.put(PROP3, "bar");
    return createFilter(renames, false);
  }

  protected Document createFilter(Map<String, String>renames,
      boolean overwrite) throws Exception {
    MovePropertyFilter factory = new MovePropertyFilter();
    factory.setPropertyNameMap(renames);
    factory.setOverwrite(overwrite);
    return factory.newDocumentFilter(createDocument());
  }

  /** Tests illegal state if configuration setters are not called. */
  public void testFactoryIllegalState() throws Exception {
    checkIllegalState(new MovePropertyFilter());
  }

  /** Tests for non-existent property should return null. */
  public void testNonExistentProperty() throws Exception {
    Map<String, String>renames = new HashMap<String, String>();
    renames.put("bar", "foo");
    Document filter = createFilter(renames, false);
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

  /** Test rename to existing property with no overwrite should augment the
      destination property values with those of the source property. */
  public void testDestinationExistsWithNoOverwrite() throws Exception {
    // Move PROP7 to PROP2, no overwrite
    Map<String, String>moves = new HashMap<String, String>();
    moves.put(PROP7, PROP2);

    // PROP2 + PROP7 = PROP4
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP2, expectedProps.get(PROP4));
    expectedProps.remove(PROP7);
    checkDocument(createFilter(moves, false), expectedProps);
  }

  /** Test rename to existing property with overwrite should replace the
      destination property values with those of the source property. */
  public void testDestinationExistsWithOverwrite() throws Exception {
    // Move PROP2 to PROP1, with overwrite
    Map<String, String>moves = new HashMap<String, String>();
    moves.put(PROP2, PROP1);

    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP1, expectedProps.get(PROP2));
    expectedProps.remove(PROP2);

    checkDocument(createFilter(moves, true), expectedProps);
  }

  /** Test toString(). */
  public void testToString() {
    // We need to preserve the insertion order for the string comparison.
    Map<String, String> aliases = new LinkedHashMap<String, String>();
    aliases.put(PROP1, "foo");
    aliases.put(PROP3, "bar");
    MovePropertyFilter factory = new MovePropertyFilter();
    factory.setPropertyNameMap(aliases);
    factory.setOverwrite(true);
    assertEquals("MovePropertyFilter: ({foo=property1, bar=property3} , true)",
        factory.toString());
  }
}
