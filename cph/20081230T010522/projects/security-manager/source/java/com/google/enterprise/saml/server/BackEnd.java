// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.saml.server;

import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.SecAuthnContext;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntityDescriptor;

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
   * @returns A session manager object.
   */
  public SessionManagerInterface getSessionManager();

  // TODO(cph): The metadata doesn't belong in the back end.

  /**
   * Get the SAML metadata description for the security manager.
   *
   * @returns An initialized SAML entity descriptor
   */
  public EntityDescriptor getSecurityManagerEntity();

  /**
   * Get the SAML metadata description for the GSA.
   *
   * @returns An initialized SAML entity descriptor
   */
  public EntityDescriptor getGsaEntity();

  /**
   * Get the SAML artifact map.
   *
   * The backend holds onto this map but doesn't use it.  The map is used by the servlets comprising
   * the SAML identity provider.
   *
   * @returns The unique artifact map for the security manager.
   */
  public SAMLArtifactMap getArtifactMap();

  public String getAuthConfigFile();

  public AuthenticationResponse handleCookie(SecAuthnContext context);

  /**
   * Attempts to authenticate a given CredentialsGroup.  This method will update
   * the provided credentialsGroup with information retrieved during the
   * authentication process (i.e. cookies, certificates, and other credentials),
   * and it may set this credentialsGroup as verified as a result.
   */
  public void authenticate(CredentialsGroup credentialsGroup);

  /**
   * Process a set of SAML authorization queries.
   * 
   * @param authzDecisionQueries A list of authorization queries to be processed.
   * @returns A list of responses, corresponding to the argument.
   */
  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries);

  public void updateSessionManager(String sessionId, Collection<CredentialsGroup> cgs);
}
