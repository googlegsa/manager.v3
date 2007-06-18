// Copyright (C) 2006 Google Inc.
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

import com.google.enterprise.connector.manager.Manager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Admin servlet for authorization
 *
 */
public class Authorization extends ConnectorManagerServlet {
  private static final Logger LOGGER =
    Logger.getLogger(Authorization.class.getName());

  /*
   * (non-Javadoc)
   * @see com.google.enterprise.connector.servlet.ConnectorManagerServlet
   * #processDoPost(java.lang.String,
   * com.google.enterprise.connector.manager.Manager, java.io.PrintWriter)
   */
  protected void processDoPost(
      String xmlBody, Manager manager, PrintWriter out) {
    handleDoPost(xmlBody, manager, out);
  }

  /**
   * Writes an answer for each resource from the request.
   *
   * @param xmlBody String the XML request body string 
   * @param manager Manager
   * @param out PrintWriter where the response is written
   */
  public static void handleDoPost(
      String xmlBody, Manager manager, PrintWriter out) {
    Element root = ServletUtil.parseAndGetRootElement(
        xmlBody, ServletUtil.XMLTAG_AUTHZ_QUERY);
    if (root == null) {
      ServletUtil.writeResponse(
          out, ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return;
    }

    NodeList queryList =
        root.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_QUERY);
    if (queryList.getLength() == 0) {
      LOGGER.log(Level.WARNING, ServletUtil.LOG_RESPONSE_EMPTY_NODE);
      ServletUtil.writeResponse(
          out, ConnectorMessageCode.RESPONSE_EMPTY_NODE);
      return;
    }

    int status = ConnectorMessageCode.SUCCESS;
    int numDocs = 0;
    Map map = new TreeMap();
    for (int i = 0; i < queryList.getLength(); ++i) {
      Element queryItem = (Element) queryList.item(i);
      String identity = ServletUtil.getFirstElementByTagName(
          queryItem, ServletUtil.XMLTAG_IDENTITY);
      String source = ServletUtil.getFirstAttribute(
          queryItem, ServletUtil.XMLTAG_IDENTITY, "source");
      List resources = ServletUtil.getAllElementsByTagName(
          queryItem, ServletUtil.XMLTAG_RESOURCE);
      if (resources.isEmpty()) {
    	status = ConnectorMessageCode.RESPONSE_NULL_RESOURCE;
    	continue;
      }
      for (Iterator iter = resources.iterator(); iter.hasNext();) {
        String url = (String) iter.next();
        String connectorName = url.substring(
            url.indexOf(ServletUtil.PROTOCOL) + ServletUtil.PROTOCOL.length());
        if (url.lastIndexOf(ServletUtil.DOCID) == -1) {
          status = ConnectorMessageCode.RESPONSE_NULL_DOCID;
          continue;
        }
        if (connectorName.indexOf('.') != -1) {
          connectorName = connectorName.substring(0, connectorName.indexOf('.'));
        } else {
          connectorName = connectorName.substring(0, connectorName.indexOf('/'));
        }
        if (connectorName.length() < 1) {
          status = ConnectorMessageCode.RESPONSE_NULL_CONNECTOR;
          continue;
        }
        Map connDocs = null;
        List urlList = null;
        if (map.containsKey(identity) == false) {
          connDocs = new TreeMap();
          urlList = new ArrayList();
          connDocs.put(connectorName, urlList);
          map.put(identity, connDocs);
        } else {
          connDocs = (Map) map.get(identity);
          if (connDocs.containsKey(connectorName)) {
            urlList = (List) connDocs.get(connectorName);
          } else {
            urlList = new ArrayList();
            connDocs.put(connectorName, urlList);
          }
        }
        urlList.add(url);
        ++numDocs;
      }
    }
    if (numDocs == 0) {
      LOGGER.warning("No docid available.");
      ServletUtil.writeResponse(out, status);
      return;
    }

    Set set = map.entrySet();
    Iterator iterator = set.iterator();
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHZ_RESPONSE, false);
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry)iterator.next();
      Map connDocs = (Map) entry.getValue();
      Set connDocSet = connDocs.entrySet();
      Iterator iterConnDocs = connDocSet.iterator();
      while (iterConnDocs.hasNext()) {
        Map.Entry entryConn = (Map.Entry)iterConnDocs.next();
        List docidList = new ArrayList();
        for (Iterator iterUrl = ((List) entryConn.getValue()).iterator();
            iterUrl.hasNext();) {
          String url = (String) iterUrl.next();
          docidList.add(url.substring(
              url.lastIndexOf(ServletUtil.DOCID) +
              ServletUtil.DOCID.length(), url.length()));
        }
        Set answerSet = manager.authorizeDocids((String) entryConn.getKey(),
            docidList, (String) entry.getKey());    
        // Assume that iter and iterDocid are in parallel.
        Iterator iter = ((List) entryConn.getValue()).iterator();
        Iterator iterDocid = docidList.iterator();
        while (iter.hasNext() && iterDocid.hasNext()) {
          ServletUtil.writeXMLTag(
              out, 2, ServletUtil.XMLTAG_ANSWER, false);
          ServletUtil.writeXMLElement(
              out, 3, ServletUtil.XMLTAG_RESOURCE, (String) iter.next());
          if (answerSet.contains(iterDocid.next())) {
            ServletUtil.writeXMLElement(
              out, 3, ServletUtil.XMLTAG_DECISION, "Permit");
          } else {
            ServletUtil.writeXMLElement(
              out, 3, ServletUtil.XMLTAG_DECISION, "Deny");
          }
          ServletUtil.writeXMLTag(
              out, 2, ServletUtil.XMLTAG_ANSWER, true);
        }
      }
    }
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHZ_RESPONSE, true);
    ServletUtil.writeStatusId(out, status);
    ServletUtil.writeRootTag(out, true);

    return;
  }
}
