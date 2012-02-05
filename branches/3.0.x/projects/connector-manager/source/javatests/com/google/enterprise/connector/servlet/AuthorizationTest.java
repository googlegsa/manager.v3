// Copyright 2006 Google Inc.
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
import com.google.enterprise.connector.manager.MockManager;

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
      "  <Identity/>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector1.localhost" +
         ServletUtil.DOCID + "foo1</Resource>\n" +
      "</ConnectorQuery>\n" +
      "</AuthorizationQuery>";

  private static final String TEST_XML5 =
      "<AuthorizationQuery>\n" + "<ConnectorQuery>\n"
          + "  <Identity source=\"gsa\">username</Identity>\n"
          + "</ConnectorQuery>\n" + "</AuthorizationQuery>";

  /** Test invalid (empty) XML request. */
  public void testInvalidRequest() throws Exception {
    String expectedResult = "<CmResponse>\n" + "  <StatusId>"
         + ConnectorMessageCode.ERROR_PARSING_XML_REQUEST + "</StatusId>\n"
         + "</CmResponse>\n";
    doTest("", expectedResult, false, null, null, null);
  }

  /**
   */
  public void testHandleDoPost1() {
    String expectedResult =
        "<CmResponse>\n" + "  <AuthorizationResponse>\n" + "    <Answer>\n"
            + "      <Resource connectorname=\"connector1\">"
            + ServletUtil.PROTOCOL
            + "connector1.localhost" + ServletUtil.DOCID + "foo1</Resource>\n"
            + "      <Decision>PERMIT</Decision>\n" + "    </Answer>\n"
            + "    <Answer>\n" + "      <Resource connectorname=\"connector2\">"
            + ServletUtil.PROTOCOL
            + "connector2.localhost" + ServletUtil.DOCID + "foo2</Resource>\n"
            + "      <Decision>PERMIT</Decision>\n" + "    </Answer>\n"
            + "    <Answer>\n" + "      <Resource connectorname=\"connector3\">"
            + ServletUtil.PROTOCOL
            + "connector3.localhost" + ServletUtil.DOCID + "foo3</Resource>\n"
            + "      <Decision>PERMIT</Decision>\n" + "    </Answer>\n"
            + "  </AuthorizationResponse>\n" + "  <StatusId>0</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_XML1, expectedResult, false, null, null, null);
  }

  private static final String NULL_CONNECTOR_NAME_RESPONSE = "<CmResponse>\n"
      + "  <StatusId>"
      + ConnectorMessageCode.RESPONSE_NULL_CONNECTOR + "</StatusId>\n"
      + "</CmResponse>\n";

  private static final String TEST_BAD_URL_XML = "<AuthorizationQuery>\n"
    + "<ConnectorQuery>\n"
    + "  <Identity password=\"pass1\" source=\"gsa\">user1</Identity>\n"
    + "  <Resource>" + ServletUtil.PROTOCOL + "connector1.localhost"
    + ServletUtil.DOCID + "foo1</Resource>\n"
    + "  <Resource>" + ServletUtil.PROTOCOL + ".localhost"
    + ServletUtil.DOCID + "foo2</Resource>\n"
    + "</ConnectorQuery>\n"
    + "<ConnectorQuery>\n"
    + "  <Identity password=\"pass2\" source=\"connector\">user2</Identity>\n"
    + "  <Resource>" + ServletUtil.PROTOCOL + "connector3.localhost"
    + ServletUtil.DOCID + "foo3</Resource>\n"
    + "</ConnectorQuery>\n"
    + "</AuthorizationQuery>";

  /**
   * The connector name of one resource is null.  Any others should be
   * processed accordingly.
   */
  public void testHandleDoPost2() {
    String expectedBadDocumentResponse = "<CmResponse>\n"
      + "  <AuthorizationResponse>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector1\">"
      + ServletUtil.PROTOCOL + "connector1.localhost"
      + ServletUtil.DOCID + "foo1"
      + "</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector3\">"
      + ServletUtil.PROTOCOL + "connector3.localhost"
      + ServletUtil.DOCID + "foo3"
      + "</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "  </AuthorizationResponse>\n"
      + "  <StatusId>" + ConnectorMessageCode.RESPONSE_NULL_CONNECTOR
      + "</StatusId>\n"
      + "</CmResponse>\n";
    doTest(TEST_XML2, NULL_CONNECTOR_NAME_RESPONSE, false, null, null, null);
    doTest(TEST_BAD_URL_XML, expectedBadDocumentResponse,
        false, null, null, null);
  }

  /**
   * docid does not exist.
   */
  public void testHandleDoPost3() {
    String expectedResult =
        "<CmResponse>\n" + "  <StatusId>"
            + ConnectorMessageCode.RESPONSE_NULL_DOCID + "</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_XML3, expectedResult, false, null, null, null);
  }

  /**
   * The identity is null.
   */
  public void testHandleDoPost4() {
    String expectedResult =
        "<CmResponse>\n" + "  <StatusId>"
        + ConnectorMessageCode.RESPONSE_NULL_IDENTITY + "</StatusId>\n"
        + "  <StatusMsg>Null Identity</StatusMsg>\n"
        + "</CmResponse>\n";
    doTest(TEST_XML4, expectedResult, false, null, null, null);
  }

  /**
   * The resource is null.
   */
  public void testHandleDoPost5() {
    String expectedResult =
        "<CmResponse>\n" + "  <StatusId>"
            + ConnectorMessageCode.RESPONSE_NULL_RESOURCE + "</StatusId>\n"
            + "</CmResponse>\n";
    doTest(TEST_XML5, expectedResult, false, null, null, null);
  }

  /** Test partial response (just permits). */
  public void testPartialResponse() {
    String queryXml =
      "<AuthorizationQuery>\n" +
      "<ConnectorQuery>\n" +
      "  <Identity source=\"gsa\">CN=foo</Identity>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector3.localhost" +
         ServletUtil.DOCID + "foo1</Resource>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector3.localhost" +
         ServletUtil.DOCID + "foo2</Resource>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector3.localhost" +
         ServletUtil.DOCID + "foo3</Resource>\n" +
      "</ConnectorQuery>\n" +
      "</AuthorizationQuery>";

    String expectedResult =
        "<CmResponse>\n" + "  <AuthorizationResponse>\n"
            + "    <Answer>\n"
            + "      <Resource connectorname=\"connector3\">"
            + ServletUtil.PROTOCOL
            + "connector3.localhost" + ServletUtil.DOCID + "foo1</Resource>\n"
            + "      <Decision>PERMIT</Decision>\n"
            + "    </Answer>\n"
            + "    <Answer>\n"
            + "      <Resource connectorname=\"connector3\">"
            + ServletUtil.PROTOCOL
            + "connector3.localhost" + ServletUtil.DOCID + "foo2</Resource>\n"
            + "      <Decision>DENY</Decision>\n"
            + "    </Answer>\n"
            + "    <Answer>\n"
            + "      <Resource connectorname=\"connector3\">"
            + ServletUtil.PROTOCOL
            + "connector3.localhost" + ServletUtil.DOCID + "foo3</Resource>\n"
            + "      <Decision>PERMIT</Decision>\n"
            + "    </Answer>\n"
            + "  </AuthorizationResponse>\n" + "  <StatusId>0</StatusId>\n"
            + "</CmResponse>\n";
    doTest(queryXml, expectedResult, false, null, null, null);
  }

  /** Test Indeterminate response. */
  public void testIndeterminateResponse() {
    String queryXml =
      "<AuthorizationQuery>\n" +
      "<ConnectorQuery>\n" +
      "  <Identity source=\"gsa\">CN=foo</Identity>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector2.localhost" +
         ServletUtil.DOCID + "foo1</Resource>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector2.localhost" +
         ServletUtil.DOCID + "foo2</Resource>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector2.localhost" +
         ServletUtil.DOCID + "foo3</Resource>\n" +
      "</ConnectorQuery>\n" +
      "</AuthorizationQuery>";

    String expectedResult =
        "<CmResponse>\n" + "  <AuthorizationResponse>\n"
            + "    <Answer>\n"
            + "      <Resource connectorname=\"connector2\">"
            + ServletUtil.PROTOCOL
            + "connector2.localhost" + ServletUtil.DOCID + "foo1</Resource>\n"
            + "      <Decision>PERMIT</Decision>\n"
            + "    </Answer>\n"
            + "    <Answer>\n"
            + "      <Resource connectorname=\"connector2\">"
            + ServletUtil.PROTOCOL
            + "connector2.localhost" + ServletUtil.DOCID + "foo2</Resource>\n"
            + "      <Decision>INDETERMINATE</Decision>\n"
            + "    </Answer>\n"
            + "    <Answer>\n"
            + "      <Resource connectorname=\"connector2\">"
            + ServletUtil.PROTOCOL
            + "connector2.localhost" + ServletUtil.DOCID + "foo3</Resource>\n"
            + "      <Decision>PERMIT</Decision>\n"
            + "    </Answer>\n"
            + "  </AuthorizationResponse>\n" + "  <StatusId>0</StatusId>\n"
            + "</CmResponse>\n";
    doTest(queryXml, expectedResult, false, null, null, null);
  }

  /** Test empty query list. */
  public void testEmptyQueryList() {
    String queryXml =
      "<AuthorizationQuery>\n" +
      "</AuthorizationQuery>";

    String expectedResult =
        "<CmResponse>\n" + "  <StatusId>0</StatusId>\n" + "</CmResponse>\n";
    doTest(queryXml, expectedResult, false, null, null, null);
  }

  /** Test null response. */
  public void testNullResponse() {
    String queryXml =
      "<AuthorizationQuery>\n" +
      "<ConnectorQuery>\n" +
      "  <Identity source=\"gsa\">CN=foo</Identity>\n" +
      "  <Resource>" + ServletUtil.PROTOCOL + "connector4.localhost" +
         ServletUtil.DOCID + "foo1</Resource>\n" +
      "</ConnectorQuery>\n" +
      "</AuthorizationQuery>";

    String expectedResult =
        "<CmResponse>\n" + "  <StatusId>0</StatusId>\n" + "</CmResponse>\n";
    doTest(queryXml, expectedResult, false, null, null, null);
  }

  /** Test XML escaped URL. */
  public void testXmlEscapedUrl() {
    String queryXml = "<AuthorizationQuery>\n"
        + "<ConnectorQuery>\n"
        + "  <Identity source=\"gsa\">CN=foo</Identity>\n"
        + "  <Resource connectorname=\"connector1\">"
        + "http://foo.bar?baz=0&amp;length&lt;120&amp;docid=O&apos;Leary"
        + "</Resource>\n"
        + "</ConnectorQuery>\n"
        + "</AuthorizationQuery>";

    String expectedResult = "<CmResponse>\n"
        + "  <AuthorizationResponse>\n"
        + "    <Answer>\n"
        + "      <Resource connectorname=\"connector1\">"
        + "http://foo.bar?baz=0&amp;length&lt;120&amp;docid=O&#39;Leary"
        + "</Resource>\n"
        + "      <Decision>PERMIT</Decision>\n"
        + "    </Answer>\n"
        + "  </AuthorizationResponse>\n"
        + "  <StatusId>0</StatusId>\n"
        + "</CmResponse>\n";

    doTest(queryXml, expectedResult, false, null, null, null);
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
  private static final String NON_AUTHN_EXPECTED_RESULT = "<CmResponse>\n"
      + "  <AuthorizationResponse>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector1\">" + ServletUtil.PROTOCOL
      + "connector1.localhost" + ServletUtil.DOCID + "foo1</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector2\">" + ServletUtil.PROTOCOL
      + "connector2.localhost" + ServletUtil.DOCID + "foo2</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector3\">" + ServletUtil.PROTOCOL
      + "connector3.localhost" + ServletUtil.DOCID + "foo3</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "  </AuthorizationResponse>\n"
      + "  <StatusId>0</StatusId>\n"
      + "</CmResponse>\n";
  private static final String USER1_EXPECTED_RESULT = "<CmResponse>\n"
      + "  <AuthorizationResponse>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector1\">" + ServletUtil.PROTOCOL
      + "connector1.localhost" + ServletUtil.DOCID + "foo1</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector2\">" + ServletUtil.PROTOCOL
      + "connector2.localhost" + ServletUtil.DOCID + "foo2</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector3\">" + ServletUtil.PROTOCOL
      + "connector3.localhost" + ServletUtil.DOCID + "foo3</Resource>\n"
      + "      <Decision>DENY</Decision>\n"
      + "    </Answer>\n"
      + "  </AuthorizationResponse>\n"
      + "  <StatusId>0</StatusId>\n"
      + "</CmResponse>\n";
  private static final String USER2_EXPECTED_RESULT = "<CmResponse>\n"
      + "  <AuthorizationResponse>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector1\">" + ServletUtil.PROTOCOL
      + "connector1.localhost" + ServletUtil.DOCID + "foo1</Resource>\n"
      + "      <Decision>DENY</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector2\">" + ServletUtil.PROTOCOL
      + "connector2.localhost" + ServletUtil.DOCID + "foo2</Resource>\n"
      + "      <Decision>DENY</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector3\">" + ServletUtil.PROTOCOL
      + "connector3.localhost" + ServletUtil.DOCID + "foo3</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "  </AuthorizationResponse>\n"
      + "  <StatusId>0</StatusId>\n"
      + "</CmResponse>\n";

  /**
   * The Identity is qualified by domain
   */
  public void testHandleDoPostDomainQualifiedId() {
    doTest(TEST_DOMAINSPECIFIC_IDENTITY, NON_AUTHN_EXPECTED_RESULT,
        false, null, null, null);
    doTest(TEST_DOMAINSPECIFIC_IDENTITY, USER1_EXPECTED_RESULT,
        true, "CN=foo", "", "dom1");
    doTest(TEST_DOMAINSPECIFIC_IDENTITY, USER2_EXPECTED_RESULT,
        true, "username", "", "dom2");
  }

  private static final String TEST_PASSWORD_IDENTITY = "<AuthorizationQuery>\n"
      + "<ConnectorQuery>\n"
      + "  <Identity password=\"pass1\" source=\"gsa\">user1</Identity>\n"
      + "  <Resource>" + ServletUtil.PROTOCOL + "connector1.localhost"
      + ServletUtil.DOCID + "foo1</Resource>\n"
      + "  <Resource>" + ServletUtil.PROTOCOL + "connector2.localhost"
      + ServletUtil.DOCID + "foo2</Resource>\n"
      + "</ConnectorQuery>\n"
      + "<ConnectorQuery>\n"
      + "  <Identity password=\"pass2\" source=\"connector\">user2</Identity>\n"
      + "  <Resource>" + ServletUtil.PROTOCOL + "connector3.localhost"
      + ServletUtil.DOCID + "foo3</Resource>\n"
      + "</ConnectorQuery>\n"
      + "</AuthorizationQuery>";

  /**
   * The Identity is qualified by domain
   */
  public void testHandleDoPostPasswordId() {
    doTest(TEST_PASSWORD_IDENTITY, NON_AUTHN_EXPECTED_RESULT,
        false, null, null, null);
    doTest(TEST_PASSWORD_IDENTITY, USER1_EXPECTED_RESULT,
        true, "user1", "pass1", "");
    doTest(TEST_PASSWORD_IDENTITY, USER2_EXPECTED_RESULT,
        true, "user2", "pass2", "");
  }

  private static final String TEST_DOCUMENT_URL = "<AuthorizationQuery>\n"
      + "  <ConnectorQuery>\n"
      + "    <Identity source=\"connector\">johndoe</Identity>\n"
      + "    <Resource connectorname=\"connector1\">"
      + "https://www.sphost/sites/mylist/test1.doc</Resource>\n"
      + "    <Resource connectorname=\"connector2\">"
      + "https://www.sphost/sites/mylist/test2.doc</Resource>\n"
      + "  </ConnectorQuery>\n"
      + "  <ConnectorQuery>\n"
      + "    <Identity source=\"connector\">janedoe</Identity>\n"
      + "    <Resource connectorname=\"connector3\">"
      + "https://www.sphost/sites/mylist/test3.doc</Resource>\n"
      + "  </ConnectorQuery>\n"
      + "</AuthorizationQuery>";

  private static final String TEST_BAD_DOCUMENT_URL = "<AuthorizationQuery>\n"
      + "  <ConnectorQuery>\n"
      + "    <Identity source=\"connector\">johndoe</Identity>\n"
      + "    <Resource connectorname=\"connector1\">"
      + "https://www.sphost/sites/mylist/test1.doc</Resource>\n"
      + "    <Resource>"
      + "https://www.sphost.missing/sites/mylist/test2.doc</Resource>\n"
      + "  </ConnectorQuery>\n"
      + "  <ConnectorQuery>\n"
      + "    <Identity source=\"connector\">janedoe</Identity>\n"
      + "    <Resource connectorname=\"connector3\">"
      + "https://www.sphost/sites/mylist/test3.doc</Resource>\n"
      + "  </ConnectorQuery>\n"
      + "</AuthorizationQuery>";

  private static final String JOHNDOE_EXPECTED_RESULT = "<CmResponse>\n"
      + "  <AuthorizationResponse>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector1\">"
      + "https://www.sphost/sites/mylist/test1.doc</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector2\">"
      + "https://www.sphost/sites/mylist/test2.doc</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector3\">"
      + "https://www.sphost/sites/mylist/test3.doc</Resource>\n"
      + "      <Decision>DENY</Decision>\n"
      + "    </Answer>\n"
      + "  </AuthorizationResponse>\n"
      + "  <StatusId>0</StatusId>\n"
      + "</CmResponse>\n";

  private static final String JANEDOE_EXPECTED_RESULT = "<CmResponse>\n"
      + "  <AuthorizationResponse>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector1\">"
      + "https://www.sphost/sites/mylist/test1.doc</Resource>\n"
      + "      <Decision>DENY</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector2\">"
      + "https://www.sphost/sites/mylist/test2.doc</Resource>\n"
      + "      <Decision>DENY</Decision>\n"
      + "    </Answer>\n"
      + "    <Answer>\n"
      + "      <Resource connectorname=\"connector3\">"
      + "https://www.sphost/sites/mylist/test3.doc</Resource>\n"
      + "      <Decision>PERMIT</Decision>\n"
      + "    </Answer>\n"
      + "  </AuthorizationResponse>\n"
      + "  <StatusId>0</StatusId>\n"
      + "</CmResponse>\n";

  /**
   * Test the searchurl is passed in as the document authorization key.
   */
  public void testSearchUrlAuthz() {
    // The parser will be able to parse the first resource and it will be added
    // to the mapping and processed.  Once it hits the bad URL, however, it
    // will bail and set the overall status of the response to indicate the
    // Connector Name was missing.
    String expectedBadDocumentResponse = "<CmResponse>\n"
        + "  <AuthorizationResponse>\n"
        + "    <Answer>\n"
        + "      <Resource connectorname=\"connector1\">"
        + "https://www.sphost/sites/mylist/test1.doc</Resource>\n"
        + "      <Decision>PERMIT</Decision>\n"
        + "    </Answer>\n"
        + "    <Answer>\n"
        + "      <Resource connectorname=\"connector3\">"
        + "https://www.sphost/sites/mylist/test3.doc</Resource>\n"
        + "      <Decision>PERMIT</Decision>\n"
        + "    </Answer>\n"
        + "  </AuthorizationResponse>\n"
        + "  <StatusId>" + ConnectorMessageCode.RESPONSE_NULL_CONNECTOR
        + "</StatusId>\n"
        + "</CmResponse>\n";
    doTest(TEST_DOCUMENT_URL, JOHNDOE_EXPECTED_RESULT, true, "johndoe", "", "");
    doTest(TEST_DOCUMENT_URL, JANEDOE_EXPECTED_RESULT, true, "janedoe", "", "");
    doTest(TEST_BAD_DOCUMENT_URL, expectedBadDocumentResponse,
        false, null, null, null);
  }

  private void doTest(String xmlBody, String expectedResult,
      boolean verifyIdentity, String username, String password, String domain) {
    LOGGER.info("Test: " + getName());
    LOGGER.info("xmlBody:\n " + xmlBody);
    MockManager manager = MockManager.getInstance();
    manager.setShouldVerifyIdentity(verifyIdentity);
    if (verifyIdentity) {
      manager.setExpectedIdentity(domain, username, password, null);
    }
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    AuthorizationHandler authorizationHandler =
        AuthorizationHandler.makeAuthorizationHandlerForTest(xmlBody, manager,
            out);
    authorizationHandler.handleDoPost();
    out.flush();
    String result = writer.toString();
    out.close();
    LOGGER.info("Expected Response:\n" + expectedResult);
    LOGGER.info("Actual Response:\n" + result);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 StringUtils.normalizeNewlines(result));
  }
}
