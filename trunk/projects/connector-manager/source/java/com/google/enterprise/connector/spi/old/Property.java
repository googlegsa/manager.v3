// Copyright (C) 2006 Google Inc.
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

package com.google.enterprise.connector.spi.old;

import com.google.enterprise.connector.spi.RepositoryException;

import java.util.Iterator;

/**
 * A named, typed data item. Properties have a list of values. 
 * Most often, there is exactly one value,
 * however some CMS's allow multiple values, so the general case is addressed
 * here by an iterator.
 */
public interface Property {

  /**
   * Gets the property's name
   * @return String name
   * @throws RepositoryException if there is connectivity or other Repository
   *           access problems
   */
  public String getName() throws RepositoryException;

  /**
   * Gets a single value, either
   * <ul>
   * <li> the first value that would have been obtained through 
   * the iterator getValues(), if it is non-null, or
   * <li> null
   * </ul>
   * @return A single value, which may be null
   * @throws RepositoryException
   */
  public Value getValue() throws RepositoryException;
  
  /**
   * Gets the values, as an iterator of {@link Value} objects
   * @return Iterator of {@link Value} objects
   * @throws RepositoryException
   */
  public Iterator getValues() throws RepositoryException;
}