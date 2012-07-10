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

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.DocumentAcceptor;
import com.google.enterprise.connector.spi.Lister;
import com.google.enterprise.connector.spi.MockConnector;
import com.google.enterprise.connector.spi.MockConnectorType;
import com.google.enterprise.connector.test.ConnectorTestUtils;
import com.google.enterprise.connector.test.JsonObjectAsMap;
import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
import com.google.enterprise.connector.util.filter.AbstractDocumentFilter;
import com.google.enterprise.connector.util.filter.DocumentFilterChain;
import com.google.enterprise.connector.util.filter.DocumentFilterFactory;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.Resource;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Unit tests for {@link ConnectorCoordinatorImpl}.
 */
public class ConnectorCoordinatorTest extends TestCase {
  // TODO(strellis): Add tests for batch control operations.
  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/TestContext.xml";

  private static final String BEANS_PREFIX =
     "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
     + "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" "
     + "\"http://www.springframework.org/dtd/spring-beans.dtd\">\n"
     + "<beans>\n";
  private static final String BEANS_POSTFIX = "</beans>\n";

  @Override
  protected void setUp() throws Exception {
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(APPLICATION_CONTEXT,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    getTypeMap().init();
  }

  private ConnectorCoordinatorImpl newCoordinator(String name) {
    return (ConnectorCoordinatorImpl) getCoordinatorMap()
        .getOrAdd(name);
  }

  private ConnectorCoordinatorMap getCoordinatorMap() {
    Context context = Context.getInstance();
    return (ConnectorCoordinatorMap) context.getRequiredBean(
        "ConnectorCoordinatorMap", ConnectorCoordinatorMap.class);
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
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";
    updateConnectorTest(instance, typeName, false, jsonConfigString);
    removeConnector(instance);
  }

  public void testCreateUpdateDestroy() throws Exception {
    final String typeName = "TestConnectorB";
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
      updateConnectorTest(instance, typeName, false, jsonConfigString);
    }

    {
      /*
       * Test update of a connector instance of type TestConnectorB. The
       * instance was created in an earlier test.
       */
      String jsonConfigString =
          "{Username:foo, Password:bar, Flavor:butterscotch, "
              + "RepositoryFile:MockRepositoryEventLog2.txt}";
      updateConnectorTest(instance, typeName, true, jsonConfigString);
    }
    removeConnector(instance);
  }


  public void testCreateWithOutConfigXml() throws Exception {
    final String name = "connector1";
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
    assertFalse(instance.exists());

    String typeName = "TestConnectorA";
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";
    // Has knowledge that updateConnectorTest passes null for configXml.
    updateConnectorTest(instance, typeName, false, jsonConfigString);

    // Test that the Configuration.configXml returned either contains null or
    // the connectorInstancePrototype for this connectorType.  Either is OK.
    Configuration configuration = instance.getConnectorConfiguration();
    if (configuration.getXml() != null) {
      TypeInfo typeInfo = getTypeMap().getTypeInfo(typeName);
      Resource resource = typeInfo.getConnectorInstancePrototype();
      assertNotNull(resource);
      assertEquals(configuration.getXml(),
                   StringUtils.streamToString(resource.getInputStream()));
    }

    removeConnector(instance);
  }

  public void testCreateWithConfigXml() throws Exception {
    final String name = "connector1";
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
    assertFalse(instance.exists());

    String typeName = "TestConnectorA";
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";

    // Create a modified connectorInstance.xml.
    TypeInfo typeInfo = getTypeMap().getTypeInfo(typeName);
    Resource resource = typeInfo.getConnectorInstancePrototype();
    String configXml = StringUtils.streamToString(resource.getInputStream());

    Configuration configuration = new Configuration(typeName,
        new JsonObjectAsMap(new JSONObject(jsonConfigString)),
        configXml.replace("TestConnectorAInstance",
                          "NewTestConnectorAInstance"));

    // This knows that updateConnectorTest passes null for configXml.
    updateConnectorTest(instance, configuration, false);

    // Test that the Configuration.configXml returned either contains null or
    // the connectorInstancePrototype for this connectorType.  Either is OK.
    Configuration storedConfiguration = instance.getConnectorConfiguration();
    assertNotNull(storedConfiguration.getXml());
    assertEquals(configuration.getXml(), storedConfiguration.getXml());

    removeConnector(instance);
  }

  public void testUpdateWithConfigXml() throws Exception {
    final String name = "connector1";
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
    assertFalse(instance.exists());

    String typeName = "TestConnectorA";
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";

    Configuration configuration = new Configuration(typeName,
        new JsonObjectAsMap(new JSONObject(jsonConfigString)), null);

    // This knows that updateConnectorTest passes null for configXml.
    updateConnectorTest(instance, configuration, false);

    // Create a modified connectorInstance.xml.
    TypeInfo typeInfo = getTypeMap().getTypeInfo(typeName);
    Resource resource = typeInfo.getConnectorInstancePrototype();
    String configXml = StringUtils.streamToString(resource.getInputStream());

    Configuration newConfiguration = new Configuration(typeName,
        new JsonObjectAsMap(new JSONObject(jsonConfigString)),
        configXml.replace("TestConnectorAInstance",
                          "NewTestConnectorAInstance"));

    // This knows that updateConnectorTest passes null for configXml.
    updateConnectorTest(instance, newConfiguration, true);

    // Test that the Configuration.configXml returned either contains null or
    // the connectorInstancePrototype for this connectorType.  Either is OK.
    Configuration storedConfiguration = instance.getConnectorConfiguration();
    assertNotNull(storedConfiguration.getXml());
    assertEquals(newConfiguration.getXml(), storedConfiguration.getXml());

    removeConnector(instance);
  }

  public void testUpdateType() throws Exception {
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
      updateConnectorTest(instance, typeName, false, jsonConfigString);
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
      updateConnectorTest(instance, typeName, true, jsonConfigString);
      assertFalse(originalConnectorDir.exists());
    }
    removeConnector(instance);
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
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";
    updateConnectorTest(instance, typeName, false, jsonConfigString);
    try {
      updateConnectorTest(instance, typeName, false, jsonConfigString);
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
    String jsonConfigString =
        "{Username:foo, Password:bar, Color:red, "
            + "RepositoryFile:MockRepositoryEventLog3.txt}";
    try {
      updateConnectorTest(instance, typeName, true, jsonConfigString);
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

    checkThreadDeadlock(new ConfigUpdater(instance1, 25),
                        new ConfigUpdater(instance2, 25));
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
    removeConnector(instance1);
    removeConnector(instance2);
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
      try { Thread.sleep(20); } catch (InterruptedException e) {}
      long[] ids = tmx.findMonitorDeadlockedThreads();
      if (ids != null) {
        ThreadInfo[] infos = tmx.getThreadInfo(ids);
        // TODO: Use logging instead?
        System.out.println("The following threads are deadlocked:");
        for (ThreadInfo ti : infos) {
          System.out.println(ti);
        }
        throw new RuntimeException("Deadlock");
      }
    }

    try {
      thread1.join(1000);
      thread2.join(1000);
    } catch (InterruptedException e) {}

    if (updater1.getException() != null)
      throw updater1.getException();
    if (updater2.getException() != null)
      throw updater2.getException();
  }

  private abstract class Updater implements Runnable {
    ConnectorCoordinatorImpl coordinator;
    private final int iterations;
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
    private final TypeInfo typeInfo;
    private final Configuration configuration;

    public ConfigUpdater(ConnectorCoordinatorImpl coordinator, int iterations)
        throws Exception {
      super(coordinator, iterations);
      typeInfo = getTypeMap().getTypeInfo(coordinator.getConnectorTypeName());
      configuration = new Configuration(coordinator.getConnectorTypeName(),
          new JsonObjectAsMap(new JSONObject(JSON_CONFIG)), null);
    }

    @Override
    void update() throws Exception {
      coordinator.setConnectorConfiguration(typeInfo, configuration,
                                            Locale.ENGLISH, true);
    }
  }

  private class ScheduleUpdater extends Updater {
    private final Schedule schedule;

    public ScheduleUpdater(ConnectorCoordinatorImpl coordinator,
                           int iterations) {
      super(coordinator, iterations);
      schedule = new Schedule(coordinator.getConnectorName(), false, 0, -1,
                              "0-0");
    }

    @Override
    void update() throws Exception {
      coordinator.setConnectorSchedule(schedule);
    }
  }

  private class CheckpointUpdater extends Updater {
    public CheckpointUpdater(ConnectorCoordinatorImpl coordinator,
                             int iterations) {
      super(coordinator, iterations);
    }

    @Override
    void update() throws Exception {
      coordinator.setConnectorState("checkpoint");
    }
  }

  private class RestartUpdater extends Updater {
    public RestartUpdater(ConnectorCoordinatorImpl coordinator,
                          int iterations) {
      super(coordinator, iterations);
    }

    @Override
    void update() throws Exception {
      coordinator.restartConnectorTraversal();
    }
  }

  private class RunOnceUpdater extends Updater {
    private final Schedule schedule;

    public RunOnceUpdater(ConnectorCoordinatorImpl coordinator,
                          int iterations) {
      super(coordinator, iterations);
      schedule = new Schedule(coordinator.getConnectorName(), false, 0, -1,
                              "0-0");
    }

    @Override
    void update() throws Exception {
      coordinator.setConnectorSchedule(schedule);
      coordinator.delayTraversal(TraversalDelayPolicy.POLL);
    }
  }

  private ConnectorCoordinatorImpl createConnector(String typeName,
      String name, String jsonConfigString)
      throws JSONException, InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
    assertFalse(instance.exists());

    updateConnectorTest(instance, typeName, false, jsonConfigString);
    return instance;
  }

  private void updateConnectorTest(ConnectorCoordinatorImpl instance,
      String typeName, boolean update, String jsonConfigString)
      throws JSONException, InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    Configuration configuration = new Configuration(typeName,
        new JsonObjectAsMap(new JSONObject(jsonConfigString)), null);
    updateConnectorTest(instance, configuration, update);
  }

  private void updateConnectorTest(ConnectorCoordinatorImpl instance,
      Configuration configuration, boolean update)
      throws InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    TypeInfo typeInfo = getTypeMap().getTypeInfo(configuration.getTypeName());
    ConfigureResponse response = instance.setConnectorConfiguration(
        typeInfo, configuration, Locale.ENGLISH, update);
    assertNull((response == null) ? null : response.getMessage(), response);
    InstanceInfo instanceInfo = instance.getInstanceInfo();
    File connectorDir = instanceInfo.getConnectorDir();
    assertTrue(connectorDir.exists());
    assertEquals(instance.getConnectorName(), instanceInfo.getName());

    // The password will be decrypted in the InstanceInfo.
    Map<String, String> instanceProps =
        instanceInfo.getConnectorConfiguration().getMap();
    String instancePasswd = instanceProps.get("Password");
    String plainPasswd = configuration.getMap().get("Password");
    assertEquals(instancePasswd, plainPasswd);

    // Verify that the google*WorkDir properties were not persisted.
    for (String propName : PropertiesUtils.GOOGLE_NONPERSISTABLE_PROPERTIES) {
      assertFalse(instanceProps.containsKey(propName));
    }
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

  /** Test setting special google* properties in the Connector. */
  public void testGoogleProperties() throws Exception {
    // Inject our test ConnectorType into the TypeMap.
    TypeInfo typeInfo = new TypeInfo(ValidatePropertiesConnectorType.TYPE_NAME,
        new ValidatePropertiesConnectorType(), null, null);
    getTypeMap().addTypeInfo(typeInfo);

    String name = "connector3";
    ConnectorCoordinatorImpl instance = createValidatePropsConnector(name);

    InstanceInfo info = instance.getInstanceInfo();
    assertEquals(name, info.getName());
    assertTrue(info.getConnector() instanceof ValidatePropertiesConnector);

    ValidatePropertiesConnector connector =
        (ValidatePropertiesConnector) (info.getConnector());
    assertEquals(name, connector.connectorName);
    assertTrue(connector.connectorWorkDir.contains(
               Context.DEFAULT_JUNIT_COMMON_DIR_PATH));
    assertTrue(connector.connectorWorkDir.contains(
               ValidatePropertiesConnectorType.TYPE_NAME));
    assertTrue(connector.connectorWorkDir.contains(name));
    assertTrue(connector.googleWorkDir.contains(
               Context.DEFAULT_JUNIT_COMMON_DIR_PATH));

    removeConnector(instance);
  }

  private ConnectorCoordinatorImpl createValidatePropsConnector(String name)
      throws JSONException, InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
    assertFalse(instance.exists());

    Configuration config = new Configuration(
        ValidatePropertiesConnectorType.TYPE_NAME,
        new HashMap<String, String>(),
        ValidatePropertiesConnector.CONNECTOR_INSTANCE_PROTOTYPE);

    updateConnectorTest(instance, config, false);
    return instance;
  }

  /**
   * A ConnectorType that can be used to check that google* properties are
   * passed to validateConfig.
   */
  private static class ValidatePropertiesConnectorType
      extends MockConnectorType {
    static final String TYPE_NAME = "ValidatePropertiesConnectorType";

    public ValidatePropertiesConnectorType() {
      super(TYPE_NAME);
    }

    @Override
    public ConfigureResponse validateConfig(Map<String, String> configMap,
        Locale locale, ConnectorFactory factory) {
      assertTrue(configMap.containsKey(PropertiesUtils.GOOGLE_CONNECTOR_NAME));

      assertTrue(configMap.containsKey(PropertiesUtils.GOOGLE_WORK_DIR));
      assertTrue(configMap.get(PropertiesUtils.GOOGLE_WORK_DIR)
           .contains(Context.DEFAULT_JUNIT_COMMON_DIR_PATH));

      assertTrue(configMap.containsKey(
          PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR));
      assertTrue(configMap.get(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR)
          .contains(Context.DEFAULT_JUNIT_COMMON_DIR_PATH));
      assertTrue(configMap.get(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR)
          .contains(TYPE_NAME));
      assertTrue(configMap.get(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR)
          .contains(configMap.get(PropertiesUtils.GOOGLE_CONNECTOR_NAME)));

      return null;
    }
  }

  /** A Connector that accepts special google* config properties. */
  private static class ValidatePropertiesConnector extends MockConnector {
    static final String CONNECTOR_INSTANCE_PROTOTYPE = BEANS_PREFIX
        + "<bean class=\"" + ValidatePropertiesConnector.class.getName() + "\">"
        + "  <property name=\"googleConnectorName\""
        + "            value=\"${googleConnectorName}\" />"
        + "  <property name=\"googleConnectorWorkDir\""
        + "            value=\"${googleConnectorWorkDir}\" />"
        + "  <property name=\"googleWorkDir\" value=\"${googleWorkDir}\" />"
        + "</bean>\n" + BEANS_POSTFIX;

    String connectorName;
    String connectorWorkDir;
    String googleWorkDir;

    public void setGoogleConnectorName(String name) {
      connectorName = name;
    }

    public void setGoogleConnectorWorkDir(String dir) {
      connectorWorkDir = dir;
    }

    public void setGoogleWorkDir(String dir) {
      googleWorkDir = dir;
    }
  }

  /**
   * Test setting a new gsa.feed.host gets propagated to Connectors
   * with setters for googleFeedHost property (used for GData config).
   */
  public void testUpdateGDataHost() throws Exception {
    // Context setup taken largely from SetManagerConfigTest.
    String applicationContext =
        "testdata/contextTests/SetManagerConfigTest.xml";
    File baseDirectory = new File("testdata/tmp/SetManagerConfigTest");
    File propFile = new File(baseDirectory, "testContext.properties");

    ConnectorTestUtils.deleteAllFiles(baseDirectory);
    assertTrue(ConnectorTestUtils.mkdirs(baseDirectory));

    // Create an original set of feed host properties.
    Properties props = new Properties();
    props.put(Context.GSA_FEED_HOST_PROPERTY_KEY, "fubar");
    PropertiesUtils.storeToFile(props, propFile, "Initial Props");

    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(applicationContext,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    context.setFeeding(false);

    // Inject our test ConnectorType into the TypeMap.
    TypeInfo typeInfo = new TypeInfo(GDataPropertiesConnectorType.TYPE_NAME,
        new GDataPropertiesConnectorType(), null, null);
    getTypeMap().addTypeInfo(typeInfo);
    context.start();

    // Create the Connector and verify it was seeded with initial feed host.
    ConnectorCoordinatorImpl instance = createGDataPropsConnector("gdata");
    GDataPropertiesConnector connector =
        (GDataPropertiesConnector) instance.getInstanceInfo().getConnector();
    assertEquals("fubar", connector.googleFeedHost);

    // Change the feed host and verify it gets updated in connector.
    context.setConnectorManagerConfig("", "shme", 14,
        Context.GSA_FEED_SECURE_PORT_INVALID, null);
    assertEquals("shme", connector.googleFeedHost);

    removeConnector(instance);
    context.shutdown(true);
    ConnectorTestUtils.deleteAllFiles(baseDirectory);
  }

  private ConnectorCoordinatorImpl createGDataPropsConnector(String name)
      throws JSONException, InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    final ConnectorCoordinatorImpl instance = newCoordinator(name);
    assertFalse(instance.exists());

    Configuration config = new Configuration(
        GDataPropertiesConnectorType.TYPE_NAME,
        new HashMap<String, String>(),
        GDataPropertiesConnector.CONNECTOR_INSTANCE_PROTOTYPE);

    updateConnectorTest(instance, config, false);
    return instance;
  }

  /**
   * A ConnectorType that can be used to check that googleFeedHost property
   * is passed to validateConfig.
   */
  private static class GDataPropertiesConnectorType
      extends MockConnectorType {
    static final String TYPE_NAME = "GDataPropertiesConnectorType";

    public GDataPropertiesConnectorType() {
      super(TYPE_NAME);
    }

    @Override
    public ConfigureResponse validateConfig(Map<String, String> configMap,
        Locale locale, ConnectorFactory factory) {
      assertTrue(configMap.containsKey(PropertiesUtils.GOOGLE_FEED_HOST));
      return null;
    }
  }

  /** A Connector that accepts special googleFeedHost config property. */
  private static class GDataPropertiesConnector extends MockConnector {
    static final String CONNECTOR_INSTANCE_PROTOTYPE = BEANS_PREFIX
        + "<bean class=\"" + GDataPropertiesConnector.class.getName() + "\">"
        + "  <property name=\"googleFeedHost\" value=\"${googleFeedHost}\" />"
        + "</bean>\n" + BEANS_POSTFIX;

    String googleFeedHost;

    public void setGoogleFeedHost(String addr) {
      googleFeedHost = addr;
    }
  }

  /** A Lister that tracks starts and shutdowns. */
  private static class CountingLister implements Lister {
    private boolean isRunning = false;
    private int startCount = 0;
    private int shutdownCount = 0;

    @Override
    public void start() {
      synchronized(this) {
        startCount++;
        isRunning = true;
      }
      // "Run" for a bit, pretending to traverse repository.
      try { Thread.sleep(250L); } catch (InterruptedException e) {}
    }

    @Override
    public synchronized void shutdown() {
      shutdownCount++;
      isRunning = false;
    }

    @Override
    public void setDocumentAcceptor(DocumentAcceptor ignored) {}

    synchronized int getStartCount() {
      return startCount;
    }
    
    synchronized int getShutdownCount() {
      return shutdownCount;
    }
    
    synchronized boolean isRunning() {
      return isRunning;
    }
  }

  /** A Connector that has a lister that tracks restarts. */
  private static class CountingListerConnector extends MockConnector {
    public CountingListerConnector() {
      super(null, null, null, null, new CountingLister());
    }
  }

  /** Tests restartConnectorTraversal restarts a Lister. */
  public void testRestartLister() throws Exception {
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + CountingListerConnector.class.getName() + "\"/>\n"
        + BEANS_POSTFIX;
        
    ConnectorCoordinatorImpl instance =
        createMockConnector("counting_lister", connectorInstancePrototype);

    @SuppressWarnings("unchecked")
    CountingLister lister = (CountingLister) instance.getLister();
    assertNotNull(lister);
    try { Thread.sleep(50L); } catch (InterruptedException e) {}

    // Lister should be up and running.
    assertTrue(lister.isRunning());
    assertEquals(1, lister.getStartCount());
    assertEquals(0, lister.getShutdownCount());

    // RestartConnectorTraversal should shut it down and start it up again.
    instance.restartConnectorTraversal();
    try { Thread.sleep(50L); } catch (InterruptedException e) {}
    assertTrue(lister.isRunning());
    assertEquals(2, lister.getStartCount());
    assertEquals(1, lister.getShutdownCount());
  }

  /* TODO (bmj): Many of these tests are really testing stuff in InstanceInfo
     and DocumentFilterFactoryFactory, but are included here to leverage
     all the test machinery to create connector instances.  Perhaps the
     machinery could be extracted moved to ConnectorTestUtils.
  */

  /** Test no Document Filters defined by Connector. */
  public void testNoDocumentFilter() throws Exception {
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + BEANS_POSTFIX;
        
    ConnectorCoordinatorImpl instance =
        createMockConnector("no_filter", connectorInstancePrototype);
    DocumentFilterFactory factory = instance.getDocumentFilterFactory();
    assertNull(factory);
  }

  /** Test Empty Document Filter Chain defined by Connector. */
  public void testEmptyDocumentFilterChain() throws Exception {
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + "<bean class=\"" + DocumentFilterChain.class.getName() + "\">"
        + "<constructor-arg><list></list></constructor-arg></bean>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("empty_filter", connectorInstancePrototype);
    DocumentFilterFactory factory = instance.getDocumentFilterFactory();
    assertNotNull(factory);
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: []", factory.toString());
  }

  /** Test Single Document Filter defined by Connector. */
  public void testSingleDocumentFilter() throws Exception {
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + "<bean class=\"" + NoopDocumentFilter.class.getName() + "\"/>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("noop_filter", connectorInstancePrototype);
    DocumentFilterFactory factory = instance.getDocumentFilterFactory();
    assertNotNull(factory);
    assertTrue(factory instanceof NoopDocumentFilter);
  }

  /** Test Multiple Document Filters defined by Connector. */
  public void testMultipleDocumentFilters() throws Exception {
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + "<bean id=\"a\" class=\"" + NoopDocumentFilter.class.getName()
        + "\"/>\n"
        + "<bean id=\"b\" class=\"" + NoopDocumentFilter.class.getName()
        + "\"/>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("noop_filter_x2", connectorInstancePrototype);
    DocumentFilterFactory factory = instance.getDocumentFilterFactory();
    assertNotNull(factory);
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: [Noop, Noop]", factory.toString());
  }

  /** Test Multiple Document Filters with Chain defined by Connector. */
  public void testMultipleDocumentFiltersWithChain() throws Exception {
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + "<bean id=\"a\" class=\"" + NoopDocumentFilter.class.getName()
        + "\"/>\n"
        + "<bean id=\"b\" class=\"" + NoopDocumentFilter.class.getName()
        + "\"/>\n"
        + "<bean class=\"" + DocumentFilterChain.class.getName() + "\">"
        + "<constructor-arg><list>"
        + "<ref bean=\"a\"/><ref bean=\"b\"/>"
        + "</list></constructor-arg></bean>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("filters_with_chain", connectorInstancePrototype);
    DocumentFilterFactory factory = instance.getDocumentFilterFactory();
    assertNotNull(factory);
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: [Noop, Noop]", factory.toString());
  }

  /** Test Multiple Document Filters in Chain defined by Connector. */
  public void testMultipleDocumentFiltersInChain() throws Exception {
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + "<bean class=\"" + DocumentFilterChain.class.getName() + "\">"
        + "<constructor-arg><list>"
        + "<bean class=\"" + NoopDocumentFilter.class.getName() + "\"/>\n"
        + "<bean class=\"" + NoopDocumentFilter.class.getName() + "\"/>\n"
        + "</list></constructor-arg></bean>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("filters_in_chain", connectorInstancePrototype);
    DocumentFilterFactory factory = instance.getDocumentFilterFactory();
    assertNotNull(factory);
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: [Noop, Noop]", factory.toString());
  }

  /** Test Multiple Document Filters, but not all included in a Chain. */
  public void testExtraDocumentFiltersWithChain() throws Exception {
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + "<bean id=\"a\" class=\"" + NoopDocumentFilter.class.getName()
        + "\"/>\n"
        + "<bean id=\"b\" class=\"" + NoopDocumentFilter.class.getName()
        + "\"/>\n"
        + "<bean id=\"c\" class=\"" + NoopDocumentFilter.class.getName()
        + "\"/>\n"
        + "<bean class=\"" + DocumentFilterChain.class.getName() + "\">"
        + "<constructor-arg><list>"
        + "<ref bean=\"c\"/>"
        + "</list></constructor-arg></bean>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("filters_with_chain", connectorInstancePrototype);
    DocumentFilterFactory factory = instance.getDocumentFilterFactory();
    assertNotNull(factory);
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: [Noop]", factory.toString());
  }

  public void testDocumentFilterFactoryFactoryNoFilterNoMap() throws Exception {
    DocumentFilterFactoryFactory factoryFactory =
        new DocumentFilterFactoryFactoryImpl(null, null);
    DocumentFilterFactory factory =
        factoryFactory.getDocumentFilterFactory("test");
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: []", factory.toString());
  }

  public void testDocumentFilterFactoryFactoryOneFilterNoMap()
      throws Exception {
    DocumentFilterFactoryFactory factoryFactory =
        new DocumentFilterFactoryFactoryImpl(new NoopDocumentFilter(), null);
    DocumentFilterFactory factory =
        factoryFactory.getDocumentFilterFactory("test");
    assertTrue(factory instanceof NoopDocumentFilter);
  }

  public void testDocumentFilterFactoryFactoryNoFilterConnectorNotFound()
       throws Exception {
    DocumentFilterFactoryFactory factoryFactory =
        new DocumentFilterFactoryFactoryImpl(null, getCoordinatorMap());
    DocumentFilterFactory factory =
        factoryFactory.getDocumentFilterFactory("nonexistant");
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: []", factory.toString());
  }

  public void testDocumentFilterFactoryFactoryNoFilterNoConnectorFilter()
      throws Exception {
    DocumentFilterFactoryFactory factoryFactory =
        new DocumentFilterFactoryFactoryImpl(null, getCoordinatorMap());
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("no_filter", connectorInstancePrototype);
    DocumentFilterFactory factory =
        factoryFactory.getDocumentFilterFactory("no_filter");
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: []", factory.toString());
  }

  public void testDocumentFilterFactoryFactoryNoFilterConnectorFilter()
      throws Exception {
    DocumentFilterFactoryFactory factoryFactory =
        new DocumentFilterFactoryFactoryImpl(null, getCoordinatorMap());
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + "<bean class=\"" + NoopDocumentFilter.class.getName() + "\"/>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("noop_filter", connectorInstancePrototype);
    DocumentFilterFactory factory =
        factoryFactory.getDocumentFilterFactory("noop_filter");
    assertNotNull(factory);
    assertTrue(factory instanceof NoopDocumentFilter);
  }

  public void testDocumentFilterFactoryFactoryGlobalFilterConnectorFilter()
      throws Exception {
    DocumentFilterFactoryFactory factoryFactory =
        new DocumentFilterFactoryFactoryImpl(new NoopDocumentFilter(),
                                             getCoordinatorMap());
    String connectorInstancePrototype = BEANS_PREFIX
        + "<bean class=\"" + MockConnector.class.getName() + "\"/>\n"
        + "<bean class=\"" + DocumentFilterChain.class.getName() + "\">"
        + "<constructor-arg><list>"
        + "<bean class=\"" + NoopDocumentFilter.class.getName() + "\"/>\n"
        + "</list></constructor-arg></bean>\n"
        + BEANS_POSTFIX;

    ConnectorCoordinatorImpl instance =
        createMockConnector("filter_in_chain", connectorInstancePrototype);
    DocumentFilterFactory factory =
        factoryFactory.getDocumentFilterFactory("filter_in_chain");

    assertNotNull(factory);
    assertTrue(factory instanceof DocumentFilterChain);
    assertEquals("DocumentFilterChain: [DocumentFilterChain: [Noop], Noop]",
                 factory.toString());
  }

  private static class NoopDocumentFilter extends AbstractDocumentFilter {
    @Override
    public String toString() {
      return "Noop";
    }
  }

  private ConnectorCoordinatorImpl createMockConnector(String name, 
      String connectorInstancePrototype)
      throws JSONException, InstantiatorException, ConnectorNotFoundException,
      ConnectorExistsException, ConnectorTypeNotFoundException {
    // Inject our test ConnectorType into the TypeMap.
    MockConnectorType type = new MockConnectorType(name);
    TypeInfo typeInfo = new TypeInfo(type.toString(), type, null, null);
    getTypeMap().addTypeInfo(typeInfo);

    final ConnectorCoordinatorImpl instance = newCoordinator(name);
    assertFalse(instance.exists());

    Configuration config = new Configuration(type.toString(),
        new HashMap<String, String>(), connectorInstancePrototype);

    updateConnectorTest(instance, config, false);
    return instance;
  }
}
