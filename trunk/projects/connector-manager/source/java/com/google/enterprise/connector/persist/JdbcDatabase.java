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

package com.google.enterprise.connector.persist;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Basic connectivity, table creation and connection pooling
 */
public class JdbcDatabase {
  private static final Logger LOGGER =
      Logger.getLogger(JdbcDatabase.class.getName());

  private final DataSource dataSource;
  private final ConnectionPool connectionPool;

  public JdbcDatabase(DataSource dataSource) {
    this.dataSource = dataSource;
    this.connectionPool = new ConnectionPool(dataSource);
    // TODO: Fetch the DataSource description property.
    LOGGER.config("Using JDBC DataSource: " + dataSource.toString());
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public ConnectionPool getConnectionPool() {
    return connectionPool;
  }

  @Override
  public synchronized void finalize() throws Exception {
    if (getConnectionPool() != null) {
      connectionPool.closeConnections();
    }
  }

  /**
   * Verify that a table exists in the DB.
   * If not, create it.
   *
   * @param tableName
   * @param createTableDdl
   */
  public void verifyTableExists(String tableName, Object[] params, List<String> createTableDdl) {
    Connection connection = null;
    boolean originalAutoCommit = true;
    try {
      connection = getConnectionPool().getConnection();
      try {
        originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        // TODO: How can I make this more atomic? If two connectors start up
        // at the same time and try to create the table, one wins and the other
        // throws an exception.

        // Check to see if the table already exists.
        DatabaseMetaData metaData = connection.getMetaData();

        // Oracle doesn't do case-insensitive table name searches.
        String tablePattern;
        if (metaData.storesUpperCaseIdentifiers()) {
          tablePattern = tableName.toUpperCase();
        } else if (metaData.storesLowerCaseIdentifiers()) {
          tablePattern = tableName.toLowerCase();
        } else {
          tablePattern = tableName;
        }
        // Now quote '-', a special character in search patterns.
        tablePattern =
            tablePattern.replace("_", metaData.getSearchStringEscape() + "_");
        ResultSet tables = metaData.getTables(null, null, tablePattern, null);
        try {
          while (tables.next()) {
            if (tableName.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
              LOGGER.config("Found table: " + tableName);
              return;
            }
          }
        } finally {
          tables.close();
        }

        // Our table was not found. Create it using the Create Table DDL.
        Statement stmt = connection.createStatement();
        try {
          for (String ddlStatement : createTableDdl) {
            String update = MessageFormat.format(ddlStatement, params);
            LOGGER.config("Creating table " + tableName + ": " + update);
            stmt.executeUpdate(update);
          }
          connection.commit();
        } finally {
          stmt.close();
        }
      } catch (SQLException e) {
        try {
          connection.rollback();
        } catch (SQLException ignored) {
        }
        throw e;
      } finally {
        try {
          connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException ignored) {
        }
        getConnectionPool().releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Failed to create table "
          + tableName, e);
    }
  }

  // A Pool of JDBC Connections.
  public static class ConnectionPool {
    private final DataSource dataSource;
    private final LinkedList<Connection> connections = new LinkedList<Connection>();

    ConnectionPool(DataSource dataSource) {
      this.dataSource = dataSource;
    }

    // Return a Connection from the ConnectionPool.  If the pool is empty,
    // then get a new Connection from the DataSource.
    synchronized Connection getConnection() throws SQLException {
      Connection conn;
      try {
        // Get a cached connection, but check if it is still functional.
        do {
          conn = connections.remove(0);
        } while (isDead(conn));
      } catch (IndexOutOfBoundsException e) {
        // Pool is empty.  Get a new connection from the dataSource.
        conn = dataSource.getConnection();
      }
      return conn;
    }

    // Release a Connection back to the pool.
    synchronized void releaseConnection(Connection conn) {
      connections.add(0, conn);
    }

    // Empty the Pool, closing all Connections.
    synchronized void closeConnections() {
      for (Connection conn : connections) {
        close(conn);
      }
      connections.clear();
    }

    // Returns true if connection is dead, false if it appears to be OK.
    private boolean isDead(Connection conn) {
      try {
        conn.getMetaData();
        return false;
      } catch (SQLException e) {
        close(conn);
        return true;
      }
    }

    // Close the Connection silently.
    private void close(Connection conn) {
      try {
        conn.close();
      } catch (SQLException e) {
        // Ignored.
      }
    }
  }
}
