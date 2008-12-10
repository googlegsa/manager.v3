package com.google.enterprise.saml.server;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.saml.common.GsaConstants.AuthNDecision;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.Cookie;

/**
 * Keep track of the user identities we manage.
 *
 */
public class UserIdentity implements AuthenticationIdentity {

  private final String username;
  private final String password;
  private AuthSite site;
  private AuthNDecision status;
  
  private final Vector<Cookie> cookieJar;

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public UserIdentity(final String username, final String password, final AuthSite site) {
    this.username = username;
    this.password = password;
    this.site = site;
    cookieJar = new Vector<Cookie>();
  }

  public String getCookie(String cookieName) {
    if (cookieName == null || cookieName.length() < 1) {
      throw new IllegalArgumentException();
    }
    for (Cookie c : cookieJar) {
      if (cookieName.equals(c.getName()))
        return c.getValue();
    }
    return null;
  }

  public String setCookie(String cookieName, String value) {
    String oldVal = getCookie(cookieName);
    Cookie c = new Cookie(cookieName, value);
    setCookie(c);
    return oldVal;
  }
  
  public void setCookie(Cookie c) {
    cookieJar.add(c);
  }

  @SuppressWarnings("unchecked")
  public Set getCookieNames() {
    Set<String> result = new HashSet<String>();
    for (Cookie c : cookieJar) {
      result.add(c.getName());
    }
    return result;    
  }

  public Vector<Cookie> getCookies() {
    return this.cookieJar;
  }
  public void setAuthSite(AuthSite site) {
    this.site = site;
  }
  public AuthSite getAuthSite() {
    return this.site;
  }
  
  public String getLoginUrl() {
    return site.getLoginUri();
  }
  
  public void setVerified() {
    status = AuthNDecision.VERIFIED;
  }
  public boolean isVerified() {
    return(status == AuthNDecision.VERIFIED);
  }
}
