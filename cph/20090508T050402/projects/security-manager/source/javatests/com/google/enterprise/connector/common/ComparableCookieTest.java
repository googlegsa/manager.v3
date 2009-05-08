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

import com.google.enterprise.common.SecurityManagerTestCase;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * Tests for the {@link ComparableCookie} class.
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

  private final Runner runTestEquals;
  private final Runner runTestHashCode;
  private final Runner runTestCompareTo;
  private final Setter<String> valueSetter;
  private final Setter<String> commentSetter;
  private final Setter<Integer> versionSetter;
  private final Setter<Integer> maxAgeSetter;
  private final Setter<Boolean> secureSetter;

  public ComparableCookieTest() {
    runTestEquals =
        new Runner() {
          @Override
          public void run(TestElement element) {
            element.testEquals();
          }
        };
    runTestHashCode =
        new Runner() {
          @Override
          public void run(TestElement element) {
            element.testHashCode();
          }
        };
    runTestCompareTo =
        new Runner() {
          @Override
          public void run(TestElement element) {
            element.testCompareTo();
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

  private abstract class Runner {
    public abstract void run(TestElement element);
  }

  private abstract class Setter<T> {
    public abstract void set(Cookie c, T value);
  }

  public void testEquals() {
    runOverPairs(runTestEquals);
    runValueInvarianceTests(runTestEquals);
  }

  public void testHashCode() {
    runOverPairs(runTestHashCode);
    runValueInvarianceTests(runTestHashCode);
  }

  public void testCompareTo() {
    runOverPairs(runTestCompareTo);
    runValueInvarianceTests(runTestCompareTo);
  }

  private void runValueInvarianceTests(Runner runner) {
    runOverModifiedPairs(runner, valueSetter, VALUES);
    runOverModifiedPairs(runner, commentSetter, COMMENTS);
    runOverModifiedPairs(runner, versionSetter, VERSIONS);
    runOverModifiedPairs(runner, maxAgeSetter, MAXAGES);
    runOverModifiedPairs(runner, secureSetter, SECURES);
  }

  private void runOverPairs(Runner runner) {
    for (String name1 : NAMES) {
      for (String name2 : NAMES) {
        for (String domain1 : DOMAINS) {
          for (String domain2 : DOMAINS) {
            for (String path1 : PATHS) {
              for (String path2 : PATHS) {
                runner.run(new TestElement(makeComparable(name1, domain1, path1),
                                           makeComparable(name2, domain2, path2)));
              }
            }
          }
        }
      }
    }
  }

  private static ComparableCookie makeComparable(String name, String domain, String path) {
    Cookie c = new Cookie(name, null);
    c.setDomain(domain);
    c.setPath(path);
    return ComparableCookie.wrap(c);
  }

  private <T> void runOverModifiedPairs(Runner runner, Setter<T> setter, T[] values) {
    runOverPairs(new ModifiedPairRunner<T>(runner, setter, values));
  }

  private class ModifiedPairRunner<T> extends Runner {

    private final Runner runner;
    private final Setter<T> setter;
    private final T[] values;

    public ModifiedPairRunner(Runner runner, Setter<T> setter, T[] values) {
      this.runner = runner;
      this.setter = setter;
      this.values = values;
    }

    @Override
    public void run(TestElement element) {
      for (T value : values) {
        {
          TestElement element2 = element.copy();
          setter.set(element2.getCookie1().getCookie(), value);
          runner.run(element2);
        }
        {
          TestElement element2 = element.copy();
          setter.set(element2.getCookie2().getCookie(), value);
          runner.run(element2);
        }
        {
          TestElement element2 = element.copy();
          setter.set(element2.getCookie1().getCookie(), value);
          setter.set(element2.getCookie2().getCookie(), value);
          runner.run(element2);
        }
        for (T value2 : values) {
          TestElement element2 = element.copy();
          setter.set(element2.getCookie1().getCookie(), value);
          setter.set(element2.getCookie2().getCookie(), value2);
          runner.run(element2);
        }
      }
    }
  }

  private class TestElement {
    private final ComparableCookie cc1;
    private final ComparableCookie cc2;
    private final String message;

    public TestElement(ComparableCookie cc1, ComparableCookie cc2) {
      this.cc1 = cc1;
      this.cc2 = cc2;
      List<Cookie> cs = new ArrayList<Cookie>();
      cs.add(cc1.getCookie());
      cs.add(cc2.getCookie());
      message = CookieUtil.setCookieHeaderValue(cs, true);
    }

    public ComparableCookie getCookie1() {
      return cc1;
    }

    public ComparableCookie getCookie2() {
      return cc2;
    }

    public TestElement copy() {
      return new TestElement(cc1, cc2);
    }

    public void testEquals() {
      if (equalCookies(cc1.getCookie(), cc2.getCookie())) {
        assertTrue(message, cc1.equals(cc2));
      } else {
        assertFalse(message, cc1.equals(cc2));
      }
    }

    public void testHashCode() {
      if (equalCookies(cc1.getCookie(), cc2.getCookie())) {
        assertEquals(message, cc1.hashCode(), cc2.hashCode());
      }
    }

    public void testCompareTo() {
      int d1 = compareCookies(cc1.getCookie(), cc2.getCookie());
      int d2 = cc1.compareTo(cc2);
      if (d1 < 0) {
        assertTrue(message, d2 < 0);
      } else if (d1 > 0) {
        assertTrue(message, d2 > 0);
      } else {
        assertTrue(message, d2 == 0);
      }
    }
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
