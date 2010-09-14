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

package com.google.enterprise.connector.util.diffing;

import java.util.logging.Logger;

/**
 * Run-time sanity checks.
 */
public class Check {
  private static final Logger LOG = Logger.getLogger(Check.class.getName());

  private Check() { // Prevent Instantiation.
  }
  /**
   * @param object
   * @throws NullPointerException if {@code object} is null.
   */
  public static void notNull(Object object) {
    if (object == null) {
      LOG.severe("internal error: null object");
      throw new NullPointerException();
    }
  }

  /**
   * @param condition
   * @param message
   * @throws RuntimeException if {@code condition is not true}.
   */
  public static void isTrue(boolean condition, String message) {
    if (!condition) {
      throw new RuntimeException("internal error: " + message);
    }
  }
}
