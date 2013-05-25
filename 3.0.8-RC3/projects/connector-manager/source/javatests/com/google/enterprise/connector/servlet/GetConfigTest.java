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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Tests {@link GetConfig} Servlet.
 */
public class GetConfigTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(GetConfigTest.class.getName());

  private static final String APPLICATION_CONTEXT =
      "testdata/contextTests/GetConfigTest.xml";
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
    context.setStandaloneContext(APPLICATION_CONTEXT,
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

  private void addConnector() throws Exception {
    // Use the manager directly to create a connector.
    HashMap<String, String> configData = new HashMap<String, String>();
    configData.put("Username", "bob");
    configData.put("Password", "pwd");
    configData.put("Color", "red");
    configData.put("RepositoryFile", "MockRepositoryEventLog1.txt");
    manager.setConnectorConfiguration(connectorName,
        new Configuration(connectorType, configData, null),
        "en", false);
    manager.setSchedule(connectorName, "#connector1:200:300000:1-12");
  }

  private String expectedResult =
        "<Manager>\n"
      + "  <ManagerConfig>\n"
      + "    <Param name=\"googlePropertiesVersion\" value=\"3\"/>\n"
      + "  </ManagerConfig>\n"
      + "  <ManagerConfigXml name=\"GetConfigTest.xml\">\n\n"
      + "  </ManagerConfigXml>\n"
      + "  <ConnectorInstances>\n"
      + "    <ConnectorInstance>\n"
      + "      <ConnectorName>connector1</ConnectorName>\n"
      + "      <ConnectorSchedules version=\"3\">\n"
      + "        <disabled>true</disabled>\n"
      + "        <load>200</load>\n"
      + "        <RetryDelayMillis>300000</RetryDelayMillis>\n"
      + "        <TimeIntervals>1-12</TimeIntervals>\n"
      + "      </ConnectorSchedules>\n"
      + "      <ConnectorConfig>\n"
      + "        <Param name=\"Color\" value=\"red\"/>\n"
      + "        <Param name=\"Password\" value=\"\"/>\n"
      + "        <Param name=\"RepositoryFile\""
      + " value=\"MockRepositoryEventLog1.txt\"/>\n"
      + "        <Param name=\"Username\" value=\"bob\"/>\n"
      + "        <Param name=\"googlePropertiesVersion\" value=\"3\"/>\n"
      + "      </ConnectorConfig>\n"
      + "      <ConnectorConfigXml>\n\n"
      + "      </ConnectorConfigXml>\n"
      + "    </ConnectorInstance>\n"
      + "  </ConnectorInstances>\n"
      + "</Manager>\n";

  /**
   * Test with connector instance with Schedule.
   */
  public void testWithConnector() throws Exception {
    addConnector();
    new GetConfig().doGet(req, res);
    StringBuffer result = new StringBuffer(res.getContentAsString());
    ConnectorTestUtils.removeManagerVersion(result);
    /* Disabled because GetConfig servlet has been reverted to 2.x version.
    // Remove the ConnectorType version.
    removeConnectorVersion(result, "TestConnectorA");

    // Drop the Connector Manager bean config.
    // But check it at least defines the Manager.
    removeCdata(result, "ProductionManager");

    // Drop the connectorInstance.xml, but make sure it
    // defines the connector instance.
    removeCdata(result, "TestConnectorAInstance");

    // Remove the encrypted password values.
    removePasswords(result);

    assertEquals(StringUtils.normalizeNewlines(expectedResult),
        StringUtils.normalizeNewlines(result.toString()));
    */
  }

  /** Removes the ConnectorType, which may contain a  version string. */
  private void removeConnectorVersion(StringBuffer buffer, String checkStr) {
    removeAndCheck(buffer, "      <ConnectorType", ">\n", checkStr);
  }

  /**
   * Removes a CDATA block, but first verifies that it contains the
   * supplied string.
   */
  private void removeCdata(StringBuffer buffer, String checkStr) {
    removeAndCheck(buffer, ServletUtil.XML_CDATA_START,
                   ServletUtil.XML_CDATA_END, checkStr);
  }

  /**
   * Removes the specified block of text, identified by the start
   * and end strings, but verify that the check string is between them.
   */
  private void removeAndCheck(StringBuffer buffer, String startStr,
                              String endStr, String checkStr) {
    int start = buffer.indexOf(startStr);
    assertTrue(start >= 0);
    int stop = buffer.indexOf(endStr, start);
    assertTrue(stop > start);
    int match = buffer.indexOf(checkStr, start);
    assertTrue(match > start && match < stop);
    buffer.delete(start, stop + endStr.length());
  }

  /** Removes password values. */
  private void removePasswords(StringBuffer buffer) {
    String password = "assword\" value=\"";
    int start = 0;
    while ((start = buffer.indexOf(password, start)) >= 0) {
      int stop = buffer.indexOf("\"/>", start);
      buffer.delete(start + password.length(), stop);
      start += password.length();
    }
  }
}
