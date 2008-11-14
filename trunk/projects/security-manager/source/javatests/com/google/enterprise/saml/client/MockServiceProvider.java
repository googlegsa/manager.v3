// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.saml.client;

import com.google.enterprise.saml.common.GettableHttpServlet;
import com.google.enterprise.saml.common.HttpServletRequestClientAdapter;
import com.google.enterprise.saml.common.HttpServletResponseClientAdapter;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.saml.common.GsaConstants.GSA_ARTIFACT_PARAM_NAME;
import static com.google.enterprise.saml.common.GsaConstants.GSA_RELAY_STATE_PARAM_NAME;
import static com.google.enterprise.saml.common.OpenSamlUtil.GOOGLE_PROVIDER_NAME;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeArtifactResolve;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertionConsumerService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAuthnRequest;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSpSsoDescriptor;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.selectPeerEndpoint;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpPost;
import static com.google.enterprise.saml.common.ServletUtil.errorServletResponse;
import static com.google.enterprise.saml.common.ServletUtil.htmlServletResponse;
import static com.google.enterprise.saml.common.ServletUtil.initializeServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

/**
 * The MockServiceProvider class implements a servlet pretending to be the part of a SAML Service
 * Provider that receives a service request from the user agent and initiates an authn request from
 * an identity provider.
 */
public class MockServiceProvider extends HttpServlet implements GettableHttpServlet {
  private static final String className = MockServiceProvider.class.getName();
  private static final Logger logger = Logger.getLogger(className);
  private static final long serialVersionUID = 1L;
  private static final String ISSUER = "http://google.com/mock-service-provider";

  private final EntityDescriptor spEntity;
  private final EntityDescriptor idpEntity;
  private final String serviceUrl;
  private final String acsUrl;
  private final GettableHttpServlet clientTransport;

  /**
   * Creates a new mock SAML service provider with the given identity provider.
   *
   * @param spEntity The entity that this is a role for.
   * @param idpEntity The identity provider entity.
   * @param serviceUrl The URL for the provided service.
   * @param acsUrl The URL for the assertion-consumer endpoint.
   * @param clientTransport A message-transport object.
   * @throws ServletException
   */
  public MockServiceProvider(EntityDescriptor spEntity, EntityDescriptor idpEntity,
      String serviceUrl, String acsUrl, GettableHttpServlet clientTransport) throws ServletException {
    init(new MockServletConfig());
    this.spEntity = spEntity;
    this.idpEntity = idpEntity;
    this.serviceUrl = serviceUrl;
    this.acsUrl = acsUrl;
    this.clientTransport = clientTransport;
    makeSpSsoDescriptor(spEntity);
    makeAssertionConsumerService(spEntity.getSPSSODescriptor(SAML20P_NS),
                                 SAML2_ARTIFACT_BINDING_URI, acsUrl)
        .setIsDefault(true);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String url = req.getRequestURL().toString();
    if (url.startsWith(serviceUrl)) {
      provideService(req, resp, url);
    } else if (url.startsWith(acsUrl)) {
      consumeAssertion(req, resp);
    } else {
      errorServletResponse(resp, SC_NOT_FOUND);
    }
  }

  private void provideService(HttpServletRequest req, HttpServletResponse resp, String relayState)
      throws ServletException, IOException {
    Object isAuthenticated = req.getSession().getAttribute("isAuthenticated");
    logger.log(Level.FINE, "isAuthenticate = " + isAuthenticated);
    if (isAuthenticated == Boolean.TRUE) {
      ifAllowed(resp);
    } else if (isAuthenticated == Boolean.FALSE) {
      errorServletResponse(resp, SC_UNAUTHORIZED);
    } else {
      ifUnknown(resp, relayState);
    }
  }

  private void ifAllowed(HttpServletResponse resp) throws IOException {
    logger.entering(className, "ifAllowed");
    PrintWriter out = htmlServletResponse(resp);
    out.print("<html><head><title>What you need</title></head>" +
              "<body><h1>What you need...</h1><p>...is what we've got!</p></body></html>");
    out.close();
    logger.exiting(className, "ifAllowed");
  }

  private void ifUnknown(HttpServletResponse resp, String relayState) throws ServletException {
    logger.entering(className, "ifUnknown");
    SAMLMessageContext<SAMLObject, AuthnRequest, NameID> context = makeSamlMessageContext();
    {
      AuthnRequest authnRequest = makeAuthnRequest();
      authnRequest.setProviderName(GOOGLE_PROVIDER_NAME);
      authnRequest.setIssuer(makeIssuer(ISSUER));
      authnRequest.setIsPassive(false);
      authnRequest.setAssertionConsumerServiceIndex(
          spEntity.getSPSSODescriptor(SAML20P_NS) .getDefaultAssertionConsumerService().getIndex());
      context.setOutboundSAMLMessage(authnRequest);
    }
    context.setRelayState(relayState);

    initializeLocalEntity(context, spEntity, spEntity.getSPSSODescriptor(SAML20P_NS),
                          Endpoint.DEFAULT_ELEMENT_NAME);
    initializePeerEntity(context, idpEntity, idpEntity.getIDPSSODescriptor(SAML20P_NS),
                         SingleSignOnService.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_REDIRECT_BINDING_URI);

    initializeServletResponse(resp);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(resp, true));

    runEncoder(new HTTPRedirectDeflateEncoder(), context);
    logger.exiting(className, "ifUnknown");
  }

  private void consumeAssertion(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    initializeServletResponse(resp);
    HttpServletResponseAdapter result = new HttpServletResponseAdapter(resp, true);
    HttpSession session = req.getSession();
    String artifact = req.getParameter(GSA_ARTIFACT_PARAM_NAME);
    String relayState = req.getParameter(GSA_RELAY_STATE_PARAM_NAME);
    if (artifact == null) {
      logger.log(Level.WARNING, "No artifact in message.");
      errorServletResponse(resp, SC_INTERNAL_SERVER_ERROR);
      return;
    }
    SAMLObject message = resolveArtifact(artifact, relayState);
    if (! (message instanceof Response)) {
      logger.log(Level.WARNING, "Error from artifact resolver.");
      errorServletResponse(resp, SC_INTERNAL_SERVER_ERROR);
      return;
    }
    Response response = (Response) message;
    String code = response.getStatus().getStatusCode().getValue();
    if (code.equals(StatusCode.SUCCESS_URI)) {
      Assertion assertion = response.getAssertions().get(0);
      session.setAttribute("isAuthenticated", true);
      session.setAttribute("verifiedIdentity", assertion.getSubject().getNameID().getValue());
      session.setAttribute("verificationStatement",
                           assertion.getStatements(AuthnStatement.DEFAULT_ELEMENT_NAME).get(0));
      result.sendRedirect(relayState);
      return;
    }
    if (code.equals(StatusCode.REQUEST_DENIED_URI)) {
      session.setAttribute("isAuthenticated", false);
      result.sendRedirect(relayState);
      return;
    }
    if (code.equals(StatusCode.AUTHN_FAILED_URI)) {
      // Do nothing.  The service provider will restart the authentication.
      result.sendRedirect(relayState);
      return;
    }
    logger.log(Level.WARNING, "Unknown <Response> status: " + code);
    errorServletResponse(resp, SC_INTERNAL_SERVER_ERROR);
  }

  private SAMLObject resolveArtifact(String artifact, String relayState)
      throws ServletException, IOException {
    SAMLMessageContext<ArtifactResponse, ArtifactResolve, NameID> context =
        makeSamlMessageContext();

    // Select endpoint
    initializeLocalEntity(context, spEntity, spEntity.getSPSSODescriptor(SAML20P_NS),
                          Endpoint.DEFAULT_ELEMENT_NAME);
    initializePeerEntity(context, idpEntity, idpEntity.getIDPSSODescriptor(SAML20P_NS),
                         ArtifactResolutionService.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_SOAP11_BINDING_URI);

    MockHttpServletRequest req =
        makeMockHttpPost(acsUrl, context.getPeerEntityEndpoint().getLocation());
    MockHttpServletResponse resp = new MockHttpServletResponse();

    HttpServletRequestClientAdapter out = new HttpServletRequestClientAdapter(req);
    context.setOutboundMessageTransport(out);
    {
      ArtifactResolve request = makeArtifactResolve(artifact);
      request.setIssuer(makeIssuer(ISSUER));
      context.setOutboundSAMLMessage(request);
    }
    context.setRelayState(relayState);
    runEncoder(new HTTPSOAP11Encoder(), context);
    out.finish();

    clientTransport.doGet(req, resp);

    HttpServletResponseClientAdapter in = new HttpServletResponseClientAdapter(resp);
    in.setHttpMethod("POST");
    context.setInboundMessageTransport(in);
    runDecoder(new HTTPSOAP11Decoder(), context);

    return context.getInboundSAMLMessage().getMessage();
  }
}
