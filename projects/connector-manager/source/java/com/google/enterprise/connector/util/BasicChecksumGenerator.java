// Copyright 2009 Google Inc.
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

package com.google.enterprise.connector.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * An implementation of {@link ChecksumGenerator} that return
 * hexadecimal-encoded checksums using algorithms from
 * {@code java.security.MessageDigest}.
 *
 * @see java.security.MessageDigest
 * @since 2.8
 */
public class BasicChecksumGenerator implements ChecksumGenerator {
  /* Algorithms supported for MessageDigest. */
  /** The MD2 message digest algorithm as defined in RFC 1319. */
  public static final String MD2 = "MD2";

  /** The MD5 message digest algorithm as defined in RFC 1321. */
  public static final String MD5 = "MD5";

  /** The Secure Hash Algorithm, as defined in NIST FIPS 180-1. */
  public static final String SHA1 = "SHA-1";

  /**
   * The Secure Hash Algorithm, as defined in NIST FIPS 180-2.
   * SHA-256 is a 256-bit hash function intended to provide 128 bits of
   * security against collision attacks.
   */
  public static final String SHA256 = "SHA-256";

  /**
   * The Secure Hash Algorithm, as defined in NIST FIPS 180-2.
   * A 384-bit hash may be obtained by truncating the SHA-512 output.
   */
  public static final String SHA384 = "SHA-384";

  /**
   * The Secure Hash Algorithm, as defined in NIST FIPS 180-2.
   * SHA-512 is a 512-bit hash function intended to provide 256 bits of
   * security.
   */
  public static final String SHA512 = "SHA-512";

  private static final int BUF_SIZE = 32768;
  private final String algorithm;
  private static final char[] HEX_DIGITS =
      new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                  'a', 'b', 'c', 'd', 'e', 'f'};
  private static char[][] byteToHex = new char[256][2];

  {
    // Create a table to convert from bytes to hex characters.
    for (int k = 0; k < 256; ++k) {
      byteToHex[k][0] = HEX_DIGITS[k & 0xF];
      byteToHex[k][1] = HEX_DIGITS[k >>> 4 & 0xF];
    }
  }

  /**
   * Constructs a {@code BasicChecksumGenerator} that uses the specified
   * message digest algorithm.  The supported algorithms are:
   * {@link BasicChecksumGenerator#MD2 "MD2"},
   * {@link BasicChecksumGenerator#MD5 "MD5"},
   * {@link BasicChecksumGenerator#SHA1 "SHA-1"},
   * {@link BasicChecksumGenerator#SHA256 "SHA-256"},
   * {@link BasicChecksumGenerator#SHA384 "SHA-384"}, and
   * {@link BasicChecksumGenerator#SHA512 "SHA-512"}
   *
   * @param algorithm message digest algorithm
   */
  public BasicChecksumGenerator(String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * Returns the message digest checksum as an unecoded array of bytes.
   *
   * @param in input stream to create a checksum for
   * @return a checksum for the bytes of {@code in}
   * @throws IOException
   */
  byte[] getDigest(InputStream in) throws IOException {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Failed to get a message digest for "
                                 + algorithm);
    }
    try {
      byte[] buf = new byte[BUF_SIZE];
      int count = in.read(buf);
      while (count != -1) {
        digest.update(buf, 0, count);
        count = in.read(buf);
      }
      return digest.digest();
    } finally {
      in.close();
    }
  }

  /**
   * Returns a hexadecimal string representation of the message digest
   * checksum of the input stream.
   *
   * @param in input stream to create a checksum for
   * @return a checksum for the bytes of {@code in}
   * @throws IOException
   */
  /* @Override */
  public String getChecksum(InputStream in) throws IOException {
    byte[] digestBytes = getDigest(in);
    StringBuilder result = new StringBuilder();
    for (int k = 0; k < digestBytes.length; ++k) {
      result.append(byteToHex[digestBytes[k] & 0xFF]);
    }
    return result.toString();
  }

  /**
   * Returns a hexadecimal string representation of the message digest
   * checksum of the input string.
   *
   * @param input a String to create a checksum for
   * @return a checksum for the bytes of {@code input}
   */
  /* @Override */
  public String getChecksum(String input) {
    try {
      return getChecksum(
          new ByteArrayInputStream(input.getBytes("UTF-8")));
    } catch (IOException e) {
      throw new RuntimeException("IO exception reading a string!?");
    }
  }
}
