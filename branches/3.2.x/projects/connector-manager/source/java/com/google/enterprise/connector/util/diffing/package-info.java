// Copyright 2014 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * The {@code diffing} package provides a framework for writing
 * connectors to repositories that do not provide information about
 * added, deleted, or updated documents.
 * For repositories that support detecting updated documents, but not
 * added or deleted documents, implementing the
 * {@link com.google.enterprise.connector.spi.Lister Lister} and
 * {@link com.google.enterprise.connector.spi.Retriever Retriever}
 * interfaces is a better alternative.
 * <p>
 * A diffing connector, written
 * using this package, provides an iterable, fully-ordered {@link
 * com.google.enterprise.connector.util.diffing.SnapshotRepository
 * SnapshotRepository} of lightweight snapshots. A {@link
 * com.google.enterprise.connector.util.diffing.DocumentSnapshot
 * DocumentSnapshot} can be compared to another snapshot, stringified,
 * or used to obtain a {@link
 * com.google.enterprise.connector.spi.Document Document} via a {@link
 * com.google.enterprise.connector.util.diffing.DocumentHandle
 * DocumentHandle} class.
 * </p>
 * <img src="doc-files/lifecycle.png" alt="Lifecycle diagram">
 *
 * @since 2.8
 */
package com.google.enterprise.connector.util.diffing;
