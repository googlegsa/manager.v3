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

import java.util.Set;

/**
 * Interface that represents a document. A document is a map of String
 * property names to {@link Property} objects. Map-like functionality is
 * provided through the {@link #findProperty(String)} method. In
 * addition, a method provides the caller with the {@code Set} of all property
 * names, which it can use to iterate over all properties.
 * <p/>
 * <strong>Important:</strong> a {@link Property} object obtained by calling
 * {@link #findProperty(String)} may be invalidated by the next
 * call to {@link #findProperty(String)}.  Typically, the caller will
 * store the current {@code Property} in a loop variable, so that it is
 * clear that this rule is observed; see the example code below.  The
 * caller may request a specific property multiple times with separate calls
 * to {@link #findProperty(String)}.  In such a case, the
 * implementation must return the same set of values associated with that
 * property name.  If the caller requests a property for which the
 * {@code Document} has no value, {@code null} should be returned.
 * <p/>
 * The typical pattern for consuming an object that implements this
 * interface is as follows (disregarding exception handling):
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
 * for (String propName : doc.getPropertyNames()) {
 *   prop = doc.findProperty(propName);
 *   // assert(prop != null);
 *   doSomething(prop);
 * }
 * </pre>
 */
public interface Document {

  /**
   * Finds a {@link Property} by name. If the current document has a property
   * then that property is returned.
   *
   * @param name the name of the {@link Property} to find
   * @return the {@link Property}, if found; {@code null} otherwise
   * @throws RepositoryException if a repository access error occurs
   * @throws RepositoryDocumentException if a document has fatal
   *         processing errors
   */
  public Property findProperty(String name) throws RepositoryException;

  /**
   * Gets the set of names of all {@link Property Properties} in this
   * {@code Document}.
   *
   * @return the names, as a {@code Set} of Strings
   * @throws RepositoryException if a repository access error occurs
   * @throws RepositoryDocumentException if a document has fatal
   *         processing errors
   */
  public Set<String> getPropertyNames() throws RepositoryException;
}
