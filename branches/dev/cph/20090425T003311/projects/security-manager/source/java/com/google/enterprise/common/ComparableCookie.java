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
    this.c = Cookie.class.cast(c.clone());
  }

  /**
   * Get the wrapped cookie.
   *
   * @return A copy of the wrapped cookie.
   */
  public Cookie getCookie() {
    return Cookie.class.cast(c.clone());
  }

  /**
   * Get the name of the cookie.
   *
   * @return The cookie's name.
   */
  public String getName() {
    return c.getName();
  }

  /**
   * Get the domain of the cookie.
   *
   * @return The cookie's domain.
   */
  public String getDomain() {
    return c.getDomain();
  }

  /**
   * Get the path of the cookie.
   *
   * @return The cookie's path.
   */
  public String getPath() {
    return c.getPath();
  }

  /**
   * Get the version of the cookie.
   *
   * @return The cookie's version.
   */
  public int getVersion() {
    return c.getVersion();
  }

  /**
   * Update the cookie's value fields.
   *
   * @param c2 A cookie containing the new value fields.
   */
  public void update(Cookie c2) {
    c.setValue(c2.getValue());
    c.setMaxAge(c2.getMaxAge());
    c.setSecure(c2.getSecure());
    c.setComment(c2.getComment());
  }

  @Override public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null || !(o instanceof ComparableCookie)) return false;
    Cookie c2 = ((ComparableCookie) o).getCookie();
    return
        equalStrings(c.getName(), c2.getName())
        && equalStrings(c.getDomain(), c2.getDomain())
        && equalStrings(c.getPath(), c2.getPath())
        && c.getVersion() == c2.getVersion();
  }

  private static boolean equalStrings(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equals(s2);
  }

  @Override public int hashCode() {
    int result = 17;
    result = (31 * result) + ((c.getName() == null) ? 0 : c.getName().hashCode());
    result = (31 * result) + ((c.getDomain() == null) ? 0 : c.getDomain().hashCode());
    result = (31 * result) + ((c.getPath() == null) ? 0 : c.getPath().hashCode());
    result = (31 * result) + c.getVersion();
    return result;
  }

  /* @Override */ public int compareTo(ComparableCookie cc) {
    Cookie c2 = cc.getCookie();
    int d = compareStrings(c.getName(), c2.getName());
    if (d != 0) { return d; }
    d = compareStrings(c.getDomain(), c2.getDomain());
    if (d != 0) { return d; }
    d = compareStrings(c.getPath(), c2.getPath());
    if (d != 0) { return d; }
    return Integer.valueOf(c.getVersion()).compareTo(c2.getVersion());
  }

  private static int compareStrings(String s1, String s2) {
    return
        (s1 == null)
        ? ((s2 == null) ? 0 : -1)
        : ((s2 == null) ? 1 : s1.compareTo(s2));
  }
}
