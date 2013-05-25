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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.database.ConnectorPersistentStoreFactory;
import com.google.enterprise.connector.database.FakeDataSource;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.util.database.JdbcDatabase;
import com.google.enterprise.connector.util.database.testing.TestJdbcDatabase;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.HashMap;

public class ConnectorInstanceFactoryTest extends AbstractTestInstanceInfo {

  private ConnectorInstanceFactory factory;
  private ConnectorPersistentStoreFactory cpsFactory;

  protected void setUp() throws Exception {
    super.setUp();
    factory = null;
    cpsFactory = null;
  }

  @Override
  protected Connector newInstance(String connectorName, String connectorDir,
      TypeInfo typeInfo, Configuration configuration) throws Exception {
    factory = new ConnectorInstanceFactory(
        connectorName, typeInfo, configuration, cpsFactory);
    try {
      Connector connector = factory.makeConnector(configuration.getMap());
      assertNotNull(connector);
      assertTrue(factory.connectors.contains(connector));
      return connector;
    } catch (RepositoryException e) {
      // ConnectorInstanceFactory wraps exceptions in a RepositoryException,
      // so rethrow the original cause.
      if ((e.getCause() != null) && (e.getCause() instanceof Exception)) {
        throw (Exception) e.getCause();
      } else {
        throw e;
      }
    }
  }

  /** Test invalid constructor arguments. */
  public void testConstructorArgs() throws Exception {
    String connectorName = "fred";
    String connectorDir = "testdata/connectorInstanceTests/positive";
    String resourceName = "testdata/connectorTypeTests/positive/connectorType.xml";
    TypeInfo typeInfo = makeTypeInfo(resourceName);
    Configuration config =
        readConfiguration(connectorName, connectorDir, typeInfo);

    ConnectorInstanceFactory factory;
    try {
      factory = new ConnectorInstanceFactory(
          null, typeInfo, config, null);
      fail("Expected NullPointerException, but none was thrown.");
    } catch (NullPointerException expected) {
      assertEquals("connectorName must not be null", expected.getMessage());
    }

    try {
      factory = new ConnectorInstanceFactory(
          connectorName, null, config, null);
      fail("Expected NullPointerException, but none was thrown.");
    } catch (NullPointerException expected) {
      assertEquals("typeInfo must not be null", expected.getMessage());
    }

    try {
      factory = new ConnectorInstanceFactory(
          connectorName, typeInfo, null, null);
      fail("Expected NullPointerException, but none was thrown.");
    } catch (NullPointerException expected) {
      assertEquals("configuration must not be null", expected.getMessage());
    }

    try {
      factory = new ConnectorInstanceFactory(connectorName, typeInfo,
          new Configuration("foo", config.getMap(), config.getXml()), null);
      fail("Expected IllegalArgumentException, but none was thrown.");
    } catch (IllegalArgumentException expected) {
      assertEquals("TypeInfo must match Configuration type",
                   expected.getMessage());
    }
  }

  private SimpleTestConnector makeSimpleConnector() throws Exception {
    Connector connector = fromDirectoryTest("fred",
        "testdata/connectorInstanceTests/default",
        "testdata/connectorTypeTests/default/connectorType.xml",
        null, null);

    assertTrue("Connector should be of type SimpleTestConnector",
        connector instanceof SimpleTestConnector);
   return (SimpleTestConnector) connector;
  }

  /** Test a supplied null or modified config map. */
  @SuppressWarnings("unchecked")
  public void testSuppliedConfig() throws Exception {
    SimpleTestConnector connector = makeSimpleConnector();
    assertEquals("not_default_user",
                 factory.originalConfig.getMap().get("Username"));
    assertEquals("not_default_user", connector.getUsername());

    // If no config map is supplied the original one is used.
    connector = (SimpleTestConnector) factory.makeConnector(null);
    assertEquals("not_default_user", connector.getUsername());

    // Supply a modified config should override original.
    HashMap<String, String> config =
        new HashMap<String, String>(factory.originalConfig.getMap());
    config.put("Username", "xyzzy");
    connector = (SimpleTestConnector) factory.makeConnector(config);
    assertEquals("xyzzy", connector.getUsername());

    // We have created 3 connector instances.
    assertEquals(3, factory.connectors.size());
  }

  /** Test the shutdown method. */
  public void testShutdown() throws Exception {
    SimpleTestConnector connector = makeSimpleConnector();

    assertEquals(1, factory.connectors.size());
    assertFalse(connector.isShutdown());
    assertFalse(connector.isDeleted());

    factory.shutdown();
    assertEquals(0, factory.connectors.size());
    assertTrue(connector.isShutdown());
  }

  /** Test databaseAccess. */
  public void testDatabaseAccess() throws Exception {
    cpsFactory = new ConnectorPersistentStoreFactory(new TestJdbcDatabase());
    SimpleTestConnector connector = makeSimpleConnector();

    // Make sure we don't actually give transient connectors access to the DB.
    assertNull(connector.getDatabaseAccess());
  }

  /** Test broken databaseAccess. */
  public void testBadDatabaseAccess() throws Exception {
    cpsFactory = new ConnectorPersistentStoreFactory(
        new JdbcDatabase(new FakeDataSource("Fake")));

    fromDirectoryTest("fred",
        "testdata/connectorInstanceTests/default",
        "testdata/connectorTypeTests/default/connectorType.xml",
        SQLException.class, "Fake JDBC DataSource has not been configured.");
  }
}
