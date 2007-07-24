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
 * Interface that represents a list of documents to be traversed. Documents are
 * accessed through an iterator-like method:
 * <code>{@link #nextDocument()}</code>, which returns the next available
 * Document or null if there are no more. Important: a Document object obtained
 * by calling <code>{@link #nextDocument()}</code> is invalidated by the next
 * call to <code>{@link #nextDocument()}</code>. Typically, the caller will
 * store the current Document in a loop variable, so that it is clear that this
 * rule is observed; see the example code below.
 * <p>
 * In addition, a this interface has a special method
 * <code>{@link #checkpoint()}</code>, which produces a String that
 * encapsulates the traversal state. The consumer may call
 * <code>{@link #checkpoint()}</code> at any time. The implementation should
 * return a string that, if supplied to
 * <code>{@link TraversalManager#resumeTraversal(String)}</code>, would cause
 * traversal to resume from the next unprocessed document.
 * </ul>
 * Boundary cases are important for <code>{@link #checkpoint()}</code>:
 * <ul>
 * <li>If <code>{@link #checkpoint()}</code> is called before any calls to
 * <code>{@link #nextDocument()}</code>, then traversal should resume with
 * the first Document that would have been returned by the first call to
 * <code>{@link #nextDocument()}</code>.
 * <li>If <code>{@link #checkpoint()}</code> is called after a call to
 * <code>{@link #nextDocument()}</code> that returns a valid document, then
 * traversal should resume with Document that would have been returned by the
 * first call to <code>{@link #nextDocument()}</code>.
 * </ul>
 * The typical pattern for consuming an object that implements this interface is
 * this (disregarding exception handling):
 * 
 * <pre>
 * DocumentList docList = ...
 * Document doc;
 * while (doc = docList.nextDocument()) {
 *   handleDoc(doc);
 *   if (whatever reason) break;
 * }
 * String check = doclist.checkpoint();
 * </pre>
 * 
 * Note: because of the restriction that the next call to
 * <code>nextDocument()</code> invalidates the previous Document, and there
 * are similar restrictions in the <code>{@link Document}</code> interface, it
 * is possible to provide a single stateful object that implements
 * <code>{link DocumentList}</code>, <code>{link Document}</code> and
 * <code>{link Property}</code>, by returning <code>this</code> (or
 * <code>null</code>) to all calls to <code>getDocument</code> and
 * <code>Document.findProperty(String)</code>. However, if preferred, the
 * implementor may also use separate objects for each of those interfaces.
 */
public interface DocumentList {

  /**
   * Returns the next Document in this document list, if there is one.
   * 
   * @return The new current document if there is one; <code>null</code>
   *         otherwise
   * @throws RepositoryException if a repository access error occurs
   */
  public Document nextDocument() throws RepositoryException;

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
