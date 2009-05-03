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

package com.google.enterprise.connector.common;

import com.google.common.collect.PeekingIterator;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * A kind of cookie set that keeps track of an old and a new value, and can compute the
 * differences between them.
 */
public class CookieDifferentiator {

  public enum Operation { ADD, REMOVE, MODIFY };

  private CookieDifferentiator() {
  }

  /**
   * Compute the differences between two cookie sets.
   *
   * @return A list of deltas comprising the differential.
   */
  public static List<Delta> differentiate(CookieSet oldCookies, CookieSet newCookies) {

    // Generate the deltas.
    PeekingIterator<ComparableCookie> oldIter = oldCookies.comparableIterator();
    PeekingIterator<ComparableCookie> newIter = newCookies.comparableIterator();
    List<Delta> differential = new ArrayList<Delta>();

    while (oldIter.hasNext() && newIter.hasNext()) {
      ComparableCookie oldCookie = oldIter.peek();
      ComparableCookie newCookie = newIter.peek();
      int d = oldCookie.compareTo(newCookie);
      if (d < 0) {
        differential.add(new Delta(Operation.REMOVE, oldCookie.getCookie()));
        oldIter.next();
      } else if (d > 0) {
        differential.add(new Delta(Operation.ADD, newCookie.getCookie()));
        newIter.next();
      } else {
        if (!sameCookies(oldCookie.getCookie(), newCookie.getCookie())) {
          differential.add(new Delta(Operation.MODIFY, newCookie.getCookie()));
        }
        oldIter.next();
        newIter.next();
      }
    }

    // At most one of the next two loops will run its body.
    while (oldIter.hasNext()) {
      differential.add(new Delta(Operation.REMOVE, oldIter.next().getCookie()));
    }
    while (newIter.hasNext()) {
      differential.add(new Delta(Operation.ADD, newIter.next().getCookie()));
    }

    return differential;
  }

  // Compare ALL of the cookies' fields.
  // RFC 2965 says name and domain are case insensitive.
  private static boolean sameCookies(Cookie c1, Cookie c2) {
    return
        sameStringIgnoreCase(c1.getName(), c2.getName())
        && sameString(c1.getValue(), c2.getValue())
        && sameStringIgnoreCase(c1.getDomain(), c2.getDomain())
        && sameString(c1.getPath(), c2.getPath())
        && sameString(c1.getComment(), c2.getComment())
        && c1.getVersion() == c2.getVersion()
        && c1.getMaxAge() == c2.getMaxAge()
        && c1.getSecure() == c2.getSecure();
  }

  private static boolean sameString(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equals(s2);
  }

  private static boolean sameStringIgnoreCase(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equalsIgnoreCase(s2);
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
