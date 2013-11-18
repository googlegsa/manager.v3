// Copyright 2006 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Tests {@link SetConnectorConfigHandler} for {@link SetConnectorConfig}
 * servlet.
 */
public class SetConnectorConfigHandlerTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(SetConnectorConfigHandlerTest.class.getName());
  private String language;
  private String connectorName;
  private String connectorType;
  private Map<String, String> configData;
  private String configXml;
  private String globalNamespace;
  private String localNamespace;
  private boolean update;
  private MockManager manager;

  @Override
  protected void setUp() {
    manager = new ConfigSavingManager();
    globalNamespace = null;
    localNamespace = null;
  }

  /** Test invalid configure xml element. */
  public void testInvalidRequest() throws Exception {
    SetConnectorConfigHandler hdl = new SetConnectorConfigHandler("", manager);
    assertEquals(ConnectorMessageCode.ERROR_PARSING_XML_REQUEST,
                 hdl.getStatus().getMessageId());
  }

  /** Test null connector name. */
  public void testNullConnectorName() throws Exception {
    language = "en";
    connectorName = ""; // Produces empty name element.
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueB1");
    configData.put("name2", "valueB2");
    configData.put("name3", "valueB3");
    configXml = null;
    update = false;
    SetConnectorConfigHandler hdl = doTest(setXMLBody());
    assertEquals(ConnectorMessageCode.RESPONSE_NULL_CONNECTOR,
                 hdl.getStatus().getMessageId());
  }

  /** Test empty config map. */
  public void testEmptyConfig() throws Exception {
    language = "en";
    connectorName = "ConnectorA";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configXml = null;
    update = false;
    SetConnectorConfigHandler hdl = doTest(setXMLBody());
    assertEquals(ConnectorMessageCode.RESPONSE_NULL_CONFIG_DATA,
                 hdl.getStatus().getMessageId());
  }

  /** Test set config - not update implies create new. */
  public void testCreateConnector() throws Exception {
    language = "en";
    connectorName = "connectorA";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    configXml = null;
    update = false;
    doTest();
  }

  /** Test set config with connectorInstance.xml */
  public void testCreateConnectorWithConfigXml() throws Exception {
    language = "en";
    connectorName = "connectorA";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    configXml = "<?xml?><beans><bean id=\"NewConfigXML\"/></beans>";
    update = false;
    doTest();
  }

  /** Test set config with connectorInstance.xml */
  public void testCreateConnectorWithConfigNamespaces() throws Exception {
    language = "en";
    connectorName = "connectorA";
    connectorType = "documentum";
    globalNamespace = "ThinkGlobally";
    localNamespace = "ActLocally";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    configXml = "<?xml?><beans><bean id=\"NewConfigXML\"/></beans>";
    update = false;

    String xmlBody = setXMLBody();

    configData.put(PropertiesUtils.GOOGLE_GLOBAL_NAMESPACE, globalNamespace);
    configData.put(PropertiesUtils.GOOGLE_LOCAL_NAMESPACE, localNamespace);

    SetConnectorConfigHandler hdl = doTest(xmlBody);
    ConnectorMessageCode status = hdl.getStatus();
    ConfigureResponse response = hdl.getConfigRes();
    assertTrue("Status: Code=" + status.getMessageId() + "  Message="
        + status.getMessage() + ((response == null) ? "" :
        "  Response: Message=" + response.getMessage()),
        status.isSuccess());
  }

  /** Test update configuration. */
  public void testUpdate() throws Exception {
    language = "en";
    connectorName = "connector_a";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    update = false;
    configXml = null;
    doTest();

    configData.put("name1", "valueB1");
    configData.put("name2", "valueB2");
    update = true;
    doTest();
  }

  /** Test update mixed case connector configuration. */
  // GSA 5.2 and greater wants only lowercase connector names,
  // so force all new connector names to be lower case.
  // Unfortunately, we cannot do this for existing connectors.
  public void testUpdateMixedCase() throws Exception {
    language = "en";
    connectorName = "connectorA";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    update = false;
    configXml = null;
    manager.setConnectorConfiguration(connectorName,
        new Configuration(connectorType, configData, configXml),
        language, update);

    configData.put("name1", "valueB1");
    configData.put("name2", "valueB2");
    update = true;
    doTest();
  }

  /** Test update configuration properties with connectorInstance.xml */
  public void testUpdateWithConfigXml() throws Exception {
    language = "en";
    connectorName = "connector_a";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    configXml = "<?xml?><beans><bean id=\"NewConfigXML\"/></beans>";
    update = false;
    doTest();

    configData.put("name1", "valueB1");
    configData.put("name2", "valueB2");
    update = true;
    doTest();
  }

  /** Test update configuration with only connectorInstance.xml */
  public void testUpdateOnlyConfigXml() throws Exception {
    language = "en";
    connectorName = "connector_a";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    configXml = null;
    update = false;
    doTest();

    update = true;
    configXml = "<?xml?><beans><bean id=\"NewConfigXML\"/></beans>";
    doTest();
  }

  /** Test setConnectorConfiguration throwing InstantiatorException. */
  public void testInstantiatorException() throws Exception {
    checkExceptionHandling(new InstantiatorException(getName()),
                           ConnectorMessageCode.EXCEPTION_INSTANTIATOR);
  }

  /** Test setConnectorConfiguration throwing ConnectorNotFoundException. */
  public void testConnectorNotFoundException() throws Exception {
    checkExceptionHandling(new ConnectorNotFoundException(getName()),
                           ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND);
  }

  /** Test setConnectorConfiguration throwing ConnectorExistsException. */
  public void testConnectorExistsException() throws Exception {
    checkExceptionHandling(new ConnectorExistsException(getName()),
                           ConnectorMessageCode.EXCEPTION_CONNECTOR_EXISTS);
  }

  /** Test setConnectorConfiguration throwing PersistentStoreException. */
  public void testPersistentStoreException() throws Exception {
    checkExceptionHandling(new PersistentStoreException(getName()),
                           ConnectorMessageCode.EXCEPTION_PERSISTENT_STORE);
  }

  /** Test setConnectorConfiguration throwing RuntimeException. */
  public void testRuntimeException() throws Exception {
    checkExceptionHandling(new RuntimeException(getName()),
                           ConnectorMessageCode.EXCEPTION_THROWABLE);
  }

  private void checkExceptionHandling(Exception exception, int expectedCode)
      throws Exception {
    manager = new ExceptionalManager(exception);
    language = "en";
    connectorName = "connectorA";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "valueA3");
    configXml = "<?xml?><beans><bean id=\"NewConfigXML\"/></beans>";
    update = false;
    SetConnectorConfigHandler hdl = doTest(setXMLBody());
    assertEquals(expectedCode, hdl.getStatus().getMessageId());

    // Avoid a bug in GSA that displays "No connector configuration
    // returned by the connector manager.", rather than the error status.
    assertNotNull(hdl.getConfigRes());
  }

  /** Test setConnectorConfiguration returning a ConfigureResponse. */
  public void testSetInvalidConfig() throws Exception {
    manager = new FailConfigurationManager();
    language = "en";
    connectorName = "connectorA";
    connectorType = "documentum";
    configData = new TreeMap<String, String>();
    configData.put("name1", "valueA1");
    configData.put("name2", "valueA2");
    configData.put("name3", "InvalidValue");
    configXml = "<?xml?><beans><bean id=\"NewConfigXML\"/></beans>";
    update = false;

    SetConnectorConfigHandler hdl = doTest(setXMLBody());
    assertEquals(ConnectorMessageCode.INVALID_CONNECTOR_CONFIG,
                 hdl.getStatus().getMessageId());
    assertNotNull(hdl.getConfigRes());
    assertNotNull(hdl.getConfigRes().getMessage());
  }

  /** Do test, expecting a successful return status. */
  private void doTest() throws Exception {
    SetConnectorConfigHandler hdl = doTest(setXMLBody());
    ConnectorMessageCode status = hdl.getStatus();
    ConfigureResponse response = hdl.getConfigRes();
    assertTrue("Status: Code=" + status.getMessageId() + "  Message="
        + status.getMessage() + ((response == null) ? "" :
        "  Response: Message=" + response.getMessage()),
        status.isSuccess());
  }

  /** Test the handler, returning the handler. */
  private SetConnectorConfigHandler doTest(String xmlBody) throws Exception {
    LOGGER.info("xmlBody: " + xmlBody);
    String name = connectorName;

    Configuration origConfig = null;
    if (update) {
      origConfig = manager.getConnectorConfiguration(connectorName);
    } else {
      // GSA 5.2 wants connectorNames to be lower case.  The handler
      // will lowercase for us, but only on new connectors; not on update.
      name = name.toLowerCase();
    }

    SetConnectorConfigHandler hdl = new SetConnectorConfigHandler(
        xmlBody, manager);
    LOGGER.info("ConnectorName: " + hdl.getConnectorName() +
                " this: " + connectorName);
    LOGGER.info("ConnectorType: " + hdl.getConnectorType() +
                " this: " + connectorType);
    if (hdl.getStatus().isSuccess()) {
      assertEquals(language, hdl.getLanguage());
      assertEquals(name, hdl.getConnectorName());
      assertEquals(connectorType, hdl.getConnectorType());
      assertEquals(update, hdl.isUpdate());
      assertEquals(configData, hdl.getConfigData());

      Configuration config = manager.getConnectorConfiguration(name);
      assertNotNull(config);
      assertEquals(connectorType, config.getTypeName());
      assertEquals(configData, config.getMap());
      if (configXml != null) {
        assertEquals(configXml, config.getXml());
        assertEquals(configXml, hdl.getConfigXml());
      } else if (origConfig != null) {
        assertEquals(origConfig.getXml(), config.getXml());
      } else {
        assertEquals(manager.getConnectorInstancePrototype(connectorType),
                     config.getXml());
      }
    }
    return hdl;
  }

  private String setXMLBody() throws Exception {
    String name = connectorName;
    if (!update ) {
      // GSA 5.2 wants connectorNames to be lower case.
      // But we can only enforce it for new connectors, not existing ones.
      name = name.toLowerCase();
    }
    String body =
      "<" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">\n" +
      "  <" + ServletUtil.QUERY_PARAM_LANG + ">" + language +
      "</" + ServletUtil.QUERY_PARAM_LANG + ">\n" +
      "  <" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">" + name +
      "</" + ServletUtil.XMLTAG_CONNECTOR_NAME + ">\n" +
      "  <" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">" + connectorType +
      "</" + ServletUtil.XMLTAG_CONNECTOR_TYPE + ">\n" +
      "  <" + ServletUtil.XMLTAG_UPDATE_CONNECTOR + ">" + update +
      "</" + ServletUtil.XMLTAG_UPDATE_CONNECTOR + ">\n";

    if (globalNamespace != null) {
       body += "  <" + ServletUtil.XMLTAG_GLOBAL_NAMESPACE + ">" +
           globalNamespace + "</" + ServletUtil.XMLTAG_GLOBAL_NAMESPACE+ ">\n";
    }
    if (localNamespace != null) {
       body += "  <" + ServletUtil.XMLTAG_LOCAL_NAMESPACE + ">" +
           localNamespace + "</" + ServletUtil.XMLTAG_LOCAL_NAMESPACE+ ">\n";
    }

    for (Map.Entry<String, String> entry : configData.entrySet()) {
      body += "  <" + ServletUtil.XMLTAG_PARAMETERS + " name=\""
          + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>\n";
    }

    if (configXml != null) {
      body += "  <" + ServletUtil.XMLTAG_CONNECTOR_CONFIG_XML + "><![CDATA["
        + configXml + "]]></" + ServletUtil.XMLTAG_CONNECTOR_CONFIG_XML + ">\n";
    }

    return body + "</" + ServletUtil.XMLTAG_CONNECTOR_CONFIG + ">";
  }

  /** A MockManager that saves Configurations and returns them. */
  private class ConfigSavingManager extends MockManager implements Manager {
    private HashMap<String, Configuration> configurations;

    public ConfigSavingManager() {
      super();
      configurations = new HashMap<String, Configuration>();
    }

    @Override
    public ConfigureResponse setConnectorConfiguration(String connectorName,
        Configuration configuration, String language, boolean update)
        throws ConnectorNotFoundException, ConnectorExistsException,
               PersistentStoreException, InstantiatorException {
      ConfigureResponse response = super.setConnectorConfiguration(
        connectorName, configuration, language, update);
      if (response == null) {
        if (configuration.getXml() == null) {
          configuration = new Configuration(configuration,
              getConnectorInstancePrototype(configuration.getTypeName()));
        }
        configurations.put(connectorName, configuration);
      }
      return response;
    }

    @Override
    public Configuration getConnectorConfiguration(String connectorName)
        throws ConnectorNotFoundException {
      Configuration config = configurations.get(connectorName);
      if (config == null) {
        throw new ConnectorNotFoundException(connectorName);
      }
      return config;
    }
  }

  /**
   * Throws either a RuntimeException, ConnectorNotFoundException,
   * ConnectorExistsException, PersistentStoreException, InstantiatorException.
   */
  private static void throwException(Exception exception)
    throws ConnectorNotFoundException, ConnectorExistsException,
           PersistentStoreException, InstantiatorException {
    if (exception instanceof ConnectorNotFoundException) {
      throw (ConnectorNotFoundException) exception;
    } else if (exception instanceof ConnectorExistsException) {
      throw (ConnectorExistsException) exception;
    } else if (exception instanceof InstantiatorException) {
      throw (InstantiatorException) exception;
    } else if (exception instanceof PersistentStoreException) {
      throw (PersistentStoreException) exception;
    } else if (exception instanceof RuntimeException) {
      // RuntimeExceptions don't need to be declared.
      throw (RuntimeException) exception;
    }
  }

  /** A MockManager that throws exception when setting configuration. */
  private class ExceptionalManager extends MockManager {
    private Exception exception;

    public ExceptionalManager(Exception exception) {
      this.exception = exception;
    }

    @Override
    public ConfigureResponse setConnectorConfiguration(String connectorName,
        Configuration configuration, String language, boolean update)
        throws ConnectorNotFoundException, ConnectorExistsException,
            PersistentStoreException, InstantiatorException {
      throwException(exception);
      return null;
    }
  }

  /** A MockManager that returns a failed configuration. */
  private class FailConfigurationManager extends MockManager {
    @Override
    public ConfigureResponse setConnectorConfiguration(String connectorName,
        Configuration configuration, String language, boolean update) {
      return new ConfigureResponse("setConnectorConfiguration: "
          + ((update) ? "update" : "add") + " " + connectorName + " "
          + configuration.toString(), null, null);
    }
  }
}

