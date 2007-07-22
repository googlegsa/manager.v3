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
 * A bundle of unique properties, accessible by name or iterator.
 */
public interface PropertyMap {

  /**
   * Finds a property by name
   * @param name The property's name
   * @return The property with that name, or null if there is none
   * @throws RepositoryException 
   */
  public Property getProperty(String name) throws RepositoryException;

  /**
   * Gets all properties
   * @return An Iterator of {@link Property} objects
   * @throws RepositoryException 
   */
  public Iterator getProperties() throws RepositoryException;
  
}
