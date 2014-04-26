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
 * Compatibility utility for creating an {@link IOException} with a cause.
 * This was needed because in Java 5 {@code IOException} does not provide
 * a constructor that takes a {@code cause} parameter.
 *
 * @since 2.8
 * @deprecated Use {@link IOException#IOException(String, Throwable)}
 */
@Deprecated
public class IOExceptionHelper {
  // Prevents instantiation.
  private IOExceptionHelper() {
  }

  /**
   * Makes a new {@code IOException} with the supplied {@code message} and
   * {@code cause} in a manner that was supported by Java 5.
   *
   * @param message the message
   * @param cause root failure cause
   * @deprecated Use {@link IOException#IOException(String, Throwable)}
   */
  @Deprecated
  public static IOException newIOException(String message, Throwable cause) {
    return new IOException(message, cause);
  }
}
