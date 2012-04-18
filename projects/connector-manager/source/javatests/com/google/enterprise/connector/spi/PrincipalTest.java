// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.spi;

import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link Principal} class.
 */
public class PrincipalTest extends TestCase {

  public void testSimpleConstructor() {
    Principal p = new Principal("test");
    assertEquals(PrincipalType.UNKNOWN, p.getPrincipalType());
    assertNull(p.getNamespace());
    assertEquals("test", p.getName());
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
                 p.getCaseSensitivityType());
  }

  public void testConstructor() {
    Principal p = new Principal(PrincipalType.UNQUALIFIED, "namespace", "test");
    assertEquals(PrincipalType.UNQUALIFIED, p.getPrincipalType());
    assertEquals("namespace", p.getNamespace());
    assertEquals("test", p.getName());
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_SENSITIVE,
                 p.getCaseSensitivityType());
  }

  public void testFullConstructor() {
    Principal p = new Principal(PrincipalType.UNQUALIFIED, "namespace", "test",
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    assertEquals(PrincipalType.UNQUALIFIED, p.getPrincipalType());
    assertEquals("namespace", p.getNamespace());
    assertEquals("test", p.getName());
    assertEquals(CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE,
                 p.getCaseSensitivityType());
  }

  public void testToString() {
    Principal p = new Principal(PrincipalType.UNQUALIFIED, "global", "test");
    String s = p.toString();
    assertTrue(s.contains("unqualified"));
    assertTrue(s.contains("global"));
    assertTrue(s.contains("test"));
    assertTrue(s.contains("-case-"));
  }

  public void testHashCode() {
    Principal p1 = new Principal("test");
    Principal p2 = new Principal(PrincipalType.UNKNOWN, null, "test");
    Principal p3 = new Principal(PrincipalType.UNKNOWN, "namespace", "test");
    Principal p4 =
        new Principal(PrincipalType.UNQUALIFIED, "namespace", "test");
    Principal p5 = new Principal("test2");
    Principal p6 =
        new Principal(PrincipalType.UNQUALIFIED, "namespace", "test");
    Principal p7 = new Principal(null);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertEquals(p4.hashCode(), p6.hashCode());
    assertFalse(p1.hashCode() == p3.hashCode());
    assertFalse(p1.hashCode() == p4.hashCode());
    assertFalse(p1.hashCode() == p5.hashCode());
    assertFalse(p7.hashCode() == p2.hashCode());
  }

  public void testEquals() {
    Principal p1 = new Principal("test");
    Principal p2 = new Principal(PrincipalType.UNKNOWN, null, "test");
    Principal p3 = new Principal(PrincipalType.UNKNOWN, "namespace", "test");
    Principal p4 =
        new Principal(PrincipalType.UNQUALIFIED, "namespace", "test");
    Principal p5 = new Principal("test2");
    Principal p6 =
        new Principal(PrincipalType.UNQUALIFIED, "namespace", "test");
    // p7 missing due to historical circumstances.
    Principal p8 = new Principal(PrincipalType.UNQUALIFIED, "global", "test");
    Principal p9 =
        new Principal(PrincipalType.UNQUALIFIED, "namespace", "test2");
    Principal p10 = new Principal(null);
    Principal p11 = new Principal(PrincipalType.UNKNOWN, "namespace", "test",
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    Principal p12 = new Principal(PrincipalType.UNKNOWN, "namespace", "TEST",
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);

    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertEquals(p2, p1);
    assertEquals(p4, p6);
    assertEquals(p4, p4);
    assertEquals(p6, p4);
    assertEquals(p11, p12);
    assertEquals(p10, new Principal(PrincipalType.UNKNOWN, null, null));
    assertFalse(p1.equals("test"));
    assertFalse(p4.equals(null));
    assureNotEqual(p1, p3, p4, p5, p8, p9, p10, p11);
  }

  /** Assure that no two principals are equal. */
  private void assureNotEqual(Principal... principals) {
    for (int i = 0; i < principals.length; i++) {
      for (int j = 0; j < principals.length; j++) {
        if (i != j) {
          assertFalse(principals[i] + " equals " + principals[j],
                      principals[i].equals(principals[j]));
        }
      }
    }
  }

  public void testCompareTo() {
    Principal p1 = new Principal("test");
    Principal p2 = new Principal(PrincipalType.UNQUALIFIED, null, "test");
    Principal p3 =
        new Principal(PrincipalType.UNQUALIFIED, "namespace", "test");
    Principal p4 = new Principal(PrincipalType.UNKNOWN, "namespace", "test");
    Principal p5 = new Principal("test2");
    Principal p6 = new Principal(PrincipalType.UNKNOWN, "namespace", "test");
    // p7 missing due to historical circumstances.
    Principal p8 = new Principal(PrincipalType.UNKNOWN, "global", "test");
    Principal p9 = new Principal(PrincipalType.UNKNOWN, "namespace", "test2");
    Principal p10 = new Principal(null);
    Principal p11 = new Principal(PrincipalType.UNKNOWN, "namespace", "test",
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    Principal p12 = new Principal(PrincipalType.UNKNOWN, "namespace", "TEST",
        CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE);
    Principal p13 = new Principal(PrincipalType.UNKNOWN, "namespace", "TEST",
        CaseSensitivityType.EVERYTHING_CASE_SENSITIVE);
    Principal p14 = new Principal(PrincipalType.UNKNOWN, "namespace", "Test2");

    assureCompareTo0(p1, p2);
    assureCompareTo0(p3, p4, p6);
    assureCompareTo0(p11, p12);
    assureCompareTo0(p12, p13);
    assureOrder(p1, p3, p4, p5, p6, p8, p9);
    assureOrder(p2, p3, p4, p5, p6, p8, p9);
    assureOrder(p3, p9);
    assureOrder(p5, p3, p4, p6, p8, p9);
    assureOrder(p6, p9);
    assureOrder(p8, p3, p4, p6, p9);
    assureOrder(p10, p1, p2, p3, p4, p5, p6, p8, p9);
    assureOrder(p11, p9);
    assureOrder(p12, p9);
    assureOrder(p13, p3, p4, p6, p9, p11, p14);
  }

  /**
   * Assure that compareTo returns 0 for any two principals.
   */
  private void assureCompareTo0(Principal... principals) {
    for (int i = 0; i < principals.length; i++) {
      for (int j = 0; j < principals.length; j++) {
        assertEquals(principals[i] + " not equals " + principals[j],
                     0, principals[i].compareTo(principals[j]));
      }
    }
  }

  /**
   * Assure that p sorts less than all other principals,
   * and all the other principals sort greater than p.
   */
  private void assureOrder(Principal p, Principal... principals) {
    for (Principal other : principals) {
      assertTrue(p + " not less than " + other,
                 p.compareTo(other) < 0);
      assertTrue(other + " not greater than " + p,
                 other.compareTo(p) > 0);
    }
  }
}
