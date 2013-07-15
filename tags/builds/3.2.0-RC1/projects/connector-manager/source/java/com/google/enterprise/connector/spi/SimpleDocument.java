// Copyright 2007 Google Inc.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of the {@link Document} interface.
 * Implementors may use this directly or for reference.
 *
 * @since 1.0
 */
public class SimpleDocument implements Document {

  private final Map<String, List<Value>> properties;

  /**
   * Constructs a {@code SimpleDocument} whose metadata consists
   * of the supplied {@code Map} of properties, associating
   * property names with their {@link Value Values}.
   *
   * @param properties a {@code Map} of document metadata
   */
  public SimpleDocument(Map<String, List<Value>> properties) {
    this.properties = properties;
  }

  @Override
  public Property findProperty(String name) {
    List<Value> list = properties.get(name);
    Property prop = null;
    if (list != null) {
      prop = new SimpleProperty(list);
    }
    return prop;
  }

  @Override
  public Set<String> getPropertyNames() {
    return properties.keySet();
  }
}
