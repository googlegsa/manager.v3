// Copyright 2008 Google Inc.
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

package com.google.enterprise.connector.jcr;

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;

/**
 * Simple Property class that can created from event data.
 */
public class JcrEventProperty implements Property {
  private String name;
  private Value value;
  boolean sentIt = false;
  // TODO(mgronber): Add iterator

  public JcrEventProperty(String name, Value value) {
    this.name = name;
    this.value = value;
  }

  public String getPropertyName() {
    return name;
  }

  public Value nextValue() throws RepositoryException {
    if (sentIt) {
      sentIt = false;
      return null;
    } else {
      sentIt = true;
      return value;
    }
  }
}
