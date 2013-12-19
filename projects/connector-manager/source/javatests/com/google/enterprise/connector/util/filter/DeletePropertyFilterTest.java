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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests DeletePropertyFilter.
 */
public class DeletePropertyFilterTest extends DocumentFilterTest {

  /** Creates a DeletePropertyFilter. */
  protected Document createFilter() throws Exception {
    HashSet<String> deletes = new HashSet<String>();
    deletes.add(PROP1);
    deletes.add(PROP3);
    return createFilter(deletes);
  }

  protected Document createFilter(Set<String>deletes)
    throws Exception {
    DeletePropertyFilter factory = new DeletePropertyFilter();
    factory.setPropertyNames(deletes);
    return factory.newDocumentFilter(createDocument());
  }

  /** Tests the Factory constructor with illegal arguments. */
  public void testFactoryIllegalArgs() throws Exception {
    DeletePropertyFilter factory = new DeletePropertyFilter();

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
  }

  /** Tests illegal state if configuration setters are not called. */
  public void testFactoryIllegalState() throws Exception {
    checkIllegalState(new DeletePropertyFilter());
  }

  /** Tests for non-existent property should return null. */
  public void testNonExistentProperty() throws Exception {
    HashSet<String>deletes = new HashSet<String>();
    deletes.add("foo");
    Document filter = createFilter(deletes);
    assertNull(filter.findProperty("foo"));
    assertNull(filter.findProperty("nonExistentProperty"));
  }

  /** Test deletes do not show up in the property names. */
  public void testDeletedNotInPropertyNames() throws Exception {
    Document filter = createFilter();
    Set<String> names = filter.getPropertyNames();
    assertFalse(names.contains(PROP1));
    assertFalse(names.contains(PROP3));

    // Make sure all the remaining properties are there.
    Set<String> origNames = new HashSet<String>(createProperties().keySet());
    origNames.remove(PROP1);
    origNames.remove(PROP3);
    assertTrue(names.containsAll(origNames));
  }

  /** Test the remaining property values should not be modified. */
  public void testNonDeletedProperties() throws Exception {
    Map<String, List<Value>> expectedProps = createProperties();
    expectedProps.remove(PROP1);
    expectedProps.remove(PROP3);
    checkDocument(createFilter(), expectedProps);
  }

  /** Test toString(). */
  public void testToString() {
    DeletePropertyFilter factory = new DeletePropertyFilter();
    factory.setPropertyName("foo");
    assertEquals("DeletePropertyFilter: [foo]", factory.toString());
  }
}
