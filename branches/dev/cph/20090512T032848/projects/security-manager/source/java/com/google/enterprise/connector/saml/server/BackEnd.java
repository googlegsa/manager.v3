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

import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.security.identity.IdentityConfig;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface to SAML server backend. Top-level classes such as servlets should
 * do transport and marshaling only, then interact with an instance of this
 * class.
 *
 */
public interface BackEnd {

  /**
   * Set the connector manager used by this backend.
   */
  public void setConnectorManager(ConnectorManager cm);

  /**
   * Get the session manager used by this backend.
   *
   * This may migrate to the next inner layer.
   *
   * @return A session manager object.
   */
  public SessionManagerInterface getSessionManager();

  /**
   * Get the SAML artifact map.
   *
   * The backend holds onto this map but doesn't use it.  The map is used by the servlets comprising
   * the SAML identity provider.
   *
   * @return The unique artifact map for the security manager.
   */
  public SAMLArtifactMap getArtifactMap();

  /**
   * Get the identity configuration.
   *
   * @return The identity configuration.
   * @throw IllegalStateException if no identity configure has been set.
   */
  public IdentityConfig getIdentityConfig();

  /**
   * Inject the identity configuration source.
   *
   * @param identityConfig The identity configuration to use.
   */
  public void setIdentityConfig(IdentityConfig identityConfig);

  /**
   * Perform the authentication process for the security manager.
   *
   * This method may be called more than once before authentication is finished, so it
   * must be able to figure out what it needs to do when called.
   *
   * @param request The current HTTP request.
   * @param response The HTTP response to fill in before returning.
   */
  public void authenticate(HttpServletRequest request, HttpServletResponse response)
      throws IOException;

  /**
   * Process a set of SAML authorization queries.
   *
   * @param authzDecisionQueries A list of authorization queries to be processed.
   * @return A list of responses, corresponding to the argument.
   */
  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries);
}
