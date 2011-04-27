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

import com.google.enterprise.connector.spi.SpiConstants.DatabaseType;

import javax.sql.DataSource;

/**
 * Provides access to the Connector Manager's configured JDBC database
 * which the connector implementer can use for any purpose.
 *
 * @see ConnectorPersistentStore
 * @since 2.8
 */
public interface LocalDatabase {

  /**
   * Gets a {@link DataSource} which the connector implementer can use for any
   * purpose.
   *
   * @return a {@link DataSource}
   */
  public DataSource getDataSource();

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
  public DatabaseResourceBundle getDatabaseResourceBundle();

  /**
   * Returns a {@link DatabaseType} enum identifying the database
   * implementation.
   *
   * @return a non-null {@link DatabaseType} enum identifying the database
   *         implementation.
   */
  public DatabaseType getDatabaseType();

  /**
   * Returns a String giving a description of the database.
   * <p/>
   * For now, the form of this string is intentionally under-specified, for
   * flexibility. The SPI only guarantees that, if this object's database type is not
   * {@link DatabaseType#OTHER}, then this string begins with the string-value
   * of that {@link DatabaseType}. More formally, for any
   * LocalDatabase object {@code db} then the following is true: {@code
   * (db.getDatabaseInfoString().startsWith(db.getDatabase().toString())) ||
   * (db.getDatabase() == DatabaseType.OTHER)}. If the database type is
   * "other", then the string should start with a simple name of the
   * database (rather than the {@code "unsupported"}).
   * <p/>
   * The remainder of the string is reserved to hold additional information,
   * such as version.
   *
   * @return a non-null String description of the database
   */
  public String getDescription();
}
