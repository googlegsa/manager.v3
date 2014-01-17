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
import com.google.enterprise.connector.spi.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests AddPropertyFilter
 */
public class AddPropertyFilterTest extends DocumentFilterTest {

  /** Creates a AddPropertyFilter. */
  protected Document createFilter(boolean overwrite,
      String propertyName, String... values) throws Exception {
    AddPropertyFilter factory = new AddPropertyFilter();
    factory.setPropertyName(propertyName);
    factory.setOverwrite(overwrite);
    factory.setPropertyValues(Arrays.asList(values));
    return factory.newDocumentFilter(createDocument());
  }

  /** Tests the Factory constructor with illegal arguments. */
  public void testFactoryIllegalArgs() throws Exception {
    AddPropertyFilter factory = new AddPropertyFilter();
    try {
      factory.setPropertyName(null);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }

    try {
      factory.setPropertyName("");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }

    try {
      factory.setPropertyValues(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // Expected.
    }

    try {
      factory.setPropertyValue(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // Expected.
    }
  }

  /** Tests illegal state if configuration setters are not called. */
  public void testFactoryIllegalState() throws Exception {
    checkIllegalState(new AddPropertyFilter());
  }

  /** Test added property exists. */
  public void testAddedPropertyExists() throws Exception {
    Document filter = createFilter(false, "foo", "bar");
    assertNotNull(filter.findProperty("foo"));
  }

  /** Test added property shows up in the property names. */
  public void testAddedPropertyInPropertyNames() throws Exception {
    Document filter = createFilter(false, "foo", "bar");
    Set<String> names = filter.getPropertyNames();
    assertTrue(names.contains("foo"));

    // Make sure all the real properties are there too.
    assertTrue(names.containsAll(createProperties().keySet()));
  }

  /** Test added Property should return all the configured values. */
  public void testAddedPropertyValues() throws Exception {
    Document filter = createFilter(false, "foo", CLEAN_STRING, EXTRA_STRING);
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put("foo", expectedProps.get(PROP4));
    checkDocument(filter, expectedProps);
  }

  /** Test add values to existing property with no overwrite should augment
      the destination property values with the configured values. */
  public void testDestinationExistsWithNoOverwrite() throws Exception {
    // Add PROP7 to PROP2, no overwrite.
    Document filter = createFilter(false, PROP2, EXTRA_STRING);
    // PROP2 + PROP7 = PROP4
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP2, expectedProps.get(PROP4));
    checkDocument(filter, expectedProps);
  }

  /** Test add to existing property with overwrite should replace the
      destination property values with the configured values. */
  public void testDestinationExistsWithOverwrite() throws Exception {
    Document filter = createFilter(true, PROP1, CLEAN_STRING, EXTRA_STRING);
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP1, expectedProps.get(PROP4));
    checkDocument(filter, expectedProps);
  }

  /** Test toString(). */
  public void testToString() {
    AddPropertyFilter factory = new AddPropertyFilter();
    factory.setPropertyName("foo");
    factory.setPropertyValue("bar");
    factory.setOverwrite(true);
    assertEquals("AddPropertyFilter: (foo , [bar] , true)",
        factory.toString());
  }
}
