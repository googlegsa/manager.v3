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

import java.lang.reflect.Method;
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

  private final Method setValueMethod;
  private final Method setCommentMethod;
  private final Method setVersionMethod;
  private final Method setMaxAgeMethod;
  private final Method setSecureMethod;

  public ComparableCookieTest() throws Exception {
    runTestEquals = new EqualsRunner();
    runTestHashCode = new HashCodeRunner();

    setValueMethod = Cookie.class.getMethod("setValue", String.class);
    setCommentMethod = Cookie.class.getMethod("setComment", String.class);
    setVersionMethod = Cookie.class.getMethod("setVersion", Integer.TYPE);
    setMaxAgeMethod = Cookie.class.getMethod("setMaxAge", Integer.TYPE);
    setSecureMethod = Cookie.class.getMethod("setSecure", Boolean.TYPE);
  }

  public void testEquals() throws Exception {
    runOverPairs(runTestEquals);
    runValueInvarianceTests(runTestEquals);
  }

  public void testHashCode() throws Exception {
    runOverPairs(runTestHashCode);
    runValueInvarianceTests(runTestHashCode);
  }

  private void runValueInvarianceTests(Runner runner) throws Exception {
    runOverModifiedPairs(runner, setValueMethod, VALUES);
    runOverModifiedPairs(runner, setCommentMethod, COMMENTS);
    runOverModifiedPairs(runner, setVersionMethod, VERSIONS);
    runOverModifiedPairs(runner, setMaxAgeMethod, MAXAGES);
    runOverModifiedPairs(runner, setSecureMethod, SECURES);
  }

  private interface Runner {
    public void run(TestElement element) throws Exception;
  }

  private static class EqualsRunner implements Runner {
    public void run(TestElement element) throws Exception {
      element.testEquals();
    }
  }

  private static class HashCodeRunner implements Runner {
    public void run(TestElement element) throws Exception {
      element.testHashCode();
    }
  }

  private static void runOverPairs(Runner runner) throws Exception {
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

  private static <T> void runOverModifiedPairs(Runner runner, Method setter, T[] values)
      throws Exception {
    runOverPairs(new ModifiedPairRunner<T>(runner, setter, values));
  }

  private static class ModifiedPairRunner<T> implements Runner {

    private final Runner runner;
    private final Method setter;
    private final T[] values;

    public ModifiedPairRunner(Runner runner, Method setter, T[] values)
      throws Exception {
      this.runner = runner;
      this.setter = setter;
      this.values = values;
    }

    public void run(TestElement element) throws Exception {
      for (T value : values) {
        {
          TestElement element2 = element.copy();
          setter.invoke(element2.getCookie1().getCookie(), value);
          runner.run(element2);
        }
        {
          TestElement element2 = element.copy();
          setter.invoke(element2.getCookie2().getCookie(), value);
          runner.run(element2);
        }
        {
          TestElement element2 = element.copy();
          setter.invoke(element2.getCookie1().getCookie(), value);
          setter.invoke(element2.getCookie2().getCookie(), value);
          runner.run(element2);
        }
        for (T value2 : values) {
          TestElement element2 = element.copy();
          setter.invoke(element2.getCookie1().getCookie(), value);
          setter.invoke(element2.getCookie2().getCookie(), value2);
          runner.run(element2);
        }
      }
    }
  }

  private static class TestElement {
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
      if (sameStringIgnoreCase(cc1.getCookie().getName(), cc2.getCookie().getName())
          && sameStringIgnoreCase(cc1.getCookie().getDomain(), cc2.getCookie().getDomain())
          && sameString(cc1.getCookie().getPath(), cc2.getCookie().getPath())) {
        assertTrue(message, cc1.equals(cc2));
      } else {
        assertFalse(message, cc1.equals(cc2));
      }
    }

    public void testHashCode() {
      if (cc1.equals(cc2)) {
        assertEquals(message, cc1.hashCode(), cc2.hashCode());
      }
    }
  }

  private static boolean sameString(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equals(s2);
  }

  private static boolean sameStringIgnoreCase(String s1, String s2) {
    return (s1 == null) ? (s2 == null) : s1.equalsIgnoreCase(s2);
  }

  // compareTo

}
