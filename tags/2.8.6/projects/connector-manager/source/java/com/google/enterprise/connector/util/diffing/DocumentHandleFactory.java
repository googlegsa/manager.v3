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

/**
 * Interface for constructing a {@link DocumentHandle} from its
 * {@link String} representation.
 *
 * @since 2.8
 */
public interface DocumentHandleFactory {
  /**
   * Creates a {@link DocumentHandle} from its {@link String}
   * representation created using {@link DocumentHandle#toString()}.
   * This may not return {@code null}.
   *
   * @param stringForm the String representation of the DocumentHandle
   * @return the reconstituted {@link DocumentHandle}
   * @throws IllegalArgumentException if {@code stringForm} is not valid.
   */
  DocumentHandle fromString(String stringForm);
}
