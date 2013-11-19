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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spiimpl.BinaryValue;

import junit.framework.TestCase;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mostly useful for other DocumentFilter tests to subclass.
 */
public class DocumentFilterTest extends TestCase {

  protected static final String PROP1 = "property1";
  protected static final String PROP2 = "property2";
  protected static final String PROP3 = "property3";
  protected static final String PROP4 = "property4";
  protected static final String PROP5 = "property5";
  protected static final String PROP6 = "property6";
  protected static final String PROP7 = "property7";
  protected static final String TEST_STRING = "The_.quick_brown_fox.jumped.over";
  protected static final String CLEAN_STRING = "The quick brown fox jumped over";
  protected static final String TEST_EXTRA_STRING = "lazy.dog's_back";
  protected static final String EXTRA_STRING = "lazy dog's back";
  protected static final String PATTERN = "[_\\.]+";
  protected static final String SPACE = " ";
  protected static final String MIMEVALUE = "text/plain;utf-8";

  /** Creates a source Document. */
  protected Document createDocument() {
    return new SimpleDocument(createProperties());
  }

  protected Document createDocument(boolean includeMimetype){
    return new SimpleDocument(createProperties(includeMimetype));
  }

  /** Default createProperties which adds mimetype to the doc. */
  protected Map<String, List<Value>> createProperties() {
    return createProperties(true);
  }

  /** Creates the filter without adding mimetype property. */
  protected Map<String, List<Value>> createProperties(boolean includeMimetype) {
    Map<String, List<Value>> props = new HashMap<String, List<Value>>();
    if (includeMimetype) {
      // Avoid valueList, which is overridden for binary values in subclasses.
      LinkedList<Value> list = new LinkedList<Value>();
      list.add(Value.getStringValue(MIMEVALUE));
      props.put(SpiConstants.PROPNAME_MIMETYPE, list);
    }
    props.put(PROP1, valueList(TEST_STRING));
    props.put(PROP2, valueList(CLEAN_STRING));
    props.put(PROP3, valueList(TEST_STRING, EXTRA_STRING));
    props.put(PROP4, valueList(CLEAN_STRING, EXTRA_STRING));
    props.put(PROP5, valueList(CLEAN_STRING, TEST_EXTRA_STRING));
    props.put(PROP6, valueList(TEST_STRING, TEST_EXTRA_STRING));
    props.put(PROP7, valueList(EXTRA_STRING));
    return props;
  }

  protected List<Value> valueList(String... values) {
    LinkedList<Value> list = new LinkedList<Value>();
    for (String value : values) {
      list.add(Value.getStringValue(value));
    }
    return list;
  }

  /** Checks that the Document Properties match the expected Properties. */
  protected void checkDocument(Document document,
      Map<String, List<Value>> expectedProps) throws Exception {
    checkDocument(document, expectedProps, null);
  }

  /**
   * Checks that the Document Properties match the expected Properties.
   *
   * @param document the document to check
   * @param expectedProps the expected properties of the document
   * @param skipProp a property who's value should not be checked
   *    but is still returned in getPropertyNames
   */
  protected void checkDocument(Document document,
      Map<String, List<Value>> expectedProps, String skipProp)
      throws Exception {
    Set<String> expectedSet;
    if (Strings.isNullOrEmpty(skipProp)) {
      expectedSet = expectedProps.keySet();
    } else {
      expectedSet = Sets.union(expectedProps.keySet(),
          ImmutableSet.of(skipProp));
    }
    assertEquals(expectedSet, document.getPropertyNames());
    checkDocumentProperties(document, expectedProps);
  }

  /**
   * Checks that the Document Properties match the expected Properties,
   * which may be a subset of the full document properties.
   */
  protected void checkDocumentProperties(Document document,
      Map<String, List<Value>> expectedProps) throws Exception {
    for (Map.Entry<String, List<Value>> entry : expectedProps.entrySet()) {
      Property prop = document.findProperty(entry.getKey());
      assertNotNull(prop);
      for (Value expectedValue : entry.getValue()) {
        Value value = prop.nextValue();
        assertNotNull(value);
        if (value instanceof BinaryValue) {
          assertEquals(getStringFromBinaryValue(expectedValue),
              getStringFromBinaryValue(value));
        } else {
          assertEquals(expectedValue.toString(), value.toString());
        }
      }
      assertNull(prop.nextValue());
    }
  }

  protected static String getStringFromBinaryValue(Value value)
      throws Exception {
    InputStream in = ((BinaryValue) value).getInputStream();
    return new String(ByteStreams.toByteArray(in));
  }

  /** Check IllegalStateException if the Filter was not properly initialized. */
  protected void checkIllegalState(DocumentFilterFactory factory)
      throws Exception {
    Document filter = factory.newDocumentFilter(createDocument());

    try {
      filter.getPropertyNames();
      fail("IllegalStateException expected");
    } catch (IllegalStateException expected) {
      // Expected.
    }

    try {
      filter.findProperty(PROP1);
      fail("IllegalStateException expected");
    } catch (IllegalStateException expected) {
      // Expected.
    }
  }

  /** Check IllegalStateException if the Filter was not properly initialized. */
  protected void checkIllegalStateFindProperty(
      DocumentFilterFactory factory) throws Exception {
    Document filter = factory.newDocumentFilter(createDocument());
    try {
      filter.findProperty(PROP1);
      fail("IllegalStateException expected");
    } catch (IllegalStateException expected) {
      // Expected.
    }
  }

  /** Test createDocument. */
  public void testCreateDocument() throws Exception {
    checkDocument(createDocument(), createProperties());
  }

  /** If no methods are overridden, the AbstractDocumentFilter is just
   * a NO-OP filter, passing all calls through to the source Document.
   */
  public void testNoopFilter() throws Exception {
    NoopDocumentFilter filter = new NoopDocumentFilter();
    checkDocument(filter.newDocumentFilter(createDocument()),
                  createProperties());
  }

  /** Test null source document. */
  public void testNullSourceDocument() throws Exception {
    NoopDocumentFilter filter = new NoopDocumentFilter();
    try {
      filter.newDocumentFilter(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {
      // Expected.
    }
  }

  /** Test toString().  Test Subclasses should override this */
  public void testToString() {
    NoopDocumentFilter filter = new NoopDocumentFilter();
    assertEquals("DocumentFilterTest$NoopDocumentFilter", filter.toString());
  }

  /** AbstractDocumentFilter is defined as abstract, so I must subclass it. */
  class NoopDocumentFilter extends AbstractDocumentFilter {
  }
}
