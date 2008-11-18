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

import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;

import java.util.List;

/**
 * Interface to SAML server backend. Top-level classes such as servlets should
 * do transport and marshaling only, then interact with an instance of this
 * class.
 * 
 */
public interface BackEnd {

  /**
   * Get the session manager used by this backend.
   *
   * This may migrate to the next inner layer.
   *
   * @returns A session manager object.
   */
  public SessionManagerInterface getSessionManager();

  /**
   * Validate username/password credentials.
   *
   * @param request The SAML authentication request being served.
   * @param username The username credential.
   * @param password The password credential.
   * @returns A SAML Response with the validation result.
   */
  public Response validateCredentials(AuthnRequest request, String username, String password);

  /**
   * Get the SAML artifact map.
   *
   * The backend holds onto this map but doesn't use it.  The map is used by the servlets comprising
   * the SAML identity provider.
   * 
   * @returns The unique artifact map for the security manager.
   */
  public SAMLArtifactMap getArtifactMap();

  /**
   * Process a set of SAML authorization queries.
   * 
   * @param authzDecisionQueries A list of authorization queries to be processed.
   * @returns A list of responses, corresponding to the argument.
   */
  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries);
}
