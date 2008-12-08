// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.session.object;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionObject {

  protected static final String SEP = "/";
  private static final AtomicInteger objectIndex = new AtomicInteger(1);
  protected static final Pattern oidPattern =
      Pattern.compile("__OBJ__" + SEP + "([a-zA-Z0-9.]+)" + SEP + "[0-9]+");

  protected SessionRoot root;
  private String oid;

  protected SessionObject() {
  }

  protected SessionObject(SessionRoot root) {
    this.root = root;
    oid = makeOid(this.getClass(), objectIndex.getAndIncrement());
    root.cacheOidValue(oid, this);
  }

  protected static String makeOid(Class<? extends SessionObject> clazz, int n) {
    return "__OBJ__" + SEP + clazz.getName() + SEP + n;
  }

  protected void init(SessionRoot root, String oid) {
    this.root = root;
    this.oid = oid;
    root.cacheOidValue(oid, this);
  }

  public SessionRoot getRoot() {
    return root;
  }

  public String getOid() {
    return oid;
  }

  protected String getString(String key) {
    return root.getValue(oid + SEP + key);
  }

  protected void setString(String key, String value) {
    root.setValue(oid + SEP + key, value);
  }

  protected int getInt(String key) {
    String value = getString(key);
    return (value == null) ? 0 : new Integer(value);
  }

  protected void setInt(String key, int value) {
    setString(key, String.valueOf(value));
  }

  protected boolean getBoolean(String key) {
    String value = getString(key);
    return (value == null) ? false : value.equals("true");
  }

  protected void setBoolean(String key, boolean value) {
    setString(key, value ? "true" : "false");
  }

  protected <T extends Enum<T>> T getEnum(String key, Class<T> clazz) {
    String value = getString(key);
    if (value == null) {
      throw new IllegalArgumentException("uninitialized value: " + key);
    }
    return Enum.valueOf(clazz, value);
  }

  protected void setEnum(String key, Enum<?> item) {
    setString(key, item.toString());
  }

  protected <T extends SessionObject> T getObject(String key) {
    String oid = getString(key);
    if (oid == null) {
      return null;
    }
    return getOidValue(oid);
  }

  private synchronized <T extends SessionObject> T getOidValue(String oid) {
    T value = root.cachedOidValue(oid);
    if (value == null) {
      Class<T> clazz = oidToType(oid);
      try {
        value = clazz.newInstance();
      } catch (InstantiationException e) {
        throw new IllegalArgumentException(e);
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
      value.init(root, oid);
      root.cacheOidValue(oid, value);
    }
    return value;
  }

  protected static <T extends SessionObject> Class<T> oidToType(String oid) {
    Matcher m = oidPattern.matcher(oid);
    if (!m.matches()) {
      throw new IllegalArgumentException("ill-formed OID: " + oid);
    }
    String typeName = m.group(1);
    Class<?> clazz;
    try {
      clazz = Class.forName(typeName);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("unknown type name: " + typeName);
    }
    @SuppressWarnings("unchecked")
        Class<T> clazzT = (Class<T>) clazz;
    return clazzT;
  }

  protected void setObject(String key, SessionObject value) {
    setString(key, (value == null) ? null : value.getOid());
  }
}
