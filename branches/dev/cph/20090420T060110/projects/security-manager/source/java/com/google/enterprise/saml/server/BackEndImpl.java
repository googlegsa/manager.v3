// Copyright (C) 2008, 2009 Google Inc.
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

import com.google.common.collect.ImmutableList;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.SecAuthnContext;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;
import com.google.enterprise.security.connectors.formauth.CookieUtil;
import com.google.enterprise.security.identity.CredentialsGroup;
import com.google.enterprise.security.identity.CredentialsGroupConfig;
import com.google.enterprise.security.identity.IdentityConfig;
import com.google.enterprise.security.identity.IdentityElement;
import com.google.enterprise.security.identity.VerificationStatus;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

import org.opensaml.common.binding.artifact.BasicSAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.common.binding.artifact.SAMLArtifactMap.SAMLArtifactMapEntry;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

/**
 * An implementation of the BackEnd interface for the Security Manager.
 */
public class BackEndImpl implements BackEnd {
  private static final Logger LOGGER = Logger.getLogger(BackEndImpl.class.getName());
  private static final int artifactLifetime = 600000;  // ten minutes

  private final SessionManagerInterface sm;
  private ConnectorManager manager;
  private final AuthzResponder authzResponder;
  private final SAMLArtifactMap artifactMap;
  private IdentityConfig identityConfig;
  private List<CredentialsGroupConfig> groupsConfig;

  protected GSASessionAdapter adapter;

  // public for testing/debugging
  public Vector<String> sessionIds;

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
    identityConfig = null;
    groupsConfig = null;
    adapter = new GSASessionAdapter(sm);
    sessionIds = new Vector<String>();
  }

  public void setConnectorManager(ConnectorManager cm) {
    this.manager = cm;
  }

  public SessionManagerInterface getSessionManager() {
    return sm;
  }

  public boolean isIdentityConfigured() throws IOException {
    return !getIdentityConfiguration().isEmpty();
  }

  public void setIdentityConfig(IdentityConfig identityConfig) {
    this.identityConfig = identityConfig;
    groupsConfig = null;
  }

  public SAMLArtifactMap getArtifactMap() {
    return artifactMap;
  }

  public List<CredentialsGroupConfig> getIdentityConfiguration() throws IOException {
    if (groupsConfig == null) {
      if (identityConfig != null) {
        groupsConfig = ImmutableList.copyOf(identityConfig.getConfig());
      }
      if (groupsConfig == null) {
        groupsConfig = ImmutableList.of();
      }
    }
    return groupsConfig;
  }

  public void authenticate(CredentialsGroup cg) {
    for (IdentityElement id : cg.getElements()) {
      String expectedTypeName;
      switch (id.getMechanism()) {
        case BASIC_AUTH:
          expectedTypeName = "BasicAuthConnector";
          break;
        case FORMS_AUTH:
          expectedTypeName = "FormAuthConnector";
          break;
        case CONNECTORS:
          expectedTypeName = "ConnAuthConnector";
          break;
        default:
          continue;
      }
      for (ConnectorStatus connStatus : manager.getConnectorStatuses()) {
        if (!connStatus.getType().equals(expectedTypeName)) {
          continue;
        }
        String connectorName = connStatus.getName();
        LOGGER.info("Got security plug-in " + connectorName);
        AuthenticationResponse authnResponse = manager.authenticate(connectorName, id, null);
        if ((null != authnResponse) && authnResponse.isValid()) {
          LOGGER.info("Authn Success, credential verified: " + id.getUsername());
          // TODO(cph): should this be set to REFUTED if the result is invalid?
          id.setVerificationStatus(VerificationStatus.VERIFIED);
        }
      }
    }
  }

  // some form of authentication has already happened, the user gave us cookies,
  // see if the cookies reveal who the user is.
  public List<AuthenticationResponse> handleCookie(SecAuthnContext context) {
    List<AuthenticationResponse> result = new ArrayList<AuthenticationResponse>();
    for (ConnectorStatus connStatus: manager.getConnectorStatuses()) {
      String connType = connStatus.getType();
      if (! (connType.equals("SsoCookieIdentityConnector")
             || connType.equals("regexCookieIdentityConnector"))) {
        continue;
      }
      String connectorName = connStatus.getName();
      LOGGER.info("Got security plug-in " + connectorName);
      AuthenticationResponse authnResponse = manager.authenticate(connectorName, null, context);
      if ((authnResponse != null) && authnResponse.isValid())
        result.add(authnResponse);
    }
    return result;
  }

  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    return authzResponder.authorizeBatch(authzDecisionQueries);
  }

  public void updateSessionManager(String sessionId, Collection<CredentialsGroup> cgs) {
    LOGGER.info("Session ID: " + sessionId);
    sessionIds.add(sessionId);
    LOGGER.info("Users: ");

    Vector<Cookie> cookies = new Vector<Cookie>();

    for (CredentialsGroup cg : cgs) {
      for (IdentityElement id : cg.getElements()) {
        LOGGER.info("IdentityElement group/username: " + cg.getName() + "/" + id.getUsername());
        // This clobbers any previously stored basic auth credentials.
        // The expectation is that only one basic auth module will be active
        // at any given time, or that if multiple basic auth modules are
        // active at once, only one of them will work.
        if (AuthNMechanism.BASIC_AUTH == id.getMechanism()) {
          if (null != id.getUsername()) {
            adapter.setUsername(sessionId, id.getUsername());
          }
          if (null != id.getPassword()) {
            adapter.setPassword(sessionId, id.getPassword());
          }
          // TODO(con): currently setting the domain will break functionality
          // for most Basic Auth headrequests. Once I figure out what's going on
          // with NtlmDomains, I'll fix this.
          //  if (null != id.getAuthSite()) {
          //    adapter.setDomain(sessionId, id.getAuthSite().getHostname());
          //  }
        }
      }
      cookies.addAll(cg.getCookies());
      LOGGER.info("Adding " + cg.getCookies().size() + " cookies to SM for this identity.");
    }

    adapter.setCookies(sessionId, CookieUtil.serializeCookies(cookies));

    // TODO(con): connectors
  }
}
