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
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import java.util.List;
import java.util.Map;

/**
 * Tests SkipDocumentFilter.
 */
public class SkipDocumentFilterTest extends DocumentFilterTest {

  /** Creates a SkipDocumentFilter. */
  private Document createFilter(
      String propName, String pattern, boolean skipOnMatch)
      throws Exception {
    return createFilter(propName, pattern, skipOnMatch, createDocument());
  }

  /** Creates a SkipDocumentFilter. */
  private Document createFilter(
      String propName, String pattern, boolean skipOnMatch, Document source)
      throws Exception {
    SkipDocumentFilter factory = new SkipDocumentFilter();
    factory.setPropertyName(propName);
    factory.setPattern(pattern);
    factory.setSkipOnMatch(skipOnMatch);
    return factory.newDocumentFilter(source);
  }

  /** Creates a Properties subset with specified properties removed. */
  private Map<String, List<Value>> createPropertiesSubset(
      String... names) {
    Map<String, List<Value>> props = createProperties();
    for (String name : names) {
      props.remove(name);
    }
    return props;
  }

  /** Reads all values of a property. */
  private static void checkProperty(Property property)
      throws RepositoryException {
    assertNotNull(property);
    while(property.nextValue() != null);
  }

  /** Tests the Factory setters with illegal arguments. */
  public void testFactoryIllegalSetterArgs() throws Exception {
    SkipDocumentFilter factory = new SkipDocumentFilter();

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

    // Null or empty patterns are OK.
    factory.setPattern(null);
    factory.setPattern("");
  }

  /** Tests illegal state if configuration setters are not called. */
  public void testFactoryIllegalState() throws Exception {
    checkIllegalStateFindProperty(new SkipDocumentFilter());
  }

  /** Tests that non-existent property should skip if skipOnMatch is false. */
  public void testSkipNonExistentProperty() throws Exception {
    Document filter = createFilter("nonExistentProperty", null, false);
    try {
      filter.findProperty("nonExistentProperty");
      fail("SkippedDocumentException expected");
    } catch (SkippedDocumentException expected) {
      // Expected.
    }
    checkDocument(filter, createProperties(), "nonExistentProperty");
  }

  /** Tests that non-existent property should not skip if skipOnMatch is true */
  public void testNoSkipNonExistentProperty() throws Exception {
    Document filter = createFilter("nonExistentProperty", null, true);
    assertNull(filter.findProperty("nonExistentProperty"));
    checkDocument(filter, createProperties(), "nonExistentProperty");
  }

  /** Tests that existing property should skip if skipOnMatch is true. */
  public void testSkipExistingProperty() throws Exception {
    Document filter = createFilter(PROP3, null, true);
    try {
      filter.findProperty(PROP3);
      fail("SkippedDocumentException expected");
    } catch (SkippedDocumentException expected) {
      // Expected.
    }
    checkDocumentProperties(filter, createPropertiesSubset(PROP3));
  }

  /** Tests that existing property should not skip if skipOnMatch is false. */
  public void testNoSkipExistingProperty() throws Exception {
    Document filter = createFilter(PROP3, null, false);
    assertNotNull(filter.findProperty(PROP3));
    checkDocument(filter, createProperties());
  }

  /** Tests that the filter doesn't skip if no values match. */
  public void testNoSkipNoMatchingValues() throws Exception {
    Document filter = createFilter(PROP1, "nonExistentPattern", true);
    checkDocument(filter, createProperties());
  }

  /** Tests that the filter doesn't skip if another property's values match. */
  public void testNoSkipOtherMatchingValues() throws Exception {
    Document filter = createFilter(PROP4, PATTERN, true);
    checkDocument(filter, createProperties());
  }

  /** Tests that the filter does skip if values match. */
  public void testSkipOnMatchingSingleValue() throws Exception {
    Document filter = createFilter(PROP1, PATTERN, true);
    try {
      checkProperty(filter.findProperty(PROP1));
      fail("SkippedDocumentException expected");
    } catch (SkippedDocumentException expected) {
      // Expected.
    }
    checkDocumentProperties(filter, createPropertiesSubset(PROP1));
  }

  /** Tests that the filter does skip if values match. */
  public void testSkipOnMatchingMultipleValues() throws Exception {
    Document filter = createFilter(PROP5, PATTERN, true);
    try {
      checkProperty(filter.findProperty(PROP5));
      fail("SkippedDocumentException expected");
    } catch (SkippedDocumentException expected) {
      // Expected.
    }
    checkDocumentProperties(filter, createPropertiesSubset(PROP5));
  }

  /** Tests that the filter doesn't skip if values do match and skipOnMatch
      is false. */
  public void testNoSkipOnMatchingSingleValue() throws Exception {
    Document filter = createFilter(PROP1, PATTERN, false);
    checkDocument(filter, createProperties());
  }

  /** Tests that the filter doesn't skip if values do match and skipOnMatch
      is false. */
  public void testNoSkipOnMatchingMultipleValues() throws Exception {
    Document filter = createFilter(PROP6, PATTERN, false);
    checkDocument(filter, createProperties());
  }

  /** Test that the filter doesn't skip if a value is null. */
  public void testNoSkipOnNullValue() throws Exception {
    Map<String, List<Value>> props = createPropertiesSubset(PROP1);
    props.put(PROP1, valueList((String) null));
    Document filter =
        createFilter(PROP1, PATTERN, true, new SimpleDocument(props));
    checkDocument(filter, props);
  }

  /** Test that null values match an empty pattern. */
  public void testSkipOnNullValue() throws Exception {
    Map<String, List<Value>> props = createPropertiesSubset(PROP1);
    props.put(PROP1, valueList((String) null));
    Document filter =
        createFilter(PROP1, "\\A\\Z", true, new SimpleDocument(props));
    try {
      checkProperty(filter.findProperty(PROP1));
      fail("SkippedDocumentException expected");
    } catch (SkippedDocumentException expected) {
      // Expected.
    }
    checkDocumentProperties(filter, createPropertiesSubset(PROP1));
  }

  /** Test toString(). */
  public void testToString() {
    SkipDocumentFilter factory = new SkipDocumentFilter();
    factory.setPropertyName("foo");
    factory.setPattern("bar");
    factory.setSkipOnMatch(true);
    assertEquals("SkipDocumentFilter: (foo , \"bar\" , true)",
        factory.toString());
  }

  /**
   * Tests that an unpublished property with a null value is skipped if
   * skipOnMatch is true.
   */
  public void testSkipUnpublishedNullProperty() throws Exception {
    Document filter = createFilter("nonExistentProperty", null, true,
        createDocumentWithUnpublishedProperty());
    try {
      filter.findProperty("nonExistentProperty");
      fail("SkippedDocumentException expected");
    } catch (SkippedDocumentException expected) {
      // Expected.
    }
  }

  /**
   * Tests that an unpublished property with a null value is not skipped if
   * skipOnMatch is false.
   */
  public void testNoSkipUnpublishedNullProperty() throws Exception {
    Document filter = createFilter("nonExistentProperty", null, false,
        createDocumentWithUnpublishedProperty());
    assertNotNull(filter.findProperty("nonExistentProperty"));
  }

  /** Tests that an unpublished property is skipped if skipOnMatch is true. */
  public void testSkipUnpublishedProperty() throws Exception {
    Document filter = createFilter("nonExistentProperty", "dummy", true,
        createDocumentWithUnpublishedProperty());
    try {
      checkProperty(filter.findProperty("nonExistentProperty"));
      fail("SkippedDocumentException expected");
    } catch (SkippedDocumentException expected) {
      // Expected.
    }
  }

  /**
   * Tests that an unpublished property is not skipped if skipOnMatch is false.
   */
  public void testNoSkipUnpublishedProperty() throws Exception {
    Document filter = createFilter("nonExistentProperty", "dummy", false,
        createDocumentWithUnpublishedProperty());
    assertNotNull(filter.findProperty("nonExistentProperty"));
  }

  /**
   * Tests that an unpublished property is not return in
   * Document.getPropertyNames and is return in 
   * SkipDocumentFilter.getPropertyNames with a value of empty string.
   */
  public void testUnpublishedPropertyInFilterNotDocument() throws Exception {
    Document doc = createDocumentWithUnpublishedProperty();
    assertFalse(doc.getPropertyNames().toString(),
        doc.getPropertyNames().contains("nonExistentProperty"));
    Document filter = createFilter("nonExistentProperty", "nondummy", true,
        doc);
    assertTrue(filter.getPropertyNames().toString(),
        filter.getPropertyNames().contains("nonExistentProperty"));
    Property unpublished = filter.findProperty("nonExistentProperty");
    assertNotNull(unpublished);
    assertEquals("", unpublished.nextValue().toString());
    assertNull(unpublished.nextValue());
  }

  /**
   * Creates a {@link SimpleDocument} with the id of "dummy" and the 
   * non-public property "nonExistentProperty" that has the value "dummy".
   */
  private SimpleDocument createDocumentWithUnpublishedProperty() {
    Map<String, Object> props =
        ConnectorTestUtils.createSimpleDocumentBasicProperties("dummy");
    return new SimpleDocument(ConnectorTestUtils.createSpiProperties(props)) {
      @Override
      public Property findProperty(String name) {
        if (name.equals("nonExistentProperty")) {
          return new SimpleProperty(valueList("dummy"));
        } else {
          return super.findProperty(name);
        }
      }
    };
  }
}
