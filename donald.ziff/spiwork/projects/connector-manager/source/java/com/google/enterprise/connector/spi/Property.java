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
 * Interface that represents a property. A property is a named list of Values.
 * Similar to <code>{@link DocumentList}</code> and
 * <code>{@link Property}</code>, list access is based on a "cursor" concept,
 * like that of <code>{@link java.sql.ResultSet}</code>. Initially, the
 * cursor is positioned before the first Value. A call to
 * <code>{@link #nextValue()}</code> both returns a boolean, indicating
 * whether there are any more values, and advances the cursor to point at the
 * next one. A call to <code>{@link #getValue()}</code> returns the Value
 * currently pointed to by the cursor.
 * <p>
 * The consumer of this structure:
 * <ul>
 * <li> Should never call <code>{@link #getValue()}</code> before first
 * calling <code>{@link #nextValue()}</code>. The implementor of this
 * interface may throw an IllegalStateException if that happens.
 * <li> Should never refer to a Property object obtained by
 * <code>{@link #getValue()}</code> after a subsequent call to
 * <code>{@link #nextValue()}</code> <b>unless</b> that subsequent call
 * returns false. In other words, a Value object obtained through
 * <code>{@link #getValue()}</code> becomes invalid as soon as another valid
 * Value is so obtained.
 * </ul>
 * The typical pattern for consuming an object that implements this interface is
 * this (disregarding exception handling):
 * 
 * <pre>
 * Property prop = ...
 * String name = prop.getPropertyName();
 * while (prop.nextValue()) {
 *   Value val = prop.getValue();
 *   handleValue(val);
 * }
 * </pre>
 */
public interface Property {

  /**
   * Get the name of the current property.
   * 
   * @return The property name
   * @throws RepositoryException if a repository access error occurs
   */
  public String getPropertyName() throws RepositoryException;

  /**
   * Moves the value cursor down one row from its current position. The value
   * cursor is initially positioned before the first value; the first call to
   * the method <code>nextValue</code> makes the first value the current
   * value; the second call makes the second value the current value, and so on.
   * 
   * @return <code>true</code> if the new current value is valid;
   *         <code>false</code> if there are no more values
   * @throws RepositoryException if a repository access error occurs
   */
  public boolean nextValue() throws RepositoryException;

  /**
   * Get the current value, as a <code>{@link Value}</code> object
   * 
   * @return the current value
   * @throws RepositoryException if a repository access error occurs
   */
  public Value getValue() throws RepositoryException;

}
