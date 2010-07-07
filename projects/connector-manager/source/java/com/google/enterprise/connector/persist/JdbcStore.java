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

import com.google.common.base.Preconditions;
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

  static final String INVENTORY_QUERY =
    "SELECT " + MODIFY_STAMP + "," + CONNECTOR_NAME + "," + PROPERTY_NAME
    + " FROM " + TABLE_NAME + " WHERE ( " + PROPERTY_VALUE + " IS NOT NULL )"
    + " ORDER BY " + CONNECTOR_NAME;

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

  /**
   * Gets the version stamps of all persistent objects.  Reads the entire
   * connector instance table and extracts the MODIFY_STAMPS for all peristed
   * data.
   *
   * @return an immutable map containing the version stamps; may be
   * empty but not {@code null}
   */
  /* @Override */
  public ImmutableMap<StoreContext, ConnectorStamps> getInventory() {
    ImmutableMap.Builder<StoreContext, ConnectorStamps> mapBuilder =
        new ImmutableMap.Builder<StoreContext, ConnectorStamps>();
    try {
      init();
      Connection connection = connectionPool.getConnection();
      try {
        // TODO: We should consider using a PreparedStatement - however this
        // is non-trivial when using connection pools.  Try using
        // MapMaker.makeComputingMap() to map connections to PreparedStatements.

        // TODO: When StoreContext moves to TypeName.  Run two queries:
        //  1) SELECT connector_name, property_value WHERE (property_name =
        //     'configuration_type' AND property_value IS NOT NULL )
        //  2) Build a sorted set of StoreContext with the connector names and
        //     types (ordered by connector_name).
        //  3) SELECT connector_name, modify_stamp, property_name
        //     WHERE ( property_name IN (checkpoint,schedule,configuration_map,
        //     configuration_xml) AND property_value IS NOT NULL ).
        //  4) Throw the results into a hashmap where modify_stamps are
        //     keyed by connector_name,property_name.
        //  5) Iterate over the list of StoreContexts, extracting the
        //     for the connector modify_stamps and adding ConnectorStamps
        //     to the inventory.
        // This gets the typename and avoids ORDER_BY in the SQL queries
        // (at the expense of doing a sort here in the CM).

        Statement statement = connection.createStatement(
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        try {
          ResultSet resultSet = statement.executeQuery(INVENTORY_QUERY);
          if (resultSet.next()) {
            while (processConnector(resultSet, mapBuilder))
              ;
          }
        } finally {
          statement.close();
        }
      } finally {
        connectionPool.releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve Connector Inventory", e);
    }
    return mapBuilder.build();
  }

  /**
   * Reads the MODIFY_STAMPS for all data pertaining to a connector instance,
   *  then adds a ConnectorStamps entry for that connector to the supplied map.
   *
   * @param resultSet ResultSet from connector instance query, ordered by
   *        connectorName.
   * @param mapBuilder Builder for map of ConnectorStamps.
   * @return true if resultSet contains more rows, false otherwise.
   */
  private boolean processConnector(ResultSet resultSet,
      ImmutableMap.Builder<StoreContext, ConnectorStamps> mapBuilder)
      throws SQLException {
    boolean moreRows;
    JdbcStamp scheduleStamp = null;
    JdbcStamp checkpointStamp = null;
    JdbcStamp configurationStamp = null;
    String connectorName = resultSet.getString(CONNECTOR_NAME);
    String type = null;
    do {
      String propName = resultSet.getString(PROPERTY_NAME);
      if (SCHEDULE.equals(propName)) {
        scheduleStamp = new JdbcStamp(resultSet.getLong(MODIFY_STAMP));
      } else if (STATE.equals(propName)) {
        checkpointStamp = new JdbcStamp(resultSet.getLong(MODIFY_STAMP));
      } else if (MAP.equals(propName) || XML.equals(propName)) {
        // ConfigurationStamp is sum of the MAP and XML MODIFY_STAMPs.
        long version =
            (configurationStamp == null) ? 0L : configurationStamp.version;
        version += resultSet.getLong(MODIFY_STAMP);
        configurationStamp = new JdbcStamp(version);
      }
    } while ((moreRows = resultSet.next()) == true  &&
             connectorName.equals(resultSet.getString(CONNECTOR_NAME)));

    if (checkpointStamp != null || scheduleStamp != null
          || configurationStamp != null) {
      ConnectorStamps stamps = new ConnectorStamps(
          checkpointStamp, configurationStamp, scheduleStamp);
      mapBuilder.put(new StoreContext(connectorName), stamps);

      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Found connector: name = " + connectorName
                    + "  stamps = " + stamps);
      }
    }
    return moreRows;
  }

  /**
   * {@inheritDoc}
   *
   * A version stamp based upon the MODIFY_STAMP database field.
   */
  private static class JdbcStamp implements Stamp {
    final long version;

    /** Constructs a File version stamp. */
    JdbcStamp(long version) {
      this.version = version;
    }

    /** {@inheritDoc} */
    /* @Override */
    public int compareTo(Stamp other) {
      return (int) (version - ((JdbcStamp) other).version);
    }

    @Override
    public String toString() {
      return Long.toString(version);
    }
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
    testStoreContext(context);
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
   * Test the StoreContext to make sure it is sane.
   *
   * @param context a StoreContext
   */
  private static void testStoreContext(StoreContext context) {
    Preconditions.checkNotNull(context, "StoreContext may not be null.");
  }

  /**
   * Retrieve a database field value.
   *
   * @param context a StoreContext
   * @param fieldName the name of the field
   * @return String value of the field, or {@code null} if not stored
   */
  private String getField(StoreContext context, String fieldName) {
    testStoreContext(context);
    try {
      init();
      Connection connection = connectionPool.getConnection();
      try {
        String query = "SELECT " + PROPERTY_VALUE + " FROM " + TABLE_NAME
            + " WHERE ( " + CONNECTOR_NAME + " = '" + context.getConnectorName()
            + "' AND " + PROPERTY_NAME + " = '" + fieldName + "' )";
        Statement stmt = connection.createStatement();
        try {
          ResultSet rs = stmt.executeQuery(query);
          if (rs.next()) {
            return rs.getString(PROPERTY_VALUE);
          }
        } finally {
          stmt.close();
        }
      } finally {
        connectionPool.releaseConnection(connection);
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
    testStoreContext(context);
    Connection connection = null;
    boolean originalAutoCommit = true;
    try {
      init();
      connection = connectionPool.getConnection();
      try {
        originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        String query = "SELECT * FROM " + TABLE_NAME
            + " WHERE ( " + CONNECTOR_NAME + " = '" + context.getConnectorName()
            + "' AND " + PROPERTY_NAME + " = '" + fieldName + "' )";

        Statement stmt = connection.createStatement(
            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        try {
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
        } finally {
          stmt.close();
        }
      } catch (SQLException e) {
        try {
          connection.rollback();
        } catch (SQLException ignored) {}
        throw e;
      } finally {
        try {
          connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException ignored) {}
        connectionPool.releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to store " + fieldName
          + " for connector " + context.getConnectorName(), e);
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
      try {
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
        throw e;
      } finally {
        try {
          connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException ignored) {}
        connectionPool.releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to create connector instance table "
                 + TABLE_NAME, e);
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
