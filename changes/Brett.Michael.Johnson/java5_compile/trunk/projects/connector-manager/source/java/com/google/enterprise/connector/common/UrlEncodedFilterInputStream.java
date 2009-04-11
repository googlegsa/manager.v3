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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * URL encodes input stream.
 */
public class UrlEncodedFilterInputStream extends FilterInputStream {

  /**
   * Given some InputStream, create an InputStream that URL encodes the
   * input stream.
   * @param in
   */
  public UrlEncodedFilterInputStream(InputStream in) {
    super(in);
  }

  private int rawBufSize = 4 * 1024;
  private byte rawBuf[] = new byte[rawBufSize];
  private byte encodedBuf[];

  /*
   * Position of next int to read.
   */
  private int encodedBufPos = 0;
  private int encodedBufEndPos = 0;  // position with no valid data

  private byte[] readBuffer = new byte[1];

  @Override
  public int read() throws IOException {
    int retVal = read(readBuffer, 0, 1);
    if (-1 == retVal) {
      return -1;
    } else {
      return readBuffer[0];
    }
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    if (len < 0) {
      return 0;
    }

    int currOff = off;  // current position to write into b
    int currLen = len;  // num bytes to write into b

    while (true) {
      // fulfill read based on already encoded bytes
      if (encodedBufEndPos - encodedBufPos > 0) {
        int numBytesToCopy =
          Math.min(currLen, encodedBufEndPos - encodedBufPos);
        System.arraycopy(encodedBuf, encodedBufPos, b, currOff, numBytesToCopy);
        encodedBufPos += numBytesToCopy;
        currLen -= numBytesToCopy;
        currOff += numBytesToCopy;
      }

      // if already done fulfilling entire read request, return
      if (currLen <= 0) {
        return len;
      }

      // try reading more data
      int bytesRead = -1;
      try {
        bytesRead = in.read(rawBuf, 0, rawBuf.length);
      } catch (IOException e) {
        // if we've read any bytes, we return that number
        if (currLen < len) {
          return len - currLen;
        } else {
          throw e;
        }
      }

      if (-1 == bytesRead) {
        if (currLen < len) {
          return len - currLen;
        } else {
          return -1;
        }
      }

      // encode data
      String rawBufStr = new String(rawBuf, 0, bytesRead, "UTF-8");
      String encodedBufStr = URLEncoder.encode(rawBufStr, "UTF-8");
      encodedBuf = encodedBufStr.getBytes("UTF-8");
      encodedBufPos = 0;
      encodedBufEndPos = encodedBuf.length;
    }
  }
}
