// Copyright (C) 2006 Google Inc.
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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 */
public class FilesystemConnectorConfigStoreTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.persist.FilesystemConnectorConfigStore
   * #getConnectorNames()}.
   * 
   * @throws IOException
   * @throws PersistentStoreException
   * @throws ConnectorNotFoundException
   */
  public final void testGetConnectorNames() throws IOException,
      ConnectorNotFoundException, PersistentStoreException {
    ResourceLoader rl = new FileSystemResourceLoader();
    Resource resource = rl.getResource("testdata/staticConnectorConfig/connectors");
    File file = resource.getFile();
    FilesystemConnectorConfigStore store = new FilesystemConnectorConfigStore();
    store.setBaseDirectory(file);
    verifyConfigStore(store);

  }

  private void verifyConfigStore(ConnectorConfigStore store)
      throws ConnectorNotFoundException, PersistentStoreException {
    Set connectorNames = new TreeSet();
    for (Iterator i = store.getConnectorNames(); i.hasNext();) {
      String connectorName = (String) i.next();
      connectorNames.add(connectorName);
    }
    Assert.assertTrue(connectorNames.contains("connectorA"));
    Assert.assertTrue(connectorNames.contains("connectorB"));
    Assert.assertTrue(connectorNames.contains("connectorC"));
    Assert.assertTrue(connectorNames.contains("connectorD"));

    Assert.assertFalse(connectorNames.contains("connectorE"));

    getConnectorInfo("connectorA", store);
    getConnectorInfo("connectorB", store);
    getConnectorInfo("connectorC", store);
    getConnectorInfo("connectorD", store);
  }

  private void getConnectorInfo(String connectorName, ConnectorConfigStore store)
      throws ConnectorNotFoundException, PersistentStoreException {
    System.out.println(connectorName + " "
        + store.getConnectorTypeName(connectorName) + " "
        + store.getConnectorResourceString(connectorName));
  }
}
