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
}
