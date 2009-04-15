// Copyright 2007-2009 Google Inc.
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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * This class does the real work for the authorization servlet
 */
public class AuthorizationHandler {
  String xmlBody;
  Manager manager;
  PrintWriter out;
  int status;
  int numDocs;
  private Map<String, Map<String, Map<String, ParsedUrl>>> parseMap;
  Map<String, Boolean> results;

  AuthorizationHandler(String xmlBody, Manager manager, PrintWriter out) {
    this.xmlBody = xmlBody;
    this.manager = manager;
    this.out = out;
    results = new HashMap<String, Boolean>();
  }

  /**
   * Factory method for testing.  Ensures that the results come back in a
   * predictable order.
   */
  static AuthorizationHandler makeAuthorizationHandlerForTest(String xmlBody,
      Manager manager, PrintWriter out) {
    AuthorizationHandler authorizationHandler = new AuthorizationHandler(
        xmlBody, manager, out);
    authorizationHandler.results = new TreeMap<String, Boolean>();
    return authorizationHandler;
  }

  /**
   * Writes an answer for each resource from the request.
   */
  public void handleDoPost() {
    AuthorizationParser authorizationParser = new AuthorizationParser(xmlBody);
    authorizationParser.parse();
    status = authorizationParser.getStatus();
    if (status == ConnectorMessageCode.ERROR_PARSING_XML_REQUEST) {
      ServletUtil.writeResponse(out,status);
      return;
    }
    parseMap = authorizationParser.getParseMap();
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
    for (Entry<String, Boolean> e : results.entrySet()) {
      writeResultElement(e.getKey(), e.getValue());
    }
  }

  private void writeResultElement(String url, boolean permit) {
    ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_ANSWER, false);
    ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_RESOURCE, url);
    ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_DECISION,
                                (permit) ? "Permit" : "Deny");
    ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_ANSWER, true);
  }

  private void computeResultSet() {
    for (Entry<String, Map<String, Map<String, ParsedUrl>>> e : parseMap.entrySet()) {
      String identity = e.getKey();
      Map<String, Map<String, ParsedUrl>> urlsByConnector = e.getValue();
      runManagerQueries(identity, urlsByConnector);
    }
  }

  private void runManagerQueries(String identity,
      Map<String, Map<String, ParsedUrl>> urlsByConnector) {
    for (Entry<String, Map<String, ParsedUrl>> e : urlsByConnector.entrySet()) {
      String connectorName = e.getKey();
      Map<String, ParsedUrl> urlsByDocid = e.getValue();
      List<String> docidList = new ArrayList<String>(urlsByDocid.keySet());
      Set<String> answerSet =
          manager.authorizeDocids(connectorName, docidList, identity);
      accumulateQueryResults(answerSet, urlsByDocid);
    }
  }

  private void accumulateQueryResults(Set<String> answerSet,
      Map<String, ParsedUrl> urlsByDocid) {
    for (Entry<String, ParsedUrl> e : urlsByDocid.entrySet()) {
      String docid = e.getKey();
      ParsedUrl parsedUrl = e.getValue();
      Boolean permit = answerSet.contains(docid) ? Boolean.TRUE : Boolean.FALSE;
      Object isDup = results.put(parsedUrl.getUrl(), permit);
      if (isDup != null) {
        //TODO (ziff): warning
      }
    }
  }
}
