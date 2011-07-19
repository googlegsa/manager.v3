// Copyright (C) 2010 Google Inc.
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

/**
 * Checks for changes in a persistent store. Intended to be run both
 * manually to handle local servlet changes, and periodically to check
 * for remote connector manager changes.
 *
 * @see com.google.enterprise.connector.persist.PersistentStore
 * @see ChangeListener
 */
interface ChangeDetector {
  /**
   * Compares the version stamps for the in-memory objects and
   * persisted objects, and notifies the {@link ChangeListener} of the
   * needed updates.
   *
   * <p>
   * The in-memory objects should reflect the persistent store, even
   * if the store contains older objects. If the version stamp for a
   * persisted object is older, then the in-memory object should be
   * reverted.
   */
  void detect();
}
