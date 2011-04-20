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

/**
 * A class that supplies SQL language syntax variations
 * for the various database implementations.
 * This mirrors {@link java.util.PropertyResourceBundle} in form
 * and function, but is designed to supply SQL language translations
 * rather than spoken language translations.
 * <p>
 * DatabasePropertyResourceBundles are typically loaded using a
 * {@link SqlResourceBundleManager}.
 * <p>
 * The DatabasePropertyResourceBundles are loaded using the
 * {@link http://code.google.com/p/eproperties EProperties}
 * enhanced Properties package, which extends the standard
 * {@code java.util.Properties} object and syntax to include things like:
 * variable substitution, nesting, inclusion and lists.
 * <p>
 * These features should all the developer to write very powerful resource
 * property files.  For instance:
 * <ul><li> Substitution with syntax like shell scripts, ant, etc.:
 * <pre>
   table.name = connector_instances
   getvalue.query = "SELECT ${column.connector_name} FROM ${table.name} ..."
   </pre></li>
 * <li>Lists that return a {@code List<String>} - great for multi-line DDL.
 * <pre>
   create.table.ddl = ( "CREATE TABLE ....", "CREATE TRIGGER ...", "CREATE INDEX ..." )
   ...
   List<String> ddlStatements = bundle.getList("create.table.ddl");
   </pre></li>
 * <li>Includes; properties files can include other properties files.
 * </li></ul>
 */
public class DatabasePropertyResourceBundle implements DatabaseResourceBundle {
  private final EProperties properties;
  private DatabasePropertyResourceBundle parent = null;

  /**
   * Creates a SqlPropertiesResouceBundle backed by the properties
   * loaded from the {@code resourceUrl} and the given {@code parent}.
   *
   * @param resourceUrl URL from which to read Properties.
   * @param parent parent the DatabasePropertyResourceBundle to assign as parent.
   *        may be null;
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
   * Wraps a SqlPropertiesResouceBundle around the supplied set
   * of {@link Properties}.
   *
   * @param properties a Properties instance.
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
   * @throws NullPointerException if {@code key} is {@code null}
   * @return the String for the given key or {@code null} if no
   *         resource was found for this key.
   */
  /* @Override */
  public String getString(String key) {
    Preconditions.checkNotNull(key);
    return properties.findProperty(key);
  }

  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  /**
   * Gets a {@code String[]} for the given {@code key} from this resource
   * bundle or one of its parents.
   *
   * @param key the key for the desired string array
   * @return a String[] for the given key, or {@code null} if no
   *         resource was found for this key.
   * @throws NullPointerException if {@code key} is {@code null}
   */
  /* @Override */
  @SuppressWarnings("unchecked")
  public String[] getStringArray(String key) {
    Preconditions.checkNotNull(key);
    Object value = properties.findValue(key);
    if (value != null) {
      if (value instanceof String) {
        return new String[] { (String) value };
      }
      if (value instanceof List) {
        return ((List<String>) value).toArray(EMPTY_STRING_ARRAY);
      }
    }
    return null;
  }
}
