// Copyright 2007 Google Inc.
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

import com.google.enterprise.connector.manager.Manager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class does the real work for the authorization servlet
 */
public class AuthorizationHandler {
  String xmlBody;
  Manager manager;
  PrintWriter out;
  private static final Logger LOGGER = Logger
      .getLogger(AuthorizationHandler.class.getName());

  int status;
  int numDocs;
  Map urlsByIdentity;
  Map results;

  AuthorizationHandler(String xmlBody, Manager manager, PrintWriter out) {
    this.xmlBody = xmlBody;
    this.manager = manager;
    this.out = out;
    results = new HashMap();
  }

  /**
   * Factory method for testing.  Ensures that the results come back in a 
   * predictable order.
   */
  static AuthorizationHandler makeAuthorizationHandlerForTest(String xmlBody,
      Manager manager, PrintWriter out) {
    AuthorizationHandler authorizationHandler = new AuthorizationHandler(
        xmlBody, manager, out);
    authorizationHandler.results = new TreeMap();
    return authorizationHandler;
  }

  /**
   * Writes an answer for each resource from the request.
   */
  public void handleDoPost() {
    parse();
    computeResultSet();
    generateXml();
    return;
  }

  private void generateXml() {
    
    ServletUtil.writeRootTag(out, false);

    if (results.size() > 0) {
      ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHZ_RESPONSE, false);
      generateEachResultXml();
      ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHZ_RESPONSE, true);
    }

    ServletUtil.writeStatusId(out, status);
    ServletUtil.writeRootTag(out, true);
  }

  private void generateEachResultXml() {
    for (Iterator i = results.entrySet().iterator(); i.hasNext(); ) {
      Entry e = (Entry) i.next();
      String url = (String) e.getKey();
      Boolean permit = (Boolean) e.getValue();
      writeResultElement(url, permit.booleanValue());
    }
  }

  private void writeResultElement(String url, boolean permit) {
    ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_ANSWER, false);
    ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_RESOURCE, url);
    if (permit) {
      ServletUtil
          .writeXMLElement(out, 3, ServletUtil.XMLTAG_DECISION, "Permit");
    } else {
      ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_DECISION, "Deny");
    }
    ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_ANSWER, true);
  }

  private void computeResultSet() {
    for (Iterator i = urlsByIdentity.entrySet().iterator(); i.hasNext();) {
      Entry e = (Entry) i.next();
      String identity = (String) e.getKey();
      Map urlsByConnector = (Map) e.getValue();
      runManagerQueries(identity, urlsByConnector);
    }
  }

  private void runManagerQueries(String identity, Map urlsByConnector) {
    for (Iterator i = urlsByConnector.entrySet().iterator(); i.hasNext();) {
      Entry e = (Entry) i.next();
      String connectorName = (String) e.getKey();
      Map urlsByDocid = (Map) e.getValue();
      Set docidSet = urlsByDocid.keySet();
      Set answerSet = manager
          .authorizeDocids(connectorName, docidSet, identity);
      accumulateQueryResults(answerSet, urlsByDocid);
    }
  }

  private void accumulateQueryResults(Set answerSet, Map urlsByDocid) {
    for (Iterator i = urlsByDocid.entrySet().iterator(); i.hasNext();) {
      Entry e = (Entry) i.next();
      String docid = (String) e.getKey();
      ParsedUrl parsedUrl = (ParsedUrl) e.getValue();
      Boolean permit = answerSet.contains(docid) ? Boolean.TRUE : Boolean.FALSE;
      Object isDup = results.put(parsedUrl.getUrl(), permit);
      if (isDup != null) {
        //TODO (ziff): warning
      }
    }
  }

  /**
   * Parse the XML into a map of maps of maps.
   * 
   * First-level map is keyed by identity - the value contains all the urls
   * governed by the same identity.
   * 
   * Second-level map is keyed by connector name - the value is all the urls
   * that come from the same connector.
   * 
   * Third-level map is keyed by docid - the value is a ParsedUrl.
   * 
   * In practice, for now, it is unlikely that the same connector will show up
   * under more than one identity. In fact, the most likely case is that the two
   * top-level maps have only one item.
   * 
   * Visibility is default to facilitate testing
   */
  void parse() {
    Element root = ServletUtil.parseAndGetRootElement(xmlBody,
        ServletUtil.XMLTAG_AUTHZ_QUERY);

    if (root == null) {
      ServletUtil.writeResponse(out,
          ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return;
    }

    NodeList queryList = root
        .getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_QUERY);

    if (queryList.getLength() == 0) {
      LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_EMPTY_NODE);
      return;
    }

    status = ConnectorMessageCode.SUCCESS;
    numDocs = 0;
    urlsByIdentity = new HashMap();

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
    String identity = ServletUtil.getFirstElementByTagName(queryItem,
        ServletUtil.XMLTAG_IDENTITY);
    String source = ServletUtil.getFirstAttribute(queryItem,
        ServletUtil.XMLTAG_IDENTITY, "source");
    List resources = ServletUtil.getAllElementsByTagName(queryItem,
        ServletUtil.XMLTAG_RESOURCE);
    if (resources.isEmpty()) {
      status = ConnectorMessageCode.RESPONSE_NULL_RESOURCE;
      return;
    }

    Map urlsByConnector = (Map) urlsByIdentity.get(identity);
    if (urlsByConnector == null) {
      urlsByConnector = new HashMap();
      urlsByIdentity.put(identity, urlsByConnector);
    }

    for (Iterator iter = resources.iterator(); iter.hasNext();) {
      parseResource(urlsByConnector, iter);
    }
  }

  private void parseResource(Map urlsByConnector, Iterator iter) {
    String url = (String) iter.next();
    ParsedUrl p = new ParsedUrl(url);
    if (p.getStatus() == ConnectorMessageCode.SUCCESS) {
      Map urlsByDocid = (Map) urlsByConnector.get(p.getConnectorName());
      if (urlsByDocid == null) {
        urlsByDocid = new HashMap();
        urlsByConnector.put(p.getConnectorName(), urlsByDocid);
      }
      urlsByDocid.put(p.getDocid(), p);
      numDocs++;
    } else {
      status = p.getStatus();
    }
  }

  /**
   * Return number of identities found. Just for testing.
   * 
   * @return number of identities
   */
  int countParsedIdentities() {
    return urlsByIdentity.size();
  }

  /**
   * Return number of connector names found for a given identity. Just for
   * testing.
   * 
   * @return number of identities
   */
  int countConnectorsForIdentity(String identity) {
    Map urlsByConnector = (Map) urlsByIdentity.get(identity);
    if (urlsByConnector == null) {
      return 0;
    }
    return urlsByConnector.size();
  }

  /**
   * Return number of urls found for a given identity-connector pair. Just for
   * testing.
   * 
   * @return number of identities
   */
  int countUrlsForIdentityConnectorPair(String identity, String connectorName) {
    Map urlsByConnector = (Map) urlsByIdentity.get(identity);
    if (urlsByConnector == null) {
      return 0;
    }
    Map urlsByDocid = (Map) urlsByConnector.get(connectorName);
    if (urlsByDocid == null) {
      return 0;
    }
    return urlsByDocid.size();
  }

  static class ParsedUrl {
    private static final Pattern URL_PATTERN = Pattern.compile("^"
        + ServletUtil.PROTOCOL + "([^./]*)(?:[^/]*)?"
        + "(?:/[dD][oO][cC]\\?(?:[^&]*&)*[dD][oO][cC][iI][dD]=([^&]*))?");

    private int urlStatus = ConnectorMessageCode.SUCCESS;
    private String url = null;
    private String connectorName = null;
    private String docid = null;

    ParsedUrl(String urlparam) {

      url = urlparam;
      Matcher matcher = URL_PATTERN.matcher(url);
      boolean found = matcher.find();

      if (!found) {
        urlStatus = ConnectorMessageCode.RESPONSE_NULL_CONNECTOR;
        return;
      } else {
        try {
          connectorName = matcher.group(1);
        } catch (IllegalStateException e) {
          // just leave the connectorName null - we'll catch the error later
        }
        try {
          docid = matcher.group(2);
        } catch (IllegalStateException e) {
          // just leave the docid null - we'll catch the error later
        }
      }

      if (docid == null || docid.length() < 1) {
        urlStatus = ConnectorMessageCode.RESPONSE_NULL_DOCID;
      }
      if (connectorName == null || connectorName.length() < 1) {
        urlStatus = ConnectorMessageCode.RESPONSE_NULL_CONNECTOR;
      }
    }

    public String getConnectorName() {
      return connectorName;
    }

    public String getDocid() {
      return docid;
    }

    public int getStatus() {
      return urlStatus;
    }

    public String getUrl() {
      return url;
    }

  }
}
