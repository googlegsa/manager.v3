// Copyright (C) 2009 Google Inc.
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

package com.google.enterprise.connector.saml.client;

import com.google.enterprise.connector.common.GettableHttpServlet;
import com.google.enterprise.connector.common.HttpExchange;
import com.google.enterprise.connector.common.PostableHttpServlet;
import com.google.enterprise.connector.common.SecurityManagerUtil;
import com.google.enterprise.connector.saml.common.HttpExchangeToInTransport;
import com.google.enterprise.connector.saml.common.HttpExchangeToOutTransport;
import com.google.enterprise.connector.security.identity.DomainCredentials;
import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.connector.spi.VerificationStatus;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeArtifactResolve;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAuthnRequest;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runEncoder;
import com.google.enterprise.connector.servlet.SecurityManagerServlet;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

public class SamlSsoClient extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {
  private static final Logger LOGGER = Logger.getLogger(SamlSsoClient.class.getName());
  private static final String CLIENT_ID_NAME = "SamlSsoClient.clientId";

  public static void startSamlRequest(HttpServletRequest request, HttpServletResponse response,
                                      SecAuthnIdentity id)
      throws IOException {
    request.getSession().setAttribute(CLIENT_ID_NAME, id);

    SAMLMessageContext<SAMLObject, AuthnRequest, NameID> context = makeSamlMessageContext();

    EntityDescriptor localEntity = SecurityManagerServlet.getSmEntity();
    SPSSODescriptor sp = localEntity.getSPSSODescriptor(SAML20P_NS);
    initializeLocalEntity(context, localEntity, sp, Endpoint.DEFAULT_ELEMENT_NAME);
    {
      EntityDescriptor peerEntity = SecurityManagerServlet.getEntity(id.getAuthority());
      initializePeerEntity(context, peerEntity, peerEntity.getIDPSSODescriptor(SAML20P_NS),
          SingleSignOnService.DEFAULT_ELEMENT_NAME,
          SAML2_REDIRECT_BINDING_URI);
    }

    // Generate the request
    AuthnRequest authnRequest = makeAuthnRequest();
    authnRequest.setProviderName("Google Security Manager");
    authnRequest.setIssuer(makeIssuer(context.getOutboundMessageIssuer()));
    authnRequest.setIsPassive(false);
    authnRequest.setAssertionConsumerServiceIndex(
        sp.getDefaultAssertionConsumerService().getIndex());
    context.setOutboundSAMLMessage(authnRequest);
    //context.setRelayState();

    // Send the request via redirect to the user agent
    SecurityManagerServlet.initResponse(response);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
    runEncoder(new HTTPRedirectDeflateEncoder(), context);
  }

  // GET implies the assertion is being sent with the artifact binding.
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    LOGGER.info("Received assertion via artifact binding");
    consumeAssertion(request, response, decodeArtifactResponse(request));
  }

  // POST implies the assertion is being sent with the POST binding.
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    LOGGER.info("Received assertion via POST binding");
    consumeAssertion(request, response, decodePostResponse(request));
  }

  private void consumeAssertion(
      HttpServletRequest request, HttpServletResponse response, Response samlResponse)
      throws IOException {
    String code = samlResponse.getStatus().getStatusCode().getValue();
    LOGGER.info("status code = " + code);
    SecAuthnIdentity id = getSamlClientId(request.getSession());
    if (code.equals(StatusCode.SUCCESS_URI)) {
      extractResponseInfo(samlResponse, id);
      id.setVerificationStatus(VerificationStatus.VERIFIED);
    } else if (code.equals(StatusCode.AUTHN_FAILED_URI)) {
      id.setVerificationStatus(VerificationStatus.REFUTED);
    } else {
      LOGGER.warning("SAML IdP failed to resolve: " + code);
      id.setVerificationStatus(VerificationStatus.INDETERMINATE);
    }

    getBackEnd().authenticate(request, response);
  }

  private void extractResponseInfo(Response samlResponse, SecAuthnIdentity id) {
    List<Assertion> assertions = samlResponse.getAssertions();
    if (assertions.size() <= 0) {
      return;
    }
    Assertion assertion = assertions.get(0);
    id.setUsername(assertion.getSubject().getNameID().getValue());
    LOGGER.info("Credential verified: " + DomainCredentials.class.cast(id).dumpInfo());
  }

  // The OpenSAML HTTPArtifactDecoder isn't implemented, so we must manually decode the
  // artifact.
  private Response decodeArtifactResponse(HttpServletRequest request)
      throws IOException {
    String artifact = request.getParameter("SAMLart");
    if (artifact == null) {
      throw new IOException("No artifact in message");
    }

    SAMLObject message = resolveArtifact(request, artifact, request.getParameter("RelayState"));
    if (! (message instanceof Response)) {
      throw new IOException("Unable to resolve artifact");
    }
    return (Response) message;
  }

  private SAMLObject resolveArtifact(HttpServletRequest request, String artifact, String relayState)
      throws IOException {
    // Establish the SAML message context.
    SAMLMessageContext<ArtifactResponse, ArtifactResolve, NameID> context =
        makeSamlMessageContext();

    EntityDescriptor localEntity = getSmEntity();
    initializeLocalEntity(context, localEntity, localEntity.getSPSSODescriptor(SAML20P_NS),
                          Endpoint.DEFAULT_ELEMENT_NAME);
    {
      EntityDescriptor peerEntity = getEntity(getSamlClientId(request.getSession()).getAuthority());
      initializePeerEntity(context, peerEntity, peerEntity.getIDPSSODescriptor(SAML20P_NS),
                           ArtifactResolutionService.DEFAULT_ELEMENT_NAME,
                           SAML2_SOAP11_BINDING_URI);
    }

    // Generate the request.
    {
      ArtifactResolve artifactResolve = makeArtifactResolve(artifact);
      artifactResolve.setIssuer(makeIssuer(localEntity.getEntityID()));
      context.setOutboundSAMLMessage(artifactResolve);
    }

    // Encode the request.
    HttpExchange exchange =
        SecurityManagerUtil.getHttpClient()
        .postExchange(new URL(context.getPeerEntityEndpoint().getLocation()), null);
    HttpExchangeToOutTransport out = new HttpExchangeToOutTransport(exchange);
    context.setOutboundMessageTransport(out);
    context.setRelayState(relayState);
    runEncoder(new HTTPSOAP11Encoder(), context);
    out.finish();

    // Do HTTP exchange.
    int status = exchange.exchange();
    if (status != 200) {
      throw new IOException("Incorrect HTTP status: " + status);
    }

    // Decode the response.
    context.setInboundMessageTransport(new HttpExchangeToInTransport(exchange));
    runDecoder(new HTTPSOAP11Decoder(), context);

    // Return the decoded response.
    return context.getInboundSAMLMessage().getMessage();
  }

  private Response decodePostResponse(HttpServletRequest request)
      throws IOException {
    SAMLMessageContext<Response, SAMLObject, NameID> context =
        makeSamlMessageContext();

    EntityDescriptor localEntity = getSmEntity();
    initializeLocalEntity(context, localEntity, localEntity.getSPSSODescriptor(SAML20P_NS),
                          AssertionConsumerService.DEFAULT_ELEMENT_NAME);
    {
      EntityDescriptor peerEntity = getEntity(getSamlClientId(request.getSession()).getAuthority());
      initializePeerEntity(context, peerEntity, peerEntity.getIDPSSODescriptor(SAML20P_NS),
          SingleSignOnService.DEFAULT_ELEMENT_NAME,
          SAML2_REDIRECT_BINDING_URI);
    }

    context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
    HTTPPostDecoder decoder = new HTTPPostDecoder();
    runDecoder(decoder, context);

    Response samlResponse = context.getInboundSAMLMessage();
    if (samlResponse == null) {
      throw new IOException("Decoded SAML response is null");
    }
    return samlResponse;
  }

  private static SecAuthnIdentity getSamlClientId(HttpSession session) {
    return SecAuthnIdentity.class.cast(session.getAttribute(CLIENT_ID_NAME));
  }

  // Public for use by BackEnd.
  public static void eraseSamlClientState(HttpSession session) {
    session.removeAttribute(CLIENT_ID_NAME);
  }
}
