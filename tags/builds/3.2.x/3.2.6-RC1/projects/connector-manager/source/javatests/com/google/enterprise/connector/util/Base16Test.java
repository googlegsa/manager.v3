// Copyright 2013 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.util;

import junit.framework.TestCase;

public class Base16Test extends TestCase {
  public void testEncodeNullString() {
    try {
      String data = null;
      Base16.lowerCase().encode(data);
      fail("Expected a NullPointerException");
    } catch (NullPointerException expected) {
    }
  }

  public void testEncodeNullBytes() {
    try {
      byte[] data = null;
      Base16.lowerCase().encode(data);
      fail("Expected a NullPointerException");
    } catch (NullPointerException expected) {
    }
  }

  public void testEncodeEmptyString() {
    assertEquals("", Base16.lowerCase().encode(""));
  }

  public void testEncodeEmptyBytes() {
    assertEquals("", Base16.lowerCase().encode(new byte[0]));
  }

  public void testEncodeBytes() {
    byte[] data = { 0xC, 0xA, 0xF, 0xE, 0xB, 0xA, 0xB, 0xE };
    String expected = "0c0a0f0e0b0a0b0e";
    assertEquals(expected, Base16.lowerCase().encode(data));
    assertEquals(expected.toUpperCase(), Base16.upperCase().encode(data));
  }

  public void testEncodeWithFormat() {
    // Construct an array with all byte values.
    byte[] data = new byte[256];
    byte value = Byte.MIN_VALUE;
    for (int i = 0; i < data.length; i++) {
      data[i] = value++;
    }

    assertEncoding(data, Base16.lowerCase(), "8081", "7e7f", "%02x");
    assertEncoding(data, Base16.upperCase(), "8081", "7E7F", "%02X");
  }

  private void assertEncoding(byte[] data, Base16 encoding, String first,
      String last, String format) {
    String encoded = encoding.encode(data);
    assertEquals(512, encoded.length());
    assertTrue(encoded, encoded.startsWith(first));
    assertTrue(encoded, encoded.endsWith(last));

    StringBuilder builder = new StringBuilder();
    for (byte b : data) {
      builder.append(String.format(format, b));
    }
    assertEquals(builder.toString(), encoded);
  }

  /**
   * Sort order of encoded UTF-16 strings is not preserved.
   * See http://en.wikipedia.org/wiki/Unicode_block
   */
  public void testStringOrdering() {
    // Pick a BMP code point above the UTF-16 surrogate range
    // (U+D800-U+DFFF) and a supplementary character above the BMP:
    // U+FB00  LATIN SMALL LIGATURE FF
    // U+10400 DESERET CAPITAL LETTER LONG I
    String ff = new String(Character.toChars(0xFB00));
    String longI = new String(Character.toChars(0x10400));

    String encodedFf = Base16.lowerCase().encode(ff);
    String encodedLongI = Base16.lowerCase().encode(longI);

    assertTrue(ff.compareTo(longI) > 0);
    assertFalse(encodedFf.compareTo(encodedLongI) > 0);
  }
}
