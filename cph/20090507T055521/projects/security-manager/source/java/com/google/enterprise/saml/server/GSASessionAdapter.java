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

import com.google.enterprise.security.identity.AuthnMechanism;
import com.google.enterprise.sessionmanager.SessionManagerInterface;

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
                           + AuthnMechanism.BASIC_AUTH.toString()
                           + GsaConstants.AUTHN_MECH_ID);
  }

  public void setUsername(String sessionId, String username) {
    sm.setValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
        + AuthnMechanism.BASIC_AUTH.toString()
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
                           + AuthnMechanism.BASIC_AUTH.toString()
                           + GsaConstants.AUTHN_MECH_TOKEN);
  }

  public void setPassword(String sessionId, String userpassword) {
    sm.setValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
        + AuthnMechanism.BASIC_AUTH.toString()
        + GsaConstants.AUTHN_MECH_TOKEN, userpassword);
  }

  /**
   * Cookies must be serialized in the correct form using CookieUtil.
   */
  public String getCookies(String sessionId) {
    return sm.getValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
                           + AuthnMechanism.FORMS_AUTH.toString()
                           + GsaConstants.AUTHN_MECH_TOKEN);
  }

  public void setCookies(String sessionId, String cookies) {
    sm.setValue(sessionId, GsaConstants.AUTHN_MECH_PREFIX
        + AuthnMechanism.FORMS_AUTH.toString()
        + GsaConstants.AUTHN_MECH_TOKEN, cookies);
  }

  public void setConnectorUserInfo(String sessionId, String cuisStr) {
    sm.setValue(sessionId,
                GsaConstants.AUTHN_MECH_PREFIX
                + AuthnMechanism.CONNECTORS.toString()
                + GsaConstants.AUTHN_MECH_ID, cuisStr);
  }

  // TODO(con) kerberos
  
  /**
   * Container class for GSA-specific contents related to SAML communication.
   */
  private static class GsaConstants {

    // ----------
    // session manager key strings

    /**
     * time the session will expire (in millis since epoch)
     * (this is set to the minimum mechanism-specific expiration time,
     *  or [perhaps] to a system-wide max, whichever comes first)
     */
    @SuppressWarnings("unused")   // TODO: Decide whether to get rid of this.
    private static final String AUTHN_SESSION_EXPIRE_TIME_KEY = "AuthN-expireTime";

    /**
     * this contains the "highest-priority" verified user identity from the
     * mechanisms that were run.  This value is passed downstream and
     * is eventually used in the SAML AuthZ SPI, connector AuthZ calls, etc.
     * The AuthN class guarantees that only identities that have been verified
     * against some external source are placed here.
     *
     * "priority" was defined by product management, and is coded by the order
     * in which the AuthN mechanisms are called.
     */
    private static final String AUTHN_SESSION_VERIFIED_IDENTITY_KEY =
        "AuthN-verified-id";

    /**
     * CSV list of groups the user is confirmed to be in.  Yet to be worked out is
     * how to handle if the user belongs to multiple sets of groups
     * from different providers which may not have distinct name-spaces.
     */
    private static final String AUTHN_USER_GROUPS_KEY = "AuthN-userGroups";

    // AuthN mechanisms specific constants
    //
    // construct SM keys by concatenating the prefix, mechanism, and field
    // e.g.
    // String pswdKey = AUTHN_MECH_PREFIX +
    //   AuthnMechanism.BASIC_AUTH.toString() + AUTHN_MECH_TOKEN;
    // String basicAuthPassword = sessionManager.get(sessionId, pswdKey);

    private static final String AUTHN_MECH_PREFIX = "AuthN-Mech-";

     /**
     * this key indicates the status of a particular mechanism.
     * see below for allowed values (e.g. AUTHN_SESSION_STARTING)
     */
    @SuppressWarnings("unused")   // TODO: Decide whether to get rid of this.
    private static final String AUTHN_MECH_STATUS = "_Status";

    /**
     * this is the identity returned by an AuthN mechansim;  it's format depends
     * on the mechanism.  for example, for BASIC_AUTH, this will be the username
     * the user entered.  For SSL, this will be the X.509 DN in the cert.
     */
    private static final String AUTHN_MECH_ID = "_Id";

    /**
     * this boolean value (valid strings are AUTHN_ID_WAS_VALIDATED and
     * AUTHN_ID_WAS_NOT_VALIDATED) indicates whether the AUTHN_MECH_ID was
     * vaidated by the method and can be trusted without futher checking
     */
    @SuppressWarnings("unused")   // TODO: Decide whether to get rid of this.
    private static final String AUTHN_MECH_ID_VERIFIED = "_Id_verified";

    /**
     * this in the time (in millis) that the above AUTHN_MECH_ID_VERIFIED expires
     *
     *  TODO: this is unused.  Should decide whether to get rid of it.
     */
    @SuppressWarnings("unused")   // TODO: Decide whether to get rid of this.
    private static final String AUTHN_MECH_EXPIRES = "_Expires";

    /**
     *
     */
    private static final String AUTHN_MECH_TOKEN = "_Token";

    // special fields appearing only in one particular AuthN mechanism:

    private static final String AUTHN_MECH_BASIC_AUTH_USER_DOMAIN_KEY =
        AUTHN_MECH_PREFIX + AuthnMechanism.BASIC_AUTH.toString() + "_USER_DOMAIN";
  }
}
