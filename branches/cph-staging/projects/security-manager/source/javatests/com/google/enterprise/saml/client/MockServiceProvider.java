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
import com.google.enterprise.saml.common.SecurityManagerServlet;
import com.google.enterprise.saml.server.BackEnd;

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
import org.springframework.mock.web.MockServletConfig;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.enterprise.saml.common.OpenSamlUtil.GOOGLE_PROVIDER_NAME;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAuthnRequest;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.selectPeerEndpoint;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;

/**
 * The MockServiceProvider class implements a servlet pretending to be the part of a SAML Service
 * Provider that receives a service request from a user agent and initiates an authn request to
 * an identity provider.
 */
public class MockServiceProvider extends SecurityManagerServlet implements GettableHttpServlet {
  private static final long serialVersionUID = 1L;

  public MockServiceProvider() throws ServletException {
    init(new MockServletConfig());
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Object isAuthenticated = req.getSession().getAttribute("isAuthenticated");
    if (isAuthenticated == Boolean.TRUE) {
      ifAllowed(resp);
    } else if (isAuthenticated == Boolean.FALSE) {
      initErrorResponse(resp, SC_UNAUTHORIZED);
    } else {
      ifUnknown(resp, req.getRequestURL().toString());
    }
  }

  private void ifAllowed(HttpServletResponse resp) throws IOException {
    PrintWriter out = initNormalResponse(resp);
    out.print("<html><head><title>What you need</title></head>" +
              "<body><h1>What you need...</h1><p>...is what we've got!</p></body></html>");
    out.close();
  }

  private void ifUnknown(HttpServletResponse resp, String relayState) throws ServletException {
    BackEnd backend = getBackEnd(getServletContext());

    SAMLMessageContext<SAMLObject, AuthnRequest, NameID> context = makeSamlMessageContext();
    
    EntityDescriptor localEntity = backend.getGsaEntity();
    SPSSODescriptor sp = localEntity.getSPSSODescriptor(SAML20P_NS);
    initializeLocalEntity(context, localEntity, sp, Endpoint.DEFAULT_ELEMENT_NAME);
    context.setOutboundMessageIssuer(localEntity.getEntityID());
    {
      EntityDescriptor peerEntity = backend.getSecurityManagerEntity();
      initializePeerEntity(context, peerEntity, peerEntity.getIDPSSODescriptor(SAML20P_NS),
                           SingleSignOnService.DEFAULT_ELEMENT_NAME);
      selectPeerEndpoint(context, SAML2_REDIRECT_BINDING_URI);
      context.setInboundMessageIssuer(peerEntity.getEntityID());
    }

    // Generate the request
    {
      AuthnRequest authnRequest = makeAuthnRequest();
      authnRequest.setProviderName(GOOGLE_PROVIDER_NAME);
      authnRequest.setIssuer(makeIssuer(localEntity.getEntityID()));
      authnRequest.setIsPassive(false);
      authnRequest.setAssertionConsumerServiceIndex(
          sp.getDefaultAssertionConsumerService().getIndex());
      context.setOutboundSAMLMessage(authnRequest);
    }
    context.setRelayState(relayState);

    // Send the request via redirect to the user agent
    initResponse(resp);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(resp, true));
    runEncoder(new HTTPRedirectDeflateEncoder(), context);
  }
}
