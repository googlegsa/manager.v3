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

package com.google.enterprise.connector.persist;

/**
 * This Exception is thrown to indicate that a specified Connector
 * already exists.
 */
public class ConnectorExistsException extends PersistentStoreException {
  /**
   * Constructs a new ConnectorExistsException with no message and no root
   * cause.
   */
  public ConnectorExistsException() {
    super();
  }

  /**
   * Constructs a ConnectorExistsException with a supplied message but no root
   * cause.
   *
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *        method.
   */
  public ConnectorExistsException(String message) {
    super(message);
  }

  /**
   * Constructs a ConnectorExistsException with message and root cause.
   *
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *        method.
   * @param rootCause root failure cause
   */
  public ConnectorExistsException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * Constructs a ConnectorExistsException with the specified root cause.
   *
   * @param rootCause root failure cause
   */

  public ConnectorExistsException(Throwable rootCause) {
    super(rootCause);
  }
}
