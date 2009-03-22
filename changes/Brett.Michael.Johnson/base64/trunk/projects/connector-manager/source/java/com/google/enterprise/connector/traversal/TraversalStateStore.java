// Copyright (C) 2008 Google Inc.
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

package com.google.enterprise.connector.traversal;

/**
 * Interface used by the Traverser to store traversal state between batches.
 */
public interface TraversalStateStore {
  /**
   * Store traversal state.
   *
   * @param state a String representation of the state to store.
   *        If null, any previous stored state is discarded.
   * @throws IllegalStateException if the store is no longer valid.
   */
  public void storeTraversalState(String state);

  /**
   * Return a stored traversal state.
   *
   * @return String representation of the stored state, or
   *         null if no state is stored.
   * @throws IllegalStateException if the store is no longer valid.
   */
  public String getTraversalState();
}
