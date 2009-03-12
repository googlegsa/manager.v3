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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class StringUtils {

  private StringUtils() {
    // prevents instantiation
  }

  /**
   * Find a named file on the classpath then read the entire content as a
   * String, skipping comment lines (lines that begin with #) and end-line
   * comments (from the first occurrence of // to the end).
   *
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
    InputStream s = StringUtils.class.getResourceAsStream(filename);
    if (s == null) {
      throw new IllegalArgumentException(
          "filename must be found on the classpath: " + filename);
    }
    InputStreamReader isr = new InputStreamReader(s);
    BufferedReader br = new BufferedReader(isr);
    return streamToString(br);
  }

  /**
   * Read a buffered reader and return the entire contents as a String,
   * skipping comment lines (lines that begin with #) and end-line comments
   * (from the first occurrence of // to the end).
   *
   * @param br  An Buffered Reader ready for reading
   * @return The contents of the reader (skipping comments)
   */
  public static String streamToString(BufferedReader br) {
    StringBuilder buf = new StringBuilder(1024);
    String line;
    try {
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#")) {
          // skip comment lines
          continue;
        }
        int index = line.indexOf("//");
        if (index == -1) {
          buf.append(line);
        } else {
          buf.append(line.subSequence(0, index));
        }
        buf.append('\n');
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return buf.toString();
  }

  /**
   * Read an entire InputStream (as UTF-8) and return its contents as a String
   *
   * @param is InputStream to read
   * @return contents as a String
   */
  public static String streamToString(InputStream is) {
    try {
      return streamToStringAndThrow(is);
    } catch (UnsupportedEncodingException e) {
      // TODO: this is ungraceful - need to plan for recovery
      throw new RuntimeException("Unsupported Encoding.");
    } catch (IOException e) {
      // TODO: this is ungraceful - need to plan for recovery
      throw new RuntimeException("I/O Problem.");
    }
  }

  /**
   * Read an entire InputStream (as UTF-8) and return its contents as a String
   *
   * @param is InputStream to read
   * @return contents as a String
   * @throws IOException
   */
  public static String streamToStringAndThrow(InputStream is)
      throws IOException {
    int bytesLen = 32768;
    byte[] bytes = new byte[bytesLen];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;
    try {
      while (offset < bytes.length
             && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        offset += numRead;
        // If we have reach the limit, we want to increase the size to get the
        // entire String.
        if (bytes.length == offset) {
          byte[] temp = bytes;
          bytesLen *= 2;
          bytes = new byte[bytesLen];
          System.arraycopy(temp, 0, bytes, 0, temp.length);
        }
      }
    } finally {
      is.close();
    }

    return new String(bytes, 0, offset, "UTF-8");
  }

  /**
   * Reads all from a Reader into a String. Close the Reader when finished.
   *
   * @param reader Reader
   * @return the String
   * @throws IOException
   */
  public static String readAllToString(Reader reader) throws IOException {
    char buf[] = new char[4096];
    StringBuilder strBuffer = new StringBuilder();
    int size = 0;
    try {
      while ((size = reader.read(buf)) != -1) {
        strBuffer.append(buf, 0, size);
      }
    } finally {
      reader.close();
    }
    return strBuffer.toString();
  }

  /**
   * Normalizes strings with \r\n newlines to just \n.
   *
   * @param input String to normalize
   * @return the normalized result
   */
  public static String normalizeNewlines(String input) {
    return input.replaceAll("\r\n", "\n");
  }
}
