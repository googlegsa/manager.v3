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

package com.google.enterprise.saml.common;

import com.google.enterprise.common.ServletBase;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.saml.server.BackEnd;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.metadata.EntityDescriptor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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

  private final Context context;
  private final Metadata metadata;
  
  protected SecurityManagerServlet() {
    ServletConfig config = getServletConfig();
    if (config == null) {
      context = Context.getInstance();
    } else {
      context = Context.getInstance(config.getServletContext());
    }
    metadata = Metadata.class.cast(context.getRequiredBean("Metadata", Metadata.class));
  }

  public ConnectorManager getConnectorManager() {
    return ConnectorManager.class.cast(context.getManager());
  }

  public BackEnd getBackEnd() {
    return getConnectorManager().getBackEnd();
  }

  public EntityDescriptor getEntity(String id) throws ServletException {
    return metadata.getEntity(id);
  }

  public EntityDescriptor getSmEntity() throws ServletException {
    return metadata.getSmEntity();
  }

  public EntityDescriptor getSpEntity() throws ServletException {
    return metadata.getSpEntity();
  }

  public String getSpUrl() {
    return metadata.getSpUrl();
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
}
