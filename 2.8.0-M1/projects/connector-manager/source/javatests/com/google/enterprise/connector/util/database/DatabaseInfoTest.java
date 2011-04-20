// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.util.database;

import junit.framework.TestCase;

/**
 * Tests for DatabaseInfo.
 */
public class DatabaseInfoTest extends TestCase {

  private static final String productName = "Apple";
  private static final String majorVersion = "Banana";
  private static final String minorVersion = "Cherry";
  private static final String minorVersion2 = "Strawberry_Rhubarb";
  private static final String description = "I like pie";

  // Test isSanitized().
  public void testIsSanitized() {
    assertTrue(DatabaseInfo.isSanitized(null));
    assertTrue(DatabaseInfo.isSanitized(""));
    assertTrue(DatabaseInfo.isSanitized("abcd"));
    assertTrue(DatabaseInfo.isSanitized("abcd1964"));
    assertTrue(DatabaseInfo.isSanitized("abcd_efgh"));
    assertTrue(DatabaseInfo.isSanitized("abcd_efgh-ijkl"));
    assertFalse(DatabaseInfo.isSanitized("AbcdEfgh"));
    assertFalse(DatabaseInfo.isSanitized("abcd.efgh"));
    assertFalse(DatabaseInfo.isSanitized("abcd efgh"));
    assertFalse(DatabaseInfo.isSanitized(" abcd "));
    assertFalse(DatabaseInfo.isSanitized("!@#$%^&*()"));
  }

  // Test sanitize().
  public void testSanitize() {
    assertEquals(null, DatabaseInfo.sanitize(null));
    assertEquals("", DatabaseInfo.sanitize(""));
    assertEquals("abcd", DatabaseInfo.sanitize("abcd"));
    assertEquals("abcd1964", DatabaseInfo.sanitize("abcd1964"));
    assertEquals("abcd_efgh", DatabaseInfo.sanitize("abcd_efgh"));
    assertEquals("abcd_efgh-ijkl", DatabaseInfo.sanitize("abcd_efgh-ijkl"));
    assertEquals("abcdefgh", DatabaseInfo.sanitize("AbcdEfgh"));
    assertEquals("abcd-efgh", DatabaseInfo.sanitize("abcd.efgh"));
    assertEquals("abcd-efgh", DatabaseInfo.sanitize("abcd efgh"));
    assertEquals("abcd", DatabaseInfo.sanitize(" abcd "));
    assertEquals("", DatabaseInfo.sanitize("!@#$%^&*()"));
  }

  // Test the straight getters.
  public void testGetters() {
    DatabaseInfo dbInfo =
        new DatabaseInfo("apple", "banana", "cherry", "Hello World");
    assertEquals("apple", dbInfo.getProductName());
    assertEquals("banana", dbInfo.getMajorVersion());
    assertEquals("cherry", dbInfo.getMinorVersion());
    assertEquals("Hello World", dbInfo.getDescription());

    dbInfo = new DatabaseInfo("apple", "", null, null);
    assertEquals("apple", dbInfo.getProductName());
    assertEquals("", dbInfo.getMajorVersion());
    assertNull(dbInfo.getMinorVersion());
    assertNull(dbInfo.getDescription());
  }

  // Test generation of ResourceBundle extension.
  public void testGetResourceBundleExtension() {
    DatabaseInfo dbInfo =  new DatabaseInfo(null, null, null, null);
    assertEquals("", dbInfo.getResourceBundleExtension());

    dbInfo =  new DatabaseInfo("", "", "", "");
    assertEquals("", dbInfo.getResourceBundleExtension());

    dbInfo =  new DatabaseInfo("apple", "", null, null);
    assertEquals("_apple", dbInfo.getResourceBundleExtension());

    dbInfo =  new DatabaseInfo("apple", "banana", null, null);
    assertEquals("_apple_banana", dbInfo.getResourceBundleExtension());

    dbInfo =  new DatabaseInfo("apple", "banana", "cherry", null);
    assertEquals("_apple_banana_cherry", dbInfo.getResourceBundleExtension());

    dbInfo =  new DatabaseInfo("apple", "", "cherry", null);
    assertEquals("_apple_cherry", dbInfo.getResourceBundleExtension());

    dbInfo =  new DatabaseInfo("apple", "banana", "strawberry_rhubarb", null);
    assertEquals("_apple_banana_strawberry_rhubarb",
                 dbInfo.getResourceBundleExtension());
  }
}
