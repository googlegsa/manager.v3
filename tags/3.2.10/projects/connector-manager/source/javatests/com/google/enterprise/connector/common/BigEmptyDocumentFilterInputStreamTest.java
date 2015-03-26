// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.common;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Tests BigEmptyDocumentFilterInputStream.
 */
public class BigEmptyDocumentFilterInputStreamTest extends TestCase {

  /** Test null source InputStream. */
  public void testNullSourceInputStream() throws Exception {
    InputStream is = new BigEmptyDocumentFilterInputStream(null, 1000);
    try {
      is.read();
      fail("Expected EmptyDocumentException");
    } catch (EmptyDocumentException expected) {
      // Expected.
    } finally {
      is.close();
    }

    is = new BigEmptyDocumentFilterInputStream(null, 1000);
    try {
      byte[] buffer = new byte[100];
      is.read(buffer);
      fail("Expected EmptyDocumentException");
    } catch (EmptyDocumentException expected) {
      // Expected.
    } finally {
      is.close();
    }
  }

  /** Test empty source InputStream. */
  public void testEmptySourceInputStream() throws Exception {
    checkEmptySourceInputStream(1000);
    checkEmptySourceInputStream(Long.MAX_VALUE);
  }

  /** Test empty source InputStream. */
  private void checkEmptySourceInputStream(long maxSize) throws Exception {
    InputStream is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("".getBytes()), maxSize);
    try {
      is.read();
      fail("Expected EmptyDocumentException");
    } catch (EmptyDocumentException expected) {
      // Expected.
    } finally {
      is.close();
    }

    is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("".getBytes()), maxSize);
    try {
      byte[] buffer = new byte[100];
      is.read(buffer);
      fail("Expected EmptyDocumentException");
    } catch (EmptyDocumentException expected) {
      // Expected.
    } finally {
      is.close();
    }
  }

  /** Test not-empty but not too big source InputStream. */
  public void testNonEmptySourceInputStream() throws Exception {
    checkNonEmptySourceInputStream(1000);
    checkNonEmptySourceInputStream(Long.MAX_VALUE);
  }

  /** Test not-empty but not too big source InputStream. */
  private void checkNonEmptySourceInputStream(long maxSize) throws Exception {
    InputStream is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("abc".getBytes()), maxSize);
    int ch = is.read();
    assertEquals('a', ch);
    ch = is.read();
    assertEquals('b', ch);
    ch = is.read();
    assertEquals('c', ch);
    ch = is.read();
    assertEquals(-1, ch);
    is.close();

    is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("abcde".getBytes()), maxSize);
    byte[] buffer = new byte[3];
    int bytesRead = is.read(buffer);
    assertEquals(3, bytesRead);
    assertEquals("abc", new String(buffer, 0, bytesRead));
    bytesRead = is.read(buffer);
    assertEquals(2, bytesRead);
    assertEquals("de", new String(buffer, 0, bytesRead));
    bytesRead = is.read(buffer);
    assertEquals(1, -bytesRead);
    is.close();
  }

  /** Test read() on too big source InputStream. */
  public void testBigSourceInputStreamRead() throws Exception {
    InputStream is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("abcde".getBytes()), 3);
    int ch = is.read();
    assertEquals('a', ch);
    ch = is.read();
    assertEquals('b', ch);
    ch = is.read();
    assertEquals('c', ch);
    try {
      ch = is.read();
      fail("Expected BigDocumentException");
    } catch (BigDocumentException expected) {
      // Expected.
    } finally {
      is.close();
    }
  }

  /** Test read(b, off, len) on too big source InputStream. */
  public void testBigSourceInputStreamReadBuff1() throws Exception {
    InputStream is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("abc".getBytes()), 10);
    byte[] buffer = new byte[10];
    int bytesRead = is.read(buffer);
    assertEquals(3, bytesRead);
    assertEquals("abc", new String(buffer, 0, bytesRead));
    bytesRead = is.read(buffer);
    assertEquals(-1, bytesRead);
  }

  /** Test read(b, off, len) on too big source InputStream. */
  public void testBigSourceInputStreamReadBuff2() throws Exception {
    InputStream is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("abc".getBytes()), 3);
    byte[] buffer = new byte[3];
    int bytesRead = is.read(buffer);
    assertEquals(3, bytesRead);
    bytesRead = is.read(buffer);
    assertEquals(-1, bytesRead);
  }

  /** Test read(b, off, len) on too big source InputStream. */
  public void testBigSourceInputStreamReadBuff3() throws Exception {
    InputStream is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("abcde".getBytes()), 3);
    try {
      byte[] buffer = new byte[3];
      int bytesRead = is.read(buffer);
      assertEquals(3, bytesRead);
      assertEquals("abc", new String(buffer, 0, bytesRead));
      bytesRead = is.read(buffer);
      fail("Expected BigDocumentException");
    } catch (BigDocumentException expected) {
      // Expected.
    } finally {
      is.close();
    }
  }

  /** Test read(b, off, len) on too big source InputStream. */
  public void testBigSourceInputStreamReadBuff4() throws Exception {
    InputStream is = new BigEmptyDocumentFilterInputStream(
        new ByteArrayInputStream("abcde".getBytes()), 3);
    try {
      byte[] buffer = new byte[10];
      int bytesRead = is.read(buffer);
      fail("Expected BigDocumentException");
    } catch (BigDocumentException expected) {
      // Expected.
    } finally {
      is.close();
    }
  }
}
