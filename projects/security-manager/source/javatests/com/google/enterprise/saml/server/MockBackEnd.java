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
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;

import java.util.List;

/**
 * Simple mock saml server Backend for testing.
 */
public class MockBackEnd implements BackEnd {
  
  private final SessionManagerInterface sessionManager;
  private ArtifactResolver artifactResolver;
  private AuthzResponder authzResponder;

  public MockBackEnd(SessionManagerInterface sm, ArtifactResolver artifactResolver,
      AuthzResponder authzResponder) {
    this.sessionManager = sm;
    this.artifactResolver = artifactResolver;
    this.authzResponder = authzResponder;
  }

  public SessionManagerInterface getSessionManager() {
    return sessionManager;
  }

  public ArtifactResolver getArtifactResolver() {
    return artifactResolver;
  }

  public AuthzResponder getAuthzResponder() {
    return authzResponder;
  }

  public SAMLArtifactMap getArtifactMap() {
    throw new UnsupportedOperationException("Unimplemented method.");
  }

  public Response validateCredentials(AuthnRequest request, String username, String password) {
    throw new UnsupportedOperationException("Unimplemented method.");
  }

  public ArtifactResponse resolveArtifact(ArtifactResolve artifactResolve) {
    throw new UnsupportedOperationException("Unimplemented method.");
  }

  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    throw new UnsupportedOperationException("Unimplemented method.");
  }
}
