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

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;
import com.google.enterprise.connector.util.XmlParseUtil;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parses the xml body of an Authorization request.
 * <p>
 * An authorization is a sequence of {@code ConnectorQuery} elements.  Each
 * {@code ConnectorQuery} has a single {@code Identity} element and a sequence
 * of {@code Resource} elements.  For example:
 * <pre>
 * &lt;AuthorizationQuery>
 *   &lt;ConnectorQuery>
 *     &lt;Identity domain="domain" password="user1" source="connector">user1</Identity>
 *     &lt;Resource>googleconnector://connector1.localhost/doc?docid=doc1a</Resource>
 *     &lt;Resource>googleconnector://connector2.localhost/doc?docid=doc2a</Resource>
 *     ...
 *   &lt;/ConnectorQuery>
 *   ...
 * </pre>
 * Note that both the {@code domain} and {@code password} attributes of the
 * {@code Identity} element are optional.
 */
public class AuthorizationParser {

  private static final Logger LOGGER =
      Logger.getLogger(AuthorizationParser.class.getName());

  private final String xmlBody;
  private ConnectorMessageCode status;
  private int numDocs;
  private final Map<AuthenticationIdentity, ConnectorQueries> parseMap;

  public AuthorizationParser(String xmlBody) {
    this.xmlBody = xmlBody;
    parseMap = new HashMap<AuthenticationIdentity, ConnectorQueries>();
    status = new ConnectorMessageCode();
    numDocs = 0;
    parse();
  }

  /**
   * Parse the Authorization Request XML into a hierarchy: AuthorizationParser,
   * ConnectorQueries, QueryResources.
   * <p>
   * The top-level, this object (AuthorizationParser) is a map from
   * AuthorizationIdentity objects to ConnectorQueries objects. This partitions
   * the request by Identity.
   * <p>
   * The second-level object, ConnectorQueries, is a map from connector name to
   * QueryResources objects. For each connector, it gives all the object for which
   * the request wants a decision.
   * <p>
   * The third-level object, QueryResources, is a map from docid strings to the
   * corresponding parsed representation as a ParsedUrl.
   * <p>
   * In practice, for now, it is unlikely that the same connector will show up
   * under more than one identity. In fact, the most likely case is that the top
   * two levels (AuthorizationParser and ConnectorQueries) each have only one
   * item.
   */
  private void parse() {
    Element root = XmlParseUtil.parseAndGetRootElement(xmlBody,
        ServletUtil.XMLTAG_AUTHZ_QUERY);

    if (root == null) {
      setStatus(ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return;
    }

    NodeList queryList =
        root.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_QUERY);

    if (queryList.getLength() == 0) {
      LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_EMPTY_NODE);
      return;
    }

    numDocs = 0;

    for (int i = 0; i < queryList.getLength(); ++i) {
      Element queryItem = (Element) queryList.item(i);
      AuthenticationIdentity identity = parseIdentityGroup(queryItem);
      // Only consider Resources for which there is an associated identity.
      // A null Identity is considered an error on the part of the GSA.
      // Skip all its resources and continue with the next ConnectorQuery item.
      // Subsequently, this ConnectorQuery will not run and none of its
      // QueryResources will be returned. The GSA will then consider them
      // INDETERMINATE.
      if (identity != null) {
        parseResourceGroup(identity, queryItem);
      }
    }

    if (numDocs == 0) {
      LOGGER.warning("No docid available.");
      return;
    }
  }

  /**
   * Utility function to establish the first level mapping from the Identity
   * to the ConnectorQueries.
   */
  private AuthenticationIdentity parseIdentityGroup(Element queryItem) {
    String username = XmlParseUtil.getFirstElementByTagName(queryItem,
        ServletUtil.XMLTAG_IDENTITY);
    String domain =
        XmlParseUtil.getFirstAttribute(queryItem, ServletUtil.XMLTAG_IDENTITY,
        ServletUtil.XMLTAG_DOMAIN_ATTRIBUTE);
    String password =
        XmlParseUtil.getFirstAttribute(queryItem, ServletUtil.XMLTAG_IDENTITY,
        ServletUtil.XMLTAG_PASSWORD_ATTRIBUTE);

    if (username == null) {
      LOGGER.warning("Null Identity");
      // TODO: Is this the only way this can happen?
      LOGGER.warning("Flexible Authorization may be misconfigured to use "
          + "connector authorization with a credential group which has no "
          + "authentication rules defined.");
      setStatus(ConnectorMessageCode.RESPONSE_NULL_IDENTITY, "Null Identity");
      return null;
    }

    AuthenticationIdentity identity = findIdentity(username, password, domain);
    ConnectorQueries urlsByConnector = getConnectorQueriesForIdentity(identity);
    if (urlsByConnector == null) {
      urlsByConnector = new ConnectorQueries();
      putConnectorQueriesForIdentity(identity, urlsByConnector);
    }
    return identity;
  }

  /**
   * Utility function to establish the second level mapping from the connector
   * name to the QueryResources and the third level mapping from the docid to the
   * AuthorizationResource.
   * <p>
   * Unfortunately the response to this complex query only has one status so
   * it's all or nothing.  If there is one improperly formatted element in the
   * query that is enough to bail on the request and set an appropriate status.
   * Therefore, the resources are validated at this point to determine if they
   * can be routed to a connector for authorization.
   */
  private void parseResourceGroup(AuthenticationIdentity identity,
      Element queryItem) {
    NodeList resourceList =
        queryItem.getElementsByTagName(ServletUtil.XMLTAG_RESOURCE);
    if (resourceList.getLength() == 0) {
      LOGGER.warning("Null Resources");
      setStatus(ConnectorMessageCode.RESPONSE_NULL_RESOURCE);
      return;
    }

    // Get the ConnectorQueries for the given Identity.
    ConnectorQueries urlsByConnector =
        getConnectorQueriesForIdentity(identity);
    for (int i = 0; i < resourceList.getLength(); ++i) {
      Element resourceItem = (Element) resourceList.item(i);
      AuthorizationResource resource = new AuthorizationResource(resourceItem);
      if (resource.getStatus() != ConnectorMessageCode.SUCCESS) {
        setStatus(resource.getStatus());
        // Skip this failed resource and continue with the next one.
        // Since it was not added to the resources for this connector, it will
        // not get auto-DENY in AuthorizationHandler.accumlateQueryResults.
        // The GSA will then consider it INDETERMINATE.
      } else {
        // Create a mapping for this resource.
        QueryResources urlsByDocid =
            urlsByConnector.getQueryResources(resource.getConnectorName());
        if (urlsByDocid == null) {
          urlsByDocid = new QueryResources();
          urlsByConnector.putQueryResources(resource.getConnectorName(),
              urlsByDocid);
        }
        urlsByDocid.putResource(resource.getDocId(), resource);
        numDocs++;
      }
    }
  }

  // Package-level visibility for testing.
  static boolean matchesIdentity(AuthenticationIdentity id, String username,
      String password, String domain) {
    if (!id.getUsername().equals(username)) {
      return false;
    }
    if (!matchNullString(password, id.getPassword())) {
      return false;
    }
    if (!matchNullString(domain, id.getDomain())) {
      return false;
    }
    return true;
  }

  /**
   * Utility method to compare two strings with the special logic of treating
   * null the same as the empty string.
   *
   * @param string1
   * @param string2
   * @return true if the given strings are considered the same.
   */
  private static boolean matchNullString(String string1, String string2) {
    String value1 = (string1 == null) ? "" : string1;
    String value2 = (string2 == null) ? "" : string2;
    return value1.equals(value2);
  }

  private AuthenticationIdentity findIdentity(String username, String password,
      String domain) {
    for (AuthenticationIdentity identity : parseMap.keySet()) {
      if (matchesIdentity(identity, username, password, domain)) {
        return identity;
      }
    }
    return new SimpleAuthenticationIdentity(username, password, domain);
  }

  public int getNumDocs() {
    return numDocs;
  }

  public void setStatus(int messageId) {
    setStatus(messageId, null);
  }

  public void setStatus(int messageId, String message) {
    // Only override a SUCCESS status, not any previous error.
    if (status.isSuccess()) {
      status = new ConnectorMessageCode(messageId, message,
                                        ConnectorMessageCode.EMPTY_PARAMS);
    }
  }

  public ConnectorMessageCode getStatus() {
    return status;
  }

  private void putConnectorQueriesForIdentity(AuthenticationIdentity identity,
      ConnectorQueries urlsByConnector) {
    parseMap.put(identity, urlsByConnector);
  }

  public Collection<AuthenticationIdentity> getIdentities() {
    return parseMap.keySet();
  }

  public ConnectorQueries getConnectorQueriesForIdentity(AuthenticationIdentity
      identity) {
    return parseMap.get(identity);
  }

  // Note: this is for testing only -- thus package private
  int countParsedIdentities() {
    return parseMap.size();
  }

  /**
   * {@code ConnectorQueries} is a map from connector name to QueryResources objects.
   * For each connector, it gives all the object for which the request wants a
   * decision.
   */
  public static class ConnectorQueries {
    private final Map<String, QueryResources> queryMap;

    /*
     * Private constructor so this class can only be constructed by
     * AuthorizationParser.
     */
    private ConnectorQueries() {
      queryMap = new HashMap<String, QueryResources>();
    }

    public int size() {
      return queryMap.size();
    }

    public Collection<String> getConnectors() {
      return queryMap.keySet();
    }

    public QueryResources getQueryResources(String connector) {
      return queryMap.get(connector);
    }

    private QueryResources putQueryResources(String connector,
        QueryResources queryResources) {
      return queryMap.put(connector, queryResources);
    }
  }

  /**
   * {@code QueryResources} is a map from docid strings to the corresponding
   * {@link AuthorizationResource}.
   */
  public static class QueryResources {
    Map<String, AuthorizationResource> resourceMap;

    /*
     * Private constructor so this class can only be constructed by
     * AuthorizationParser.
     */
    private QueryResources() {
      resourceMap = new HashMap<String, AuthorizationResource>();
    }

    private void putResource(String docid, AuthorizationResource p) {
      resourceMap.put(docid, p);
    }

    public Collection<String> getDocids() {
      return resourceMap.keySet();
    }

    public int size() {
      return resourceMap.size();
    }

    public AuthorizationResource getResource(String docid) {
      return resourceMap.get(docid);
    }
  }
}
