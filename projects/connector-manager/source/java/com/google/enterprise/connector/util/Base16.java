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

import com.google.common.base.Charsets;

/**
 * Base16 encoder class. Converts strings and byte arrays to hex strings.
 * Base16 is an URL-safe and usually order-preserving encoding, but it
 * does take more space than some other encodings without those
 * properties, such as Base64.
 * <p>
 * Note: The conversion of Java strings to bytes using UTF-8 is not
 * order-preserving, because the binary order of UTF-16 is different
 * from UTF-8 in the case of supplementary characters.
 *
 * @since TODO(jlacey)
 * @see <a href="http://tools.ietf.org/html/rfc4648">RFC 4648</a>
 * @see <a href="http://www.unicode.org/L2/L2001/01230-utf8s.htm">UTF8-S</a>
 */
/* TODO(jlacey): Replace with Guava's BaseEncoding when we upgrade to r14? */
public class Base16 {
  private static final String HEX_STRING = "0123456789abcdef";
  private static final char[] HEX_CHARS_LOWER = HEX_STRING.toCharArray();
  private static final char[] HEX_CHARS_UPPER =
      HEX_STRING.toUpperCase().toCharArray();

  /** Constructs a Base16 encoder that uses lowercase hex letters (a-f). */
  public static Base16 lowerCase() {
    return new Base16(HEX_CHARS_LOWER);
  }

  /** Constructs a Base16 encoder that uses uppercase hex letters (A-F). */
  public static Base16 upperCase() {
    return new Base16(HEX_CHARS_UPPER);
  }

  private final char[] alphabet;

  private Base16(char[] alphabet) {
    this.alphabet = alphabet;
  }

  /**
   * Encodes a string into Base16 notation. This method does not
   * preserve the sort order of the input strings in the presence of
   * Unicode supplementary characters.
   *
   * @param value the data to convert
   * @return a string of the equivalent hex representation of the bytes
   */
  public String encode(String value) {
    return encode(value.getBytes(Charsets.UTF_8));
  }

  /**
   * Encodes a byte array into Base16 notation.
   *
   * @param value the data to convert
   * @return a string of the equivalent hex representation of the bytes
   */
  public String encode(byte[] value) {
    StringBuilder builder = new StringBuilder(value.length * 2);
    encode(value, builder);
    return builder.toString();
  }

  /**
   * Encodes a byte array into Base16 notation and appends the result
   * to the {@code StringBuilder}.
   *
   * @param value the data to convert
   * @param builder the {@code StringBuilder} to append the encoded string to
   */
  public void encode(byte[] value, StringBuilder builder) {
    for (byte b : value) {
      encode(b, builder);
    }
  }

  /**
   * Encodes a single byte into Base16 notation and appends the result
   * to the {@code StringBuilder}.
   *
   * @param b the byte to convert
   * @param builder the {@code StringBuilder} to append the encoded string to
   */
  public void encode(byte b, StringBuilder builder) {
    builder.append(alphabet[(b >> 4) & 0x0F]).append(alphabet[b & 0x0F]);
  }
}
