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
import com.google.enterprise.connector.spi.DatabaseResourceBundle;
import com.google.enterprise.connector.spi.SpiConstants.DatabaseType;
import com.google.enterprise.connector.util.BasicChecksumGenerator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Basic database connectivity, table creation and connection pooling.
 * <p>
 * The JdbcDatabase wraps a {@link javax.sql.DataSource} instance,
 * adding additional features specifically beneficial to Connectors
 * and the ConnectorManager, such as:
 * <ul><li>Information regarding the underlying database implementation,
 * including the vendor name, version information, and description.</li>
 * <li>Maintaining a ConnectionPool for the DataSource.</li>
 * <li>Manufacturing legal database table names base upon a Connector name.</li>
 * <li>Creating database tables based upon a supplied DDL, or verifying
 * that such a table exists.</li>
 * <li>Methods useful for forming DatabaseResourceBundle filenames for
 * the specific vendor implementation.</li>
 * </ul>
 *
 * @since 2.8
 */
public class JdbcDatabase {
  private static final Logger LOGGER =
      Logger.getLogger(JdbcDatabase.class.getName());

  private final DataSource dataSource;
  private final DatabaseConnectionPool connectionPool;

  private DatabaseType databaseType;
  private String productName;
  private String description;
  private String resourceBundleExtension;

  public JdbcDatabase(DataSource dataSource) {
    this.dataSource = dataSource;
    this.connectionPool = new DatabaseConnectionPool(dataSource);
    LOGGER.config("Using JDBC DataSource: " + dataSource.toString());
    getDatabaseInfo();
  }

  @Override
  protected void finalize() throws Throwable {
    shutdown();
  }

  public synchronized void shutdown() {
    connectionPool.closeConnections();
  }

  /**
   * Returns {@code true} if the configured {@code JdbcDatabase} is unavailable.
   *
   * @return {@code true} if this {@code JdbcDatabase} is disabled, {@code false}
   * otherwise.
   */
  public boolean isDisabled() {
    // If I can successfully establish a Connection to the database, assume
    // the DataSource is functional.  Otherwise, consider it disabled.
    try {
      dataSource.getConnection().close();
      return false;
    } catch (SQLException e) {
      return true;
    }
  }

  /**
   * Return the underlying {@link DataSource} for the database instance.
   *
   *
   * @return the underlying {@link DataSource} for the database instance.
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Return the {@link DatabaseConnectionPool} used to maintain a collection
   * of opened {@link Connection Connections} to the {@link DataSource}.
   *
   * @return the {@link DatabaseConnectionPool} for this database
   */
  public DatabaseConnectionPool getConnectionPool() {
    return connectionPool;
  }

  /**
   * Return the {@link DatabaseType} for this database.
   *
   * @return the {@link DatabaseType} for this database
   */
  public DatabaseType getDatabaseType() {
    return databaseType;
  }

  /**
   * Return a sanitzed version of the vendor product name.
   * This sanitized string is used to create the resourceBundleExtension
   * and will generally match one of the supported DatabaseTypes.
   *
   * @return the sanitized product name
   */
  public String getProductName() {
    return productName;
  }

  /**
   * Return a description string for this database.
   * This may be a vendor-supplied string or simply the unsanitized
   * product name and version string.
   *
   * @return the {@code description} string
   */
  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return description;
  }

  /**
   * Returns the {@link DatabaseResourceBundle} extension that may be added
   * to a {@code ResourceBundle baseName} to build the name of the resource
   * that is specific to the database implementation identified by this
   * {@code JdbcDatabase} instance.
   * <p>
   * This is will be constructed from the database {@code productName},
   * {@code majorVersion}, and {@code minorVersion} separated by underbars
   * ({@code '_'}), for instance MySQL v5.6 will return "{@code _mysql_5_6}".
   *
   * @return the resource bundle extension
   */
  public String getResourceBundleExtension() {
    return resourceBundleExtension;
  }

  /**
   * If the supplied string is not {@code null} or empty, prepend an underscore,
   * else return the empty string.
   */
  private String bundleNameFragment(String string) {
    return Strings.isNullOrEmpty(string) ? "" : "_" + string;
  }

  /**
   * Sanitizes the supplied string for use as a portion of a
   * ResourceBundleExtension.  Specifically, a sanitized string
   * should consist of nothing other than lowercase alphabetics
   * [a-z], numerics [0-9], underscore '_', and hyphen '-'.
   * <p>
   * The following actions are taken sanitize the input string:
   * <ul><li>Alphabetics are converted to lowercase.</li>
   * <li>Leading and trailing invalid characters are removed.</li>
   * <li>The remaining invalid characters are converted to hyphens.</li>
   * </ul>
   * <p>
   * For example, the string "Hello World!" would sanitize to "hello-world".
   *
   * @param string the {@code String} to be sanitized.
   * @return sanitized version of the supplied {@code string}
   */
  private String sanitize(String string) {
    if (string != null) {
      return string.toLowerCase().replaceAll("[^a-z0-9_]+", " ").trim()
             .replace(' ', '-');
    }
    return string;
  }

  /**
   * Extract vendor-specific infromation about this database from
   * the DataBaseMetaData.  This will be used to identify the DatabaseType
   * and build the DatabaseResourceBundle extenstion for the database.
   */
  private void getDatabaseInfo() {
    // TODO: Support Manual Configuration of this information,
    // which would override this detection.

    // If the database is unavailable, don't try to fetch its metadata.
    if (isDisabled()) {
      databaseType = DatabaseType.OTHER;
      productName = description = "Disabled Database";
      resourceBundleExtension = "";
      return;
    }

    try {
      Connection connection = connectionPool.getConnection();
      try {
        DatabaseMetaData metaData = connection.getMetaData();
        productName = metaData.getDatabaseProductName();
        String productVersion = metaData.getDatabaseProductVersion();
        int majorVersion = metaData.getDatabaseMajorVersion();
        int minorVersion = metaData.getDatabaseMinorVersion();
        LOGGER.config("JDBC Database ProductName: " + productName);
        LOGGER.config("JDBC Database ProductVersion: " + productVersion);
        LOGGER.config("JDBC Database MajorVersion: " + majorVersion);
        LOGGER.config("JDBC Database MinorVersion: " + minorVersion);

        description = productName + " " + productVersion;
        productName = sanitize(productName);
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

        // Determine the DatabaseResourceBundle extension for this DB.
        resourceBundleExtension = bundleNameFragment(productName)
            + bundleNameFragment(Integer.toString(majorVersion))
            + bundleNameFragment(Integer.toString(minorVersion));

        return;
      } finally {
        connectionPool.releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Failed to retrieve DatabaseMetaData", e);
    }

    // Fallback in case we can't get anything from DatabaseMetaData.
    databaseType = DatabaseType.OTHER;
    productName = description = "Unknown Database";
    resourceBundleExtension = "";
  }

  /**
   * Returns the maximum table name length for this database vendor.
   */
  public int getMaxTableNameLength() {
    int maxTableNameLength;
    try {
      Connection connection = connectionPool.getConnection();
      try {
        DatabaseMetaData metaData = connection.getMetaData();
        maxTableNameLength = metaData.getMaxTableNameLength();
        if (maxTableNameLength == 0) {
          maxTableNameLength = 255;
        }
      } finally {
        connectionPool.releaseConnection(connection);
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
   * invalid SQL identifier characters, then it is constructed from an MD5 hash
   * of the requested name.
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
    Connection connection = connectionPool.getConnection();
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
      connectionPool.releaseConnection(connection);
    }
  }
}
