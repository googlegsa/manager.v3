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

package com.google.enterprise.connector.spi;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Simple convenience implementation of the spi.PropertyMap interface. This
 * class is not part of the spi - it is provided for developers to assist in
 * implementations of the spi.
 */
public class SimplePropertyMap extends HashMap implements PropertyMap {

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.PropertyMap#getProperties()
   */
  public Iterator getProperties() throws RepositoryException {
    return this.values().iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.enterprise.connector.spi.PropertyMap
   * #getProperty(java.lang.String)
   */
  public Property getProperty(String name) throws RepositoryException {
    return (Property) this.get(name);
  }

  /**
   * Puts a property on the property map, associated with its name.
   * 
   * @param p A property to be added to the property map. The property must be
   *        non-null and have a non-null String name (obtained through
   *        getName())
   * @return The previous property on the map that had this name, if any; null
   *         otherwise
   * @throws RepositoryException
   */
  public Property putProperty(Property p) throws RepositoryException {
    if (p == null) {
      throw new IllegalArgumentException();
    }
    String name = p.getName();
    if (name == null) {
      throw new IllegalArgumentException();
    }
    return (Property) this.put(name, p);
  }

}
