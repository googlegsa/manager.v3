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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorizationParser {

  private String xmlBody;
  private static final Logger LOGGER =
      Logger.getLogger(AuthorizationParser.class.getName());

  private int status;
  private int numDocs;
  private Map <String, Map<String, Map<String, ParsedUrl>>> parseMap;

  public AuthorizationParser(String xmlBody) {
    this.xmlBody = xmlBody;
    status = ConnectorMessageCode.SUCCESS;
    numDocs = 0;
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
   * In practice, for now, it is unlikely that the same connector will
   * show up under more than one identity. In fact, the most likely case
   * is that the two top-level maps have only one item.
   *
   * Visibility is default to facilitate testing
   */
  void parse() {
    Element root =
        ServletUtil.parseAndGetRootElement(xmlBody,
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
    parseMap = new HashMap<String, Map<String, Map<String, ParsedUrl>>>();

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
    String identity =
        ServletUtil.getFirstElementByTagName(queryItem,
            ServletUtil.XMLTAG_IDENTITY);
    String source =
        ServletUtil.getFirstAttribute(queryItem, ServletUtil.XMLTAG_IDENTITY,
            "source");
    List <String> resources =
        ServletUtil.getAllElementsByTagName(queryItem,
            ServletUtil.XMLTAG_RESOURCE);
    if (resources.isEmpty()) {
      status = ConnectorMessageCode.RESPONSE_NULL_RESOURCE;
      return;
    }

    Map <String, Map<String, ParsedUrl>> urlsByConnector =
        parseMap.get(identity);
    if (urlsByConnector == null) {
      urlsByConnector = new HashMap<String, Map<String, ParsedUrl>>();
      parseMap.put(identity, urlsByConnector);
    }

    for (Iterator <String> iter = resources.iterator(); iter.hasNext();) {
      parseResource(urlsByConnector, iter);
    }
  }

  private void parseResource(
      Map <String, Map<String, ParsedUrl>> urlsByConnector,
      Iterator <String> iter) {
    String url = iter.next();
    ParsedUrl p = new ParsedUrl(url);
    if (p.getStatus() == ConnectorMessageCode.SUCCESS) {
      Map <String, ParsedUrl> urlsByDocid =
          urlsByConnector.get(p.getConnectorName());
      if (urlsByDocid == null) {
        urlsByDocid = new HashMap<String, ParsedUrl>();
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
    return parseMap.size();
  }

  /**
   * Return number of connector names found for a given identity.
   * Just for testing.
   *
   * @return number of identities
   */
  int countConnectorsForIdentity(String identity) {
    Map <String, Map<String, ParsedUrl>> urlsByConnector =
        parseMap.get(identity);
    if (urlsByConnector == null) {
      return 0;
    }
    return urlsByConnector.size();
  }

  /**
   * Return number of urls found for a given identity-connector pair.
   * Just for testing.
   *
   * @return number of identities
   */
  int countUrlsForIdentityConnectorPair(String identity, String connectorName) {
    Map <String, Map<String, ParsedUrl>> urlsByConnector =
        parseMap.get(identity);
    if (urlsByConnector == null) {
      return 0;
    }
    Map <String, ParsedUrl> urlsByDocid = urlsByConnector.get(connectorName);
    if (urlsByDocid == null) {
      return 0;
    }
    return urlsByDocid.size();
  }

  public int getNumDocs() {
    return numDocs;
  }

  public int getStatus() {
    return status;
  }

  public Map <String, Map<String, Map<String, ParsedUrl>>> getParseMap() {
    return parseMap;
  }
}
