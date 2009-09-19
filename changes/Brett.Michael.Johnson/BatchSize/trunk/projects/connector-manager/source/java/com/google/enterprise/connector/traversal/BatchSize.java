// Copyright 2009 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.traversal;

/**
 * Holder for the desired and maximum batch size.
 */
public class BatchSize {
  private final int hint;
  private final int maximum;

  /**
   * Construct a new {@link BatchSize}.
   *
   * @param batchHint optimal number of documents to return in this batch.
   * @param batchMaximum maximum number of documents to return in this batch.
   */
  public BatchSize(int batchHint, int batchMaximum) {
    this.hint = batchHint;
    this.maximum = batchMaximum;
  }

  public int getHint() {
    return hint;
  }

  public int getMaximum() {
    return maximum;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    BatchSize other = (BatchSize) obj;
    return ((hint == other.hint) && (maximum == other.maximum));
  }

  @Override
  public String toString() {
    return "BatchSize: hint  = " + hint + " maximum = " + maximum;
  }
}
