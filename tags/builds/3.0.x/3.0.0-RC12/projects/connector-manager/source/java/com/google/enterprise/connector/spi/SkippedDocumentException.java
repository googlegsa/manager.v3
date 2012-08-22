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

package com.google.enterprise.connector.spi;

/**
 * Thrown for documents that should be skipped for non-error reasons.
 * The Connector Manager skips over documents that throw
 * {@link RepositoryDocumentException RepositoryDocumentExceptions},
 * proceeding on to the next document.
 * Although the parent class {@code RepositoryDocumentExceptions} are logged at
 * WARNING level, due to their potential error conditions, these
 * {@code SkippedDocumentExceptions} are logged at FINER level as they can be
 * considered expected per-document events.
 * It is the responsibility of the Connector to construct a checkpoint
 * that skips over the offending Document if {@link DocumentList#checkpoint()}
 * is called after {@code SkippedDocumentException} is thrown.
 *
 * @since 2.4
 */
/* TODO (bmj): This is a temporary solution and should be replaced.
 * It uses Exceptions for non-exceptional cases.
 */
public class SkippedDocumentException extends RepositoryDocumentException {
  /**
   * Constructs a SkippedDocumentException with a supplied message.
   * The message should indicate the reason this document was skipped.
   *
   * @param message the message indicating the reason this document was skipped
   */
  public SkippedDocumentException(String message) {
    super(message);
  }
}
