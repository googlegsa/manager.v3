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

import com.google.enterprise.security.manager.SessionInterface;

import java.util.Iterator;

public class SessionList<T extends SessionObject> extends SessionObject implements Iterable<T> {

  private static final String N_ELEMENTS_KEY = "nElements";
  private static final String ELEMENTS_KEY = "elements";

  public SessionList(SessionInterface session) {
    super(session);
    setInt(N_ELEMENTS_KEY, 0);
  }

  public int size() {
    return getInt(N_ELEMENTS_KEY);
  }

  public void add(T element) {
    // We assume here that only the security manager writes, and the GSA
    // only reads.  So we need only worry about thread contention in the
    // security manager.
    int n;
    synchronized (session) {
      n = getInt(N_ELEMENTS_KEY);
      setObject(ELEMENTS_KEY + SEP + n, element);
      setInt(N_ELEMENTS_KEY, n + 1);
    }
  }

  public Iterator<T> iterator() {
    return new SessionListIterator();
  }

  class SessionListIterator implements Iterator<T> {

    private int index = 0;

    public boolean hasNext() {
      return index < size();
    }

    public T next() {
      int n;
      synchronized (this) {
        if (index >= size()) {
          return null;
        }
        n = index++;
      }
      return getObject(ELEMENTS_KEY + SEP + n);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
