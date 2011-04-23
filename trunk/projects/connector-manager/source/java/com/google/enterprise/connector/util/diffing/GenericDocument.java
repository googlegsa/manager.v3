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
 * An generic implementation of {@link Document}.
 *
 * @since 2.8
 */
public class GenericDocument implements Document {
  private final Map<String, List<Value>> properties =
      new HashMap<String, List<Value>>();

  /**
   * Constructs a document with no properties.
   */
  public GenericDocument() {
  }

  /**
   * Sets a property for this Document. If {@code propertyValue} is
   * {@code null}, this does nothing.
   *
   * @param propertyName the property name
   * @param propertyValue the property value, as a String
   */
  public void setProperty(String propertyName, String propertyValue) {
    if (propertyValue != null) {
      properties.put(propertyName,
          Collections.singletonList(Value.getStringValue(propertyValue)));
    }
  }

  /**
   * Sets a date/time property for this document. If {@code calendar} is
   * {@code null}, this does nothing.
   *
   * @param propertyName the property name
   * @param calendar the property value, as a {@code Calendar}
   */
  public void setProperty(String propertyName, Calendar calendar) {
    if (calendar != null) {
      properties.put(propertyName,
          Collections.singletonList(Value.getDateValue(calendar)));
    }
  }

  /**
   * Sets a property of type {@code InputStream} for this Document.
   * However, if propertyValue is {@code null}, do nothing.
   *
   * @param propertyName the property name
   * @param propertyValue the property value, as an {@code InputStream}
   */
  public void setProperty(String propertyName, InputStream propertyValue) {
    if (propertyValue != null) {
      properties.put(propertyName,
          Collections.singletonList(Value.getBinaryValue(propertyValue)));
    }
  }

  /**
   * Sets a multi-valued {@link String} property for this Document. If the
   * the property value List is {@code null} or empty, this does nothing.

   * @param propertyName the property name
   * @param propertyValues a List of property values
   */
  public void setProperty(String propertyName, List<String> propertyValues) {
    if (propertyValues != null && propertyValues.size() > 0) {
      List<Value> values = new ArrayList<Value>(propertyValues.size());
      for (String val : propertyValues) {
        values.add(Value.getStringValue(val));
      }
      properties.put(propertyName, Collections.unmodifiableList(values));
    }
  }

  /* @Override */
  public Property findProperty(String name) {
    List<Value> property = properties.get(name);
    return (property == null) ? null : new SimpleProperty(property);
  }

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
