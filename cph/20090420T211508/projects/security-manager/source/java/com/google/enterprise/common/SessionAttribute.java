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

/**
 * An implementation of a typed session attribute.  This allows an object to be associated
 * with a session ID, while managing to be type-safe in most instances.  The type
 * parameter T is the type of object that can be stored in the attribute.
 */
public class SessionAttribute<T> {
  /**
   * This structure has a wildcard type parameter because the objects it contains can have
   * any value for the type parameter.
   */
  private static final Map<String, SessionAttribute<?>> allAttributes =
      new HashMap<String, SessionAttribute<?>>();

  /**
   * Get a named session attribute.
   *
   * @param <E> The type of object to be stored in this attribute.
   * @param key The name of the attribute.
   * @return The attribute with this name, newly created if necessary.
   */
  public static <E> SessionAttribute<E> getNamed(String key) {
    @SuppressWarnings("unchecked")
    SessionAttribute<E> attr = (SessionAttribute<E>) allAttributes.get(key);
    if (attr == null) {
      attr = new SessionAttribute<E>();
      allAttributes.put(key, attr);
    }
    return attr;
  }

  /** The mapping from session ID to object for this attribute.  */
  private final Map<String, T> data;

  // Private to require use of getNamed() instead.
  private SessionAttribute() {
    data = new HashMap<String, T>();
  }

  /**
   * Get this attribute's value for a session.
   *
   * @param sessionId The ID of the session.
   * @return The attribute value.
   */
  public T get(String sessionId) {
    return data.get(sessionId);
  }

  /**
   * Set this attribute's value for a session.
   *
   * @param sessionId The ID of the session.
   * @param datum The new value for the given session.
   */
  public void put(String sessionId, T datum) {
    data.put(sessionId, datum);
  }

  /**
   * Remove this attribute's value for a session.
   *
   * @param sessionId The ID of the session.
   */
  public void remove(String sessionId) {
    data.remove(sessionId);
  }

  /**
   * Erase all values for a session.
   * To be used by external logic when a session expires.
   *
   * @param sessionId The ID of the session.
   */
  public static void eraseSession(String sessionId) {
    for (SessionAttribute<?> attr : allAttributes.values()) {
      attr.remove(sessionId);
    }
  }
}
