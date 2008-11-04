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

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPArtifactEncoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.springframework.mock.web.MockServletConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.saml.common.OpenSamlUtil.GOOGLE_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertion;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertionConsumerService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAuthnStatement;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSingleSignOnService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatusMessage;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSubject;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.SamlTestUtil.errorServletResponse;
import static com.google.enterprise.saml.common.SamlTestUtil.htmlServletResponse;
import static com.google.enterprise.saml.common.SamlTestUtil.initializeServletResponse;

import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;

public class MockIdentityProvider extends HttpServlet {
  private static final String className = MockIdentityProvider.class.getName();
  private static final Logger logger = Logger.getLogger(className);
  private static final long serialVersionUID = 1L;
  private static final String SSO_SAML_CONTEXT = "ssoSamlContext";

  private final SingleSignOnService ssoService;
  private final String formPostUrl;
  private final List<AssertionConsumerService> assertionConsumers;
  private int assertionConsumerIndex;
  private final SAMLArtifactMap artifactMap;

  public MockIdentityProvider(String ssoUrl, String formPostUrl) throws ServletException {
    init(new MockServletConfig());
    ssoService = makeSingleSignOnService(SAML2_REDIRECT_BINDING_URI, ssoUrl);
    this.formPostUrl = formPostUrl;
    assertionConsumers = new ArrayList<AssertionConsumerService>();
    assertionConsumerIndex = 0;
    artifactMap = new BasicSAMLArtifactMap(null, new MapBasedStorageService<String, SAMLArtifactMapEntry>(), 0);
  }

  /**
   * Get the endpoint to which AuthnRequest messages are to be sent.
   */
  public SingleSignOnService getSingleSignOnService() {
    return ssoService;
  }

  /**
   * Register an assertion consumer (one of the service provider's roles) with the identity
   * provider. Returns an endpoint for the use of the caller.
   */
  public int addAssertionConsumerService(String binding, String location) {
    if (SAML2_ARTIFACT_BINDING_URI.equals(binding)) {
      int index = assertionConsumerIndex++;
      assertionConsumers.add(makeAssertionConsumerService(binding, location, index));
      return index;
    } else if (SAML2_POST_BINDING_URI.equals(binding)) {
      // TODO(cph): implement this
      throw new IllegalArgumentException();
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String url = req.getRequestURL().toString();
    if (!ssoService.getLocation().equals(url)) {
      logger.log(Level.WARNING, "GET to unknown URI.");
      errorServletResponse(resp, HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    SAMLMessageContext<AuthnRequest, Response, NameID> context = makeSamlMessageContext();
    context.setInboundMessageTransport(new HttpServletRequestAdapter(req));
    HTTPRedirectDeflateDecoder decoder = new HTTPRedirectDeflateDecoder();
    runDecoder(decoder, context);
    req.getSession().setAttribute(SSO_SAML_CONTEXT, context);
    generateForm(req, resp);
  }

  @SuppressWarnings("unchecked")
  private <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject> SAMLMessageContext<TI, TO, TN> getMessageContext(HttpSession session, String name) {
    return (SAMLMessageContext<TI, TO, TN>) session.getAttribute(name);
  }

  private void generateForm(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter out = htmlServletResponse(resp);
    out.print("<html><head><title>Please Login</title></head><body>\n" +
              "<form action=\"" +
              formPostUrl +
              "\" method=POST>\n" +
              "User Name:<input type=text name=username><br>\n" +
              "Password:<input type=password name=password><br>\n" +
              "<input type=submit>\n" +
              "</form>\n" +
              "</body></html>\n");
    out.close();
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession();
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        getMessageContext(session, SSO_SAML_CONTEXT);
    if (context == null) {
      logger.log(Level.WARNING, "Unable to get identity provider message context.");
      errorServletResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    Status status = makeStatus();
    Response response = makeResponse(context.getInboundSAMLMessage(), status);

    String username = req.getParameter("username");
    String password = req.getParameter("password");

    if (!formPostUrl.equals(req.getRequestURI())) {
      requestError(status, "POST to unknown URI.");
    } else if ((username == null) || (password == null)) {
      requestError(status, "Missing required POST parameter(s).");
    } else if (!authenticate(username, password)) {
      logger.log(Level.INFO, "Authentication failed.");
      status.getStatusCode().setValue(StatusCode.AUTHN_FAILED_URI);
    } else {
      logger.log(Level.INFO, "Authenticated successfully as " + username + ".");
      status.getStatusCode().setValue(StatusCode.SUCCESS_URI);
      Assertion assertion = makeAssertion(makeIssuer(GOOGLE_ISSUER), makeSubject(username));
      assertion.getAuthnStatements().add(makeAuthnStatement(AuthnContext.IP_PASSWORD_AUTHN_CTX));
      response.getAssertions().add(assertion);
    }

    context.setOutboundSAMLMessage(response);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(resp, true));
    initializeServletResponse(resp);
    HTTPArtifactEncoder encoder = new HTTPArtifactEncoder(null, null, artifactMap);
    runEncoder(encoder, context);
  }

  private void requestError(Status status, String message) {
    logger.log(Level.WARNING, message);
    status.getStatusCode().setValue(StatusCode.REQUESTER_URI);
    status.setStatusMessage(makeStatusMessage(message));
  }

  private boolean authenticate(String username, String password) {
    return "joe".equals(username) && "plumber".equals(password);
  }
}
