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
import com.google.enterprise.session.manager.CredentialsGroup;
import com.google.enterprise.session.manager.DomainCredentials;
import com.google.enterprise.session.manager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

/**
 * Simple mock saml server Backend for testing.
 */
public class MockBackEnd implements BackEnd {
  private static final Logger LOGGER = Logger.getLogger(MockBackEnd.class.getName());
  private static final int artifactLifetime = 600000;  // ten minutes

  private final SessionManagerInterface sessionManager;
  private final SAMLArtifactMap artifactMap;

  /**
   * Create a new backend object.
   * 
   * @param sm The session manager to use.
   * @param authzResponder The authorization responder to use.
   */
  public MockBackEnd(SessionManagerInterface sm, AuthzResponder authzResponder,
                     String acsUrl, String ssoUrl, String arUrl) {
    this.sessionManager = sm;
    artifactMap = new BasicSAMLArtifactMap(
        new BasicParserPool(),
        new MapBasedStorageService<String, SAMLArtifactMapEntry>(),
        artifactLifetime);
  }

  /** {@inheritDoc} */
  public SessionManagerInterface getSessionManager() {
    return sessionManager;
  }

  /** {@inheritDoc} */
  public EntityDescriptor getSecurityManagerEntity() {
    return null;
  }

  /** {@inheritDoc} */
  public EntityDescriptor getGsaEntity() {
    return null;
  }

  /** {@inheritDoc} */
  public SAMLArtifactMap getArtifactMap() {
    return artifactMap;
  }

  /** {@inheritDoc} */
  public void validateCredentials(CredentialsGroup group) {
    if (group.isVerifiable()) {
      boolean isValid = areCredentialsValid(group);
      if (isValid) {
        LOGGER.log(Level.INFO, "Authenticated successfully as " + group.getUsername());
      }
    } else {
      LOGGER.log(Level.INFO, "Authentication failed");
    }
    for (DomainCredentials credentials: group.getElements()) {
      credentials.setVerified(true);
    }
  }

  // trivial implementation
  private boolean areCredentialsValid(CredentialsGroup group) {
    return "joe".equals(group.getUsername()) && "plumber".equals(group.getPassword());
  }

  /** {@inheritDoc} */
  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    throw new UnsupportedOperationException("Unimplemented method.");
  }

  public String handleCookies(List<Cookie> cookies) {
    throw new UnsupportedOperationException("Unimplemented method.");
  }

  public void setConnectorManager(ConnectorManager cm) {
    // TODO Auto-generated method stub
    
  }

  public String getAuthConfigFile() {
    // TODO Auto-generated method stub
    return null;
  }
}
