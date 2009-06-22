// Copyright (C) 2006-2009 Google Inc.
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
import com.google.enterprise.connector.manager.MockManager;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Tests the AuthenticateTest servlet class
 *
 */
public class AuthenticateTest extends TestCase {
  private static final Logger LOGGER =
    Logger.getLogger(AuthenticateTest.class.getName());

  public void testHandleDoPost() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Password>fooPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "    <Success ConnectorName=\"connector2\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";
    doTest(xmlBody, expectedResult, null, "fooUser", "fooPassword");
  }

  public void testWithDomain() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Domain>fooDomain</Domain>" +
      "    <Password>fooPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "    <Success ConnectorName=\"connector2\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";
    doTest(xmlBody, expectedResult, "fooDomain", "fooUser", "fooPassword");
  }

  public void testWithTwoConnectorNameElements() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Connectors>\n" +
      "    <Connectorname>connector1</Connectorname>\n" +
      "    <Connectorname>connector2</Connectorname>\n" +
      "  </Connectors>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Domain>fooDomain</Domain>" +
      "    <Password>fooPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "    <Success ConnectorName=\"connector2\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";
    // This shows that the proposed extension is ignored against current CMs.
    doTest(xmlBody, expectedResult, "fooDomain", "fooUser", "fooPassword");
  }

  public void testWithOneConnectorNameElement() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Connectors>\n" +
      "    <Connectorname>connector1</Connectorname>\n" +
      "  </Connectors>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Domain>fooDomain</Domain>" +
      "    <Password>fooPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    // In the next CM, this expected result should be changed to have only one
    // "Success" element.
    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "    <Success ConnectorName=\"connector2\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";
    doTest(xmlBody, expectedResult, "fooDomain", "fooUser", "fooPassword");
  }

  private void doTest(String xmlBody, String expectedResult, String domain,
      String username, String password) {
    LOGGER.info("xmlBody: " + xmlBody);
    MockManager manager = MockManager.getInstance();
    manager.setShouldVerifyIdentity(true);
    manager.setExpectedIdentity(domain, username, password);
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    Authenticate.handleDoPost(xmlBody, manager, out);
    out.flush();
    StringBuffer result = writer.getBuffer();
    LOGGER.info(result.toString());
    LOGGER.info(expectedResult);
    Assert.assertEquals(StringUtils.normalizeNewlines(expectedResult),
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
  }
}
