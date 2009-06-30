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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleDocument implements Document {

  private final Map<String, List<Value>> properties;

  public SimpleDocument(Map<String, List<Value>> properties) {
    this.properties = properties;
  }

  public Property findProperty(String name) {
    List<Value> list = properties.get(name);
    Property prop = null;
    if (list != null) {
      prop = new SimpleProperty(list);
    }
    return prop;
  }

  public Set<String> getPropertyNames() {
    return properties.keySet();
  }

  /**
   * Creates a {@link SimpleDocument} with the provided id and a
   * minimal set of additional properties.
   */
  public static SimpleDocument createSimpleDocument(String docId) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(10 * 1000);

    Map<String, Object> props = new HashMap<String, Object>();
    props.put(SpiConstants.PROPNAME_DOCID, docId);
    props.put(SpiConstants.PROPNAME_LASTMODIFIED, cal);
    props.put(SpiConstants.PROPNAME_DISPLAYURL, "http://myserver/docid="
        + docId);
    props.put(SpiConstants.PROPNAME_CONTENT, "Hello World!");
    return createSimpleDocument(props);
  }

  /**
   * Creates a {@link SimpleDocument} with the properties in the provided
   * {@link Map}.
   */
  public static SimpleDocument createSimpleDocument(Map<String,
      Object> props) {
    Map<String, List<Value>> spiValues = new HashMap<String, List<Value>>();
    for (Map.Entry<String, Object> entry : props.entrySet()) {
      Object obj = entry.getValue();
      Value val = null;
      if (obj instanceof String) {
        val = Value.getStringValue((String) obj);
      } else if (obj instanceof Calendar) {
        val = Value.getDateValue((Calendar) obj);
      } else {
        throw new AssertionError(obj);
      }
      List<Value> values = new ArrayList<Value>();
      values.add(val);
      spiValues.put(entry.getKey(), values);
    }
    return new SimpleDocument(spiValues);
  }

}
