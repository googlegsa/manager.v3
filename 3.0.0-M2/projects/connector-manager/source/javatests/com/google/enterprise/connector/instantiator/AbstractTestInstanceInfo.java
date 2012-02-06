// Copyright 2011 Google Inc.
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
import com.google.enterprise.connector.instantiator.InstanceInfo.FactoryCreationFailureException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NoBeansFoundException;
import com.google.enterprise.connector.instantiator.InstanceInfo.PropertyProcessingFailureException;
import com.google.enterprise.connector.instantiator.TypeInfo.TypeInfoException;
import com.google.enterprise.connector.spi.Connector;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for InstanceInfoTest and ConnectorInstanceFactoryTest.
 * Forgive the odd name, but it is not to be confused with a Test Case
 * at run time.
 */
public abstract class AbstractTestInstanceInfo extends TestCase {
  protected Logger logger;

  protected void setUp() throws Exception {
    super.setUp();
    logger = Logger.getLogger(this.getClass().getName());
  }

  /**
   * Constructs a new Connector Instance.
   *
   * @param connectorName the name of the Connector instance.
   * @param connectorDir the Connector's on-disk directory.
   * @param typeInfo the Connector's prototype.
   * @param connfiguration the Connector Configuration.
   * @return new Connector instance.
   */
  protected abstract Connector newInstance(String connectorName,
      String connectorDir, TypeInfo typeInfo, Configuration configuration)
      throws Exception;

  /**
   * Constructs a new Connector Configuration based
   * upon its on-disk persistently stored configuration.
   *
   * @param connectorName the name of the Connector instance.
   * @param connectorDir the Connector's on-disk directory.
   * @param typeInfo the Connector's prototype.
   * @return the Connector Configuration.
   */
  protected Configuration readConfiguration(String connectorName,
      String connectorDir, TypeInfo typeInfo) throws Exception {
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
    Configuration configuration =
      new Configuration(typeInfo.getConnectorTypeName(),
                        PropertiesUtils.toMap(props), xml);
    assertNotNull(configuration);
    return configuration;
  }

  /** Makes a TypeInfo from the supplied Spring resource. */
  protected TypeInfo makeTypeInfo(String resourceName)
      throws TypeInfoException {
    Resource r = new FileSystemResource(resourceName);
    TypeInfo typeInfo = TypeInfo.fromSpringResourceAndThrow(r);
    assertNotNull(typeInfo);
    return typeInfo;
  }

  /**
   * Constructs a new Connector Configuration based
   * upon the supplied Configuration.
   *
   * @param connectorName the name of the Connector instance.
   * @param connfiguration the Connector Configuration.
   * @param connectorDir the Connector's on-disk directory.
   * @param typeInfo the Connector's prototype.
   * @param expectedException Class for expected exception to be thrown.
   *        If null, no exception is expected.
   * @param expectedMessage Expected message of exception to be thrown.
   *        If null, don't care about the message.
   * @return the Connector instance.
   */
  protected Connector fromConfigurationTest(String connectorName,
      String connectorDir, TypeInfo typeInfo, Configuration configuration,
      Class expectedException, String expectedMessage) throws Exception {
    try {
      Connector connector =
          newInstance(connectorName, connectorDir, typeInfo, configuration);
      assertNull("Expected exception but got none", expectedException);
      assertNotNull(connector);
      return connector;
    } catch (Exception e) {
      if (e.getClass() == expectedException) {
        if (expectedMessage != null) {
          assertTrue("Thrown exception message does not match expected message",
                     e.getMessage().startsWith(expectedMessage));
        }
        logger.log(Level.FINE, "Expected exception thrown", e);
      } else {
        logger.log(Level.WARNING,
            "Unexpected exception during instance info creation", e);
        throw e;
      }
    }
    return null;
  }

  /**
   * Constructs a new Connector Configuration based
   * upon its on-disk persistently stored configuration.
   *
   * @param connectorName the name of the Connector instance.
   * @param connectorDir the Connector's on-disk directory.
   * @param resourceName the ConnectorType Spring resource name.
   * @param expectedException Class for expected exception to be thrown.
   *        If null, no exception is expected.
   * @param expectedMessage Expected message of exception to be thrown.
   *        If null, don't care about the message.
   * @return the Connector instance.
   */
  protected Connector fromDirectoryTest(String connectorName,
      String connectorDir, String resourceName, Class expectedException,
      String expectedMessage) throws Exception {
    TypeInfo typeInfo = makeTypeInfo(resourceName);
    Configuration configuration =
        readConfiguration(connectorName, connectorDir, typeInfo);
    return fromConfigurationTest(connectorName, connectorDir, typeInfo,
        configuration, expectedException, expectedMessage);
  }


  /** Test successful Connector Instance creation. */
  public final void testFromDirectoryPositive() throws Exception {
    fromDirectoryTest("fred",
                      "testdata/connectorInstanceTests/positive",
                      "testdata/connectorTypeTests/positive/connectorType.xml",
                      null, null);
  }

  /** Test properties file doesn't fill in all properties. */
  public final void testInsufficientProperties() throws Exception {
    fromDirectoryTest("fred",
                      "testdata/connectorInstanceTests/negative3",
                      "testdata/connectorTypeTests/positive/connectorType.xml",
                      PropertyProcessingFailureException.class, null);
  }

  /** Test connectorInstance.xml that doesn't implement Connector. */
  public final void testNoConnectorBean() throws Exception {
    fromDirectoryTest("fred",
       "testdata/connectorInstanceTests/positive",
       "testdata/connectorInstanceTests/badConnectorType1/connectorType.xml",
       NoBeansFoundException.class, null);
  }

  /** Test custom connectorInstance.xml. */
  public final void testCustomInstancePrototype() throws Exception {
    Connector connector = fromDirectoryTest("fred",
        "testdata/connectorInstanceTests/custom1",
        "testdata/connectorTypeTests/positive/connectorType.xml",
        null, null);

    assertTrue("Connector should be of type CustomProtoTestConnector",
        connector instanceof CustomProtoTestConnector);
    CustomProtoTestConnector c = (CustomProtoTestConnector) connector;
    assertEquals("oogabooga", c.getCustomProperty());
  }

  /** Test a bad custom connectorInstance.xml. */
  public final void testBadCustomInstancePrototype() throws Exception {
    fromDirectoryTest("fred",
        "testdata/connectorInstanceTests/custom2",
        "testdata/connectorTypeTests/positive/connectorType.xml",
        FactoryCreationFailureException.class, null);
  }

  /** Test overspecified properties. */
  public final void testOverspecifiedProperties() throws Exception {
    Connector connector = fromDirectoryTest("fred",
        "testdata/connectorInstanceTests/overspecifiedProperties",
        "testdata/connectorTypeTests/positive/connectorType.xml",
        null, null);

    assertTrue("Connector should be of type CustomProtoTestConnector",
        connector instanceof CustomProtoTestConnector);
    CustomProtoTestConnector c = (CustomProtoTestConnector) connector;
    assertEquals("hungadunga", c.getCustomProperty());
    assertEquals(47, c.getCustomIntProperty());
  }

  /**
   * Testing case where Connector wants to specify some default properties that
   * can be overridden.
   */
  public final void testDefaultProperties() throws Exception {
    Connector connector = fromDirectoryTest("fred",
        "testdata/connectorInstanceTests/default",
        "testdata/connectorTypeTests/default/connectorType.xml",
        null, null);

    assertTrue("Connector should be of type SimpleTestConnector",
        connector instanceof SimpleTestConnector);
    SimpleTestConnector c = (SimpleTestConnector) connector;
    assertEquals("Checking default - color", "red", c.getColor());
    assertEquals("Checking default empty override - repo file",
        "", c.getRepositoryFileName());
    assertEquals("Checking default override - user",
        "not_default_user", c.getUsername());
    assertEquals("Checking setting - work dir name",
        "/tomcat/webapps/connector-manager/WEB-INF", c.getWorkDirName());
  }
}
