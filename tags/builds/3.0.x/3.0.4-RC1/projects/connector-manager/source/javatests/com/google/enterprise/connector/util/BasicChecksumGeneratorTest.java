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

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

/**
 * How do you test a hash function?
 *
 */
public class BasicChecksumGeneratorTest extends TestCase {
  private static final int TEST_SIZE = 1000000;
  private final Random rand = new Random(0L);

  public void testConsistency() throws IOException {
    BasicChecksumGenerator csg = new BasicChecksumGenerator("SHA1");

    // Generate 10^6 bytes of junk.
    byte[] input = new byte[TEST_SIZE];
    for (int k = 0; k < input.length; ++k) {
      input[k] = (byte) rand.nextInt(256);
    }

    // Get checksum a few times and make sure the result is the same.
    String x = csg.getChecksum(new ByteArrayInputStream(input));
    for (int k = 0; k < 10; ++k) {
      String y = csg.getChecksum(new ByteArrayInputStream(input));
      assertEquals(x, y);
    }
  }

  /**
   * Make sure the checksum is reasonably effective at detecting changes.
   */
  public void testSingleByteChanges() throws IOException {
    BasicChecksumGenerator csg = new BasicChecksumGenerator("SHA1");

    // Generate 10^6 bytes of junk.
    byte[] input = new byte[TEST_SIZE];
    for (int k = 0; k < input.length; ++k) {
      input[k] = (byte) ((k * 123456789) % 256);
    }
    String x = csg.getChecksum(new ByteArrayInputStream(input));

    // Make a bunch of single-byte changes at arbitrary points and
    // make sure the checksum changes.
    for (int k = 0; k < 100; ++k) {
      int index = rand.nextInt(TEST_SIZE);
      byte original = input[index];
      input[k] = (byte) rand.nextInt(256);
      String y = csg.getChecksum(new ByteArrayInputStream(input));
      assertFalse(x.equals(y));
      input[index] = original;
    }

    // Make sure changing the first or last character changes the checksum.
    byte original = input[0];
    input[0] = (byte) rand.nextInt(256);
    String y = csg.getChecksum(new ByteArrayInputStream(input));
    assertFalse(x.equals(y));
    input[0] = original;

    original = input[TEST_SIZE - 1];
    input[TEST_SIZE - 1] = (byte) rand.nextInt(256);
    y = csg.getChecksum(new ByteArrayInputStream(input));
    assertFalse(x.equals(y));
  }
}
