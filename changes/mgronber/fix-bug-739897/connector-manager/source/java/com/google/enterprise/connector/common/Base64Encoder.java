// Copyright (C) 2006 Google Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

/**
 * Base64 encoding (RFC1521)
 *
 */
public class Base64Encoder {

  private Base64Encoder() { }

  // Base64 uses 64 characters in this map, and '=' for padding
  private final static char charMap[] = {
    'A','B','C','D','E','F','G','H', // 0
    'I','J','K','L','M','N','O','P', // 1
    'Q','R','S','T','U','V','W','X', // 2
    'Y','Z','a','b','c','d','e','f', // 3
    'g','h','i','j','k','l','m','n', // 4
    'o','p','q','r','s','t','u','v', // 5
    'w','x','y','z','0','1','2','3', // 6
    '4','5','6','7','8','9','+','/'  // 7
  };

  /**
   * Write out a buffer using Base64 encoding.
   *
   * @param data the character buffer
   * @param out output writer
   * @return the number of bytes processed
   * @throws IOException 
   */
  public static int encode(byte data[], Writer out)
    throws IOException {
    return encode(data, 0, data.length, out);
  }

  /**
   * Encoding algorithm. 
   * @param data input data array
   * @param off offset into data array
   * @param len length to encode
   * @param outBuf output character array, outBuf.length >= ceil(len/3)*4
   */
  private static int encodeImpl(byte data[], int off, int len, char outBuf[])
    throws IOException {
    int outLen = 0;
    byte a, b, c;
    while (len >= 3) {
      a = data[off];
      b = data[off+1];
      c = data[off+2];
      outBuf[outLen++] = charMap[(a >>> 2) & 0x3F];
      outBuf[outLen++] = charMap[((a << 4) & 0x30) + 
                                         ((b >>> 4) & 0xf)];
      outBuf[outLen++] = charMap[((b << 2) & 0x3c) + 
                                         ((c >>> 6) & 0x3)];
      outBuf[outLen++] = charMap[c & 0x3F];
      len -= 3;
      off += 3;
    }

    if (len == 1) {
      a = data[off];
      b = 0;
      c = 0;
      outBuf[outLen++] = charMap[(a >>> 2) & 0x3F];
      outBuf[outLen++] = charMap[((a << 4) & 0x30) + 
                                         ((b >>> 4) & 0xf)];
      outBuf[outLen++] = '=';
      outBuf[outLen++] = '=';
      off++;
    } else if (len == 2) {
      a = data[off];
      b = data[off+1];
      c = 0;
      outBuf[outLen++] = charMap[(a >>> 2) & 0x3F];
      outBuf[outLen++] = charMap[((a << 4) & 0x30) + 
                                         ((b >>> 4) & 0xf)];
      outBuf[outLen++] = charMap[((b << 2) & 0x3c) + 
                                         ((c >>> 6) & 0x3)];
      outBuf[outLen++] = '=';
      off+=2;
    }

    return outLen;
  }

  /**
   * Write out a buffer using Base64 encoding.
   *
   * WARNING: This method will not flush the Writer 'out' (second argument). 
   * You'll have to do this yourself, or lose some of your precious data. 
   *
   * @param data the character buffer
   * @param off the starting offset in data
   * @param len length of data in data
   * @param out output writer
   * @return the number of bytes processed
   * @throws IOException 
   */
  public static int encode(byte data[], int off, int len, Writer out)
    throws IOException {
    char[] outBuf = new char[(data.length + 2 / 3) * 4]; // ceil(length/3)*4
    int rtn = encodeImpl(data, off, len, outBuf);
    out.write(outBuf, 0, rtn);
    return len;
  }

  /**
   * Write out a whole {@link InputStream} using Base64 encoding.
   *
   * WARNING: This method will not flush the Writer 'out' (second argument). 
   * You'll have to do this yourself, or lose some of your precious data. 
   *
   * @param inStream the input data stream
   * @param out output writer
   * @param bufferSize size of buffer used for each encoding run
   * @return number of bytes processed
   * @throws IOException 
   */
  public static int encode(InputStream inStream, Writer out, int bufferSize)
    throws IOException {
    int len = 0;
    byte[] buffer = new byte[bufferSize];
    int run = inStream.read(buffer, 0, buffer.length);
    char[] outBuf = new char[(buffer.length + 2 / 3 ) * 4]; // ceil(length/3)*4
    while (run != -1) {
      int outLen = encodeImpl(buffer, 0, run, outBuf);
      out.write(outBuf, 0, outLen);
      len += run;
      run = inStream.read(buffer, 0, buffer.length);
    }
    return len;
  }

  /**
   * Write out a whole {@link InputStream} using Base64 encoding,
   * using default buffer size of 1024*3.
   *
   * WARNING: This method will not flush the Writer 'out' (second argument). 
   * You'll have to do this yourself, or lose some of your precious data. 
   *
   * @param inStream the input data stream
   * @param out output writer
   * @return number of bytes processed
   * @throws IOException 
   */
  public static int encode(InputStream inStream, Writer out)
    throws IOException {
    return encode(inStream, out, 1024*3);
  }
 
}
