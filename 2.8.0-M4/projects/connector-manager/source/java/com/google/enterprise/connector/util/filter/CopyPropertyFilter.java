// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.util.filter;

import com.google.common.base.Preconditions;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * A {@link Document} filter that copies a {@link Property Property's}
 * values to another property. The new property name is added to the
 * Document's set of properties, if it is not already in that set.
 * <p/>
 * Care must be taken when copying a property to an already existing property.
 * Suppose a document has two properties, "{@code PropertyA}" and
 * "{@code PropertyB}".  After copying {@code PropertyA} to {@code PropertyB},
 * a request for {@code PropertyB}'s values will subseqently return the values
 * of {@code PropertyA}, leaving the original values of {@code PropertyB}
 * orphaned (unless {@code PropertyB} was copied, as well).
 */
public class CopyPropertyFilter extends AbstractDocumentFilter {

  /** Map of new property names to actual property names */
  protected Map<String, String>nameMap;

  /**
   * Sets the property name map.  This {@code Map<String, String>}
   * maps a new name (the {@code key}) to the actual property name
   * (the {@code value}).
   *
   * @param propertyNameMap newName-to-propertyName {@link Map}
   * @throws NullPointerException if {@code propertyNameMap} is {@code null}
   */
  public void setPropertyNameMap(Map<String, String>propertyNameMap) {
    Preconditions.checkNotNull(propertyNameMap);
    this.nameMap = propertyNameMap;
  }

  /**
   * Finds a {@link Property} by {@code name}. If the
   * {@link Document} has a property of that name, then that property
   * is returned.
   */
  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    Preconditions.checkState(nameMap != null, "must set propertyNameMap");

    String realName = nameMap.get(name);
    if ((realName != null) && LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Accessing property " + realName + " as " + name);
    }
    return source.findProperty((realName == null) ? name : realName);
  }

  /**
   * Gets the set of names of all {@link Property Properties} in the
   * {@link Document}, including any new Properties that might be
   * produced by copying other Properties.
   */
  @Override
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    Preconditions.checkState(nameMap != null, "must set propertyNameMap");
    Set<String> superSet = new HashSet<String>(source.getPropertyNames());
    superSet.addAll(nameMap.keySet());
    return superSet;
  }

  @Override
  public String toString() {
    return super.toString() + ": " + nameMap;
  }
}
