// Copyright (C) 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.Value;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SimplePropertyTest extends TestCase {

  public void testSingleValue() throws Exception {
    Value expected = Value.getStringValue("test1");
    SimpleProperty property = new SimpleProperty(expected);

    // We should get our value back, but only once.
    Value value = property.nextValue();
    assertNotNull(value);
    assertEquals(expected, value);

    // Next fetch should yield null.
    value = property.nextValue();
    assertNull(value);

    // In fact, all subsequent fetches should yield null.
    for (int i = 0; i < 10; i++) {
      assertNull(property.nextValue());
    }
  }

  public void testMultiValues() throws Exception {
    LinkedList<Value> list = new LinkedList<Value>();
    for (int i = 0; i < 10; i++) {
      list.add(Value.getStringValue("test" + i));
    }
    // We should get all our values back, in order.
    checkMultiValues(list);
  }

  public void testImmutableMultiValues() throws Exception {
    LinkedList<Value> list = new LinkedList<Value>();
    for (int i = 0; i < 10; i++) {
      list.add(Value.getStringValue("test" + i));
    }
    // The property should not modify the list of values.
    checkMultiValues(Collections.unmodifiableList(list));
  }

  public void testHeterogeneousValues() throws Exception {
    LinkedList<Value> list = new LinkedList<Value>();
    list.add(Value.getStringValue("test1"));
    list.add(Value.getBinaryValue("test2".getBytes()));
    list.add(Value.getBooleanValue(true));
    list.add(Value.getBooleanValue("false"));
    list.add(Value.getDoubleValue(3.1415926535897932384626433832795));
    list.add(Value.getLongValue(1234567890L));
    list.add(Value.getDateValue(
             Value.iso8601ToCalendar("1970-01-01T00:00:00.999Z")));

    // Should work with different subclasses of Value.
    checkMultiValues(list);
  }

  private void checkMultiValues(List<Value> list) throws Exception {
    SimpleProperty property = new SimpleProperty(list);

    // We should get our values back, in order.
    for (Value expected : list) {
      Value value = property.nextValue();
      assertNotNull(value);
      assertEquals(expected, value);
    }

    // All subsequent fetches should yield null.
    for (int i = 0; i < 10; i++) {
      assertNull(property.nextValue());
    }
  }
}
