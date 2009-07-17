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

import com.google.enterprise.connector.security.identity.CredentialsGroup;
import com.google.enterprise.connector.security.identity.IdentityConfig;
import com.google.enterprise.connector.spi.AuthenticationIdentity;

import org.opensaml.common.binding.artifact.SAMLArtifactMap;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
   * @throws IllegalStateException if no identity configuration has been set.
   */
  public IdentityConfig getIdentityConfig();

  /**
   * Inject the identity configuration source.
   *
   * @param identityConfig The identity configuration to use.
   */
  public void setIdentityConfig(IdentityConfig identityConfig);

  /**
   * Get the prompt limit.
   *
   * This is the number of times the back end will prompt the user before giving up.
   *
   * @return The prompt limit.
   */
  public int getMaxPrompts();

  /**
   * Set the prompt limit.
   *
   * This is the number of times the back end will prompt the user before giving up.
   *
   * @param maxPrompts The new prompt limit.
   */
  public void setMaxPrompts(int maxPrompts);

  /**
   * Get the credentials groups for a session.
   *
   * @param request An HTTP request to identity the session.
   * @return A list of the credentials groups for that session.
   * @throws IOException
   */
  public List<CredentialsGroup> getCredentialsGroups(HttpServletRequest request)
      throws IOException;

  /**
   * Perform the authentication process for the security manager.
   *
   * This method may be called more than once before authentication is finished, so it
   * must be able to figure out what it needs to do when called.
   *
   * @param request The current HTTP request.
   * @param response The HTTP response to fill in before returning.
   * @throws IOException
   */
  public void authenticate(HttpServletRequest request, HttpServletResponse response)
      throws IOException;

  /**
   * Gets authorization for a set of documents by ID.
   *
   * @param docidList The document set represented as a list of Strings: the
   *        docid for each document
   * @param identity An AuthenticationIdentity object that encapsulates the
   *        user's identity
   * @return A Set of String IDs indicating which documents the user can see.
   */
  public Set<String> authorizeDocids(List<String> docidList, AuthenticationIdentity identity);
}
