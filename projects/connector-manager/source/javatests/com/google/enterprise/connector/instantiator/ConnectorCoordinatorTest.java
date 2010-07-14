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
import com.google.enterprise.connector.manager.Context;
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
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Locale;
import java.util.Map;

/**
 * Unit tests for {@link ConnectorCoordinatorImpl}.
 */
public class ConnectorCoordinatorTest extends TestCase {
  // TODO(strellis): Add tests for batch control operations.
  private static final String TEST_DIR = "testdata/contextTests/";
  private static final String APPLICATION_CONTEXT =
      "ConnectorCoordinatorTest.xml";

  @Override
  protected void setUp() throws Exception {
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(TEST_DIR + APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);

    getTypeMap().init();
  }

  private ConnectorCoordinatorImpl newCoordinator(String name) {
    Context context = Context.getInstance();
    ConnectorCoordinatorMap ccm =
        (ConnectorCoordinatorMap) context.getRequiredBean(
        "ConnectorCoordinatorMap", ConnectorCoordinatorMap.class);

    return (ConnectorCoordinatorImpl) ccm.getOrAdd(name);
  }

  private TypeMap getTypeMap() {
    Context context = Context.getInstance();
    return (TypeMap) context.getRequiredBean("TypeMap", TypeMap.class);
  }

  public void testCreateDestroy() throws Exception {
    final String name = "connector1";
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
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
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
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
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
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
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
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
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
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

  public void testThreadDeadlock() throws Exception {
    final String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
        + "RepositoryFile:MockRepositoryEventLog3.txt}";
    final ConnectorCoordinatorImpl instance1 =
        createConnector("TestConnectorA", "connector1", jsonConfigString);
    final ConnectorCoordinatorImpl instance2 =
        createConnector("TestConnectorA", "connector2", jsonConfigString);

    checkThreadDeadlock(new ConfigUpdater(instance1, 50),
                        new ConfigUpdater(instance2, 50));
    checkThreadDeadlock(new ScheduleUpdater(instance1, 100),
                        new ScheduleUpdater(instance2, 100));
    checkThreadDeadlock(new CheckpointUpdater(instance1, 100),
                        new CheckpointUpdater(instance2, 100));
    checkThreadDeadlock(new RestartUpdater(instance1, 100),
                        new RestartUpdater(instance2, 100));
    checkThreadDeadlock(new RunOnceUpdater(instance1, 100),
                        new RunOnceUpdater(instance2, 100));
    checkThreadDeadlock(new RestartUpdater(instance1, 100),
                        new RunOnceUpdater(instance1, 100));
  }

  private void checkThreadDeadlock(Updater updater1, Updater updater2)
      throws Exception {
    // Start two threads that repeatedly update the connector instances.
    Thread thread1 = new Thread(updater1, "Updater1");
    Thread thread2 = new Thread(updater2, "Updater2");
    thread1.start();
    thread2.start();

    // Check for thread deadlock and exit if it occurs.
    // TODO: Can we clean up the deadlocked threads?
    ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
    while (thread1.isAlive() && thread2.isAlive()) {
      try { Thread.sleep(500); } catch (InterruptedException e) {}
      long[] ids = tmx.findDeadlockedThreads();
      if (ids != null) {
        ThreadInfo[] infos = tmx.getThreadInfo(ids, true, true);
        // TODO: Use logging instead?
        System.out.println("The following threads are deadlocked:");
        for (ThreadInfo ti : infos) {
          System.out.println(ti);
        }
        throw new RuntimeException("Deadlock");
      }
    }

    if (updater1.getException() != null)
      throw updater1.getException();
    if (updater2.getException() != null)
      throw updater2.getException();
  }

  private abstract class Updater implements Runnable {
    ConnectorCoordinatorImpl coordinator;
    private int iterations;
    private Exception exception;

    public Updater(ConnectorCoordinatorImpl coordinator, int iterations) {
      this.coordinator = coordinator;
      this.iterations = iterations;
    }

    abstract void update() throws Exception;

    public void run() {
      try {
        for (int i = 0; i < iterations; i++) {
          update();
        }
      } catch (Exception e) {
        e.printStackTrace(System.out);
        exception = e;
      }
    }

    public Exception getException() {
      return exception;
    }
  }

  private class ConfigUpdater extends Updater {
    private static final String JSON_CONFIG =
        "{Username:foo, Password:bar, Color:blue, "
        + "RepositoryFile:MockRepositoryEventLog2.txt}";
    private final Map<String, String> config;
    private final TypeInfo typeInfo;

    public ConfigUpdater(ConnectorCoordinatorImpl coordinator, int iterations)
        throws Exception {
      super(coordinator, iterations);
      config = new JsonObjectAsMap(new JSONObject(JSON_CONFIG));
      typeInfo = getTypeMap().getTypeInfo(coordinator.getConnectorTypeName());
    }

    void update() throws Exception {
      coordinator.setConnectorConfig(typeInfo, config, Locale.ENGLISH, true);
    }
  }

  private class ScheduleUpdater extends Updater {
    private final String schedule;

    public ScheduleUpdater(ConnectorCoordinatorImpl coordinator,
                           int iterations) {
      super(coordinator, iterations);
      schedule = coordinator.getConnectorName() + ":0:-1:0-0";
    }

    void update() throws Exception {
      coordinator.setConnectorSchedule(schedule);
    }
  }

  private class CheckpointUpdater extends Updater {
    public CheckpointUpdater(ConnectorCoordinatorImpl coordinator,
                             int iterations) {
      super(coordinator, iterations);
    }

    void update() throws Exception {
      coordinator.setConnectorState("checkpoint");
    }
  }

  private class RestartUpdater extends Updater {
    public RestartUpdater(ConnectorCoordinatorImpl coordinator,
                          int iterations) {
      super(coordinator, iterations);
    }

    void update() throws Exception {
      coordinator.restartConnectorTraversal();
    }
  }

  private class RunOnceUpdater extends Updater {
    private final String schedule;

    public RunOnceUpdater(ConnectorCoordinatorImpl coordinator,
                          int iterations) {
      super(coordinator, iterations);
      schedule = coordinator.getConnectorName() + ":0:-1:0-0";
    }

    void update() throws Exception {
      coordinator.setConnectorSchedule(schedule);
      coordinator.delayTraversal(TraversalDelayPolicy.POLL);
    }
  }

  private ConnectorCoordinatorImpl createConnector(String typeName,
      String name, String jsonConfigString)
      throws JSONException, InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    final String language = "en";
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
    assertFalse(instance.exists());

    updateConnectorTest(instance, typeName, language, false,
          jsonConfigString);
    return instance;
  }

  private void updateConnectorTest(ConnectorCoordinatorImpl instance,
      String typeName, String language, boolean update, String jsonConfigString)
      throws JSONException, InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    Map<String, String> config =
        new JsonObjectAsMap(new JSONObject(jsonConfigString));
    Locale locale = I18NUtil.getLocaleFromStandardLocaleString(language);
    ConfigureResponse response = instance.setConnectorConfig(
        getTypeMap().getTypeInfo(typeName), config, locale, update);
    assertNull((response == null) ? null : response.getMessage(), response);
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
