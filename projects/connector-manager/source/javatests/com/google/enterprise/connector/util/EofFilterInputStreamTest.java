// Copyright 2010 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.util.EofFilterInputStream;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Test that EofFilterInputStream protects against the
 * poorly behaved Apache Commons IO AutoCloseInputStream.
 * Regression test for Issue 212.
 */
public class EofFilterInputStreamTest extends TestCase {
  private static final String errorMsg = "Access closed stream.";

  private byte[] bytes = new byte[]{ 'a', 'b', 'c'};
  private byte[] buffer = new byte[1024];

  /**
   * This tests the expected behavior when reading while at EOF
   * on a traditional stream.
   */
  public void testTraditionalEOF() throws IOException {
    InputStream in =
        new ClosableInputStream(new ByteArrayInputStream(bytes));

    int rtn = in.read(buffer);
    assertEquals(3, rtn);

    // We should keep hitting EOF now.
    rtn = in.read(buffer);
    assertEquals(-1, rtn);

    rtn = in.read(buffer, 0, buffer.length);
    assertEquals(-1, rtn);

    rtn = in.read();
    assertEquals(-1, rtn);

    rtn = in.read(buffer);
    assertEquals(-1, rtn);

    rtn = in.read(buffer, 0, buffer.length);
    assertEquals(-1, rtn);

    rtn = in.read();
    assertEquals(-1, rtn);

    // Now rewind to the beginning and try again.
    in.reset();

    rtn = in.read(buffer);
    assertEquals(3, rtn);

    rtn = in.read(buffer);
    assertEquals(-1, rtn);

    // Now close the stream.  Further access should be denied.
    in.close();
    try {
      rtn = in.read(buffer);
      fail("IOException was not thrown on access to closed stream.");
    } catch (IOException ioe) {
      assertEquals(errorMsg, ioe.getMessage());
    }
  }

  /**
   * This tests the bad behavior of AutoCloseInputStream, which doesn't
   * allow repeated reads while at EOF.
   */
  public void testAutoCloseEOF() throws IOException {
    InputStream in = new AutoCloseInputStream(
        new ClosableInputStream(new ByteArrayInputStream(bytes)));

    int rtn = in.read(buffer);
    assertEquals(3, rtn);

    // We should hit EOF now.
    rtn = in.read(buffer);
    assertEquals(-1, rtn);

    // But a second attempt will throw an IOException. Bad, Bad InputStream!
    try {
      rtn = in.read(buffer);
      fail("IOException was not thrown on access to autoclosed stream.");
    } catch (IOException ioe) {
      assertEquals(errorMsg, ioe.getMessage());
    }

    in.close();
  }

  /**
   * This tests that the EofFilterInputStream restores the traditional
   * expected behavior when reading while at EOF on an AutoClosed stream.
   */
  public void testAutoCloseProtection() throws IOException {
    InputStream in =
        new EofFilterInputStream(new AutoCloseInputStream(
            new ClosableInputStream(new ByteArrayInputStream(bytes))));

    int rtn = in.read(buffer);
    assertEquals(3, rtn);

    // We should keep hitting EOF now.
    rtn = in.read(buffer);
    assertEquals(-1, rtn);

    rtn = in.read(buffer, 0, buffer.length);
    assertEquals(-1, rtn);

    rtn = in.read();
    assertEquals(-1, rtn);

    rtn = in.read(buffer);
    assertEquals(-1, rtn);

    rtn = in.read(buffer, 0, buffer.length);
    assertEquals(-1, rtn);

    rtn = in.read();
    assertEquals(-1, rtn);

    // Now explicitly close the stream.  Further access should be denied.
    in.close();
    try {
      rtn = in.read(buffer);
      fail("IOException was not thrown on access to closed stream.");
    } catch (IOException ioe) {
      assertEquals(errorMsg, ioe.getMessage());
    }
  }

  /**
   * EofFilterInputStream can't protect againt mark()/reset() on an
   * AutoClosed stream, because the underlying resource is no longer available.
   */
  public void testAutoCloseProtection2() throws IOException {
    InputStream in =
        new EofFilterInputStream(new AutoCloseInputStream(
            new ClosableInputStream(new ByteArrayInputStream(bytes))));

    int rtn = in.read(buffer);
    assertEquals(3, rtn);

    // We should keep hitting EOF now.
    rtn = in.read(buffer);
    assertEquals(-1, rtn);

    rtn = in.read(buffer);
    assertEquals(-1, rtn);

    // Attempts to rewind should fail because the shielded resource is
    // actually no longer available.
    try {
      in.reset();
      fail("IOException was not thrown on reset of closed stream.");
    } catch (IOException ioe) {
      assertEquals(errorMsg, ioe.getMessage());
    }
  }

  /**
   * An InputStream that can be closed, and once closed throws IOExceptions
   * when accessed. Hides the fact that ByteArrayInputStream.close() is a NOOP.
   */
  private class ClosableInputStream extends FilterInputStream {
    boolean isClosed;
    public ClosableInputStream(InputStream in) {
      super(in);
      isClosed = false;
    }

    @Override
    public int read() throws IOException {
      if (isClosed) {
        throw new IOException(errorMsg);
      }
      return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
      return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (isClosed) {
        throw new IOException(errorMsg);
      }
      return super.read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
      if (isClosed) {
        throw new IOException(errorMsg);
      }
      super.reset();
    }

    @Override
    public void close() throws IOException {
      isClosed = true;
      super.close();
    }
  }

  /**
   * An InputStream that automatically closes upon hitting EOF,
   * like the poorly behaved Apache Commons IO AutoCloseInputStream.
   */
  private class AutoCloseInputStream extends FilterInputStream {
    public AutoCloseInputStream(InputStream in) {
      super(in);
    }

    @Override
    public int read() throws IOException {
      int rtn = super.read();
      if (-1 == rtn) {
        close();
      }
      return rtn;
    }

    @Override
    public int read(byte[] b) throws IOException {
      return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      int rtn = super.read(b, off, len);
      if (-1 == rtn) {
        close();
      }
      return rtn;
    }
  }
}
