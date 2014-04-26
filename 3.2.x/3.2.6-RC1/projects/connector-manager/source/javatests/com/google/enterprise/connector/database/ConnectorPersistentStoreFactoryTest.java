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

import com.google.enterprise.connector.database.FakeDataSource;
import com.google.enterprise.connector.spi.ConnectorPersistentStore;
import com.google.enterprise.connector.spi.LocalDatabase;
import com.google.enterprise.connector.util.database.JdbcDatabase;
import com.google.enterprise.connector.util.database.testing.TestJdbcDatabase;

import junit.framework.TestCase;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Tests ConnectorPersistentStoreFactory.
 */
public class ConnectorPersistentStoreFactoryTest extends TestCase {

  /**
   * Test newConnectorPersistentStore.
   */
  public void testNewConnectorPersistentStore() throws Exception {
    JdbcDatabase database = new TestJdbcDatabase();
    ConnectorPersistentStoreFactory factory =
        new ConnectorPersistentStoreFactory(database);

    ConnectorPersistentStore cpStore =
        factory.newConnectorPersistentStore("test", "TestConnectorA", null);
    assertNotNull(cpStore);

    LocalDatabase localDb = cpStore.getLocalDatabase();
    assertNotNull(localDb);
    assertSame(database.getDataSource(), localDb.getDataSource());
    assertEquals(database.getDatabaseType(), localDb.getDatabaseType());

    assertNull(cpStore.getLocalDocumentStore());
  }

  /**
   * Test FakeDataSource. This will produce a disabled JdbcDatabase.
   * Trying to create a ConnectorPersistentStore based upon a disabled
   * database should throw a SQLException.
   */
  public void testFakeDataSource() throws Exception {
    JdbcDatabase database = new JdbcDatabase(new FakeDataSource("Fake"));
    ConnectorPersistentStoreFactory factory =
        new ConnectorPersistentStoreFactory(database);

    try {
      ConnectorPersistentStore cpStore =
          factory.newConnectorPersistentStore("test", "TestConnectorA", null);
      fail("Expected a SQLException, but got none.");
    } catch (SQLException expected) {
      assertEquals("Fake JDBC DataSource has not been configured.",
                   expected.getMessage());
    }
  }
}
