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
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.FilesystemConnectorConfigStore;
import com.google.enterprise.connector.persist.MockConnectorStateStore;
import com.google.enterprise.connector.pusher.MockPusher;
import com.google.enterprise.connector.test.JsonObjectAsMap;
import com.google.enterprise.connector.traversal.Traverser;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 
 */
public class SpringInstantiatorTest extends TestCase {

  /**
   * Test method for
   * {@link com.google.enterprise.connector.instantiator.SpringInstantiator
   * #setConnectorConfig(java.lang.String, java.lang.String, java.util.Map)} and
   * {@link com.google.enterprise.connector.instantiator.SpringInstantiator
   * #dropConnector(java.lang.String)}.
   * 
   * @throws IOException
   * @throws JSONException
   * @throws InstantiatorException
   * @throws ConnectorTypeNotFoundException
   * @throws ConnectorNotFoundException
   */
  public final void testSetConnectorConfig() throws IOException, JSONException,
      ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException {

    ConnectorTypeInstantiator connectorTypeInstantiator =
        new SpringConnectorTypeInstantiator();

    SpringConnectorInstantiator connectorInstantiator =
        new SpringConnectorInstantiator();

    // set up a ConnectorConfigStore
    ResourceLoader rl = new FileSystemResourceLoader();
    Resource resource =
        rl.getResource("testdata/dynamicConnectorConfig/connectors");
    File baseDir = resource.getFile();
    FilesystemConnectorConfigStore store = new FilesystemConnectorConfigStore();
    store.setBaseDirectory(baseDir);

    // set up a pusher
    MockPusher pusher = new MockPusher(System.out);

    // set up a ConnectorStateStore
    MockConnectorStateStore css = new MockConnectorStateStore();

    // wire up the dependencies
    connectorInstantiator.setStore(store);
    connectorInstantiator.setConnectorStateStore(css);
    connectorInstantiator.setPusher(pusher);

    Instantiator instantiator =
        new SpringInstantiator(connectorTypeInstantiator, connectorInstantiator);

    {
      // setup the input for this test
      String jsonInput =
          "{\"Repository File\":\"MockRepositoryEventLog1.txt\"}";
      JSONObject jo = new JSONObject(jsonInput);
      Map configKeys = new JsonObjectAsMap(jo);

      String connectorName = "foo";
      String connectorTypeName = "TestConnector1";

      createAndDrop(baseDir, instantiator, configKeys, connectorName,
          connectorTypeName);
    }
    
    {
      // setup the input for this test
      String jsonInput =
          "{\"Repository File\":\"MockRepositoryEventLog2.txt\"}";
      JSONObject jo = new JSONObject(jsonInput);
      Map configKeys = new JsonObjectAsMap(jo);

      String connectorName = "bar";
      String connectorTypeName = "TestConnector2";

      createAndDrop(baseDir, instantiator, configKeys, connectorName,
          connectorTypeName);
    }
  }

  private void createAndDrop(File baseDir, Instantiator instantiator,
      Map configKeys, String connectorName, String connectorTypeName)
      throws ConnectorNotFoundException, ConnectorTypeNotFoundException,
      InstantiatorException {
    File connectorTypeDir = new File(baseDir, connectorTypeName);
    File connectorFile = new File(connectorTypeDir, connectorName + ".xml");

    Assert.assertFalse(connectorFile.exists());

    instantiator.setConnectorConfig(connectorName, connectorTypeName,
        configKeys);

    Traverser t = instantiator.getTraverser(connectorName);

    Assert.assertFalse(t == null);

    Assert.assertTrue(connectorFile.exists());

    instantiator.dropConnector(connectorName);

    try {
      t = instantiator.getTraverser(connectorName);
      fail("exception should have been thrown");
    } catch (ConnectorNotFoundException e) {
      t = null;
    }

    Assert.assertFalse(connectorFile.exists());
  }

}
