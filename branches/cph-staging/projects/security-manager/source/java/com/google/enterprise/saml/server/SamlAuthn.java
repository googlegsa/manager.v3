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

import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.common.GettableHttpServlet;
import com.google.enterprise.saml.common.Metadata;
import com.google.enterprise.saml.common.PostableHttpServlet;
import com.google.enterprise.saml.common.SecurityManagerServlet;

import org.opensaml.common.binding.SAMLMessageContext;
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
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.enterprise.saml.common.OpenSamlUtil.SM_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializeLocalEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.initializePeerEntity;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertion;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAuthnStatement;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatusMessage;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSubject;
import static com.google.enterprise.saml.common.OpenSamlUtil.runDecoder;
import static com.google.enterprise.saml.common.OpenSamlUtil.runEncoder;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;

/**
 * Handler for SAML authentication requests.  These requests are sent by a service provider, in our
 * case the Google Search Appliance.  This is one part of the security manager's identity provider.
 */
public class SamlAuthn extends SecurityManagerServlet
    implements GettableHttpServlet, PostableHttpServlet {

  /** Required for serializable classes. */
  private static final long serialVersionUID = 1L;
  private OmniForm loginForm;
  private static final Logger LOGGER = Logger.getLogger(SamlAuthn.class.getName());

  public SamlAuthn() {
    super(Metadata.SM_KEY);
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
      throws ServletException, IOException {
    BackEnd backend = getBackEnd(getServletContext());

    // Establish the SAML message context
    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        newSamlMessageContext(request.getSession());
    {
      EntityDescriptor localEntity = getLocalEntity();
      initializeLocalEntity(context, localEntity, localEntity.getIDPSSODescriptor(SAML20P_NS),
                            SingleSignOnService.DEFAULT_ELEMENT_NAME);
    }

    // Decode the request
    context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
    runDecoder(new HTTPRedirectDeflateDecoder(), context);

    // Select entity for response
    {
      EntityDescriptor peerEntity = getPeerEntity(context.getInboundMessageIssuer());
      initializePeerEntity(context, peerEntity, peerEntity.getSPSSODescriptor(SAML20P_NS),
                           AssertionConsumerService.DEFAULT_ELEMENT_NAME,
                           SAML2_ARTIFACT_BINDING_URI);
    }

    // If there's a cookie we can decode, use that
    // TODO fold this nicely into multiple identities
    if (tryCookies(request, response)) {
      return;
    }
   
    String formHtml = omniform(backend.getAuthConfigFile(), request);

    response.getWriter().print(formHtml);
  }

  private String getAction(HttpServletRequest request) {
    String url = request.getRequestURL().toString();
    int q = url.indexOf("?");
    
    return (q < 0) ? url : url.substring(0, q);
  }
    
  private String omniform(String configFile, HttpServletRequest request)
      throws NumberFormatException, IOException {
    File tmpFile = new File(configFile);
    LOGGER.info("Opened CSV file " + tmpFile.getAbsolutePath());
    FileReader file = new FileReader(tmpFile);
    CSVReader reader = new CSVReader(file);
    loginForm = new OmniForm(reader, getAction(request));
    String formHtml = loginForm.writeForm(null);
    
    return formHtml;
  }

  /**
   * Try to find a cookie that can be decoded into an identity.
   *
   * @param request The HTTP request message.
   * @param response The HTTP response message.  Filled in if a suitable cookie was found.
   * @return Whether or not a usable cookie was found.
   */
  private boolean tryCookies(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    BackEnd backend = getBackEnd(getServletContext());
    Map<String, String> cookieJar = getCookieJar(request);
    if (cookieJar == null) {
      return false;
    }

    AuthenticationResponse authnResponse =
        backend.handleCookie(cookieJar);
    if ((authnResponse != null) && authnResponse.isValid()) {
      // TODO make sure authnResponse has subject in BackEnd
      String username = authnResponse.getData();
      if (username != null) {
        SAMLMessageContext<AuthnRequest, Response, NameID> context =
            existingSamlMessageContext(request.getSession());
        context.setOutboundSAMLMessage(
            makeSuccessfulResponse(context.getInboundSAMLMessage(), username));
        doRedirect(context, backend, response);
        return true;
      }
    }
    return false;
  }

  /**
   * If this request carries cookies, we use registered security connectors to
   * figure out the user identity from the cookie.
   *
   * @param request The HTTP request message.
   * @return A Map of cookies, or null if no cookies.
   */
  private Map<String, String> getCookieJar(HttpServletRequest request) {
    Cookie[] jar = request.getCookies();
    if ((jar == null) || (jar.length == 0)) {
      return null;
    }
    Map<String, String> cookieJar = new HashMap<String, String>(jar.length);
    for (Cookie c: jar) {
      cookieJar.put(c.getName(), c.getValue());
    }
    return cookieJar;
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
      throws ServletException, IOException {
    BackEnd backend = getBackEnd(getServletContext());

    SAMLMessageContext<AuthnRequest, Response, NameID> context =
        existingSamlMessageContext(request.getSession());

    UserIdentity[] ids = (UserIdentity[]) request.getSession().getAttribute(SecurityManagerServlet.CREDENTIALS);
    ids = loginForm.parse(request, ids);
    for (UserIdentity id : ids) {
      // TODO make the context keep a queue of response SAMLMessage's
      Response samlMsg = backend.validateCredentials(context.getInboundSAMLMessage(), id);
      if (samlMsg != null)
        context.setOutboundSAMLMessage(samlMsg);    
    }
    
    // if not all identities get verified, repost omniform, with visual cues
    boolean hasInvalidCredential = false;
    for (UserIdentity id : ids) {
      if (id == null) // Skip auth site for which the user did not provide credentials
        continue;
      if (id.isVerified()) {
        Vector<Cookie> jar = id.getCookies();
        for (Cookie c : jar) {
          response.addCookie(c);
        }
      } else
        hasInvalidCredential = true;
    }
    
    if (hasInvalidCredential) {
      request.getSession().setAttribute(SecurityManagerServlet.CREDENTIALS, ids);
      response.getWriter().print(loginForm.writeForm(ids));
      return;
    }
    
    // generate artifact, redirect, plant cookies
    LOGGER.info("All identities verified");
    doRedirect(context, backend, response);
  }

  public static Response makeSuccessfulResponse(AuthnRequest request, String username) {
    LOGGER.info("Authenticated successfully as " + username);
    Response response = makeResponse(request, makeStatus(StatusCode.SUCCESS_URI));
    Assertion assertion = makeAssertion(makeIssuer(SM_ISSUER), makeSubject(username));
    assertion.getAuthnStatements().add(makeAuthnStatement(AuthnContext.IP_PASSWORD_AUTHN_CTX));
    response.getAssertions().add(assertion);
    return response;
  }

  public static Response makeUnsuccessfulResponse(AuthnRequest request, String code,
                                                  String message) {
    LOGGER.log(Level.WARNING, message);
    Status status = makeStatus(code);
    status.setStatusMessage(makeStatusMessage(message));
    return makeResponse(request, status);
  }

  private void doRedirect(SAMLMessageContext<AuthnRequest, Response, NameID> context,
      BackEnd backend, HttpServletResponse response)
      throws ServletException {
    // Encode the response message
    initResponse(response);
    context.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
    HTTPArtifactEncoder encoder = new HTTPArtifactEncoder(null, null, backend.getArtifactMap());
    encoder.setPostEncoding(false);
    runEncoder(encoder, context);
  }
    
}
