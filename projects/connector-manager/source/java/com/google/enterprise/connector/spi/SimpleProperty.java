// Copyright 2006 Google Inc.
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Simple implementation of the {@link Property} interface.
 * Implementors may use this directly or for reference.
 */
public class SimpleProperty implements Property {

  final Iterator<Value> iterator;

  /**
   * Constructs a property with a single value.
   *
   * @param value the property's {@link Value}
   * @since 2.4
   */
  public SimpleProperty(Value value) {
    this(Collections.singletonList(value));
  }

  /**
   * Constructs a property with multiple values.
   *
   * @param values a {@code List} of the property's {@link Value Values}
   */
  public SimpleProperty(List<Value> values) {
    this.iterator = values.iterator();
  }

  /* @Override */
  public Value nextValue() {
    return (iterator.hasNext()) ? iterator.next() : null;
  }
}
