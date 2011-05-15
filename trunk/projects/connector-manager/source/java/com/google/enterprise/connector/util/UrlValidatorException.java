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

/**
 * Represents an invalid HTTP response during URL validation.
 *
 * @since 2.6.6
 */
public class UrlValidatorException extends Exception {
  /** The HTTP status code. */
  private final int statusCode;

  /**
   * Constructs an exception with a status code and optional message.
   *
   * @param statusCode the HTTP status code
   * @param message the HTTP response message, which may be @{code null}
   */
  public UrlValidatorException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  /**
   * Gets the HTTP status code.
   *
   * @return the HTTP status code
   */
  public int getStatusCode() {
    return statusCode;
  }
}
