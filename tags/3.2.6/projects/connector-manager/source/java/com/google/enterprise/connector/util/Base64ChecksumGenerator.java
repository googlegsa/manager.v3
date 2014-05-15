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

package com.google.enterprise.connector.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of {@link ChecksumGenerator} that return Base64-encoded
 * checksums using algorithms from {@code java.security.MessageDigest}.
 *
 * @see java.security.MessageDigest
 * @since 2.8
 */
public class Base64ChecksumGenerator extends BasicChecksumGenerator {

  /**
   * Constructs a {@code Base64ChecksumGenerator} that uses the specified
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
  public Base64ChecksumGenerator(String algorithm) {
    super(algorithm);
  }

  /**
   * Returns a Base64 encoded representation of the message digest
   * checksum of the input stream.
   *
   * @param in input stream to create a checksum for
   * @return a checksum for the bytes of {@code in}
   * @throws IOException if there is an error reading the input stream
   */
  @Override
  public String getChecksum(InputStream in) throws IOException {
    return Base64.encodeWebSafe(super.getDigest(in), false);
  }
}
