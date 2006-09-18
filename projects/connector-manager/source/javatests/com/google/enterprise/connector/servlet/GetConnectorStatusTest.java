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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.ConnectorStatus;


/**
 * Tests the GetConnectorStatus servlet class
 *
 */
public class GetConnectorStatusTest extends TestCase {
  private static final Logger LOG =
    Logger.getLogger(GetConnectorStatusTest.class.getName());

  /**
   * Test method for {@link com.google.enterprise.connector.servlet.GetConnectorStatus
   * #handleDoGet(java.io.PrintWriter, com.google.enterprise.connector.manager.ConnectorStatus)}.
   *
   * connectorStatus = null
   * 
   */
  public void testHandleDoGet() throws IOException {
    ConnectorStatus connectorStatus = null;
    String expectedResult =
        "<CmResponse>\n" +
        "  <StatusId>0</StatusId>\n" +
        "  <ConnectorStatus>null</ConnectorStatus>\n" +
        "</CmResponse>\n";
    doTest(connectorStatus, expectedResult);
  }

  public void testHandleDoGet2() throws IOException {
    String name = null;
    String type = "Documentum";
    int status = 0;
    doTestStatus(name, type, status);
  }

  public void testHandleDoGet3() throws IOException {
    String name = "connectorName";
    String type = null;
    int status = 0;
    doTestStatus(name, type, status);
  }

  public void testHandleDoGet4() throws IOException {
    String name = "";
    String type = "Documentum";
    int status = 0;
    doTestStatus(name, type, status);
  }
 
  public void testHandleDoGet5() throws IOException {
    String name = "connectorName";
    String type = "Documentum";
    int status = 0;
    doTestStatus(name, type, status);
  }

  private void doTestStatus(String name, String type, int status) throws IOException {
    ConnectorStatus connectorStatus =
        new ConnectorStatus(name, type, status);
    String expectedResult =
        "<CmResponse>\n" +
        "  <StatusId>0</StatusId>\n" +
        "  <ConnectorStatus>\n" +
        "    <ConnectorName>" + name + "</ConnectorName>\n" +
        "    <ConnectorType>" + type + "</ConnectorType>\n" +
        "    <Status>" +  Integer.toString(status) +  "</Status>\n" +
        "  </ConnectorStatus>\n" +
        "</CmResponse>\n";
    doTest(connectorStatus, expectedResult);
  }

  private void doTest(ConnectorStatus connectorStatus,
                      String expectedResult)
      throws IOException {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    GetConnectorStatus.handleDoGet(out, connectorStatus);
    out.flush();
    StringBuffer result = writer.getBuffer();
    LOG.info(result.toString());
    LOG.info(expectedResult);
    Assert.assertEquals (StringUtils.normalizeNewlines(expectedResult), 
        StringUtils.normalizeNewlines(result.toString()));
  }
}
