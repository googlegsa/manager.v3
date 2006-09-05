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

import com.google.enterprise.connector.manager.ConnectorManagerException;

/**
 *
 */
public class PersistentStoreException extends ConnectorManagerException {

  /**
   * 
   */
  public PersistentStoreException() {
    super();
  }

  /**
   * @param message
   */
  public PersistentStoreException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param rootCause
   */
  public PersistentStoreException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * @param rootCause
   */
  public PersistentStoreException(Throwable rootCause) {
    super(rootCause);
  }

}
