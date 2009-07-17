// Copyright 2006-2008 Google Inc.  All Rights Reserved.
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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;


/**
 * Tests the GetConnectorStatus servlet class
 *
 */
public class GetConnectorStatusTest extends TestCase {
  private static final Logger LOG =
    Logger.getLogger(GetConnectorStatusTest.class.getName());

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.GetConnectorStatus
   * #handleDoGet(String, Manager, PrintWriter)}.
   *
   * connectorStatus = null
   *
   */
  public void testHandleDoGet2() {
    String name = null;
    int status = 0;
    doTestStatus(name, status);
  }

  public void testHandleDoGet3() {
    String name = "connectorName";
    int status = 0;
    doTestStatus(name, status);
  }

  public void testHandleDoGet4() {
    String name = "";
    int status = 0;
    doTestStatus(name, status);
  }

  public void testHandleDoGet5() {
    String name = "connectorName";
    int status = 0;
    doTestStatus(name, status);
  }

  private void doTestStatus(String name, int status) {
    String type = "Documentum";
    String expectedResult =
        "<CmResponse>\n" +
        "  <StatusId>0</StatusId>\n" +
        "  <ConnectorStatus>\n" +
        "    <ConnectorName>" + name + "</ConnectorName>\n" +
        "    <ConnectorType>" + type + "</ConnectorType>\n" +
        "    <Status>" +  Integer.toString(status) +  "</Status>\n" +
        "    <ConnectorSchedules version=\"3\">" + name + ":100:0:0-0</ConnectorSchedules>\n" +
        "  </ConnectorStatus>\n" +
        "</CmResponse>\n";
    doTest(name, expectedResult);
  }

  private void doTest(String connectorName,
                      String expectedResult) {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    Manager manager = MockManager.getInstance();
    GetConnectorStatus.handleDoGet(connectorName, manager, out);
    out.flush();
    StringBuffer result = writer.getBuffer();
    LOG.info(result.toString());
    LOG.info(expectedResult);
    Assert.assertEquals (StringUtils.normalizeNewlines(expectedResult),
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
  }
}
