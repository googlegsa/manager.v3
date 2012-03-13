// Copyright 2009 Google Inc.
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

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for a factory for an {@link InputStream} to read a document.
 * Passing an {@link InputStreamFactory} is beneficial if the recipient
 * may wish to delay opening an {@code InputStream} (if at all), or if the
 * recipient needs to open an  {@code InputStream} several times.
 */
public interface InputStreamFactory {
  /**
   * Returns an {@linkplain InputStream}. Each call returns a new fresh one
   * that is set to the beginning of the document.
   * <p/>
   * The caller is responsible for closing the returned {@link InputStream}.
   *
   * @throws IOException
   */
  public InputStream getInputStream() throws IOException;
}
