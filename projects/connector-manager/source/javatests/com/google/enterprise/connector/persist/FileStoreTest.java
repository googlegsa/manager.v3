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
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storeDir = new File("testdata/tmp/filestore");
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

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorSchedule2() {
    try {
      store.getConnectorSchedule(new StoreContext(null, storeDir));
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorName may not be null or empty.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorState2() {
    try {
      store.getConnectorState(new StoreContext(null, storeDir));
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorName may not be null or empty.",
                   e.getMessage());
    }
  }

  // Tests if the exception is thrown correctly when the connector name is null.
  public void testGetConnectorConfiguration2() {
    StoreContext storeContext = new StoreContext(null, storeDir);
    try {
      store.getConnectorConfiguration(storeContext);
      fail("failed to throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("StoreContext.connectorName may not be null or empty.",
                   e.getMessage());
    }
  }
}
