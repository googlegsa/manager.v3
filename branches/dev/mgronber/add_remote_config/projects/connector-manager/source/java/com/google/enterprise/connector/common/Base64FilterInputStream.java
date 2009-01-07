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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Base64 encodes an input stream.
 */
public class Base64FilterInputStream extends FilterInputStream {

  /**
   * Given some InputStream, create an InputStream that base64 encodes the
   * input stream.
   * @param in
   */
  public Base64FilterInputStream(InputStream in) {
    super(in);
  }

  /*
   * Choose a raw buffer size that is divisible by three so we don't have any
   * leftover bytes.
   */
  private int rawBufSize = 3 * 1024;
  private byte rawBuf[] = new byte[rawBufSize];
  private byte encodedBuf[];

  private int rawBufEndPos = 0;  // position starting with no valid data

  /*
   * Position of next int to read.
   */
  private int encodedBufPos = 0;
  private int encodedBufEndPos = 0;  // position starting with no valid data

  private byte[] readBuffer = new byte[1];

  public int read() throws IOException {
    int retVal = read(readBuffer, 0, 1);
    if (-1 == retVal) {
      return -1;
    } else {
      return readBuffer[0];
    }
  }

  public int read(byte b[], int off, int len) throws IOException {
    if (len < 0) {
      return 0;
    }

    int currOff = off;  // current position to write into b
    int currLen = len;  // num bytes to write into b
    while (true) {
      int numLeftoverBytes = rawBufEndPos;
      int numEncodableBytes = 0;

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

      // if we reach here, it means we need more encoded bytes
      while (0 == numEncodableBytes) {
        int bytesRead = -1;
        try {
          bytesRead =
            in.read(rawBuf, rawBufEndPos, rawBuf.length - rawBufEndPos);
        } catch (IOException e) {
          // if we've read any bytes, we return that number
          if (currLen < len) {
            return len - currLen;
          } else {
            throw e;
          }
        }
        if (-1 == bytesRead) {
          if (0 == numLeftoverBytes) {
            if (currLen < len) {
              return len - currLen;
            } else {
              return -1;
            }
          } else {
            // if we can't read new bytes, but we had some from before, then
            // it is okay to encode those.
            numEncodableBytes += numLeftoverBytes;
            numLeftoverBytes = 0;
            break;
          }
        } else {
          rawBufEndPos += bytesRead;
        }

        // encode bytes in groups of three bytes
        numLeftoverBytes = rawBufEndPos % 3;
        numEncodableBytes = rawBufEndPos - numLeftoverBytes;
      }

      // encode the encodable bytes (i.e. all bytes except possibly one or two
      // extra bytes if it isn't a multiple of three)
      StringWriter writer = new StringWriter();
      Base64Encoder.encode(rawBuf, 0, numEncodableBytes, writer);
      encodedBuf = writer.toString().getBytes();
      encodedBufPos = 0;
      encodedBufEndPos = encodedBuf.length;
      System.arraycopy(rawBuf, numEncodableBytes, rawBuf, 0, numLeftoverBytes);
      rawBufEndPos = numLeftoverBytes;
    }
  }
}
