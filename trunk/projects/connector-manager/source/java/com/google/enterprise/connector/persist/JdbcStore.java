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

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Manage persistence for schedule and state and configuration
 * for a named connector. The persistent store for these data items
 * are columns in a database table, accessed via JDBC.
 */
public class JdbcStore implements PersistentStore {

  private static final Logger LOGGER =
      Logger.getLogger(JdbcStore.class.getName());

  /* SQL Table and Column names */
  static final String TABLE_NAME = "google_connectors";
  static final String ID = "id";
  static final String MODIFY_STAMP = "modify_stamp";
  static final String CONNECTOR_NAME = "connector_name";
  static final String PROPERTY_NAME = "property_name";
  static final String PROPERTY_VALUE = "property_value";

  /* Property Names */
  static final String SCHEDULE = "schedule";
  static final String STATE = "checkpoint";
  static final String TYPE = "configuration_type";
  static final String MAP = "configuration_map";
  static final String XML = "configuration_xml";

  private DataSource dataSource;
  private ConnectionPool connectionPool;

  /* Connector Instance Create Table DDL
   * {0} is TABLE_NAME
   * {1} is ID (PRIMARY KEY)
   * {2} is MODIFY_STAMP
   * {3} is CONNECTOR_NAME
   * {4} is PROPERTY_NAME
   * {5} is PROPERTY_VALUE
   */
  private String createTableDdl = "CREATE TABLE IF NOT EXISTS {0} ( "
      + "{1} INT IDENTITY PRIMARY KEY NOT NULL, {2} INT, "
      + "{3} VARCHAR(64) NOT NULL, "
      + "{4} VARCHAR(64) NOT NULL, {5} VARCHAR NULL )";

  private synchronized void init() {
    if (connectionPool == null) {
      if (dataSource == null) {
        throw new IllegalStateException("Must set dataSource");
      }

      // Create a Pool of JDBC Connections.
      connectionPool = new ConnectionPool(dataSource);

      // Verify that the connector instances table exists.
      verifyTableExists();
    }
  }

  @Override
  public synchronized void finalize() throws Exception {
    if (connectionPool != null) {
      connectionPool.closeConnections();
      connectionPool = null;
    }
  }

  /**
   * Sets the JDBC {@link DataSource} used to access the
   * {@code Connectors} table.
   *
   * @param dataSource a JDBC {@link DataSource}
   */
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    // TODO: Fetch the DataSource description property.
    LOGGER.config("Using JDBC DataSource: " + dataSource.toString());
  }

  /**
   * Sets the ddl statement used for creation of the connector instance table
   * in the database.  The syntax for table creation and data types might
   * vary slightly for different database vendors.
   *
   * @param createTableDdl an SQL statement that creates the connector instance
   * table.
   */
  public void setCreateTableDdl(String createTableDdl) {
    this.createTableDdl = createTableDdl;
  }

  /* @Override */
  public ImmutableMap<StoreContext, ConnectorStamps> getInventory() {
    throw new RuntimeException("TODO");
  }

  /**
   * Retrieves connector schedule.
   *
   * @param context a StoreContext
   * @return connectorSchedule schedule of the corresponding connector.
   */
  /* @Override */
  public Schedule getConnectorSchedule(StoreContext context) {
    String schedule = getField(context, SCHEDULE);
    return (schedule == null) ? null : new Schedule(schedule);
  }

  /**
   * Stores connector schedule.
   *
   * @param context a StoreContext
   * @param connectorSchedule schedule of the corresponding connector.
   */
  /* @Override */
  public void storeConnectorSchedule(StoreContext context,
      Schedule connectorSchedule) {
    String schedule = (connectorSchedule == null)
        ? null : connectorSchedule.toString();
    setField(context, SCHEDULE, schedule);
  }

  /**
   * Remove a connector schedule.
   *
   * @param context a StoreContext
   */
  /* @Override */
  public void removeConnectorSchedule(StoreContext context) {
    storeConnectorSchedule(context, null);
  }

  /**
   * Gets the stored state of a named connector.
   *
   * @param context a StoreContext
   * @return the state, or null if no state has been stored for this connector.
   */
  /* @Override */
  public String getConnectorState(StoreContext context) {
    return getField(context, STATE);
  }

  /**
   * Stores connector state.
   *
   * @param context a StoreContext
   * @param connectorState state of the corresponding connector
   */
  /* @Override */
  public void storeConnectorState(StoreContext context, String connectorState) {
    setField(context, STATE, connectorState);
  }

  /**
   * Remove connector state.
   *
   * @param context a StoreContext
   */
  /* @Override */
  public void removeConnectorState(StoreContext context) {
    storeConnectorState(context, null);
  }

  /**
   * Gets the stored configuration of a named connector.
   *
   * @param context a StoreContext
   * @return the configuration map, or null if no configuration
   *         has been stored for this connector.
   */
  /* @Override */
  public Configuration getConnectorConfiguration(StoreContext context) {
    String config = getField(context, MAP);
    String configXml = getField(context, XML);
    String type = getField(context, TYPE);
    if (type == null && config == null && configXml == null) {
      return null;
    }
    try {
      Properties props = PropertiesUtils.loadFromString(config);
      return new Configuration(type, PropertiesUtils.toMap(props), configXml);
    } catch (PropertiesException e) {
      LOGGER.log(Level.WARNING, "Failed to read connector configuration for "
                 + context.getConnectorName(), e);
      return null;
    }
  }

  /**
   * Stores the configuration of a named connector.
   *
   * @param context a StoreContext
   * @param configuration map to store
   */
  /* @Override */
  public void storeConnectorConfiguration(StoreContext context,
      Configuration configuration) {
    String configMap = null;
    String configXml = null;
    String type = null;
    if (configuration != null) {
      Properties properties = PropertiesUtils.fromMap(configuration.getMap());
      try {
        configMap = PropertiesUtils.storeToString(properties, null);
      } catch (PropertiesException e) {
        LOGGER.log(Level.WARNING, "Failed to store connector configuration for "
                   + context.getConnectorName(), e);
        return;
      }
      configXml = configuration.getXml();
      type = configuration.getTypeName();
    }
    setField(context, TYPE, type);
    setField(context, XML, configXml);
    setField(context, MAP, configMap);
  }

  /**
   * Remove a stored connector configuration.
   *
   * @param context a StoreContext
   */
  /* @Override */
  public void removeConnectorConfiguration(StoreContext context) {
    storeConnectorConfiguration(context, null);
  }

  /**
   * Retrieve a database field value.
   *
   * @param context a StoreContext
   * @param fieldName the name of the field
   * @return String value of the field, or {@code null} if not stored
   */
  private String getField(StoreContext context, String fieldName) {
    try {
      init();
      Connection connection = connectionPool.getConnection();
      String query = "SELECT " + PROPERTY_VALUE + " FROM " + TABLE_NAME
          + " WHERE ( " + CONNECTOR_NAME + " = '" + context.getConnectorName()
          + "' AND " + PROPERTY_NAME + " = '" + fieldName + "' )";
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      try {
        if (rs.next()) {
          return rs.getString(PROPERTY_VALUE);
        }
      } finally {
        rs.close();
        stmt.close();
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve " + fieldName
          + " for connector " + context.getConnectorName(), e);
    }
    return null;
  }

  /**
   * Update a database field value.
   *
   * @param context a StoreContext
   * @param fieldName the name of the field
   * @param fieldValue the value of the field
   */
  private void setField(StoreContext context,
                        String fieldName, String fieldValue) {
    Connection connection = null;
    boolean originalAutoCommit = true;
    try {
      init();
      connection = connectionPool.getConnection();
      originalAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);  // TODO: Does this really need to be a transaction?

      String query = "SELECT * FROM " + TABLE_NAME
          + " WHERE ( " + CONNECTOR_NAME + " = '" + context.getConnectorName()
          + "' AND " + PROPERTY_NAME + " = '" + fieldName + "' )";

      Statement stmt = connection.createStatement(
          ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
      ResultSet rs = stmt.executeQuery(query);

      if (rs.next()) {
        // This connector property exists, update the property value.
        if (fieldValue == null) {
          rs.updateNull(PROPERTY_VALUE);
        } else {
          rs.updateString(PROPERTY_VALUE, fieldValue);
        }
        // Bump the ModifyStamp, so others may know the value has changed.
        rs.updateInt(MODIFY_STAMP, rs.getInt(MODIFY_STAMP) + 1);
        rs.updateRow();
      } else {
        // This connector property does not exist, insert it with new value.
        rs.moveToInsertRow();
        rs.updateInt(MODIFY_STAMP, 1); // Bootstrap the ModifyStamp
        rs.updateString(CONNECTOR_NAME, context.getConnectorName());
        rs.updateString(PROPERTY_NAME, fieldName);
        if (fieldValue == null) {
          rs.updateNull(PROPERTY_VALUE);
        } else {
          rs.updateString(PROPERTY_VALUE, fieldValue);
        }
        rs.insertRow();
      }
      connection.commit();
      rs.close();
      stmt.close();

    } catch (SQLException e) {
      if (connection != null) {
        try {
          connection.rollback();
        } catch (SQLException ignored) {}
      }
      LOGGER.log(Level.WARNING, "Failed to store " + fieldName
          + " for connector " + context.getConnectorName(), e);

    } finally {
      if (connection != null) {
        try {
          connection.setAutoCommit(originalAutoCommit);
          connectionPool.releaseConnection(connection);
        } catch (SQLException ignored) {}
      }
    }
  }

  /**
   * Verify that the GOOGLE_CONNECTORS table exists in the DB.
   * If not, create it.
   */
  private void verifyTableExists() {
    Connection connection = null;
    boolean originalAutoCommit = true;
    try {
      init();
      connection = connectionPool.getConnection();
      originalAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(false);

      // TODO: How can I make this more atomic? If two connectors start up
      // at the same time and try to create the table, one wins and the other
      // throws an exception.

      // Check to see if our connector instance table already exists.
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet tables = metaData.getTables(null, null, TABLE_NAME, null);
      try {
        while (tables.next()) {
          if (TABLE_NAME.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
            LOGGER.config("Found Persistent Store table: " + TABLE_NAME );
            return;
          }
        }
      } finally {
        tables.close();
      }

      // Our table was not found. Create it using the Create Table DDl.
      Statement stmt = connection.createStatement();
      try {
        Object[] params = { TABLE_NAME, ID, MODIFY_STAMP, CONNECTOR_NAME,
                            PROPERTY_NAME, PROPERTY_VALUE };
        String update = MessageFormat.format(createTableDdl, params);
        LOGGER.config("Creating Persistent Store table: \"" + update + "\"");
        stmt.executeUpdate(update);
        connection.commit();
      } finally {
        stmt.close();
      }
    } catch (SQLException e) {
      try {
        connection.rollback();
      } catch (SQLException ignored) {}
      LOGGER.log(Level.WARNING, "Failed to create connector instance table "
          + TABLE_NAME, e);
    } finally {
      if (connection != null) {
        try {
          connection.setAutoCommit(originalAutoCommit);
          connectionPool.releaseConnection(connection);
        } catch (SQLException ignored) {}
      }
    }
  }

  // A Pool of JDBC Connections.
  // TODO: Move this to a utility class?
  private class ConnectionPool {
    private DataSource dataSource;
    private LinkedList<Connection> connections = new LinkedList<Connection>();

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
        conn =  dataSource.getConnection();
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
