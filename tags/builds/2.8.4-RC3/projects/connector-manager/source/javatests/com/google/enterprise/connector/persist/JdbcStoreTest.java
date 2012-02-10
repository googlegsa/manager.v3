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

package com.google.enterprise.connector.persist;

import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.util.database.JdbcDatabase;

import org.h2.jdbcx.JdbcDataSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Class to test JDBC persistent store.
 */
public class JdbcStoreTest extends PersistentStoreTestAbstract {
  protected DataSource dataSource;
  protected JdbcDatabase jdbcDatabase;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Setup in-memory H2 JDBC DataSource;
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:testdb");
    ds.setUser("sa");
    ds.setPassword("sa");
    dataSource = ds;
    jdbcDatabase = new JdbcDatabase(ds);

    store = new JdbcStore();
    ((JdbcStore) store).setDatabase(jdbcDatabase);
    ((JdbcStore) store).setResourceClassLoader(new TestClassLoader());
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      jdbcDatabase.shutdown();
    } finally {
      super.tearDown();
    }
  }

  // Tests creating Connector Table lazily.
  public void testCreateTable() throws SQLException {
    Connection connection = jdbcDatabase.getConnectionPool().getConnection();
    String tableName = "google_connectors";

    // Assert the table does not yet exist.
    assertFalse(jdbcDatabase.verifyTableExists(tableName, null));

    // Accessing the table should force its creation.
    Schedule schedule =
        store.getConnectorSchedule(getStoreContext("nonexist"));
    assertNull(schedule);

    // Assert the table does now exist.
    assertTrue(jdbcDatabase.verifyTableExists(tableName, null));

    connection.close();
    jdbcDatabase.getConnectionPool().releaseConnection(connection);
  }

  // A ClassLoader that looks for resources relative to the
  // current working directory and the source/resources directory.
  private class TestClassLoader extends ClassLoader {
    private static final String RESOURCE_DIR = "source/resources/";

    @Override
    public URL getResource(String name) {
      try {
        File file = new File(name);
        if (file.exists() && file.isFile()) {
          return file.toURI().toURL();
        } else {
          file = new File(RESOURCE_DIR + name);
          if (file.exists() && file.isFile()) {
            return file.toURI().toURL();
          }
        }
      } catch (MalformedURLException e) {
        // Fall through and look on classpath.
      }
      return this.getClass().getClassLoader().getResource(name);
    }
  }
}
