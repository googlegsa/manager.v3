// Copyright 2010 Google Inc.  All Rights Reserved.
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

import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import java.io.File;
import java.util.Properties;

/**
 * Class to test JDBC persistent store for
 * ConnectorStateStore, ConnectorScheduleStore,
 * ConnectorConfigStore interfaces.
 */
public class JdbcStoreTest extends TestCase {
  private static final String dsName = "jdbc/testDataSourcName";

  protected File storeDir;
  protected JdbcStore store;
  protected DataSource dataSource;

  @Override
  protected void setUp() throws Exception {
    // Setup in-memory H2 JDBC DataSource;
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:testdb");
    ds.setUser("sa");
    ds.setPassword("sa");
    dataSource = ds;

    store = new JdbcStore();
    store.setDataSource(dataSource);
  }

  @Override
  protected void tearDown() throws Exception {
    store.finalize();
  }

  // Tests creating Connector Table.
  public void testCreateTable() throws SQLException {
    // Connect to the database.
    Connection connection = dataSource.getConnection();

    assertFalse(tableExists(connection, JdbcStore.TABLE_NAME));

    String schedule = store.getConnectorSchedule(new StoreContext("nonexist"));
    assertNull(schedule);

    /* TODO: Why does this fail for in-memory DBs?
    assertTrue(tableExists(connection, JdbcStore.TABLE_NAME));
    */

    connection.close();
  }

  private boolean tableExists(Connection connection, String tableName)
      throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    ResultSet tables = metaData.getTables(null, null, tableName, null);
    try {
      while (tables.next()) {
        if (tableName.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
          return true;
        }
      }
      return false;
    } finally {
      tables.close();
    }
  }

  // Tests getting and setting for a valid connector name and schedule.
  public void testGetandSetConnectorSchedule() {
    String expectedSchedule = "schedule of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    store.storeConnectorSchedule(storeContext, expectedSchedule);
    String resultSchedule = store.getConnectorSchedule(storeContext);
    assertEquals(expectedSchedule, resultSchedule);
  }

  // Tests getting schedule for an unknown connector
  public void testGetConnectorSchedule() {
    String schedule = store.getConnectorSchedule(
        new StoreContext("some wierd connector name"));
    assertNull(schedule);
  }

  // Tests schedule cannot be retrieved after removal.
  public void testRemoveConnectorSchedule() {
    String connectorName = "foo";
    String connectorSchedule = "foo's schedule";
    StoreContext storeContext = new StoreContext(connectorName);
    String schedule = store.getConnectorSchedule(storeContext);
    assertNull(schedule);
    store.storeConnectorSchedule(storeContext, connectorSchedule);
    schedule = store.getConnectorSchedule(storeContext);
    assertEquals(connectorSchedule, schedule);
    store.removeConnectorSchedule(storeContext);
    schedule = store.getConnectorSchedule(storeContext);
    assertNull(schedule);
  }

  // Tests getting and setting for a valid connector name and state.
  public void testGetandSetConnectorState() {
    String expectedState = "state of connectorA";
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    store.storeConnectorState(storeContext, expectedState);
    String resultState = store.getConnectorState(storeContext);
    assertEquals(expectedState, resultState);
  }

  //Tests getting state for an unknown connector.
  public void testGetConnectorState() {
    String state = store.getConnectorState(
        new StoreContext("some wierd connector name"));
    assertNull(state);
  }

  // Tests state cannot be retrieved after removal.
  public void testRemoveConnectorState() {
    String connectorName = "foo";
    String connectorState = "foo's state";
    StoreContext storeContext = new StoreContext(connectorName);
    String state = store.getConnectorState(storeContext);
    assertNull(state);
    store.storeConnectorState(storeContext, connectorState);
    state = store.getConnectorState(storeContext);
    assertEquals(connectorState, state);
    store.removeConnectorState(storeContext);
    state = store.getConnectorState(storeContext);
    assertNull(state);
  }

  // Tests getting and setting for a valid connector name and config.
  public void testGetandSetConnectorConfiguration() {
    Properties expectedConfig = new Properties();
    expectedConfig.setProperty("property1", "value1");
    expectedConfig.setProperty("property2", "2");
    expectedConfig.setProperty("property3", "true");
    String connectorName = "connectorA";
    StoreContext storeContext = new StoreContext(connectorName);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    Properties resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, resultConfig);
  }

  // Tests getting and setting a configuration that should encrypt
  // some properties.
  public void testEncryptedConnectorConfiguration() {
    Properties expectedConfig = new Properties();
    expectedConfig.setProperty("property1", "value1");
    expectedConfig.setProperty("property2", "2");
    expectedConfig.setProperty("property3", "true");
    expectedConfig.setProperty("password", "fred");
    expectedConfig.setProperty("PASSWORDS", "fred");
    expectedConfig.setProperty("xyzpasswordzy", "fred");
    String connectorName = "connectorB";
    StoreContext storeContext = new StoreContext(connectorName);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    Properties resultConfig = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, resultConfig);
  }

  // Tests getting configuration for an unknown connector.
  public void testGetConnectorConfiguration() {
    Properties config = store.getConnectorConfiguration(
        new StoreContext("some wierd connector name"));
    // Should return null, not an empty Properties object.
    assertNull(config);
  }

  // Tests configuration cannot be retrieved after removal.
  public void testRemoveConnectorConfiguration() {
    String connectorName = "foo";
    Properties expectedConfig = new Properties();
    expectedConfig.setProperty("property1", "value1");
    expectedConfig.setProperty("property2", "2");
    expectedConfig.setProperty("property3", "true");
    StoreContext storeContext = new StoreContext(connectorName);
    Properties config = store.getConnectorConfiguration(storeContext);
    assertNull(config);
    store.storeConnectorConfiguration(storeContext, expectedConfig);
    config = store.getConnectorConfiguration(storeContext);
    ConnectorTestUtils.compareMaps(expectedConfig, config);
    store.removeConnectorConfiguration(storeContext);
    config = store.getConnectorConfiguration(storeContext);
    assertNull(config);
  }
}
