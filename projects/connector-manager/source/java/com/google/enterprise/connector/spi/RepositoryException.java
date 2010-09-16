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

package com.google.enterprise.connector.spi;

/**
 * Thrown for general repository errors, such as connectivity problems.
 */
public class RepositoryException extends Exception {
  /**
   * Root failure cause
   */
  protected Throwable rootCause;

  /**
   * Constructs a new RepositoryException with no message and no root cause.
   */
  public RepositoryException() {
    super();
  }

  /**
   * Constructs a RepositoryException with a supplied message but no root cause.
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *          method.
   */
  public RepositoryException(String message) {
    super(message);
  }

  /**
   * Constructs a RepositoryException with message and root cause.
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *          method.
   * @param rootCause root failure cause
   */
  public RepositoryException(String message, Throwable rootCause) {
    super(message);
    this.rootCause = rootCause;
  }

  /**
   * Constructs a RepositoryException with the specified root cause.
   * @param rootCause root failure cause
   */
  public RepositoryException(Throwable rootCause) {
    super();
    this.rootCause = rootCause;
  }

  /**
   * Returns the detail message, including messages from nested exceptions
   * if any.
   *
   * @return the message
   */
  @Override
  public String getMessage() {
    String s = super.getMessage();
    if (rootCause == null) {
      return s;
    } else {
      String s2 = rootCause.getMessage();
      return s == null ? s2 : s + ": " + s2;
    }
  }

  /**
   * Returns the embedded cause (throwable) which may be null.
   *
   * @return the cause
   */
  @Override
  public Throwable getCause() {
    return rootCause;
  }
}
