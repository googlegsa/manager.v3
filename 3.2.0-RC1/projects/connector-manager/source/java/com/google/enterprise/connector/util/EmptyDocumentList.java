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

package com.google.enterprise.connector.util;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;

/**
 * An empty document list that provides a new checkpoint.
 *
 * @since 3.0
 */
public class EmptyDocumentList implements DocumentList {
  private final String checkpoint;

  /**
   * Constructs an empty document list with the given checkpoint.
   *
   * @param checkpoint the checkpoint for this document list
   */
  public EmptyDocumentList(String checkpoint) {
    this.checkpoint = checkpoint;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code null}, always, to indicate that this document list
   * contains no documents.
   */
  @Override
  public Document nextDocument() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @return the checkpoint for this document list
   */
  @Override
  public String checkpoint() {
    return checkpoint;
  }
}
