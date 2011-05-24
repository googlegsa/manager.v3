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
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A {@link Document} filter that alters the values of the specified
 * {@link Property Properties}.  The filter will scrutinize the
 * {@link Value Values} returned by the supplied {@link Property Properties}.
 * If the value (as a string) matches the regular expression {@code pattern},
 * then all matching regions of the value will be replace with the
 * {@code replacement} string.
 * The modified value may then either augment or overwrite the original value.
 */
public class ModifyPropertyFilter extends AbstractDocumentFilter {

  /** The logger for this class. */
  private static final Logger LOGGER =
      Logger.getLogger(ModifyPropertyFilter.class.getName());

  /** The names of the Properties to filter. */
  protected Set<String> propertyNames;

  /** The regex pattern to match in the property {@link Value Values}. */
  protected Pattern pattern;

  /** The replacement string for matching regions in the values. */
  protected String replacement = "";

  /**
   * If {@code true}, overwrite the matching property values; otherwise supply
   * the modified value as an additional Value (like multi-valued Properties).
   */
  protected boolean overwrite = false;

  /**
   * Sets the the name of the {@link Property} to filter.
   *
   * @param propertyName the name of the {@link Property} to filter
   * @throws IllegalArgumentException if {@code propertyName} is {@code null}
   *         or empty
   */
  public void setPropertyName(String propertyName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(propertyName),
                                "propertyName may not be null or empty");
    this.propertyNames = Collections.singleton(propertyName);
  }

  /**
   * Sets the the names of the {@link Property Properties} to filter.
   *
   * @param propertyNames a {@code Set} of names of the
   *        {@link Property Properties} to filter
   * @throws NullPointerException if {@code propertyNames} is {@code null}
   */
  public void setPropertyNames(Set<String> propertyNames) {
    Preconditions.checkNotNull(propertyNames, "propertyNames may not be null");
    this.propertyNames = propertyNames;
  }

  /**
   * Sets the regular expression pattern to match in the values.
   * The supplied {@code pattern} must conform to the syntax defined in
   * <a href="http://java.sun.com/j2se/1.5/docs/api/java/util/regex/Pattern.html">
   * {@code java.util.regex.Pattern}</a>.
   *
   * @param pattern the regular expression pattern to match in the values
   * @throws PatternSyntaxException if {@code pattern}'s syntax is invalid
   * @throws IllegalArgumentException if {@code pattern} is {@code null} or empty
   */
  public void setPattern(String pattern) throws PatternSyntaxException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(pattern),
                                "pattern may not be null or empty");
    this.pattern = Pattern.compile(pattern);
  }

  /**
   * Sets the replacement string for matching regions in the values.
   *
   * @param replacement the replacement String for matching regions in the
   *        values
   */
  public void setReplacement(String replacement) {
    this.replacement = Strings.nullToEmpty(replacement);
  }

  /**
   * Sets the {@code overwrite} values flag. If {@code true}, matching values
   * are overwritten with the modified value.  If {@code false}, matching
   * values are augmented by adding an additional modified value.
   * Default {@code overwrite} is {@code false}.
   *
   * @param overwrite the overwrite flag
   */
  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  /**
   * Finds a {@link Property} by {@code name}. If the {@code source}
   * {@link Document} has a property of that name, then that property
   * is returned.
   * <p/>
   * {@link Value Values} returned by the supplied {@link Property Properties}.
   * If any of the Property's values (as a string) match the regular
   * expression {@code pattern}, then all matching regions of the value
   * will be replace with the {@code replacement} string.
   * <p/>
   * The modified value may either augment or overwrite the original value,
   * based upon the {@code overwrite} flag.
   */
  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    Preconditions.checkState(propertyNames != null, "must set propertyNames");
    Preconditions.checkState(pattern != null, "must set pattern");

    if (!propertyNames.contains(name)) {
      // Not a property of interest. Just fetch it from the source.
      return source.findProperty(name);
    }

    // For properties of interest, fetch the values and examine them.
    // If a value matches the pattern, either replace or augment that value.
    Property prop = source.findProperty(name);
    LinkedList<Value> values = new LinkedList<Value>();
    Value value;
    while ((value = prop.nextValue()) != null) {
      String original = value.toString();
      String modified = pattern.matcher(original).replaceAll(replacement);
      if (original.equals(modified)) {
        values.add(value);
      } else if (overwrite) {
        values.add(Value.getStringValue(modified));
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Property Filter replaced " + name + " value "
                        + "\"" + original +  "\" with \""+ modified + "\"");
        }
      } else {
        values.add(value);
        values.add(Value.getStringValue(modified));
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Property Filter injected " + name
                        + " value \"" + modified + "\"");
        }
      }
    }
    return new SimpleProperty(values);
  }

  @Override
  public String toString() {
    return super.toString() + ": (" + propertyNames + " , "
        + pattern.pattern() + " , " + overwrite + ")";
  }
}
