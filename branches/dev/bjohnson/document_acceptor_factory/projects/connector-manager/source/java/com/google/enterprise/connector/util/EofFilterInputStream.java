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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This Filter protects against the poorly behaved Apache Commons IO
 * {@code AutoCloseInputStream}, which can close a stream out from underneath
 * us. When that happens, reads while at EOF throw {@code IOException} rather
 * than returning -1.  This filter avoids calling {@code read()} on the
 * source stream once it is known to be at End-Of-File.
 *
 * @since 2.8
 */
public class EofFilterInputStream extends FilterInputStream {
  private boolean atEOF;

  public EofFilterInputStream(InputStream in) {
    super(in);
    atEOF = false;
  }

  @Override
  public int read() throws IOException {
    if (atEOF) {
      return -1;
    }
    int rtn = super.read();
    if (rtn == -1) {
      atEOF = true;
    }
    return rtn;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (atEOF) {
      return -1;
    }
    int rtn = super.read(b, off, len);
    if (rtn == -1) {
      atEOF = true;
    }
    return rtn;
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    // Doesn't protect against someone setting the mark while at EOF,
    // but good enough for our purposes.
    atEOF = false;
  }

  @Override
  public void close() throws IOException {
    // Once we are explicitly closed, we no longer need to protect
    // against unwarranted IOExceptions.
    atEOF = false;
    super.close();
  }
}
