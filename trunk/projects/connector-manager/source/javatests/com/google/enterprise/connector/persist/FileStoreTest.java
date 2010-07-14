// Copyright 2008 Google Inc.
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

import com.google.enterprise.connector.instantiator.TypeMap;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import java.io.File;

/**
 * Class to test File System persistent store.
 */
public class FileStoreTest extends PersistentStoreTestAbstract {
  private static final String TEST_DIR_NAME = "testdata/tmp/FileStoreTests";
  private final File baseDirectory  = new File(TEST_DIR_NAME);
  private static final String NAME = "test";

  private TypeMap typeMap;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    assertTrue(baseDirectory.mkdirs());
    typeMap = new TypeMap(TEST_DIR_NAME);
    typeMap.init();
    FileStore fileStore = new FileStore();
    fileStore.setTypeMap(typeMap);
    super.store = fileStore;
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
    } finally {
      super.tearDown();
    }
  }

  @Override
  protected StoreContext getStoreContext(String connectorName,
                                         String typeName) {
    File typeDir = new File(typeMap.getTypesDirectory(), typeName);
    File connectorDir = new File(typeDir, connectorName);
    if (!connectorDir.exists()) {
      assertTrue(connectorDir.mkdirs());
    }
    return new StoreContext(connectorName, connectorDir);
  }

  // Tests if the exception is thrown correctly when the connectorDir is null.
  public void testGetConnectorScheduleNullConnectorDir() {
    try {
      store.getConnectorSchedule(new StoreContext(NAME, null));
      fail("failed to throw exception");
    } catch (NullPointerException e) {
      assertEquals("StoreContext.connectorDir may not be null.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is null.
  public void testGetConnectorStateNullConnectorDir() {
    try {
      store.getConnectorState(new StoreContext(NAME, null));
      fail("failed to throw exception");
    } catch (NullPointerException e) {
      assertEquals("StoreContext.connectorDir may not be null.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is null.
  public void testGetConnectorConfigurationNullConnectorDir() {
    try {
      store.getConnectorConfiguration(new StoreContext(NAME, null));
      fail("failed to throw exception");
    } catch (NullPointerException e) {
      assertEquals("StoreContext.connectorDir may not be null.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is
  // nonexistent.
  public void testGetConnectorScheduleNoConnectorDir() {
    StoreContext storeContext =
        new StoreContext(NAME, new File(baseDirectory, "nonexistent"));
    try {
      store.getConnectorSchedule(storeContext);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().startsWith(
          "StoreContext.connectorDir directory must exist"));
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is
  // nonexistent.
  public void testGetConnectorStateNoConnectorDir() {
    StoreContext storeContext =
        new StoreContext(NAME, new File(baseDirectory, "nonexistent"));
    try {
      store.getConnectorState(storeContext);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().startsWith(
          "StoreContext.connectorDir directory must exist"));
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is
  // nonexistent.
  public void testGetConnectorConfigurationNoConnectorDir() {
    StoreContext storeContext =
        new StoreContext(NAME, new File(baseDirectory, "nonexistent"));
    try {
      store.getConnectorConfiguration(storeContext);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().startsWith(
          "StoreContext.connectorDir directory must exist"));
    }
  }

  // Tests connector removal leaves no files behind.
  public void testRemoveConnector() {
    StoreContext storeContext = getStoreContext(NAME, "xyzzy");
    File connectorDir = storeContext.getConnectorDir();
    assertTrue(connectorDir.list().length == 0);
    store.storeConnectorConfiguration(storeContext, getConfiguration());
    store.storeConnectorSchedule(storeContext, getSchedule());
    store.storeConnectorState(storeContext, getCheckpoint());
    // This should have created several storage files.
    assertTrue(connectorDir.list().length == 4);
    store.removeConnectorState(storeContext);
    store.removeConnectorSchedule(storeContext);
    store.removeConnectorConfiguration(storeContext);
    // This should have deleted all the storage files.
    assertTrue(connectorDir.list().length == 0);
  }
}
