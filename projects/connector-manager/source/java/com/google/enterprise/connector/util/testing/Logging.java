// Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.util.testing;

import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Tools to test logging output. */
public class Logging {
  /**
   * Enables logging and captures matching log messages. The messages
   * will not be localized or formatted.
   *
   * @param clazz the class to enable logging for
   * @param substring capture log messages containing this substring
   * @param output captured messages will be added to this collection
   */
  public static void captureLogMessages(Class<?> clazz,
      final String substring, final Collection<? super String> output) {
    Logger logger = Logger.getLogger(clazz.getName());
    logger.setLevel(Level.ALL);

    logger.addHandler(new Handler() {
        @Override public void close() {}
        @Override public void flush() {}

        @Override public void publish(LogRecord record) {
          if (record.getMessage().contains(substring)) {
            output.add(record.getMessage());
          }
        }
      });
  }

  private Logging() {
    throw new AssertionError();
  }
}
