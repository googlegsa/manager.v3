// Copyright 2007-2008 Google Inc.
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

import java.util.Set;

/**
 * Interface that represents a document. A document is a map of String
 * property names to Property objects. Map-like functionality is provided
 * through the <code>{@link #findProperty(String)}</code> method. In
 * addition, a method provides the caller with the Set of all property
 * names, which it can use to iterate over all properties.
 * <p>
 * Important: a Property object obtained by calling
 * <code>{@link #findProperty(String)}</code> may be invalidated by the next
 * call to <code>{@link #findProperty(String)}</code>.  Typically, the
 * caller will store the current Property in a loop variable, so that it is
 * clear that this rule is observed; see the example code below.  The
 * caller may request a specific property multiple times with separate calls
 * to <code>{@link #findProperty(String)}</code>.  In such a case, the
 * implementation must return the same set of values associated with that
 * property name.  If the caller requests a property for which the Document
 * has no value, <code>null</code> should be returned.
 * <p>
 * The typical pattern for consuming an object that implements this
 * interface is this (disregarding exception handling):
 *
 * <pre>
 * Document doc = ...
 * Property prop = null;
 * if ((prop = doc.findProperty(specialPropName1)) != null) {
 *   doSomethingSpecial(prop);
 * }
 * if ((prop = doc.findProperty(specialPropName2)) != null) {
 *   doSomethingElseSpecial(prop);
 * }
 * ... so on for other special properties as needed ...
 * for (Iterator i = doc.getPropertyNames().iterator(); i.hasNext(); ) {
 *   prop = doc.findProperty((String) i.next());
 *   // assert(prop != null);
 *   doSomething(prop);
 * }
 * </pre>
 */
public interface Document {

  /**
   * Finds a property by name. If the current document has a property then
   * that property is returned.
   *
   * @param name The name of the property to find
   * @return The Property, if found; <code>null</code> otherwise
   * @throws RepositoryException if a repository access error occurs
   * @throws RepositoryDocumentException if a document has fatal
   *         processing errors
   */
  public Property findProperty(String name) throws RepositoryException;

  /**
   * Gets the set of names of all Properties in this Document.
   *
   * @return The names, as a Set
   * @throws RepositoryException if a repository access error occurs
   * @throws RepositoryDocumentException if a document has fatal
   *         processing errors
   */
  public Set getPropertyNames() throws RepositoryException;
}
