// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.common.I18NUtil;
import com.google.enterprise.connector.manager.ConnectorManagerException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.persist.MockPersistentStore;
import com.google.enterprise.connector.persist.PersistentStore;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.test.JsonObjectAsMap;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

/**
 * Unit test for {@link SpringInstantiator}.
 */
public class InstantiatorTest extends TestCase {
  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/InstantiatorTest.xml";

  private static final MockPersistentStore persistentStore =
      new MockPersistentStore();

  private SpringInstantiator instantiator;

  @Override
  protected void setUp() throws Exception {
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    persistentStore.clear();
    instantiator = createInstantiator();
    assertEquals(0, connectorCount());
  }

  private SpringInstantiator createInstantiator() {
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    SpringInstantiator si = (SpringInstantiator) context.getRequiredBean(
        "Instantiator", SpringInstantiator.class);
    si.init();
    return si;
  }

  /**
   * Returns the static MockPersistentStore instance.  Used by
   * Spring to wire that static Persistent store into the context.
   */
  public static final PersistentStore getPersistentStore() {
    return persistentStore;
  }

  /**
   * Test method for adding, updating, deleting connectors.
   */
  public final void testAddUpdateDelete() throws Exception {
    {
      /*
       * Test creation of a connector of type TestConnectorA.
       * The type should already have been created.
       */
      String name = "connector1";
      String typeName = "TestConnectorA";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Color:red, "
          + "RepositoryFile:MockRepositoryEventLog3.txt}";
      updateConnectorTest(instantiator, name, typeName, language,
                          false, jsonConfigString);
    }

    {
      /*
       * Test creation of a connector of type TestConnectorB.
       * The type should already have been created.
       */
      String name = "connector2";
      String typeName = "TestConnectorB";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:minty-fresh, "
          + "RepositoryFile:MockRepositoryEventLog3.txt}";
      updateConnectorTest(instantiator, name, typeName, language,
                          false, jsonConfigString);
    }

    assertEquals(2, connectorCount());

    {
      /*
       * Test update of a connector instance of type TestConnectorB.
       * The instance was created in an earlier test.
       */
      String name = "connector2";
      String typeName = "TestConnectorB";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:butterscotch, "
          + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instantiator, name, typeName, language,
                          true, jsonConfigString);
    }

    assertEquals(2, connectorCount());

    {
      /*
       * Test creation of a connector second instance of type TestConnectorB.
       */
      String name = "connector3";
      String typeName = "TestConnectorB";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:chocolate, "
          + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instantiator, name, typeName, language,
                          false, jsonConfigString);
    }

    assertEquals(3, connectorCount());

    {
      /*
       * Test update of a connector instance of type TestConnectorB, to type
       * TestConnectorA. The instance was created in an earlier test.
       */
      String name = "connector3";
      String typeName = "TestConnectorA";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Color:blue, "
          + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instantiator, name, typeName, language,
                          true, jsonConfigString);
    }

    assertEquals(3, connectorCount());

    {
      /*
       * Test create of an existing connector instance of type TestConnectorA.
       * It should throw a ConnectorExistsException.
       */
      String name = "connector2";
      String typeName = "TestConnectorA";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:butterscotch, "
          + "RepositoryFile:MockRepositoryEventLog2.txt}";
      try {
        updateConnectorTest(instantiator, name, typeName, language,
                            false, jsonConfigString);
      } catch (ConnectorExistsException e) {
        assertTrue(true);
      } catch (ConnectorManagerException e) {
        assertTrue(false);
      }
    }

    assertEquals(3, connectorCount());

    {
      /*
       * Test update of a non-existing connector instance of type
       * TestConnectorB. It should throw a ConnectorNotFoundException.
       */
      String name = "connectorNew";
      String typeName = "TestConnectorB";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:butterscotch, "
          + "RepositoryFile:MockRepositoryEventLog2.txt}";
      try {
        updateConnectorTest(instantiator, name, typeName, language,
                            true, jsonConfigString);
      } catch (ConnectorNotFoundException e) {
        assertTrue(true);
      } catch (ConnectorManagerException e) {
        assertTrue(false);
      }
    }

    assertEquals(3, connectorCount());

    /*
     * Test dropping connectors.  Once dropped, I should not be able to
     * get items from its interface.  Regression test for Issue 60.
     */
    instantiator.removeConnector("connector1");
    try {
      AuthorizationManager authz =
          instantiator.getAuthorizationManager("connector1");
      assertNull(authz);
    } catch (ConnectorNotFoundException e1) {
      assertTrue(true);
    }
    assertFalse(connectorExists("connector1"));

    instantiator.removeConnector("connector2");
    try {
      AuthenticationManager authn =
          instantiator.getAuthenticationManager("connector2");
      assertNull(authn);
    } catch (ConnectorNotFoundException e2) {
      assertTrue(true);
    }
    assertFalse(connectorExists("connector2"));

    instantiator.removeConnector("connector3");
    try {
      instantiator.getConnectorCoordinator("connector3").getTraversalManager();
      fail("Exception expected.");
    } catch (ConnectorNotFoundException e3) {
      assertEquals("Connector instance connector3 not available.",
          e3.getMessage());
    }
    assertFalse(connectorExists("connector3"));

    assertEquals(0, connectorCount());
  }

  /**
   * Test method for adding, updating, deleting connectors with
   * connectorInstance.xml.
   */
  public final void testAddUpdateDeleteConnectorInstanceXml() throws Exception {
    {
      /*
       * Test creation of a connector of type TestConnectorA.
       * The type should already have been created.
       */
      String name = "connector100";
      String typeName = "TestConnectorA";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Color:red, "
          + "RepositoryFile:MockRepositoryEventLog3.txt}";
      updateConnectorTest(instantiator, name, language, false,
          new Configuration(typeName, getConfigMap(jsonConfigString),
          instantiator.getConnectorInstancePrototype(typeName)));
    }

    {
      /*
       * Test creation of a connector of type TestConnectorB.
       * The type should already have been created.
       */
      String name = "connector200";
      String typeName = "TestConnectorB";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:minty-fresh, "
          + "RepositoryFile:MockRepositoryEventLog3.txt}";
      updateConnectorTest(instantiator, name, language, false,
          new Configuration(typeName, getConfigMap(jsonConfigString),
          instantiator.getConnectorInstancePrototype(typeName)
          .replace("TestConnector", "ModifiedTestConnector")));
    }

    assertEquals(2, connectorCount());

    {
      /*
       * Test update of a connector instance of type TestConnectorB.
       * The instance was created in an earlier test.
       */
      String name = "connector200";
      String typeName = "TestConnectorB";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:butterscotch, "
          + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instantiator, name, language, true,
          new Configuration(typeName, getConfigMap(jsonConfigString),
          instantiator.getConnectorInstancePrototype(typeName)
          .replace("TestConnector", "UpdatedTestConnector")));
    }

    assertEquals(2, connectorCount());

    {
      /*
       * Test update of a connector instance of type TestConnectorB
       * without specifying a connectorInstance.xml.  Should use the
       * last one saved.
       */
      String name = "connector200";
      String typeName = "TestConnectorB";
      String language = "en";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:wild-cherry, "
          + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instantiator, name, language, true,
          new Configuration(typeName, getConfigMap(jsonConfigString), null));

      Configuration config = instantiator.getConnectorConfiguration(name);
      assertNotNull(config);
      assertNotNull(config.getXml());
      assertTrue(config.getXml().contains("UpdatedTestConnector"));
    }

    assertEquals(2, connectorCount());

    /*
     * Test dropping connectors.  Once dropped, I should not be able to
     * get items from its interface.  Regression test for Issue 60.
     */
    instantiator.removeConnector("connector100");
    try {
      AuthorizationManager authz =
          instantiator.getAuthorizationManager("connector100");
      assertNull(authz);
    } catch (ConnectorNotFoundException e1) {
      assertTrue(true);
    }
    assertFalse(connectorExists("connector100"));

    instantiator.removeConnector("connector200");
    try {
      AuthenticationManager authn =
          instantiator.getAuthenticationManager("connector200");
      assertNull(authn);
    } catch (ConnectorNotFoundException e2) {
      assertTrue(true);
    }
    assertFalse(connectorExists("connector200"));

    assertEquals(0, connectorCount());
  }

  private class Issue63ChildThread extends Thread {
    volatile boolean didFinish = false;
    volatile Exception myException;
    @Override
    public void run() {
      try {
        // Get the Traverser for our connector instance.
        TraversalManager oldTraversalManager =
            instantiator.getConnectorCoordinator("connector1")
                .getTraversalManager();
        TraversalManager newTraversalManager =
            instantiator.getConnectorCoordinator("connector1")
                .getTraversalManager();
        if (oldTraversalManager != newTraversalManager) {
          throw new Exception("oldTraverser = " + oldTraversalManager
              + " must match newTraverser = " + newTraversalManager);
        }

        // Sleep for a bit, allowing the test thread time
        // to update the connector.
        Thread.sleep(200);

        // Get the Traverser for our connector instance.
        // It should be a new traverser reflecting the updated connector.
        newTraversalManager =
          instantiator.getConnectorCoordinator("connector1")
              .getTraversalManager();
        if (oldTraversalManager == newTraversalManager) {
          throw new Exception("oldTraverser = " + oldTraversalManager
              + " must match newTraverser = " + newTraversalManager);
        }
      } catch (Exception e) {
        myException = e;
      }
      didFinish = true;
    }
  }

  public final void testStartWithConnector() throws Exception {
    final String name = "connector1";
    final String typeName = "TestConnectorA";
    final String language = "en";

    {
      /*
       * Test creation of a connector of type TestConnectorA.
       * The type should already have been created.
       */
      String jsonConfigString =
          "{Username:foo, Password:bar, Color:red, "
          + "RepositoryFile:MockRepositoryEventLog3.txt}";
      updateConnectorTest(instantiator, name, typeName, language,
                          false, jsonConfigString);
      instantiator.shutdown(true, 5000);
    }

    SpringInstantiator newInstantiator = createInstantiator();
    assertNotSame(instantiator, newInstantiator);

    {
      String readTypeName = newInstantiator.getConnectorTypeName(name);
      assertEquals(typeName, readTypeName);
    }
  }

  /**
   * Tests the synchronization problems that surfaced with Issue 63.
   *
   * @throws JSONException
   * @throws InstantiatorException
   * @throws ConnectorExistsException
   * @throws ConnectorNotFoundException
   */
  public final void testIssue63Synchronization() throws Exception {
    // Create a connector.
    String name = "connector1";
    String typeName = "TestConnectorA";
    String language = "en";
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
        + "RepositoryFile:MockRepositoryEventLog3.txt}";
    updateConnectorTest(instantiator, name, typeName, language,
                        false, jsonConfigString);

    // Spawn a child thread that looks up the connector.
    Issue63ChildThread child = new Issue63ChildThread();
    child.start();

    // Sleep for a bit, allowing the child to fetch the connector interface.
    try {
      Thread.sleep(100);
    } catch (InterruptedException ie) {
      fail("Unexpected thread interruption.");
    }

    // Update the connector with a new color config.
    String newJsonConfigString =
        "{Username:foo, Password:bar, Color:blue, "
        + "RepositoryFile:MockRepositoryEventLog3.txt}";
    updateConnectorTest(instantiator, name, typeName, language,
                        true, newJsonConfigString);

    // Join with the child thread, allowing it to finish.
    try {
      child.join();
    } catch (InterruptedException e) {
      fail("Unexpected thread interruption.");
    }
    assertTrue(child.didFinish);
    if (child.myException != null) {
      throw new Exception("Unexpected exception in child.", child.myException);
    }

    instantiator.removeConnector(name);
  }

  /*
   * Returns the count of connectors in the InstanceMap.
   */
  private int connectorCount() {
    return instantiator.getConnectorNames().size();
  }

  /*
   * Returns true if a connector exists in the InstanceMap,
   * false otherwise.
   */
  private boolean connectorExists(String connectorName) {
    for (String name : instantiator.getConnectorNames()) {
      if (connectorName.equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  /*
   * Create a configMap from a JSON string.
   */
  private Map<String, String> getConfigMap(String jsonConfigString)
      throws JSONException {
    return new JsonObjectAsMap(new JSONObject(jsonConfigString));
  }

  private void updateConnectorTest(SpringInstantiator instantiator, String name,
      String typeName, String language, boolean update, String jsonConfigString)
      throws JSONException, InstantiatorException, ConnectorExistsException,
             ConnectorNotFoundException, ConnectorTypeNotFoundException {
    updateConnectorTest(instantiator, name, language, update,
      new Configuration(typeName, getConfigMap(jsonConfigString), null));
  }

  private void updateConnectorTest(SpringInstantiator instantiator,
      String name, String language, boolean update, Configuration config)
      throws JSONException, InstantiatorException, ConnectorExistsException,
             ConnectorNotFoundException, ConnectorTypeNotFoundException {
    TraversalManager oldTraversersalManager = null;
    if (update) {
      oldTraversersalManager =
          instantiator.getConnectorCoordinator(name).getTraversalManager();
    }
    Locale locale = I18NUtil.getLocaleFromStandardLocaleString(language);
    instantiator.setConnectorConfiguration(name, config, locale, update);

    // Make sure that this connector now exists.
    assertTrue(connectorExists(name));

    // Make sure that this connector has the correct type associated.
    assertEquals(config.getTypeName(), instantiator.getConnectorTypeName(name));

    AuthorizationManager authz = instantiator.getAuthorizationManager(name);
    assertNotNull(authz);

    AuthenticationManager authn = instantiator.getAuthenticationManager(name);
    assertNotNull(authn);

    TraversalManager traversalManager =
        instantiator.getConnectorCoordinator(name).getTraversalManager();
    assertNotNull(traversalManager);

    // If this is an update, make sure that we get a different traverser.
    // Regression test for Issues 35, 63.
    if (update) {
      assertNotSame(oldTraversersalManager, traversalManager);
    }

    Configuration newConfig = instantiator.getConnectorConfiguration(name);

    // the password will be decrypted in the InstanceInfo
    Map<String, String> instanceProps = newConfig.getMap();
    String instancePasswd = instanceProps.get("Password");
    String plainPasswd = config.getMap().get("Password");
    assertEquals(instancePasswd, plainPasswd);

    // Check if the configuration properties match those set.
    ConnectorTestUtils.compareMaps(config.getMap(), instanceProps);

    // Creating a new connector with no connectorInstancePrototype should
    // get you the default.
    if (!update && config.getXml() == null) {
      assertEquals(instantiator.getConnectorInstancePrototype(
                   config.getTypeName()), newConfig.getXml());
    }

    // If a connectorInstancePrototype was supplied, make sure it was
    // saved with the connector instance.
    if (config.getXml() != null) {
      assertEquals(config.getXml(), newConfig.getXml());
    }
  }
}
