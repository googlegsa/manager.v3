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

import javax.servlet.http.Cookie;

/**
 * A cookie wrapper that is comparable.  The comparison involves only the name, domain,
 * and path.  This class is useful for building sorted sets of cookies.
 */
public class ComparableCookie implements Comparable<ComparableCookie> {
  private final Cookie c;

  /**
   * Get a ComparableCookie for a given cookie.
   *
   * @param c The given cookie.
   * @return A corresponding ComparableCookie.
   */
  public static ComparableCookie wrap(Cookie c) {
    if (c == null) {
      throw new NullPointerException();
    }
    return new ComparableCookie(c);
  }

  // Private to force use of static factory method.
  private ComparableCookie(Cookie c) {
    this.c = c;
  }

  /**
   * Get the wrapped cookie.
   *
   * @return A copy of the wrapped cookie.
   */
  public Cookie getCookie() {
    return c;
  }

  // RFC 2965 says name and domain are case insensitive.
  @Override public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    Cookie c2;
    if (o instanceof ComparableCookie) {
      c2 = ((ComparableCookie) o).getCookie();
    } else if (o instanceof ComparableCookie) {
      c2 = ((Cookie) o);
    } else {
      return false;
    }
    return
        equalStringsIgnoreCase(c.getName(), c2.getName())
        && equalStringsIgnoreCase(c.getDomain(), c2.getDomain())
        && equalStrings(c.getPath(), c2.getPath());
  }

  private static boolean equalStrings(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equals(s2);
  }

  private static boolean equalStringsIgnoreCase(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equalsIgnoreCase(s2);
  }

  @Override public int hashCode() {
    int result = 17;
    result = (31 * result) + ((c.getName() == null) ? 0 : c.getName().hashCode());
    result = (31 * result) + ((c.getDomain() == null) ? 0 : c.getDomain().hashCode());
    result = (31 * result) + ((c.getPath() == null) ? 0 : c.getPath().hashCode());
    return result;
  }

  /* @Override */ public int compareTo(ComparableCookie cc) {
    Cookie c2 = cc.getCookie();
    int d = compareStringsIgnoreCase(c.getName(), c2.getName());
    if (d != 0) { return d; }
    d = compareStringsIgnoreCase(c.getDomain(), c2.getDomain());
    if (d != 0) { return d; }
    return compareStrings(c.getPath(), c2.getPath());
  }

  private static int compareStrings(String s1, String s2) {
    return
        (s1 == null)
        ? ((s2 == null) ? 0 : -1)
        : ((s2 == null) ? 1 : s1.compareTo(s2));
  }

  private static int compareStringsIgnoreCase(String s1, String s2) {
    return
        (s1 == null)
        ? ((s2 == null) ? 0 : -1)
        : ((s2 == null) ? 1 : s1.compareToIgnoreCase(s2));
  }
}
