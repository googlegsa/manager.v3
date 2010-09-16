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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.manager.ConnectorManagerException;

/**
 * This is the top-level Exception class for all exceptions that can come up
 * from the Instantiator.
 */
public class InstantiatorException extends ConnectorManagerException {
  /**
   * Constructs a new InstantiatorException with no message and no root
   * cause.
   */
  public InstantiatorException() {
    super();
  }

  /**
   * Constructs a InstantiatorException with a supplied message but no root
   * cause.
   *
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *        method.
   */
  public InstantiatorException(String message) {
    super(message);
  }

  /**
   * Constructs a InstantiatorException with message and root cause.
   *
   * @param message the message. Can be retrieved by the {@link #getMessage()}
   *        method.
   * @param rootCause root failure cause
   */
  public InstantiatorException(String message, Throwable rootCause) {
    super(message);
    this.rootCause = rootCause;
  }

  /**
   * Constructs a InstantiatorException with the specified root cause.
   *
   * @param rootCause root failure cause
   */
  public InstantiatorException(Throwable rootCause) {
    super();
    this.rootCause = rootCause;
  }

}
