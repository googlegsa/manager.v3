// Copyright (C) 2008 Google Inc.
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
 * Thrown for problems accessing individual documents in a repository.
 * By default, the Connector Manager skips over documents that throw
 * RepositoryDocumentExceptions, proceding onto the next document.
 * It is the responsibility of the Connector to construct a checkpoint
 * that skips over the offending Document if DocumentList.checkpoint()
 * is called after RepositoryDocumentException is thrown.
 */
public class RepositoryDocumentException extends RepositoryException {
  /**
   * Constructs a new RepositoryDocumentException with no message
   * and no root cause.
   */
  public RepositoryDocumentException() {
    super();
  }

  /**
   * Constructs a RepositoryDocumentException with a supplied message,
   * but no root cause.
   * @param message the message.
   */
  public RepositoryDocumentException(String message) {
    super(message);
  }

  /**
   * Constructs a RepositoryDocumentException with message and root cause.
   * @param message the message.
   * @param rootCause root failure cause
   */
  public RepositoryDocumentException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * Constructs a RepositoryDocumentException with the specified root cause.
   * @param rootCause root failure cause
   */
  public RepositoryDocumentException(Throwable rootCause) {
    super(rootCause);
  }
}
