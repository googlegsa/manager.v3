// Copyright 2006 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * A {@link TraversalManager} should implement this interface if it wants the
 * Connector Manager to supply it with a {@link TraversalContext} object.
 *
 * @since 1.0
 */
public interface TraversalContextAware {
  /**
   * Supplies a {@link TraversalContext} object which the
   * {@link TraversalManager} can use to get various information
   * from the Connector Manager.
   *
   * @param traversalContext the {@link TraversalContext}
   */
  public void setTraversalContext(TraversalContext traversalContext);
}
