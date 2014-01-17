// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.spi;

import java.sql.PreparedStatement;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Supplies SQL language syntax variations for the various database
 * implementations.  This mirrors {@code java.util.ResourceBundle} in form
 * and function, but is designed to supply SQL language translations
 * rather than spoken language translations.
 * <p/>
 * Like {@code java.util.PropertyResourceBundle}, {@code DatabaseResourceBundle}
 * resources are specified in {@code Properties} files.  Unlike
 * {@link PropertyResourceBundle}, {@code DatabaseResourceBundle} uses the
 * <a href="http://code.google.com/p/eproperties">EProperties</a>
 * enhanced Properties package, which extends the standard
 * {@code java.util.Properties} and syntax to include things like:
 * variable substitution, nesting, inclusion and lists.
 * For instance:
 * <ul><li>Variable substitution syntax that mirrors the syntax used in
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
 * </li></ul>
 * <p/>
 * These features allow the connector developer to write very powerful
 * resource property files.
 * <p/>
 * Connectors must locate the DatabaseResourceBundle property files in their
 * {@code config} package.  The properties files have a base name derived from
 * the ConnectorType name (with any instances of '.' replaced with '_')
 * concatenated with "_sql".
 * Vendor-specific SQL syntax variations are formed by adding the
 * product name, major and minor versions to the base name.  So resource
 * property file names would look like:
 * <pre>
 * <ConnectorTypeName>_sql[_productName][_majorVersion][_minorVersion].properties
 * </pre>
 * For instance, suppose the ConnectorType name as defined in
 * {@code connectorType.xml} is "File.List", and the configured JDBC
 * {@code DataSource} is MySQL v5.1.  The Connector Manager will attempt to
 * load {@code DatabaseResourceBundles} from the following resources:
 * <ol>
 * <li>config.File_List_sql_mysql_5_1.properties</li>
 * <li>config.File_List_sql_mysql_5.properties</li>
 * <li>config.File_List_sql_mysql.properties</li>
 * <li>config.File_List_sql.properties</li>
 * </ol>
 *
 * Resources are searched in that order, from most specific to least
 * specific.  Variable substitution are also resolved from most specific
 * to least specific, so {@code File_List_sql_mysql.properties}
 * may refer to a property defined in {@code File_List_sql.properties},
 * but not one defined in {@code File_List_sql_mysql_5_1.properties}.
 *
 * @since 2.8
 */
public interface DatabaseResourceBundle {
  /**
   * Gets a resource that is specific to the active database implementation.
   * This API is modeled on {@link ResourceBundle#getString(String)} and is
   * intended to be used in a similar way. That is, it may return a String
   * (typically, SQL) into which parameter substitution may be done using
   * {@link MessageFormat#format(Object)}, JDBC {@link PreparedStatement}
   * and similar techniques.
   * <p/>
   * The implementation will assure that the correct resource is returned for
   * this connector type and for the active database implementation.
   * <p/>
   * If there is no resource defined for this key, {@code null} is returned
   * (unlike {@link ResourceBundle#getString(String)}, which throws an
   * exception).
   *
   * @param key as {@link ResourceBundle#getString(String)}
   * @return as {@link ResourceBundle#getString(String)}
   */
  public String getString(String key);

  /**
   * The same comments apply as {{@link #getString(String)}, only this
   * API corresponds to {@link ResourceBundle#getStringArray(String)}.
   * <p/>
   * If there is no resource defined for this key, an array of length zero is
   * returned (unlike {@link ResourceBundle#getString(String)}, which throws an
   * exception).
   *
   * @param key as {@link ResourceBundle#getStringArray(String)}
   * @return as {@link ResourceBundle#getStringArray(String)}
   */
  public String[] getStringArray(String key);
}
