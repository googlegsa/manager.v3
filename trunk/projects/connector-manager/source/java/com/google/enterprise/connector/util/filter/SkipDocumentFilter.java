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
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.Value;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A {@link Document} filter that forces a Document to be skipped (or not)
 * based upon the presence/abscence of a specific {@link Property},
 * or based upon a match on one of the {@link Value Values} of that
 * property.
 * <p/>
 * <b>Example {@code documentFilters.xml} Configurations:</b>
 * <p/>
 * The following example skips documents that have a {@code NoIndex Property}.
 * <pre><code>
   &lt;bean id="NoIndex"
      class="com.google.enterprise.connector.util.filter.SkipDocumentFilter"&gt;
     &lt;property name="propertyName" value="NoIndex"/&gt;
     &lt;property name "skipOnMatch" value="true"/&gt;
   &lt;/bean&gt;
   </code></pre>
 * The following example skips documnents whose {@code Classification Property}
 * value is not {@code PUBLIC} or {@code DECLASSIFIED}.
 * <pre><code>
   &lt;!-- Filter out all but PUBLIC and DECLASSIFIED documents. --&gt;
   &lt;bean id="Classified"
      class="com.google.enterprise.connector.util.filter.SkipDocumentFilter"&gt;
     &lt;property name="propertyName" value="Classification"/&gt;
     &lt;property name="pattern" value="(PUBLIC)|(DECLASSIFIED)"/&gt;
     &lt;property name "skipOnMatch" value="false"/&gt;
   &lt;/bean&gt;
   </code></pre>
 *
 * @since 2.8.4
 */
public class SkipDocumentFilter extends AbstractDocumentFilter {

  /** The name of the {@link Property} to match. */
  protected String propertyName;

  /** The regex pattern to match in the property {@link Value Values}. */
  protected Pattern pattern;

  /**
   *  If {@code true} skip the document on a match.
   *  If {@code false} skip the document on a failed match.
   */
  protected boolean skipOnMatch;

  /**
   * Sets the the name of the {@link Property} to match.  If no
   * {@code pattern} is set, then any Document that exposes the
   * named property is considered a matching document.  If a {@code pattern}
   * is set, then any value of the property that matches the
   * regular expression is considered a match.
   *
   * @param propertyName the name of the {@link Property} to filter
   * @throws IllegalArgumentException if {@code propertyName} is {@code null}
   *         or empty
   */
  public void setPropertyName(String propertyName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(propertyName),
                                "propertyName may not be null or empty");
    this.propertyName = propertyName;
  }

  /**
   * Sets the regular expression pattern to match in the values.
   * The supplied {@code pattern} must conform to the syntax defined in
   * {@link java.util.regex.Pattern}.  If one of the property's values
   * matches this regular expression, this is considered a matching
   * document.
   * <p/>
   * If no pattern is specified, then the mere presence of the named
   * property would be considered a match.
   *
   * @param pattern the regular expression pattern to match in the values
   * @throws PatternSyntaxException if {@code pattern}'s syntax is invalid
   */
  public void setPattern(String pattern) throws PatternSyntaxException {
    if (!Strings.isNullOrEmpty(pattern)) {
      this.pattern = Pattern.compile(pattern);
    }
  }

  /**
   * Sets the skip document behaviour flag.
   * If {@code true} skip the document on a match.
   * If {@code false} skip the document on a failed match.
   *
   * @param skipOnMatch If {@code true} skip the document on a match,
   *        otherwise skip the document if the match fails.
   */
  public void setSkipOnMatch(boolean skipOnMatch) {
    this.skipOnMatch = skipOnMatch;
  }

  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    Preconditions.checkState(propertyName != null, "must set propertyName");
    Property prop = source.findProperty(name);
    if (propertyName.equals(name)) {
      if (pattern == null) {
        // If there is no pattern, then check presence/absence of the property.
        if ((prop == null) ^ skipOnMatch) {
          throw new SkippedDocumentException("Skipping document based upon "
              + ((prop == null) ? "absence" : "presence") + " of property "
              + propertyName);
        }
      } else if (prop != null) {
        return new SkipProperty(prop);
      }
    }
    return prop;
  }

  @Override
  public String toString() {
    return super.toString() + ": (" + propertyName + " , \""
           + pattern.pattern() + "\" , " + skipOnMatch + ")";
  }

  /**
   * Checks for a pattern match on the source property values.
   */
  private class SkipProperty implements Property {
    private final Property property;

    public SkipProperty(Property property) {
      this.property = property;
    }

    @Override
    public Value nextValue() throws RepositoryException {
      // Look for a pattern match in any of the property values.
      Value value = property.nextValue();
      if (value != null) {
        // TODO: pattern.matches() or pattern.find()??
        if (pattern.matcher(value.toString()).find() ^ !skipOnMatch) {
          throw new SkippedDocumentException("Skipping document based upon "
              + "property " + propertyName + " value: " + value.toString());
        }
      }
      return value;
    }
  }
}


