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

/**
 * Base64 encodes an input stream.
 */
public class Base64FilterInputStream extends FilterInputStream {

  /* NOTE: Since output line position is not maintained across calls to
   * read(...), the actual length of lines will approximate this.
   * For our purposes, strict adherence to RFC 2045 is not necessary.
   * We only want to make viewing teedFeedFiles easier.
   */
  static final int BASE64_LINE_LENGTH = 76;
  private boolean breakLines = false;


  /**
   * Given some InputStream, create an InputStream that base64 encodes the
   * input stream.  No line breaks are included in the output stream.
   *
   * @param in an InputStream providing source data for encoding
   */
  public Base64FilterInputStream(InputStream in) {
    this (in, false);
  }

  /**
   * Given some InputStream, create an InputStream that base64 encodes the
   * input stream.
   *
   * @param in an InputStream providing source data for encoding
   * @param breakLines if true, add line breaks
   */
  public Base64FilterInputStream(InputStream in, boolean breakLines) {
    super(in);
    this.breakLines = breakLines;
  }

  /* This is used when reading small amounts of data. */
  private byte[] inputBuffer = new byte[3];
  private byte[] encodedBuffer = new byte[4];
  private int encodedBufPos = 4;  // Position of next byte to read.

  public int read() throws IOException {
    if (encodedBufPos >= 4) {
      int retVal = fillbuff(inputBuffer, 0, 3);
      if (-1 == retVal) {
        return -1;
      } else {
        Base64.encode3to4(inputBuffer, 0, retVal, encodedBuffer, 0,
                          Base64.ALPHABET);
        encodedBufPos = 0;
      }
    }
    return encodedBuffer[encodedBufPos++];
  }

  public int read(byte b[], int off, int len) throws IOException {
    // If there is some leftover morsel of encoded data, return that.
    if (len > 3 && encodedBufPos < 4) {
      len = 4 - encodedBufPos;
    }

    // Special case reads of less than one encoded quadbyte.
    int bytesWritten = 0;
    if (len < 4) {
      for (; bytesWritten < len; bytesWritten++) {
        int aByte = read();
        if (aByte == -1) {
          return (bytesWritten > 0) ? bytesWritten : -1;
        } else {
          b[off + bytesWritten] = (byte)aByte;
        }
      }
      return bytesWritten;
    }

    // Determine the number of threebyte datum we need to read to
    // fill the destination buffer with quadbyte encoded data.
    // If we are breaking lines, try to constrain the read size
    // so that it generates whole lines.
    int readLen;
    int lineLen;
    if (breakLines && len > BASE64_LINE_LENGTH) {
      readLen = (((len / (BASE64_LINE_LENGTH+1)) * BASE64_LINE_LENGTH) / 4) * 3;
      lineLen = BASE64_LINE_LENGTH;
    } else {
      readLen = (len / 4) * 3;
      lineLen = Integer.MAX_VALUE;
    }

    // Read the input data into the tail end of the target buffer.
    int readBytes = fillbuff(b, off + (len - readLen), readLen);
    if (readBytes == -1) {
      return -1;
    }

    // Convert the buffer in-place.
    bytesWritten = Base64.encode(b, off + (len - readLen), readBytes, b, off,
                                 Base64.ALPHABET, lineLen);
    return bytesWritten;
  }

  /**
   * Try to fill up the buffer with data read from the input stream.
   * This is tolerant of short reads - returning less than the requested
   * amount of data, even if there is more available.
   *
   * @param b buffer to fill
   * @param off offset into b to start filling
   * @param len number of bytes to read
   */
  private int fillbuff(byte b[], int off, int len) throws IOException {
    int bytesRead = 0;
    while (bytesRead < len) {
      int val = in.read(b, off + bytesRead, len - bytesRead);
      if (val == -1) {
        return (bytesRead > 0) ? bytesRead : -1;
      }
      bytesRead += val;
    }
    return bytesRead;
  }

  /**
   * We don't support mark() or reset().
   */
  public boolean markSupported() {
    return false;
  }

  /**
   * Return the number of bytes available to read.
   */
  public int available() throws IOException {
    int available = ((in.available() + 2) / 3) * 4;
    if (encodedBufPos < 4) {
      available += (4 - encodedBufPos);
    }
    if (breakLines) {
      available += available/BASE64_LINE_LENGTH;
    }
    return available;
  }

  /**
   * Skip over bytes in the input stream.
   *
   * @param n number of bytes to skip.
   * @return number of bytes skipped.
   */
  public long skip(long n) throws IOException {
    long skipped = 0;

    if (breakLines) {
      n -= n/BASE64_LINE_LENGTH;
    }

    // Skip over encoded morsel.
    while (encodedBufPos < 4 && n > 0) {
      encodedBufPos++;
      skipped++;
      n--;
    }
    // Skip over enough threebytes to cover the resulting encoded quadbytes.
    if (n > 0) {
      skipped += in.skip((n / 4) * 3);
    }
    return skipped;
  }
}
