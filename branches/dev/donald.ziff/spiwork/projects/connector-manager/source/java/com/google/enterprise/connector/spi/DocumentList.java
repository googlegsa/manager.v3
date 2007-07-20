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
 * <p>
 * In addition, a this interface has a special method
 * <code>{@link #checkpoint()}</code>, which produces a String that
 * encapsulates the cursor position, so that if this String is provided to
 * <code>{@link TraversalManager#resumeTraversal(String)}</code>, the
 * traversal will resume from the next document after this one.
 * </ul>
 * The typical pattern for consuming an object that implements this interface is
 * this (disregarding exception handling):
 * 
 * <pre>
 * DocumentList docList = ...
 * while (docList.nextDocument()) {
 *   Document doc = docList.getDocument();
 *   handleDoc(doc);
 *   if (whatever reason) break;
 * }
 * String check = doclist.checkpoint();
 * </pre>
 * 
 * Note: one possible implementation technique is to provide a single stateful
 * object that implements <code>{link DocumentList}</code>,
 * <code>{link Document}</code> and <code>{link Property}</code>, and
 * returns <code>this</code> to all calls to <code>getDocument</code> and
 * <code>Document.getProperty()</code>. Or, if preferred, the implementor may
 * use separate objects for each of those interfaces.
 * 
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

  /**
   * Provides a checkpoint that can be used to control traversal. The
   * implementor should provide a string that can be used by the framework to
   * resume traversal starting just after this document. The framework will
   * typically call this method on the last document it has chosen to process
   * (for whatever reason: scheduling, completion of all documents currently
   * available, etc.) It will persist this string, so that it can be recovered
   * after a crash if necessary. When it chooses to restart traversal, it will
   * supply this string in a call to
   * <code>{@link TraversalManager#resumeTraversal(String)}</code>.
   * 
   * @return A string that can be used by a subsequent call to the
   *         {@link TraversalManager#resumeTraversal(String)} method.
   * @throws RepositoryException if a repository access error occurs
   */
  public String checkpoint() throws RepositoryException;
}
