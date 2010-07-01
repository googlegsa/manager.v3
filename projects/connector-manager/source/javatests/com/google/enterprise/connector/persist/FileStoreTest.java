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

import com.google.enterprise.connector.test.ConnectorTestUtils;

import java.io.File;

/**
 * Class to test File System persistent store.
 */
public class FileStoreTest extends PersistentStoreTestAbstract {
  static final String NAME = "test";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storeDir = new File("testdata/tmp/filestore/" + TYPENAME + "/" + NAME);
    assertTrue(storeDir.mkdirs());
    store = new FileStore();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      assertTrue(ConnectorTestUtils.deleteAllFiles(storeDir));
    } finally {
      super.tearDown();
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is null.
  public void testGetConnectorSchedule3() {
    try {
      store.getConnectorSchedule(new StoreContext(NAME, null));
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorDir may not be null.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is null.
  public void testGetConnectorState3() {
    try {
      store.getConnectorState(new StoreContext(NAME, null));
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorDir may not be null.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is null.
  public void testGetConnectorConfiguration3() {
    StoreContext storeContext = new StoreContext(NAME, null);
    try {
      store.getConnectorConfiguration(storeContext);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorDir may not be null.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is nonexistent.
  public void testGetConnectorSchedule4() {
    StoreContext storeContext =
        new StoreContext(NAME, new File(storeDir, "nonexistent"));
    try {
      store.getConnectorSchedule(storeContext);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorDir directory must exist.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is nonexistent.
  public void testGetConnectorState4() {
    StoreContext storeContext =
        new StoreContext(NAME, new File(storeDir, "nonexistent"));
    try {
      store.getConnectorState(storeContext);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorDir directory must exist.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connectorDir is nonexistent.
  public void testGetConnectorConfiguration4() {
    StoreContext storeContext =
        new StoreContext(NAME, new File(storeDir, "nonexistent"));
    try {
      store.getConnectorConfiguration(storeContext);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorDir directory must exist.",
                   e.getMessage());
    }
  }

  // Tests connector removal leaves no files behind.
  public void testRemoveConnector() {
    StoreContext storeContext = new StoreContext(NAME, storeDir);
    assertTrue(storeDir.list().length == 0);
    store.storeConnectorConfiguration(storeContext, getConfiguration());
    store.storeConnectorSchedule(storeContext, getSchedule());
    store.storeConnectorState(storeContext, getCheckpoint());
    // This should have created several storage files.
    assertTrue(storeDir.list().length == 4);
    store.removeConnectorState(storeContext);
    store.removeConnectorSchedule(storeContext);
    store.removeConnectorConfiguration(storeContext);
    // This should have deleted all the storage files.
    assertTrue(storeDir.list().length == 0);
  }
}
