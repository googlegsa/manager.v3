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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * A kind of cookie set that keeps track of an old and a new value, and can compute the
 * differences between them.
 */
public class CookieDifferentiator {

  public enum Operation { ADD, REMOVE, MODIFY };

  private final CookieSet oldCookies;
  private final CookieSet newCookies;
  private final List<Delta> differential;

  public CookieDifferentiator() {
    oldCookies = new CookieSet();
    newCookies = new CookieSet();
    differential = new ArrayList<Delta>();
  }

  /**
   * Get the new cookie set.
   *
   * @return The new cookie set, which may be examined or modified as needed.
   */
  public CookieSet getNewCookies() {
    return newCookies;
  }

  /**
   * Compute the differences between the old and new sets.
   *
   * This compares the old cookie set to the new, computing and storing a set of
   * differences.  Then the old cookie set is modified to match the new one.
   */
  public void commitStep() {

    // Generate the deltas.
    CookieStream oldStream = new CookieStream(oldCookies);
    CookieStream newStream = new CookieStream(newCookies);
    differential.clear();

    while (oldStream.hasNext() && newStream.hasNext()) {
      ComparableCookie oldCookie = oldStream.next();
      ComparableCookie newCookie = newStream.next();
      int d = oldCookie.compareTo(newCookie);
      if (d < 0) {
        differential.add(new Delta(Operation.REMOVE, oldCookie.getCookie()));
        newStream.unread(newCookie);
      } else if (d > 0) {
        differential.add(new Delta(Operation.ADD, newCookie.getCookie()));
        oldStream.unread(oldCookie);
      } else if (!sameCookies(oldCookie.getCookie(), newCookie.getCookie())) {
        differential.add(new Delta(Operation.MODIFY, newCookie.getCookie()));
      }
    }

    // At most one of the next two loops will run its body.
    while (oldStream.hasNext()) {
      differential.add(new Delta(Operation.REMOVE, oldStream.next().getCookie()));
    }
    while (newStream.hasNext()) {
      differential.add(new Delta(Operation.ADD, newStream.next().getCookie()));
    }

    // Change the old set to match the new.
    oldCookies.clear();
    oldCookies.addAll(newCookies);
  }

  /**
   * Undo any changes made to the new cookies since the last differentiation.
   */
  public void abortStep() {
    newCookies.clear();
    newCookies.addAll(oldCookies);
  }

  /**
   * Get the previously computed differential.
   *
   * @return A list of deltas comprising the differential.
   */
  public List<Delta> getDifferential() {
    return ImmutableList.copyOf(differential);
  }

  /**
   * Determine whether cookies were added in the differential.
   *
   * @return True iff at least one cookie was added.
   */
  public boolean hasAddedCookies() {
    for (Delta delta : differential) {
      if (delta.getOperation() == Operation.ADD) {
        return true;
      }
    }
    return false;
  }

  // Compare ALL of the cookies' fields.
  private static boolean sameCookies(Cookie c1, Cookie c2) {
    return
        sameString(c1.getName(), c2.getName())
        && sameString(c1.getValue(), c2.getValue())
        && sameString(c1.getDomain(), c2.getDomain())
        && sameString(c1.getPath(), c2.getPath())
        && sameString(c1.getComment(), c2.getComment())
        && c1.getVersion() == c2.getVersion()
        && c1.getMaxAge() == c2.getMaxAge()
        && c1.getSecure() == c2.getSecure();
  }

  private static boolean sameString(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equals(s2);
  }

  // Like an iterator but with an unread() method.
  private static class CookieStream {
    private Iterator<ComparableCookie> i;
    private ComparableCookie e;

    CookieStream(CookieSet cookies) {
      i = cookies.cookies.iterator();
      e = null;
    }

    boolean hasNext() {
      return e != null || i.hasNext();
    }

    ComparableCookie next() {
      if (e != null) {
        ComparableCookie result = e;
        e = null;
        return result;
      }
      return i.hasNext() ? i.next() : null;
    }

    // Only valid immediately after next() has returned c.
    void unread(ComparableCookie e) {
      this.e = e;
    }
  }

  /**
   * An object that represents one difference between an old and a new cookie set.  A
   * sequence of these objects provides all the information needed to transform the old
   * set to the new one.  The operation specifies what change to make, while the cookie
   * specifies what the change is to be made on.
   */
  public static class Delta {

    private final Operation operation;
    private final Cookie cookie;

    Delta(Operation operation, Cookie cookie) {
      this.operation = operation;
      this.cookie = Cookie.class.cast(cookie.clone());
    }

    /**
     * Get the operation for this delta.
     *
     * @return The operation.
     */
    public Operation getOperation() {
      return operation;
    }

    /**
     * Get the cookie for this delta.
     *
     * @return The cookie.
     */
    public Cookie getCookie() {
      return cookie;
    }
  }
}
