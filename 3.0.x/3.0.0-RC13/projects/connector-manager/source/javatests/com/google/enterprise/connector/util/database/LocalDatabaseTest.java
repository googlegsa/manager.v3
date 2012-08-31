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

import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.ConnectorType;
import com.google.enterprise.connector.spi.DatabaseResourceBundle;
import com.google.enterprise.connector.spi.LocalDatabase;
import com.google.enterprise.connector.spi.SpiConstants.DatabaseType;

import org.h2.jdbcx.JdbcDataSource;

import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;

import junit.framework.TestCase;

public class LocalDatabaseTest extends TestCase {

  private DataSource dataSource;
  private JdbcDatabase database;
  private String connectorTypeName;
  private ConnectorType connectorType;
  private LocalDatabase localDatabase;

  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/TestContext.xml";

  @Override
  protected void setUp() throws Exception {
    // Setup the ConnectorType.
    connectorTypeName = "LocalDatabaseConnectorType";
    connectorType = new LocalDatabaseConnectorType();

    // Setup in-memory H2 JDBC DataSource;
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:testdb");
    ds.setUser("sa");
    ds.setPassword("sa");
    dataSource = ds;
    database = new JdbcDatabase(dataSource);

    // Setup LocalDatabase
    localDatabase =
        new LocalDatabaseImpl(database, connectorTypeName, connectorType);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      database.shutdown();
    } finally {
      super.tearDown();
    }
  }

  private TypeMap getTypeMap() {
    Context context = Context.getInstance();
    return (TypeMap) context.getRequiredBean("TypeMap", TypeMap.class);
  }

  // Test getDataSource.
  public void testGetDataSource() throws Exception {
    assertEquals(dataSource, localDatabase.getDataSource());
  }

  // Test getDatabaseType.
  public void testGetDatabaseType() throws Exception {
    assertEquals(DatabaseType.H2, localDatabase.getDatabaseType());
  }

  // Test getDescription
  public void testGetDescription() throws Exception {
    assertTrue(localDatabase.getDescription()
               .startsWith(DatabaseType.H2.toString()));
  }

  // Test auto-generation of ResourceBundle baseName.
  public void testResourceBundleName() throws Exception {
    // Test ResourceBundle baseName is formed from connector type name.
    LocalDatabaseImpl localDb = (LocalDatabaseImpl) localDatabase;
    assertEquals("config." + connectorTypeName + "_sql",
                 localDb.resourceBundleBaseName);

    // Test that underscores in connector type name are preserved when
    // creating ResourceBundle baseName.
    localDb = new LocalDatabaseImpl(database, "Name_With_Underscores",
                                    connectorType);
    assertEquals("config.Name_With_Underscores_sql",
                 localDb.resourceBundleBaseName);

    // Test that periods in connector type name are converted to underscores
    // when creating ResourceBundle baseName.
    localDb = new LocalDatabaseImpl(database, "Name.With.Periods",
                                    connectorType);
    assertEquals("config.Name_With_Periods_sql",
                 localDb.resourceBundleBaseName);
  }

  // Fake ConnectorType.
  private class LocalDatabaseConnectorType implements ConnectorType {
    /* @Override */
    public ConfigureResponse getConfigForm(Locale locale) {
      throw new UnsupportedOperationException("Fake ConnectorType");
    }

    /* @Override */
    public ConfigureResponse getPopulatedConfigForm(
        Map<String, String> configMap, Locale locale) {
      throw new UnsupportedOperationException("Fake ConnectorType");
    }

    /* @Override */
    public ConfigureResponse validateConfig(Map<String, String> configData,
        Locale locale, ConnectorFactory connectorFactory) {
      throw new UnsupportedOperationException("Fake ConnectorType");
    }
  }
}
