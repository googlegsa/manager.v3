// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.common;

import java.util.HashMap;
import java.util.Map;

public class SessionAttribute<T> {
  private static final Map<String, SessionAttribute<?>> allAttributes =
      new HashMap<String, SessionAttribute<?>>();

  public static <E> SessionAttribute<E> getNamed(String key) {
    @SuppressWarnings("unchecked")
    SessionAttribute<E> attr = (SessionAttribute<E>) allAttributes.get(key);
    if (attr == null) {
      attr = new SessionAttribute<E>();
      allAttributes.put(key, attr);
    }
    return attr;
  }

  private final Map<String, T> data;

  private SessionAttribute() {
    data = new HashMap<String, T>();
  }

  public static void eraseSession(String sessionId) {
    for (SessionAttribute<?> attr : allAttributes.values()) {
      attr.remove(sessionId);
    }
  }

  public T get(String sessionId) {
    return data.get(sessionId);
  }

  public void put(String sessionId, T datum) {
    data.put(sessionId, datum);
  }

  public void remove(String sessionId) {
    data.remove(sessionId);
  }
}
