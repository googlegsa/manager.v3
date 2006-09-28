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

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Admin servlet for authorization
 *
 */
public class Authorization extends HttpServlet {
  private static final Logger LOG =
    Logger.getLogger(Authorization.class.getName());

  private static String PROTOCOL = "googleconnector://";
  private static String DOCID = "docID=";

  /**
   * Returns an answer for each resource from the request.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);  
  }

  /**
   * Returns an answer for each resource from the request.
   * @param req 
   * @param res 
   * @throws ServletException 
   * @throws IOException 
   * 
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    BufferedReader reader = req.getReader();
    PrintWriter out = res.getWriter();
    res.setContentType(ServletUtil.MIMETYPE_XML);
    String xmlBody = StringUtils.readAllToString(reader);
    if (xmlBody.length() < 1) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_REQUEST;
      ServletUtil.writeSimpleResponse(out, status);
      LOG.info("The request is empty");
      return;
    }

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    handleDoPost(out, xmlBody, manager);
    out.close();

  }

  public static void handleDoPost(PrintWriter out,
      String xmlBody, Manager manager) {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(xmlBody, errorHandler);
    NodeList nodeList =
        document.getElementsByTagName(ServletUtil.XMLTAG_AUTHZ_QUERY);
    if (nodeList.getLength() == 0) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE;
      LOG.info(ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE);
      ServletUtil.writeSimpleResponse(out, status);
      return;
    }

    NodeList queryList =
        document.getElementsByTagName(ServletUtil.XMLTAG_CONNECTOR_QUERY);
    if (queryList.getLength() == 0) {
      status = ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE;
      LOG.info(ServletUtil.XML_RESPONSE_STATUS_EMPTY_NODE);
      ServletUtil.writeSimpleResponse(out, status);
      return;
    }

    int numDocs = 0;
    Map map = new TreeMap();
    for (int i = 0; i < queryList.getLength(); ++i) {
      String identity = ServletUtil.getFirstElementByTagName(
          (Element) queryList.item(i), ServletUtil.XMLTAG_IDENTITY);
      String source = ServletUtil.getFirstAttribute(
          (Element) queryList.item(i), ServletUtil.XMLTAG_IDENTITY, "source");
      List resources = ServletUtil.getAllElementsByTagName(
          (Element) queryList.item(i), ServletUtil.XMLTAG_RESOURCE);
      if (resources.isEmpty()) {
    	status = ServletUtil.XML_RESPONSE_STATUS_NULL_RESOURCE;
    	continue;
      }
      for (Iterator iter = resources.iterator(); iter.hasNext();) {
        String url = (String) iter.next();
        String connectorName = url.substring(
            url.indexOf(PROTOCOL) + PROTOCOL.length());
        if (url.lastIndexOf(DOCID) == -1) {
          status = ServletUtil.XML_RESPONSE_STATUS_NULL_DOCID;
          continue;
        }
        if (connectorName.indexOf('.') != -1) {
          connectorName = connectorName.substring(0, connectorName.indexOf('.'));
        } else {
          connectorName = connectorName.substring(0, connectorName.indexOf('/'));
        }
        if (connectorName.length() < 1) {
          status = ServletUtil.XML_RESPONSE_STATUS_NULL_CONNECTOR;
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
            urlList = (List) map.get(connectorName);
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
      LOG.info("No docid available.");
      ServletUtil.writeSimpleResponse(out, status);
      return;
    }

    Set set = map.entrySet();
    Iterator iterator = set.iterator();
    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, false);
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
              url.lastIndexOf(DOCID) + DOCID.length(), url.length()));
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
    ServletUtil.writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID, status);
    ServletUtil.writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, true);

    return;
  }
}
