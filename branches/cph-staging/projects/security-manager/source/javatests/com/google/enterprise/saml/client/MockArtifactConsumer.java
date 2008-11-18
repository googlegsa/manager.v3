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
import com.google.enterprise.saml.common.HttpTransport;
import com.google.enterprise.saml.common.SecurityManagerServlet;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.saml.common.GsaConstants.GSA_ARTIFACT_PARAM_NAME;
import static com.google.enterprise.saml.common.GsaConstants.GSA_RELAY_STATE_PARAM_NAME;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeArtifactResolve;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.selectPeerEndpoint;
import static com.google.enterprise.saml.common.SamlTestUtil.makeMockHttpPost;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

/**
 * The MockArtifactConsumer class implements a servlet pretending to be the part of a SAML Service
 * Provider that receives a service request from the user agent and initiates an authn request from
 * an identity provider.
 */
public class MockArtifactConsumer extends SecurityManagerServlet implements GettableHttpServlet {
  private static final Logger LOGGER = Logger.getLogger(MockArtifactConsumer.class.getName());
  private static final long serialVersionUID = 1L;

  private final EntityDescriptor localEntity;
  private final EntityDescriptor peerEntity;
  private final HttpTransport transport;

  /**
   * Creates a new mock SAML service provider with the given identity provider.
   *
   * @param localEntity The entity that this is a role for.
   * @param peerEntity The identity provider entity.
   * @param transport A message-transport provider.
   * @throws ServletException
   */
  public MockArtifactConsumer(EntityDescriptor localEntity, EntityDescriptor peerEntity,
      HttpTransport transport)
      throws ServletException {
    this.localEntity = localEntity;
    this.peerEntity = peerEntity;
    this.transport = transport;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpServletResponseAdapter result = new HttpServletResponseAdapter(resp, true);
    HttpSession session = req.getSession();
    String artifact = req.getParameter(GSA_ARTIFACT_PARAM_NAME);
    String relayState = req.getParameter(GSA_RELAY_STATE_PARAM_NAME);
    if (artifact == null) {
      LOGGER.log(Level.WARNING, "No artifact in message.");
      initErrorResponse(resp, SC_INTERNAL_SERVER_ERROR);
      return;
    }
    SAMLObject message = resolveArtifact(artifact, relayState, req.getRequestURL().toString());
    if (! (message instanceof Response)) {
      LOGGER.log(Level.WARNING, "Error from artifact resolver.");
      initErrorResponse(resp, SC_INTERNAL_SERVER_ERROR);
      return;
    }
    initResponse(resp);
    Response response = (Response) message;
    String code = response.getStatus().getStatusCode().getValue();
    if (code.equals(StatusCode.SUCCESS_URI)) {
      Assertion assertion = response.getAssertions().get(0);
      session.setAttribute("isAuthenticated", true);
      session.setAttribute("verifiedIdentity", assertion.getSubject().getNameID().getValue());
      session.setAttribute("verificationStatement",
                           assertion.getStatements(AuthnStatement.DEFAULT_ELEMENT_NAME).get(0));
    } else if (code.equals(StatusCode.REQUEST_DENIED_URI)) {
      session.setAttribute("isAuthenticated", false);
    } else {
      // Do nothing.  The service provider will restart the authentication.
    }
    result.sendRedirect(relayState);
  }

  private SAMLObject resolveArtifact(String artifact, String relayState, String clientUrl)
      throws ServletException, IOException {
    SAMLMessageContext<ArtifactResponse, ArtifactResolve, NameID> context =
        makeSamlMessageContext();

    // Select endpoint
    initializeLocalEntity(context, localEntity, localEntity.getSPSSODescriptor(SAML20P_NS),
                          Endpoint.DEFAULT_ELEMENT_NAME);
    initializePeerEntity(context, peerEntity, peerEntity.getIDPSSODescriptor(SAML20P_NS),
                         ArtifactResolutionService.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_SOAP11_BINDING_URI);

    MockHttpServletRequest req =
        makeMockHttpPost(clientUrl, context.getPeerEntityEndpoint().getLocation());
    MockHttpServletResponse resp = new MockHttpServletResponse();

    HttpServletRequestClientAdapter out = new HttpServletRequestClientAdapter(req);
    context.setOutboundMessageTransport(out);
    {
      ArtifactResolve request = makeArtifactResolve(artifact);
      request.setIssuer(makeIssuer(localEntity.getEntityID()));
      context.setOutboundSAMLMessage(request);
    }
    context.setRelayState(relayState);
    runEncoder(new HTTPSOAP11Encoder(), context);
    out.finish();

    transport.exchange(req, resp);

    HttpServletResponseClientAdapter in = new HttpServletResponseClientAdapter(resp);
    in.setHttpMethod("POST");
    context.setInboundMessageTransport(in);
    runDecoder(new HTTPSOAP11Decoder(), context);

    return context.getInboundSAMLMessage().getMessage();
  }
}
