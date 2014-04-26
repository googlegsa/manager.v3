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

package com.google.enterprise.connector.database;

import com.google.common.base.Preconditions;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.LocalDatabase;
import com.google.enterprise.connector.util.database.JdbcDatabase;
import com.google.enterprise.connector.util.database.LocalDatabaseImpl;

import java.sql.SQLException;

/**
 * Factory used to construct {@link ConnectorPersistentStore} instances for
 * use by {@link Connector}s that are {@link ConnectorPersistentStoreAware}.
 */
public class ConnectorPersistentStoreFactory {
  private final JdbcDatabase jdbcDatabase;

  /**
   * Factory used to construct {@link ConnectorPersistentStore} instances
   * backed by the underlying {@link JdbcDatabase}.
   *
   * @param jdbcDatabase a {@link JdbcDatabase} to use for all manufactured
   *        {@link LocalDatabase} instances.
   */
  public ConnectorPersistentStoreFactory(JdbcDatabase jdbcDatabase) {
    Preconditions.checkNotNull(jdbcDatabase, "jdbcDatabase must not be null");
    this.jdbcDatabase = jdbcDatabase;
  }

  /**
   * Constructs a new {@link ConnectorPersistentStore} instance specific
   * to the supplied {@code Connector}.
   *
   * @param connectorName the name for {@code Connector} instance
   * @param connectorTypeName the name for {@code connectorType}
   * @param connectorType the Connector's {@link ConnectorType}
   */
  @SuppressWarnings("deprecation") // TODO(jlacey): Document store is deprecated
  public ConnectorPersistentStore newConnectorPersistentStore(
      String connectorName, String connectorTypeName,
      ConnectorType connectorType) throws SQLException {
    // If I can successfully establish a Connection to the database,
    // assume the JdbcDatabase is functional.
    jdbcDatabase.getDataSource().getConnection().close();

    final LocalDatabase localDatabase =
        new LocalDatabaseImpl(jdbcDatabase, connectorTypeName, connectorType);
    return new ConnectorPersistentStore() {
      @Override
      public com.google.enterprise.connector.spi.LocalDocumentStore getLocalDocumentStore() {
        return null;
      }

      @Override
      public LocalDatabase getLocalDatabase() {
        return localDatabase;
      }
    };
  }
}
