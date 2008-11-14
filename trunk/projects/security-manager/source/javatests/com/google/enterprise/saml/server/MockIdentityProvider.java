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

package com.google.enterprise.saml.server;

import com.google.enterprise.saml.common.GettableHttpServlet;
import com.google.enterprise.saml.common.PostableHttpServlet;
import com.google.enterprise.saml.common.SecurityManagerServlet;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.binding.encoding.HTTPArtifactEncoder;
import org.opensaml.saml2.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml2.core.Artifact;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.parse.BasicParserPool;
import org.springframework.mock.web.MockServletConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeArtifactResolutionService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeArtifactResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertion;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAuthnStatement;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIdpSsoDescriptor;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSingleSignOnService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatusMessage;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSubject;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.selectPeerEndpoint;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

public class MockIdentityProvider extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {
  private static final String className = MockIdentityProvider.class.getName();
  private static final Logger logger = Logger.getLogger(className);
  private static final long serialVersionUID = 1L;
  private static final String ISSUER = "http://google.com/mock-identity-provider";
  private static final String SSO_SAML_CONTEXT = "ssoSamlContext";
  private static final int artifactLifetime = 600000;  // ten minutes

  private final EntityDescriptor idpEntity;
  private final EntityDescriptor spEntity;
  private final IDPSSODescriptor idp;
  private final String formPostUrl;
  private final String arUrl;
  private final SAMLArtifactMap artifactMap;
  private final SingleSignOnService ssoService;

  public MockIdentityProvider(EntityDescriptor idpEntity, EntityDescriptor spEntity, String ssoUrl,
      String formPostUrl, String arUrl) throws ServletException {
    init(new MockServletConfig());
    this.idpEntity = idpEntity;
    this.spEntity = spEntity;
    this.formPostUrl = formPostUrl;
    this.arUrl = arUrl;
    artifactMap = new BasicSAMLArtifactMap(
        new BasicParserPool(),
        new MapBasedStorageService<String, SAMLArtifactMapEntry>(),
        artifactLifetime);
    idp = makeIdpSsoDescriptor(idpEntity);
    ssoService = makeSingleSignOnService(idp, SAML2_REDIRECT_BINDING_URI, ssoUrl);
    makeArtifactResolutionService(idp, SAML2_SOAP11_BINDING_URI, arUrl).setIsDefault(true);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String url = req.getRequestURL().toString();
    if (url.startsWith(ssoService.getLocation())) {
      requestCredentials(req, resp);
    } else {
      logger.log(Level.WARNING, "GET unknown URL: " + url);
      initErrorResponse(resp, SC_NOT_FOUND);
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String url = req.getRequestURL().toString();
    if (formPostUrl.equals(url)) {
      validateCredentials(req, resp);
    } else if (arUrl.equals(url)) {
      resolveArtifact(req, resp);
    } else {
      logger.log(Level.WARNING, "POST unknown URL: " + url);
      initErrorResponse(resp, SC_NOT_FOUND);
    }
  }

  private void requestCredentials(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    SAMLMessageContext<AuthnRequest, Response, NameID> context = makeSamlMessageContext();
    context.setInboundMessageTransport(new HttpServletRequestAdapter(req));
    HTTPRedirectDeflateDecoder decoder = new HTTPRedirectDeflateDecoder();
    runDecoder(decoder, context);
    req.getSession().setAttribute(SSO_SAML_CONTEXT, context);
    PrintWriter out = initNormalResponse(resp);
    out.print("<html><head><title>Please Login</title></head><body>\n" +
              "<form action=\"" +
              formPostUrl +
              "\" method=POST>\n" +
              "User Name:<input type=text name=username><br>\n" +
              "Password:<input type=password name=password><br>\n" +
              "<input type=submit>\n" +
              "</form>\n" +
              "</body></html>\n");
    out.close();
  }

  private void validateCredentials(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession();
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        getMessageContext(session, SSO_SAML_CONTEXT);
    if (context == null) {
      logger.log(Level.WARNING, "Unable to get identity provider message context.");
      initErrorResponse(resp, SC_INTERNAL_SERVER_ERROR);
      return;
    }

    Status status = makeStatus();
    context.setOutboundSAMLMessage(makeResponse(context.getInboundSAMLMessage(), status));
    
    String username = req.getParameter("username");
    String password = req.getParameter("password");
    if ((username == null) || (password == null)) {
      logger.log(Level.WARNING, "Missing required POST parameter(s)");
      status.getStatusCode().setValue(StatusCode.REQUESTER_URI);
      status.setStatusMessage(makeStatusMessage("Missing required POST parameter(s)"));
    } else if (areCredentialsValid(username, password)) {
      logger.log(Level.INFO, "Authenticated successfully as " + username);
      status.getStatusCode().setValue(StatusCode.SUCCESS_URI);
      Assertion assertion = makeAssertion(makeIssuer(ISSUER), makeSubject(username));
      assertion.getAuthnStatements().add(makeAuthnStatement(AuthnContext.IP_PASSWORD_AUTHN_CTX));
      context.getOutboundSAMLMessage().getAssertions().add(assertion);
    } else {
      logger.log(Level.INFO, "Authentication failed");
      status.getStatusCode().setValue(StatusCode.REQUEST_DENIED_URI);
      status.setStatusMessage(makeStatusMessage("Authentication failed"));
    }

    encodeArtifactResponse(context, resp);
  }

  private void encodeArtifactResponse(SAMLMessageContext<AuthnRequest, Response, NameID> context,
      HttpServletResponse resp) throws ServletException {

    // Select endpoint
    initializeLocalEntity(context, idpEntity, idpEntity.getIDPSSODescriptor(SAML20P_NS),
                          SingleSignOnService.DEFAULT_ELEMENT_NAME);
    initializePeerEntity(context, spEntity, spEntity.getSPSSODescriptor(SAML20P_NS),
                         AssertionConsumerService.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_ARTIFACT_BINDING_URI);

    // Do encoding
    initResponse(resp);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(resp, true));
    HTTPArtifactEncoder encoder = new HTTPArtifactEncoder(null, null, artifactMap);
    encoder.setPostEncoding(false);
    runEncoder(encoder, context);
  }

  private boolean areCredentialsValid(String username, String password) {
    return "joe".equals(username) && "plumber".equals(password);
  }

  @SuppressWarnings("unchecked")
  private <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject>
      SAMLMessageContext<TI, TO, TN> getMessageContext(HttpSession session, String name) {
    return (SAMLMessageContext<TI, TO, TN>) session.getAttribute(name);
  }

  private void resolveArtifact(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException {
    SAMLMessageContext<ArtifactResolve, ArtifactResponse, NameID> context =
        makeSamlMessageContext();
    context.setInboundMessageTransport(new HttpServletRequestAdapter(req));
    initResponse(resp);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(resp, true));

    // Select endpoint
    initializeLocalEntity(context, idpEntity, idpEntity.getIDPSSODescriptor(SAML20P_NS),
                          ArtifactResolutionService.DEFAULT_ELEMENT_NAME);
    initializePeerEntity(context, spEntity, spEntity.getSPSSODescriptor(SAML20P_NS),
                         Endpoint.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_SOAP11_BINDING_URI);

    // Decode request
    runDecoder(new HTTPSOAP11Decoder(), context);
    ArtifactResolve request = context.getInboundSAMLMessage();

    // Create response
    ArtifactResponse response = makeArtifactResponse(request, makeStatus(StatusCode.SUCCESS_URI));
    response.setIssuer(makeIssuer(ISSUER));

    // Look up artifact and add resulting object to response
    Artifact artifact = request.getArtifact();
    String encodedArtifact = artifact.getArtifact();
    SAMLArtifactMapEntry entry = artifactMap.get(encodedArtifact);
    if (entry != null) {
      response.setMessage(entry.getSamlMessage());
    }

    // Encode response
    context.setOutboundSAMLMessage(response);
    runEncoder(new HTTPSOAP11Encoder(), context);
  }
}
