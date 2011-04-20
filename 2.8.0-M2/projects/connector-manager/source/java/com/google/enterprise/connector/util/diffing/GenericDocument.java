// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util.diffing;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.Value;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An generic implementation of com.google.enterprise.connector.spi.Document.
 *
 * @see com.google.enterprise.connector.spi.Document
 */
public class GenericDocument implements Document {
  private final Map<String, List<Value>> properties = new HashMap<String, List<Value>>();

  /**
   * Construct a document with no properties.
   */
  public GenericDocument() {
  }

  /**
   * Sets a property for this document. If propertyValue is null this does
   * nothing.
   *
   * @param propertyName
   * @param propertyValue
   */
  public void setProperty(String propertyName, String propertyValue) {
    if (propertyValue != null) {
      properties.put(propertyName, Collections.singletonList(Value.getStringValue(propertyValue)));
    }
  }

  /**
   * Sets a date/time property for this document. If {@code time} is null this
   * does nothing.
   *
   * @param propertyName
   * @param cal
   */
  public void setProperty(String propertyName, Calendar cal) {
    if (cal != null) {
      properties.put(propertyName, Collections.singletonList(Value.getDateValue(cal)));
    }
  }

  /**
   * Sets a property of type java.io.inputStream for this document. However, if
   * propertyValue is null, do nothing.
   *
   * @param propertyName
   * @param propertyValue
   */
  public void setProperty(String propertyName, InputStream propertyValue) {
    if (propertyValue != null) {
      properties.put(propertyName, Collections.singletonList(Value.getBinaryValue(propertyValue)));
    }
  }

  /**
   * Sets a repeating {@link String} property for this document. If the
   * the property value is null this does nothing.
   */
  public void setProperty(String propertyName, List<String> repeatingValue) {
    if (repeatingValue != null) {
      List<Value> values = new ArrayList<Value>(repeatingValue.size());
      for (String v : repeatingValue) {
        values.add(Value.getStringValue(v));
      }
      properties.put(propertyName, Collections.unmodifiableList(values));
    }
  }

  /* @Override */
  public Property findProperty(String name) {
    List<Value> property = properties.get(name);
    return (property == null) ? null : new SimpleProperty(property);
  }

  /*
   * Return the set of all property names.
   */
  /* @Override */
  public Set<String> getPropertyNames() {
    return Collections.unmodifiableSet(properties.keySet());
  }

  /**
   * Release any resources this document is holding. This implementation does
   * nothing.
   */
  public void release() {
  }
}
