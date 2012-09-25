// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
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
 * A {@code FilterInputStream} that protects against large documents and empty
 * documents.
 * If we have read more than {@link FileSizeLimitInfo.maxDocumentSize}
 * bytes from the input, or if we get EOF after reading zero bytes,
 * we throw a subclass of {@code IOException} that is used as a signal for
 * {@link AlternateContentFilterInputStream} to switch to alternate content.
 */
public class BigEmptyDocumentFilterInputStream extends FilterInputStream {
  private final long maxDocumentSize;
  private long currentDocumentSize;

  /**
   * @param in InputStream containing raw document content
   * @param maxDocumentSize maximum allowed size in bytes of data read from in
   */
  public BigEmptyDocumentFilterInputStream(InputStream in,
                                           long maxDocumentSize) {
    super(in);
    this.maxDocumentSize = maxDocumentSize;
    this.currentDocumentSize = 0;
  }

  @Override
  public int read() throws IOException {
    if (in == null) {
      throw new EmptyDocumentException();
    }
    int val = super.read();
    if (val == -1) {
      if (currentDocumentSize == 0) {
        throw new EmptyDocumentException();
      }
    } else if (++currentDocumentSize > maxDocumentSize) {
      throw new BigDocumentException();
    }
    return val;
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    if (in == null) {
      throw new EmptyDocumentException();
    }
    int bytesRead = super.read(b, off,
        (int) Math.min(len, maxDocumentSize - currentDocumentSize + 1));
    if (bytesRead == -1) {
      if (currentDocumentSize == 0) {
        throw new EmptyDocumentException();
      } else {
        return bytesRead;
      }
    } else if ((currentDocumentSize += bytesRead) > maxDocumentSize) {
      throw new BigDocumentException();
    }
    return bytesRead;
  }

  @Override
  public boolean markSupported() {
    return false;
  }

  @Override
  public void close() throws IOException {
    if (in != null) {
      super.close();
    }
  }
}

