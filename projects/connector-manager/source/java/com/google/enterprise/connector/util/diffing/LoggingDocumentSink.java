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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link DocumentSink} that logs filtered documents.
 *
 * @since 2.8
 */
public class LoggingDocumentSink implements DocumentSink {
  private static final Logger LOG =
      Logger.getLogger(LoggingDocumentSink.class.getName());

  /* @Override */
  public void add(String documentId, FilterReason reason) {
    LOG.log(Level.FINER, "Skipping Document {0} with reason {1}",
        new String[]{documentId, reason.toString()});
  }
}
