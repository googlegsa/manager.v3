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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This class processes XML response from Connector Manager servlet calls and
 * set proper returning code, message and parameter list if applicable
 * (ConnectorMessageCode).
 * 
 */
public class ConnectorManagerServletResponse {

  private Document document;
  private ConnectorMessageCode cmMessageCode;

  /**
   * Constructor
   * Process an XML response returning from a servlet call set cmMessageCode.
   * 
   * @param xmlResponse String
   */
  public ConnectorManagerServletResponse(String xmlResponse) {
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    this.document = ServletUtil.parse(xmlResponse, errorHandler);
    this.cmMessageCode = new ConnectorMessageCode();
    if (this.document == null) {
      this.cmMessageCode.setMessageId(ConnectorMessageCode.EXCEPTION_XML_PARSING);
      return;
    }

    NodeList nodeList = document
        .getElementsByTagName(ServletUtil.XMLTAG_RESPONSE_ROOT);
    if (nodeList.getLength() == 0) {
      this.cmMessageCode.setMessageId(ConnectorMessageCode.RESPONSE_EMPTY_RESPONSE);
      return;
    }

    int statusId = ConnectorMessageCode.SUCCESS;
    try {
      statusId = Integer.parseInt(ServletUtil.getFirstElementByTagName(
        (Element) nodeList.item(0), ServletUtil.XMLTAG_STATUSID));
    } catch (NumberFormatException e) {
      this.cmMessageCode
          .setMessageId(ConnectorMessageCode.RESPONSE_STATUS_ID_NON_INTEGER);
      return;
    }

    this.cmMessageCode.setMessageId(statusId);
    if (statusId == ConnectorMessageCode.SUCCESS) {
      return;
    }

    String message = ServletUtil.getFirstElementByTagName((Element) nodeList
        .item(0), ServletUtil.XMLTAG_STATUS_MESSAGE);
    this.cmMessageCode.setMessage(message);

    NodeList paramList = ((Element) nodeList.item(0))
        .getElementsByTagName(ServletUtil.XMLTAG_STATUS_PARAMS);
    int length = paramList.getLength();
    if (length == 0) {
      return;
    }

    String[] params = new String[length];
    for (int i = 0; i < length; ++i) {
      int order = Integer.parseInt(((Element) paramList.item(i))
        .getAttribute(ServletUtil.XMLTAG_STATUS_PARAM_ORDER));
      if (order < 0 || order >= length) {
        continue;
      }
      params[order] = ((Element) paramList.item(i))
          .getAttribute(ServletUtil.XMLTAG_STATUS_PARAM);
    }
    this.cmMessageCode.setParams(params);

    return;
  }

  public ConnectorMessageCode getCmMessageCode() {
    return cmMessageCode;
  }

  public void setCmMessageCode(ConnectorMessageCode cmMessageCode) {
    this.cmMessageCode = cmMessageCode;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }
}
