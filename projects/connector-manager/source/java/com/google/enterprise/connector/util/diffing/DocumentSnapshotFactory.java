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
 * Interface for constructing a {@link DocumentSnapshot} from its
 * {@link String} representation.
 *
 * @since 2.8
 */
public interface DocumentSnapshotFactory {
  /**
   * Creates a {@link DocumentSnapshot} from its serialized {@link String}
   * representation that was created by {@link DocumentSnapshot#toString()}.
   * The returned value must not be {@code null}.
   *
   * @param stringForm the String representation of the {@link DocumentSnapshot}
   * @return the reconstituted {@link DocumentSnapshot}
   * @throws IllegalArgumentException if stringForm is not valid.
   */
  DocumentSnapshot fromString(String stringForm);
}
