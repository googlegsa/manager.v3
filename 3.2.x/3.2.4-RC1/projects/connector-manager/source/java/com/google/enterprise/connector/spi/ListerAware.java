// Copyright 2011 Google Inc.
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
 * A {@link Session} should implement this optional interface if it allows
 * {@link Lister} feed documents.
 *
 * @since 3.0
 */
public interface ListerAware {
  /**
   * Return a {@link Lister} that may be used to supply documents for indexing.
   */
  public Lister getLister() throws RepositoryException;
}
