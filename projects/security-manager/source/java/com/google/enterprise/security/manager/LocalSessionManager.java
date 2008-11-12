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


import com.google.enterprise.sessionmanager.KeyMaterial;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link SessionManagerInterface} for use in the security
 * manager
 */
public class LocalSessionManager implements SessionManagerInterface {

  private AtomicInteger sessionCounter;
  private Map<String, SessionInterface> sessions;

  private static final String SESSION_PREFIX = "s";

  public LocalSessionManager() {
    sessionCounter = new AtomicInteger();
    sessions = new HashMap<String, SessionInterface>();
  }

  public String createSession() {
    int sessionNumber = sessionCounter.getAndIncrement();
    StringBuilder b = new StringBuilder();
    b.append(SESSION_PREFIX);
    b.append(Integer.toString(sessionNumber));
    String sessionId = new String(b);
    if (sessions.containsKey(sessionId)) {
      throw new IllegalStateException();
    }
    sessions.put(sessionId, new LocalSession());
    return sessionId;
  }

  public void deleteSession(String sessionId) throws IndexOutOfBoundsException {
    if (!sessions.containsKey(sessionId)) {
      throw new IndexOutOfBoundsException();
    }
    sessions.remove(sessionId);
  }

  public String getKrb5CcacheFilename(String sessionId) throws IndexOutOfBoundsException {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    return s.getKrb5CcacheFilename();
  }

  public String getKrb5Identity(String sessionId) throws IndexOutOfBoundsException {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    return s.getKrb5Identity();
  }

  public String getKrb5ServerNameIfEnabled() {
    // todo: implement this
    throw new UnsupportedOperationException();
  }

  public KeyMaterial getKrb5TokenForServer(String sessionId, String server)
      throws IndexOutOfBoundsException {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    throw new UnsupportedOperationException();
  }

  public String getValue(String sessionId, String key) throws IndexOutOfBoundsException {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    return s.getValue(key);
  }

  public byte[] getValueBin(String sessionId, String key) throws IndexOutOfBoundsException {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    return s.getValueBin(key);
  }

  public boolean keyExists(String sessionId, String key) {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    return s.keyExists(key);
  }

  public String parseKrb5Keytab(String filepath) {
    throw new UnsupportedOperationException();
  }

  public long sessionAge(String sessionId) {
    // todo: implement this
    throw new UnsupportedOperationException();
  }

  public boolean sessionExists(String sessionId) {
    return sessions.containsKey(sessionId);
  }

  public void setValue(String sessionId, String key, String newValue)
      throws IndexOutOfBoundsException {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    s.setValue(key, newValue);
  }

  public void setValueBin(String sessionId, String key, byte[] newValue)
      throws IndexOutOfBoundsException {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    s.setValueBin(key, newValue);
  }

  public String storeKrb5Identity(String sessionId, String spnegoBlob)
      throws IndexOutOfBoundsException {
    SessionInterface s = sessions.get(sessionId);
    if (s == null) {
      throw new IndexOutOfBoundsException();
    }
    return s.storeKrb5Identity(spnegoBlob);
  }

}
