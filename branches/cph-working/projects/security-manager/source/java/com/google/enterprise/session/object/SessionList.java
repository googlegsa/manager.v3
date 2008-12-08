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

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class SessionList<T extends SessionObject> extends SessionObject implements Iterable<T> {

  private static final String N_ELEMENTS_KEY = "nElements";
  private static final String ELEMENTS_KEY = "elements";

  public SessionList(SessionRoot root) {
    super(root);
    setInt(N_ELEMENTS_KEY, 0);
  }

  public synchronized int size() {
    return getInt(N_ELEMENTS_KEY);
  }

  public synchronized T get(int index) {
    if (index < 0 || index >= size()) {
      throw new ArrayIndexOutOfBoundsException();
    }
    return getObject(ELEMENTS_KEY + SEP + index);
  }

  public synchronized void set(int index, T element) {
    if (index < 0 || index >= size()) {
      throw new ArrayIndexOutOfBoundsException();
    }
    setObject(ELEMENTS_KEY + SEP + index, element);
  }

  public synchronized void add(T element) {
    // We assume here that only the security manager writes, and the GSA
    // only reads.  So we need only worry about thread contention in the
    // security manager.
    int n = size();
    setObject(ELEMENTS_KEY + SEP + n, element);
    setInt(N_ELEMENTS_KEY, n + 1);
  }

  public ListIterator<T> iterator() {
    return new SessionListIterator();
  }

  class SessionListIterator implements ListIterator<T> {

    private int index = 0;
    private int prevIndex = -1;

    public boolean hasNext() {
      return index < size();
    }

    public boolean hasPrevious() {
      return index > 0;
    }

    public int nextIndex() {
      return index;
    }

    public int previousIndex() {
      return index - 1;
    }

    public synchronized T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      prevIndex = (index++);
      return getObject(ELEMENTS_KEY + SEP + prevIndex);
    }

    public synchronized T previous() {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      prevIndex = (--index);
      return getObject(ELEMENTS_KEY + SEP + prevIndex);
    }

    public void set(T element) {
      if (prevIndex < 0) {
        throw new IllegalStateException();
      }
      setObject(ELEMENTS_KEY + SEP + prevIndex, element);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public void add(T o) {
      throw new UnsupportedOperationException();
    }
  }
}
