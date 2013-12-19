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

import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.servlet.AuthorizationParser.ConnectorQueries;
import com.google.enterprise.connector.servlet.AuthorizationParser.QueryResources;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthorizationResponse;
import com.google.enterprise.connector.spi.XmlUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map.Entry;

/**
 * This class does the real work for the authorization servlet
 */
public class AuthorizationHandler {
  private static final Logger LOGGER =
      Logger.getLogger(AuthorizationHandler.class.getName());

  String xmlBody;
  Manager manager;
  PrintWriter out;
  ConnectorMessageCode status;

  Map<AuthorizationResource, AuthorizationResponse.Status> results;

  AuthorizationHandler(String xmlBody, Manager manager, PrintWriter out) {
    this.xmlBody = xmlBody;
    this.manager = manager;
    this.out = out;
    results = new HashMap<AuthorizationResource, AuthorizationResponse.Status>();
  }

  /**
   * Factory method for testing.  Ensures that the results come back in a
   * predictable order.
   */
  static AuthorizationHandler makeAuthorizationHandlerForTest(String xmlBody,
      Manager manager, PrintWriter out) {
    AuthorizationHandler authorizationHandler = new AuthorizationHandler(
        xmlBody, manager, out);
    authorizationHandler.results =
        new TreeMap<AuthorizationResource, AuthorizationResponse.Status>();
    return authorizationHandler;
  }

  /**
   * Writes an answer for each resource from the request.
   */
  public void handleDoPost() {
    NDC.pushAppend("AuthZ");
    try {
      AuthorizationParser authorizationParser =
          new AuthorizationParser(xmlBody);
      status = authorizationParser.getStatus();
      if (status.getMessageId() ==
          ConnectorMessageCode.ERROR_PARSING_XML_REQUEST) {
        ServletUtil.writeResponse(out,status);
        return;
      }
      computeResultSet(authorizationParser);
      generateXml();
    } finally {
      NDC.pop();
    }
  }

  private void generateXml() {
    ServletUtil.writeRootTag(out, false);
    if (results.size() > 0) {
      ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHZ_RESPONSE, false);
      generateEachResultXml();
      ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHZ_RESPONSE, true);
    }
    ServletUtil.writeMessageCode(out, status);
    ServletUtil.writeRootTag(out, true);
  }

  private void generateEachResultXml() {
    for (Entry<AuthorizationResource, AuthorizationResponse.Status> e :
         results.entrySet()) {
      writeResultElement(e.getKey(), e.getValue());
    }
  }

  private void writeResultElement(AuthorizationResource resource,
      AuthorizationResponse.Status decision) {
    ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_ANSWER, false);
    // Add the connector name attribute to the resource element.
    try {
      // TODO: Either fix ServletUtil XML code to XML escape attr values and
      // element text bodies, or add the ability to append attributes to
      // XmlUtils.appendStartTag().
      out.write(ServletUtil.indentStr(3) + "<" + ServletUtil.XMLTAG_RESOURCE);
      XmlUtils.xmlAppendAttr(ServletUtil.XMLTAG_CONNECTOR_NAME_ATTRIBUTE,
                             resource.getConnectorName(), out);
      out.append('>');
      XmlUtils.xmlAppendAttrValue(resource.getUrl(), out);
      XmlUtils.xmlAppendEndTag(ServletUtil.XMLTAG_RESOURCE, out);
    } catch (IOException e) {
      // Can't happen with PrintWriter.
    }
    ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_DECISION,
                                decision.toString());
    ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_ANSWER, true);
  }

  private void computeResultSet(AuthorizationParser authorizationParser) {
    for (AuthenticationIdentity identity: authorizationParser.getIdentities()) {
      NDC.pushAppend(identity.getUsername());
      try {
        ConnectorQueries queries =
            authorizationParser.getConnectorQueriesForIdentity(identity);
        runManagerQueries(identity, queries);
      } finally {
        NDC.pop();
      }
    }
  }

  private void runManagerQueries(AuthenticationIdentity identity,
      ConnectorQueries urlsByConnector) {
    for (String connectorName : urlsByConnector.getConnectors()) {
      NDC.pushAppend(connectorName);
      try {
        // TODO [bmj]: It is conceivable that multiple URLs (resources) could
        // map to the same repository document (docid).  We should handle the
        // duplicates, somehow?
        QueryResources urlsByDocid = urlsByConnector.getQueryResources(connectorName);
        List<String> docidList = new ArrayList<String>(urlsByDocid.getDocids());
        Collection<AuthorizationResponse> answerSet =
            manager.authorizeDocids(connectorName, docidList, identity);
        if (answerSet != null) {
          accumulateQueryResults(answerSet, urlsByDocid);
        }
      } finally {
        NDC.pop();
      }
    }
  }

  private void accumulateQueryResults(Collection<AuthorizationResponse> answerSet,
      QueryResources urlsByDocid) {
    Set<String> docids = new HashSet<String>(urlsByDocid.getDocids());
    for (AuthorizationResponse response : answerSet) {
      String docid = response.getDocid();
      AuthorizationResource resource = urlsByDocid.getResource(docid);
      if (resource == null) {
        LOGGER.warning("Received unexpected AuthorizationResponse for document "
                       + docid);
      } else {
        results.put(resource, response.getStatus());
        docids.remove(docid);
      }
    }
    // Return DENY for documents not returned by connector.
    for (String docid : docids) {
      AuthorizationResource resource = urlsByDocid.getResource(docid);
      results.put(resource, AuthorizationResponse.Status.DENY);
      if (LOGGER.isLoggable(Level.FINEST)) {
        LOGGER.finest("AUTHORIZED " + docid + ": "
                      + AuthorizationResponse.Status.DENY);
      }
    }
  }
}
