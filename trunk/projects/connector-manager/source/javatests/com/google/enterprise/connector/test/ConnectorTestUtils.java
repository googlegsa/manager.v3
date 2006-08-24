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

package com.google.enterprise.connector.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConnectorTestUtils {
  
  private ConnectorTestUtils() {
    // prevents instantiation
  }

  /**
   * Find a named file on the classpath then read the entire content as a 
   * String, skipping
   * comment lines (lines that begin with #) and end-line comments 
   * (from the first occurrence of // to the end). 
   * @param filename The name of file on the classpath
   * @return The contents of the reader (skipping comments)
   */
  public static String streamToString(String filename) {
    if (filename == null) {
      throw new IllegalArgumentException("filename is null");
    }
    if (filename.length() < 1) {
      throw new IllegalArgumentException("filename is empty");
    }
    InputStream s = ConnectorTestUtils.class.getResourceAsStream(filename);
    if (s == null) {
      throw new IllegalArgumentException(
        "filename must be found on the classpath: " + filename);
    }
    InputStreamReader isr = new InputStreamReader(s);
    BufferedReader br = new BufferedReader(isr);
    return streamToString(br);
  }
  /**
   * Read a buffered reader and return the entire contents as a String, skipping
   * comment lines (lines that begin with #) and end-line comments 
   * (from the first occurrence of // to the end). 
   * @param br  An Buffered Reader ready for reading
   * @return The contents of the reader (skipping comments)
   */
  public static String streamToString(BufferedReader br) {
    StringBuffer b = new StringBuffer(1024);
    String line;
    try {
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#")) {
          // skip comment lines
          continue;
        }
        int index = line.indexOf("//");
        if (index == -1) {
          b.append(line);          
        } else {
          b.append(line.subSequence(0, index));
        }
        b.append('\n');        
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new String(b);
  }
  
  public static String streamToString(InputStream is) {
    byte buf[] = new byte[2048];
    int bytesRead;
    try {
      bytesRead = is.read(buf);
    } catch (IOException e) {
      throw new IllegalArgumentException("I/O problem reading stream");
    }
    String res = new String(buf,0,bytesRead);
    return res;
  }
  
}
