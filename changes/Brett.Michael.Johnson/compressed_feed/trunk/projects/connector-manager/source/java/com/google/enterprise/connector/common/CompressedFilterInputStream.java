// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;

/**
 * Compresses an input stream using java.util.zip.Deflater.
 */
public class CompressedFilterInputStream extends FilterInputStream {
  private Deflater deflater;

  // Approximately equal to the size of the buffer in GsaFeedConnection.
  private byte[] inputBuff = new byte[32768];
  private byte[] oneByte = new byte[1];

  /**
   * Given some InputStream, create an InputStream that compresses the
   * input stream using java.util.zip.Deflate.
   *
   * @param in an InputStream providing source data for compressing.
   */
  public CompressedFilterInputStream(InputStream in) {
    super(in);
    deflater = new Deflater();
  }

  // Supported, but shouldn't really happen in our environment.
  @Override
  public int read() throws IOException {
    int rtn = read(oneByte, 0, 1);
    return (rtn < 0) ? rtn : (oneByte[0] & 0xFF);
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    int rtn = 0;
    do {
      // If the compressor needs more input, get some.
      if (deflater.needsInput()) {
        int bytesRead = fillbuff(inputBuff);
        if (bytesRead < 0) {
          if (deflater.finished()) {
            return bytesRead;
          } else {
            deflater.finish();
          }
        } else {
          deflater.setInput(inputBuff, 0, bytesRead);
        }
      }
      // Write compressed data to the output.
      rtn = deflater.deflate(b, off, len);
    } while (rtn == 0);
    return rtn;
  }

  /**
   * Try to fill up the buffer with data read from the input stream.
   * This is tolerant of short reads - returning less than the requested
   * amount of data, even if there is more available.
   *
   * @param b buffer to fill
   */
  private int fillbuff(byte b[]) throws IOException {
    int bytesRead = 0;
    int off = 0;
    int len = b.length;
    while (bytesRead < len) {
      int val = in.read(b, off + bytesRead, len - bytesRead);
      if (val == -1) {
        return (bytesRead > 0) ? bytesRead : val;
      }
      bytesRead += val;
    }
    return bytesRead;
  }

  @Override
  public void close() throws IOException {
    super.close();
    deflater.end();
  }

  // No support for mark() or reset().
  @Override
  public boolean markSupported() {
    return false;
  }

  // No support for skip().
  @Override
  public long skip(long n) {
    return 0L;
  }
}
