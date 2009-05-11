// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.connector.security.manager;

import com.google.common.base.Preconditions;
import com.google.enterprise.sessionmanager.KeyMaterial;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link SessionInterface} for use in the security manager
 */
public class LocalSession implements SessionInterface {

  private final Map<String, SessionValue> s;
  private String krb5Identity;

  public LocalSession() {
    s = new HashMap<String, SessionValue>();
    krb5Identity = null;
  }

  public boolean keyExists(String key) {
    Preconditions.checkNotNull(key);
    boolean result = s.containsKey(key);
    return result;
  }

  public void setValue(String key, String newValue) {
    Preconditions.checkNotNull(key);
    Preconditions.checkNotNull(newValue);
    SessionValue v = new SessionValue(newValue);
    s.put(key, v);
  }

  public String getValue(String key) {
    Preconditions.checkNotNull(key);
    SessionValue v = s.get(key);
    if (v == null) {
      return null;
    }
    return v.getValue();
  }

  public void setValueBin(String key, byte[] newValue) {
    Preconditions.checkNotNull(key);
    Preconditions.checkNotNull(newValue);
    SessionValue v = new SessionValue(newValue);
    s.put(key, v);
  }

  public byte[] getValueBin(String key) {
    Preconditions.checkNotNull(key);
    SessionValue v = s.get(key);
    if (v == null) {
      return null;
    }
    return v.getValueBin();
  }

  public String storeKrb5Identity(String spnegoBlob) {
    krb5Identity = spnegoBlob;
    return krb5Identity;
  }

  public KeyMaterial getKrb5TokenForServer(String server) {
    // todo: implement this
    throw new UnsupportedOperationException();
  }

  public String getKrb5Identity() {
    return krb5Identity;
  }

  public String getKrb5CcacheFilename() {
    return null;
  }

  /**
   * Container class for values that lets them be fetched either as Strings or
   * byte arrays.  Conversion is done using UTF-8.
   */
  static private class SessionValue {
    private final String s;
    private final byte[] ba;

    SessionValue(String s) {
      Preconditions.checkNotNull(s);
      this.s = s;
      this.ba = null;
    }

    SessionValue(byte[] ba) {
      Preconditions.checkNotNull(ba);
      this.s = null;
      this.ba = ba;
    }

    byte[] getValueBin() {
      if (ba != null) {
        return ba;
      }
      try {
        return s.getBytes("UTF8");
      } catch (UnsupportedEncodingException e) {
        throw new IllegalArgumentException(e);
      }
    }

    String getValue() {
      if (s != null) {
        return s;
      }
      try {
        return new String(ba, "UTF8");
      } catch (UnsupportedEncodingException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }
}
