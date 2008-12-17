package com.google.enterprise.saml.server;

import com.google.enterprise.saml.common.GsaConstants.AuthNMechanism;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

  public static List<AuthSite> getSites(String configFile) throws IOException {
    CSVReader reader = new CSVReader(new FileReader(configFile));
    List<AuthSite> sites = new ArrayList<AuthSite>();
    String[] nextLine;
   
    // Each line is FQDN,realm,auth_method,optional_sample_login_url
    // If sample login is missing, we deduce as FQDN + realm
    while ((nextLine = reader.readNext()) != null) {
      AuthNMechanism method = null;
      if (nextLine[2].equals(AuthNMechanism.BASIC_AUTH.toString()))
        method = AuthNMechanism.BASIC_AUTH;
      if (nextLine[2].equals(AuthNMechanism.FORMS_AUTH.toString()))
        method = AuthNMechanism.FORMS_AUTH;
      sites.add(new AuthSite(nextLine[0], nextLine[1], method,
                             nextLine.length > 3 ? nextLine[3] : null));
    }
    return sites;
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
