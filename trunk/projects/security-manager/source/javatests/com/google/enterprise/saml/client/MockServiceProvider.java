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
import com.google.enterprise.saml.common.SamlTestUtil;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The MockServiceProvider class implements a servlet pretending to be the part of a SAML Service
 * Provider that receives a service request from the user agent and initiates an authn request from
 * an identity provider.
 */
public class MockServiceProvider extends HttpServlet {
  private static final String className = MockServiceProvider.class.getName();
  private static final Logger logger = Logger.getLogger(className);
  private static final long serialVersionUID = 1L;

  /**
   * The endpoint representing the identity provider.
   */
  private final Endpoint idpEndpoint;

  /**
   * Creates a new mock SAML service provider with the given identity provider.
   *
   * @param idpUrl The URL for the identity provider.
   */
  public MockServiceProvider(String idpUrl) {
    idpEndpoint =
        OpenSamlUtil.makeSingleSignOnService(SAMLConstants.SAML2_REDIRECT_BINDING_URI, idpUrl);
  }

  // This method is normally declared "protected", but the testing harness will need to be able to
  // call it from the mock user agent, so we declare it as "public".
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    logger.entering(className, "doGet");
    Object isAuthenticated = req.getSession().getAttribute("isAuthenticated");
    logger.log(Level.FINE, "isAuthenticate = " + isAuthenticated);
    if (isAuthenticated == Boolean.TRUE) {
      ifAllowed(resp);
    } else if (isAuthenticated == Boolean.FALSE) {
      ifDenied(resp);
    } else {
      ifUnknown(req, resp);
    }
    logger.exiting(className, "doGet");
  }

  private void ifAllowed(HttpServletResponse resp) throws IOException {
    logger.entering(className, "ifAllowed");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("text/html");
    resp.setCharacterEncoding("UTF-8");
    resp.setBufferSize(0x1000);
    PrintWriter writer = resp.getWriter();
    writer.print("<html><head><title>What you need</title></head>");
    writer.print("<body><h1>What you need...</h1><p>...is what we've got!</p></body></html>");
    writer.close();
    logger.exiting(className, "ifAllowed");
  }

  private void ifDenied(HttpServletResponse resp) throws IOException {
    logger.entering(className, "ifDenied");
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    resp.setContentType("text/html");
    resp.setCharacterEncoding("UTF-8");
    resp.setBufferSize(0x1000);
    PrintWriter writer = resp.getWriter();
    writer.print("<html><head><title>Access Denied</title></head>");
    writer.print("<body><h1>Access Denied</h1></body></html>");
    writer.close();
    logger.exiting(className, "ifDenied");
  }

  private void ifUnknown(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
    logger.entering(className, "ifUnknown");
    SamlTestUtil.initializeServletResponse(resp);
    SAMLMessageContext<SAMLObject, AuthnRequest, NameID> context =
        OpenSamlUtil.makeSamlMessageContext();
    context.setOutboundSAMLMessage(buildRequest(req));
    context.setOutboundSAMLProtocol("http");
    context.setPeerEntityEndpoint(idpEndpoint);
    context.setRelayState(req.getRequestURI());
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(resp, true));

    HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
    try {
      encoder.encode(context);
    } catch (MessageEncodingException e) {
      throw new ServletException(e);
    }
    logger.exiting(className, "ifUnknown");
  }

  private AuthnRequest buildRequest(HttpServletRequest req) {
    AuthnRequest request = OpenSamlUtil.makeAuthnRequest();
    request.setProtocolBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);
    request.setProviderName(OpenSamlUtil.GOOGLE_PROVIDER_NAME);
    request.setIssuer(OpenSamlUtil.makeIssuer(OpenSamlUtil.GOOGLE_ISSUER));
    request.setIsPassive(false);
    request.setAssertionConsumerServiceURL(req.getRequestURL().toString());
    return request;
  }
}
