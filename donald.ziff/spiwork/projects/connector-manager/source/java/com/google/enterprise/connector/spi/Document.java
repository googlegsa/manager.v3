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
 * Interface that represents a document. A document is a set of Properties that
 * can be explored both as a map and a list. List-like functionality is provided
 * through the <code>nextProperty()</code> and <code>getProperty()</code>
 * methods. Map-like functionality is provided through the
 * <code>{@link #findProperty(String)}</code> method, discussed below. In
 * addition, a Document has a special method <code>{@link #checkpoint()}</code>,
 * which produces a String that encapsulates the current document so that if
 * this String is provided to
 * <code>{@link TraversalManager#resumeTraversal(String)}</code>, the
 * traversal will resume from the next document after this one.
 * <p>
 * Similar to <code>{@link DocumentList}</code> and
 * <code>{@link Property}</code>, the list functionality is based on a
 * "cursor" concept, like that of <code>{@link java.sql.ResultSet}</code>.
 * Initially, the cursor is positioned before the first Property. A call to
 * <code>{@link #nextProperty()}</code> both returns a boolean, indicating
 * whether there are any more properties, and advances the cursor to point at
 * the next one. A call to <code>{@link #getProperty()}</code> returns the
 * Property currently pointed to by the cursor.
 * <p>
 * The consumer of this structure:
 * <ul>
 * <li> Should never call <code>{@link #getProperty()}</code> before first
 * calling <code>{@link #nextProperty()}</code>. The implementor of this
 * interface may throw an IllegalStateException if that happens.
 * <li> Should never refer to a Property object obtained by
 * <code>{@link #getProperty()}</code> after a subsequent call to
 * <code>{@link #nextProperty()}</code> <b>unless</b> that subsequent call
 * returns false. In other words, a Property object becomes invalid as soon as
 * another valid Property is obtained.
 * </ul>
 * <p>
 * Unlike <code>{@link DocumentList}</code> and <code>{@link Property}</code>,
 * which only provide list-like access, this interface also provides map-like
 * access. Map-like access is provided through
 * <code>{@link #findProperty(String)}</code>, with
 * <code>{@link #getProperty()}</code>.
 * <p>
 * A call to <code>{@link #findProperty(String)}</code> returns true or false,
 * depending on whether this document has a property of the specified name. If
 * it returns true, then the next call to <code>{@link #getProperty()}</code>
 * will return that property.
 * <p>
 * The caller must not mix list access with map access. After the first call to
 * <code>{@link #nextProperty()}</code>, the caller may no longer call
 * <code>{@link #findProperty(String)}</code>. In addition, the first call to
 * <code>{@link #nextProperty()}</code> will always return the first
 * property--in particular it will not return the "next" property following the
 * previous one accessed by the last <code>{@link #findProperty(String)}</code>,
 * with <code>{@link #findProperty(String)}</code> call.
 * <p>
 * The typical pattern for consuming an object that implements this interface is
 * this (disregarding exception handling):
 * 
 * <pre>
 * Document doc = ...
 * Property specialProp;
 * if (doc.findProperty(specialPropName1)) {
 *   specialProp = doc.getProperty();
 *   doSomethingSpecial(specialProp);
 * }
 * if (doc.findProperty(specialPropName2)) {
 *   specialProp = doc.getProperty();
 *   doSomethingElseSpecial(specialProp);
 * }
 * ... etc. for other special properties ...
 * Property prop = null;
 * while (doc.nextProperty()) {
 *   prop = doc.getProperty();
 *   handleProp(prop);
 * }
 * </pre>
 */
public interface Document {

  /**
   * Moves the property cursor down one row from its current position. The
   * property cursor is initially positioned before the first property; the
   * first call to the method <code>nextProperty</code> makes the first
   * property the current property; the second call makes the second property
   * the current property, and so on.
   * <p>
   * Calls to <code>{@link #findProperty(String)}</code> are not permitted
   * after the first call to <code>nextProperty()</code>. The implementor may
   * throw an IllegalStateException if the caller violates this rule.
   * 
   * @return <code>true</code> if the new current property is valid;
   *         <code>false</code> if there are no more properties
   * @throws RepositoryException if a repository access error occurs
   */
  public boolean nextProperty() throws RepositoryException;

  /**
   * Finds a property by name. If the current document has a property with that
   * name, then it becomes the current property: that is, the next call to
   * <code>getProperty</code> will return the named property. All calls to
   * this method must be done before the first call to <code>nextProperty</code>;
   * the implementor may throw an IllegalStateException if the caller violates
   * this rule.
   * 
   * @param name The name of the property to find
   * @return <code>true</code> if the new current document has a valid
   *         property with the requested name; <code>false</code> if not
   * @throws RepositoryException if a repository access error occurs
   */
  public boolean findProperty(String name) throws RepositoryException;

  /**
   * Returns the current Property
   * 
   * @return the current Property
   * @throws RepositoryException
   */
  public Property getProperty() throws RepositoryException;

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
