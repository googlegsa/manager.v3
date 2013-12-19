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

package com.google.enterprise.connector.servlet;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.enterprise.connector.common.PropertiesUtils;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.ConfigureResponse;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

/**
 * Tests {@link GetConnectorConfigToEdit} Servlet.
 */
public class GetConnectorConfigToEditTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(GetConnectorConfigToEditTest.class.getName());

  private String connectorType = "TestConnectorA";
  private File connectorDir = new File(Context.DEFAULT_JUNIT_COMMON_DIR_PATH
      + "connectors/" + connectorType);
  private Manager manager;
  private Instantiator instantiator;

  @Override
  protected void setUp() {
    // Clear out any old connector state files.
    ConnectorTestUtils.deleteAllFiles(connectorDir);

    // Create a stand alone context with real ProductionManager.
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    context.setFeeding(false);
    context.start();
    manager = context.getManager();
    instantiator = context.getInstantiator();
  }

  @Override
  protected void tearDown() {
    // Clear out any old connector state files.
    ConnectorTestUtils.deleteAllFiles(connectorDir);
  }

  /** Test ConnectorNotFound returns status code. */
  public void testConnectorNotFound() throws Exception {
    String connectorName = "UnknownConnector";
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>5303</StatusId>\n" +
      "  <CMParams Order=\"0\" CMParam=\""+ connectorName + "\"/>\n" +
      "</CmResponse>\n";
    doTest(connectorName, expectedResult);
  }

  /** Test InstantiatorException returns status code. */
  public void testInstantiatorException() throws Exception {
    manager = new ExceptionThrowingManager();
    String connectorName = "UnknownConnector";
    String expectedResult =
      "<CmResponse>\n" +
      "  <StatusId>5305</StatusId>\n" +
      "</CmResponse>\n";
    doTest(connectorName, expectedResult);
  }

  /** A MockManager that throws exception when getting config form. */
  private class ExceptionThrowingManager extends MockManager {
    @Override
    public ConfigureResponse getConfigFormForConnector(String connectorName,
        String language) throws InstantiatorException {
      throw new InstantiatorException("getConfigFormForConnector: "
          + "connectorName = " + connectorName);
    }
  }

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.
   * GetConnectorConfigToEdit#handleDoGet(String, String, Manager, PrintWriter)}.
   */
  public void testHandleDoGet() throws Exception {
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n"
        + "    <FormSnippet><![CDATA[<tr>\n"
        + "<td>Username</td>\n"
        + "<td>\n"
        + "<input type=\"text\" name=\"Username\" />\n"
        + "</td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Password</td>\n"
        + "<td>\n"
        + "<input type=\"password\" name=\"Password\" />\n"
        + "</td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Color</td>\n"
        + "<td>\n"
        + "<input type=\"text\" name=\"Color\" />\n"
        + "</td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Repository File</td>\n"
        + "<td>\n"
        + "<input type=\"text\" name=\"Repository File\" />\n"
        + "</td>\n"
        + "</tr>\n"
        + "]]></FormSnippet>\n"
        + "    <message>Sample form for connectorAlang en</message>\n"
        + "  </ConfigureResponse>\n"
        + "</CmResponse>\n";
    String connectorName = "connectorA";
    manager = MockManager.getInstance();
    doTest(connectorName, expectedResult);
  }

  /**
   * Tests case where config values have reserved XML characters.
   */
  public void testHandleDoGetWithXml() throws Exception {
    String connectorName = "xml-con-01";
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n"
        + "    <FormSnippet><![CDATA[<tr>\n"
        + "<td>Username</td>\n"
        + "<td><input name=\"Username\""
        + " type=\"text\" value=\" &quot;>bob>&amp;<alice;'\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Password</td>\n"
        + "<td><input name=\"Password\""
        + " type=\"password\" value=\"****************\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Color</td>\n"
        + "<td><input name=\"Color\""
        + " type=\"text\" value=\" &quot;>bob>&amp;<alice;'\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>RepositoryFile</td>\n"
        + "<td><input name=\"RepositoryFile\""
        + " type=\"text\" value=\"MockRepositoryEventLog1.txt\"></td>\n"
        + "</tr>\n"
        + "]]></FormSnippet>\n"
        + "    <ConnectorConfigXml><![CDATA["
        + instantiator.getConnectorInstancePrototype(connectorType)
        + "]]></ConnectorConfigXml>\n"
        + "  </ConfigureResponse>\n"
        + "</CmResponse>\n";

    // Use the manager directly to create a connector with properties that have
    // reserved XML characters in them.
    Map<String, String> configData = new HashMap<String, String>();
    String evilValue = " \">bob>&<alice;'";
    configData.put("Username", evilValue);
    configData.put("Password", evilValue);
    configData.put("Color", evilValue);
    configData.put("RepositoryFile", "MockRepositoryEventLog1.txt");
    manager.setConnectorConfiguration(connectorName,
        new Configuration(connectorType, configData, null),
        "en", false);

    // Check the properties values to make sure they were not encoded.
    Map<String, String> retrievedData =
        manager.getConnectorConfiguration(connectorName).getMap();
    assertEquals(evilValue, retrievedData.get("Username"));
    assertEquals(evilValue, retrievedData.get("Password"));
    assertEquals(evilValue, retrievedData.get("Color"));

    // Use the Servlet to get the populated config form.  Make sure it can be
    // parsed and make sure the reserved XML properties are preserved.
    doTest(connectorName, expectedResult);
  }

  /**
   * Tests case where config values have reserved XML characters.
   */
  public void testHandleDoGetWithModifiedXml() throws Exception {
    String connectorName = "xml-con-02";
    String connectorInstancePrototype =
        instantiator.getConnectorInstancePrototype(connectorType);
    String connectorInstanceXml = connectorInstancePrototype.replace(
        "TestConnectorAInstance", "ModifiedTestConnectorAInstance");
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n"
        + "    <FormSnippet><![CDATA[<tr>\n"
        + "<td>Username</td>\n"
        + "<td><input name=\"Username\""
        + " type=\"text\" value=\" &quot;>bob>&amp;<alice;'\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Password</td>\n"
        + "<td><input name=\"Password\""
        + " type=\"password\" value=\"****************\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>Color</td>\n"
        + "<td><input name=\"Color\""
        + " type=\"text\" value=\" &quot;>bob>&amp;<alice;'\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td>RepositoryFile</td>\n"
        + "<td><input name=\"RepositoryFile\""
        + " type=\"text\" value=\"MockRepositoryEventLog1.txt\"></td>\n"
        + "</tr>\n"
        + "]]></FormSnippet>\n"
        + "    <ConnectorConfigXml><![CDATA["
        + connectorInstanceXml
        + "]]></ConnectorConfigXml>\n"
        + "  </ConfigureResponse>\n"
        + "</CmResponse>\n";


    // Use the manager directly to create a connector with properties that have
    // reserved XML characters in them.
    Map<String, String> configData = new HashMap<String, String>();
    String evilValue = " \">bob>&<alice;'";
    configData.put("Username", evilValue);
    configData.put("Password", evilValue);
    configData.put("Color", evilValue);
    configData.put("RepositoryFile", "MockRepositoryEventLog1.txt");
    manager.setConnectorConfiguration(connectorName,
        new Configuration(connectorType, configData, connectorInstanceXml),
        "en", false);

    // Check the modifed connectorInstance.xml is used.
    assertEquals(connectorInstanceXml,
        manager.getConnectorConfiguration(connectorName).getXml());

    // Use the Servlet to get the populated config form.  Make sure it can be
    // parsed and make sure the reserved XML properties are preserved.
    doTest(connectorName, expectedResult);
  }

  private void doTest(String connectorName, String expectedResult)
      throws Exception {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);

    // Use the Servlet to get the populated config form.  Make sure it can
    // be parsed and make sure the reserved XML properties are preserved.
    GetConnectorConfigToEdit.handleDoGet(connectorName, "en", manager, out);
    out.flush();
    String result = writer.toString();
    out.close();
    LOGGER.info("Expected Response:\n" + expectedResult);
    LOGGER.info("Actual Response:\n" + result);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 ConnectorTestUtils.removeColRowSpan(
                 StringUtils.normalizeNewlines(result)));
  }
}
