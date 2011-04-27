// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.util.diffing;

import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Interface for constructing a {@link Document} representing a change
 * to be applied to the GSA after fetching any needed information from
 * the repository holding the document.
 *
 * @since 2.8
 */
public interface DocumentHandle {
  /**
   * Returns the Document's id. The returned id may not be {@code null} and
   * and must match the value returned by calling
   * {@code getDocument().findProperty(PROPNAME_DOCID))}
   */
  String getDocumentId();

  /**
   * Returns the non-{@code null} {@link Document} for applying this change
   * to the Google Search Appliance.
   */
  Document getDocument() throws RepositoryException;

  /**
   * Returns serialized {@link String} representation of this {@link
   * DocumentHandle} suitable for deserialization using {@link
   * DocumentHandleFactory#fromString(String)}. The return value must
   * not be {@code null}.
   */
  @Override
  String toString();
}
