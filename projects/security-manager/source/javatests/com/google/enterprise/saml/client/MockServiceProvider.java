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

import com.google.enterprise.saml.common.OpenSamlUtil;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The MockServiceProvider class implements a servlet that pretends to be an SAML Service
 * Provider (in our case, it pretends to be the GSA SAML AuthN client).  This is part of a test
 * harness for the security manager's SAML front end.
 */
public class MockServiceProvider extends HttpServlet {
  // private static final Logger LOGGER = Logger.getLogger(MockServiceProvider.class.getName());
  private static final long serialVersionUID = 1L;
  private String idpUrl;

  public MockServiceProvider(String idpUrl) {
    this.idpUrl = idpUrl;
  }

  // This method is normally declared "protected", but the testing harness will need to be able to
  // call it from the mock user agent, so we declare it as "public".
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (req.getSession().getAttribute("isAuthenticated") == null) {
      SAMLMessageContext<SAMLObject, AuthnRequest, NameID> context =
          OpenSamlUtil.makeSamlMessageContext();
      {
        AuthnRequest request = OpenSamlUtil.makeAuthnRequest();
        request.setProtocolBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
        request.setProviderName(OpenSamlUtil.GOOGLE_PROVIDER_NAME);
        request.setIssuer(OpenSamlUtil.makeIssuer(OpenSamlUtil.GOOGLE_ISSUER));
        request.setIsPassive(false);
        request.setAssertionConsumerServiceURL(idpUrl);
        context.setOutboundSAMLMessage(request);
      }
      context.setOutboundSAMLProtocol("http");
      context.setPeerEntityEndpoint(OpenSamlUtil.makeSingleSignOnService(
          SAMLConstants.SAML2_REDIRECT_BINDING_URI, idpUrl));
      // context.setRelayState("relayState");
      context.setOutboundMessageTransport(new HttpServletResponseAdapter(resp, true));

      HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
      try {
        encoder.encode(context);
      } catch (MessageEncodingException e) {
        throw new ServletException(e);
      }
    } else {
      resp.setContentType("text/html");
      resp.setCharacterEncoding("UTF-8");
      resp.setBufferSize(0x1000);
      PrintWriter writer = resp.getWriter();
      writer.print("<html><head><title>What you need</title></head>");
      writer.print("<body><h1>What you need...</h1><p>...is what we've got!</p></body></html>");
      writer.println("");
      writer.close();
      resp.setStatus(200);
    }
  }
}
