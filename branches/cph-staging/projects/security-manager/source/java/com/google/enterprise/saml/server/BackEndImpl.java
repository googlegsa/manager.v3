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

import com.google.enterprise.saml.common.GsaConstants;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Logger;

/**
 * The implementation of the BackEnd interface for the Security Manager.
 *
 * At present, this implementation is very shallow and returns canned responses
 * for almost everything.
 */
public class BackEndImpl implements BackEnd {
  private static final Logger LOGGER =
      Logger.getLogger(BackEndImpl.class.getName());

  private SessionManagerInterface sm;
  private ArtifactResolver artifactResolver;
  private AuthzResponder authzResponder;
  private ArtifactStore artifacts;

  public BackEndImpl() {
    artifacts = new ArtifactStore();
  }
  public void setSessionManager(SessionManagerInterface sm) {
    this.sm = sm;
  }

  public SessionManagerInterface getSessionManager() {
    return sm;
  }

  public void setArtifactResolver(ArtifactResolver artifactResolver) {
    this.artifactResolver = artifactResolver;
  }

  public void setAuthzResponder(AuthzResponder authzResponder) {
    this.authzResponder = authzResponder;
  }

  public String loginRedirect(String referer, String relayState, String subject) {
    String urlEncodedRelayState = "";
    try {
      urlEncodedRelayState = URLEncoder.encode(relayState, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      LOGGER.warning("Could not encode RelayState");
    }

    String artifact = "foo";
    artifacts.put(artifact, subject);
    String redirectUrl = referer + GsaConstants.GSA_ARTIFACT_HANDLER_NAME
        + "?" + GsaConstants.GSA_ARTIFACT_PARAM_NAME + "=" + artifact
        + "&" + GsaConstants.GSA_RELAY_STATE_PARAM_NAME + "=" + urlEncodedRelayState;

    LOGGER.info("Referer: " + referer);
    LOGGER.info("RelayState: " + relayState);
    LOGGER.info("URLEncoded RelayState: " + urlEncodedRelayState);
    LOGGER.info("Redirect URL: " + redirectUrl);

    return redirectUrl;
  }

  public ArtifactResponse resolveArtifact(ArtifactResolve artifactResolve) {
    return artifactResolver.resolve(artifactResolve, artifacts);
  }

  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    return authzResponder.authorizeBatch(authzDecisionQueries);
  }
}
