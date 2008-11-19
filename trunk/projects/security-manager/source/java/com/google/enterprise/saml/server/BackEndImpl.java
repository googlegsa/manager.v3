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
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.util.List;

import static com.google.enterprise.saml.common.OpenSamlUtil.GSA_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.SM_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeArtifactResolutionService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeAssertionConsumerService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeEntityDescriptor;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIdpSsoDescriptor;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSingleSignOnService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSpSsoDescriptor;

import static org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

/**
 * An implementation of the BackEnd interface for the Security Manager.
 */
public class BackEndImpl implements BackEnd {
  private static final int artifactLifetime = 600000;  // ten minutes

  private final SessionManagerInterface sm;
  private final AuthzResponder authzResponder;
  private final SAMLArtifactMap artifactMap;
  private final EntityDescriptor smEntity;
  private final EntityDescriptor gsaEntity;

  // TODO(cph): The metadata doesn't belong in the back end.

  /**
   * Create a new backend object.
   * 
   * @param sm The session manager to use.
   * @param authzResponder The authorization responder to use.
   */
  public BackEndImpl(SessionManagerInterface sm, AuthzResponder authzResponder,
                     String acsUrl, String ssoUrl, String arUrl) {
    this.sm = sm;
    this.authzResponder = authzResponder;
    artifactMap = new BasicSAMLArtifactMap(
        new BasicParserPool(),
        new MapBasedStorageService<String, SAMLArtifactMapEntry>(),
        artifactLifetime);

    // Build metadata for security manager
    smEntity = makeEntityDescriptor(SM_ISSUER);
    IDPSSODescriptor idp = makeIdpSsoDescriptor(smEntity);
    makeSingleSignOnService(idp, SAML2_REDIRECT_BINDING_URI, ssoUrl);
    makeArtifactResolutionService(idp, SAML2_SOAP11_BINDING_URI, arUrl).setIsDefault(true);

    // Build metadata for GSA
    gsaEntity = makeEntityDescriptor(GSA_ISSUER);
    SPSSODescriptor sp = makeSpSsoDescriptor(gsaEntity);
    makeAssertionConsumerService(sp, SAML2_ARTIFACT_BINDING_URI, acsUrl).setIsDefault(true);
  }

  /** {@inheritDoc} */
  public SessionManagerInterface getSessionManager() {
    return sm;
  }

  public EntityDescriptor getSecurityManagerEntity() {
    return smEntity;
  }

  public EntityDescriptor getGsaEntity() {
    return gsaEntity;
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
