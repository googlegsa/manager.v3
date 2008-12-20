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

import com.google.common.collect.ImmutableMap;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.manager.ConnectorManager;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.persist.PersistentStoreException;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;
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
import org.opensaml.util.storage.MapBasedStorageService;
import org.opensaml.xml.parse.BasicParserPool;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import static com.google.enterprise.saml.common.OpenSamlUtil.GSA_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.SM_ISSUER;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeArtifactResolutionService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeEntityDescriptor;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeIdpSsoDescriptor;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSingleSignOnService;
import static com.google.enterprise.saml.common.OpenSamlUtil.makeSpSsoDescriptor;
import com.google.enterprise.security.connectors.formauth.CookieUtil;

import static org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;
import static org.opensaml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

import javax.servlet.http.Cookie;

/**
 * An implementation of the BackEnd interface for the Security Manager.
 */
public class BackEndImpl implements BackEnd {
  private static final int artifactLifetime = 600000;  // ten minutes

  private final SessionManagerInterface sm;
  private ConnectorManager manager;
  private final AuthzResponder authzResponder;
  private final SAMLArtifactMap artifactMap;
  private final EntityDescriptor smEntity;
  private final EntityDescriptor gsaEntity;
  private final String loginFormConfigFile;
  private static final Logger LOGGER = Logger.getLogger(BackEndImpl.class.getName());

  protected GSASessionAdapter adapter;

  // public for testing/debugging
  public Vector<String> sessionIds;

  // TODO(cph): The metadata doesn't belong in the back end.

  /**
   * Create a new backend object.
   * 
   * @param sm The session manager to use.
   * @param authzResponder The authorization responder to use.
   */
  public BackEndImpl(SessionManagerInterface sm, AuthzResponder authzResponder,
                     String loginFormConfigFile, String ssoUrl, String arUrl) {
    this.sm = sm;
    this.authzResponder = authzResponder;
    this.loginFormConfigFile = loginFormConfigFile;
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
    makeSpSsoDescriptor(gsaEntity);
    // SPSSODescriptor sp = makeSpSsoDescriptor(gsaEntity);
    // makeAssertionConsumerService(sp, SAML2_ARTIFACT_BINDING_URI, acsUrl).setIsDefault(true);

    adapter = new GSASessionAdapter(sm);
    sessionIds = new Vector<String>();
  }

  public void setConnectorManager(ConnectorManager cm) {
    this.manager = cm;
  }
  public SessionManagerInterface getSessionManager() {
    return sm;
  }

  public EntityDescriptor getSecurityManagerEntity() {
    return smEntity;
  }

  public EntityDescriptor getGsaEntity() {
    return gsaEntity;
  }

  public SAMLArtifactMap getArtifactMap() {
    return artifactMap;
  }

  public String getAuthConfigFile() {
    return loginFormConfigFile;
  }

  public Response validateCredentials(AuthnRequest request, UserIdentity id) {
    if (id == null || id.isVerified())
      return null;
    
    return
        (handleAuthn(id)
        ? SamlAuthn.makeSuccessfulResponse(request, id.getUsername())
        : SamlAuthn.makeUnsuccessfulResponse(
            request, StatusCode.REQUEST_DENIED_URI, "Authentication failed"));
  }

  // return true if credentials are checked against Identity provider: either valid or invalid
  private boolean handleAuthn(UserIdentity id) {
    AuthSite site = id.getAuthSite();
    
    for (ConnectorStatus connStatus: getConnectorStatuses(manager)) {
      String connectorName = connStatus.getName();
      System.out.println("Got security plug-in " + connectorName);

      // Use connectorName as a clue as to what type of auth mechanism is suitable
      if (connectorName.startsWith("Basic") && site.getMethod() != AuthNMechanism.BASIC_AUTH)
        continue;
      if (connectorName.startsWith("Form") && site.getMethod() != AuthNMechanism.FORMS_AUTH)
        continue;
      if (connectorName.startsWith("Conn") && site.getMethod() != AuthNMechanism.CONNECTORS)
        continue;
      AuthenticationResponse authnResponse =
          manager.authenticate(connectorName, id, null);
      if ((authnResponse != null) && authnResponse.isValid()) {
        id.setVerified();
        // TODO deal with cases where id.id != authnResponse.getData();
      }
    }
    return true;

  }

  // some form of authentication has already happened, the user gave us cookies,
  // see if the cookies reveal who the user is.
  public AuthenticationResponse handleCookie(Map<String, String> cookieJar) {
    for (ConnectorStatus connStatus: getConnectorStatuses(manager)) {
      String connectorName = connStatus.getName();
      LOGGER.info("Got security plug-in " + connectorName);

      // Use connectorName as a clue as to what type of auth mechanism is suitable
      if (!connectorName.startsWith("FORM-"))
        continue;
      AuthenticationResponse authnResponse =
          manager.authenticate(connectorName, null, cookieJar);
      if ((authnResponse != null) && authnResponse.isValid())
        return authnResponse;
    }
    
    return null;
  }

  public List<Response> authorize(List<AuthzDecisionQuery> authzDecisionQueries) {
    return authzResponder.authorizeBatch(authzDecisionQueries);
  }
  
  @SuppressWarnings("unchecked")
  private List<ConnectorStatus> getConnectorStatuses(ConnectorManager manager) {
    List<ConnectorStatus> connList = manager.getConnectorStatuses();
    if (connList == null || connList.isEmpty()) {
      instantiateConnector(manager);
      connList = manager.getConnectorStatuses();
    }
    return connList;
  }

  public void updateSessionManager(String sessionId, UserIdentity[] ids) {    
    LOGGER.info("Session ID: " + sessionId);
    sessionIds.add(sessionId);
    LOGGER.info("Users: ");

    Vector<Cookie> cookies = new Vector<Cookie>();

    for (UserIdentity id : ids) {
      if (null == id) {
        LOGGER.warning("null id");
        continue;
      }
      LOGGER.info("user id to sm: " + id.toString());

      // This clobbers any priorly stored basic auth credentials.
      // The expectation is that only one basic auth module will be active
      // at any given time, or that if multiple basic auth modules are
      // active at once, only one of them will work.
      if (AuthNMechanism.BASIC_AUTH == id.getAuthSite().getMethod()) {
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

      cookies.addAll(id.getCookies());     
    }

    adapter.setCookies(sessionId, CookieUtil.serializeCookies(cookies));

    // TODO(con): connectors
  }
  
  // TODO get rid of this when we have a way of configuring plug-ins
  private void instantiateConnector(ConnectorManager manager) {
    String connectorName = "FormLei";
    String connectorType = "CookieConnector";
    String language = "en";

    Map<String, String> configData =
        ImmutableMap.of(
            "CookieName", "SMSESSION",
            "ServerUrl", "http://gama.corp.google.com/user1/ssoAgent.asp",
            "HttpHeaderName", "User-Name");
    Map<String, String> configBasicAuth = ImmutableMap.of(
            "ServerUrl", "http://leiz.mtv.corp.google.com/basic/");
    Map<String, String> configFormAuth = ImmutableMap.of(
            "CookieName", "SMSESSION");
    Map<String, String> configConnAuth = ImmutableMap.of(
            "SpiVersion", "0");
    try {
      manager.setConnectorConfig(connectorName, connectorType,
                                 configData, language, false);
      manager.setConnectorConfig("BasicAuth", "BasicAuthConnector",
                                 configBasicAuth, language, false);
      manager.setConnectorConfig("FormAuth", "FormAuthConnector",
      		                     configFormAuth, language, false);
      manager.setConnectorConfig("ConnAuth", "ConnAuthConnector",
                                 configConnAuth, language, false);
    } catch (ConnectorNotFoundException e) {
      LOGGER.info("ConnectorNotFound: " + e.toString());
    } catch (InstantiatorException e) {
      LOGGER.info("Instantiator: " + e.toString());
    } catch (PersistentStoreException e) {
      LOGGER.info("PersistentStore: " + e.toString());
    }
  }

}
