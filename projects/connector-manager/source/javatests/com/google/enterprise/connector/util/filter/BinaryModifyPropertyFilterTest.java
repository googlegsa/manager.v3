// Copyright 2012 Google Inc.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests the {@code ModifyPropertyFilter} class on binary values.
 *
 * @author kiransama@google.com (Kiran Sama)
 */
public class BinaryModifyPropertyFilterTest extends ModifyPropertyFilterTest{
  
  protected static final String MIMETYPE = "text/plain";
  protected static final String NOMATCH_MIMETYPE = "text/xml";
  protected static final String ENCODING = "UTF-8";

  /** Creates a ModifyPropertyFilter. */
  @Override
  protected Document createFilter(
      Set<String> propNames, String pattern, boolean overwrite)
      throws Exception {
    ModifyPropertyFilter factory = createBasicFilter(propNames, 
        pattern, overwrite);
    return factory.newDocumentFilter(createDocument());
  }
  
  /** Creates a ModifyPropertyFilter. */
  protected Document createNoMatchMimeTypeFilter(
      Set<String> propNames, String pattern, boolean overwrite)
      throws Exception {
    ModifyPropertyFilter factory = createBasicFilter(propNames, 
        pattern, overwrite);
    factory.setMimeType(NOMATCH_MIMETYPE);
    return factory.newDocumentFilter(createDocument());
  }
  
  /** Creates a filter to test Docs with No Mimetype. */
  protected Document createFilterForNoMimeTypeDocs(
      Set<String> propNames, String pattern, boolean overwrite)
      throws Exception {
    ModifyPropertyFilter factory = createBasicFilter(propNames, 
        pattern, overwrite);
    return factory.newDocumentFilter(createDocument(false));
  }
  
  /** Separated out because of commonality in different tests. */
  protected ModifyPropertyFilter createBasicFilter(
      Set<String> propNames, String pattern, boolean overwrite){
    ModifyPropertyFilter factory = new ModifyPropertyFilter();
    factory.setEncoding(ENCODING);
    factory.setMimeType(MIMETYPE);
    factory.setPropertyNames(propNames);
    factory.setPattern(pattern);
    factory.setReplacement(SPACE);
    factory.setOverwrite(overwrite);
    return factory;
  }

  /** Returns the BinaryValue object list generated from the list of strings. */
  @Override
  protected List<Value> valueList(String... values) {
    LinkedList<Value> list = new LinkedList<Value>();
    for (String value : values) {
      if (value == null) {
        list.add(Value.getBinaryValue(new byte[0]));
      } else {
        list.add(Value.getBinaryValue(value.getBytes()));
      }
    }
    return list;
  }
  
  /** Tests that default filter changes the value in the target property. */
  public void testDefaultFilterMatchingFirstValueInMultiValueProperty() 
      throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP3, valueList(TEST_STRING, CLEAN_STRING, EXTRA_STRING));
    Document filter = super.createFilter(PROP3, PATTERN, false);
    checkDocument(filter, expectedProps);
  }
  
  /** Tests that default filter changes the values in the target property. */
  public void testDefaultFilterMatchingValuesInMultiValueProperty() 
      throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.put(PROP6, valueList(TEST_STRING, CLEAN_STRING, 
        TEST_EXTRA_STRING, EXTRA_STRING));
    Document filter = super.createFilter(PROP6, PATTERN, false);
    checkDocument(filter, expectedProps);
  }
  
  /** Test that filter doesn't modify if mimetype check is failed. */
  public void testNoMatchMimeTypeFilter() throws Exception {
    Document filter = createNoMatchMimeTypeFilter(
        Collections.singleton(PROP1), PATTERN, true);
    checkDocument(filter, createProperties());
  }
  
  /** Test that filter doesn't modify if document has no mimetype paarameter */
  public void testNoMimeTypeDocuments() throws Exception {
    Document filter = createFilterForNoMimeTypeDocs(
        Collections.singleton(PROP1), PATTERN, true);
    checkDocument(filter, createProperties(false));
  }
}
