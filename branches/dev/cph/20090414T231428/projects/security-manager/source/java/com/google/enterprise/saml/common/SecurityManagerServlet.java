// Copyright (C) 2008, 2009 Google Inc.
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

import com.google.enterprise.common.ServletBase;
import com.google.enterprise.common.SessionAttribute;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.SecAuthnContext;
import com.google.enterprise.saml.server.BackEnd;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.metadata.EntityDescriptor;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static com.google.enterprise.saml.common.OpenSamlUtil.makeSamlMessageContext;

/**
 * Useful utilities for writing servlets.
 */
public abstract class SecurityManagerServlet extends ServletBase {

  private static final long serialVersionUID = 1L;

  /** Session attribute to hold the authentication context. */
  private static final SessionAttribute<SecAuthnContext> AUTHN_CONTEXT_ATTR =
      SessionAttribute.getNamed("authnContext");

  /** Session attribute to hold the SAML message context. */
  private static final SessionAttribute<SAMLMessageContext<? extends SAMLObject, ? extends SAMLObject, ? extends SAMLObject>> SAML_CONTEXT_ATTR =
      SessionAttribute.getNamed("samlContext");

  public static ConnectorManager getConnectorManager() {
    return ConnectorManager.class.cast(Context.getInstance().getManager());
  }

  public static BackEnd getBackEnd() {
    return getConnectorManager().getBackEnd();
  }

  public static EntityDescriptor getEntity(String id) throws ServletException {
    return getMetadata().getEntity(id);
  }

  public static EntityDescriptor getSmEntity() throws ServletException {
    return getMetadata().getSmEntity();
  }

  private static Metadata getMetadata() {
    return Metadata.class.cast(Context.getInstance().getRequiredBean("Metadata", Metadata.class));
  }

  /**
   * Get the GSA session ID for an HTTP request.
   *
   * @param request An HTTP request.
   * @return The corresponding session ID.
   * @throws ServletException if no session ID is found.
   */
  protected static String getSessionId(HttpServletRequest request)
      throws ServletException {
    String sessionId = getSessionId1(request);
    if (sessionId == null) {
      throw new ServletException("No session ID found");
    }
    return sessionId;
  }

  /**
   * Get the GSA session ID for an HTTP request.
   *
   * @param request An HTTP request.
   * @return If the request has a session ID.
   */
  protected static boolean hasSessionId(HttpServletRequest request) {
    return (getSessionId1(request) != null);
  }

  private static String getSessionId1(HttpServletRequest request) {
    Cookie[] cs = request.getCookies();
    if (cs != null) {
      for (Cookie c : cs) {
        if (c.getName().equals(GsaConstants.AUTHN_SESSION_ID_COOKIE_NAME)) {
          return c.getValue();
        }
      }
    }
    return null;
  }

  /**
   * Get an authentication context for a session, creating if needed.
   *
   * @param request An HTTP request.
   * @return An authentication context for that request.
   */
  protected static SecAuthnContext getAuthnContext(HttpServletRequest request)
      throws ServletException {
    String sessionId = getSessionId(request);
    SecAuthnContext context = AUTHN_CONTEXT_ATTR.get(sessionId);
    if (context == null) {
      context = new SecAuthnContext();
      context.addCookies(request.getCookies());
      AUTHN_CONTEXT_ATTR.put(sessionId, context);
    }
    return context;
  }

  /**
   * Create a new SAML message context and associate it with this request.
   *
   * @param request The current request object.
   * @return A new message context.
   */
  public static <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject>
        SAMLMessageContext<TI, TO, TN> newSamlMessageContext(HttpServletRequest request)
      throws ServletException {
    SAMLMessageContext<TI, TO, TN> context = makeSamlMessageContext();
    SAML_CONTEXT_ATTR.put(getSessionId(request), context);
    return context;
  }

  /**
   * Fetch a SAML message context from a request.
   *
   * @param request The current request object.
   * @return The previously saved message context.
   * @throws ServletException if there's no context in the request.
   */
  public static <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject>
        SAMLMessageContext<TI, TO, TN> existingSamlMessageContext(HttpServletRequest request)
      throws ServletException {
    // Restore context and signal error if none.
    @SuppressWarnings("unchecked")
    SAMLMessageContext<TI, TO, TN> context =
        (SAMLMessageContext<TI, TO, TN>) SAML_CONTEXT_ATTR.get(getSessionId(request));
    if (context == null) {
      throw new ServletException("Unable to get SAML message context.");
    }
    return context;
  }
}
