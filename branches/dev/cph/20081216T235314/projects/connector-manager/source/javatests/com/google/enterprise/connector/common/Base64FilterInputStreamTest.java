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
}
