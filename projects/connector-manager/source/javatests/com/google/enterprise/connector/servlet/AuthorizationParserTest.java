// Copyright 2009 Google Inc. All Rights Reserved.
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

import com.google.enterprise.connector.servlet.AuthorizationParser.ConnectorQueries;
import com.google.enterprise.connector.servlet.AuthorizationParser.QueryUrls;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import junit.framework.TestCase;

/**
 * Tests the AuthorizationParser class
 */
public class AuthorizationParserTest extends TestCase {
  private static final String TWO_IDENTITIES_TWO_CONNECTORS =
      "<AuthorizationQuery>\n" + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1a</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2a</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1b</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2b</Resource>\n"
          + "</ConnectorQuery>\n" + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username2</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1c</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/doc?" 
          + "docid=doc2c</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/doc?" 
          + "docid=doc1d</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/doc?" 
          + "docid=doc2d</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "</AuthorizationQuery>\n";
  private static final String ONE_IDENTITY_TWO_QUERIES =
      "<AuthorizationQuery>\n" + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1a</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2a</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1b</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2b</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1c</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2c</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1d</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2d</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "</AuthorizationQuery>\n";
  private static final String TWO_IDENTITIES_MULTIPLE_QUERIES =
      "<AuthorizationQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username1</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1a</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username2</Identity>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2a</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username1</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1b</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity source=\"connector\">username2</Identity>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2b</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "</AuthorizationQuery>\n";

  private static final String MALFORMED_XML =
      "<AuthorizationQuery>\n" 
          + "<ConnectorQuery>\n" 
          + "</AuthorizationQuery>\n" 
          + "";

  private boolean compareDomains(String domain1, String domain2) {
    if (domain1 == null) {
      return (domain2 == null);
    }
    return domain1.equals(domain2);
  }

  private ConnectorQueries getConnectorQueriesByIdentity(AuthorizationParser p,
      AuthenticationIdentity identity) {
    ConnectorQueries result = null;
    for (AuthenticationIdentity i : p.getIdentities()) {
      String username = identity.getUsername();
      String domain = identity.getDomain();
      if (username.equals(i.getUsername()) && 
          compareDomains(domain, i.getDomain())) {
        assertNull("Should be only one identity with username \"" 
            + username + "\" and domain \"" + domain + "\"", result);
        result = p.getConnectorQueriesForIdentity(i);
      }
    }
    return result;
  }

  private int countConnectorsForUsername(AuthorizationParser p, 
      String username) {
    AuthenticationIdentity i = new SimpleAuthenticationIdentity(username);
    return countConnectorsForIdentity(p, i);
  }

  private int countConnectorsForIdentity(AuthorizationParser p, 
      AuthenticationIdentity i) {
    return getConnectorQueriesByIdentity(p, i).size();
  }

  private int countUrlsForUsernameConnectorPair(AuthorizationParser p, 
      String username, String connectorName) {
    AuthenticationIdentity i = new SimpleAuthenticationIdentity(username);
    return countUrlsForIdentityConnectorPair(p, i, connectorName);
  }

  private int countUrlsForIdentityConnectorPair(AuthorizationParser p, 
      AuthenticationIdentity i, String connectorName) {
    ConnectorQueries cq = getConnectorQueriesByIdentity(p, i);
    assertNotNull(cq);
    QueryUrls queryUrls = cq.getQueryUrls(connectorName);
    if (queryUrls == null) {
      return 0;
    }
    return queryUrls.size();
  }

  public void testSimple() {
    AuthorizationParser ap = 
        new AuthorizationParser(AuthorizationTest.TEST_XML1);
    assertEquals(2, ap.countParsedIdentities());
    assertEquals(1, countConnectorsForUsername(ap, "username"));
    assertEquals(2, countConnectorsForUsername(ap, "CN=foo"));
    assertEquals(1, countUrlsForUsernameConnectorPair(ap, "username", 
        "connector3"));
  }

  public void testTwoIdentitiesTwoConnectors() {
    AuthorizationParser ap = 
        new AuthorizationParser(TWO_IDENTITIES_TWO_CONNECTORS);
    assertEquals(2, ap.countParsedIdentities());
    assertEquals(2, countConnectorsForUsername(ap, "username"));
    assertEquals(2, countConnectorsForUsername(ap, "username2"));
    assertEquals(2, countUrlsForUsernameConnectorPair(ap, "username", 
        "connector2"));
  }

  public void testOneIdentityTwoQueries() {
    AuthorizationParser ap = 
        new AuthorizationParser(ONE_IDENTITY_TWO_QUERIES);
    assertEquals(1, ap.countParsedIdentities());
    assertEquals(2, countConnectorsForUsername(ap, "username"));
    assertEquals(4, countUrlsForUsernameConnectorPair(ap, "username", 
        "connector2"));
  }

  public void testTwoIdentitiesMultipleQueries() {
    AuthorizationParser ap = 
        new AuthorizationParser(TWO_IDENTITIES_MULTIPLE_QUERIES);
    assertEquals(2, ap.countParsedIdentities());
    assertEquals(1, countConnectorsForUsername(ap, "username1"));
    assertEquals(0, countUrlsForUsernameConnectorPair(ap, "username1", 
        "connector2"));
  }

  public void testMalformedXml() {
    AuthorizationParser ap = new AuthorizationParser(MALFORMED_XML);
    assertEquals(ConnectorMessageCode.ERROR_PARSING_XML_REQUEST, 
        ap.getStatus());
  }

  private static final String ONE_DOMAINSPECIFIC_IDENTITY =
      "<AuthorizationQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity domain=\"foodomain\" source=\"connector\">" 
          + "username</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1a</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2a</Resource>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1b</Resource>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2b</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "</AuthorizationQuery>\n";

  public void testOneDomainSpecificIdentity() {
    AuthorizationParser ap = 
        new AuthorizationParser(ONE_DOMAINSPECIFIC_IDENTITY);
    assertEquals(1, ap.countParsedIdentities());
    SimpleAuthenticationIdentity id =
        new SimpleAuthenticationIdentity("username", null, "foodomain");
    assertEquals(2, countConnectorsForIdentity(ap, id));
    assertEquals(2, countUrlsForIdentityConnectorPair(ap, id, "connector1"));
    assertEquals(2, countUrlsForIdentityConnectorPair(ap, id, "connector2"));
  }

  private static final String TWO_DOMAINSPECIFIC_IDENTITIES_MULTIPLE_QUERIES =
      "<AuthorizationQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity domain=\"arglebargle\" source=\"connector\">" 
          +	"xyzzy</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1a</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity domain=\"bazfaz\" source=\"connector\">" 
          +	"xyzzy</Identity>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2a</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity domain=\"arglebargle\" source=\"connector\">" 
          + "xyzzy</Identity>\n"
          + "  <Resource>googleconnector://connector1.localhost/" 
          + "doc?docid=doc1b</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "<ConnectorQuery>\n"
          + "  <Identity domain=\"bazfaz\" source=\"connector\">" 
          +	"xyzzy</Identity>\n"
          + "  <Resource>googleconnector://connector2.localhost/" 
          + "doc?docid=doc2b</Resource>\n"
          + "</ConnectorQuery>\n" 
          + "</AuthorizationQuery>\n";

  public void testTwoDomainSpecificIdentitiesMultipleQueries() {
    AuthorizationParser ap =
        new AuthorizationParser(TWO_DOMAINSPECIFIC_IDENTITIES_MULTIPLE_QUERIES);
    assertEquals(2, ap.countParsedIdentities());
    SimpleAuthenticationIdentity i1 =
        new SimpleAuthenticationIdentity("xyzzy", null, "arglebargle");
    assertEquals(1, countConnectorsForIdentity(ap, i1));
    assertEquals(2, countUrlsForIdentityConnectorPair(ap, i1, "connector1"));
    assertEquals(0, countUrlsForIdentityConnectorPair(ap, i1, "connector2"));
    SimpleAuthenticationIdentity i2 = 
        new SimpleAuthenticationIdentity("xyzzy", null, "bazfaz");
    assertEquals(1, countConnectorsForIdentity(ap, i2));
    assertEquals(0, countUrlsForIdentityConnectorPair(ap, i2, "connector1"));
    assertEquals(2, countUrlsForIdentityConnectorPair(ap, i2, "connector2"));
  }
}
