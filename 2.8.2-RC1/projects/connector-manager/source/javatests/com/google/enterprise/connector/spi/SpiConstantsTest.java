// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

import junit.framework.TestCase;

import java.lang.reflect.Field;

public class SpiConstantsTest extends TestCase {
  /**
   * Tests that all of the SPI property name constants match
   * {@code RESERVED_PROPNAME_PREFIX}.
   */
  public void testReservedNamePattern() throws IllegalAccessException {
    for (Field field : SpiConstants.class.getFields()) {
      String name = field.getName();
      if (name.startsWith("PROPNAME")) {
        String value = (String) field.get(null);
        assertTrue(name + " = " + value,
            value.startsWith(SpiConstants.RESERVED_PROPNAME_PREFIX));
      }
    }
  }

  /**
   * Asserts that the given name is not a reserved name.
   */
  private void assertIsNotReserved(String value) {
    assertFalse(value,
        value.startsWith(SpiConstants.RESERVED_PROPNAME_PREFIX));
  }

  /** Tests variations on names that are not reserved. */
  public void testUnreservedNames() {
    assertIsNotReserved("google_name");
    assertIsNotReserved("mygoogle:name");
    assertIsNotReserved("other:name");
  }
}
