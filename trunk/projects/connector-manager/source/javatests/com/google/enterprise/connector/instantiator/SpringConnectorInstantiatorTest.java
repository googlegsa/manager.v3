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

package com.google.enterprise.connector.instantiator;

import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.FilesystemConnectorConfigStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.traversal.Traverser;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;

/**
 * Tests for the Spring-base Connector Instantiator
 */
public class SpringConnectorInstantiatorTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.instantiator.SpringConnectorInstantiator#getTraverser(java.lang.String)}.
   * 
   * @throws IOException
   * @throws InstantiatorException
   * @throws ConnectorNotFoundException
   */
  public final void testGetTraverser() throws IOException,
      ConnectorNotFoundException, InstantiatorException {
    SpringConnectorInstantiator inst = new SpringConnectorInstantiator();

    // set up a ConnectorConfigStore
    ResourceLoader rl = new FileSystemResourceLoader();
    Resource resource = rl.getResource("testdata/staticConnectorConfig/connectors");
    File file = resource.getFile();
    FilesystemConnectorConfigStore store = new FilesystemConnectorConfigStore();
    store.setBaseDirectory(file);

    // set up a pusher
    MockPusher pusher = new MockPusher(System.out);

    // set up a ConnectorStateStore
    MockConnectorStateStore css = new MockConnectorStateStore();

    // wire up the dependencies
    inst.setStore(store);
    inst.setConnectorStateStore(css);
    inst.setPusher(pusher);

    verifyTraverser("connectorA", inst);
    verifyTraverser("connectorB", inst);
    verifyTraverser("connectorC", inst);
    verifyTraverser("connectorD", inst);

  }

  private void verifyTraverser(String connectorName,
      ConnectorInstantiator inst) throws ConnectorNotFoundException,
      InstantiatorException {
    Traverser t = inst.getTraverser(connectorName);
    if (t == null) {
      fail("should get a non-null traverser for " + connectorName);
    }
  }
}
