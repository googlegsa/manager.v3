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

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.SecAuthnContext;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.security.identity.AuthnDomainGroup;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.IdentityConfig;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Simple mock saml server Backend for testing.
 */
public class MockBackEnd implements BackEnd {
  //private static final Logger LOGGER = Logger.getLogger(MockBackEnd.class.getName());
  private static final int artifactLifetime = 600000;  // ten minutes

  private final SessionManagerInterface sessionManager;
  private final SAMLArtifactMap artifactMap;
  private IdentityConfig identityConfig;
  private List<AuthnDomainGroup> authnDomainGroups;

  /**
   * Create a new backend object.
   * 
   * @param sm The session manager to use.
   * @param authzResponder The authorization responder to use.
   */
  public MockBackEnd(SessionManagerInterface sm, AuthzResponder authzResponder) {
    this.sessionManager = sm;
    artifactMap = new BasicSAMLArtifactMap(
        new BasicParserPool(),
        new MapBasedStorageService<String, SAMLArtifactMapEntry>(),
        artifactLifetime);
    authnDomainGroups = null;
  }

  public SessionManagerInterface getSessionManager() {
    return sessionManager;
  }

  public SAMLArtifactMap getArtifactMap() {
    return artifactMap;
  }

  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    throw new UnsupportedOperationException("Unimplemented method.");
  }

  public void updateSessionManager(String sessionId, Collection<CredentialsGroup> cgs) {
  }

  public void setIdentityConfig(IdentityConfig identityConfig) {
    this.identityConfig = identityConfig;
  }

  public List<AuthnDomainGroup> getAuthnDomainGroups() throws IOException {
    if (authnDomainGroups == null) {
      authnDomainGroups = ImmutableList.copyOf(identityConfig.getConfig());
    }
    return authnDomainGroups;
  }

  public AuthenticationResponse handleCookie(SecAuthnContext context) {
    return null;
  }

  public void authenticate(CredentialsGroup credentialsGroup) {
  }

  public void setConnectorManager(ConnectorManager cm) {
  }
}
