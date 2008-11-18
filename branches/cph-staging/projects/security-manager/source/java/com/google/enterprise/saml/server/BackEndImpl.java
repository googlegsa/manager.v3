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

import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.util.List;

/**
 * An implementation of the BackEnd interface for the Security Manager.
 */
public class BackEndImpl implements BackEnd {
  private static final int artifactLifetime = 600000;  // ten minutes

  private final SessionManagerInterface sm;
  private final AuthzResponder authzResponder;
  private final SAMLArtifactMap artifactMap;

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
  }

  /** {@inheritDoc} */
  public SessionManagerInterface getSessionManager() {
    return sm;
  }

  /** {@inheritDoc} */
  public Response validateCredentials(AuthnRequest request, String username, String password) {
    return
        (areCredentialsValid(username, password))
        ? SamlAuthn.makeSuccessfulResponse(request, username)
        : SamlAuthn.makeUnsuccessfulResponse(
            request, StatusCode.REQUEST_DENIED_URI, "Authentication failed");
  }

  // TODO(cph): replace this with something real.
  private boolean areCredentialsValid(String username, String password) {
    return "joe".equals(username) && "plumber".equals(password);
  }

  /** {@inheritDoc} */
  public SAMLArtifactMap getArtifactMap() {
    return artifactMap;
  }

  /** {@inheritDoc} */
  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    return authzResponder.authorizeBatch(authzDecisionQueries);
  }
}
