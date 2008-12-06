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

package com.google.enterprise.security.manager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SessionObject {

  protected static final String SEP = "/";
  private static int nextIndex = 0;
  private static final Pattern oidPattern =
      Pattern.compile("__OBJ__" + SEP + "([a-zA-Z0-9.]+)" + SEP + "[0-9]+");

  protected SessionInterface session;
  private String oid;

  protected SessionObject() {
  }

  protected SessionObject(SessionInterface session) {
    this.session = session;
    int n;
    synchronized (SessionObject.class) {
      n = (nextIndex++);
    }
    oid = "__OBJ__" + SEP + this.getClass().getName() + SEP + n;
    session.cacheOidValue(oid, this);
  }

  protected void init(SessionInterface session, String oid) {
    this.session = session;
    this.oid = oid;
  }

  public String getOid() {
    return oid;
  }

  protected String getString(String key) {
    return session.getValue(oid + SEP + key);
  }

  protected void setString(String key, String value) {
    session.setValue(oid + SEP + key, value);
  }

  protected int getInt(String key) {
    return new Integer(getString(key));
  }

  protected void setInt(String key, int value) {
    setString(key, String.valueOf(value));
  }

  protected boolean getBoolean(String key) {
    return getString(key) == "true";
  }

  protected void setBoolean(String key, boolean value) {
    setString(key, value ? "true" : "false");
  }

  protected <T extends Enum<T>> T getEnum(String key, Class<T> clazz) {
    return Enum.valueOf(clazz, getString(key));
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

  protected <T extends SessionObject> T getOidValue(String oid) {
    T value = session.getCachedOidValue(oid);
    if (value == null) {
      value = oidToObject(oid);
      session.cacheOidValue(oid, value);
    }
    return value;
  }

  private <T extends SessionObject> T oidToObject(String oid) {
    Class<T> clazz = oidToType(oid);
    T object;
    try {
      object = clazz.newInstance();
    } catch (InstantiationException e) {
      throw new IllegalArgumentException(e);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
    object.init(session, oid);
    return object;
  }

  private static <T extends SessionObject> Class<T> oidToType(String oid) {
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
