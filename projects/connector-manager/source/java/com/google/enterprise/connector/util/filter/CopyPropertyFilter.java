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
import com.google.common.collect.ImmutableBiMap;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * A {@link Document} filter that copies a {@link Property Property's}
 * values to another property. The new property name is added to the
 * Document's set of properties, if it is not already in that set.
 * <p/>
 * If the {@code overwrite} flag is {@code true}, the copied
 * property values replace any existing values of the target property.
 * Otherwise, the copied property values supplement any existing values
 * of the target property.
 * <p/>
 * <b>Example {@code documentFilters.xml} Configurations:</b>
 * <p/>
 * The following example copies {@code HeadLine} and {@code ByLine} property
 * values to the {@code Title} and {@code Author} properties, respectively.
 * The original {@code HeadLine} and {@code ByLine} properties will still be
 * indexed and searchable.
 * <pre><code>
   &lt;!-- Make news articles appear in title and author searches. --&gt;
   &lt;bean id="MoveNewsProps"
      class="com.google.enterprise.connector.util.filter.CopyPropertyFilter"&gt;
     &lt;property name="propertyNameMap"&gt;
       &lt;map&gt;
         &lt;entry key="HeadLine" value="Title"/&gt;
         &lt;entry key="ByLine" value="Author"/&gt;
       &lt;/map&gt;
     &lt;/property&gt;
     &lt;property name="overwrite" value="false"/&gt;
   &lt;/bean&gt;
   </code></pre>
 */
public class CopyPropertyFilter extends AbstractDocumentFilter {

  /** Map of new property names to actual property names */
  protected Map<String, String> nameMap;

  /**
   * If {@code true}, overwrite any existing destination property values
   * with the copied property values; otherwise supply the copied values
   * as additional Values (like multi-valued Properties).
   */
  protected boolean overwrite = false;

  /**
   * Sets the property name map.  This {@code Map<String, String>}
   * maps source property name (the {@code key}) to the destination
   * property name (the {@code value}).
   *
   * @param propertyNameMap sourceName-to-destinationName {@link Map}
   * @throws NullPointerException if {@code propertyNameMap} is {@code null}
   */
  public void setPropertyNameMap(Map<String, String> propertyNameMap) {
    Preconditions.checkNotNull(propertyNameMap);
    this.nameMap = ImmutableBiMap.copyOf(propertyNameMap).inverse();
  }

  /**
   * Sets the {@code overwrite} values flag. If {@code true}, any existing
   * values of the copied-to property are overwritten with the copied values.
   * If {@code false}, the copied values augment those of the copied-to
   * property.
   * Default {@code overwrite} is {@code false}.
   *
   * @param overwrite the overwrite flag
   */
  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  /**
   * Finds a {@link Property} by {@code name}.  If the requested property
   * is configured as a copy from another property, then that other property's
   * values are copied to the named property.  If the {@code overwrite} flag
   * is {@code true}, then the copied property values replace any existing
   * values of the requested property.  Otherwise, the copied property values
   * supplement any existing values of the requested property.
   */
  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    Preconditions.checkState(nameMap != null, "must set propertyNameMap");

    String sourceName = nameMap.get(name);
    // If not copying this property, pass on request.
    if (sourceName == null) {
      return source.findProperty(name);
    }

    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Accessing property " + sourceName + " as " + name);
    }

    if (overwrite) {
      // If overwrite, replace any existing values of named property
      // with the values from sourceName property.
      return source.findProperty(sourceName);
    } else {
      // If not overwrite, augment existing values of named property
      // with the values from sourceName property.
      List<Value> values = super.getPropertyValues(source, name);
      values.addAll(super.getPropertyValues(source, sourceName));
      return values.isEmpty() ? null : new SimpleProperty(values);
    }
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
    return super.toString() + ": (" + nameMap + " , " + overwrite + ")";
  }
}
