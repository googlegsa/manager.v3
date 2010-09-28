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

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
      database.finalize();
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
}
