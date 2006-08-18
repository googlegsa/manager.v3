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

package com.google.enterprise.connector.mock;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock document property object.
 * This encapsulates a typed name-value pair: the name is a string, the value is
 * also implemented as a string (for now), but we remember the base type of the
 * property, drawn from an enclosed enum: STRING, DATE and INTEGER.
 * <p>
 * TODO(ziff): add a typed getter for the value
 */
public class MockRepositoryProperty {
  /**
   * Enumeration for property carrier types
   * @author ziff@google.com (Donald "Max" Ziff)
   */
  public enum PropertyType {
    STRING("string"), 
    DATE("date"), 
    INTEGER("integer"),
    UNDEFINED("undefined");

    private String tag;

    private PropertyType(String t) {
      tag = t;
    }

    public String toString() {
      return tag;
    }
    
    public static PropertyType findPropertyType(String tag) {
      if (tag == null) {
        return UNDEFINED;
      }
      for (PropertyType p: PropertyType.values()) {
        if (tag.equals(p.tag)) {
          return p;
        }
      }
      return UNDEFINED;
    }
  }
  
  private String name;
  private PropertyType type;
  private String value;
  private List<String> multivalues;
  private boolean repeating;

  public MockRepositoryProperty(String name, PropertyType type, String value) {
    init(name, type, value);
  }

  public MockRepositoryProperty(String name, Object o) {
    if (o == null) {
      init (name, PropertyType.STRING, "");
    } else if (o instanceof String) {
      String value = (String) o;
      init (name, PropertyType.STRING, value);
    } else if (o instanceof Integer) {
      Integer value = (Integer) o;
      init (name, PropertyType.INTEGER, value.toString());
    } else if (o instanceof JSONObject) {
      JSONObject jo = (JSONObject) o;
      init(name, jo);
    } else {
      throw new IllegalArgumentException(
        "Can't construct a MockRepositoryProperty from this: " + o);
    }
  }
  
  private void init(String name, PropertyType type, String value) {
    this.name = name;
    this.type = type;
    this.value = value;
    this.repeating = false;
    this.multivalues = null;
  }
  
  private void init(String name, JSONObject jo) {
    String s = jo.optString("type");
    PropertyType type = PropertyType.findPropertyType(s);
    if (type == PropertyType.UNDEFINED) {
      throw new IllegalArgumentException("Type must be specified");
    }
    Object v = jo.opt("value");
    if (v instanceof String) {
      String value = (String) v;
      init(name, type, value);
    } else if (v instanceof Integer) {
      Integer value = (Integer) v;
      init (name, PropertyType.INTEGER, value.toString());
    } else if (v instanceof JSONArray) {
      JSONArray ja = (JSONArray) v;
      this.name = name;
      this.type = type;
      this.repeating = true;
      this.multivalues = new ArrayList<String>(ja.length());
      for (int i=0; i<ja.length(); i++) {
        this.multivalues.add(ja.optString(i));
      }
    } else {
      throw new IllegalArgumentException(
        "Can't make a property from this json object");
    }
    
  }
  
  private String valuesToString() {
    if (!repeating) {
      return value;
    } else {
      StringBuffer buf = new StringBuffer(1024);
      buf.append('[');
      String separator = "";
      for (String v: multivalues) {
        buf.append(separator);
        buf.append(v);
        separator = ", ";
      }
      buf.append(']');
      return new String(buf);
    }
  }

  public String toString() {
    return name + "(" + type.toString() + "):" + 
      (repeating ? valuesToString() : value);
  }

  public String getName() {
    return name;
  }

  public PropertyType getType() {
    return type;
  }

  public String getValue() {
    if (!repeating) {
      return value;
    }
    if (multivalues == null || multivalues.size() < 1) {
      return "";
    }
    return multivalues.get(0);
  }

  private final static String[] EMPTY_STRING_ARRAY = new String[0];
  
  public String[] getValues() {
    if (!repeating) {
      return new String[] {value};
    } else {
      return multivalues.toArray(EMPTY_STRING_ARRAY);
    }
  }

  public boolean isRepeating() {
    return repeating;
  }

}
