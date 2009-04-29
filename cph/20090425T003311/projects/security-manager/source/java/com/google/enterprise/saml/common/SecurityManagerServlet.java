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

package com.google.enterprise.saml.common;

import com.google.enterprise.common.CookieDifferentiator;
import com.google.enterprise.common.CookieSet;
import com.google.enterprise.common.ServletBase;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.saml.server.BackEnd;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.metadata.EntityDescriptor;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;

/**
 * Useful utilities for writing servlets.
 */
public abstract class SecurityManagerServlet extends ServletBase {

  private static final long serialVersionUID = 1L;

  /** Name of the session attribute that holds the SAML message context. */
  private static final String SAML_CONTEXT = "samlMessageContext";

  /** Name of the attribute that holds the username/passwords awaiting verification. */
  protected static final String CREDENTIALS = "credentials";

  /** Name of the attribute that holds the session's cookie differentiator. */
  private static final String COOKIE_DIFFERENTIATOR_NAME = "CookieDifferentiator";

  /** Name of the attribute that holds the GSA's session ID. */
  protected static final String GSA_SESSION_ID_NAME = "GsaSessionId";

  public ConnectorManager getConnectorManager() {
    return ConnectorManager.class.cast(Context.getInstance().getManager());
  }

  public BackEnd getBackEnd() {
    return getConnectorManager().getBackEnd();
  }

  public SessionManagerInterface getSessionManager() {
    return getBackEnd().getSessionManager();
  }

  public EntityDescriptor getEntity(String id) throws ServletException {
    return getMetadata().getEntity(id);
  }

  public EntityDescriptor getSmEntity() throws ServletException {
    return getMetadata().getSmEntity();
  }

  private Metadata getMetadata() {
    return Metadata.class.cast(Context.getInstance().getRequiredBean("Metadata", Metadata.class));
  }

  /**
   * Create a new SAML message context and associate it with this session.
   *
   * @param session The current session object.
   * @return A new message context.
   */
  public static <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject>
        SAMLMessageContext<TI, TO, TN> newSamlMessageContext(HttpSession session) {
    SAMLMessageContext<TI, TO, TN> context = makeSamlMessageContext();
    session.setAttribute(SAML_CONTEXT, context);
    return context;
  }

  /**
   * Fetch a SAML message context from a session.
   *
   * @param session The current session object.
   * @return A new message context.
   * @throws ServletException if there's no context in the session.
   */
  public static <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject>
        SAMLMessageContext<TI, TO, TN> existingSamlMessageContext(HttpSession session)
      throws ServletException {
    // Restore context and signal error if none.
    @SuppressWarnings("unchecked")
    SAMLMessageContext<TI, TO, TN> context =
        (SAMLMessageContext<TI, TO, TN>) session.getAttribute(SAML_CONTEXT);
    if (context == null) {
      throw new ServletException("Unable to get SAML message context.");
    }
    return context;
  }

  /**
   * Get a session's cookie differentiator.
   *
   * @param session An HTTP session.
   * @return The cookie differentiator.
   */
  public static CookieDifferentiator getCookieDifferentiator(HttpSession session) {
    CookieDifferentiator cd =
        CookieDifferentiator.class.cast(session.getAttribute(COOKIE_DIFFERENTIATOR_NAME));
    if (cd == null) {
      cd = new CookieDifferentiator();
      session.setAttribute(COOKIE_DIFFERENTIATOR_NAME, cd);
    }
    return cd;
  }

  /**
   * Get a session's user-agent cookies.
   *
   * @param session An HTTP session.
   * @return The cookies.
   */
  public static CookieSet getUserAgentCookies(HttpSession session) {
    return getCookieDifferentiator(session).getNewCookies();
  }

  /**
   * Get a named cookie from an incoming HTTP request.
   *
   * @param session An HTTP request.
   * @param name The name of the cookie to return.
   * @return The corresponding cookie, or null if no such cookie.
   */
  public static Cookie getUserAgentCookie(HttpSession session, String name) {
    for (Cookie c : getUserAgentCookies(session)) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  /**
   * Get the GSA session ID string for a session.
   *
   * @param session An HTTP session.
   * @return The session ID, or null if none available.
   */
  public static String getGsaSessionId(HttpSession session) {
    String sessionId = String.class.cast(session.getAttribute(GSA_SESSION_ID_NAME));
    if (sessionId == null) {
      Cookie sessionCookie = getUserAgentCookie(session, GsaConstants.AUTHN_SESSION_ID_COOKIE_NAME);
      if (sessionCookie != null) {
        sessionId = sessionCookie.getValue();
        session.setAttribute(GSA_SESSION_ID_NAME, sessionId);
      }
    }
    return sessionId;
  }
}
