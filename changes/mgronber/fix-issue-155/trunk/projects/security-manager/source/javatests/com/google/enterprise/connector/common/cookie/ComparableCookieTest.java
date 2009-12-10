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

import com.google.enterprise.connector.common.SecurityManagerTestCase;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * Tests for the {@link ComparableCookie} class.
 *
 * The strategy here is to generate a lot of different cookies with a mix of components,
 * and to compare every cookie with every other cookie.  Rather than generate very large
 * lists of cookie pairs, where the list length is the square of the number of individual
 * cookies, we build a framework that iterates over all the cookie pairs and invokes a
 * Runner#run method on each pair.
 *
 * There are two types of Runner classes: the first type does a simple comparison of the
 * cookies, which we implement as a singleton instance.  This type of Runner is used to
 * test whether the name/domain/path members of a cookie are properly compared to one
 * another.
 *
 * The second type does additional iteration by building variants of the argument cookies
 * and comparing those variants to one another.  This type is used to test that members
 * other than name/domain/path do not affect the comparisons.  To support this
 * generically, we use Setter singletons, one for each of the members to be set.
 */
public class ComparableCookieTest extends SecurityManagerTestCase {

  private static final String[] NAMES = { "", "cookie1", "COOKIE1", "cookie2" };
  private static final String[] DOMAINS = { "", ".google.com", ".GOOGLE.COM", ".not-google.com" };
  private static final String[] PATHS = { null, "", "/", "/foo/", "/FOO/", "/bar/" };
  private static final String[] VALUES = { null, "", "v1" };
  private static final String[] COMMENTS = { null, "", "comment" };
  private static final Integer[] VERSIONS = { -1, 0, 1 };
  private static final Integer[] MAXAGES = { -1, 0, 1 };
  private static final Boolean[] SECURES = {  false, true  };

  // Runner singletons, one for each method being tested.
  private final Runner equalsTestRunner;
  private final Runner hashCodeTestRunner;
  private final Runner compareToTestRunner;

  // Setter singletons, one for each cookie member being modified.
  private final Setter<String> valueSetter;
  private final Setter<String> commentSetter;
  private final Setter<Integer> versionSetter;
  private final Setter<Integer> maxAgeSetter;
  private final Setter<Boolean> secureSetter;

  public ComparableCookieTest() {
    equalsTestRunner =
        new Runner() {
          @Override
          public void run(ComparableCookie cc1, ComparableCookie cc2) {
            equalsTest(cc1, cc2);
          }
        };
    hashCodeTestRunner =
        new Runner() {
          @Override
          public void run(ComparableCookie cc1, ComparableCookie cc2) {
            hashCodeTest(cc1, cc2);
          }
        };
    compareToTestRunner =
        new Runner() {
          @Override
          public void run(ComparableCookie cc1, ComparableCookie cc2) {
            compareToTest(cc1, cc2);
          }
        };
    valueSetter =
        new Setter<String>() {
          @Override
          public void set(Cookie c, String value) {
            c.setValue(value);
          }
        };
    commentSetter =
        new Setter<String>() {
          @Override
          public void set(Cookie c, String value) {
            c.setComment(value);
          }
        };
    versionSetter =
        new Setter<Integer>() {
          @Override
          public void set(Cookie c, Integer value) {
            c.setVersion(value);
          }
        };
    maxAgeSetter =
        new Setter<Integer>() {
          @Override
          public void set(Cookie c, Integer value) {
            c.setMaxAge(value);
          }
        };
    secureSetter =
        new Setter<Boolean>() {
          @Override
          public void set(Cookie c, Boolean value) {
            c.setSecure(value);
          }
        };
  }

  // Test the equals() method.
  public void testEquals() {
    runTests(equalsTestRunner);
  }

  // Test the hashCode() method.
  public void testHashCode() {
    runTests(hashCodeTestRunner);
  }

  // Test the compareTo() method.
  public void testCompareTo() {
    runTests(compareToTestRunner);
  }

  /**
   * Run all tests for a single method.
   *
   * @param runner A runner for a single test on that method.
   */
  private void runTests(Runner runner) {
    runOverPairs(runner);
    runValueInvarianceTest(runner, valueSetter, VALUES);
    runValueInvarianceTest(runner, commentSetter, COMMENTS);
    runValueInvarianceTest(runner, versionSetter, VERSIONS);
    runValueInvarianceTest(runner, maxAgeSetter, MAXAGES);
    runValueInvarianceTest(runner, secureSetter, SECURES);
  }

  /**
   * Iterate over all name/domain/path pairs.
   *
   * @param runner A runner to invoke on each cookie pair.
   */
  private void runOverPairs(Runner runner) {
    for (String name1 : NAMES) {
      for (String name2 : NAMES) {
        for (String domain1 : DOMAINS) {
          for (String domain2 : DOMAINS) {
            for (String path1 : PATHS) {
              for (String path2 : PATHS) {
                runner.run(makeComparable(name1, domain1, path1),
                           makeComparable(name2, domain2, path2));
              }
            }
          }
        }
      }
    }
  }

  /**
   * Iterate over all name/domain/path/M pairs, for a given member M.
   *
   * @param <T> The type of member M.
   * @param runner A runner to invoke on each cookie pair.
   * @param setter A setter that modifies member M.
   * @param values An array of values suitable for member M.
   */
  private <T> void runValueInvarianceTest(Runner runner, Setter<T> setter, T[] values) {
    runOverPairs(new ValueInvarianceRunner<T>(runner, setter, values));
  }

  /**
   * A nested Runner class that compares variants of the argument cookies.  The variants
   * differ from the arguments in that a single cookie member is modified, which is
   * supposed to have the same test result as a comparison of the arguments.
   */
  private class ValueInvarianceRunner<T> extends Runner {

    /** The basic test Runner to be called. */
    private final Runner runner;

    /** A Setter for the cookie member being modified. */
    private final Setter<T> setter;

    /** An array of values that will be tried for the cookie member. */
    private final T[] values;

    public ValueInvarianceRunner(Runner runner, Setter<T> setter, T[] values) {
      this.runner = runner;
      this.setter = setter;
      this.values = values;
    }

    @Override
    public void run(ComparableCookie cc1, ComparableCookie cc2) {
      for (T valueA : values) {
        // Modify the left cookie only.
        {
          ComparableCookie cc1a = copyComparable(cc1);
          setter.set(cc1a.getCookie(), valueA);
          runner.run(cc1a, cc2);
        }
        // Modify the right cookie only.
        {
          ComparableCookie cc2a = copyComparable(cc2);
          setter.set(cc2a.getCookie(), valueA);
          runner.run(cc1, cc2a);
        }
        // Modify both cookies with the same value.
        {
          ComparableCookie cc1a = copyComparable(cc1);
          ComparableCookie cc2a = copyComparable(cc2);
          setter.set(cc1a.getCookie(), valueA);
          setter.set(cc2a.getCookie(), valueA);
          runner.run(cc1a, cc2a);
        }
        // Modify each cookie with a different value.
        {
          ComparableCookie cc1a = copyComparable(cc1);
          setter.set(cc1a.getCookie(), valueA);
          for (T valueB : values) {
            ComparableCookie cc2a = copyComparable(cc2);
            setter.set(cc2a.getCookie(), valueB);
            runner.run(cc1a, cc2a);
          }
        }
      }
    }
  }

  private abstract class Runner {
    public abstract void run(ComparableCookie cc1, ComparableCookie cc2);
  }

  private abstract class Setter<T> {
    public abstract void set(Cookie c, T value);
  }

  // Test the equals() method.
  private static void equalsTest(ComparableCookie cc1, ComparableCookie cc2) {
    if (equalCookies(cc1.getCookie(), cc2.getCookie())) {
      assertTrue(assertionMessage(cc1, cc2), cc1.equals(cc2));
    } else {
      assertFalse(assertionMessage(cc1, cc2), cc1.equals(cc2));
    }
  }

  // Test the hashCode() method.
  private static void hashCodeTest(ComparableCookie cc1, ComparableCookie cc2) {
    if (equalCookies(cc1.getCookie(), cc2.getCookie())) {
      assertEquals(assertionMessage(cc1, cc2), cc1.hashCode(), cc2.hashCode());
    }
  }

  // Test the compareTo() method.
  private static void compareToTest(ComparableCookie cc1, ComparableCookie cc2) {
    int d1 = compareCookies(cc1.getCookie(), cc2.getCookie());
    int d2 = cc1.compareTo(cc2);
    if (d1 < 0) {
      assertTrue(assertionMessage(cc1, cc2), d2 < 0);
    } else if (d1 > 0) {
      assertTrue(assertionMessage(cc1, cc2), d2 > 0);
    } else {
      assertTrue(assertionMessage(cc1, cc2), d2 == 0);
    }
  }

  private static String assertionMessage(ComparableCookie cc1, ComparableCookie cc2) {
    List<Cookie> cs = new ArrayList<Cookie>();
    cs.add(cc1.getCookie());
    cs.add(cc2.getCookie());
    return CookieUtil.setCookieHeaderValue(cs, true);
  }

  private static ComparableCookie makeComparable(String name, String domain, String path) {
    Cookie c = new Cookie(name, null);
    c.setDomain(domain);
    c.setPath(path);
    return ComparableCookie.wrap(c);
  }

  private static ComparableCookie copyComparable(ComparableCookie cc) {
    Cookie c = cc.getCookie();
    return makeComparable(c.getName(), c.getDomain(), c.getPath());
  }

  private static boolean equalCookies(Cookie c1, Cookie c2) {
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

  private static int compareCookies(Cookie c1, Cookie c2) {
    int d = compareStringsIgnoreCase(c1.getName(), c2.getName());
    if (d != 0) return d;
    d = compareStringsIgnoreCase(c1.getDomain(), c2.getDomain());
    if (d != 0) return d;
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
