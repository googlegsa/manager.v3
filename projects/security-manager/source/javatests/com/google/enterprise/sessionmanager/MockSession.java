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

package com.google.enterprise.sessionmanager;

import com.google.common.base.Preconditions;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple Mock of the SessionManagerInterface.
 */
public class MockSession implements SessionInterface {

  private final Map<String, SessionValue> s;
  private String krb5Identity;
  private String krb5CcacheFilename;

  public MockSession() {
    s = new HashMap<String, SessionValue>();
    krb5Identity = null;
    krb5CcacheFilename = null;
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
    throw new UnsupportedOperationException();
  }

  public String getKrb5Identity() {
    return krb5Identity;
  }

  public String getKrb5CcacheFilename() {
    return krb5CcacheFilename;
  }

  static class SessionValue {
    private final String s;
    private final byte[] ba;

    private static Charset utf8 = Charset.forName("UTF-8");

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
      return s.getBytes(utf8);
    }

    String getValue() {
      if (s != null) {
        return s;
      }
      return new String(ba, utf8);
    }
  }
}
