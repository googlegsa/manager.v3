// Copyright 2006 Google Inc.  All Rights Reserved.
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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Test for Base64FilterInputStream
 */
public class Base64FilterInputStreamTest extends TestCase {
  public void testRead() throws IOException {
    byte[] bytes = new byte[]{ 'a', 'b', 'c'};
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    Base64FilterInputStream is = new Base64FilterInputStream(bais);
    int val;
    byte[] expectedBytes = new byte[]{ 'Y', 'W', 'J', 'j' };
    byte[] resultBytes = new byte[expectedBytes.length];
    int index = 0;
    while (-1 != (val = is.read())) {
      resultBytes[index] = (byte) val;
      index++;
    }
    Assert.assertTrue(Arrays.equals(expectedBytes, resultBytes));
  }

  public void testReadArray() throws IOException {
    byte[] bytes = new byte[]{ 'a', 'b', 'c', 'd' };
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    Base64FilterInputStream is = new Base64FilterInputStream(bais);
    int val;
    byte[] expectedBytes = new byte[]{ 'Y', 'W', 'J', 'j', 'Z', 'A', '=', '=' };
    byte[] resultBytes = new byte[expectedBytes.length];
    int bytesRead = is.read(resultBytes, 0, resultBytes.length);
    Assert.assertEquals(expectedBytes.length, bytesRead);
    Assert.assertTrue(Arrays.equals(expectedBytes, resultBytes));
  }

  /**
   * Compare results from read() and read(bytes[], int, int) methods.
   * @throws IOException
   */
  public void testReadMethods() throws IOException {
    byte[] bytes = new byte[]{ 'a' };
    ByteArrayInputStream bais1 = new ByteArrayInputStream(bytes);
    Base64FilterInputStream is1 = new Base64FilterInputStream(bais1);
    ByteArrayInputStream bais2 = new ByteArrayInputStream(bytes);
    Base64FilterInputStream is2 = new Base64FilterInputStream(bais2);

    int val;
    byte[] resultBytes = new byte[4];
    val = is1.read(resultBytes, 0, 4);
    Assert.assertEquals(4, val);

    int index = 0;
    while (-1 != (val = is2.read())) {
      Assert.assertTrue(resultBytes[index] == (byte) val);
      index++;
    }
  }

  /**
   * ByteArrayInputStream that returns a single byte at a time even if you
   * request more.
   */
  private class SingleByteArrayInputStream extends ByteArrayInputStream {
    public SingleByteArrayInputStream(byte[] bytes) {
      super(bytes);
    }

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
    byte[] bytes = new byte[]{ 'a', 'b', 'c'};
    SingleByteArrayInputStream bais = new SingleByteArrayInputStream(bytes);
    Base64FilterInputStream is = new Base64FilterInputStream(bais);
    int val;
    byte[] expectedBytes = new byte[]{ 'Y', 'W', 'J', 'j' };
    byte[] resultBytes = new byte[expectedBytes.length];
    int index = 0;
    while (-1 != (val = is.read())) {
      resultBytes[index] = (byte) val;
      index++;
    }
    Assert.assertTrue(Arrays.equals(expectedBytes, resultBytes));
  }

  static String input = " Google's indices consist of information that has been" +
                   " identified, indexed and compiled through an automated" +
                   " process with no advance review by human beings. Given" +
                   " the enormous volume of web site information added," +
                   " deleted, and changed on a frequent basis, Google cannot" +
                   " and does not screen anything made available through its" +
                   " indices. For each web site reflected in Google's" +
                   " indices, if either (i) a site owner restricts access to" +
                   " his or her web site or (ii) a site is taken down from" +
                   " the web, then, upon receipt of a request by the site" +
                   " owner or a third party in the second instance, Google" +
                   " would consider on a case-by-case basis requests to" +
                   " remove the link to that site from its indices. However," +
                   " if the operator of the site does not take steps to" +
                   " prevent it, the automatic facilities used to create" +
                   " the indices are likely to find that site and index it" +
                   " again in a relatively short amount of time.";
  static String expect = "IEdvb2dsZSdzIGluZGljZXMgY29uc2lzdCBvZiBpbmZvcm1hdGlvbi" +
                    "B0aGF0IGhhcyBiZWVuIGlkZW50aWZpZWQsIGluZGV4ZWQgYW5kIGNv" +
                    "bXBpbGVkIHRocm91Z2ggYW4gYXV0b21hdGVkIHByb2Nlc3Mgd2l0aC" +
                    "BubyBhZHZhbmNlIHJldmlldyBieSBodW1hbiBiZWluZ3MuIEdpdmVu" +
                    "IHRoZSBlbm9ybW91cyB2b2x1bWUgb2Ygd2ViIHNpdGUgaW5mb3JtYX" +
                    "Rpb24gYWRkZWQsIGRlbGV0ZWQsIGFuZCBjaGFuZ2VkIG9uIGEgZnJl" +
                    "cXVlbnQgYmFzaXMsIEdvb2dsZSBjYW5ub3QgYW5kIGRvZXMgbm90IH" +
                    "NjcmVlbiBhbnl0aGluZyBtYWRlIGF2YWlsYWJsZSB0aHJvdWdoIGl0" +
                    "cyBpbmRpY2VzLiBGb3IgZWFjaCB3ZWIgc2l0ZSByZWZsZWN0ZWQgaW" +
                    "4gR29vZ2xlJ3MgaW5kaWNlcywgaWYgZWl0aGVyIChpKSBhIHNpdGUg" +
                    "b3duZXIgcmVzdHJpY3RzIGFjY2VzcyB0byBoaXMgb3IgaGVyIHdlYi" +
                    "BzaXRlIG9yIChpaSkgYSBzaXRlIGlzIHRha2VuIGRvd24gZnJvbSB0" +
                    "aGUgd2ViLCB0aGVuLCB1cG9uIHJlY2VpcHQgb2YgYSByZXF1ZXN0IG" +
                    "J5IHRoZSBzaXRlIG93bmVyIG9yIGEgdGhpcmQgcGFydHkgaW4gdGhl" +
                    "IHNlY29uZCBpbnN0YW5jZSwgR29vZ2xlIHdvdWxkIGNvbnNpZGVyIG" +
                    "9uIGEgY2FzZS1ieS1jYXNlIGJhc2lzIHJlcXVlc3RzIHRvIHJlbW92" +
                    "ZSB0aGUgbGluayB0byB0aGF0IHNpdGUgZnJvbSBpdHMgaW5kaWNlcy" +
                    "4gSG93ZXZlciwgaWYgdGhlIG9wZXJhdG9yIG9mIHRoZSBzaXRlIGRv" +
                    "ZXMgbm90IHRha2Ugc3RlcHMgdG8gcHJldmVudCBpdCwgdGhlIGF1dG" +
                    "9tYXRpYyBmYWNpbGl0aWVzIHVzZWQgdG8gY3JlYXRlIHRoZSBpbmRp" +
                    "Y2VzIGFyZSBsaWtlbHkgdG8gZmluZCB0aGF0IHNpdGUgYW5kIGluZG" +
                    "V4IGl0IGFnYWluIGluIGEgcmVsYXRpdmVseSBzaG9ydCBhbW91bnQg" +
                    "b2YgdGltZS4=";

  /* test read() interface */
  public void testRead1() throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
    Base64FilterInputStream is = new Base64FilterInputStream(bais);
    byte[] resultBytes = new byte[expect.length()];

    int val;
    int index = 0;
    while (-1 != (val = is.read())) {
      resultBytes[index] = (byte) val;
      index++;
    }
    Assert.assertTrue(expect.equals(new String(resultBytes)));
  }

  /* test read(byte[]) interface */
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
    byteArrayRead(expect.length());
    byteArrayRead(((expect.length() + 3)/4)*4);
    byteArrayRead(expect.length() - 1);
    byteArrayRead(2048);
  }

  /* test read(byte[], off, len) interface */
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
    byteArrayRead2(expect.length());
    byteArrayRead2(((expect.length() + 3)/4)*4);
    byteArrayRead2(expect.length() - 1);
    byteArrayRead2(2048);
  }

  /* test read(byte[], off, len) interface where off != 0 */
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
    byteArrayRead3(expect.length());
    byteArrayRead3(((expect.length() + 3)/4)*4);
    byteArrayRead3(expect.length() - 1);
    byteArrayRead3(2048);
  }

  /* test use of mixed read() and read(byte[], off, len) interface */
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
    byteArrayRead4(expect.length(), 1);
    byteArrayRead4(((expect.length() + 3)/4)*4, 1);
    byteArrayRead4(expect.length() - 1, 1);
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
    byteArrayRead4(expect.length(), 2);
    byteArrayRead4(((expect.length() + 3)/4)*4, 2);
    byteArrayRead4(expect.length() - 1, 2);
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
    byteArrayRead4(expect.length(), 3);
    byteArrayRead4(((expect.length() + 3)/4)*4, 3);
    byteArrayRead4(expect.length() - 1, 3);
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
    byteArrayRead4(expect.length(), 4);
    byteArrayRead4(((expect.length() + 3)/4)*4, 4);
    byteArrayRead4(expect.length() - 1, 4);
    byteArrayRead4(2048, 4);
  }


  /* test read(byte[]) interface */
  public void byteArrayRead(int buffsize) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
    Base64FilterInputStream is = new Base64FilterInputStream(bais);
    byte[] resultBytes = new byte[buffsize];
    StringBuffer resultBuffer = new StringBuffer(expect.length());

    int val;
    while (-1 != (val = is.read(resultBytes))) {
      resultBuffer.append(new String(resultBytes, 0, val));
    }
    Assert.assertTrue(expect.equals(resultBuffer.toString()));
  }

  /* test read(byte[], off, len) interface */
  public void byteArrayRead2(int buffsize) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
    Base64FilterInputStream is = new Base64FilterInputStream(bais);
    byte[] resultBytes = new byte[buffsize];
    StringBuffer resultBuffer = new StringBuffer(expect.length());

    int val;
    while (-1 != (val = is.read(resultBytes, 0, buffsize))) {
      resultBuffer.append(new String(resultBytes, 0, val));
    }
    Assert.assertTrue(expect.equals(resultBuffer.toString()));
  }

  /* test read(byte[], off, len) interface, where off != 0 */
  public void byteArrayRead3(int buffsize) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
    Base64FilterInputStream is = new Base64FilterInputStream(bais);
    byte[] resultBytes = new byte[buffsize + 3];
    StringBuffer resultBuffer = new StringBuffer(expect.length());

    int val;
    while (-1 != (val = is.read(resultBytes, 3, buffsize))) {
      resultBuffer.append(new String(resultBytes, 3, val));
    }
    Assert.assertTrue(expect.equals(resultBuffer.toString()));
  }

  /* test use of mixed read() and read(byte[], off, len) interface */
  public void byteArrayRead4(int buffsize, int readsize) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
    Base64FilterInputStream is = new Base64FilterInputStream(bais);
    byte[] resultBytes = new byte[buffsize];
    StringBuffer resultBuffer = new StringBuffer(expect.length());

    int val;
    while ((readsize-- > 0) && (-1 != (val = is.read()))) {
      resultBuffer.append((char) val);
    }

    while (-1 != (val = is.read(resultBytes, 0, buffsize))) {
      resultBuffer.append(new String(resultBytes, 0, val));
    }
    Assert.assertTrue(expect.equals(resultBuffer.toString()));
  }
}
