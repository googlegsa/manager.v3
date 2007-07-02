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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
  private Map parseMap;
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
    AuthParser authParser = new AuthParser(xmlBody);
    authParser.parse();
    status = authParser.getStatus();
    if (status == ConnectorMessageCode.ERROR_PARSING_XML_REQUEST) {
      ServletUtil.writeResponse(out,status);
    }
    parseMap = authParser.getParseMap();
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
    for (Iterator i = parseMap.entrySet().iterator(); i.hasNext();) {
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
      List docidList = new ArrayList(urlsByDocid.keySet());
      Set answerSet = manager
          .authorizeDocids(connectorName, docidList, identity);
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



  
}
