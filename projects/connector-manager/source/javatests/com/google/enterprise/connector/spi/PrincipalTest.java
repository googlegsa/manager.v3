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

import com.google.enterprise.connector.spi.SpiConstants.PrincipalType;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link Principal} class.
 */
public class PrincipalTest extends TestCase {

  public void testSimpleConstructor() {
    Principal p = new Principal("test");
    assertNull(p.getType());
    assertNull(p.getNamespace());
    assertEquals("test", p.getName());
  }

  public void testConstructor() {
    Principal p = new Principal(PrincipalType.DN, "namespace", "test");
    assertEquals(PrincipalType.DN, p.getType());
    assertEquals("namespace", p.getNamespace());
    assertEquals("test", p.getName());
  }

  public void testToString() {
    Principal p = new Principal(PrincipalType.DN, "global", "test");
    String s = p.toString();
    assertTrue(s.contains("dn"));
    assertTrue(s.contains("global"));
    assertTrue(s.contains("test"));
  }

  public void testHashCode() {
    Principal p1 = new Principal("test");
    Principal p2 = new Principal(null, null, "test");
    Principal p3 = new Principal(null, "namespace", "test");
    Principal p4 = new Principal(PrincipalType.DN, "namespace", "test");
    Principal p5 = new Principal("test2");
    Principal p6 = new Principal(PrincipalType.DN, "namespace", "test");
    Principal p7 = new Principal(null, null, null);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertEquals(p4.hashCode(), p6.hashCode());
    assertFalse(p1.hashCode() == p3.hashCode());
    assertFalse(p1.hashCode() == p4.hashCode());
    assertFalse(p1.hashCode() == p5.hashCode());
    assertFalse(p7.hashCode() == p2.hashCode());
  }

  public void testEquals() {
    Principal p1 = new Principal("test");
    Principal p2 = new Principal(null, null, "test");
    Principal p3 = new Principal(null, "namespace", "test");
    Principal p4 = new Principal(PrincipalType.DN, "namespace", "test");
    Principal p5 = new Principal("test2");
    Principal p6 = new Principal(PrincipalType.DN, "namespace", "test");
    Principal p7 = new Principal(PrincipalType.DNS, "namespace", "test");
    Principal p8 = new Principal(PrincipalType.DN, "global", "test");
    Principal p9 = new Principal(PrincipalType.DN, "namespace", "test2");
    Principal p10 = new Principal(null, null, null);

    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertEquals(p2, p1);
    assertEquals(p4, p6);
    assertEquals(p4, p4);
    assertEquals(p6, p4);
    assertEquals(p10, new Principal(null, null, null));
    assertFalse(p1.equals("test"));
    assertFalse(p4.equals(null));
    assureNotEqual(p1, p3, p4, p5, p7, p8, p9, p10);
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
    Principal p2 = new Principal(null, null, "test");
    Principal p3 = new Principal(null, "namespace", "test");
    Principal p4 = new Principal(PrincipalType.DN, "namespace", "test");
    Principal p5 = new Principal("test2");
    Principal p6 = new Principal(PrincipalType.DN, "namespace", "test");
    Principal p7 = new Principal(PrincipalType.DNS, "namespace", "test");
    Principal p8 = new Principal(PrincipalType.DN, "global", "test");
    Principal p9 = new Principal(PrincipalType.DN, "namespace", "test2");
    Principal p10 = new Principal(null, null, null);

    assertTrue(p10.compareTo(null) > 0);
    assureCompareTo0(p1, p2);
    assureCompareTo0(p3, p4, p6, p7);
    assureOrder(p1, p3, p4, p5, p6, p7, p8, p9);
    assureOrder(p2, p3, p4, p5, p6, p7, p8, p9);
    assureOrder(p3, p9);
    assureOrder(p5, p3, p4, p6, p7, p8, p9);
    assureOrder(p6, p9);
    assureOrder(p8, p3, p4, p6, p7, p9);
    assureOrder(p10, p1, p2, p3, p4, p5, p6, p7, p8, p9);
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
