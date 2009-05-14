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
import com.google.enterprise.connector.common.ServletBase;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.newSamlMessageContext;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runDecoder;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;

public class MockSamlIdp extends ServletBase
    implements GettableHttpServlet {

  private final String localEntityId;
  private final String responseId;

  public MockSamlIdp(String localEntityId, String responseId) {
    this.localEntityId = localEntityId;
    this.responseId = responseId;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // Establish the SAML message context.
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        newSamlMessageContext(request.getSession());
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
                           SAML2_ARTIFACT_BINDING_URI);
    }

    if (responseId != null) {
      List<String> ids = new ArrayList<String>(1);
      ids.add(responseId);
      SamlAuthn.makeSuccessfulSamlSsoResponse(request, response, ids);
    } else {
      SamlAuthn.makeUnsuccessfulSamlSsoResponse(request, response, "Unable to authenticate");
    }
  }
}
