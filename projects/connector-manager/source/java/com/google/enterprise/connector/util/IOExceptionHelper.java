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

/**
 * Compatibility Utility for creating an {@link IOException}.
 * with a cause. This is needed because in Java 5
 * IOException does not provide a constructor with a cause.
 */
public class IOExceptionHelper {
  // Prevents instantiation.
  private IOExceptionHelper() {
  }

  /**
   * Makes a new IOException with the supplied
   * message and cause in a manner that is supported by
   * java 5.
   */
  public static IOException newIOException(String msg, Throwable cause) {
    IOException result = new IOException(msg);
    result.initCause(cause);
    return result;
  }
}