// Copyright (C) 2008, 2009 Google Inc.
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

package com.google.enterprise.security.connectors.connauth;

import com.google.enterprise.common.PostableHttpServlet;
import com.google.enterprise.connector.servlet.Authenticate;
import com.google.enterprise.connector.servlet.ConnectorMessageCode;
import com.google.enterprise.connector.servlet.ServletUtil;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockCMAuthServer extends Authenticate
  implements PostableHttpServlet {
  private static final Logger LOGGER = Logger.getLogger(MockCMAuthServer.class.getName());
  private static final long serialVersionUID = 1L;
  private static final String connectorName = "mockConnector";

  protected final Map<String, String> passwordMap;

  public MockCMAuthServer() {
    passwordMap = new HashMap<String, String>();
    passwordMap.put("joe", "plumber");
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    PrintWriter out = resp.getWriter();

    // What follows is mostly a copy of code from the connector manager.
    // We need it in order to parse the input request.
    Element root = ServletUtil.parseAndGetRootElement(
        req.getInputStream(),
        ServletUtil.XMLTAG_AUTHN_REQUEST);
    if (root == null) {
      ServletUtil.writeResponse(out, ConnectorMessageCode.ERROR_PARSING_XML_REQUEST);
      return;
    }

    NodeList credList = root.getElementsByTagName(ServletUtil.XMLTAG_AUTHN_CREDENTIAL);
    if (credList.getLength() == 0) {
      LOGGER.warning(ServletUtil.LOG_RESPONSE_EMPTY_NODE);
      ServletUtil.writeResponse(out, ConnectorMessageCode.RESPONSE_EMPTY_NODE);
      return;
    }

    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHN_RESPONSE, false);

    Element elt = Element.class.cast(credList.item(0));
    String username = ServletUtil.getFirstElementByTagName(elt, ServletUtil.XMLTAG_AUTHN_USERNAME);
    String password = ServletUtil.getFirstElementByTagName(elt, ServletUtil.XMLTAG_AUTHN_PASSWORD);
    String mapValue = passwordMap.get(username);
    if ((mapValue != null) && mapValue.equals(password)) {
      ServletUtil.writeXMLTagWithAttrs(
          out, 2, ServletUtil.XMLTAG_SUCCESS,
          ServletUtil.XMLTAG_CONNECTOR_NAME + "=\"" + connectorName + "\"",
          false);
      ServletUtil.writeXMLElement(
          out, 3, ServletUtil.XMLTAG_IDENTITY, username);
      ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_SUCCESS, true);
    } else {
      ServletUtil.writeXMLTagWithAttrs(
          out, 2, ServletUtil.XMLTAG_FAILURE,
          ServletUtil.XMLTAG_CONNECTOR_NAME + "=\"" + connectorName + "\"",
          true);
    }
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHN_RESPONSE, true);
    ServletUtil.writeRootTag(out, true);
  }
}
