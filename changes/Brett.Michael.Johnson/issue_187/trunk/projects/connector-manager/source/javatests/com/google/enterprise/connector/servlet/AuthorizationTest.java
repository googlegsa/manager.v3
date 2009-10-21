// Copyright 2006-2009 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
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
 * Tests the Authorization servlet class
 *
 */
public class AuthorizationTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(AuthorizationTest.class.getName());

  protected static final String TEST_XML1 =
      "<AuthorizationQuery>\n" +
      "<ConnectorQuery>\n" +
      "  <Identity source=\"gsa\">CN=foo</Identity>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector1.localhost" +
         ServletUtil.DOCID + "foo1</Resource>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector2.localhost" +
         ServletUtil.DOCID + "foo2</Resource>\n" +
      "</ConnectorQuery>\n" +
      "<ConnectorQuery>\n" +
      "  <Identity source=\"connector\">username</Identity>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector3.localhost" +
         ServletUtil.DOCID + "foo3</Resource>\n" +
      "</ConnectorQuery>\n" +
      "</AuthorizationQuery>";

  private static final String TEST_XML2 =
      "<AuthorizationQuery>\n" +
      "<ConnectorQuery>\n" +
      "  <Identity source=\"gsa\">CN=foo</Identity>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + ".localhost" +
         ServletUtil.DOCID + "foo1</Resource>\n" +
      "</ConnectorQuery>\n" +
      "</AuthorizationQuery>";

  private static final String TEST_XML3 =
      "<AuthorizationQuery>\n" +
      "<ConnectorQuery>\n" +
      "  <Identity source=\"gsa\">CN=foo</Identity>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "Connector3.localhost" +
         "/doc?DOC=foo1</Resource>\n" +
      "</ConnectorQuery>\n" +
      "</AuthorizationQuery>";

  private static final String TEST_XML4 =
      "<AuthorizationQuery>\n" +
      "<ConnectorQuery>\n" +
      "  <Identity source=\"gsa\"></Identity>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector1.localhost" +
         ServletUtil.DOCID + "foo1</Resource>\n" +
      "</ConnectorQuery>\n" +
      "</AuthorizationQuery>";


  private static final String TEST_XML5 =
      "<AuthorizationQuery>\n" + "<ConnectorQuery>\n"
          + "  <Identity source=\"gsa\">username</Identity>\n"
          + "</ConnectorQuery>\n" + "</AuthorizationQuery>";

  /**
   */
  public void testHandleDoPost1() {
    String expectedResult =
        "<CmResponse>\n" + "  <AuthorizationResponse>\n" + "    <Answer>\n"
            + "      <Resource>" + ServletUtil.PROTOCOL
            + "connector1.localhost" + ServletUtil.DOCID + "foo1</Resource>\n"
            + "      <Decision>Permit</Decision>\n" + "    </Answer>\n"
            + "    <Answer>\n" + "      <Resource>" + ServletUtil.PROTOCOL
            + "connector2.localhost" + ServletUtil.DOCID + "foo2</Resource>\n"
            + "      <Decision>Permit</Decision>\n" + "    </Answer>\n"
            + "    <Answer>\n" + "      <Resource>" + ServletUtil.PROTOCOL
            + "connector3.localhost" + ServletUtil.DOCID + "foo3</Resource>\n"
            + "      <Decision>Permit</Decision>\n" + "    </Answer>\n"
            + "  </AuthorizationResponse>\n" + "  <StatusId>0</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_XML1, expectedResult);
  }

  /**
   * The connector name is null.
   */
  public void testHandleDoPost2() {
    String expectedResult =
        "<CmResponse>\n" + "  <StatusId>"
            + ConnectorMessageCode.RESPONSE_NULL_CONNECTOR + "</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_XML2, expectedResult);
  }

  /**
   * docid does not exist.
   */
  public void testHandleDoPost3() {
    String expectedResult =
        "<CmResponse>\n" + "  <StatusId>"
            + ConnectorMessageCode.RESPONSE_NULL_DOCID + "</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_XML3, expectedResult);
  }

  /**
   * The identity is null (empty).
   */
  public void testHandleDoPost4() {
    String expectedResult =
        "<CmResponse>\n" + "  <AuthorizationResponse>\n" + "    <Answer>\n"
            + "      <Resource>" + ServletUtil.PROTOCOL
            + "connector1.localhost" + ServletUtil.DOCID + "foo1</Resource>\n"
            + "      <Decision>Permit</Decision>\n" + "    </Answer>\n"
            + "  </AuthorizationResponse>\n" + "  <StatusId>0</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_XML4, expectedResult);
  }

  /**
   * The resource is null.
   */
  public void testHandleDoPost5() {
    String expectedResult =
        "<CmResponse>\n" + "  <StatusId>"
            + ConnectorMessageCode.RESPONSE_NULL_RESOURCE + "</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_XML5, expectedResult);
  }

  private static final String TEST_DOMAINSPECIFIC_IDENTITY =
    "<AuthorizationQuery>\n" 
    + "<ConnectorQuery>\n" 
    + "  <Identity domain=\"dom1\" source=\"gsa\">CN=foo</Identity>\n" 
    + "  <Resource>" + ServletUtil.PROTOCOL + "connector1.localhost" 
    + ServletUtil.DOCID + "foo1</Resource>\n" 
    + "  <Resource>" + ServletUtil.PROTOCOL + "connector2.localhost" 
    + ServletUtil.DOCID + "foo2</Resource>\n" 
    + "</ConnectorQuery>\n" 
    + "<ConnectorQuery>\n" 
    + "  <Identity domain=\"dom2\" source=\"connector\">username</Identity>\n" 
    + "  <Resource>" + ServletUtil.PROTOCOL + "connector3.localhost" 
    + ServletUtil.DOCID + "foo3</Resource>\n" 
    + "</ConnectorQuery>\n" 
    + "</AuthorizationQuery>";

  /**
   * The Identity is qualified by domain
   */
  public void testHandleDoPostDomainQualifiedId() {
    String expectedResult =
        "<CmResponse>\n" 
            + "  <AuthorizationResponse>\n" 
            + "    <Answer>\n"
            + "      <Resource>" + ServletUtil.PROTOCOL + "connector1.localhost" 
            + ServletUtil.DOCID + "foo1</Resource>\n"
            + "      <Decision>Permit</Decision>\n" 
            + "    </Answer>\n"
            + "    <Answer>\n" 
            + "      <Resource>" + ServletUtil.PROTOCOL + "connector2.localhost" 
            + ServletUtil.DOCID + "foo2</Resource>\n"
            + "      <Decision>Permit</Decision>\n" 
            + "    </Answer>\n"
            + "    <Answer>\n" 
            + "      <Resource>" + ServletUtil.PROTOCOL + "connector3.localhost" 
            + ServletUtil.DOCID + "foo3</Resource>\n"
            + "      <Decision>Permit</Decision>\n" 
            + "    </Answer>\n"
            + "  </AuthorizationResponse>\n" 
            + "  <StatusId>0</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_DOMAINSPECIFIC_IDENTITY, expectedResult);
  }

  private void doTest(String xmlBody, String expectedResult) {
    LOGGER.info("xmlBody: " + xmlBody);
    Manager manager = MockManager.getInstance();
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    AuthorizationHandler authorizationHandler =
        AuthorizationHandler.makeAuthorizationHandlerForTest(xmlBody, manager,
            out);
    authorizationHandler.handleDoPost();
    out.flush();
    StringBuffer result = writer.getBuffer();
    LOGGER.info(result.toString());
    LOGGER.info(expectedResult);
    Assert.assertEquals(StringUtils.normalizeNewlines(expectedResult),
        StringUtils.normalizeNewlines(result.toString()));
    out.close();
  }
}
