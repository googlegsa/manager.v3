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

import com.google.enterprise.common.ServletBase;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.saml.server.BackEnd;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.metadata.EntityDescriptor;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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

  /** Name of the attribute that holds the session's credentials groups. */
  private static final String CREDENTIALS_GROUPS_NAME = "CredentialsGroups";

  public static ConnectorManager getConnectorManager() {
    return ConnectorManager.class.cast(Context.getInstance().getManager());
  }

  public static BackEnd getBackEnd() {
    return getConnectorManager().getBackEnd();
  }

  public static SessionManagerInterface getSessionManager() {
    return getBackEnd().getSessionManager();
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
   * Get a named cookie from an incoming HTTP request.
   *
   * @param request An HTTP request.
   * @param name The name of the cookie to return.
   * @return The corresponding cookie, or null if no such cookie.
   */
  protected Cookie getUserAgentCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie c: cookies) {
        if (c.getName().equals(name)) {
          return c;
        }
      }
    }
    return null;
  }

  protected static List<CredentialsGroup> getCredentialsGroups(HttpServletRequest request)
      throws IOException {
    HttpSession session = request.getSession();
    List<CredentialsGroup> groups = sessionCredentialsGroups(session);
    if (null == groups) {
      groups = CredentialsGroup.newGroups(getBackEnd().getAuthnDomainGroups(), session);
      session.setAttribute(CREDENTIALS_GROUPS_NAME, groups);
    }
    return groups;
  }

  // Exposed for debugging:
  @SuppressWarnings("unchecked")
  public static List<CredentialsGroup> sessionCredentialsGroups(HttpSession session) {
    return List.class.cast(session.getAttribute(CREDENTIALS_GROUPS_NAME));
  }
}
