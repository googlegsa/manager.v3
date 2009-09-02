// Copyright (C) 2008 Google Inc.
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

import static com.google.enterprise.connector.saml.common.OpenSamlUtil.GOOGLE_PROVIDER_NAME;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAuthnRequest;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runEncoder;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;

import com.google.enterprise.connector.common.GettableHttpServlet;
import com.google.enterprise.connector.common.SecurityManagerTestCase;
import com.google.enterprise.connector.common.ServletBase;

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
import javax.servlet.http.HttpSession;

/**
 * The MockServiceProvider class implements a servlet pretending to be the part of a SAML Service
 * Provider that receives a service request from a user agent and initiates an authn request to
 * an identity provider.
 */
public class MockServiceProvider extends ServletBase implements GettableHttpServlet {
  private static final long serialVersionUID = 1L;

  public MockServiceProvider() throws ServletException {
    init(new MockServletConfig());
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    HttpSession session = req.getSession();

    // MockArtifactConsumer sets a flag with the authentication decision.
    // Read that flag and dispatch on its value.
    Boolean isAuthenticated = Boolean.class.cast(session.getAttribute("isAuthenticated"));
    if (isAuthenticated == null) {
      ifUnknown(resp, req.getRequestURL().toString());
    } else if (isAuthenticated) {
      ifAllowed(resp);
    } else {
      initErrorResponse(resp, SC_FORBIDDEN);
    }
  }

  private void ifAllowed(HttpServletResponse resp) throws IOException {
    PrintWriter out = initNormalResponse(resp);
    out.print("<html><head><title>What you need</title></head>" +
              "<body><h1>What you need...</h1><p>...is what we've got!</p></body></html>");
    out.close();
  }

  private void ifUnknown(HttpServletResponse resp, String relayState) throws IOException {
    SAMLMessageContext<SAMLObject, AuthnRequest, NameID> context = makeSamlMessageContext();

    EntityDescriptor localEntity = getEntity(SecurityManagerTestCase.GSA_TESTING_ISSUER);
    SPSSODescriptor sp = localEntity.getSPSSODescriptor(SAML20P_NS);
    initializeLocalEntity(context, localEntity, sp, Endpoint.DEFAULT_ELEMENT_NAME);
    {
      EntityDescriptor peerEntity = getSmEntity();
      initializePeerEntity(context, peerEntity, peerEntity.getIDPSSODescriptor(SAML20P_NS),
          SingleSignOnService.DEFAULT_ELEMENT_NAME,
          SAML2_REDIRECT_BINDING_URI);
    }

    // Generate the request
    {
      AuthnRequest authnRequest = makeAuthnRequest();
      authnRequest.setProviderName(GOOGLE_PROVIDER_NAME);
      authnRequest.setIssuer(makeIssuer(context.getOutboundMessageIssuer()));
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
