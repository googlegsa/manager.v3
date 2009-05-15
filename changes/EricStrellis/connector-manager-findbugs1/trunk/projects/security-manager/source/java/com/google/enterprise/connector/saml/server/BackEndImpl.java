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

import com.google.enterprise.connector.common.ServletBase;
import com.google.enterprise.connector.common.cookie.CookieDifferentiator;
import com.google.enterprise.connector.common.cookie.CookieSet;
import com.google.enterprise.connector.common.cookie.CookieUtil;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.security.identity.AuthnMechanism;
import com.google.enterprise.connector.security.identity.CredentialsGroup;
import com.google.enterprise.connector.security.identity.DomainCredentials;
import com.google.enterprise.connector.security.identity.IdentityConfig;
import com.google.enterprise.connector.security.ui.OmniForm;
import com.google.enterprise.connector.security.ui.OmniFormHtml;
import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.connector.spi.VerificationStatus;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * An implementation of the BackEnd interface for the Security Manager.
 */
public class BackEndImpl implements BackEnd {
  private static final Logger LOGGER = Logger.getLogger(BackEndImpl.class.getName());
  private static final int artifactLifetime = 600000;  // ten minutes
  private static final int defaultMaxPrompts = 3;

  private enum AuthnState { IDLE, IN_OMNIFORM }

  /** Name of the attribute that holds the session's authentication state. */
  private static final String AUTHN_STATE_NAME = "AuthnState";

  /** Name of the attribute that holds the session's credentials groups. */
  private static final String CREDENTIALS_GROUPS_NAME = "CredentialsGroups";

  /** Name of the attribute that holds the session's OmniForm object. */
  private static final String OMNI_FORM_NAME = "SamlAuthnOmniForm";

  /** Name of the attribute that holds the session's prompt counter. */
  private static final String PROMPT_COUNTER_NAME = "SamlAuthnPromptCounter";

  /** Name of the attribute that holds the session's incoming cookie set. */
  private static final String INCOMING_COOKIES_NAME = "IncomingCookies";

  /** Name of the attribute that holds the session's outgoing cookie set. */
  private static final String OUTGOING_COOKIES_NAME = "OutgoingCookies";

  /** Name of the cookie in which we store the sessionId */
  public static final String GSA_SESSION_ID_COOKIE_NAME = "GSA_SESSION_ID";

  private final SessionManagerInterface sm;
  private ConnectorManager manager;
  private final AuthzResponder authzResponder;
  private final SAMLArtifactMap artifactMap;
  private IdentityConfig identityConfig;
  private int maxPrompts;

  // temporary: to permit testing with no session manager
  // todo: remove this with the entire session manager
  private boolean dontUseSessionManager = false;

  protected GSASessionAdapter adapter;

  // public for testing/debugging
  public Vector<String> sessionIds;

  /**
   * Create a new backend object.
   *
   * @param sm The session manager to use.
   * @param authzResponder The authorization responder to use.
   */
  public BackEndImpl(SessionManagerInterface sm, AuthzResponder authzResponder) {
    this.sm = sm;
    this.authzResponder = authzResponder;
    artifactMap = new BasicSAMLArtifactMap(
        new BasicParserPool(),
        new MapBasedStorageService<String, SAMLArtifactMapEntry>(),
        artifactLifetime);
    identityConfig = null;
    adapter = new GSASessionAdapter(sm);
    sessionIds = new Vector<String>();
    maxPrompts = defaultMaxPrompts;
  }

  /* @Override */
  public void setConnectorManager(ConnectorManager cm) {
    this.manager = cm;
  }

  /* @Override */
  public SessionManagerInterface getSessionManager() {
    return sm;
  }

  /* @Override */
  public IdentityConfig getIdentityConfig() {
    if (identityConfig == null) {
      throw new IllegalStateException("No IdentityConfig has been provided");
    }
    return identityConfig;
  }

  /* @Override */
  public void setIdentityConfig(IdentityConfig identityConfig) {
    this.identityConfig = identityConfig;
  }

  /* @Override */
  public SAMLArtifactMap getArtifactMap() {
    return artifactMap;
  }

  /* @Override */
  public int getMaxPrompts() {
    return maxPrompts;
  }

  /* @Override */
  public void setMaxPrompts(int maxPrompts) {
    this.maxPrompts = maxPrompts;
  }

  /* @Override */
  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    return authzResponder.authorizeBatch(authzDecisionQueries);
  }

  /* @Override */
  public void authenticate(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    updateIncomingCookies(request);

    switch (getAuthnState(request.getSession())) {
      case IDLE:
        authenticate_idle(request, response);
        break;
      case IN_OMNIFORM:
        authenticate_in_omniform(request, response);
        break;
      default:
        throw new IllegalStateException("Unknown authentication state");
    }
  }

  private void authenticate_idle(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // If there are cookies we can decode, use them.
    List<String> ids = tryCookies(request);
    if (!ids.isEmpty()) {
      successfulSamlResponse(request, response, ids);
    } else if (!getCredentialsGroups(request).isEmpty()) {
      maybePrompt(request, response);
    } else {
      unsuccessfulSamlResponse(request, response, "Security Manager not configured");
    }
  }

  // Try to find cookies that can be decoded into identities.
  private List<String> tryCookies(HttpServletRequest request) throws IOException {
    List<String> ids = new ArrayList<String>();
    for (CredentialsGroup cg : getCredentialsGroups(request)) {
      for (DomainCredentials dc : cg.getElements()) {
        if (dc.getUsername() == null) {
          handleCookie(dc);
          String username = dc.getUsername();
          if (username != null) {
            ids.add(username);
          }
        }
      }
    }
    return ids;
  }

  // some form of authentication has already happened, the user gave us cookies,
  // see if the cookies reveal who the user is.
  private void handleCookie(SecAuthnIdentity id) {
    if (id.getVerificationStatus() != VerificationStatus.TBD) {
      return;
    }
    for (ConnectorStatus connStatus: manager.getConnectorStatuses()) {
      String connType = connStatus.getType();
      if (! (connType.equals("SsoCookieIdentityConnector")
             || connType.equals("regexCookieIdentityConnector"))) {
        continue;
      }
      String connectorName = connStatus.getName();
      LOGGER.info("Got security plug-in " + connectorName);
      if (manager.authenticate(connectorName, id)) {
        LOGGER.info("Cookie(s) cracked for " + id.getDomain());
      }
    }
  }

  private void authenticate_in_omniform(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    getOmniForm(request).handleFormSubmit(request);

    // Run all possible verifications given the user's input.
    List<String> ids = new ArrayList<String>();
    for (CredentialsGroup cg : getCredentialsGroups(request)) {
      if (!cg.isVerified() && cg.isVerifiable()) {
        authenticateCredentialsGroup(cg);
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
      return;
    }

    successfulSamlResponse(request, response, ids);
  }

  private void authenticateCredentialsGroup(CredentialsGroup cg) {
    for (DomainCredentials dCred : cg.getElements()) {
      if (dCred.getVerificationStatus() != VerificationStatus.TBD) {
        continue;
      }
      String expectedTypeName;
      switch (dCred.getMechanism()) {
        case BASIC_AUTH:
          expectedTypeName = "BasicAuthConnector";
          break;
        case FORMS_AUTH:
          expectedTypeName = "FormAuthConnector";
          break;
        case CONNECTORS:
          expectedTypeName = "ConnAuthConnector";
          break;
        default:
          continue;
      }
      for (ConnectorStatus connStatus : manager.getConnectorStatuses()) {
        if (!connStatus.getType().equals(expectedTypeName)) {
          continue;
        }
        String connectorName = connStatus.getName();
        LOGGER.info("Got security plug-in " + connectorName);
        if (manager.authenticate(connectorName, dCred)) {
          LOGGER.info("Credential verified: " + dCred.dumpInfo());
        }
      }
    }
  }

  private void successfulSamlResponse(
      HttpServletRequest request, HttpServletResponse response, List<String> ids)
      throws IOException {
    updateOutgoingCookies(request, response);

    Cookie sessionCookie = getUserAgentCookie(request.getSession(), GSA_SESSION_ID_COOKIE_NAME);
    if (sessionCookie != null) {
      updateSessionManager(sessionCookie.getValue(), getCredentialsGroups(request));
    }

    SamlAuthn.makeSuccessfulSamlSsoResponse(request, response, ids);
    setIdleState(request.getSession());
  }

  private void unsuccessfulSamlResponse(
      HttpServletRequest request, HttpServletResponse response, String message)
      throws IOException {
    updateOutgoingCookies(request, response);
    SamlAuthn.makeUnsuccessfulSamlSsoResponse(request, response, message);
    setIdleState(request.getSession());
  }

  private void maybePrompt(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    HttpSession session = request.getSession();
    int n = getPromptCounter(session);
    if (n < maxPrompts) {
      PrintWriter writer = ServletBase.initNormalResponse(response);
      writer.print(getOmniForm(request).generateForm());
      writer.close();
      setPromptCounter(session, n + 1);
      setAuthnState(session, AuthnState.IN_OMNIFORM);
    } else {
      unsuccessfulSamlResponse(request, response, "Incorrect username or password");
    }
  }

  private AuthnState getAuthnState(HttpSession session) {
    AuthnState state = AuthnState.class.cast(session.getAttribute(AUTHN_STATE_NAME));
    return (state == null) ? AuthnState.IDLE : state;
  }

  private void setAuthnState(HttpSession session, AuthnState state) {
    session.setAttribute(AUTHN_STATE_NAME, state);
  }

  private void setIdleState(HttpSession session) {
    setAuthnState(session, AuthnState.IDLE);
    resetPromptCounter(session);
  }

  private int getPromptCounter(HttpSession session) {
    Integer counter = Integer.class.cast(session.getAttribute(PROMPT_COUNTER_NAME));
    return (counter == null) ? 0 : counter;
  }

  private void setPromptCounter(HttpSession session, int counter) {
    session.setAttribute(PROMPT_COUNTER_NAME, Integer.valueOf(counter));
  }

  private void resetPromptCounter(HttpSession session) {
    session.removeAttribute(PROMPT_COUNTER_NAME);
  }

  /* @Override */
  public List<CredentialsGroup> getCredentialsGroups(HttpServletRequest request)
      throws IOException {
    HttpSession session = request.getSession();
    List<CredentialsGroup> groups = sessionCredentialsGroups(session);
    if (null == groups) {
      groups = CredentialsGroup.newGroups(getIdentityConfig().getConfig(), session);
      session.setAttribute(CREDENTIALS_GROUPS_NAME, groups);
    }
    return groups;
  }

  // Exposed for debugging:
  @SuppressWarnings("unchecked")
  public static List<CredentialsGroup> sessionCredentialsGroups(HttpSession session) {
    return List.class.cast(session.getAttribute(CREDENTIALS_GROUPS_NAME));
  }

  private OmniForm getOmniForm(HttpServletRequest request)
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

  /**
   * Collect cookies from an incoming request and update our collection to match.
   *
   * Should be called by every servlet accepting requests from a user agent.
   *
   * @param request A request from a user agent.
   */
  private void updateIncomingCookies(HttpServletRequest request) {
    HttpSession session = request.getSession();
    CookieSet incoming = getUserAgentCookies(session);
    incoming.clear();
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      CookieUtil.subtractCookieSets(
          Arrays.asList(cookies), getOutgoingCookies(session), incoming);
    }
    CookieUtil.logRequestCookies(Level.INFO, "Incoming cookies from user agent", incoming);
  }

  /**
   * Collect new cookies from the authentication connectors and forward to user agent.
   *
   * Each newly received cookie is sent back, unless its name conflicts with one of the
   * cookies we've received from the user agent.
   *
   * @param request A request from a user agent.
   * @param response The response being prepared to send back.
   */
  private void updateOutgoingCookies(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    HttpSession session = request.getSession();

    // Find all IdP cookies that don't conflict with incoming cookies.
    CookieSet newOutgoing = new CookieSet();
    CookieSet incoming = getUserAgentCookies(session);
    for (CredentialsGroup cg : getCredentialsGroups(request)) {
      for (DomainCredentials dc : cg.getElements()) {
        CookieUtil.subtractCookieSets(
            dc.getCookies(), incoming, newOutgoing);
      }
    }

    // Remove any cookies missing their domains; the user-agent can't use those since
    // they're implicitly scoped to the IdP.
    Iterator<Cookie> iter = newOutgoing.iterator();
    while (iter.hasNext()) {
      String domain = iter.next().getDomain();
      if (domain == null || domain.isEmpty()) {
        iter.remove();
      }
    }

    // Send back any changes not previously sent.
    CookieSet toSend = CookieDifferentiator.differentiate(getOutgoingCookies(session), newOutgoing);
    setOutgoingCookies(session, newOutgoing);
    for (Cookie c : toSend) {
      response.addCookie(c);
    }
    CookieUtil.logResponseCookies(Level.INFO, "Outgoing cookies to user agent", toSend);
  }

  private static CookieSet getOutgoingCookies(HttpSession session) {
    return getCookieSet(session, OUTGOING_COOKIES_NAME);
  }

  private static void setOutgoingCookies(HttpSession session, CookieSet cookies) {
    session.setAttribute(OUTGOING_COOKIES_NAME, cookies);
  }

  /**
   * Get a session's incoming user-agent cookies.
   *
   * @param session An HTTP session.
   * @return The cookies.
   */
  public static CookieSet getUserAgentCookies(HttpSession session) {
    return getCookieSet(session, INCOMING_COOKIES_NAME);
  }

  private static CookieSet getCookieSet(HttpSession session, String name) {
    CookieSet cookies = CookieSet.class.cast(session.getAttribute(name));
    if (cookies == null) {
      cookies = new CookieSet();
      session.setAttribute(name, cookies);
    }
    return cookies;
  }

  /**
   * Get a named cookie from an incoming HTTP request.
   *
   * @param session An HTTP session.
   * @param name The name of the cookie to return.
   * @return The corresponding cookie, or null if no such cookie.
   */
  public static Cookie getUserAgentCookie(HttpSession session, String name) {
    for (Cookie c : getUserAgentCookies(session)) {
      // According to RFC 2965, cookie names are case-insensitive.
      if (c.getName().equalsIgnoreCase(name)) {
        return c;
        }
      }
    return null;
  }

  /* @Override */
  public void updateSessionManager(String sessionId, Collection<CredentialsGroup> cgs) {
    if (dontUseSessionManager) {
      LOGGER.info("Bypassing session manager");
      return;
    }

    LOGGER.info("Session ID: " + sessionId);
    sessionIds.add(sessionId);

    List<Cookie> cookies = new ArrayList<Cookie>();
    for (CredentialsGroup cg : cgs) {

      LOGGER.info("CG " + cg.getHumanName() + " has id: " + cg.getUsername());

      for (DomainCredentials dCred : cg.getElements()) {
        // This clobbers any priorly stored basic auth credentials.
        // The expectation is that only one basic auth module will be active
        // at any given time, or that if multiple basic auth modules are
        // active at once, only one of them will work.
        if (AuthnMechanism.BASIC_AUTH == dCred.getMechanism()) {
          if (null != cg.getUsername()) {
            adapter.setUsername(sessionId, cg.getUsername());
          }
          if (null != cg.getPassword()) {
            adapter.setPassword(sessionId, cg.getPassword());
          }
          // TODO(con): currently setting the domain will break functionality
          // for most Basic Auth headrequests. Once I figure out what's going on
          // with NtlmDomains, I'll fix this.
          //  if (null != id.getAuthSite()) {
          //    adapter.setDomain(sessionId, id.getAuthSite().getHostname());
          //  }
        }

        LOGGER.info("DomainCredential " + dCred.getDomain() + " cookies: " +
                    CookieUtil.setCookieHeaderValue(dCred.getCookies(), false));
        cookies.addAll(dCred.getCookies());
      }
    }
    adapter.setCookies(sessionId, CookieUtil.serializeCookies(cookies));
    LOGGER.info("Cookies sent to session manager: " +
                CookieUtil.setCookieHeaderValue(cookies, false));

    // TODO(con): connectors
  }

  public void setDontUseSessionManager(boolean dontUseSessionManager) {
    this.dontUseSessionManager = dontUseSessionManager;
  }

  public boolean isDontUseSessionManager() {
    return dontUseSessionManager;
  }
}
