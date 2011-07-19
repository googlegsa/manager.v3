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

package com.google.enterprise.connector.manager;

/**
 * This is the top-level Exception class for all exceptions that can come up
 * through the manager interface.
 */
public class ConnectorManagerException extends Exception {
  /**
   * Constructs a new ConnectorManagerException with no message and no root
   * cause.
   */
  public ConnectorManagerException() {
    super();
  }

  /**
   * Constructs a ConnectorManagerException with a supplied message but no root
   * cause.
   *
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *        method.
   */
  public ConnectorManagerException(String message) {
    super(message);
  }

  /**
   * Constructs a ConnectorManagerException with message and root cause.
   *
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *        method.
   * @param rootCause root failure cause
   */
  public ConnectorManagerException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * Constructs a ConnectorManagerException with the specified root cause.
   *
   * @param rootCause root failure cause
   */
  public ConnectorManagerException(Throwable rootCause) {
    // The cause.toString is normally copied as the message, but we
    // override getMessage differently.
    super(null, rootCause);
  }

  /**
   * Returns the detail message, including messages from nested exceptions if
   * any.
   *
   * @return the message
   */
  @Override
  public String getMessage() {
    String s = super.getMessage();
    Throwable rootCause = getCause();
    if (rootCause == null) {
      return s;
    } else {
      String s2 = rootCause.getMessage();
      return s == null ? s2 : s + ": " + s2;
    }
  }
}
