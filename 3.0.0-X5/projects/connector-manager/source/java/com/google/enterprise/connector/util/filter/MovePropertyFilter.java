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
 * A filter that moves {@link Document} {@link Property} values to another
 * property.  The source property names do not appear in the Document's set
 * of property names.
 * <p/>
 * If the {@code overwrite} flag is {@code true}, the moved
 * property values replace any existing values of the target property.
 * Otherwise, the moved property values supplement any existing values
 * of the target property.
 * <p/>
 * <b>Example {@code documentFilters.xml} Configurations:</b>
 * <p/>
 * The following example moves {@code HeadLine} and {@code ByLine} property
 * values to the {@code Title} and {@code Author} properties, respectively.
 * The original {@code HeadLine} and {@code ByLine} properties will not be
 * indexed and will not be searchable. This latter behaviour differs from the
 * {@link CopyPropertyFilter} from which this filter derives.
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
public class MovePropertyFilter extends CopyPropertyFilter {
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
