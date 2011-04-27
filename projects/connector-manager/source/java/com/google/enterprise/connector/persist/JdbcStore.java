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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.DatabaseResourceBundle;
import com.google.enterprise.connector.util.database.JdbcDatabase;
import com.google.enterprise.connector.util.database.DatabaseResourceBundleManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  /* Property Names */
  static final String SCHEDULE = "schedule";
  static final String STATE = "checkpoint";
  static final String TYPE = "configuration_type";
  static final String MAP = "configuration_map";
  static final String XML = "configuration_xml";

  static final String RESOURCE_BUNDLE_NAME = "sql.connector-manager.JdbcStore";
  private DatabaseResourceBundle resourceBundle = null;

  // Classloader tailored to test environment.
  private ClassLoader classLoader = null;

  private JdbcDatabase database = null;

  /* Cached SQL Resources */
  private String inventoryStampsQuery;
  private String inventoryTypesQuery;
  private String getValueQuery;
  private String setValueQuery;
  private String connectorNameColumn;
  private String modifyStampColumn;
  private String propertyNameColumn;
  private String propertyValueColumn;

  private synchronized void init() {
    if (resourceBundle != null) {
      return;
    }
    if (database == null) {
      throw new IllegalStateException("Must set JdbcDatabase");
    }

    // Locate our SQL DatabaseResourceBundle.
    DatabaseResourceBundleManager mgr = new DatabaseResourceBundleManager();
    resourceBundle = mgr.getResourceBundle(RESOURCE_BUNDLE_NAME,
        database.getResourceBundleExtension(), classLoader);
    if (resourceBundle == null) {
      // TODO: PersistentStore interface methods should be able to throw PersistentStoreExceptions.
      throw new RuntimeException("Failed to load SQL ResourceBundle "
                                 + RESOURCE_BUNDLE_NAME);
   }

    // Verify that the connector instance table exists.
    String tableName = getResource("table.name");
    if (!database.verifyTableExists(tableName,
         resourceBundle.getStringArray("table.create.ddl"))) {
      // TODO: PersistentStore interface methods should be able to throw PersistentStoreExceptions.
      throw new RuntimeException("Persistent Store Table does not exist "
                                 + tableName);
    }

    // Cache some SQL resources.
    // TODO: These queries should really be PreparedStatements.
    inventoryStampsQuery = getResource("getinventory.stamps.query");
    inventoryTypesQuery = getResource("getinventory.types.query");
    getValueQuery = getResource("getvalue.query");
    setValueQuery = getResource("setvalue.query");

    connectorNameColumn = getResource("column.connector_name");
    modifyStampColumn = getResource("column.modify_stamp");
    propertyNameColumn = getResource("column.property_name");
    propertyValueColumn = getResource("column.property_value");
  }

  /**
   * Sets the JDBC {@link DataSource} used to access the
   * {@code Connectors} table.
   *
   * @param dataBase a JDBC {@link DataSource}
   */
  public void setDatabase(JdbcDatabase dataBase) {
    this.database = dataBase;
  }

  @VisibleForTesting
  public JdbcDatabase getDatabase() {
    return database;
  }

  /* Sets the ClassLoader that will be used to locate SQL Resources. */
  @VisibleForTesting
  void setResourceClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Returns the SQL resource for the supplied key.
   */
  private String getResource(String key) {
    String value = resourceBundle.getString(key);
    if (value == null) {
      LOGGER.log(Level.WARNING, "Failed to resolve SQL resource " + key);
    }
    return value;
  }

  /**
   * Returns {@code true} if the configured {@link JdbcDatabase} is unavailable.
   *
   * @return {@code true} if this PersistentStore is disabled, {@code false}
   * otherwise.
   */
  /* @Override */
  public boolean isDisabled() {
    return (database == null) ? true : database.isDisabled();
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
      Connection connection = database.getConnectionPool().getConnection();
      try {
        // TODO: We should consider using a PreparedStatement - however this
        // is non-trivial when using connection pools.  Try using
        // MapMaker.makeComputingMap() to map connections to PreparedStatements.
        Map<String, Map<String, JdbcStamp>> stampAlbum =
            new HashMap<String, Map<String, JdbcStamp>>();

        // Collect the Stamps for the various interesting properties.
        Statement statement = connection.createStatement(
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        try {
          ResultSet resultSet = statement.executeQuery(inventoryStampsQuery);
          while (resultSet.next()) {
            String connectorName = resultSet.getString(connectorNameColumn);
            Map<String, JdbcStamp> stamps = stampAlbum.get(connectorName);
            if (stamps == null) {
              stamps = new HashMap<String, JdbcStamp>();
              stampAlbum.put(connectorName, stamps);
            }
            stamps.put(resultSet.getString(propertyNameColumn),
                       new JdbcStamp(resultSet.getLong(modifyStampColumn)));
          }
        } finally {
          statement.close();
        }

        // Find all connectors with non-null Type, construct a StoreContext
        // for the connector+type, and build an inventory of that connector's
        // stamps from the previous query.
        // (Connectors with no Type have been deleted.)
        statement = connection.createStatement(
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        try {
          Object[] params = { quoteValue(TYPE) };
          String query = MessageFormat.format(inventoryTypesQuery, params);
          ResultSet resultSet = statement.executeQuery(query);
          while (resultSet.next()) {
            String connectorName = resultSet.getString(connectorNameColumn);
            StoreContext storeContext = new StoreContext(connectorName,
                resultSet.getString(propertyValueColumn));
            Map<String, JdbcStamp> stamps = stampAlbum.get(connectorName);
            ConnectorStamps connectorStamps;
            if (stamps == null) {
              connectorStamps = new ConnectorStamps(null, null, null);
            } else {
              JdbcStamp mapStamp = stamps.get(MAP);
              JdbcStamp xmlStamp = stamps.get(XML);
              JdbcStamp configStamp = new JdbcStamp(
                  ((mapStamp == null) ? 0L : mapStamp.version) +
                  ((xmlStamp == null) ? 0L : xmlStamp.version));
              connectorStamps = new ConnectorStamps(
                  stamps.get(STATE), configStamp, stamps.get(SCHEDULE));
            }
            mapBuilder.put(storeContext, connectorStamps);
            if (LOGGER.isLoggable(Level.FINE)) {
              LOGGER.fine("Found connector: name = " + connectorName
                          + "  type = " + storeContext.getTypeName()
                          + "  stamps = " + connectorStamps);
            }
          }
        } finally {
          statement.close();
        }
      } finally {
        database.getConnectionPool().releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to retrieve Connector Inventory", e);
    }
      // Finally, construct the inventory.
    return mapBuilder.build();
  }

  /**
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
   * Quotes the supplied value using single qoutes.  MessageFormat
   * considers embedded single-quotes special, and doesn't do
   * substitutions within them.  Unfortunately, this is exactly
   * where we want to use substitutions: in SQL queries like:
   * {@code ... WHERE ( connector_name='{0}' ...}.
   * <p>
   * One solution is to add the quote characters to the value being
   * substituted in (the purpose of this method).  Another solution would
   * be to avoid MessageFormat, possibly trying PreparedStatement syntax.
   */
  // TODO: Use PreparedStatements.
  private String quoteValue(String value) {
    return "'" + value.replace("'", "''") + "'";
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
      Connection connection = database.getConnectionPool().getConnection();
      try {
        Object[] params =
            { quoteValue(context.getConnectorName()), quoteValue(fieldName) };
        String query = MessageFormat.format(getValueQuery, params);
        Statement stmt = connection.createStatement();
        try {
          ResultSet rs = stmt.executeQuery(query);
          if (rs.next()) {
            return rs.getString(propertyValueColumn);
          }
        } finally {
          stmt.close();
        }
      } finally {
        database.getConnectionPool().releaseConnection(connection);
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
      connection = database.getConnectionPool().getConnection();
      try {
        originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        Object[] params =
            { quoteValue(context.getConnectorName()), quoteValue(fieldName) };
        String query = MessageFormat.format(setValueQuery, params);
        Statement stmt = connection.createStatement(
            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        try {
          ResultSet rs = stmt.executeQuery(query);
          if (rs.next()) {
            // This connector property exists, update the property value.
            if (fieldValue == null) {
              rs.updateNull(propertyValueColumn);
            } else {
              rs.updateString(propertyValueColumn, fieldValue);
            }
            // Bump the ModifyStamp, so others may know the value has changed.
            rs.updateInt(modifyStampColumn, rs.getInt(modifyStampColumn) + 1);
            rs.updateRow();
          } else {
            // This connector property does not exist, insert it with new value.
            rs.moveToInsertRow();
            rs.updateInt(modifyStampColumn, 1); // Bootstrap the ModifyStamp
            rs.updateString(connectorNameColumn, context.getConnectorName());
            rs.updateString(propertyNameColumn, fieldName);
            if (fieldValue == null) {
              rs.updateNull(propertyValueColumn);
            } else {
              rs.updateString(propertyValueColumn, fieldValue);
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
        database.getConnectionPool().releaseConnection(connection);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARNING, "Failed to store " + fieldName
          + " for connector " + context.getConnectorName(), e);
    }
  }
}
