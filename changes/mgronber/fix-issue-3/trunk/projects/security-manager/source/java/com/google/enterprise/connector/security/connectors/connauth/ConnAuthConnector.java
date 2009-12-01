// Copyright (C) 2008 Google Inc.
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

import com.google.enterprise.connector.common.HttpExchange;
import com.google.enterprise.connector.common.SecurityManagerUtil;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;
import com.google.enterprise.connector.spi.VerificationStatus;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

public class ConnAuthConnector implements Connector, Session, AuthenticationManager {

  @SuppressWarnings("unused")
  private final String spiVersion;
  private static final Logger LOGGER =
    Logger.getLogger(ConnAuthConnector.class.getName());

  public ConnAuthConnector(String spiVersion) {
    this.spiVersion = spiVersion;
  }

  /**
   * Send an authentication request to a connector manager, to see if the
   * username and password provided by a search user is valid for any of the
   * connectors the manager is responsible for.
   */
  public AuthenticationResponse authenticate(AuthenticationIdentity raw)
      throws RepositoryException {
    SecAuthnIdentity identity = SecAuthnIdentity.class.cast(raw);
    AuthenticationResponse notfound = new AuthenticationResponse(false, null);
    String username = identity.getUsername();
    String password = identity.getPassword();
    if (username == null || password == null) {
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return notfound;
    }
    List<ConnectorUserInfo> connectorUserInfos = null;

    String siteUri = identity.getSampleUrl();
    if (siteUri == null) {
      LOGGER.warning("null URL for connector manager");
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return null;
    }

    String request = createAuthnRequest(username, password);
    HttpExchange exchange;
    try {
      exchange = SecurityManagerUtil.getHttpClient().postExchange(new URL(siteUri), null);
    } catch (MalformedURLException e2) {
      LOGGER.warning("Bad URL for connector manager: " + siteUri);
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return notfound;
    }

    try {
      exchange.setRequestHeader("Content-Type", "text/xml");
      exchange.setRequestBody(request.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e1) {
      LOGGER.warning("Bad Encoding: " + e1.toString());
      identity.setVerificationStatus(VerificationStatus.INDETERMINATE);
      return notfound;
    }

    try {
      int status = exchange.exchange();
      if (status > 300) {
        throw new IOException("Message exchange returned status " + status);
      }
    } catch (IOException e) {
      exchange.close();
      throw new RepositoryException(e);
    }

    try {
      connectorUserInfos = parseResponse(exchange.getResponseEntityAsStream(), "");
    } catch (SAXException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    } finally {
      exchange.close();
    }

    if ((connectorUserInfos == null) || connectorUserInfos.isEmpty()) {
      identity.setVerificationStatus(VerificationStatus.REFUTED);
      return notfound;
    }

    /*
     * "encode" authn result from multiple CMS into one AuthenticationResponse.
     * As long as there is one CMS that said "I know you", this user is considered good.
     * Results are encoded as <connector1 name>/<id1 name>,<connector2 name>/<id2 name>...
     * An "id name" is empty if that connector said "I don't know you".
     */
    StringBuffer data = new StringBuffer();
    for (ConnectorUserInfo user : connectorUserInfos) {
      if (data.length() > 0)
        data.append(",");
      data.append(user.getConnectorName());
      data.append("/");
      data.append(user.getIdentity());
    }
    identity.setVerificationStatus(VerificationStatus.VERIFIED);
    return new AuthenticationResponse(true, data.toString());
  }

  /**
   * Create the XML representing an &lt;AuthnRequest>.
   *
   * @param username The username to include in the request.
   * @param password The password to include in the request.
   * @return The XML string representing the &lt;AuthnRequest>.
   */
  private String createAuthnRequest(String username, String password) {
    StringBuffer builder = new StringBuffer();
    builder.append("<AuthnRequest>");
    builder.append("<Credentials>");
    builder.append("<Username>" + username + "</Username>");
    builder.append("<Password>" + password + "</Password>");
    builder.append("</Credentials>");
    builder.append("</AuthnRequest>");
    return builder.toString();
  }

  /**
   * Extract the connector identities to be associated with the search user
   * from a &lt;CmResponse> message.
   *
   * @param response The XML response message to parse.
   * @return A list of information associated with a search user,
   * where each item in the list is for a specific connector.
   * @throws SAXException If there was an error parsing response.
   * @throws IOException If there was an error reading response.
   */
  private List<ConnectorUserInfo> parseResponse(InputStream response, String managerId)
      throws SAXException, IOException {
    XMLReader reader = XMLReaderFactory.createXMLReader();
    ConnAuthnResponseHandler handler = new ConnAuthnResponseHandler(managerId);
    reader.setContentHandler(handler);
    reader.parse(new InputSource(response));
    return handler.getConnectorUserInfos();
  }

  public Session login() {
    return this;
  }

  public AuthenticationManager getAuthenticationManager() {
    return this;
  }

  public AuthorizationManager getAuthorizationManager() {
    throw new UnsupportedOperationException();
  }

  public TraversalManager getTraversalManager() {
    throw new UnsupportedOperationException();
  }
}
