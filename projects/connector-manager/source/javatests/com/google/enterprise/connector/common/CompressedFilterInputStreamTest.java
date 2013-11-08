// Copyright 2009 Google Inc.  All Rights Reserved.
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

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;

/**
 * Test for CompressedFilterInputStream
 */
public class CompressedFilterInputStreamTest extends TestCase {
  // Size of CompressedFilterInputStream buffer for testing.
  private static final int BUFF_SIZE = 8192;

  private static byte[] emptyInput = new byte[0];
  private static byte[] tinyInput = new byte[]{'a'};
  private static byte[] smallInput = new byte[]{ 'a', 'b', 'c', 'd' };
  private static byte[] mediumInput =
      ( " Google's indices consist of information that has been"
      + " identified, indexed and compiled through an automated"
      + " process with no advance review by human beings. Given"
      + " the enormous volume of web site information added,"
      + " deleted, and changed on a frequent basis, Google cannot"
      + " and does not screen anything made available through its"
      + " indices. For each web site reflected in Google's"
      + " indices, if either (i) a site owner restricts access to"
      + " his or her web site or (ii) a site is taken down from"
      + " the web, then, upon receipt of a request by the site"
      + " owner or a third party in the second instance, Google"
      + " would consider on a case-by-case basis requests to"
      + " remove the link to that site from its indices. However,"
      + " if the operator of the site does not take steps to"
      + " prevent it, the automatic facilities used to create"
      + " the indices are likely to find that site and index it"
      + " again in a relatively short amount of time.").getBytes();
  private static byte[] largeInput;
  private static byte[] bigInput;

  private static byte[] emptyExpected;
  private static byte[] tinyExpected;
  private static byte[] smallExpected;
  private static byte[] mediumExpected;
  private static byte[] largeExpected;
  private static byte[] bigExpected;

  @Override
  protected void setUp() throws Exception {

    // Make an input exactly the size of the CompressedFilterInputStream
    // internal buffer.
    int len = BUFF_SIZE;
    largeInput = new byte[len];
    for (int i = 0; i < len; i++) {
      largeInput[i] = (byte) Math.round(Math.random());
    }

    // Make an input larger than the CompressedFilterInputStream
    // internal buffer.  Large enough to test first full buffer,
    // subsequent full buffer(s), final partial buffer.
    len = 2 * BUFF_SIZE + 69;
    bigInput = new byte[len];
    for (int i = 0; i < len; i++) {
      bigInput[i] = (byte) Math.round(Math.random());
    }

    // Precalculate the expected results.
    emptyExpected = compress(emptyInput);
    tinyExpected = compress(tinyInput);
    smallExpected = compress(smallInput);
    mediumExpected = compress(mediumInput);
    largeExpected = compress(largeInput);
    bigExpected = compress(bigInput);

    input = mediumInput;
    expect = mediumExpected;
  }

  /* Return a compressed version of the input. */
  private static byte[] compress(byte[] input) {
    Deflater deflater = new Deflater();
    byte[] compressed = new byte[Math.max(BUFF_SIZE, input.length * 4)];
    deflater.setInput(input);
    deflater.finish();
    int len = deflater.deflate(compressed);
    assertTrue(deflater.finished());
    deflater.end();

    // Return a byte[] that is exactly the right size.
    byte[] output = new byte[len];
    System.arraycopy(compressed, 0, output, 0, len);
    return output;
  }

  /* These are extremely useful when debugging these tests.
  private static String toHex(byte[] input) {
    return toHex(input, 0, input.length);
  }

  private static String toHex(byte[] input, int off, int len) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < len ; i++) {
      byte c = input[off + i];
      Base16.upperCase().encode(c, buf);
      buf.append(' ');
    }
    return(buf.toString().trim());
  }
  */

  /**
   * Test read() one byte.
   */
  public void testRead() throws IOException {
    checkRead(emptyInput, emptyExpected);
    checkRead(tinyInput, tinyExpected);
    checkRead(smallInput, smallExpected);
    checkRead(mediumInput, mediumExpected);
    checkRead(largeInput, largeExpected);
    checkRead(bigInput, bigExpected);
  }

  private void checkRead(byte[] input, byte[] expected) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(input);
    CompressedFilterInputStream is =
        new CompressedFilterInputStream(bais, BUFF_SIZE);
    int val = -1;
    byte[] result = new byte[expected.length];
    int index = 0;
    while (-1 != (val = is.read())) {
      result[index++] = (byte) val;
    }
    is.close();
    assertEquals(expected.length, index);
    assertTrue(Arrays.equals(expected, result));
  }

  /**
   * Test read(byte[] dest, int off, int len);
   */
  public void testReadArray() throws IOException {
    checkReadArray(emptyInput, emptyExpected);
    checkReadArray(tinyInput, tinyExpected);
    checkReadArray(smallInput, smallExpected);
    checkReadArray(mediumInput, mediumExpected);
    checkReadArray(largeInput, largeExpected);
    checkReadArray(bigInput, bigExpected);
  }

  private void checkReadArray(byte[] input, byte[] expected) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(input);
    CompressedFilterInputStream is =
        new CompressedFilterInputStream(bais, BUFF_SIZE);
    byte[] result = new byte[expected.length];

    int total = 0;
    int val;
    while (-1 != (val = is.read(result, total, result.length - total))) {
      total += val;
    }
    is.close();
    assertEquals(expected.length, total);
    assertTrue(Arrays.equals(expected, result));
  }

  /**
   * ByteArrayInputStream that returns a single byte at a time even if you
   * request more.
   */
  private class SingleByteArrayInputStream extends ByteArrayInputStream {
    public SingleByteArrayInputStream(byte[] bytes) {
      super(bytes);
    }

    @Override
    public int read(byte[] b, int off, int len) {
      int byteValue = read();

      if (-1 == byteValue) {
        return -1;
      } else {
        b[off] = (byte) byteValue;
        return 1;
      }
    }
  }

  /**
   * Test that this stream works even if the underlying stream does not return
   * bytes in multiples of 3.
   */
  public void testBug243976() throws IOException {
    byte[] input = mediumInput;
    byte[] expected = mediumExpected;
    SingleByteArrayInputStream bais = new SingleByteArrayInputStream(input);
    CompressedFilterInputStream is = new CompressedFilterInputStream(bais);
    int val;
    byte[] result = new byte[expected.length];
    int index = 0;
    while (-1 != (val = is.read())) {
      result[index] = (byte) val;
      index++;
    }
    assertTrue(Arrays.equals(expected, result));
  }

  private static byte[] input;
  private static byte[] expect;

  /* Test read(byte[]) interface. */
  public void testReadByteArray() throws Exception {
    byteArrayRead(1);
    byteArrayRead(2);
    byteArrayRead(3);
    byteArrayRead(4);
    byteArrayRead(5);
    byteArrayRead(6);
    byteArrayRead(7);
    byteArrayRead(8);
    byteArrayRead(16);
    byteArrayRead(23);
    byteArrayRead(64);
    byteArrayRead(expect.length);
    byteArrayRead(((expect.length + 3)/4)*4);
    byteArrayRead(expect.length - 1);
    byteArrayRead(2048);
  }

  /* Test read(byte[], off, len) interface. */
  public void testReadByteArrayWithOffLen() throws Exception {
    byteArrayRead2(1);
    byteArrayRead2(2);
    byteArrayRead2(3);
    byteArrayRead2(4);
    byteArrayRead2(5);
    byteArrayRead2(6);
    byteArrayRead2(7);
    byteArrayRead2(8);
    byteArrayRead2(16);
    byteArrayRead2(23);
    byteArrayRead2(64);
    byteArrayRead2(expect.length);
    byteArrayRead2(((expect.length + 3)/4)*4);
    byteArrayRead2(expect.length - 1);
    byteArrayRead2(2048);
  }

  /* Test read(byte[], off, len) interface where off != 0. */
  public void testReadByteArrayWithOffset() throws Exception {
    byteArrayRead3(1);
    byteArrayRead3(2);
    byteArrayRead3(3);
    byteArrayRead3(4);
    byteArrayRead3(5);
    byteArrayRead3(6);
    byteArrayRead3(7);
    byteArrayRead3(8);
    byteArrayRead3(16);
    byteArrayRead3(23);
    byteArrayRead3(64);
    byteArrayRead3(expect.length);
    byteArrayRead3(((expect.length + 3)/4)*4);
    byteArrayRead3(expect.length - 1);
    byteArrayRead3(2048);
  }

  /* Test use of mixed read() and read(byte[], off, len) interface. */
  public void testReadWithReadByteArray() throws Exception {
    byteArrayRead4(1, 1);
    byteArrayRead4(2, 1);
    byteArrayRead4(3, 1);
    byteArrayRead4(4, 1);
    byteArrayRead4(5, 1);
    byteArrayRead4(6, 1);
    byteArrayRead4(7, 1);
    byteArrayRead4(8, 1);
    byteArrayRead4(16, 1);
    byteArrayRead4(23, 1);
    byteArrayRead4(64, 1);
    byteArrayRead4(expect.length, 1);
    byteArrayRead4(((expect.length + 3)/4)*4, 1);
    byteArrayRead4(expect.length - 1, 1);
    byteArrayRead4(2048, 1);

    byteArrayRead4(1, 2);
    byteArrayRead4(2, 2);
    byteArrayRead4(3, 2);
    byteArrayRead4(4, 2);
    byteArrayRead4(5, 2);
    byteArrayRead4(6, 2);
    byteArrayRead4(7, 2);
    byteArrayRead4(8, 2);
    byteArrayRead4(16, 2);
    byteArrayRead4(23, 2);
    byteArrayRead4(64, 2);
    byteArrayRead4(expect.length, 2);
    byteArrayRead4(((expect.length + 3)/4)*4, 2);
    byteArrayRead4(expect.length - 1, 2);
    byteArrayRead4(2048, 2);

    byteArrayRead4(1, 3);
    byteArrayRead4(2, 3);
    byteArrayRead4(3, 3);
    byteArrayRead4(4, 3);
    byteArrayRead4(5, 3);
    byteArrayRead4(6, 3);
    byteArrayRead4(7, 3);
    byteArrayRead4(8, 3);
    byteArrayRead4(16, 3);
    byteArrayRead4(23, 3);
    byteArrayRead4(64, 3);
    byteArrayRead4(expect.length, 3);
    byteArrayRead4(((expect.length + 3)/4)*4, 3);
    byteArrayRead4(expect.length - 1, 3);
    byteArrayRead4(2048, 3);

    byteArrayRead4(1, 4);
    byteArrayRead4(2, 4);
    byteArrayRead4(3, 4);
    byteArrayRead4(4, 4);
    byteArrayRead4(5, 4);
    byteArrayRead4(6, 4);
    byteArrayRead4(7, 4);
    byteArrayRead4(8, 4);
    byteArrayRead4(16, 4);
    byteArrayRead4(23, 4);
    byteArrayRead4(64, 4);
    byteArrayRead4(expect.length, 4);
    byteArrayRead4(((expect.length + 3)/4)*4, 4);
    byteArrayRead4(expect.length - 1, 4);
    byteArrayRead4(2048, 4);
  }

  /* Test read(byte[]) interface. */
  public void byteArrayRead(int buffsize) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input);
    CompressedFilterInputStream is = new CompressedFilterInputStream(bais);
    byte[] buff = new byte[buffsize];
    byte[] result = new byte[expect.length];

    int total = 0;
    int val;
    while (-1 != (val = is.read(buff))) {
      System.arraycopy(buff, 0, result, total, val);
      total += val;
    }
    is.close();
    assertTrue(Arrays.equals(result, expect));
  }

  /* Test read(byte[], off, len) interface. */
  public void byteArrayRead2(int buffsize) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input);
    CompressedFilterInputStream is = new CompressedFilterInputStream(bais);
    byte[] buff = new byte[buffsize];
    byte[] result = new byte[expect.length];

    int total = 0;
    int val;
    while (-1 != (val = is.read(buff, 0, buffsize))) {
      System.arraycopy(buff, 0, result, total, val);
      total += val;
    }
    is.close();
    assertTrue(Arrays.equals(result, expect));
  }

  /* Test read(byte[], off, len) interface, where off != 0. */
  public void byteArrayRead3(int buffsize) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input);
    CompressedFilterInputStream is = new CompressedFilterInputStream(bais);
    byte[] buff = new byte[buffsize + 3];
    byte[] result = new byte[expect.length];

    int total = 0;
    int val;
    while (-1 != (val = is.read(buff, 3, buffsize))) {
      System.arraycopy(buff, 3, result, total, val);
      total += val;
    }
    is.close();
    assertTrue(Arrays.equals(result, expect));
  }

  /* Test use of mixed read() and read(byte[], off, len) interface. */
  public void byteArrayRead4(int buffsize, int readsize) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input);
    CompressedFilterInputStream is = new CompressedFilterInputStream(bais);
    byte[] buff = new byte[buffsize];
    byte[] result = new byte[expect.length];

    int total = 0;
    int val;
    while ((readsize-- > 0) && (-1 != (val = is.read()))) {
      result[total++] = (byte) val;
    }

    while (-1 != (val = is.read(buff, 0, buffsize))) {
      System.arraycopy(buff, 0, result, total, val);
      total += val;
    }
    is.close();
    assertTrue(Arrays.equals(result, expect));
  }
}
