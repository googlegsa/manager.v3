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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests ModifyPropertyFilter.
 */
public class ModifyPropertyFilterTest extends DocumentFilterTest {

  /** Creates a ModifyPropertyFilter. */
  protected Document createFilter(String propName, String pattern)
      throws Exception {
    return createFilter(propName, pattern, false);
  }

  /** Creates a ModifyPropertyFilter. */
  protected Document createFilter(
      String propName, String pattern, boolean overwrite)
      throws Exception {
    return createFilter(Collections.singleton(propName), pattern, overwrite);
  }

  /** Creates a ModifyPropertyFilter. */
  protected Document createFilter(
      Set<String> propNames, String pattern, boolean overwrite)
      throws Exception {
    ModifyPropertyFilter factory = new ModifyPropertyFilter();
    factory.setPropertyNames(propNames);
    factory.setPattern(pattern);
    factory.setReplacement(SPACE);
    factory.setOverwrite(overwrite);
    return factory.newDocumentFilter(createDocument());
  }

  /** Tests the Factory setters with illegal arguments. */
  public void testFactoryIllegalSetterArgs() throws Exception {
    ModifyPropertyFilter factory = new ModifyPropertyFilter();

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
      factory.setPropertyNames((Set<String>) null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // Expected.
    }

    try {
      factory.setPattern(null);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }

    try {
      factory.setPattern("");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }

    // Null or empty replacements are OK.
    factory.setReplacement(null);
    factory.setReplacement("");
  }

  /** Tests illegal state if configuration setters are not called. */
  public void testFactoryIllegalState() throws Exception {
    ModifyPropertyFilter factory = new ModifyPropertyFilter();

    // Test with neither propertyName, nor pattern set.
    checkIllegalStateFindProperty(factory);

    // Test with propertyName, but no pattern set.
    factory.setPropertyName(PROP1);
    checkIllegalStateFindProperty(factory);

    // Test with pattern, but no propertyName set.
    factory = new ModifyPropertyFilter();
    factory.setPattern(PATTERN);
    checkIllegalStateFindProperty(factory);
  }

  /** Tests for non-existent property should return null. */
  public void testNonExistentProperty() throws Exception {
    Document filter = createFilter(PROP1, PATTERN);
    assertNull(filter.findProperty("nonExistentProperty"));
  }

  public void testFilterNoMatchingProperty() throws Exception {
    Document filter = createFilter("nonExistentProperty", PATTERN);
    checkDocument(filter, createProperties());
  }

  /** Tests that the filter doesn't modify any Values that don't match. */
  public void testFilterNoMatchingValues() throws Exception {
    Document filter = createFilter(PROP1, "nonExistentPattern");
    checkDocument(filter, createProperties());
  }

  /**
   * Tests that the filter doesn't modify values in other properties
   * that do match.
   */
  public void testFilterOtherMatchingValuesInSingleValueProperty() throws Exception {
    Document filter = createFilter(PROP2, PATTERN);
    checkDocument(filter, createProperties());
  }

  /**
   * Tests that the filter doesn't modify values in other properties
   * that do match.
   */
  public void testFilterOtherMatchingValuesInMultiValueProperty() throws Exception {
    Document filter = createFilter(PROP4, PATTERN);
    checkDocument(filter, createProperties());
  }

  /** Tests that the filter changes the value in the target property. */
  public void testFilterMatchingValuesInSingleValueProperty() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP1, valueList(TEST_STRING, CLEAN_STRING));

    Document filter = createFilter(PROP1, PATTERN);
    checkDocument(filter, expectedProps);
  }

  /** Tests that the filter changes the value in the target property. */
  public void testFilterMatchingFirstValueInMultiValueProperty() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP3, valueList(TEST_STRING, CLEAN_STRING, EXTRA_STRING));

    Document filter = createFilter(PROP3, PATTERN);
    checkDocument(filter, expectedProps);
  }

  /** Tests that the filter changes the value in the target property. */
  public void testFilterMatchingLastValueInMultiValueProperty() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP5, valueList(CLEAN_STRING, TEST_EXTRA_STRING, EXTRA_STRING));

    Document filter = createFilter(PROP5, PATTERN);
    checkDocument(filter, expectedProps);
  }

  /** Tests that the filter changes the values in the target property. */
  public void testFilterMatchingValuesInMultiValueProperty() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP6, valueList(TEST_STRING, CLEAN_STRING, TEST_EXTRA_STRING, EXTRA_STRING));

    Document filter = createFilter(PROP6, PATTERN);
    checkDocument(filter, expectedProps);
  }

  /** Test overwriting single value (rather than add an additional value). */
  public void testOverwriteValueInSingleValueProperty() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP1, valueList(CLEAN_STRING));
    Document filter = createFilter(PROP1, PATTERN, true);
    checkDocument(filter, expectedProps);
  }

  /** Test overwriting values in a multi-valued property. */
  public void testOverwriteValueInMultiValueProperty() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP3, valueList(CLEAN_STRING, EXTRA_STRING));
    Document filter = createFilter(PROP3, PATTERN, true);
    checkDocument(filter, expectedProps);
  }

  /** Test overwriting all the values in a multi-valued property. */
  public void testOverwriteAllValuesInMultiValueProperty() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP6, valueList(CLEAN_STRING, EXTRA_STRING));
    Document filter = createFilter(PROP6, PATTERN, true);
    checkDocument(filter, expectedProps);
  }

  /** Test filtering multiple properties at once. */
  public void testMultipleProperties() throws Exception {
    Set<String> names = new HashSet<String>();
    names.add(PROP1);
    names.add(PROP6);
    Document filter = createFilter(names, PATTERN, true);

    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP1, valueList(CLEAN_STRING));
    expectedProps.put(PROP6, valueList(CLEAN_STRING, EXTRA_STRING));
    checkDocument(filter, expectedProps);
  }

  /** Test capturing groups. */
  public void testCaputuringGroups() throws Exception {
    ModifyPropertyFilter factory = new ModifyPropertyFilter();
    factory.setPropertyName(PROP4);
    factory.setPattern("(quick|lazy)");
    factory.setReplacement("very $1");
    factory.setOverwrite(true);
    Document filter = factory.newDocumentFilter(createDocument());

    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP4,
        valueList(CLEAN_STRING.replaceAll("quick", "very quick"),
                  EXTRA_STRING.replaceAll("lazy", "very lazy")));
    checkDocument(filter, expectedProps);
  }

  /** Test toString(). */
  public void testToString() {
    ModifyPropertyFilter factory = new ModifyPropertyFilter();
    factory.setPropertyName("foo");
    factory.setPattern(PATTERN);
    factory.setReplacement("bar");
    factory.setMimeType("text/plain");
    factory.setOverwrite(true);
    assertEquals("ModifyPropertyFilter: ([foo] , \"[_\\.]+\" , \"bar\" , "
        + "true , \"UTF-8\" , [text/plain])",
        factory.toString());
  }
}
