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

package com.google.enterprise.connector.security.connectors.connauth;

import com.google.common.collect.Lists;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

/**
 * A SAX handler for parsing &lt;AuthnResponse> messages sent from a connector
 * manager.
 *
 */
final class ConnAuthnResponseHandler extends DefaultHandler {

  private static final String IDENTITY = "Identity";
  private static final String SUCCESS = "Success";
  private static final String FAILURE = "Failure";

  private StringBuilder currentIdentity = new StringBuilder();
  private boolean inIdentity = false;
  private String currentConnectorManager;
  private String currentConnector;

  private List<ConnectorUserInfo> userInfos = Lists.newArrayList();

  public ConnAuthnResponseHandler(String currentConnectorManager) {
    super();
    this.currentConnectorManager = currentConnectorManager;
  }

  /**
   * @see
   * org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
   */
  @Override
  public void startElement(String uri, String localName, String qName,
                           Attributes attributes) throws SAXException {
    if (IDENTITY.equals(localName)) {
      inIdentity = true;
    } else if (SUCCESS.equals(localName) || FAILURE.equals(localName)) {
      currentConnector = attributes.getValue("ConnectorName");
      if (currentConnector == null) {
        throw new SAXException("<" + localName + "> element is missing "
                               + "\"ConnectorName\" attribute.");
      }
    }
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(String, String, String)
   */
  @Override
  public void endElement(String uri, String localName, String qName) {
    if (IDENTITY.equals(localName)) {
      inIdentity = false;
      return;
    }
    if (SUCCESS.equals(localName)) {
      userInfos.add(new ConnectorUserInfo(currentConnectorManager,
          currentConnector, currentIdentity.toString()));
    }
    if (SUCCESS.equals(localName) || FAILURE.equals(localName)) {
      currentConnector = null;
      currentIdentity.setLength(0);
    }
  }

  /**
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  @Override
  public void characters(char[] ch, int start, int length) {
    if (inIdentity) {
      currentIdentity.append(ch, start, length);
    }
  }

  /**
   * Call this after parsing an XML document.
   *
   * @return Connector user information associated with the search
   * user.
   */
  public List<ConnectorUserInfo> getConnectorUserInfos() {
    return userInfos;
  }
}
