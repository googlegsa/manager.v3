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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.test.ConnectorTestUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Tests GetConnectorList servlet class.
 */
public class GetConnectorListTest extends TestCase {
  private static final Logger LOGGER = Logger
    .getLogger(GetConnectorListTest.class.getName());

  /**
   * Test method for
   * {@link com.google.enterprise.connector.servlet.GetConnectorList
   * #handleDoPost(com.google.enterprise.connector.manager.Manager,
   * java.io.PrintWriter)}
   * where connectorTypes = null.
   *
   * @throws IOException
   */
  public void testHandleDoGet1() throws IOException {
    Manager manager = new MockManager() {
        public Set getConnectorTypeNames() { return null; }
      };
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>" + ConnectorMessageCode.RESPONSE_NULL_CONNECTOR_TYPE
        + "</StatusId>\n"
        + "</CmResponse>\n";
    doTest(manager, expectedResult);
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.servlet.GetConnectorList
   * #handleDoPost(com.google.enterprise.connector.manager.Manager,
   * java.io.PrintWriter)}
   * where connectorTypes = {"Documentum", "Filenet", "Sharepoint"}.
   *
   * @throws IOException
   */
  public void testHandleDoGet2() throws IOException {
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "  <ConnectorTypes>\n"
        + "    <ConnectorType>Documentum</ConnectorType>\n"
        + "    <ConnectorType>Filenet</ConnectorType>\n"
        + "    <ConnectorType>Sharepoint</ConnectorType>\n"
        + "  </ConnectorTypes>\n"
        + "</CmResponse>\n";
    doTest(MockManager.getInstance(), expectedResult);
  }

  private void doTest(Manager manager, String expectedResult)
      throws IOException {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    GetConnectorList.handleDoPost(manager, out);
    out.flush();
    StringBuffer result = writer.getBuffer();
    ConnectorTestUtils.removeManagerVersion(result);
    LOGGER.info(result.toString());
    LOGGER.info(expectedResult);
    Assert.assertEquals(StringUtils.normalizeNewlines(expectedResult),
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
  }
}
