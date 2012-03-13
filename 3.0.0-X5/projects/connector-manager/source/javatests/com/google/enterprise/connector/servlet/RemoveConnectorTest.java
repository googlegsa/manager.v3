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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;

import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Tests RemoveConnector servlet
 *
 */
public class RemoveConnectorTest extends TestCase {

  private static final Logger LOGGER =
      Logger.getLogger(RemoveConnectorTest.class.getName());

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.RemoveConnector#
   * handleDoGet(String, Manager, PrintWriter)}.
   */
  public void testHandleDoGet1() {
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "</CmResponse>\n";
    String connectorName = "connect1";
    doTest(connectorName, expectedResult);
  }

  public void testHandleDoGet2() {
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>"
        + ConnectorMessageCode.EXCEPTION_CONNECTOR_NOT_FOUND
        + "</StatusId>\n"
        + "  <CMParams Order=\"0\" CMParam=\"connector2\"/>\n"
        + "</CmResponse>\n";
    String connectorName = "connector2";
    doTest(connectorName, expectedResult);
  }

  private void doTest(String connectorName, String expectedResult) {
    Manager manager = MockManager.getInstance();
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    RemoveConnector.handleDoGet(connectorName, manager, out);
    out.flush();
    String result = writer.toString();
    out.close();
    LOGGER.info("Expected Response:\n" + expectedResult);
    LOGGER.info("Actual Response:\n" + result);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 StringUtils.normalizeNewlines(result));
  }
}
