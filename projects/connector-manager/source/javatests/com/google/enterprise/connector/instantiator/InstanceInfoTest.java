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

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.common.PropertiesException;
import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.InstanceInfo.InstanceInfoException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NullConfigurationException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NullConnectorNameException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NullDirectoryException;
import com.google.enterprise.connector.instantiator.InstanceInfo.NullTypeInfoException;
import com.google.enterprise.connector.persist.FileStore;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import org.springframework.core.io.ByteArrayResource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;

public class InstanceInfoTest extends AbstractTestInstanceInfo {

  @Override
  protected Connector newInstance(String connectorName, String connectorDir,
      TypeInfo typeInfo, Configuration configuration) throws Exception {
    InstanceInfo instanceInfo = new InstanceInfo(connectorName,
        (connectorDir != null) ? new File(connectorDir) : null,
        typeInfo, configuration);
    assertNotNull(instanceInfo);
    return instanceInfo.getConnector();
  }

  /** Test invalid constructor arguments. */
  public final void testConstructorArgs() throws Exception {
    String connectorName = "fred";
    String connectorDir = "testdata/connectorInstanceTests/positive";
    String resourceName = "testdata/connectorTypeTests/positive/connectorType.xml";
    TypeInfo typeInfo = makeTypeInfo(resourceName);
    Configuration configuration =
        readConfiguration(connectorName, connectorDir, typeInfo);

    // Test null connector name in constructor.
    fromConfigurationTest(null, connectorDir, typeInfo, configuration,
                          NullConnectorNameException.class, null);

    // Test null connector directory in constructor.
    fromConfigurationTest(connectorName, null, typeInfo, configuration,
                          NullDirectoryException.class, null);

    // Test null TypeInfo in constructor.
    fromConfigurationTest(connectorName, connectorDir, null, configuration,
                          NullTypeInfoException.class, null);

    // Test null Configuration.
    fromConfigurationTest(connectorName, connectorDir, typeInfo, null,
                          NullConfigurationException.class, null);
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
    ConnectorTestUtils.deleteAllFiles(connectorDir);
    // Then recreate it empty
    assertTrue(ConnectorTestUtils.mkdirs(connectorDir));

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
      Configuration configuration =
          readConfiguration(connectorName, connectorDir.getPath(), typeInfo);
      instanceInfo = new InstanceInfo(
          connectorName, connectorDir, typeInfo, configuration);
      assertNotNull(instanceInfo);

    } catch (InstanceInfoException e) {
      exceptionThrown = true;
      logger.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    } catch (PropertiesException e) {
      exceptionThrown = true;
      logger.log(Level.WARNING,
          "unexpected exception during instance info creation", e);
    }
    assertFalse(exceptionThrown);

    // Check that the password is decrypted properly in the configuration.
    Configuration config = instanceInfo.getConnectorConfiguration();
    assertEquals(plainTextPassword, config.getMap().get("Password"));
    assertEquals("MockRepositoryEventLog3.txt",
                 config.getMap().get("RepositoryFile"));

    // Clean up temp directory and files
    ConnectorTestUtils.deleteAllFiles(new File(testDirName));
  }

  /** Parses an XML instance with non-ASCII characters. */
  public void testNonAsciiXml() throws Exception {
    String resourceName =
        "testdata/connectorTypeTests/default/connectorType.xml";
    TypeInfo typeInfo = makeTypeInfo(resourceName);

    String expected = "fonc\u00e9";
    String xml =
        "<?xml version='1.0' encoding='UTF-8'?>\n"
        + "<!DOCTYPE beans PUBLIC '-//SPRING//DTD BEAN//EN' "
        + "'http://www.springframework.org/dtd/spring-beans.dtd'>\n"
        + "<beans><bean id='SimpleTestConnectorInstance' "
        + "class='" + SimpleTestConnector.class.getName() + "'>\n"
        + "<property name='color' value='" + expected + "'/>\n"
        + "</bean></beans>\n";

    Configuration configuration =
      new Configuration(typeInfo.getConnectorTypeName(),
          ImmutableMap.<String, String>of(), xml);
    Connector instance =
        InstanceInfo.makeConnectorWithSpring("fred", typeInfo, configuration);
    assertEquals(expected, ((SimpleTestConnector) instance).getColor());
  }

  /**
   * Shows that getBytes() is harmless with properties files, which
   * are encoded using ASCII anyway.
   */
  public void testNonAsciiProperties() throws Exception {
    String expected = "fonc\u00e9";
    ByteArrayResource resource = (ByteArrayResource) InstanceInfo
        .getPropertiesResource("fred", ImmutableMap.of("Color", expected));
    String props = new String(resource.getByteArray());
    assertFalse(props, props.contains(expected));
    Properties properties = PropertiesUtils.loadFromString(props);
    assertEquals(expected, properties.getProperty("Color"));
  }
}
