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
