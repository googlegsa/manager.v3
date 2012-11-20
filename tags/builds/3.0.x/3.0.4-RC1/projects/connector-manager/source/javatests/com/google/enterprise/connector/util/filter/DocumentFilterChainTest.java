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

import junit.framework.TestCase;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SimpleDocument;
import com.google.enterprise.connector.spi.Value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Tests DocumentFilterChain.
 */
public class DocumentFilterChainTest extends DocumentFilterTest {

  private List<DocumentFilterFactory> factoryList(DocumentFilterFactory... factories) {
    List<DocumentFilterFactory> list = new LinkedList<DocumentFilterFactory>();
    for (DocumentFilterFactory factory : factories) {
      list.add(factory);
    }
    return list;
  }

  private DocumentFilterFactory newFactory(String name, String pattern,
                                           String replacement) {
    ModifyPropertyFilter factory = new ModifyPropertyFilter();
    factory.setPropertyName(name);
    factory.setPattern(pattern);
    factory.setReplacement(replacement);
    return factory;
  }

  /** Test that an empty chain returns original document. */
  public void testEmptyChain() throws Exception {
    DocumentFilterChain chain = new DocumentFilterChain();
    Document original = createDocument();
    Document filter = chain.newDocumentFilter(original);
    assertSame(original, filter);
  }

  /** Test that multiple filters work in parallel. */
  public void testMultipleFiltersDifferentProperties() throws Exception {
    DocumentFilterChain chain = new DocumentFilterChain(factoryList(
        newFactory(PROP1, PATTERN, SPACE),
        newFactory(PROP3, PATTERN, SPACE)));
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP1, valueList(TEST_STRING, CLEAN_STRING));
    expectedProps.put(PROP3, valueList(TEST_STRING, CLEAN_STRING, EXTRA_STRING));
    checkDocument(chain.newDocumentFilter(createDocument()), expectedProps);
  }

  /** Test that multiple filters work in series. */
  public void testMultipleFiltersSameProperty() throws Exception {
    DocumentFilterChain chain = new DocumentFilterChain(factoryList(
        newFactory(PROP1, PATTERN, "XYZZY"),
        newFactory(PROP1, "XYZZY", SPACE)));
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP1, valueList(TEST_STRING,
                                       TEST_STRING.replaceAll(PATTERN, "XYZZY"),
                                       CLEAN_STRING));
    checkDocument(chain.newDocumentFilter(createDocument()), expectedProps);
  }

  /**
   * Test that multiple filters looking for non-matching things
   * pass the document through unchanged.
   */
  public void testMultipleFiltersSamePropertyNoMatch() throws Exception {
    DocumentFilterChain chain = new DocumentFilterChain(factoryList(
        newFactory(PROP1, "foobar", SPACE), newFactory(PROP3, "xyzzy", SPACE)));
    checkDocument(chain.newDocumentFilter(createDocument()), createProperties());
  }
}
