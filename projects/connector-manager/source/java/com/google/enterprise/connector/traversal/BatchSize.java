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
   * Constructs a new {@link BatchSize} with a hint and maximum of 0.
   */
  public BatchSize() {
    this(0, 0);
  }

  /**
   * Constructs a new {@link BatchSize}, containing an optimal batch size
   * ({@code batchHint}), and a maximum batch size ({@code batchMaxium})
   * number of items to return in a
   * {@link com.google.enterprise.connector.spi.DocumentList DocumentList}.
   *<p>
   * Connectors should try to return an optimal size batch to stay within
   * the host load limit.  The hint acts as a suggestion; the connector
   * may choose to return a {@code DocumentList} that contains fewer documents
   * or more documents than the hint.  The {@link Traverser} will iterate over
   * the {@code DocumentList} until it is exhausted, the {@code batchMaximum}
   * number of documents are returned, or the {@code traversalTimeLimit}
   * expires.
   *
   * @param batchHint optimal number of documents to return in this batch.
   * @param batchMaximum maximum number of documents to return in this batch.
   */
  public BatchSize(int batchHint, int batchMaximum) {
    if (batchHint < 0 || batchMaximum < 0 || batchMaximum < batchHint) {
      throw new IllegalArgumentException("Batch Size cannot be negative.");
    }
    this.hint = batchHint;
    this.maximum = batchMaximum;
  }

  /**
   * Returns the optimal traversal batch size.
   * Connectors should try to return an optimal size batch to stay within
   * the host load limit.  The hint acts as a suggestion; the connector
   * may choose to return a {@code DocumentList} that contains fewer documents
   * or more documents than the hint.
   *
   * @return the optimal number of documents to return in this batch.
   */
  public int getHint() {
    return hint;
  }

  /**
   * Returns the maximal traversal batch size.
   * Connectors should try to return an optimal size batch to stay within
   * the host load limit.  The {@link Traverser} will not process more than
   * {@code batchMaximum} items from the batch.
   *
   * @return the optimal number of documents to return in this batch.
   */
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + hint;
    result = prime * result + maximum;
    return result;
  }

  @Override
  public String toString() {
    return "BatchSize: hint = " + hint + " maximum = " + maximum;
  }
}
