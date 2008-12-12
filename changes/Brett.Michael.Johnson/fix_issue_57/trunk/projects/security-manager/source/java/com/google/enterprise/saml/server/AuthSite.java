package com.google.enterprise.saml.server;

import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

/**
 * Keep track of the Auth sites we manage.
 *
 */
public class AuthSite {
  private final String FQDN;
  private final String realm;
  private final AuthNMechanism method;
  private String loginUri; 
  
  public AuthSite(String hostname, String realm, AuthNMechanism method, String uri) {
    this.FQDN = hostname;
    this.realm = realm;
    this.method = method;
    if (uri != null && uri.length() > 0)
      this.loginUri = uri;
  }
  
  public String getHostname() {
    return FQDN;
  }
  public String getRealm() {
    return realm;
  }
  public AuthNMechanism getMethod() {
    return method;
  }
 
  public String getLoginUri() {
    if (loginUri != null) {
      return loginUri;
    }
    return FQDN + realm;
  }

}
