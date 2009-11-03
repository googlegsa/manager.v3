// Copyright (C) 2006-2008 Google Inc.
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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.persist.ConnectorExistsException;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.TestCase;

/**
 * Tests GetConnectorConfigToEdit Servlet.
 */
public class GetConnectorConfigToEditTest extends TestCase {
  private static final Logger LOG =
      Logger.getLogger(GetConnectorConfigToEditTest.class.getName());

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.
   * GetConnectorConfigToEdit#handleDoGet(String, String, Manager, PrintWriter)}.
   */
  public void testHandleDoGet() {
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
    Manager manager = MockManager.getInstance();
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    GetConnectorConfigToEdit.handleDoGet(connectorName, "en", manager, out);
    StringBuffer result = writer.getBuffer();
    LOG.info(result.toString());
    LOG.info(expectedResult);
    out.flush();
    assertEquals (StringUtils.normalizeNewlines(expectedResult),
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
  }

  /**
   * Tests case where config values have reserved XML characters. 
   */
  public void testHandleDoGetWithXml()
      throws ConnectorNotFoundException, ConnectorExistsException,
      PersistentStoreException, InstantiatorException {
    String expected =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "  <ConfigureResponse>\n"
        + "    <FormSnippet><![CDATA[<tr>\n"
        + "<td colspan=\"1\" rowspan=\"1\">Username</td>\n"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"Username\""
        + " type=\"text\" value=\"&quot;>bob>&amp;<alice;'\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td colspan=\"1\" rowspan=\"1\">Password</td>\n"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"Password\""
        + " type=\"password\" value=\"***************\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td colspan=\"1\" rowspan=\"1\">Color</td>\n"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"Color\""
        + " type=\"text\" value=\"&quot;>bob>&amp;<alice;'\"></td>\n"
        + "</tr>\n"
        + "<tr>\n"
        + "<td colspan=\"1\" rowspan=\"1\">RepositoryFile</td>\n"
        + "<td colspan=\"1\" rowspan=\"1\"><input name=\"RepositoryFile\""
        + " type=\"text\" value=\"MockRepositoryEventLog1.txt\"></td>\n"
        + "</tr>\n"
        + "]]></FormSnippet>\n"
        + "  </ConfigureResponse>\n"
        + "</CmResponse>\n";      
    String connectorName = "xml-con-01";
    File connectorDir = new File(Context.DEFAULT_JUNIT_COMMON_DIR_PATH
        + "connectors/TestConnectorA/"
        + connectorName);

    // Clear out any old connector state files.
    ConnectorTestUtils.deleteAllFiles(connectorDir);

    // Create a stand alone context with real ProductionManager.
    Context.refresh();
    Context context = Context.getInstance();
    context.setStandaloneContext(Context.DEFAULT_JUNIT_CONTEXT_LOCATION,
        Context.DEFAULT_JUNIT_COMMON_DIR_PATH);
    context.setFeeding(false);
    context.start();
    Manager manager = context.getManager();

    // Use the manager directly to create a connector with properties that have
    // reserved XML characters in them.
    Map<String, String> configData = new HashMap<String, String>();
    String evilValue = "\">bob>&<alice;'";
    configData.put("Username", evilValue);
    configData.put("Password", evilValue);
    configData.put("Color", evilValue);
    configData.put("RepositoryFile", "MockRepositoryEventLog1.txt");
    manager.setConnectorConfig(connectorName, "TestConnectorA", configData,
        "en", false);
    
    // Use the Servlet to get the populated config form.  Make sure it can be
    // parsed and make sure the reserved XML properties are preserved.
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    GetConnectorConfigToEdit.handleDoGet(connectorName, "en", manager, out);
    StringBuffer result = writer.getBuffer();
    LOG.info(result.toString());
    out.flush();
    assertEquals (StringUtils.normalizeNewlines(expected),
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
    
    // Check the properties values to make sure they were not encoded.
    Map<String, String> retrievedData =
        manager.getConnectorConfig(connectorName);
    assertEquals(evilValue, retrievedData.get("Username"));
    assertEquals(evilValue, retrievedData.get("Password"));
    assertEquals(evilValue, retrievedData.get("Color"));

    // Cleanup.
    ConnectorTestUtils.deleteAllFiles(connectorDir);
  }
}
