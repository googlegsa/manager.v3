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

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.MockManager;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.SpiConstants;

import junit.framework.TestCase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Tests the AuthenticateTest servlet class
 *
 */
public class AuthenticateTest extends TestCase {
  private static final Logger LOGGER =
    Logger.getLogger(AuthenticateTest.class.getName());

  public void testAuthenticate() {
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
    doTest(xmlBody, expectedResult, null, "fooUser", "fooPassword", null);
  }

  public void testEmptyBody() {
    String xmlBody = "";
    String expectedResult = "<CmResponse>\n  <StatusId>"
        + ConnectorMessageCode.ERROR_PARSING_XML_REQUEST
        + "</StatusId>\n</CmResponse>\n";
    doTest(xmlBody, expectedResult, null, "fooUser", "fooPassword", null);
  }

  public void testNoCredentials() {
    String xmlBody = "<AuthnRequest>\n</AuthnRequest>";
    String expectedResult = "<CmResponse>\n  <StatusId>"
        + ConnectorMessageCode.RESPONSE_EMPTY_NODE
        + "</StatusId>\n</CmResponse>\n";
    doTest(xmlBody, expectedResult, null, "fooUser", "fooPassword", null);
  }

  public void testBadUsername() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>badUser</Username>\n" +
      "    <Password>fooPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    doTestFailedAuthentication(xmlBody);
  }

  public void testBadPassword() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Password>badPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    doTestFailedAuthentication(xmlBody);
  }

  public void doTestFailedAuthentication(String xmlBody) {
    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Failure ConnectorName=\"connector1\"/>\n" +
      "    <Failure ConnectorName=\"connector2\"/>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";
    doTest(xmlBody, expectedResult, null, "fooUser", "fooPassword", null);
  }

  public void testWithDomain() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Domain>fooDomain</Domain>\n" +
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
    doTest(xmlBody, expectedResult, "fooDomain", "fooUser", "fooPassword", null);
  }

  public void testPassAndFail() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Domain>connector1</Domain>\n" +
      "    <Password>fooPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "    <Failure ConnectorName=\"connector2\"/>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";
    doTest(xmlBody, expectedResult, "connector1", "fooUser", "fooPassword", null);
  }

  public void testFailAndPass() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Domain>connector2</Domain>\n" +
      "    <Password>fooPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Failure ConnectorName=\"connector1\"/>\n" +
      "    <Success ConnectorName=\"connector2\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "    </Success>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";
    doTest(xmlBody, expectedResult, "connector2", "fooUser", "fooPassword", null);
  }

  public void testWithTwoConnectorNameElements() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Connectors>\n" +
      "    <ConnectorName>connector1</ConnectorName>\n" +
      "    <ConnectorName>connector2</ConnectorName>\n" +
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
    doTest(xmlBody, expectedResult, "fooDomain", "fooUser", "fooPassword", null);
  }

  public void testWithOneConnectorNameElement() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Connectors>\n" +
      "    <ConnectorName>connector1</ConnectorName>\n" +
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
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";
    doTest(xmlBody, expectedResult, "fooDomain", "fooUser", "fooPassword", null);
  }

  public void testGetGroupsNoPassword() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    doTestGroups(xmlBody);
  }


  public void testGetGroupsEmptyPassword() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Password></Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    // An empty password should fail Authentication.
    doTestFailedAuthentication(xmlBody);
  }

  public void testAuthenticateWithGroups() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Password>fooPassword</Password>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    doTestGroups(xmlBody);
  }

  public void testPassAndFailGroups() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Domain>connector1</Domain>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "      <Group>staff</Group>\n" +
      "      <Group>wheel</Group>\n" +
      "    </Success>\n" +
      "    <Failure ConnectorName=\"connector2\"/>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";

    doTest(xmlBody, expectedResult, "connector1", "fooUser", "fooPassword",
           makeGroups("staff", "wheel"));
  }

  public void testGroupsWithNamespaces() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Connectors>\n" +
      "    <ConnectorName>connector1</ConnectorName>\n" +
      "  </Connectors>\n" +
      "  <Credentials>\n" +
      "    <Username>fooUser</Username>\n" +
      "    <Domain>connector1</Domain>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      /* TODO: Re-enable once the GSA supports principal-type attribute.
      "      <Group principaltype=\"dn\" namespace=\"global\">staff</Group>\n" +
      "      <Group principaltype=\"dn\" namespace=\"global\">wheel</Group>\n" +
      * else */
      "      <Group namespace=\"global\">staff</Group>\n" +
      "      <Group namespace=\"global\">wheel</Group>\n" +
      /* end TODO */
      "      <Group principal-type=\"unqualified\" namespace=\"local\">wheel" +
        "</Group>\n" +
      "      <Group principal-type=\"unqualified\" namespace=\"local\">slo" +
        "</Group>\n" +
      "    </Success>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";

    Collection<Principal> groups = ImmutableList.of(
        new Principal(SpiConstants.PrincipalType.UNKNOWN, "global", "staff"),
        new Principal(SpiConstants.PrincipalType.UNKNOWN, "global", "wheel"),
        new Principal(SpiConstants.PrincipalType.UNQUALIFIED, "local", "wheel"),
        new Principal(SpiConstants.PrincipalType.UNQUALIFIED, "local", "slo"));
    doTest(xmlBody, expectedResult, "connector1", "fooUser", "fooPassword",
           groups);
  }

  private void doTestGroups(String xmlBody) {
    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "      <Group>staff</Group>\n" +
      "      <Group>wheel</Group>\n" +
      "    </Success>\n" +
      "    <Success ConnectorName=\"connector2\">\n" +
      "      <Identity>fooUser</Identity>\n" +
      "      <Group>staff</Group>\n" +
      "      <Group>wheel</Group>\n" +
      "    </Success>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";

    doTest(xmlBody, expectedResult, null, "fooUser", "fooPassword",
           makeGroups("staff", "wheel"));
  }

  /** Tests unsafe XML characters in user and group names. */
  public void testEvilUserAndGroupNames() {
    String xmlBody =
      "<AuthnRequest>\n" +
      "  <Credentials>\n" +
      "    <Username>foo&lt;bar&gt;baz&amp;</Username>\n" +
      "    <Domain>connector1</Domain>\n" +
      "  </Credentials>\n" +
      "</AuthnRequest>";

    String expectedResult =
      "<CmResponse>\n" +
      "  <AuthnResponse>\n" +
      "    <Success ConnectorName=\"connector1\">\n" +
      "      <Identity>foo&lt;bar>baz&amp;</Identity>\n" +
      "      <Group>st&amp;ff</Group>\n" +
      "      <Group>we&#39;ll</Group>\n" +
      "    </Success>\n" +
      "    <Failure ConnectorName=\"connector2\"/>\n" +
      "  </AuthnResponse>\n" +
      "</CmResponse>\n";

    doTest(xmlBody, expectedResult, "connector1", "foo<bar>baz&", "fooPassword",
           makeGroups("st&ff", "we'll"));
  }

  private Collection<String> makeGroups(String... groups) {
    return ImmutableList.copyOf(groups);
  }

  private void doTest(String xmlBody, String expectedResult, String domain,
      String username, String password, Collection<?> groups) {
    LOGGER.info("============== " + getName() + " ====================");
    LOGGER.info("xmlBody:\n" + xmlBody);
    MockManager manager = MockManager.getInstance();
    manager.setShouldVerifyIdentity(true);
    manager.setExpectedIdentity(domain, username, password, groups);
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    Authenticate.handleDoPost(xmlBody, manager, out);
    out.flush();
    String result = writer.toString();
    out.close();
    LOGGER.info("expected result:\n" + expectedResult);
    LOGGER.info("actual result:\n" + result);
    assertEquals(StringUtils.normalizeNewlines(expectedResult),
                 StringUtils.normalizeNewlines(result));
  }
}
