// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Simple convenience implementation of the spi.Property interface. This class
 * is not part of the spi - it is provided for developers to assist in
 * implementations of the spi.
 */
public class SimpleProperty implements Property {

  private final String name;
  private final boolean repeating;
  private final Value value;
  private final List valueList;

  public SimpleProperty(String name, Value value) {
    this.name = name;
    this.repeating = false;
    this.value = value;
    this.valueList = null;
  }

  public SimpleProperty(String name, List valueList) {
    this.name = name;
    this.repeating = true;
    this.value = null;
    this.valueList = valueList;
  }

  public SimpleProperty(String name, String value) {
    this.name = name;
    this.value = new SimpleValue(ValueType.STRING, value);
    this.repeating = false;
    this.valueList = null;
  }

  public SimpleProperty(String name, boolean value) {
    this.name = name;
    this.value = new SimpleValue(ValueType.BOOLEAN, value ? "true" : "false");
    this.repeating = false;
    this.valueList = null;
  }

  public SimpleProperty(String name, long value) {
    this.name = name;
    this.value = new SimpleValue(ValueType.LONG, Long.toString(value));
    this.repeating = false;
    this.valueList = null;
  }

  public SimpleProperty(String name, double value) {
    this.name = name;
    this.value = new SimpleValue(ValueType.STRING, Double.toString(value));
    this.repeating = false;
    this.valueList = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Property#getName()
   */
  public String getName() throws RepositoryException {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Property#getValue()
   */
  public Value getValue() throws RepositoryException {
    if (repeating) {
      if (valueList == null) {
        return null;
      }
      return (Value) valueList.get(0);
    }
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.Property#getValues()
   */
  public Iterator getValues() throws RepositoryException {
    if (repeating) {
        if (valueList == null) {
            return Arrays.asList(new Value[]{}).iterator();
          }
      return valueList.iterator();
    }
    return Arrays.asList(new Value[]{value}).iterator();
  }

}
