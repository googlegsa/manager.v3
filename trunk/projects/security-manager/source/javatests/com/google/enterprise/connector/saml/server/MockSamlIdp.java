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

package com.google.enterprise.connector.saml.server;

import com.google.enterprise.connector.common.GettableHttpServlet;
import com.google.enterprise.connector.servlet.SecurityManagerServlet;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.parse.BasicParserPool;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runDecoder;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI;

public class MockSamlIdp extends SecurityManagerServlet
    implements GettableHttpServlet {

  private static final int artifactLifetime = 600000;  // ten minutes
  private static final SAMLArtifactMap artifactMap =
      new BasicSAMLArtifactMap(
          new BasicParserPool(),
          new MapBasedStorageService<String, SAMLArtifactMapEntry>(),
          artifactLifetime);

  private final String localEntityId;
  private final String responseId;
  private final boolean usePost;

  public MockSamlIdp(String localEntityId, String responseId, boolean usePost) {
    this.localEntityId = localEntityId;
    this.responseId = responseId;
    this.usePost = usePost;
  }

  // For MockSamlArtifactResolver.
  static SAMLArtifactMap getArtifactMap() {
    return artifactMap;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // Establish the SAML message context.
    SAMLMessageContext<AuthnRequest, Response, NameID> context = makeSamlMessageContext();
    {
      EntityDescriptor localEntity = getEntity(localEntityId);
      initializeLocalEntity(context, localEntity, localEntity.getIDPSSODescriptor(SAML20P_NS),
                            SingleSignOnService.DEFAULT_ELEMENT_NAME);
    }

    // Decode the request.
    context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
    runDecoder(new HTTPRedirectDeflateDecoder(), context);

    // Select entity for response.
    {
      EntityDescriptor peerEntity = getEntity(context.getInboundMessageIssuer());
      initializePeerEntity(context, peerEntity, peerEntity.getSPSSODescriptor(SAML20P_NS),
                           AssertionConsumerService.DEFAULT_ELEMENT_NAME,
                           usePost ? SAML2_POST_BINDING_URI : SAML2_ARTIFACT_BINDING_URI);
    }

    if (responseId != null) {
      SamlAuthn.makeSuccessfulSamlSsoResponse(
          response, context, usePost ? null : artifactMap, responseId, null);
    } else {
      SamlAuthn.makeUnsuccessfulSamlSsoResponse(
          response, context, usePost ? null : artifactMap, "Unable to authenticate");
    }
  }
}
