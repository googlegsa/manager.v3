// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.database;

/**
 * Extends the {@code LocalDocumentStore} interface, adding
 * {@link #cancel()} and {@link reset()} methods needed by
 * the Connector Manager.
 *
 * @deprecated The uses of this interface are ignored
 */
@SuppressWarnings("deprecation")
@Deprecated
public interface DocumentStore
    extends com.google.enterprise.connector.spi.LocalDocumentStore {

  /**
   * Cancels any pending additions to the Documents table.
   * This discards any data that has not already been committed,
   * specifically, Documents that would have been written by a
   * call to {@link #flush()}.
   */
  public void cancel();

  /**
   * Deletes the Document Table associated with this Connector.
   */
  public void delete();
}
