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
import java.util.Set;

/**
 * A filter that renames {@link Document} {@link Property Properties}.
 * The renamed property's values remain unchanged.  The original names of the
 * renamed properties do not appear in the Document's set of property names.
 * <p/>
 * Care must be taken when renaming a property to an already existing property.
 * Suppose a document has two properties, "{@code PropertyA}" and
 * "{@code PropertyB}".  After renaming {@code PropertyA} to {@code PropertyB},
 * a request for {@code PropertyB}'s values will subseqently return the values
 * of {@code PropertyA}, leaving the original values of {@code PropertyB}
 * orphaned (unless {@code PropertyB} was renamed, as well).
 */
public class RenamePropertyFilter extends CopyPropertyFilter {
  /**
   * Gets the set of names of all {@link Property Properties} in the
   * {@link Document}, substituting original names of renamed Properties
   * with the new name.
   */
  @Override
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    Preconditions.checkState(nameMap != null, "must set propertyNameMap");
    Set<String> names = new HashSet<String>(source.getPropertyNames());

    // Remove the original names of the renamed properties.
    names.removeAll(nameMap.values());

    // Add in the new names of the renamed properties.
    names.addAll(nameMap.keySet());

    return names;
  }
}
