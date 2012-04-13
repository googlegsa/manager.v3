// Copyright 2011 Google Inc.
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

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests JdbcDataSourceFactory.
 */
public class JdbcDataSourceFactoryTest extends TestCase {

  /**
   * Test newJdbcDataSource with enough info to create a real DataSource.
   */
  @SuppressWarnings("unchecked")
  public void testNewJdbcDatasource() throws Exception {
    DataSource ds = JdbcDataSourceFactory.newJdbcDataSource("In-memory H2",
        "org.h2.jdbcx.JdbcDataSource", "jdbc:h2:mem:testdb");
    assertNotNull(ds);
    assertTrue(ds instanceof org.h2.jdbcx.JdbcDataSource);

    JdbcDataSource h2ds = (JdbcDataSource) ds;
    h2ds.setURL("jdbc:h2:mem:testdb");
    h2ds.setUser("sa");
    h2ds.setPassword("");
    h2ds.getConnection().close();
  }

  /**
   * Test newJdbcDataSource with null property value.
   */
  public void testNullProperty() throws Exception {
    DataSource ds = JdbcDataSourceFactory.newJdbcDataSource("Test",
        "org.h2.jdbcx.JdbcDataSource", null);
    assertNotNull(ds);
    assertTrue(ds instanceof FakeDataSource);
  }

  /**
   * Test newJdbcDataSource with empty string property value.
   */
  public void testEmptyProperty() throws Exception {
    DataSource ds = JdbcDataSourceFactory.newJdbcDataSource("Test",
        "org.h2.jdbcx.JdbcDataSource", "");
    assertNotNull(ds);
    assertTrue(ds instanceof FakeDataSource);
  }

  /**
   * Test driver class not found.
   */
  public void testDriverNotFound() throws Exception {
    DataSource ds = JdbcDataSourceFactory.newJdbcDataSource("ROT",
        "gov.cia.GeorgeKaplan", "NorthByNorthwest");
    assertNotNull(ds);
    assertTrue(ds instanceof FakeDataSource);
  }

  /**
   * Test FakeDataSource.  Calling the setters should be OK, but
   * trying to get a Connection should throw a SQLException.
   */
  @SuppressWarnings("unchecked")
  public void testFakeDataSource() throws Exception {
    DataSource ds = JdbcDataSourceFactory.newJdbcDataSource("H2",
        "org.h2.jdbcx.JdbcDataSource", "");
    assertNotNull(ds);
    assertTrue(ds instanceof FakeDataSource);

    FakeDataSource fakeds = (FakeDataSource) ds;
    fakeds.setURL("jdbc:h2:mem:testdb");
    fakeds.setUser("sa");
    fakeds.setPassword("");

    try {
      ds.getConnection();
      fail("Expected SQLException not thrown.");
    } catch (SQLException se) {
      // Expected.
    }

    try {
      ds.getConnection("sa", "");
      fail("Expected SQLException not thrown.");
    } catch (SQLException se) {
      // Expected.
    }
  }
}
