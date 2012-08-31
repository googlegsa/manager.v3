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
import com.google.common.base.Strings;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link Document} filter that removes the specified
 * {@link Property Properties} from the document.
 * The deleted properties are not fed to the GSA and do not
 * appear in the set of properties available from the document.
 * <p/>
 * <b>Example {@code documentFilters.xml} Configuration:</b>
 * <p/>
 * The following example filters out the {@code Department} and {@code Section}
 * properties from the document.
 * <pre><code>
   &lt;!-- Remove Department and Section properties. --&gt;
   &lt;bean id="DeleteDeptAndSect"
      class="com.google.enterprise.connector.util.filter.DeletePropertyFilter"&gt;
     &lt;property name="propertyNames"/&gt;
       &lt;set&gt;
         &lt;value&gt;Department&lt;/value&gt;
         &lt;value&gt;Section&lt;/value&gt;
       &lt;/set&gt;
     &lt;/property&gt;
   &lt;/bean&gt;
   </code></pre>
 */
public class DeletePropertyFilter extends AbstractDocumentFilter {

  /** The names of the Properties to filter. */
  protected Set<String> propertyNames;

  /**
   * Sets the the name of the {@link Property} to remove.
   *
   * @param propertyName the name of the {@link Property} to remove
   * @throws IllegalArgumentException if {@code propertyName} is {@code null}
   *         or empty
   */
  public void setPropertyName(String propertyName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(propertyName),
                                "propertyName may not be null or empty");
    this.propertyNames = Collections.singleton(propertyName);
  }

  /**
   * Sets the the names of the {@link Property Properties} to remove.
   *
   * @param propertyNames a {@code Set} of names of the
   *        {@link Property Properties} to remove
   * @throws NullPointerException if {@code propertyNames} is {@code null}
   */
  public void setPropertyNames(Set<String> propertyNames) {
    Preconditions.checkNotNull(propertyNames, "propertyNames may not be null");
    this.propertyNames = propertyNames;
  }

  /**
   * Finds a {@link Property} by {@code name}. If the {@link Document} has a
   * property of that name, and that property has not been deleted, then the
   * property is returned.
   */
  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    Preconditions.checkState(propertyNames != null, "must set propertyName(s)");

    return (propertyNames.contains(name)) ? null : source.findProperty(name);
  }

  /**
   * Gets the set of names of all {@link Property Properties} in the
   * {@link Document}, removing the names of deleted Properties from
   * the Set returned by the {@code source} {@link Document}.
   */
  @Override
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    Preconditions.checkState(propertyNames != null, "must set propertyName(s)");

    // Remove the named properties from the set of property names.
    Set<String> names = new HashSet<String>(source.getPropertyNames());
    names.removeAll(propertyNames);
    return names;
  }

  @Override
  public String toString() {
    return super.toString() + ": " + propertyNames;
  }
}
