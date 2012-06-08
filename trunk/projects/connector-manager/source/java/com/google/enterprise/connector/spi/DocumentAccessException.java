// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

/**
 * Thrown in cases where the search or traversal user does not have enough
 * privileges to access a document.
 * <p/>
 * It is the responsibility of the Connector to construct a checkpoint
 * that skips over the offending Document if {@link DocumentList#checkpoint()}
 * is called after this exception is thrown.
 *
 * @since 3.0
 */
public class DocumentAccessException extends RepositoryDocumentException {
  /**
   * Constructs a new {@code DocumentAccessException} with no message
   * and no root cause.
   */
  public DocumentAccessException() {
    super();
  }

  /**
   * Constructs a {@code DocumentAccessException} with a supplied message,
   * but no root cause.
   *
   * @param message the message
   */
  public DocumentAccessException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code DocumentAccessException} with message and root
   * cause.
   *
   * @param message the message
   * @param rootCause root failure cause
   */
  public DocumentAccessException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * Constructs a {@code DocumentAccessException} with the specified root
   * cause.
   *
   * @param rootCause root failure cause
   */
  public DocumentAccessException(Throwable rootCause) {
    super(rootCause);
  }
}
