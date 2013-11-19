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

import junit.framework.TestCase;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Class to test JDBC persistent store.
 */
public class JdbcDatabaseTest extends TestCase {

  protected DataSource dataSource;
  protected JdbcDatabase database;

  @Override
  protected void setUp() throws Exception {

    // Setup in-memory H2 JDBC DataSource;
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:testdb");
    ds.setUser("sa");
    ds.setPassword("sa");
    dataSource = ds;
    database = new JdbcDatabase(dataSource);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      database.shutdown();
    } finally {
      super.tearDown();
    }
  }

  // Test creating a Table.
  public void testCreateTable() throws SQLException {
    // Connect to the database.
    Connection connection = database.getConnectionPool().getConnection();
    String tableName = "test_table";

    // Assert the table does not yet exist.
    assertFalse(database.verifyTableExists(tableName, null));

    String[] createTableDdl = {
        "CREATE TABLE IF NOT EXISTS " + tableName
        + " ( foo INT IDENTITY PRIMARY KEY NOT NULL)" };

    // Verify that we can create the table.
    assertTrue(database.verifyTableExists(tableName, createTableDdl));

    // Assert the table does now exist.
    assertTrue(database.verifyTableExists(tableName, null));

    connection.close();
    database.getConnectionPool().releaseConnection(connection);
  }

  // Tests getting the maximum table name length.
  public void testGetMaxTableNameLength() {
    // H2 has no max table name length, so we expect the default 255.
    assertEquals(255, database.getMaxTableNameLength());
  }

  // Checks that a bad connector name is encoded into valid SQL identifier
  // table name.
  private void checkMakeTableName(String prefix, String connectorName,
                                    int maxLength) throws Exception {
    String tableName =
        JdbcDatabase.makeTableName(prefix, connectorName, maxLength);
    assertEquals(-1, tableName.indexOf(connectorName));
    assertTrue(tableName.matches("[a-z0-9]+[a-z0-9_]*"));
    assertTrue(tableName.length() <= maxLength);
  }

  // Tests makeTableName with a connector name a safe name.
  public void testMakeTableNameSimpleConnectorName() throws Exception {
    String prefix = "googe_documents_";
    String connectorName = "connector_name";
    String tableName = database.makeTableName(prefix, connectorName);
    assertEquals(prefix + connectorName, tableName);
  }

  // Tests makeTableName with invalid SQL identifier characters in the
  // connector name.
  public void testMakeTableNameInvalidConnectorName() throws Exception {
    checkMakeTableName(null, "A!@#$T^Y&*-+", 64);
  }

  // Tests makeTableName with too long connector name.
  public void testMakeTableNameLongConnectorName() throws Exception {
    checkMakeTableName("gdoc_",
         "qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfg",
         30);
  }

  // Tests makeTableName with too long connector name.
  public void testMakeTableNameLongerConnectorName() throws Exception {
    checkMakeTableName("gdoc_",
         "qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfg",
         64);
  }

  // Tests makeTableName with a connector name that has hyphens.
  // Hyphens are allowed in connector names, but not SQL identifiers.
  public void testMakeTableNameHyphenatedConnectorName() throws Exception {
    checkMakeTableName("googe_documents_", "hyphenated-name", 64);
  }
}

