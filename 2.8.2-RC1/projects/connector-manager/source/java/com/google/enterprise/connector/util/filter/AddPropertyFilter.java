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
import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * A {@link Document} filter that adds a new {@link Property}
 * or adds values to an existing Property.
 * <p/>
 * If the {@code overwrite} flag is {@code true}, the new
 * property values replace any existing values of the target property.
 * Otherwise, the new property values augment any existing values
 * of the target property.
 */
public class AddPropertyFilter extends AbstractDocumentFilter {

  /** The name of the new {@link Property}. */
  protected String propertyName;

  /** The List of addtional {@link Value Values} for the {@link Property}. */
  protected List<Value> additionalValues;

  /**
   * If {@code true}, overwrite any existing destination property values
   * with the new property values; otherwise augment the existing values
   * with the additional Values (like multi-valued Properties).
   */
  protected boolean overwrite = false;

  /**
   * Sets the the name of the {@link Property} to add.  If the property
   * already exists for this {@link Document}, either overwrite or
   * augment the existing property values with the additional values.
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
   * Sets the List of additional String values that will be returned for
   * the configured Property.
   *
   * @param values a List of String values.
   */
  public void setPropertyValues(List<String> values) {
    Preconditions.checkNotNull(values, "values may not be null");
    ImmutableList.Builder<Value> builder = ImmutableList.builder();
    for (String value : values) {
      builder.add(Value.getStringValue(value));
    }
    this.additionalValues = builder.build();
  }

  /**
   * Sets the additional String value that will be returned for
   * the configured Property.
   *
   * @param value a String value.
   */
  public void setPropertyValue(String value) {
    Preconditions.checkNotNull(value, "value may not be null");
    this.additionalValues = ImmutableList.of(Value.getStringValue(value));
  }

  /**
   * Sets the {@code overwrite} values flag. If {@code true}, any existing
   * values of the  property are overwritten with the copied values.
   * If {@code false}, the copied values augment those of the
   * property.  Default {@code overwrite} is {@code false}.
   *
   * @param overwrite the overwrite flag
   */
  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  /**
   * Finds a {@link Property} by {@code name}.  If the requested property
   * is the filtered property, return that property with the configured
   * additional values.
   * <p/>
   * If the {@code overwrite} flag is {@code true}, the new
   * property values replace any existing values of the target property.
   * Otherwise, the new property values augment any existing values
   * of the target property.
   */
  @Override
  public Property findProperty(Document source, String name)
      throws RepositoryException {
    Preconditions.checkState(propertyName != null, "must set propertyName");

    if (!propertyName.equals(name)) {
      // If not adding this property, pass on request.
      return source.findProperty(name);
    }

    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Adding property " + name);
    }

    if (overwrite) {
      // If overwrite, replace any existing values of named property
      // with the new values.
      return new SimpleProperty(additionalValues);
    } else {
      // If not overwrite, augment existing values of named property
      // with the addtional values.
      List<Value> values = super.getPropertyValues(source, name);
      values.addAll(additionalValues);
      return new SimpleProperty(values);
    }
  }

  /**
   * Gets the set of names of all {@link Property Properties} in the
   * {@link Document}, including any new Properties that might be
   * added.
   */
  @Override
  public Set<String> getPropertyNames(Document source)
      throws RepositoryException {
    Preconditions.checkState(propertyName != null, "must set propertyName");
    Set<String> superSet = new HashSet<String>(source.getPropertyNames());
    superSet.add(propertyName);
    return superSet;
  }

  @Override
  public String toString() {
    return super.toString() + ": (" + propertyName + " , "
           + additionalValues.toString() + " , " + overwrite + ")";
  }
}
