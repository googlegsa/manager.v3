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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * An {@link InputStreamFactory} that manufactures
 * URL {@link InputStream}.
 */
public class UrlInputStreamFactory implements InputStreamFactory {
  private final URL url;

  /**
   * {@link UrlInputStreamFactory} constructor.
   *
   * @param url a {@link URL} for which to generate an {@link InputStream}
   */
  public UrlInputStreamFactory(URL url) {
    this.url = url;
  }

  /* @Override */
  public InputStream getInputStream() throws IOException {
    return url.openStream();
  }
}
