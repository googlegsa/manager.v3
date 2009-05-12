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

package com.google.enterprise.connector.common.cookie;

import com.google.common.collect.PeekingIterator;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.Cookie;

/**
 * A set of cookies, distinguished only by their name, domain, and path; cookies that
 * differ in those attributes can coexist in the set.  A cookie in the set may be
 * modified, but such modifications don't affect the set; to change a cookie in place it
 * must be removed and then the modified copy added back.
 */
public class CookieSet extends AbstractSet<Cookie> {

  private SortedSet<ComparableCookie> cookies;

  public CookieSet() {
    this.cookies = new TreeSet<ComparableCookie>();
  }

  @Override public boolean add(Cookie c) {
    return cookies.add(wrap(c));
  }

  @Override public boolean addAll(Collection<? extends Cookie> cs) {
    return cookies.addAll(wrapSet(cs));
  }

  @Override public void clear() {
    cookies.clear();
  }

  @Override public boolean contains(Object o) {
    return cookies.contains(wrap(o));
  }

  @Override public boolean containsAll(Collection<?> os) {
    return cookies.containsAll(wrapSet(os));
  }

  @Override public boolean isEmpty() {
    return cookies.isEmpty();
  }

  @Override public Iterator<Cookie> iterator() {
    return new CookieIterator(cookies.iterator());
  }

  @Override public boolean remove(Object o) {
    return cookies.remove(wrap(o));
  }

  @Override public boolean removeAll(Collection<?> os) {
    return cookies.removeAll(wrapSet(os));
  }

  @Override public boolean retainAll(Collection<?> os) {
    return cookies.retainAll(wrapSet(os));
  }

  @Override public int size() {
    return cookies.size();
  }

  /**
   * Get an iterator that supports lookahead.
   *
   * @return A peeking iterator.
   */
  PeekingIterator<Cookie> peekingIterator() {
    return new CookieIterator(cookies.iterator());
  }

  private ComparableCookie wrap(Object o) {
    if (o == null) {
      throw new NullPointerException();
    }
    return ComparableCookie.wrap(Cookie.class.cast(o));
  }

  private Set<ComparableCookie> wrapSet(Collection<?> os) {
    Set<ComparableCookie> cs = new TreeSet<ComparableCookie>();
    for (Object o : os) {
      cs.add(wrap(o));
    }
    return cs;
  }

  private static class CookieIterator implements PeekingIterator<Cookie> {

    private final Iterator<ComparableCookie> iter;
    private Cookie peeked;

    CookieIterator(Iterator<ComparableCookie> iter) {
      this.iter = iter;
      peeked = null;
    }

    public boolean hasNext() {
      return peeked != null || iter.hasNext();
    }

    public Cookie next() {
      if (peeked == null) {
        return iter.next().getCookie();
      }
      Cookie c = peeked;
      peeked = null;
      return c;
    }

    public Cookie peek() {
      if (peeked == null) {
        peeked = iter.next().getCookie();
      }
      return peeked;
    }

    public void remove() {
      iter.remove();
    }
  }
}
