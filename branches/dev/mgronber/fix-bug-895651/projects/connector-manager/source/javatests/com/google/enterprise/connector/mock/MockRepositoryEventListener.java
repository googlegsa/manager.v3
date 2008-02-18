// Copyright 2008 Google Inc. All Rights Reserved.
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

package com.google.enterprise.connector.mock;

/**
 * Interface for objects that should be notified of events affecting the
 * MockRepositoryDocumentStore.
 */
public interface MockRepositoryEventListener {

  /**
   * Called when an event has operated on the MockRepositoryDocumentStore.
   */
  public void onEvent(MockRepositoryEvent event);
}
