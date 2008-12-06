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

import com.google.enterprise.saml.common.Metadata;
import com.google.enterprise.saml.common.PostableHttpServlet;
import com.google.enterprise.saml.common.SecurityManagerServlet;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.binding.encoding.HTTPSOAP11Encoder;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeArtifactResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.selectPeerEndpoint;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

/**
 * Servlet to handle SAML artifact-resolution requests.  This is one part of the security manager's
 * identity provider, and will handle only those artifacts generated by the identity provider.
 */
public class SamlArtifactResolve extends SecurityManagerServlet implements PostableHttpServlet {

  /** Required for serializable classes. */
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(SamlArtifactResolve.class.getName());

  public SamlArtifactResolve() {
    super(Metadata.SM_KEY);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    BackEnd backend = getBackEnd(getServletContext());

    // Establish the SAML message context
    SAMLMessageContext<ArtifactResolve, ArtifactResponse, NameID> context =
        makeSamlMessageContext();

    EntityDescriptor localEntity = getLocalEntity();
    initializeLocalEntity(context, localEntity, localEntity.getIDPSSODescriptor(SAML20P_NS),
                          ArtifactResolutionService.DEFAULT_ELEMENT_NAME);
    context.setOutboundMessageIssuer(localEntity.getEntityID());

    // TODO(cph): need way to select the correct peer entity.
    EntityDescriptor peerEntity = getPeerEntity();
    initializePeerEntity(context, peerEntity, peerEntity.getSPSSODescriptor(SAML20P_NS),
                         Endpoint.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_SOAP11_BINDING_URI);
    context.setInboundMessageIssuer(peerEntity.getEntityID());

    // Decode the request
    context.setInboundMessageTransport(new HttpServletRequestAdapter(req));
    runDecoder(new HTTPSOAP11Decoder(), context);
    ArtifactResolve artifactResolve = context.getInboundSAMLMessage();

    // Create response
    ArtifactResponse artifactResponse =
        makeArtifactResponse(artifactResolve, makeStatus(StatusCode.SUCCESS_URI));
    artifactResponse.setIssuer(makeIssuer(localEntity.getEntityID()));

    // Look up artifact and add any resulting object to response
    String encodedArtifact = artifactResolve.getArtifact().getArtifact();
    SAMLArtifactMap artifactMap = backend.getArtifactMap();
    if (artifactMap.contains(encodedArtifact)) {
      LOGGER.info("local Entity ID: " + localEntity.getEntityID());
      LOGGER.info("peer entity ID: " + peerEntity.getEntityID());
      SAMLArtifactMapEntry entry = artifactMap.get(encodedArtifact);
      LOGGER.info("artifact issuer: " + entry.getIssuerId());
      LOGGER.info("artifact relying party: " + entry.getRelyingPartyId());
      if ((!entry.isExpired())
          && localEntity.getEntityID().equals(entry.getIssuerId())
          && peerEntity.getEntityID().equals(entry.getRelyingPartyId())) {
        artifactResponse.setMessage(entry.getSamlMessage());
        LOGGER.info("Artifact resolved");
      }
      // Always remove the artifact after use
      artifactMap.remove(encodedArtifact);
    }

    // Encode response
    context.setOutboundSAMLMessage(artifactResponse);
    initResponse(resp);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(resp, true));
    runEncoder(new HTTPSOAP11Encoder(), context);
  }
}
