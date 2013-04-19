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
 * Interface that represents a property. A property is a list of
 * {@link Value Values}. Values are accessed through an iterator-like method:
 * {@link #nextValue()}, which returns the next available {@link Value}
 * or {@code null} if there are no more. Note: {@link Value} objects are
 * immutable, unlike {@link DocumentList} and {@link Document} objects.
 * <p/>
 * The typical pattern for consuming an object that implements this interface
 * is this (disregarding exception handling):
 *
 * <pre>
 * Property prop = ...
 * Value v;
 * while ((v = prop.nextValue()) != null) {
 *   doSomething(v);
 * }
 * </pre>
 *
 * @since 1.0
 */
public interface Property {

  /**
   * Returns the next {@link Value} in this property, if there is one.
   *
   * @return the new current value, if there is one; {@code null} otherwise
   * @throws RepositoryException if a repository access error occurs
   */
  public Value nextValue() throws RepositoryException;
}
