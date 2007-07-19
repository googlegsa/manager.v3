// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * Interface that represents a list of documents to be traversed. This type is
 * the return type of <code>{@link TraversalManager#startTraversal()}</code>
 * and <code>{@link TraversalManager#resumeTraversal(String)}</code>.
 * <p>
 * Documents are accessed through <code>{@link #nextDocument()}</code> and
 * <code>{@link #getDocument()}</code> interfaces, similar to the "cursor"
 * concept of <code>{@link java.sql.ResultSet}</code>. Initially, the cursor
 * is positioned before the first Document. A call to
 * <code>{@link #nextDocument()}</code> both returns a boolean, indicating
 * whether there are any more documents in this list, and advances the cursor to
 * point at the next one. A call to <code>{@link #getDocument()}</code>
 * returns the Document currently pointed to by the cursor.
 * <p>
 * The consumer of this structure:
 * <ul>
 * <li> Should never call <code>{@link #getDocument()}</code> before first
 * calling <code>{@link #nextDocument()}</code>. The implementor of this
 * interface may throw an IllegalStateException if that happens.
 * <li> Should never refer to a Document object obtained by
 * <code>{@link #getDocument()}</code> after a subsequent call to
 * <code>{@link #nextDocument()}</code> <b>unless</b> that subsequent call
 * returns false. In other words, a Document object becomes invalid as soon as
 * another valid Document is obtained.
 * </ul>
 * The typical pattern for consuming an object that implements this interface is
 * this (disregarding exception handling):
 * 
 * <pre>
 * DocumentList docList = ...
 * Document doc = null;
 * while (docList.nextDocument()) {
 *   doc = docList.getDocument();
 *   handleDoc(doc);
 * }
 * if (doc != null) {
 *   // last call to nextDocument() returned false, so doc is still valid
 *   handleDocSpecially(doc); 
 * }
 * </pre>
 */
public interface DocumentList {

  /**
   * Moves the document cursor down one row from its current position. The
   * document cursor is initially positioned before the first document; the
   * first call to this method makes the first document current; the second call
   * makes the second document current, and so on. 
   * 
   * @return <code>true</code> if the new current document is valid;
   *         <code>false</code> if there are no more documents
   * @throws RepositoryException if a repository access error occurs
   */
  public boolean nextDocument() throws RepositoryException;

  /**
   * Returns the current document.
   * 
   * @return the current document
   * @throws RepositoryException if a repository access error occurs
   */
  public Document getDocument() throws RepositoryException;
}
