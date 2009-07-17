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

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parses the xml body of an Authorization request.
 * </p>
 * An authorization is a sequence of {@code ConnectorQuery} elements.  Each
 * {@code ConnectorQuery} has a single {@code Identity} element and a sequence
 * of {@code Resource} elements.  For example:
 * <pre>
 * &lt;AuthorizationQuery>
 *   &lt;ConnectorQuery>
 *     &lt;Identity domain="domain" source="connector">user1</Identity>
 *     &lt;Resource>googleconnector://connector1.localhost/doc?docid=doc1a</Resource>
 *     &lt;Resource>googleconnector://connector2.localhost/doc?docid=doc2a</Resource>
 *     ...
 *   &lt;/ConnectorQuery>
 *   ...
 * </pre>
 * Note that the {@code domain} attribute of the {@code Identity} element is
 * optional.
 */
public class AuthorizationParser {

  private static final Logger LOGGER =
      Logger.getLogger(AuthorizationParser.class.getName());

  private final String xmlBody;
  private int status;
  private int numDocs;
  private final Map<AuthenticationIdentity, ConnectorQueries> parseMap;

  public AuthorizationParser(String xmlBody) {
    this.xmlBody = xmlBody;
    parseMap = new HashMap<AuthenticationIdentity, ConnectorQueries>();
    status = ConnectorMessageCode.SUCCESS;
    numDocs = 0;
    parse();
  }

  /**
   * Parse the Authorization Request XML into a hierarchy: AuthorizationParser,
   * ConnectorQueries, QueryUrls.
   * <p>
   * The top-level, this object (AuthorizationParser) is a map from
   * AuthorizationIdentity objects to ConnectorQueries objects. This partitions
   * the request by Identity.
   * <p>
   * The second-level object, ConnectorQueries, is a map from connector name to
   * QueryUrls objects. For each connector, it gives all the object for which
   * the request wants a decision.
   * <p>
   * The third-level object, QueryUrls, is a map from docid strings to the
   * corresponding parsed representation as a ParsedUrl.
   * <p>
   * In practice, for now, it is unlikely that the same connector will show up
   * under more than one identity. In fact, the most likely case is that the top
   * two levels (AuthorizationParser and ConnectorQueries) each have only one
   * item.
   */
  private void parse() {
    Element root = ServletUtil.parseAndGetRootElement(xmlBody,
        ServletUtil.XMLTAG_AUTHZ_QUERY);

    if (root == null) {
      status = ConnectorMessageCode.ERROR_PARSING_XML_REQUEST;
      return;
    }

    NodeList queryList =
        root.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_QUERY);

    if (queryList.getLength() == 0) {
      LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_EMPTY_NODE);
      return;
    }

    status = ConnectorMessageCode.SUCCESS;
    numDocs = 0;

    for (int i = 0; i < queryList.getLength(); ++i) {
      Element queryItem = (Element) queryList.item(i);
      parseIdentityGroup(queryItem);
      if (status != ConnectorMessageCode.SUCCESS) {
        return;
      }
    }

    if (numDocs == 0) {
      LOGGER.warning("No docid available.");
      return;
    }
  }

  private void parseIdentityGroup(Element queryItem) {
    String username = ServletUtil.getFirstElementByTagName(queryItem,
        ServletUtil.XMLTAG_IDENTITY);
    String domain =
        ServletUtil.getFirstAttribute(queryItem, ServletUtil.XMLTAG_IDENTITY,
            ServletUtil.XMLTAG_DOMAIN_ATTRIBUTE);
    List<String> resources = ServletUtil.getAllElementsByTagName(queryItem,
        ServletUtil.XMLTAG_RESOURCE);
    if (resources.isEmpty()) {
      status = ConnectorMessageCode.RESPONSE_NULL_RESOURCE;
      return;
    }

    AuthenticationIdentity identity = findIdentity(username, domain);
    ConnectorQueries urlsByConnector = getConnectorQueriesForIdentity(identity);
    if (urlsByConnector == null) {
      urlsByConnector = new ConnectorQueries();
      putConnectorQueriesForIdentity(identity, urlsByConnector);
    }

    for (String url : resources) {
      parseResource(urlsByConnector, url);
    }
  }

  // Package-level visibility for testing.
  static boolean matchesIdentity(AuthenticationIdentity id, String username,
      String domain) {
    if (username == null && id.getUsername() != null) {
      return false;
    }
    if (!id.getUsername().equals(username)) {
      return false;
    }
    // A null domain is considered a match for an empty string domain.
    String domain1 = (domain == null) ? "" : domain;
    String domain2 = (id.getDomain() == null) ? "" : id.getDomain();
    return (domain1.equals(domain2));
  }

  private AuthenticationIdentity findIdentity(String username, String domain) {
    for (AuthenticationIdentity identity : parseMap.keySet()) {
      if (matchesIdentity(identity, username, domain)) {
        return identity;
      }
    }
    return new SimpleAuthenticationIdentity(username, "", domain);
  }

  private void parseResource(ConnectorQueries urlsByConnector, String url) {
    ParsedUrl p = new ParsedUrl(url);
    if (p.getStatus() == ConnectorMessageCode.SUCCESS) {
      QueryUrls urlsByDocid = urlsByConnector.getQueryUrls(p.getConnectorName());
      if (urlsByDocid == null) {
        urlsByDocid = new QueryUrls();
        urlsByConnector.putQueryUrls(p.getConnectorName(), urlsByDocid);
      }
      urlsByDocid.putUrl(p.getDocid(), p);
      numDocs++;
    } else {
      status = p.getStatus();
    }
  }

  public int getNumDocs() {
    return numDocs;
  }

  public int getStatus() {
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
   * {@code ConnectorQueries} is a map from connector name to QueryUrls objects.
   * For each connector, it gives all the object for which the request wants a
   * decision.
   */
  public static class ConnectorQueries {
    private final Map<String, QueryUrls> queryMap;

    /*
     * Private constructor so this class can only be constructed by
     * AuthorizationParser.
     */
    private ConnectorQueries() {
      queryMap = new HashMap<String, QueryUrls>();
    }

    public int size() {
      return queryMap.size();
    }

    public Collection<String> getConnectors() {
      return queryMap.keySet();
    }

    public QueryUrls getQueryUrls(String connector) {
      return queryMap.get(connector);
    }

    private QueryUrls putQueryUrls(String connector, QueryUrls queryUrls) {
      return queryMap.put(connector, queryUrls);
    }
  }

  /**
   * {@code QueryUrls} is a map from docid strings to the corresponding parsed
   * representation as a ParsedUrl.
   */
  public static class QueryUrls {
    Map<String, ParsedUrl> urlMap;

    /*
     * Private constructor so this class can only be constructed by
     * AuthorizationParser.
     */
    private QueryUrls() {
      urlMap = new HashMap<String, ParsedUrl>();
    }

    private void putUrl(String docid, ParsedUrl p) {
      urlMap.put(docid, p);
    }

    public Collection<String> getDocids() {
      return urlMap.keySet();
    }

    public int size() {
      return urlMap.size();
    }

    public ParsedUrl getUrl(String docid) {
      return urlMap.get(docid);
    }
  }
}
