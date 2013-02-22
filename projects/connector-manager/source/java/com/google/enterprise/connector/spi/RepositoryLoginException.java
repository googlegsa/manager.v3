// Copyright 2006 Google Inc.
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
 * Thrown to indicate unsuccessful login.
 */
public class RepositoryLoginException extends RepositoryException {
  /**
   * Constructs a RepositoryLoginException with no message and no root cause.
   */
  public RepositoryLoginException() {
    super();
  }

  /**
   * Constructs a RepositoryLoginException with a supplied message but no root
   * cause.
   *
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *        method.
   */
  public RepositoryLoginException(String message) {
    super(message);
  }

  /**
   * Constructs a RepositoryLoginException with message and root cause.
   *
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *        method.
   * @param rootCause root failure cause
   */
  public RepositoryLoginException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * Constructs a RepositoryLoginException with the specified root cause.
   *
   * @param rootCause root failure cause
   */
  public RepositoryLoginException(Throwable rootCause) {
    super(rootCause);
  }
}
