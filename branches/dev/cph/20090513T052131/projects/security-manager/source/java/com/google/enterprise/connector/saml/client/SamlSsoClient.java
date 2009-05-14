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

import com.google.enterprise.connector.common.ServletBase;
import com.google.enterprise.connector.spi.SecAuthnIdentity;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAuthnRequest;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runEncoder;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;

public class SamlSsoClient {

  private static final String CLIENT_ID_NAME = "SamlSsoClient.clientId";

  // Don't instantiate -- static methods only.
  private SamlSsoClient() {
  }

  public static void startSamlRequest(HttpServletRequest request, HttpServletResponse response,
                                      SecAuthnIdentity id)
      throws IOException {
    request.getSession().setAttribute(CLIENT_ID_NAME, id);

    SAMLMessageContext<SAMLObject, AuthnRequest, NameID> context = makeSamlMessageContext();

    EntityDescriptor localEntity = ServletBase.getSmEntity();
    SPSSODescriptor sp = localEntity.getSPSSODescriptor(SAML20P_NS);
    initializeLocalEntity(context, localEntity, sp, Endpoint.DEFAULT_ELEMENT_NAME);
    {
      EntityDescriptor peerEntity = ServletBase.getEntity(id.getAuthority());
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
    ServletBase.initResponse(response);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
    runEncoder(new HTTPRedirectDeflateEncoder(), context);
  }

  static SecAuthnIdentity getSamlClientId(HttpSession session) {
    return SecAuthnIdentity.class.cast(session.getAttribute(CLIENT_ID_NAME));
  }

  public static void eraseSamlClientState(HttpSession session) {
    session.removeAttribute(CLIENT_ID_NAME);
  }
}
