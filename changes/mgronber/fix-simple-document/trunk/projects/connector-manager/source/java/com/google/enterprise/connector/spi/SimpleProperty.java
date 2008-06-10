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

import java.util.Iterator;
import java.util.List;

public class SimpleProperty implements Property {

  List values;
  Iterator iterator;

  public SimpleProperty(List values) {
    this.values = values;
    this.iterator = null;
  }

  public SimpleProperty(SimpleProperty object) {
    this.values = object.values;
    this.iterator = null;
  }

  public Value nextValue() {
    if (iterator == null) {
      iterator = values.iterator();
    }
    return (iterator.hasNext()) ? (Value) iterator.next() : null;
  }
}
