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

public class SessionMap<K extends SessionObject, V extends SessionObject>
    extends SessionObject {

  private static final String ENTRIES_KEY = "entries";

  public SessionMap(SessionInterface session) {
    super(session);
    setObject(ENTRIES_KEY, new SessionList<SessionPair<K, V>>(session));
  }

  private SessionList<SessionPair<K, V>> getEntries() {
    return getObject(ENTRIES_KEY);
  }

  public boolean hasValue(K key) {
    for (SessionPair<K, V> entry: getEntries()) {
      if (entry.getFirst() == key) {
        return true;
      }
    }
    return false;
  }

  public V get(K key) {
    for (SessionPair<K, V> entry: getEntries()) {
      if (entry.getFirst() == key) {
        return entry.getSecond();
      }
    }
    return null;
  }

  public V getRequired(K key) {
    for (SessionPair<K, V> entry: getEntries()) {
      if (entry.getFirst() == key) {
        return entry.getSecond();
      }
    }
    throw new IllegalArgumentException("unknown key: " + key);
  }

  public void put(K key, V value) {
    SessionList<SessionPair<K, V>> entries = getEntries();
    for (SessionPair<K, V> entry: entries) {
      if (entry.getFirst() == key) {
        entry.setSecond(value);
        return;
      }
    }
    entries.add(new SessionPair<K, V>(session, key, value));
  }
}
