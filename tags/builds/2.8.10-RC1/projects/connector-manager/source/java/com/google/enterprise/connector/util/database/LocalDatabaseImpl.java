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

package com.google.enterprise.connector.util.database;

import com.google.common.annotations.VisibleForTesting;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.DatabaseResourceBundle;
import com.google.enterprise.connector.spi.LocalDatabase;
import com.google.enterprise.connector.spi.SpiConstants.DatabaseType;

import javax.sql.DataSource;

/**
 * An implemenation of {@link LocalDatabase}, built as a thin wrapper upon
 * {@link JdbcDatabase} and {@link DatabaseResourceBundleManager}.
 *
 * @since 2.8
 */
public class LocalDatabaseImpl implements LocalDatabase {
  private final JdbcDatabase database;
  private final ClassLoader classLoader;
  private final DatabaseResourceBundleManager resourceBundleManager;

  /* Automatically generate the ResourceBundle baseName,
   * based upon the ConnectorType name and an arbitary
   * prefix and suffix. This is so much easier for the
   * Connector developer than allowing them to specify
   * the baseName of their choice.  Note that the use
   * of a baseName suffix (containing an underscore) deviates
   * considerably from the typical use of ResourceBundles
   * for natural languages, while providing no benefit to
   * the developer or user.
   */
  private static final String RESOURCE_BUNDLE_PREFIX = "config.";
  private static final String RESOURCE_BUNDLE_SUFFIX = "_sql";
  @VisibleForTesting
  protected String resourceBundleBaseName;

  /**
   * Constructs a {@link LocalDatabase} implementation for Connectors
   * of type {@code connectorTypeName}.  The {@code connectorTypeName}
   * is used to select {@link DatabaseResourceBundle}s for use by the
   * Connector.
   *
   * @param jdbcDatabase backing database for this LocalDatabase
   * @param connectorTypeName the Connector's ConnectorType name
   * @param connectorType a ConnectorType whose ClassLoader to use to locate
   *        DatabaseResourceBundles. This may be specific to Connector type.
   *        If {@code null}, the default ClassLoader will be used.
   */
  public LocalDatabaseImpl(JdbcDatabase jdbcDatabase, String connectorTypeName,
                           ConnectorType connectorType) {
    // TODO: Does this restrict resource lookup to the JAR file containing
    // the ConnectorType?  This may or may not be desirable.
    this(jdbcDatabase, connectorTypeName, (connectorType == null) ? null :
         connectorType.getClass().getClassLoader());
  }

  /**
   * Constructs a {@link LocalDatabase} implementation for Connectors
   * of type {@code connectorTypeName}.  The {@code connectorTypeName}
   * is used to select {@link DatabaseResourceBundle}s for use by the
   * Connector.
   *
   * @param jdbcDatabase backing database for this LocalDatabase
   * @param connectorTypeName the Connector's ConnectorType name.
   * @param classLoader ClassLoader to use to locate DatabaseResourceBundles.
   *        If {@code null}, the default ClassLoader will be used.
   */
  @VisibleForTesting
  public LocalDatabaseImpl(JdbcDatabase jdbcDatabase, String connectorTypeName,
                           ClassLoader classLoader) {
    this.database = jdbcDatabase;
    this.classLoader = classLoader;
    this.resourceBundleManager = new DatabaseResourceBundleManager();

    // If the connectorTypeName contains periods, replace them so as
    // not to look like a resource package name.
    resourceBundleBaseName = RESOURCE_BUNDLE_PREFIX
        + connectorTypeName.replace('.', '_') + RESOURCE_BUNDLE_SUFFIX;
  }

  /**
   * Gets a {@link DataSource} which the connector implementer can use for any
   * purpose.
   *
   * @return a {@link DataSource}
   */
  /* @Override */
  public DataSource getDataSource() {
    return database.getDataSource();
  }

  /**
   * Gets a {@link DatabaseResourceBundle} through which the connector
   * implementor can get database-specific resources, such as SQL. The {@code
   * DatabaseResourceBundle} returned will be constructed by the Connector
   * Manager to return resources specific to this connector's type and to the
   * specific database version currently in use. See the Developer's guide for
   * details on how implementors can supply resources to the installation.
   *
   * @return a {@link DatabaseResourceBundle}
   */
  /* @Override */
  public DatabaseResourceBundle getDatabaseResourceBundle() {
    return resourceBundleManager.getResourceBundle(resourceBundleBaseName,
        database.getResourceBundleExtension(), classLoader);
  }

  /**
   * Returns a {@link DatabaseType} enum identifying the database
   * implementation.
   *
   * @return a non-null {@link DatabaseType} enum identifying the database
   *         implementation.
   */
  /* @Override */
  public DatabaseType getDatabaseType() {
    return database.getDatabaseType();
  }

  /**
   * Returns a String giving a description of the database.
   * <p/>
   * For now, the form of this string is intentionally under-specified,
   * for flexibility. The SPI only guarantees that, if this object's database
   * type is not {@link DatabaseType#OTHER}, then this string begins with the
   * string-value of that {@link DatabaseType}. More formally, for any
   * {@code LocalDatabase} object {@code db} then the following is true:
   * {@code (db.getDescription().startsWith(db.getDatabaseType().toString())) ||
   * (db.getDatabaseType() == DatabaseType.OTHER)}. If the database type is
   * "other", then the string should start with a simple name of the
   * database (rather than the {@code "unsupported"}).
   * <p/>
   * The remainder of the string is reserved to hold additional information,
   * such as version.
   *
   * @return a non-{@code null} String description of the database
   */
  /* @Override */
  public String getDescription() {
    // Return a not-really-descriptive description.
    return database.getProductName();
    //  + " (" + database.getDescription() + ")";
  }
}
