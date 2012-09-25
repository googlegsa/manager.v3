// Copyright 2007 Google Inc.
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

import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.InstanceInfo.FactoryCreationFailureException;
import com.google.enterprise.connector.instantiator.InstanceInfo.InstanceInfoException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NoBeansFoundException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NullConnectorNameException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NullDirectoryException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NullTypeInfoException;
import com.google.enterprise.connector.instantiator.InstanceInfo.PropertyProcessingFailureException;
import com.google.enterprise.connector.instantiator.TypeInfo.TypeInfoException;
import com.google.enterprise.connector.persist.FileStore;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstanceInfoTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(InstanceInfoTest.class.getName());

  /**
   * Constructs a new Connector Instance based
   * upon its on-disk persistently stored configuration.
   *
   * @param connectorName the name of the Connector instance.
   * @param connectorDir the Connector's on-disk directory.
   * @param typeInfo the Connector's prototype.
   * @return new InstanceInfo representing the Connector instance.
   * @throws InstanceInfoException
   */
  private InstanceInfo fromDirectory(String connectorName,
      File connectorDir, TypeInfo typeInfo) throws InstanceInfoException {
    try {
      // Read the configuration from svn-controlled connectorDir.
      // Note that this is not the test directory generally used for
      // persistence, and it shouldn't be.  We want to avoid writing
      // to svn-controlled directories when running the tests.
      File propFile = new File(connectorDir, connectorName +".properties");
      Properties props = PropertiesUtils.loadFromFile(propFile);
      File xmlFile = new File(connectorDir, TypeInfo.CONNECTOR_INSTANCE_XML);
      String xml = null;
      if (xmlFile.exists()) {
        xml = StringUtils.streamToStringAndThrow(new FileInputStream(xmlFile));
      }
      Configuration config = new Configuration(
          typeInfo.getConnectorTypeName(), PropertiesUtils.toMap(props), xml);
      return new InstanceInfo(connectorName, connectorDir, typeInfo, config);
    } catch (IOException ioe) {
      throw new InstanceInfoException("I/O error:", ioe);
    } catch (PropertiesException pe) {
      throw new InstanceInfoException("Properties error:", pe);
    }
  }

  public final void testFromDirectoryPositive() {
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    File connectorDir = new File("testdata/connectorInstanceTests/positive");
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    boolean exceptionThrown = false;
    try {
      fromDirectory("fred", connectorDir, typeInfo);
    } catch (InstanceInfoException e) {
      exceptionThrown = true;
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertFalse(exceptionThrown);
  }

  public final void testConstructorNegative0() {
    // test null directory argument
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    InstanceInfo instanceInfo = null;
    boolean correctExceptionThrown = false;
    try {
      instanceInfo = new InstanceInfo("fred", null, typeInfo, null);
    } catch (NullDirectoryException e) {
      correctExceptionThrown = true;
      LOGGER.log(Level.WARNING, "Null directory exception", e);
    } catch (InstanceInfoException e) {
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertTrue(correctExceptionThrown);
    assertNull(instanceInfo);
  }

  public final void testConstructorNegative1() {
    // test null TypeInfo argument
    InstanceInfo instanceInfo = null;
    File connectorDir = new File("testdata/connectorInstanceTests/positive");
    boolean correctExceptionThrown = false;
    try {
      instanceInfo = new InstanceInfo("fred", connectorDir, null, null);
    } catch (NullTypeInfoException e) {
      correctExceptionThrown = true;
      LOGGER.log(Level.WARNING, "Null directory exception", e);
    } catch (InstanceInfoException e) {
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertTrue(correctExceptionThrown);
    assertNull(instanceInfo);
  }

  public final void testConstructorNegative2() {
    // test null connector name argument
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    InstanceInfo instanceInfo = null;
    File connectorDir = new File("testdata/connectorInstanceTests/positive");
    boolean correctExceptionThrown = false;
    try {
      instanceInfo = new InstanceInfo(null, connectorDir, typeInfo, null);
    } catch (NullConnectorNameException e) {
      correctExceptionThrown = true;
      LOGGER.log(Level.WARNING, "Null directory exception", e);
    } catch (InstanceInfoException e) {
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertTrue(correctExceptionThrown);
    assertNull(instanceInfo);
  }

  public final void testFromDirectoryNegative3() {
    // test properties file doesn't fill in all properties
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    InstanceInfo instanceInfo = null;
    File connectorDir = new File("testdata/connectorInstanceTests/negative3");
    boolean correctExceptionThrown = false;
    try {
      instanceInfo = fromDirectory("fred", connectorDir, typeInfo);
    } catch (PropertyProcessingFailureException e) {
      correctExceptionThrown = true;
      LOGGER.log(Level.WARNING, "Property processing exception", e);
    } catch (InstanceInfoException e) {
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertTrue(correctExceptionThrown);
    assertNull(instanceInfo);
  }

  public final void testFromDirectoryNegative4() {
    // test connectorInstance.xml that doesn't implement Connector
    String resourceName =
        "testdata/connectorInstanceTests/badConnectorType1/connectorType.xml";
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    InstanceInfo instanceInfo = null;
    File connectorDir = new File("testdata/connectorInstanceTests/positive");
    boolean correctExceptionThrown = false;
    try {
      instanceInfo = fromDirectory("fred", connectorDir, typeInfo);
    } catch (NoBeansFoundException e) {
      correctExceptionThrown = true;
      LOGGER.log(Level.WARNING, "Null directory exception", e);
    } catch (InstanceInfoException e) {
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertTrue(correctExceptionThrown);
    assertNull(instanceInfo);
  }

  /** Test encrypted property */
  public final void testFromDirectoryEncrypted() throws Exception {
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    String testDirName = "testdata/tmp/InstantiatorTests";
    String connectorName = "fred";
    String plainTextPassword = "password_test";

    // Create a TypeMap for our custom type.
    TypeMap typeMap = new TypeMap(resourceName, testDirName);
    typeMap.init();
    TypeInfo typeInfo = typeMap.getTypeInfo(
        typeMap.getConnectorTypeNames().iterator().next());

    // Make sure that the test directory does not exist
    File connectorDir = new File(typeInfo.getConnectorTypeDir(), connectorName);
    assertTrue(ConnectorTestUtils.deleteAllFiles(connectorDir));
    // Then recreate it empty
    assertTrue(connectorDir.mkdirs());

    // Force use of FileStore PersistentStore.
    FileStore fileStore = new FileStore();
    fileStore.setTypeMap(typeMap);
    InstanceInfo.setPersistentStore(fileStore);

    // Jam a properties file with encrypted passwords into the connector dir,
    // making it look like it was persisted.
    Properties props = new Properties();
    props.setProperty("RepositoryFile", "MockRepositoryEventLog3.txt");
    props.setProperty("Password", plainTextPassword);
    InstanceInfo instanceInfo = null;
    boolean exceptionThrown = false;
    File propFile = new File(connectorDir, connectorName + ".properties");
    try {
      // Write properties out to temp file
      PropertiesUtils.storeToFile(props, propFile, null);

      // Make sure the password does not occur in plain-text in the file.
      String temp = StringUtils.streamToString(new FileInputStream(propFile));
      assertFalse(temp.contains(plainTextPassword));

      // Now instantiate the connector with those properties.
      instanceInfo = fromDirectory(connectorName, connectorDir, typeInfo);
    } catch (InstanceInfoException e) {
      exceptionThrown = true;
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    } catch (PropertiesException e) {
      exceptionThrown = true;
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertFalse(exceptionThrown);

    // Check that the password is decrypted properly in the configuration.
    Configuration config = instanceInfo.getConnectorConfiguration();
    assertEquals(plainTextPassword, config.getMap().get("Password"));
    assertEquals("MockRepositoryEventLog3.txt",
                 config.getMap().get("RepositoryFile"));

    // Clean up temp directory and files
    assertTrue(ConnectorTestUtils.deleteAllFiles(new File(testDirName)));
  }

  private TypeInfo makeValidTypeInfo(String resourceName) {
    Resource r = new FileSystemResource(resourceName);
    TypeInfo typeInfo = null;
    boolean exceptionThrown = false;
    try {
      typeInfo = TypeInfo.fromSpringResourceAndThrow(r);
    } catch (TypeInfoException e) {
      exceptionThrown = true;
      LOGGER.log(Level.WARNING, "Type Info Creation Problem", e);
    }
    assertFalse(exceptionThrown);
    assertNotNull(typeInfo);
    return typeInfo;
  }

  public final void testCustomInstancePrototype() {
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    File connectorDir = new File("testdata/connectorInstanceTests/custom1");
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    InstanceInfo instanceInfo = null;
    boolean exceptionThrown = false;
    try {
      instanceInfo = fromDirectory("fred", connectorDir, typeInfo);
    } catch (InstanceInfoException e) {
      exceptionThrown = true;
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertFalse(exceptionThrown);
    assertTrue("Connector should be of type CustomProtoTestConnector",
        instanceInfo.getConnector() instanceof CustomProtoTestConnector);
    CustomProtoTestConnector c =
        (CustomProtoTestConnector) instanceInfo.getConnector();
    assertEquals("oogabooga", c.getCustomProperty());
  }

  public final void testBadCustomInstancePrototype() {
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    File connectorDir = new File("testdata/connectorInstanceTests/custom2");
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    boolean exceptionThrown = false;
    try {
      fromDirectory("fred", connectorDir, typeInfo);
    } catch (InstanceInfoException e) {
      exceptionThrown = true;
      assertTrue("Expected InstanceInfoException",
          e instanceof FactoryCreationFailureException);
    }
    assertTrue(exceptionThrown);
  }

  public final void testOverspecifiedProperties() {
    String resourceName =
        "testdata/connectorTypeTests/positive/connectorType.xml";
    File connectorDir =
        new File("testdata/connectorInstanceTests/overspecifiedProperties");
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    InstanceInfo instanceInfo = null;
    boolean exceptionThrown = false;
    try {
      instanceInfo = fromDirectory("fred", connectorDir, typeInfo);
    } catch (InstanceInfoException e) {
      exceptionThrown = true;
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertFalse(exceptionThrown);
    assertTrue("Connector should be of type CustomProtoTestConnector",
        instanceInfo.getConnector() instanceof CustomProtoTestConnector);
    CustomProtoTestConnector c =
        (CustomProtoTestConnector) instanceInfo.getConnector();
    assertEquals("hungadunga", c.getCustomProperty());
    assertEquals(47, c.getCustomIntProperty());
  }

  /**
   * Testing case where Connector wants to specify some default properties that
   * can be overridden.
   */
  public final void testDefaultProperties() {
    String resourceName =
        "testdata/connectorTypeTests/default/connectorType.xml";
    File connectorDir =
        new File("testdata/connectorInstanceTests/default");
    TypeInfo typeInfo = makeValidTypeInfo(resourceName);
    InstanceInfo instanceInfo = null;
    try {
      instanceInfo = fromDirectory("fred", connectorDir, typeInfo);
    } catch (InstanceInfoException e) {
      LOGGER.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
      fail(e.getMessage());
    }
    assertTrue("Connector should be of type SimpleTestConnector",
        instanceInfo.getConnector() instanceof SimpleTestConnector);
    SimpleTestConnector c = (SimpleTestConnector)instanceInfo.getConnector();
    assertEquals("Checking default - color", "red", c.getColor());
    assertEquals("Checking default empty override - repo file",
        "", c.getRepositoryFileName());
    assertEquals("Checking default override - user",
        "not_default_user", c.getUsername());
    assertEquals("Checking setting - work dir name",
        "/tomcat/webapps/connector-manager/WEB-INF", c.getWorkDirName());
  }
}
