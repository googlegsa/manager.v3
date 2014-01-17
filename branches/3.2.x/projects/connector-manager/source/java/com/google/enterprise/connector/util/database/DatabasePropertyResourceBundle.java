// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.util.database;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.enterprise.connector.spi.DatabaseResourceBundle;

import net.jmatrix.eproperties.EProperties;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Supplies SQL language syntax variations for the various database
 * implementations.  This mirrors {@link java.util.PropertyResourceBundle}
 * in form and function, but is designed to supply SQL language translations
 * rather than spoken language translations.
 * <p/>
 * {@code DatabasePropertyResourceBundles} are typically loaded using a
 * {@link DatabaseResourceBundleManager}.
 * <p/>
 * Like {@code java.util.PropertyResourceBundle}, {@code DatabaseResourceBundle}
 * resources are specified in {@code Properties} files.  Unlike
 * {@link java.util.PropertyResourceBundle}, {@code DatabaseResourceBundle} uses
 * the <a href="http://code.google.com/p/eproperties">EProperties</a>
 * enhanced Properties package, which extends the standard
 * {@code java.util.Properties} and syntax to include things like:
 * variable substitution, nesting, inclusion and lists.
 * For instance:
 * <ul>
 * <li>Variable substitution syntax that mirrors the syntax used in
 * shell scripts, ant, etc.:
 * <pre>
 * table.name = connector_instances
 * getvalue.query = "SELECT ${column.connector.name} FROM ${table.name} ..."
 * </pre>
 * Variables may be defined in the {@code DatabaseResourceBundle} in which they
 * are used, or any of the ancestor {@code DatabaseResourceBundles} (see below).
 * Variable substitution is done at the time of the call to
 * {@link #getString(String)} or {@link #getStringArray(String)}, so the
 * returned string(s) have all known substitutions resolved.<pre> </pre></li>
 * <li>Lists that may be fetched as an array of Strings - great for potentially
 * multi-statement DDL. For instance, this table creation DDL resource
 * definition consists of three distinct SQL statements:
 * <pre>
 * create.table.ddl = ( "CREATE TABLE ...", "CREATE TRIGGER ...", "CREATE INDEX ..." )
 * </pre>
 * which can be retrieved using {@link #getStringArray(String)}:
 * <pre>
 * String[] ddlStatements = bundle.getStringArray("create.table.ddl");
 * </pre>
 * </li>
 * </ul>
 *
 * @since 2.8
 */
public class DatabasePropertyResourceBundle implements DatabaseResourceBundle {
  private static final Logger LOGGER =
      Logger.getLogger(DatabaseResourceBundle.class.getName());

  private final EProperties properties;
  private DatabasePropertyResourceBundle parent = null;

  /**
   * Creates a {@link DatabaseResourceBundle} backed by the properties
   * loaded from the {@code resourceUrl} and the given {@code parent}.
   *
   * @param resourceUrl the resource URL from which to read Properties
   * @param parent parent the DatabasePropertyResourceBundle to assign as parent
   *        - may be {@code null}
   * @throws NullPointerException if {@code resourceUrl} is {@code null}
   * @throws IOException if there is an error reading the resource file
   */
  public DatabasePropertyResourceBundle(URL resourceUrl,
      DatabasePropertyResourceBundle parent) throws IOException {
    Preconditions.checkNotNull(resourceUrl);
    if (parent == null) {
      properties = new EProperties();
    } else {
      this.parent = parent;
      properties = new EProperties(parent.properties);
    }
    properties.load(resourceUrl);
  }

  /**
   * Wraps a {@code DatabasePropertyResouceBundle} around the supplied set
   * of {@link Properties}.
   *
   * @param properties a {@link Properties} instance
   * @throws NullPointerException if {@code properties} is {@code null}
   */
  @VisibleForTesting
  DatabasePropertyResourceBundle(Properties properties) {
    Preconditions.checkNotNull(properties);
    if (properties instanceof EProperties) {
      this.properties = (EProperties) properties;
    } else {
      this.properties = new EProperties();
      this.properties.addAll(properties);
    }
  }

  /**
   * Sets the parent {@code DatabasePropertyResourceBundle} of this
   * {@code DatabasePropertyResourceBundle}.
   *
   * @param parent a DatabasePropertyResourceBundle
   */
  @VisibleForTesting
  void setParent(DatabasePropertyResourceBundle parent) {
    this.parent = parent;
    // Hook up the parent's Properties to be the parent of these
    // Properties.  This lets the Properties implementation traverse
    // the ancestry, looking for matches.  It also allows EProperties
    // substitutions to perform as expected.
    properties.setParent((parent == null) ? null : parent.properties, null);
  }

  /** Returns the parent {@code DatabasePropertyResourceBundle}. */
  @VisibleForTesting
  DatabasePropertyResourceBundle getParent() {
    return parent;
  }

  /**
   * Gets a string for the given key from this resource bundle or one of
   * its parents.
   *
   * @param key the key for the desired string
   * @return the String for the given key, or {@code null} if no
   *         resource was found for this key
   * @throws NullPointerException if {@code key} is {@code null}
   */
  @Override
  public String getString(String key) {
    Preconditions.checkNotNull(key);
    String value = properties.findProperty(key);
    LOGGER.finest("Get database resource: " + key + " = " + value);
    return value;
  }

  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  /**
   * Gets an array of Strings for the given {@code key} from this resource
   * bundle or one of its parents.
   *
   * @param key the key for the desired string array
   * @return a String[] for the given key, or {@code null} if no
   *         resource was found for this key
   * @throws NullPointerException if {@code key} is {@code null}
   */
  @Override
  @SuppressWarnings("unchecked")
  public String[] getStringArray(String key) {
    Preconditions.checkNotNull(key);
    Object value = properties.findValue(key);
    if (value != null) {
      if (value instanceof String) {
        LOGGER.finest("Get database resource: " + key + " = " + value);
        return new String[] { (String) value };
      }
      if (value instanceof List) {
        String[] values = ((List<String>) value).toArray(EMPTY_STRING_ARRAY);
        LOGGER.finest("Get database resource: " + key + " = " + values);
        return values;
      }
    }
    return null;
  }
}
