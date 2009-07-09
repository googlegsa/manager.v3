// Copyright 2007-2009 Google Inc.
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

import java.util.Iterator;
import java.util.List;

public class SimpleProperty implements Property {

  Value value;              // For single value.
  Iterator<Value> iterator; // For multiple values.

  public SimpleProperty(Value value) {
    this.value = value;
  }

  public SimpleProperty(List<Value> values) {
    this.iterator = values.iterator();
  }

  public Value nextValue() {
    if (value != null) {
      Value retval = value;
      value = null;
      return retval;
    } else if (iterator != null && iterator.hasNext()) {
      return iterator.next();
    }
    return null;
  }
}
