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
 * Holder for the result of running {@link Traverser#runBatch(BatchSize)}
 */
public class BatchResult {
  private final TraversalDelayPolicy delayPolicy;
  private final int countProcessed;

  /**
   * Construct a new {@link BatchResult}.
   */
  public BatchResult(TraversalDelayPolicy delayPolicy, int countProcessed) {
    this.delayPolicy = delayPolicy;
    this.countProcessed = countProcessed;
  }

  public TraversalDelayPolicy getDelayPolicy() {
    return delayPolicy;
  }

  public int getCountProcessed() {
    return countProcessed;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + countProcessed;
    result =
        prime * result + ((delayPolicy == null) ? 0 : delayPolicy.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BatchResult other = (BatchResult) obj;
    if (countProcessed != other.countProcessed) {
      return false;
    }
    if (delayPolicy == null) {
      if (other.delayPolicy != null) {
        return false;
      }
    } else if (!delayPolicy.equals(other.delayPolicy)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "BatchResult: delayPolicy = " + delayPolicy + " countProcessed = "
        + countProcessed;
  }
}
