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

import java.util.HashMap;
import java.util.Map;

/**
 * Mock Session manager that does only 1 level of key-value mapping
 * (i.e. it doesn't map by session id).
 *
 * All methods in this mock session manager ignore the session id field, so
 * all calls to this mock effectively use the same session id.
 */
public class MockSessionManager implements SessionManagerInterface {

  private final Map<String, String> stringMap;
  private final Map<String, byte[]> byteMap;

  public MockSessionManager() {
    stringMap = new HashMap<String, String>();
    byteMap = new HashMap<String, byte[]>();
  }

  public boolean sessionExists(String sessionId) {
    return true;
  }

  public boolean keyExists(String sessionId, String key) {
    return stringMap.containsKey(key);
  }

  public long sessionAge(String sessionId) {
    return 0;
  }

  public String createSession() {
    return null;
  }

  public void setValue(String sessionId, String key, String newValue)
      throws IndexOutOfBoundsException {
    stringMap.put(key, newValue);
  }

  public String getValue(String sessionId, String key)
      throws IndexOutOfBoundsException {
    if (stringMap.containsKey(key)) {
      return stringMap.get(key);
    }
    return null;
  }

  public void setValueBin(String sessionId, String key, byte[] newValue)
      throws IndexOutOfBoundsException {
    byteMap.put(key, newValue);
  }

  public byte[] getValueBin(String sessionId, String key)
      throws IndexOutOfBoundsException {
    if (byteMap.containsKey(key)) {
      return byteMap.get(key);
    }
    return null;
  }

  public void deleteSession(String sessionId) throws IndexOutOfBoundsException {
    return;
  }

  public String storeKrb5Identity(String sessionId, String spnegoBlob)
      throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException();
  }

  public KeyMaterial getKrb5TokenForServer(String sessionId, String server)
      throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException();
  }

  public String getKrb5Identity(String sessionId)
      throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException();
  }

  public String getKrb5CcacheFilename(String sessionId)
      throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException();
  }

  public String parseKrb5Keytab(String filepath) {
    throw new UnsupportedOperationException();
  }

  public String getKrb5ServerNameIfEnabled() {
    throw new UnsupportedOperationException();
  }
}
