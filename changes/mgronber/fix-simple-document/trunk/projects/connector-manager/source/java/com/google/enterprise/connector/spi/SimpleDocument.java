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

import java.util.Map;
import java.util.Set;

public class SimpleDocument implements Document {
  private Map properties;

  public SimpleDocument(Map properties) {
    this.properties = properties;
  }

  public Property findProperty(String name) throws RepositoryException {
    Property prop = (Property) properties.get(name);
    if (prop != null) {
      if (prop instanceof SimpleProperty) {
        prop = new SimpleProperty(((SimpleProperty) prop).getValues());
      } else {
        throw new RepositoryException("Unknown property type: " + prop);
      }
    }
    return prop;
  }

  public Set getPropertyNames() {
    return properties.keySet();
  }
}
