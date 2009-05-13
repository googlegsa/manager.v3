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

package com.google.enterprise.connector.saml.server;

import com.google.common.base.Preconditions;
import com.google.enterprise.connector.common.GettableHttpServlet;
import com.google.enterprise.connector.common.PostableHttpServlet;
import com.google.enterprise.connector.saml.common.SecurityManagerServlet;
import com.google.enterprise.connector.security.identity.CredentialsGroup;
import com.google.enterprise.connector.security.identity.DomainCredentials;
import com.google.enterprise.connector.security.ui.OmniForm;
import com.google.enterprise.connector.security.ui.OmniFormHtml;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.encoding.HTTPArtifactEncoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAssertion;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAttribute;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAttributeStatement;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAttributeValue;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAudience;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAudienceRestriction;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeAuthnStatement;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeConditions;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.makeSubject;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.connector.saml.common.OpenSamlUtil.runEncoder;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;

/**
 * Handler for SAML authentication requests.  These requests are sent by a service provider, in our
 * case the Google Search Appliance.  This is one part of the security manager's identity provider.
 */
public class SamlAuthn extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {
  private static final Logger LOGGER = Logger.getLogger(SamlAuthn.class.getName());
  /** Required for serializable classes. */
  private static final long serialVersionUID = 1L;
  private static final String PROMPT_COUNTER_NAME = "SamlAuthnPromptCounter";
  private static final String OMNI_FORM_NAME = "SamlAuthnOmniForm";
  private static final int defaultMaxPrompts = 3;

  private int maxPrompts;

  public SamlAuthn() {
    maxPrompts = defaultMaxPrompts;
  }

  public void setMaxPrompts(int maxPrompts) {
    this.maxPrompts = maxPrompts;
  }

  /**
   * Accept an authentication request and (eventually) respond to the service provider with a
   * response.  The request is generated by the service provider, then sent to the user agent as a
   * redirect.  The user agent redirects here, with the SAML AuthnRequest message encoded as a query
   * parameter.
   *
   * It's our job to authenticate the user behind the agent.  At the moment we respond with a
   * trivial form that prompts for username and password, but soon this will be replaced by
   * something more sophisticated.  Once the user agent posts the credentials, we validate them and
   * send the response.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    updateIncomingCookies(request);
    BackEnd backend = getBackEnd();

    // Establish the SAML message context.
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        newSamlMessageContext(request.getSession());
    {
      EntityDescriptor localEntity = getSmEntity();
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

    // If there are cookies we can decode, use them.
    if (tryCookies(request, response)) {
    } else if (backend.isIdentityConfigured()) {
      maybePrompt(request, response);
    } else {
      makeUnsuccessfulResponse(request, response, "Security Manager not configured");
    }
    updateOutgoingCookies(request, response);
  }

  // Try to find cookies that can be decoded into identities.
  private boolean tryCookies(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    BackEnd backend = getBackEnd();
    List<String> ids = new ArrayList<String>();
    for (CredentialsGroup cg : getCredentialsGroups(request)) {
      for (DomainCredentials dc : cg.getElements()) {
        if (dc.getUsername() == null) {
          backend.handleCookie(dc);
          String username = dc.getUsername();
          if (username != null) {
            ids.add(username);
          }
        }
      }
    }
    if (ids.isEmpty()) {
      return false;
    }
    makeSuccessfulResponse(request, response, ids);
    return true;
  }

  /**
   * Extract the username and password from the parameters, then ask the backend to validate them.
   * The backend returns the appropriate SAML Response message, which we then encode and return to
   * the service provider.  At the moment we only support the Artifact binding for the response.
   *
   * @param request The HTTP request message.
   * @param response The HTTP response message.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    updateIncomingCookies(request);
    BackEnd backend = getBackEnd();

    // Run all possible verifications given the user's input.
    OmniForm omniform = getOmniForm(request);
    omniform.handleFormSubmit(request);
    List<String> ids = new ArrayList<String>();
    for (CredentialsGroup cg : getCredentialsGroups(request)) {
      if (!cg.isVerified() && cg.isVerifiable()) {
        backend.authenticate(cg);
        if (cg.isVerified()) {
          ids.add(cg.getUsername());
        } else {
          LOGGER.info("Credentials group unfulfilled: " + cg.getHumanName());
        }
      }
    }

    // Now decide whether we need more input.
    // TODO(cph): this heuristic is WRONG!
    if (ids.isEmpty()) {
      maybePrompt(request, response);
      updateOutgoingCookies(request, response);
      return;
    }

    // This sequence is done; reset for next.
    resetPromptCounter(request.getSession());

    makeSuccessfulResponse(request, response, ids);
    updateOutgoingCookies(request, response);
  }

  private void maybePrompt(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    HttpSession session = request.getSession();
    if (shouldPrompt(session)) {
      PrintWriter writer = initNormalResponse(response);
      writer.print(getOmniForm(request).generateForm());
      writer.close();
    } else {
      makeUnsuccessfulResponse(request, response, "Incorrect username or password");
    }
  }

  private boolean shouldPrompt(HttpSession session) {
    Object value = session.getAttribute(PROMPT_COUNTER_NAME);
    int n = (value == null) ? 0 : Integer.class.cast(value);
    if (n < maxPrompts) {
      session.setAttribute(PROMPT_COUNTER_NAME, Integer.valueOf(n + 1));
      return true;
    }
    resetPromptCounter(session);
    return false;
  }

  private void resetPromptCounter(HttpSession session) {
    session.removeAttribute(PROMPT_COUNTER_NAME);
  }

  private static OmniForm getOmniForm(HttpServletRequest request)
      throws IOException {
    HttpSession session = request.getSession();
    OmniForm omniform = sessionOmniForm(session);
    if (null == omniform) {
      omniform = new OmniForm(getCredentialsGroups(request), new OmniFormHtml(getAction(request)));
      session.setAttribute(OMNI_FORM_NAME, omniform);
    }
    return omniform;
  }

  // Exposed for debugging:
  public static OmniForm sessionOmniForm(HttpSession session) {
    return OmniForm.class.cast(session.getAttribute(OMNI_FORM_NAME));
  }

  private static String getAction(HttpServletRequest request) {
    String url = request.getRequestURL().toString();
    int q = url.indexOf("?");
    return (q < 0) ? url : url.substring(0, q);
  }

  // We have at least one verified identity.  The first identity is considered the primary.
  private void makeSuccessfulResponse(HttpServletRequest request, HttpServletResponse response,
                                      List<String> ids)
      throws IOException {
    LOGGER.info("Verified IDs: " + idsToString(ids));

    List<CredentialsGroup> credentialsGroups = getCredentialsGroups(request);

    // Update the Session Manager with the necessary info.
    // TODO(cph): Until we finish off-boarding, the session ID should be required.
    String sessionId = getGsaSessionId(request.getSession());
    if (sessionId != null) {
      getBackEnd().updateSessionManager(sessionId, credentialsGroups);
    }

    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        existingSamlMessageContext(request.getSession());

    // Generate <Assertion> with <AuthnStatement>.
    Assertion assertion =
        makeAssertion(makeIssuer(getSmEntity().getEntityID()), makeSubject(ids.get(0)));
    assertion.getAuthnStatements().add(makeAuthnStatement(AuthnContext.IP_PASSWORD_AUTHN_CTX));

    // Generate <Conditions> with <AudienceRestriction>.
    Conditions conditions = makeConditions();
    AudienceRestriction restriction = makeAudienceRestriction();
    restriction.getAudiences().add(makeAudience(context.getInboundMessageIssuer()));
    conditions.getAudienceRestrictions().add(restriction);
    assertion.setConditions(conditions);

    // Generate <Response>.
    Response samlResponse =
        makeResponse(context.getInboundSAMLMessage(), makeStatus(StatusCode.SUCCESS_URI));

    // Add metadata attribute for serialized identities.
    addIdentityMetadataAttribute(assertion, credentialsGroups);

    samlResponse.getAssertions().add(assertion);
    context.setOutboundSAMLMessage(samlResponse);
    doRedirect(request, response);
  }

  private void addIdentityMetadataAttribute(Assertion assertion,
      List<CredentialsGroup> credentialsGroups) {
    Preconditions.checkNotNull(credentialsGroups);
    Attribute attribute = makeAttribute("serialized-ids");
    for (CredentialsGroup cg : credentialsGroups) {
      for (DomainCredentials dCred : cg.getElements()) {
        String serializedId = dCred.toJson();
        attribute.getAttributeValues().add(makeAttributeValue(serializedId));
      }
    }
    AttributeStatement attrStatement = makeAttributeStatement();
    attrStatement.getAttributes().add(attribute);
    assertion.getAttributeStatements().add(attrStatement);
  }

  private String idsToString(List<String> ids) {
    StringBuffer buffer = new StringBuffer();
    for (String id: ids) {
      if (buffer.length() > 0) {
        buffer.append(", ");
      }
      buffer.append(id);
    }
    return buffer.toString();
  }

  private void makeUnsuccessfulResponse(HttpServletRequest request, HttpServletResponse response,
                                        String message)
      throws IOException {
    LOGGER.warning(message);
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        existingSamlMessageContext(request.getSession());
    context.setOutboundSAMLMessage(makeResponse(context.getInboundSAMLMessage(),
                                                makeStatus(StatusCode.AUTHN_FAILED_URI, message)));
    doRedirect(request, response);
  }

  private void doRedirect(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        existingSamlMessageContext(request.getSession());
    // Encode the response message.
    initResponse(response);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
    HTTPArtifactEncoder encoder =
        new HTTPArtifactEncoder(null, null, getBackEnd().getArtifactMap());
    encoder.setPostEncoding(false);
    runEncoder(encoder, context);
  }
}
