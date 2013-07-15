// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

public class UrlEncodedFilterInputStreamTest extends TestCase {
  public void testRead() throws IOException {
    final String bytesStr = "abc<>&= ";
    final String expectedEncodedBytesStr = "abc%3C%3E%26%3D+";
    byte[] bytes = bytesStr.getBytes("UTF-8");
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    UrlEncodedFilterInputStream is = new UrlEncodedFilterInputStream(bais);
    int val;
    byte[] expectedBytes = bytesStr.getBytes("UTF-8");
    byte[] resultBytes = new byte[3*expectedBytes.length];
    int index = 0;
    while (-1 != (val = is.read())) {
      resultBytes[index] = (byte) val;
      index++;
    }
    String encodedStr = new String(resultBytes, 0, index, "UTF-8");
    Assert.assertEquals(expectedEncodedBytesStr, encodedStr);
  }

  public void testReadArray() throws IOException {
    final String bytesStr = "abc<>&= ";
    final String expectedEncodedBytesStr = "abc%3C%3E%26%3D+";
    byte[] bytes = bytesStr.getBytes("UTF-8");
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    UrlEncodedFilterInputStream is = new UrlEncodedFilterInputStream(bais);
    byte[] resultBytes = new byte[expectedEncodedBytesStr.length()];
    int bytesRead = is.read(resultBytes, 0, resultBytes.length);
    String encodedStr = new String(resultBytes, 0, bytesRead, "UTF-8");
    Assert.assertEquals(expectedEncodedBytesStr.length(), bytesRead);
    Assert.assertEquals(expectedEncodedBytesStr, encodedStr);
  }
}
