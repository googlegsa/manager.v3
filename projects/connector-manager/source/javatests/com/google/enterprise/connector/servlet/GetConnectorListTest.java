// Copyright 2006-2009 Google Inc.  All Rights Reserved.
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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Tests GetConnectorList servlet class.
 */
public class GetConnectorListTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(GetConnectorListTest.class.getName());

  /**
   * Test method for
   * {@link GetConnectorList#doGet(HttpServletRequest, HttpServletResponse)}.
   */
  public void testDoGet() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET",
        "/connector-manager/getConnectorList");
    MockHttpServletResponse res = new MockHttpServletResponse();
    new GetConnectorList().doGet(req, res);
    String expectedResult =
        "<CmResponse>\n"
        + "  <StatusId>" + ConnectorMessageCode.RESPONSE_NULL_CONNECTOR_TYPE
        + "</StatusId>\n"
        + "</CmResponse>\n";
    checkResponse(new StringBuffer(res.getContentAsString()), expectedResult);
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.servlet.GetConnectorList
   * #handleDoPost(com.google.enterprise.connector.manager.Manager,
   * java.io.PrintWriter)}
   * where connectorTypes = null.
   */
  public void testNoConnectorTypes() {
    Manager manager = new MockManager() {
        @Override
        public Set<String> getConnectorTypeNames() { return null; }
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
   */
  public void testMultipleConnectorTypes() {
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

  private void doTest(Manager manager, String expectedResult) {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    GetConnectorList.handleDoPost(manager, out);
    out.flush();
    StringBuffer result = writer.getBuffer();
    out.close();
    checkResponse(result, expectedResult);
  }

  private void checkResponse(StringBuffer result, String expectedResult) {
    ConnectorTestUtils.removeManagerVersion(result);
    LOGGER.info(result.toString());
    LOGGER.info(expectedResult);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 StringUtils.normalizeNewlines(result.toString()));
  }
}
