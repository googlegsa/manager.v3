// Copyright 2009 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.persist.StoreContext;
import com.google.enterprise.connector.traversal.TraversalStateStore;

class MockTraversalStateStore implements TraversalStateStore {
  private final PersistentStore stateStore;
  private final StoreContext storeContext;

  MockTraversalStateStore(PersistentStore stateStore,
      StoreContext storeContext) {
    this.stateStore = stateStore;
    this.storeContext = storeContext;
  }

  /**
   * Store traversal state.
   *
   * @param state a String representation of the state to store.
   *        If null, any previous stored state is discarded.
   */
  public void storeTraversalState(String state) {
    if (state == null) {
      stateStore.removeConnectorState(storeContext);
    } else {
      stateStore.storeConnectorState(storeContext, state);
    }
  }

  /**
   * Return a stored traversal state.
   *
   * @returns String representation of the stored state, or
   *          null if no state is stored.
   */
  public String getTraversalState() {
    return stateStore.getConnectorState(storeContext);
  }
}
