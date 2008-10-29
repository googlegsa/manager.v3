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

import com.google.enterprise.saml.common.HttpServletRequestClientAdapter;
import com.google.enterprise.saml.common.HttpServletResponseClientAdapter;
import com.google.enterprise.saml.common.OpenSamlUtil;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.security.SecurityException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The MockArtifactConsumer class implements a servlet pretending to be the part of a SAML Service
 * Provider that receives an artifact from the user agent and resolves it using the identity
 * provider.
 */
public class MockArtifactConsumer extends HttpServlet {
  // private static final Logger LOGGER = Logger.getLogger(MockArtifactConsumer.class.getName());
  private static final long serialVersionUID = 1L;
  private final Endpoint endpoint;
  private final MockArtifactResolver resolver;

  public MockArtifactConsumer(String idpUrl, MockArtifactResolver resolver) {
    this.endpoint =
        OpenSamlUtil.makeArtifactResolutionService(SAMLConstants.SAML2_SOAP11_BINDING_URI, idpUrl);
    this.resolver = resolver;
  }

  // This method is normally declared "protected", but the testing harness will need to be able to
  // call it from the mock user agent, so we declare it as "public".
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession();
    String artifact = req.getParameter("SAMLart");
    String relayState = req.getParameter("RelayState");
    if (artifact == null) {
      throw new ServletException("No artifact in request.");
    }
    ArtifactResponse artifactResponse =
        receiveArtifactResponse(
            resolver.resolve(sendArtifactResolve(OpenSamlUtil.makeArtifactResolve(artifact),
                                                 relayState)));
    if (!(artifactResponse.getMessage() instanceof Response)) {
      throw new ServletException();
    }
    HttpServletResponseAdapter result = new HttpServletResponseAdapter(resp, true);
    Response response = (Response) artifactResponse.getMessage();
    String code = response.getStatus().getStatusCode().getValue();
    if (StatusCode.SUCCESS_URI.equals(code)) {
      Assertion assertion = response.getAssertions().get(0);
      session.setAttribute("isAuthenticated", true);
      session.setAttribute("verifiedIdentity", assertion.getSubject().getNameID().getValue());
      session.setAttribute("verificationStatement", assertion.getStatements(AuthnStatement.DEFAULT_ELEMENT_NAME).get(0));
    } else if (StatusCode.REQUEST_DENIED_URI.equals(code)) {
      session.setAttribute("isAuthenticated", false);
    } else if (StatusCode.AUTHN_FAILED_URI.equals(code)) {
      // Do nothing.  The service provider will restart the authentication.
    } else {
      throw new ServletException("Bad <Response>:" + code);
    }
    result.sendRedirect(relayState);
  }

  private MockHttpServletRequest sendArtifactResolve(ArtifactResolve request, String relayState)
      throws ServletException {
    SAMLMessageContext<SAMLObject, ArtifactResolve, NameID> context =
        OpenSamlUtil.makeSamlMessageContext();
    HttpServletRequestClientAdapter transport = new HttpServletRequestClientAdapter();
    context.setOutboundMessageTransport(transport);
    context.setOutboundSAMLMessage(request);
    context.setRelayState(relayState);
    context.setPeerEntityEndpoint(endpoint);

    HTTPSOAP11Encoder encoder = new HTTPSOAP11Encoder();
    try {
      encoder.encode(context);
    } catch (MessageEncodingException e) {
      throw new ServletException(e);
    }
    return transport.getRequest();
  }

  private ArtifactResponse receiveArtifactResponse(MockHttpServletResponse response)
      throws ServletException {
    SAMLMessageContext<ArtifactResponse, SAMLObject, NameID> context =
        OpenSamlUtil.makeSamlMessageContext();
    HttpServletResponseClientAdapter transport = new HttpServletResponseClientAdapter(response);
    context.setInboundMessageTransport(transport);
    context.setPeerEntityEndpoint(endpoint);
    HTTPSOAP11Decoder decoder = new HTTPSOAP11Decoder();
    try {
      decoder.decode(context);
    } catch (MessageDecodingException e) {
      throw new ServletException(e);
    } catch (SecurityException e) {
      throw new ServletException(e);
    }
    return context.getInboundSAMLMessage();
  }
}
