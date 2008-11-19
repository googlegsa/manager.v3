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

import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.enterprise.saml.common.OpenSamlUtil.SM_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertion;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAuthnStatement;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatusMessage;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSubject;

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
  public MockBackEnd(SessionManagerInterface sm, AuthzResponder authzResponder) {
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
  public Response validateCredentials(AuthnRequest request, String username, String password) {
    Status status = makeStatus();
    Response response = makeResponse(request, status);
    if (areCredentialsValid(username, password)) {
      LOGGER.log(Level.INFO, "Authenticated successfully as " + username);
      status.getStatusCode().setValue(StatusCode.SUCCESS_URI);
      Assertion assertion = makeAssertion(makeIssuer(SM_ISSUER), makeSubject(username));
      assertion.getAuthnStatements().add(makeAuthnStatement(AuthnContext.IP_PASSWORD_AUTHN_CTX));
      response.getAssertions().add(assertion);
    } else {
      LOGGER.log(Level.INFO, "Authentication failed");
      status.getStatusCode().setValue(StatusCode.REQUEST_DENIED_URI);
      status.setStatusMessage(makeStatusMessage("Authentication failed"));
    }
    return response;
  }

  // trivial implementation
  private boolean areCredentialsValid(String username, String password) {
    return "joe".equals(username) && "plumber".equals(password);
  }

  /** {@inheritDoc} */
  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    throw new UnsupportedOperationException("Unimplemented method.");
  }
}
