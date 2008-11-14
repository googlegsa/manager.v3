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

package com.google.enterprise.saml.server;

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.common.GettableHttpServlet;
import com.google.enterprise.saml.common.PostableHttpServlet;
import com.google.enterprise.saml.common.SecurityManagerServlet;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPArtifactEncoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.saml.common.OpenSamlUtil.SM_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertion;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAuthnStatement;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatusMessage;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSubject;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.selectPeerEndpoint;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;

/**
 * Handler for SAML authentication requests.  These requests are sent by a service provider, in our
 * case the Google Search Appliance.  This is one part of the security manager's identity provider.
 */
public class SamlAuthn extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {

  /** Required for serializable classes. */
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(SamlAuthn.class.getName());

  private final EntityDescriptor localEntity;
  private final EntityDescriptor peerEntity;

  /**
   * Create a new SAML authentication server.
   *
   * @param localEntity Metadata descriptor for the identity provider this servlet is a part of.
   * @param peerEntity Metadata descriptor for the service provider this servlet accepts requests
   * from.
   */
  public SamlAuthn(EntityDescriptor localEntity, EntityDescriptor peerEntity) {
    this.localEntity = localEntity;
    this.peerEntity = peerEntity;
  }

  /**
   * Accept an authentication request and (eventually) respond to the service provider with a
   * response.  The request is generated by the service provider, then sent to the user agent as a
   * redirect.  The user agent redirects here, with the SAML AuthnRequest message encoded as a query
   * parameter.
   *
   * It's our job to authenticate the user behind the agent.  At the moment we respond with a
   * trivial form that prompts for username and password, but soon this will be replaced by
   * something more sophisticated.  Once the user agent posts the credentials, we validate them and
   * send the response.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();

    // Establish the SAML message context
    SAMLMessageContext<AuthnRequest, Response, NameID> context = newSamlMessageContext(session);
    initializeLocalEntity(context, localEntity, localEntity.getIDPSSODescriptor(SAML20P_NS),
                          SingleSignOnService.DEFAULT_ELEMENT_NAME);
    initializePeerEntity(context, peerEntity, peerEntity.getSPSSODescriptor(SAML20P_NS),
                         AssertionConsumerService.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_ARTIFACT_BINDING_URI);
    context.setInboundMessageIssuer(peerEntity.getEntityID());
    context.setOutboundMessageIssuer(localEntity.getEntityID());

    // Decode the request
    context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
    runDecoder(new HTTPRedirectDeflateDecoder(), context);

    // If there's a cookie we can decode, use that
    if (tryCookies(request, response)) {
      return;
    }

    // Otherwise, query the user
    PrintWriter out = initNormalResponse(response);
    out.print("<html><head><title>Please Login</title></head><body>\n" +
              "<form action=\"" +
              request.getRequestURL().toString() +
              "\" method=POST>\n" +
              "User Name:<input type=text name=username><br>\n" +
              "Password:<input type=password name=password><br>\n" +
              "<input type=submit>\n" +
              "</form>\n" +
              "</body></html>\n");
    out.close();
  }

  /**
   * Try to find a cookie that can be decoded into an identity.
   *
   * @param request The HTTP request message.
   * @param response The HTTP response message.  Filled in if a suitable cookie was found.
   * @return Whether or not a usable cookie was found.
   */
  private boolean tryCookies(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    Map<String, String> cookieJar = getCookieJar(request);
    if (cookieJar == null) {
      return false;
    }
    ConnectorManager manager = getConnectorManager(getServletContext());
    List<ConnectorStatus> connList = getConnectorStatuses(manager);
    for (ConnectorStatus status: connList) {
      String connectorName = status.getName();
      LOGGER.info("Got security plug-in " + connectorName);
      AuthenticationResponse authnResponse =
          manager.authenticate(connectorName, null, null, cookieJar);
      if ((authnResponse != null) && authnResponse.isValid()) {
        // TODO make sure authnResponse has subject in BackEnd
        String username = authnResponse.getData();
        if (username != null) {
          SAMLMessageContext<AuthnRequest, Response, NameID> context =
              existingSamlMessageContext(request.getSession());
          context.setOutboundSAMLMessage(
              makeSuccessfulResponse(context.getInboundSAMLMessage(), username));
          doRedirect(context, manager.getBackEnd(), response);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * If this request carries cookies, we use registered security connectors to
   * figure out the user identity from the cookie.
   *
   * @param request The HTTP request message.
   * @return A Map of cookies, or null if no cookies.
   */
  private Map<String, String> getCookieJar(HttpServletRequest request) {
    Cookie[] jar = request.getCookies();
    if ((jar == null) || (jar.length == 0)) {
      return null;
    }
    Map<String, String> cookieJar = new HashMap<String, String>(jar.length);
    for (int i = 0; i < jar.length; i++) {
      cookieJar.put(jar[i].getName(), jar[i].getValue());
    }
    return cookieJar;
  }

  @SuppressWarnings("unchecked")
  private List<ConnectorStatus> getConnectorStatuses(ConnectorManager manager) {
    List<ConnectorStatus> connList = manager.getConnectorStatuses();
    if (connList == null || connList.isEmpty()) {
      instantiateConnector(manager);
      connList = manager.getConnectorStatuses();
    }
    return connList;
  }

  // TODO get rid of this when we have a way of configuring plug-ins
  private void instantiateConnector(ConnectorManager manager) {
    String connectorName = "Lei";
    String connectorType = "CookieConnector";
    String language = "en";

    Map<String, String> configData =
        ImmutableMap.of(
            "CookieName", "SMSESSION",
            "ServerUrl", "http://gama.corp.google.com/user1/ssoAgent.asp",
            "HttpHeaderName", "User-Name");
    try {
      manager.setConnectorConfig(connectorName, connectorType,
                                 configData, language, false);
    } catch (ConnectorNotFoundException e) {
      LOGGER.info("ConnectorNotFound: " + e.toString());
    } catch (InstantiatorException e) {
      LOGGER.info("Instantiator: " + e.toString());
    } catch (PersistentStoreException e) {
      LOGGER.info("PersistentStore: " + e.toString());
    }
  }

  /**
   * Extract the username and password from the parameters, then ask the backend to validate them.
   * The backend returns the appropriate SAML Response message, which we then encode and return to
   * the service provider.  At the moment we only support the Artifact binding for the response.
   *
   * @param request The HTTP request message.
   * @param response The HTTP response message.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    BackEnd backend = getBackEnd(getServletContext());

    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        existingSamlMessageContext(request.getSession());

    // Get credentials and confirm they are present
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    if ((username == null) || (password == null)) {
      context.setOutboundSAMLMessage(
          makeUnsuccessfulResponse(context.getInboundSAMLMessage(),
                                   StatusCode.REQUESTER_URI,
                                   "Missing required POST parameter(s)"));
    } else {
      context.setOutboundSAMLMessage(
          backend.validateCredentials(context.getInboundSAMLMessage(), username, password));
    }

    doRedirect(context, backend, response);
  }

  public static Response makeSuccessfulResponse(AuthnRequest request, String username) {
    LOGGER.log(Level.INFO, "Authenticated successfully as " + username);
    Response response = makeResponse(request, makeStatus(StatusCode.SUCCESS_URI));
    Assertion assertion = makeAssertion(makeIssuer(SM_ISSUER), makeSubject(username));
    assertion.getAuthnStatements().add(makeAuthnStatement(AuthnContext.IP_PASSWORD_AUTHN_CTX));
    response.getAssertions().add(assertion);
    return response;
  }

  public static Response makeUnsuccessfulResponse(AuthnRequest request, String code,
                                                  String message) {
    LOGGER.log(Level.WARNING, message);
    Status status = makeStatus(code);
    status.setStatusMessage(makeStatusMessage(message));
    return makeResponse(request, status);
  }

  private void doRedirect(SAMLMessageContext<AuthnRequest, Response, NameID> context,
      BackEnd backend, HttpServletResponse response)
      throws ServletException {
    // Encode the response message
    initResponse(response);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
    HTTPArtifactEncoder encoder = new HTTPArtifactEncoder(null, null, backend.getArtifactMap());
    encoder.setPostEncoding(false);
    runEncoder(encoder, context);
  }
}
