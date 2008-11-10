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

import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.enterprise.saml.common.OpenSamlUtil.GOOGLE_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertion;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAuthnStatement;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIssuer;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeResponse;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatus;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeStatusMessage;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSubject;

/**
 * The implementation of the BackEnd interface for the Security Manager.
 *
 * At present, this implementation is very shallow and returns canned responses
 * for almost everything.
 */
public class BackEndImpl implements BackEnd {
  private static final Logger LOGGER = Logger.getLogger(BackEndImpl.class.getName());
  private static final int artifactLifetime = 600000;  // ten minutes

  private final SessionManagerInterface sm;
  private final ArtifactResolver artifactResolver;
  private final AuthzResponder authzResponder;
  private final SAMLArtifactMap artifactMap;

  public BackEndImpl(SessionManagerInterface sm, ArtifactResolver artifactResolver, AuthzResponder authzResponder) {
    this.sm = sm;
    this.artifactResolver = artifactResolver;
    this.authzResponder = authzResponder;
    artifactMap = new BasicSAMLArtifactMap(
        new BasicParserPool(),
        new MapBasedStorageService<String, SAMLArtifactMapEntry>(),
        artifactLifetime);
  }

  public SessionManagerInterface getSessionManager() {
    return sm;
  }

  public Response validateCredentials(AuthnRequest request, String username, String password) {
    Status status = makeStatus();
    Response response = makeResponse(request, status);
    if (areCredentialsValid(username, password)) {
      LOGGER.log(Level.INFO, "Authenticated successfully as " + username);
      status.getStatusCode().setValue(StatusCode.SUCCESS_URI);
      Assertion assertion = makeAssertion(makeIssuer(GOOGLE_ISSUER), makeSubject(username));
      assertion.getAuthnStatements().add(makeAuthnStatement(AuthnContext.IP_PASSWORD_AUTHN_CTX));
      response.getAssertions().add(assertion);
    } else {
      LOGGER.log(Level.INFO, "Authentication failed");
      status.getStatusCode().setValue(StatusCode.REQUEST_DENIED_URI);
      status.setStatusMessage(makeStatusMessage("Authentication failed"));
    }
    return response;
  }

  private boolean areCredentialsValid(String username, String password) {
    return "joe".equals(username) && "plumber".equals(password);
  }

  public SAMLArtifactMap getArtifactMap() {
    return artifactMap;
  }

  public ArtifactResponse resolveArtifact(ArtifactResolve artifactResolve) {
    return artifactResolver.resolve(artifactResolve);
  }

  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    return authzResponder.authorizeBatch(authzDecisionQueries);
  }

  public Manager getConnectorManager() {
    // TODO Auto-generated method stub
    return null;
  }
}
