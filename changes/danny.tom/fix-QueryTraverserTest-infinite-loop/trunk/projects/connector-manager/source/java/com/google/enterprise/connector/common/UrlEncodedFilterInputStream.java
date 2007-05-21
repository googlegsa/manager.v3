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
  
  public int read() throws IOException {
    // if we have processed all read data
    if (encodedBufEndPos == encodedBufPos) {
      // try reading more data
      int bytesRead = in.read(rawBuf, 0, rawBuf.length);
      if (-1 == bytesRead) {
        return -1;
      }
      
      String rawBufStr = new String(rawBuf, 0, bytesRead, "UTF-8");
      String encodedBufStr = URLEncoder.encode(rawBufStr, "UTF-8");
      encodedBuf = encodedBufStr.getBytes("UTF-8");
      encodedBufPos = 0;
      encodedBufEndPos = encodedBuf.length;
    }
    
    // INVARIANT: we have encoded data that can be returned
    int result = encodedBuf[encodedBufPos];
    encodedBufPos++;
    return result;
  }
  
  public int read(byte b[], int off, int len) throws IOException {
    if (len < 0) {
      return 0;
    }
    
    int bytesRead = 0;
    for (int i = 0; i < len; i++) {
      int val = read();
      if (-1 == val) {
        if (0 == i) {
          return -1;
        } else {
          return bytesRead;
        }
      } else {
        bytesRead++;
        // we read a legitimate value so add that to byte array
        b[off + i] = (byte) val;
      }
    }
    
    return bytesRead;
  }
}
