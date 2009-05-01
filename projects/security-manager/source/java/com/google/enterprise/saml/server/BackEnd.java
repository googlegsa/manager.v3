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
import com.google.enterprise.connector.spi.SecAuthnIdentity;
import com.google.enterprise.security.identity.AuthnDomainGroup;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.IdentityConfig;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

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
   * Is there a reasonable identity configuration?
   *
   * @return <code>true</code> if so.
   */
  public boolean isIdentityConfigured() throws IOException;

  /**
   * Inject the identity configuration source.
   *
   * @param identityConfig The identity configuration to use.
   */
  public void setIdentityConfig(IdentityConfig identityConfig);

  /**
   * Get the identity configuration.
   *
   * @return The identity configuration as a list of authn domain groups.
   */
  public List<AuthnDomainGroup> getAuthnDomainGroups() throws IOException;

  /**
   * Attempt to find a cookie that can be converted to a verified identity.
   *
   * @param identity The authn identity containing the cookies to try.
   */
  public void handleCookie(SecAuthnIdentity identity);

  /**
   * Attempts to authenticate a given CredentialsGroup.  This method will update
   * the provided credentialsGroup with information retrieved during the
   * authentication process (i.e. cookies, certificates, and other credentials),
   * and it may set this credentialsGroup as verified as a result.
   *
   * @param credentialsGroup The credentials group to authenticate.
   */
  public void authenticate(CredentialsGroup credentialsGroup);

  /**
   * Process a set of SAML authorization queries.
   *
   * @param authzDecisionQueries A list of authorization queries to be processed.
   * @return A list of responses, corresponding to the argument.
   */
  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries);

  /**
   * Update the GSA session manager with the identity information we've collected.
   *
   * @param sessionId The session manager ID to associate the information with.
   * @param cgs The set of identity information to be associated.
   */
  public void updateSessionManager(String sessionId, Collection<CredentialsGroup> cgs);
}
