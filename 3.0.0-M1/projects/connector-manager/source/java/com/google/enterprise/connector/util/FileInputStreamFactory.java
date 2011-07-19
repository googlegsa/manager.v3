// Copyright 2011 Google Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStreamFactory} that manufactures
 * {@link FileInputStream FileInputStreams}.
 */
public class FileInputStreamFactory implements InputStreamFactory {
  private final File file;

  /**
   * {@link FileInputStreamFactory} constructor.
   *
   * @param file a {@link File} for which to generate an {@link InputStream}
   */
  public FileInputStreamFactory(File file) {
    this.file = file;
  }

  /**
   * {@link FileInputStreamFactory} constructor.
   *
   * @param filename the name of a file for which to generate an
   *        {@link InputStream}.
   */
  public FileInputStreamFactory(String filename) {
    this(new File(filename));
  }

  /* @Override */
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(file);
  }
}
