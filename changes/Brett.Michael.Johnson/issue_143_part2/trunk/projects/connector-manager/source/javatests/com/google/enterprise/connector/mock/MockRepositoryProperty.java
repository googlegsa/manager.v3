// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.common.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mock document property object. This encapsulates a typed name-value pair: the
 * name is a string, the value is also implemented as a string (for now), but we
 * remember the base type of the property, drawn from an enclosed enum: STRING,
 * DATE and INTEGER.
 * <p>
 * TODO(ziff): add a typed getter for the value
 */
public class MockRepositoryProperty {
  private static Logger LOGGER =
      Logger.getLogger(MockRepositoryProperty.class.getName());

  // Constants used within raw ACL properties to delimit sections and label
  // scope.
  public static final String USER_SCOPE = "user";
  public static final String GROUP_SCOPE = "group";
  public static final int SCOPE_TYPE_SEP = ':';
  public static final int SCOPE_ROLE_SEP = '=';

  /**
   * Enumeration for property carrier types
   */
  public static class PropertyType implements Comparable<PropertyType> {
    private static int nextOrdinal = 0;
    private final int ordinal = nextOrdinal++;

    public static final PropertyType STRING = new PropertyType("string");
    public static final PropertyType DATE = new PropertyType("date");
    public static final PropertyType INTEGER = new PropertyType("integer");
    public static final PropertyType STREAM = new PropertyType("stream");
    public static final PropertyType UNDEFINED = new PropertyType("undefined");

    private static final PropertyType[] PRIVATE_VALUES =
        {STRING, DATE, INTEGER, STREAM, UNDEFINED};
    public static final List<PropertyType> Values =
        Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));


    private final String tag;

    private PropertyType(String t) {
      tag = t;
    }

    @Override
    public String toString() {
      return tag;
    }

    public static PropertyType findPropertyType(String tag) {
      if (tag == null) {
        return UNDEFINED;
      }
      for (int i = 0; i < PRIVATE_VALUES.length; i++) {
        if (PRIVATE_VALUES[i].tag.equals(tag)) {
          return PRIVATE_VALUES[i];
        }
      }
      return UNDEFINED;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ordinal;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      PropertyType other = (PropertyType) obj;
      if (ordinal != other.ordinal) {
        return false;
      }
      return true;
    }

    public int compareTo(PropertyType propertyType) {
      return ordinal - propertyType.ordinal;
    }
  }

  private String name;
  private PropertyType type;
  private String value;
  private InputStream streamValue;  // used for streams
  private boolean streamRead;  // indicates that stream has been used
  private List<String> multivalues;
  private boolean repeating;

  public MockRepositoryProperty(String name, PropertyType type, String value) {
    init(name, type, value);
  }

  public MockRepositoryProperty(String name, Object o) {
    if (o == null) {
      init(name, PropertyType.STRING, "");
    } else if (o instanceof String) {
      String value = (String) o;
      if (value.startsWith("{") && value.endsWith("}")) {
        // this could be a json object - try to parse it as such
        JSONObject jo = null;
        try {
          jo = new JSONObject(value);
          init(name, jo);
          return;
        } catch (IllegalArgumentException e) {
          // it was a json object, but not the right kind to initialize a
          // property
          LOGGER.log(Level.FINEST, "Unable to initialize JSON String", e);
        } catch (JSONException e) {
          // it wasn't anything like a json object. it must be a regular string
          LOGGER.log(Level.FINEST, "Unable to parse as JSON String", e);
        }
      }
      init(name, PropertyType.STRING, value);
    } else if (o instanceof Integer) {
      Integer value = (Integer) o;
      init(name, PropertyType.INTEGER, value.toString());
    } else if (o instanceof JSONObject) {
      JSONObject jo = (JSONObject) o;
      init(name, jo);
    } else if (o instanceof InputStream) {
      InputStream is = (InputStream) o;
      init(name, is);
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
      init(name, PropertyType.INTEGER, value.toString());
    } else if (v instanceof JSONArray) {
      JSONArray ja = (JSONArray) v;
      this.name = name;
      this.type = type;
      this.repeating = true;
      this.multivalues = new ArrayList<String>(ja.length());
      for (int i = 0; i < ja.length(); i++) {
        this.multivalues.add(ja.optString(i));
      }
    } else {
      throw new IllegalArgumentException(
          "Can't make a property from this json object");
    }
  }

  private void init(String name, InputStream is) {
    this.name = name;
    this.type = PropertyType.STREAM;
    this.streamValue = is;
    this.value = null;
    this.repeating = false;
    this.multivalues = null;
  }

  private String valuesToString() {
    if (!repeating) {
      return value;
    } else {
      return multivalues.toString();
    }
  }

  @Override
  public String toString() {
    return name + "(" + type.toString() + "):"
        + (repeating ? valuesToString() : value);
  }

  public String getName() {
    return name;
  }

  public PropertyType getType() {
    return type;
  }

  public String getValue() {
    if (type.equals(PropertyType.STREAM)) {
      if (null == value) {
        value = StringUtils.streamToString(streamValue);
        streamRead = true;
      }
      return value;
    }
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
      return new String[] {getValue()};
    } else {
      return multivalues.toArray(EMPTY_STRING_ARRAY);
    }
  }

  /**
   * Return the underlying InputStream.  This method should only be called if
   * the type is PropertyType.STREAM.
   *
   * @return the underlying InputStream if object is of type STREAM
   */
  public InputStream getStreamValue() {
    if (!type.equals(PropertyType.STREAM)) {
      throw new IllegalStateException("Can only call getStreamValue() on " +
            "properties of type PropertyType.STREAM");
    }

    if (streamRead) {
      if (null != value) {
        return new ByteArrayInputStream(value.getBytes());
      } else {
        throw new IllegalStateException("Cannot call getStreamValue() twice");
      }
    }
    return streamValue;
  }

  public boolean isRepeating() {
    return repeating;
  }

}
