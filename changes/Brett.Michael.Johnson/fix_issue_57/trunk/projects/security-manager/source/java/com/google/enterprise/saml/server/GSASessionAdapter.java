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
import com.google.enterprise.saml.common.GsaConstants;

import java.util.List;
import java.util.Map;

/**
 * Adapter for populating the Session Manager with the keys that
 * the GSA currently expects.
 */
public class GSASessionAdapter {

  protected String verifiedUserId;
  protected List<String> groups;
  protected Map<String, String> connectorToUserName;
  protected Map<String, String> connectorToManagerName;
  protected String basicAuthUsername;
  protected String ntlmDomain;
  protected String userPassword;
  protected String userCookies;
  protected boolean krb5CredentialsAreInitialized = false;

  private SessionManagerInterface sm;

  public GSASessionAdapter(SessionManagerInterface sm) {
    this.sm = sm;
  }

  public String getGroups(String sessionId) {
    return sm.getValue(sessionId, GsaConstants.AUTHN_USER_GROUPS_KEY);
  }

  public void setGroups(String sessionId, String groupsStr) {
    sm.setValue(sessionId, GsaConstants.AUTHN_USER_GROUPS_KEY, groupsStr);
  }

  public String getVerifiedUserId(String sessionId) {
    return sm.getValue(sessionId,
        GsaConstants.AUTHN_SESSION_VERIFIED_IDENTITY_KEY);
  }

  public void setVerifiedId(String sessionId, String verifiedId) {
    sm.setValue(sessionId,
        GsaConstants.AUTHN_SESSION_VERIFIED_IDENTITY_KEY, verifiedId);
  }

  public String getUsername(String sessionId) {
    return sm.getValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
                           + GsaConstants.AuthNMechanism.BASIC_AUTH.toString()
                           + GsaConstants.AUTHN_MECH_ID);
  }

  public void setUsername(String sessionId, String username) {
    sm.setValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
        + GsaConstants.AuthNMechanism.BASIC_AUTH.toString()
        + GsaConstants.AUTHN_MECH_ID, username);
  }

  public String getDomain(String sessionId) {
    return sm.getValue(sessionId,
                GsaConstants.AUTHN_MECH_BASIC_AUTH_USER_DOMAIN_KEY);
  }

  public void setDomain(String sessionId, String userDomain) {
    sm.setValue(sessionId,
        GsaConstants.AUTHN_MECH_BASIC_AUTH_USER_DOMAIN_KEY, userDomain);
  }


  public String getPassword(String sessionId) {
    return sm.getValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
                           + GsaConstants.AuthNMechanism.BASIC_AUTH.toString()
                           + GsaConstants.AUTHN_MECH_TOKEN);
  }

  public void setPassword(String sessionId, String userpassword) {
    sm.setValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
        + GsaConstants.AuthNMechanism.BASIC_AUTH.toString()
        + GsaConstants.AUTHN_MECH_TOKEN, userpassword);
  }

  /**
   * Cookies must be serialized in the correct form using CookieUtil.
   */
  public String getCookies(String sessionId) {
    return sm.getValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
                           + GsaConstants.AuthNMechanism.FORMS_AUTH.toString()
                           + GsaConstants.AUTHN_MECH_TOKEN);
  }

  public void setCookies(String sessionId, String cookies) {
    sm.setValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
        + GsaConstants.AuthNMechanism.FORMS_AUTH.toString()
        + GsaConstants.AUTHN_MECH_TOKEN, cookies);
  }

  public void setConnectorUserInfo(String sessionId, String cuisStr) {
    sm.setValue(sessionId,
                GsaConstants.AUTHN_MECH_PREFIX
                + GsaConstants.AuthNMechanism.CONNECTORS.toString()
                + GsaConstants.AUTHN_MECH_ID, cuisStr);
  }

  // TODO(con) kerberos
}
