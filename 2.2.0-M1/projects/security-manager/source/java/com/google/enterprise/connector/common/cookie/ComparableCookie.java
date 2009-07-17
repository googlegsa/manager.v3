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

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (o instanceof ComparableCookie) {
      return equalCookies(c, ((ComparableCookie) o).getCookie());
    }
    // TODO(?): Findbugs: This equals method is checking to see if the argument
    // is some incompatible type (i.e., a class that is neither a supertype nor
    // subtype of the class that defines the equals method).
    if (o instanceof Cookie) {
      return equalCookies(c, ((Cookie) o));
    }
    return false;
  }

  // RFC 2965 says name and domain are case insensitive.
  public static boolean equalCookies(Cookie c1, Cookie c2) {
    return
        equalStringsIgnoreCase(c1.getName(), c2.getName())
        && equalStringsIgnoreCase(c1.getDomain(), c2.getDomain())
        && equalStrings(c1.getPath(), c2.getPath());
  }

  private static boolean equalStrings(String s1, String s2) {
    return deNull(s1).equals(deNull(s2));
  }

  private static boolean equalStringsIgnoreCase(String s1, String s2) {
    return deNull(s1).equalsIgnoreCase(deNull(s2));
  }

  @Override
  public int hashCode() {
    return cookieHashCode(c);
  }

  public static int cookieHashCode(Cookie c) {
    int result = 17;
    result = (31 * result) + hashStringIgnoreCase(c.getName());
    result = (31 * result) + hashStringIgnoreCase(c.getDomain());
    result = (31 * result) + hashString(c.getPath());
    return result;
  }

  private static int hashString(String s) {
    return deNull(s).hashCode();
  }

  private static int hashStringIgnoreCase(String s) {
    return deNull(s).toLowerCase().hashCode();
  }

  /* @Override */
  public int compareTo(ComparableCookie cc) {
    return compareCookies(c, cc.getCookie());
  }

  public static int compareCookies(Cookie c1, Cookie c2) {
    int d = compareStringsIgnoreCase(c1.getName(), c2.getName());
    if (d != 0) { return d; }
    d = compareStringsIgnoreCase(c1.getDomain(), c2.getDomain());
    if (d != 0) { return d; }
    return compareStrings(c1.getPath(), c2.getPath());
  }

  private static int compareStrings(String s1, String s2) {
    return deNull(s1).compareTo(deNull(s2));
  }

  private static int compareStringsIgnoreCase(String s1, String s2) {
    return deNull(s1).compareToIgnoreCase(deNull(s2));
  }

  private static String deNull(String s) {
    return ((s == null) ? "" : s);
  }
}
