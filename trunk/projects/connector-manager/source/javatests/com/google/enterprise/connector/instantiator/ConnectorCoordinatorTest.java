// Copyright 2009 Google Inc.
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

import com.google.enterprise.connector.common.I18NUtil;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.pusher.PusherFactory;
import com.google.enterprise.connector.scheduler.LoadManager;
import com.google.enterprise.connector.scheduler.LoadManagerFactory;
import com.google.enterprise.connector.scheduler.HostLoadManager;
import com.google.enterprise.connector.scheduler.MockLoadManagerFactory;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.test.JsonObjectAsMap;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.Map;

/**
 * Unit tests for {@link ConnectorCoordinatorImpl}.
 */
public class ConnectorCoordinatorTest extends TestCase {
  // TODO(strellis): Add tests for batch control operations.
  private static final String TEST_DIR_NAME = "testdata/tempInstantiatorTests";
  private final File baseDirectory = new File(TEST_DIR_NAME);
  private TypeMap typeMap;
  private LoadManagerFactory loadManagerFactory;
  private final PusherFactory pusherFactory = null;
  private final ThreadPool threadPool = null;

  @Override
  protected void setUp() throws Exception {
    assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
    assertTrue(baseDirectory.mkdirs());
    typeMap = new TypeMap("classpath*:config/connectorType.xml", TEST_DIR_NAME);
    typeMap.init();
    loadManagerFactory = new MockLoadManagerFactory();
  }

  @Override
  protected void tearDown() throws Exception {
    assertTrue(ConnectorTestUtils.deleteAllFiles(baseDirectory));
  }

  public void testCreateDestroy() throws Exception {
    final String name = "connector1";
    final ConnectorCoordinatorImpl instance = new ConnectorCoordinatorImpl(
        name, pusherFactory, loadManagerFactory, threadPool);
    assertFalse(instance.exists());
    /*
     * Test creation of a connector of type TestConnectorA. The type should
     * already have been created.
     */
    String typeName = "TestConnectorA";
    String language = "en";
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";
    updateConnectorTest(instance, typeName, language, false, jsonConfigString);
    removeConnector(instance);
  }

  public void testCreateUpdateDestroy() throws Exception {
    final String typeName = "TestConnectorB";
    final String language = "en";
    final String name = "connector2";
    final ConnectorCoordinatorImpl instance = new ConnectorCoordinatorImpl(
        name, pusherFactory, loadManagerFactory, threadPool);
    assertFalse(instance.exists());
    {
      /*
       * Test creation of a connector of type TestConnectorB. The type should
       * already have been created.
       */
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:minty-fresh, "
              + "RepositoryFile:MockRepositoryEventLog3.txt}";
      updateConnectorTest(instance, typeName, language, false,
          jsonConfigString);
    }

    {
      /*
       * Test update of a connector instance of type TestConnectorB. The
       * instance was created in an earlier test.
       */
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:butterscotch, "
              + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instance, typeName, language, true, jsonConfigString);
    }
    removeConnector(instance);
  }

  public void testUpdateType() throws Exception {
    final String language = "en";
    final String name = "connector2";
    final ConnectorCoordinatorImpl instance = new ConnectorCoordinatorImpl(
        name, pusherFactory, loadManagerFactory, threadPool);
    assertFalse(instance.exists());
    {
      /*
       * Test creation of a connector second instance of type TestConnectorB.
       */
      String typeName = "TestConnectorB";
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:chocolate, "
              + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instance, typeName, language, false,
          jsonConfigString);
    }

    {
      InstanceInfo instanceInfo = instance.getInstanceInfo();
      assertTrue(instance.exists());
      // Remember the connector directory for the original type so we can
      // verify that it was removed when we change the type.
      File originalConnectorDir = instanceInfo.getConnectorDir();
      assertTrue(originalConnectorDir.exists());

      /*
       * Test update of a connector instance of type TestConnectorA. The
       * instance was created in an earlier test.
       */
      String typeName = "TestConnectorA";
      String jsonConfigString =
          "{Username:foo, Password:bar, Color:blue, "
              + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instance, typeName, language, true, jsonConfigString);
      assertFalse(originalConnectorDir.exists());
    }
  }

  public void testCreateExising() throws Exception {
    final String name = "connector1";
    final ConnectorCoordinatorImpl instance = new ConnectorCoordinatorImpl(
        name, pusherFactory, loadManagerFactory, threadPool);
    assertFalse(instance.exists());
    /*
     * Test creation of a connector of type TestConnectorA. The type should
     * already have been created.
     */
    String typeName = "TestConnectorA";
    String language = "en";
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";
    updateConnectorTest(instance, typeName, language, false, jsonConfigString);
    try {
      updateConnectorTest(instance, typeName, language, false,
          jsonConfigString);
      fail("Exception expected.");
    } catch (ConnectorExistsException e) {
      // Expected.
    }
    removeConnector(instance);
  }

  public void testUpdateMissing() throws Exception {
    final String name = "connector1";
    final ConnectorCoordinatorImpl instance = new ConnectorCoordinatorImpl(
        name, pusherFactory, loadManagerFactory, threadPool);
    assertFalse(instance.exists());
    /*
     * Test creation of a connector of type TestConnectorA. The type should
     * already have been created.
     */
    String typeName = "TestConnectorA";
    String language = "en";
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";
    try {
      updateConnectorTest(instance, typeName, language, true, jsonConfigString);
      fail("Exception expected.");
    } catch (ConnectorNotFoundException e) {
      // Expected.
    }
  }

  private void updateConnectorTest(ConnectorCoordinatorImpl instance,
      String typeName, String language, boolean update, String jsonConfigString)
      throws JSONException, InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    Map<String, String> config =
        new JsonObjectAsMap(new JSONObject(jsonConfigString));
    Locale locale = I18NUtil.getLocaleFromStandardLocaleString(language);
    ConfigureResponse response = instance.setConnectorConfig(
        typeMap.getTypeInfo(typeName), config, locale, update);
    assertNull(response);
    InstanceInfo instanceInfo = instance.getInstanceInfo();
    File connectorDir = instanceInfo.getConnectorDir();
    assertTrue(connectorDir.exists());
    assertEquals(instance.getConnectorName(), instanceInfo.getName());

    // The password will be decrypted in the InstanceInfo.
    Map<String, String> instanceProps =
        instanceInfo.getConnectorConfiguration().getMap();
    String instancePasswd = instanceProps.get("Password");
    String plainPasswd = config.get("Password");
    assertEquals(instancePasswd, plainPasswd);

    // Verify that the googleConnectorName property is intact.
    assertTrue(
        instanceProps.containsKey(PropertiesUtils.GOOGLE_CONNECTOR_NAME));
    assertEquals(instance.getConnectorName(), instanceProps
        .get(PropertiesUtils.GOOGLE_CONNECTOR_NAME));

    // Verify that the google*WorkDir properties are intact.
    assertTrue(instanceProps.containsKey(PropertiesUtils.GOOGLE_WORK_DIR));
    assertTrue(instanceProps
        .containsKey(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR));
  }

  private void removeConnector(ConnectorCoordinatorImpl instance)
      throws ConnectorNotFoundException {
    InstanceInfo instanceInfo = instance.getInstanceInfo();
    assertTrue(instance.exists());
    File connectorDir = instanceInfo.getConnectorDir();
    assertTrue(connectorDir.exists());
    instance.removeConnector();
    assertFalse(instance.exists());
    assertFalse(connectorDir.exists());
  }
}
