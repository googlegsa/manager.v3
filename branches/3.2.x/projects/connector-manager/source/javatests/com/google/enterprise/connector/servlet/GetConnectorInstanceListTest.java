// Copyright 2011 Google Inc.
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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.Configuration;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Tests {@link GetConnectorInstanceList} Servlet.
 */
public class GetConnectorInstanceListTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(GetConnectorInstanceListTest.class.getName());

  private String connectorName = "connector1";
  private String connectorType = "TestConnectorA";
  private File connectorDir = new File(Context.DEFAULT_JUNIT_COMMON_DIR_PATH
      + "connectors/" + connectorType);
  private Manager manager;
  private MockHttpServletRequest req;
  private MockHttpServletResponse res;

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

    req = new MockHttpServletRequest();
    res = new MockHttpServletResponse();
  }

  @Override
  protected void tearDown() {
    // Clear out any old connector state files.
    ConnectorTestUtils.deleteAllFiles(connectorDir);
  }

  /** Test with no connector instances. */
  public void testNoConnectors() throws Exception {
    String expectedResult =
        "<CmResponse>\n  <StatusId>5215</StatusId>\n</CmResponse>\n";
    doTest(expectedResult);
  }

  /**
   * Test with connector instance, no Schedule and namespaces.
   */
  public void testWithConnectorNoScheduleAndNamespaces() throws Exception {
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "  <ConnectorInstances>\n"
        + "    <ConnectorInstance>\n"
        + "      <ConnectorName>connector1</ConnectorName>\n"
        + "      <ConnectorType>TestConnectorA</ConnectorType>\n"
        + "      <Status>0</Status>\n"
        + "      <ConnectorSchedules version=\"3\">#:500:300000:0-0"
        + "</ConnectorSchedules>\n"
        + "      <ConnectorSchedule version=\"1\">:500:0-0"
        + "</ConnectorSchedule>\n"
        + "    </ConnectorInstance>\n"
        + "  </ConnectorInstances>\n"
        + "</CmResponse>\n";

    addConnector(null);
    doTest(expectedResult);
  }

  /**
   * Test with connector instance with Schedule.
   */
  public void testWithConnector() throws Exception {
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "  <ConnectorInstances>\n"
        + "    <ConnectorInstance>\n"
        + "      <ConnectorName>connector1</ConnectorName>\n"
        + "      <ConnectorType>TestConnectorA</ConnectorType>\n"
        + "      <Status>0</Status>\n"
        + "      <ConnectorSchedules version=\"3\">#connector1:200:300000:1-12"
        + "</ConnectorSchedules>\n"
        + "      <ConnectorSchedule version=\"1\">connector1:200:1-12"
        + "</ConnectorSchedule>\n"
        + "      <GlobalNamespace>ThinkGlobally</GlobalNamespace>\n"
        + "      <LocalNamespace>ActLocally</LocalNamespace>\n"
        + "    </ConnectorInstance>\n"
        + "  </ConnectorInstances>\n"
        + "</CmResponse>\n";

    Map<String, String> additionalConfigData = new HashMap<String, String>();
    additionalConfigData.put("googleGlobalNamespace", "ThinkGlobally");
    additionalConfigData.put("googleLocalNamespace", "ActLocally");
    addConnector(additionalConfigData);
    manager.setSchedule(connectorName, "#connector1:200:300000:1-12");
    doTest(expectedResult);
  }

  private void addConnector(Map<String, String> additionalConfig)
      throws Exception {
    // Use the manager directly to create a connector.
    HashMap<String, String> configData = new HashMap<String, String>();
    configData.put("Username", "bob");
    configData.put("Password", "pwd");
    configData.put("Color", "red");
    configData.put("RepositoryFile", "MockRepositoryEventLog1.txt");
    if (additionalConfig != null) {
      configData.putAll(additionalConfig);
    }
    manager.setConnectorConfiguration(connectorName,
        new Configuration(connectorType, configData, null),
        "en", false);
  }

  private void doTest(String expectedResult) throws Exception {
    new GetConnectorInstanceList().doGet(req, res);
    StringBuffer result = new StringBuffer(res.getContentAsString());
    ConnectorTestUtils.removeManagerVersion(result);
    removeConnectorVersion(result);
    assertEquals(result.toString(),
        StringUtils.normalizeNewlines(expectedResult),
        StringUtils.normalizeNewlines(result.toString()));
  }

  /**
   * Removes the connector manager version string from the buffer.
   * This allows the tests that compare actual output to expected
   * output to function across versions, jvms, and platforms.
   */
  private void removeConnectorVersion(StringBuffer buffer) {
    int start = buffer.indexOf("      <" + ServletUtil.XMLTAG_VERSION + ">");
    if (start >= 0) {
      buffer.delete(start, buffer.indexOf("\n", start) + 1);
    }
  }
}
