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
 * Interface that represents a list of documents to be traversed.
 * Documents are accessed through an iterator-like method:
 * {@link #nextDocument()}, which returns the next available
 * {@link Document} or {@code null} if there are no more.
 * <p>
 * <strong>Important:</strong> a {@link Document} object obtained
 * by calling {@link #nextDocument()} is invalidated by the next
 * call to {@link #nextDocument()}. Typically, the caller will
 * store the current Document in a loop variable, so that it is 
 * clear that this rule is observed; see the example code below.
 * <p>
 * In addition, a this interface has a special method
 * {@link #checkpoint()}, which produces a String that
 * encapsulates the traversal state. The consumer may call
 * {@link #checkpoint()} at any time. The implementation should
 * return a non-null String that, if supplied to
 * {@link TraversalManager#resumeTraversal(String)}, would cause
 * traversal to resume from the next unprocessed document.
 * <p>
 * Boundary cases are important for {@link #checkpoint()}:
 * <ul>
 * <li>If {@link #checkpoint()} is called before any calls to
 * {@link #nextDocument()}, then traversal should resume with
 * the Document that would have been returned by the first call to
 * {@link #nextDocument()}.</li>
 * <li>If {@link #checkpoint()} is called after a call to
 * {@link #nextDocument()} that returns a valid document, then
 * traversal should resume with the Document that would have been 
 * returned by the next call to {@link #nextDocument()}.</li>
 * <li>If {@link #checkpoint()} is not called, then traversal should
 * resume from the previous checkpoint, as if none of the documents
 * in this {@code DocumentList} had been processed.</li>
 * </ul>
 * The typical pattern for consuming an object that implements this 
 * interface is as follows (disregarding exception handling):
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
 * <strong>Note:</strong> because of the restriction that the next call to
 * {@link #nextDocument()} invalidates the previous Document, and there
 * are similar restrictions in the {@link Document} interface, it is possible
 * to provide a single stateful object that implements {@link DocumentList},
 * {@link Document} and {@link Property}, by returning {@code this}
 * (or {@code null}) to all calls to {@link #nextDocument()} and
 * {@link Document#findProperty(String)}. However, if preferred, the
 * implementor may also use separate objects for each of those interfaces.
 *
 * @since 1.0
 */
public interface DocumentList {

  /**
   * Returns the next {@link Document} in this document list, if there is one.
   *
   * @return the new current document if there is one; {@code null}
   *         otherwise
   * @throws RepositoryException if a repository access error occurs
   *         The Connector Manager will stop processing the
   *         {@code DocumentList}, call {@link #checkpoint()}, and wait
   *         a short period of time before resuming traversal.
   * @throws RepositoryDocumentException if this specific document
   *         has unrecoverable errors.  This document will be skipped,
   *         and {@code nextDocument()} or {@link #checkpoint()}
   *         may be subsequently called.
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
   * {@link TraversalManager#resumeTraversal(String)}.
   * <p>
   * If {@code null} is returned, then no new checkpoint will be saved,
   * and the existing checkpoint will be supplied to
   * {@link TraversalManager#startTraversal()} or
   * {@link TraversalManager#resumeTraversal(String)},
   * in effect, restarting the traversal from the last saved checkpoint.
   *
   * @return a non-{@code null} {@code String} that can be supplied
   *         subsequently to {@link TraversalManager#resumeTraversal(String)}
   * @throws RepositoryException if a repository access error occurs or if
   *         there is insufficient information to create a checkpoint string.
   *         If {@code checkpoint()} throws an exception, a subsequent
   *         call to {@link TraversalManager#resumeTraversal(String)} will be
   *         supplied the last known good checkpoint string.
   */
  public String checkpoint() throws RepositoryException;
}
