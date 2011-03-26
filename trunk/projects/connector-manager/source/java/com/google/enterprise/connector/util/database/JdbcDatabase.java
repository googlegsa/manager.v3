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
import com.google.common.base.Strings;
import com.google.enterprise.connector.spi.SpiConstants.DatabaseType;
import com.google.enterprise.connector.util.BasicChecksumGenerator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
  private final DatabaseConnectionPool connectionPool;
  private DatabaseType databaseType;
  private DatabaseInfo databaseInfo;

  public JdbcDatabase(DataSource dataSource) {
    this.dataSource = dataSource;
    this.connectionPool = new DatabaseConnectionPool(dataSource);
    // TODO: Fetch the DataSource description property.
    LOGGER.config("Using JDBC DataSource: " + dataSource.toString());
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public DatabaseConnectionPool getConnectionPool() {
    return connectionPool;
  }

  public DatabaseInfo getDatabaseInfo() {
    getInfo();
    return databaseInfo;
  }

  public DatabaseType getDatabaseType() {
    getInfo();
    return databaseType;
  }

  private void getInfo() {
    if (databaseInfo != null) {
      return;
    }
    // TODO: Support Manual Configuration of this information,
    // which would override this detection.
    try {
      Connection connection = getConnectionPool().getConnection();
      try {
        DatabaseMetaData metaData = connection.getMetaData();
        int majorVersion = metaData.getDatabaseMajorVersion();
        int minorVersion = metaData.getDatabaseMinorVersion();
        String productName = metaData.getDatabaseProductName();
        String productVersion = metaData.getDatabaseProductVersion();
        LOGGER.config("JDBC Database ProductName: " + productName);
        LOGGER.config("JDBC Database ProductVersion: " + productVersion);
        LOGGER.config("JDBC Database MajorVersion: " + majorVersion);
        LOGGER.config("JDBC Database MinorVersion: " + minorVersion);

        String description = productName + " " + productVersion;
        productName = DatabaseInfo.sanitize(productName);
        if (productName.equals(DatabaseType.ORACLE.toString())) {
          // Oracle already has a really long description string.
          description = productVersion.replace("\n", " ");
        } else if (productName.equals("microsoft-sql-server")) {
          // If it looks like "Microsoft SQL Server", shorten name.
          productName = DatabaseType.SQLSERVER.toString();
        } else if (productName.startsWith("db2")) {
          // IBM embellishes the DB2 productName with extra platform info.
          productName = "db2";
        } else if (productName.indexOf("derby") >= 0) {
          productName = "derby";
        } else if (productName.indexOf("firebird") >= 0) {
          productName = "firebird";
        } else if (productName.indexOf("informix") >= 0) {
          // Like anyone still uses this.
          productName = "informix";
        } else if (productName.indexOf("sybase") >= 0) {
          // Both Microsoft and Sybase products are called "SQL Server".
          productName = "sybase";
        }
        // Most of the others have simple, one-word product names.

        databaseType = DatabaseType.findDatabaseType(productName);
        databaseInfo =
            new DatabaseInfo(productName, Integer.toString(majorVersion),
            Integer.toString(minorVersion), description);
        return;
      } finally {
        getConnectionPool().releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Failed to retrieve DatabaseMetaData", e);
    }

    // Fallback in case we can't get anything from DatabaseMetaData.
    databaseType = DatabaseType.OTHER;
    databaseInfo = new DatabaseInfo(null, null, null, "Unknown Database");
  }

  @Override
  public synchronized void finalize() throws Exception {
    if (getConnectionPool() != null) {
      connectionPool.closeConnections();
    }
  }

  /**
   * Returns the maximum table name length for this database vendor.
   */
  public int getMaxTableNameLength() {
    int maxTableNameLength;
    try {
      Connection connection = getConnectionPool().getConnection();
      try {
        DatabaseMetaData metaData = connection.getMetaData();
        maxTableNameLength = metaData.getMaxTableNameLength();
        if (maxTableNameLength == 0) {
          maxTableNameLength = 255;
        }
      } finally {
        getConnectionPool().releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.warning("Failed to fetch database maximum table name length.");
      maxTableNameLength = 30;  // Assume the worst. Oracle is 30 chars.
    }
    return maxTableNameLength;
  }

  /**
   * Constructs a database table name based up the configured table name prefix
   * and the Connector name.  All attempts are made to make this a straight
   * concatenation.  However if the connector name is too long or contains
   * invalid SQL identifier characters, then we hash it.
   *
   * @param prefix the generated table name will begin with this prefix
   * @param connectorName the connector name
   */
  public String makeTableName(String prefix, String connectorName) {
    return makeTableName(prefix, connectorName, getMaxTableNameLength());
  }

  /**
   * Constructs a database table name based up the configured table name prefix
   * and the Connector name.  All attempts are made to make this a straight
   * concatenation.  However if the connector name is too long or contains
   * invalid SQL identifier characters, then we hash it.
   *
   * @param prefix the generated table name will begin with this prefix
   * @param connectorName the connector name
   * @param maxLength the maximum length of the generated table name
   */
  @VisibleForTesting
  static String makeTableName(String prefix, String connectorName, int maxLength) {
    prefix = Strings.nullToEmpty(prefix);
    String suffix;
    if ((connectorName.matches("[a-z0-9]+[a-z0-9_]*")) &&
        ((connectorName.length() + prefix.length()) <= maxLength)) {
      suffix = connectorName;
    } else {
      BasicChecksumGenerator sumGen = new BasicChecksumGenerator("MD5");
      suffix = sumGen.getChecksum(connectorName);
      if (prefix.length() + suffix.length() > maxLength) {
        suffix = suffix.substring(0, maxLength - prefix.length());
      }
    }
    // TODO: Match case of vendor identifiers?
    return (prefix + suffix).toLowerCase();
  }

  /**
   * Verify that a table named {@code tableName} exists in the database.
   * If not, create it, using the supplied DDL statements.
   *
   * @param tableName the name of the table to find in the database.
   * @param createTableDdl DDL statements that may be used to create the
   *        table if it does not exist.  If {@code null}, no attempt will
   *        be made to create the table.
   *
   * @return {@code true} if the table exists or was successfully created,
   *         {@code false} if the table does not exist.
   */
  public boolean verifyTableExists(String tableName, String[] createTableDdl) {
    try {
      // Try to create table.
      return verifyTableAndThrow(tableName, createTableDdl);
    } catch (SQLException e1) {
      // If that fails, we may have multiple clients trying to create the table
      // at the same time; wait a bit, then try again.
      try { Thread.sleep(30000); } catch (InterruptedException ignored) {}
      try {
        return verifyTableAndThrow(tableName, createTableDdl);
      } catch (SQLException e2) {
        LOGGER.log(Level.SEVERE, "Failed to create table " + tableName, e2);
        return false;
      }
    }
  }

  /**
   * Verify that a table named {@code tableName} exists in the database.
   * If not, create it, using the supplied DDL statements.
   *
   * @param tableName the name of the table to find in the database.
   * @param createTableDdl DDL statements that may be used to create the
   *        table if it does not exist.  If {@code null}, no attempt will
   *        be made to create the table.
   *
   * @return {@code true} if the table exists or was successfully created,
   *         {@code false} if the table does not exist.
   * @throws SQLException if table existence could not be determined or if
   *         table creation fails.
   */
  private boolean verifyTableAndThrow(String tableName, String[] createTableDdl)
      throws SQLException {
    boolean originalAutoCommit = true;
    Connection connection = getConnectionPool().getConnection();
    try {
      originalAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);

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
      // Now quote '%' and '-', a special characters in search patterns.
      tablePattern =
          tablePattern.replace("%", metaData.getSearchStringEscape() + "%");
      tablePattern =
          tablePattern.replace("_", metaData.getSearchStringEscape() + "_");
      ResultSet tables = metaData.getTables(null, null, tablePattern, null);
      try {
        while (tables.next()) {
          if (tableName.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
            LOGGER.config("Found table: " + tableName);
            return true;
          }
        }
      } finally {
        tables.close();
      }

      // Our table was not found.
      if (createTableDdl == null) {
        return false;
      }

      // Create the table using the supplied Create Table DDL.
      Statement stmt = connection.createStatement();
      try {
        for (String ddlStatement : createTableDdl) {
          LOGGER.config("Creating table " + tableName + ": " + ddlStatement);
          stmt.executeUpdate(ddlStatement);
        }
        connection.commit();
      } finally {
        stmt.close();
      }
      return true;
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
  }
}
