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

import java.io.InputStream;

/**
 * A {@link Retriever} provides access to document content, based upon a docid.
 *
 * @since 3.0
 */
public interface Retriever {
  /**
   * Return an {@code InputStream} that may be used to access content for the
   * document identified by {@code docid}.
   *
   * @param docid the document identifier
   * @return an InputStream for the document content or {@code null} if the
   *         document has no content.
   * @throws RepositoryDocumentException if there was a document-specific
   *         error accessing the content, for instance the document does not
   *         exist or should be skipped
   * @throws RepositoryException if there was a problem accessing the document
   *         repository
   */
  public InputStream getContent(String docid) throws RepositoryException;

  /**
   * Return a {@link Document} instance populated with meta-data for the
   * document identified by {@code docid}.  The returned Document should
   * <em>not</em> include the document content. The meta-data <em>should</em>
   * minimally include the {@code google:lastmodified} Property.
   * It should also include the document {@code google:mimetype} Properties,
   * if readily  available, to satisfy HEAD requests.
   *
   * @return a Document instance with Properties containing document meta-data
   * @throws RepositoryDocumentException if there was a document-specific
   *         error accessing the metadata, for instance the document does not
   *         exist or should be skipped
   * @throws RepositoryException if there was a problem accessing the document
   *         repository
   */
  public Document getMetaData(String docid) throws RepositoryException;
}
