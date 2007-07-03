// Copyright 2006 Google Inc. All Rights Reserved.
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

  private static final String TEST_XML1 =
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

  private static final String TWO_IDENTITIES_TWO_CONNECTORS =
      "<AuthorizationQuery>\n"
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1a</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" +
                "doc?docid=doc2a</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1b</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" +
                "doc?docid=doc2b</Resource>\n"
          + "</ConnectorQuery>\n"
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username2</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1c</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/doc?" +
                "docid=doc2c</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/doc?" +
                "docid=doc1d</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/doc?" +
                "docid=doc2d</Resource>\n"
          + "</ConnectorQuery>\n" + "</AuthorizationQuery>\n";
  private static final String ONE_IDENTITY_TWO_QUERIES =
      "<AuthorizationQuery>\n"
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1a</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" +
                "doc?docid=doc2a</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1b</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" +
                "doc?docid=doc2b</Resource>\n"
          + "</ConnectorQuery>\n"
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1c</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" +
                "doc?docid=doc2c</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1d</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" +
                "doc?docid=doc2d</Resource>\n"
          + "</ConnectorQuery>\n" + "</AuthorizationQuery>\n";
  private static final String TWO_IDENTITIES_MULTIPLE_QUERIES =
      "<AuthorizationQuery>\n"
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username1</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1a</Resource>\n"
          + "</ConnectorQuery>\n"
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username2</Identity>\n"
          + "  <Resource>googleconnector://connector2.localhost/" +
                "doc?docid=doc2a</Resource>\n"
          + "</ConnectorQuery>\n"
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username1</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" +
                "doc?docid=doc1b</Resource>\n"
          + "</ConnectorQuery>\n"
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username2</Identity>\n"
          + "  <Resource>googleconnector://connector2.localhost/" +
                "doc?docid=doc2b</Resource>\n"
          + "</ConnectorQuery>\n" + "</AuthorizationQuery>\n";

  private static final String MALFORMED_XML =
      "<AuthorizationQuery>\n" + "<ConnectorQuery>\n"
          + "</AuthorizationQuery>\n" + "";


  public void testParsing() {
    {
      AuthorizationParser authorizationParser = 
        new AuthorizationParser(TEST_XML1);
      authorizationParser.parse();
      Assert.assertEquals(2, authorizationParser.countParsedIdentities());
      Assert.assertEquals(1, authorizationParser
          .countConnectorsForIdentity("username"));
      Assert.assertEquals(2, authorizationParser
          .countConnectorsForIdentity("CN=foo"));
      Assert.assertEquals(1, authorizationParser
          .countUrlsForIdentityConnectorPair("username", "connector3"));
    }
    {
      AuthorizationParser authorizationParser =
          new AuthorizationParser(TWO_IDENTITIES_TWO_CONNECTORS);
      authorizationParser.parse();
      Assert.assertEquals(2, authorizationParser.countParsedIdentities());
      Assert.assertEquals(2, authorizationParser
          .countConnectorsForIdentity("username"));
      Assert.assertEquals(2, authorizationParser
          .countConnectorsForIdentity("username2"));
      Assert.assertEquals(2, authorizationParser
          .countUrlsForIdentityConnectorPair("username", "connector2"));
    }
    {
      AuthorizationParser authorizationParser = 
        new AuthorizationParser(ONE_IDENTITY_TWO_QUERIES);
      authorizationParser.parse();
      Assert.assertEquals(1, authorizationParser.countParsedIdentities());
      Assert.assertEquals(2, authorizationParser
          .countConnectorsForIdentity("username"));
      Assert.assertEquals(4, authorizationParser
          .countUrlsForIdentityConnectorPair("username", "connector2"));
    }
    {
      AuthorizationParser authorizationParser =
          new AuthorizationParser(TWO_IDENTITIES_MULTIPLE_QUERIES);
      authorizationParser.parse();
      Assert.assertEquals(2, authorizationParser.countParsedIdentities());
      Assert.assertEquals(1, authorizationParser
          .countConnectorsForIdentity("username1"));
      Assert.assertEquals(0, authorizationParser
          .countUrlsForIdentityConnectorPair("username1", "connector2"));
    }
    {
      AuthorizationParser authorizationParser = 
        new AuthorizationParser(MALFORMED_XML);
      authorizationParser.parse();
      Assert.assertNull(authorizationParser.getParseMap());
      Assert.assertEquals(ConnectorMessageCode.ERROR_PARSING_XML_REQUEST,
          authorizationParser.getStatus());
    }
  }

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
