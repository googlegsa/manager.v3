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

package com.google.enterprise.saml.server;

import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPArtifactEncoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatusMessage;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.selectPeerEndpoint;
import static com.google.enterprise.saml.common.ServletUtil.errorServletResponse;
import static com.google.enterprise.saml.common.ServletUtil.htmlServletResponse;
import static com.google.enterprise.saml.common.ServletUtil.initializeServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;

/**
 * Handler for SAML login from the Google Search Appliance.
 */
public class SamlAuthn extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(SamlAuthn.class.getName());
  private static final String SSO_SAML_CONTEXT = "ssoSamlContext";

  private final String formPostUrl;
  private final EntityDescriptor spEntity;
  private final EntityDescriptor idpEntity;

  public SamlAuthn(String formPostUrl, EntityDescriptor spEntity, EntityDescriptor idpEntity) {
    this.formPostUrl = formPostUrl;
    this.spEntity = spEntity;
    this.idpEntity = idpEntity;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();

    // Establish the SAML message context
    SAMLMessageContext<AuthnRequest, Response, NameID> context = makeSamlMessageContext();
    initializeLocalEntity(context, idpEntity, idpEntity.getIDPSSODescriptor(SAML20P_NS),
                          SingleSignOnService.DEFAULT_ELEMENT_NAME);
    initializePeerEntity(context, spEntity, spEntity.getSPSSODescriptor(SAML20P_NS),
                         AssertionConsumerService.DEFAULT_ELEMENT_NAME);
    selectPeerEndpoint(context, SAML2_ARTIFACT_BINDING_URI);
    session.setAttribute(SSO_SAML_CONTEXT, context);

    // Decode the request
    context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
    runDecoder(new HTTPRedirectDeflateDecoder(), context);

    // Generate the login form
    PrintWriter out = htmlServletResponse(response);
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

  /**
   * Extract username/password from login form, lookup the user then generate
   * SAML artifact.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession();
    ConnectorManager manager =
        (ConnectorManager) Context.getInstance(getServletContext()).getManager();
    BackEnd backend = manager.getBackEnd();

    // Restore context and signal error if none.
    @SuppressWarnings("unchecked")
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        (SAMLMessageContext<AuthnRequest, Response, NameID>) session.getAttribute(SSO_SAML_CONTEXT);
    if (context == null) {
      LOGGER.log(Level.WARNING, "Unable to get identity provider message context.");
      errorServletResponse(response, SC_INTERNAL_SERVER_ERROR);
      return;
    }

    // Get credentials and confirm they are present
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    if ((username == null) || (password == null)) {
      LOGGER.log(Level.WARNING, "Missing required POST parameter(s)");
      Status status = makeStatus(StatusCode.REQUESTER_URI);
      status.setStatusMessage(makeStatusMessage("Missing required POST parameter(s)"));
      context.setOutboundSAMLMessage(makeResponse(context.getInboundSAMLMessage(), status));
    } else {
      context.setOutboundSAMLMessage(
          backend.validateCredentials(context.getInboundSAMLMessage(), username, password));
    }

    // Encode the response message
    initializeServletResponse(response);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
    HTTPArtifactEncoder encoder = new HTTPArtifactEncoder(null, null, backend.getArtifactMap());
    encoder.setPostEncoding(false);
    runEncoder(encoder, context);
  }
}
