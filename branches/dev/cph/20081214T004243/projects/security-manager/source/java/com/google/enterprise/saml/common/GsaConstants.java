// Copyright 2008 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.saml.common;

/**
 * Container class for GSA-specific contents related to SAML communication.
 */
public class GsaConstants {

  public static final String GSA_ARTIFACT_HANDLER_NAME =
      "SamlArtifactConsumer";
  public static final String GSA_ARTIFACT_PARAM_NAME = "SAMLart";
  public static final String GSA_RELAY_STATE_PARAM_NAME = "RelayState";

  public static enum AuthNMechanism {
    BASIC_AUTH,     // aka SekuLite
    FORMS_AUTH,     // was known as (non-SAML) "SSO"
    SAML,           // the SAML SPI
    SSL,            // client-side x.509 certificate authN
    CONNECTORS,     // connector manager authN logic
    SPNEGO_KERBEROS // GSSAPI/SPNEGO/Kerberos WWW-Authenticate handshake
  }

  public static enum AuthNDecision {
    TBD,            // haven't gone through verification yet
    VERIFIED        // recognized by one IdP
  }
  
  // name of the cookie that we store the sessionId in
  // (package exposed for access by unit tests)
  public static final String AUTHN_SESSION_ID_COOKIE_NAME = "GSA_SESSION_ID";

  // Cookie serialization constants.
  public static final String COOKIE_FIELD_SEPERATOR = "====";
  public static final String COOKIE_RECORD_SEPARATOR = "::::";

  // ----------
  // session manager key strings

  /**
   * time the session will expire (in millis since epoch)
   * (this is set to the minimum mechanism-specific expiration time,
   *  or [perhaps] to a system-wide max, whichever comes first)
   */
  public static final String AUTHN_SESSION_EXPIRE_TIME_KEY = "AuthN-expireTime";

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
  public static final String AUTHN_SESSION_VERIFIED_IDENTITY_KEY =
      "AuthN-verified-id";

  /**
   * CSV list of groups the user is confirmed to be in.  Yet to be worked out is
   * how to handle if the user belongs to multiple sets of groups
   * from different providers which may not have distinct name-spaces.
   */
  public static final String AUTHN_USER_GROUPS_KEY = "AuthN-userGroups";

  // AuthN mechanisms specific constants
  //
  // construct SM keys by concatenating the prefix, mechanism, and field
  // e.g.
  // String pswdKey = AUTHN_MECH_PREFIX +
  //   AuthNMechanism.BASIC_AUTH.toString() + AUTHN_MECH_TOKEN;
  // String basicAuthPassword = sessionManager.get(sessionId, pswdKey);

  public static final String AUTHN_MECH_PREFIX = "AuthN-Mech-";

   /**
   * this key indicates the status of a particular mechanism.
   * see below for allowed values (e.g. AUTHN_SESSION_STARTING)
   */
  public static final String AUTHN_MECH_STATUS = "_Status";

  /**
   * this is the identity returned by an AuthN mechansim;  it's format depends
   * on the mechanism.  for example, for BASIC_AUTH, this will be the username
   * the user entered.  For SSL, this will be the X.509 DN in the cert.
   */
  public static final String AUTHN_MECH_ID = "_Id";

  /**
   * this boolean value (valid strings are AUTHN_ID_WAS_VALIDATED and
   * AUTHN_ID_WAS_NOT_VALIDATED) indicates whether the AUTHN_MECH_ID was
   * vaidated by the method and can be trusted without futher checking
   */
  public static final String AUTHN_MECH_ID_VERIFIED = "_Id_verified";

  /**
   * this in the time (in millis) that the above AUTHN_MECH_ID_VERIFIED expires
   *
   */
  public static final String AUTHN_MECH_EXPIRES = "_Expires";

  /**
   *
   */
  public static final String AUTHN_MECH_TOKEN = "_Token";

  // special fields appearing only in one particular AuthN mechanism:

  public static final String AUTHN_MECH_BASIC_AUTH_USER_DOMAIN_KEY =
      AUTHN_MECH_PREFIX + AuthNMechanism.BASIC_AUTH.toString() + "_USER_DOMAIN";
}
