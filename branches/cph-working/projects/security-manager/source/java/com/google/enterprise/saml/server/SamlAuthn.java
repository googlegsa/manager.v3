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
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.common.GettableHttpServlet;
import com.google.enterprise.saml.common.GsaConstants;
import com.google.enterprise.saml.common.PostableHttpServlet;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPArtifactEncoder;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatusMessage;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.selectPeerEndpoint;
import static com.google.enterprise.saml.common.ServletUtil.errorServletResponse;
import static com.google.enterprise.saml.common.ServletUtil.getBackEnd;
import static com.google.enterprise.saml.common.ServletUtil.htmlServletResponse;
import static com.google.enterprise.saml.common.ServletUtil.initializeServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;

/**
 * Handler for SAML authentication requests.  These requests are sent by a service provider, in our
 * case the Google Search Appliance.  This is one part of the security manager's identity provider.
 */
public class SamlAuthn extends HttpServlet
    implements GettableHttpServlet, PostableHttpServlet {

  /** Required for serializable classes. */
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(SamlAuthn.class.getName());

  /** Name of the session attribute that holds the SAML message context. */
  private static final String SSO_SAML_CONTEXT = "ssoSamlContext";

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
    SAMLMessageContext<AuthnRequest, Response, NameID> context = makeSamlMessageContext();
    initializeLocalEntity(context, localEntity, localEntity.getIDPSSODescriptor(SAML20P_NS),
                          SingleSignOnService.DEFAULT_ELEMENT_NAME);
    initializePeerEntity(context, peerEntity, peerEntity.getSPSSODescriptor(SAML20P_NS),
                         AssertionConsumerService.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_ARTIFACT_BINDING_URI);
    context.setInboundMessageIssuer(peerEntity.getEntityID());
    context.setOutboundMessageIssuer(localEntity.getEntityID());

    // Save the context for use when the form results are received
    session.setAttribute(SSO_SAML_CONTEXT, context);

    /**
     * If this request carries cookie, we use registered security connectors to
     * figure out user identity from the cookie.
     */
    Cookie[] jar = request.getCookies();
    if (jar != null) {
      HashMap cookieJar = new HashMap(jar.length);
      for (int i = 0; i < jar.length; i++) {
        cookieJar.put(jar[i].getName(), jar[i].getValue());
      }
 
      if (handleAuthn(response, gsaUrlString,
                      request.getParameter(GsaConstants.GSA_RELAY_STATE_PARAM_NAME),
                      null, null, cookieJar) != null)
        return;
    }
    
    // Decode the request
    context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
    runDecoder(new HTTPRedirectDeflateDecoder(), context);

    // Generate the login form
    PrintWriter out = htmlServletResponse(response);
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

  private HttpServletResponse handleAuthn(HttpServletResponse response,
      String gsaUrlString, String relay, String username, String password,
      HashMap cookieJar) throws IOException {
    
    ServletContext servletContext = this.getServletContext();
    ConnectorManager manager = (ConnectorManager) 
      Context.getInstance(servletContext).getManager();
    BackEnd backend = manager.getBackEnd();
    List connList = manager.getConnectorStatuses();
    if (connList == null || connList.isEmpty()) {
      instantiateConnector(manager);
      connList = manager.getConnectorStatuses();
    }
    for (Iterator iter = connList.iterator(); iter.hasNext();) {
      String connectorName = ((ConnectorStatus) iter.next()).getName();
      LOGGER.info("Got security plug-in " + connectorName);
      
      AuthenticationResponse authnResponse = 
        manager.authenticate(connectorName, username, password, cookieJar);
      if ((authnResponse != null) && authnResponse.isValid()) {
        // TODO make sure authnResponse has subject in BackEnd
        String subject = (authnResponse.getData() == null) ? username :
          authnResponse.getData();
        String redirectUrl = backend.loginRedirect(gsaUrlString, relay, subject);           

        if (redirectUrl == null) {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          response.sendError(404);
          return response;
        }
        
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.sendRedirect(redirectUrl);
        return response;
      }
    }
    
    return null;
  }

  /**
   * Extract the username and password from the parameters, then ask the backend to validate them.
   * The backend returns the appropriate SAML Response message, which we then encode and return to
   * the service provider.  At the moment we only support the Artifact binding for the response.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();
    BackEnd backend = getBackEnd(getServletContext());

    // Restore context and signal error if none.
    @SuppressWarnings("unchecked")
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        (SAMLMessageContext<AuthnRequest, Response, NameID>) session.getAttribute(SSO_SAML_CONTEXT);
    if (context == null) {
      LOGGER.log(Level.WARNING, "Unable to get identity provider message context.");
      errorServletResponse(response, SC_INTERNAL_SERVER_ERROR);
      return;
    }

    // Get credentials and confirm they are present
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    if ((username == null) || (password == null)) {
      LOGGER.log(Level.WARNING, "Missing required POST parameter(s)");
      Status status = makeStatus(StatusCode.REQUESTER_URI);
      status.setStatusMessage(makeStatusMessage("Missing required POST parameter(s)"));
      context.setOutboundSAMLMessage(makeResponse(context.getInboundSAMLMessage(), status));
    } else {
      context.setOutboundSAMLMessage(
          backend.validateCredentials(context.getInboundSAMLMessage(), username, password));
    }

    // Encode the response message
    initializeServletResponse(response);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
    HTTPArtifactEncoder encoder = new HTTPArtifactEncoder(null, null, backend.getArtifactMap());
    encoder.setPostEncoding(false);
    runEncoder(encoder, context);
  }

  // TODO get rid of this when we have a way of configuring plug-ins
  private void instantiateConnector(ConnectorManager manager) {
    String connectorName = "Lei";
    String connectorType = "CookieConnector";
    String language = "en";
    
    Map<String, String> configData;   
    configData = ImmutableMap.of("CookieName", "SMSESSION",
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
}
