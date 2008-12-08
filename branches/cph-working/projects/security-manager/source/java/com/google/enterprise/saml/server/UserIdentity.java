package com.google.enterprise.saml.server;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.session.metadata.AuthnDomainMetadata.AuthnMechanism;
import com.google.enterprise.session.object.AuthnDomainCredentials;
import com.google.enterprise.session.object.SessionCookie;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.Cookie;

/**
 * Keep track of the user identities we manage.
 *
 */
public class UserIdentity implements AuthenticationIdentity {

  private final AuthnDomainCredentials credentials;

  public UserIdentity(AuthnDomainCredentials credentials) {
    this.credentials = credentials;
  }

  /** {@inheritDoc} */
  public String getUsername() {
    return credentials.getUsername();
  }

  /** {@inheritDoc} */
  public String getPassword() {
    return credentials.getPassword();
  }

  /** {@inheritDoc} */
  public String getCookie(String cookieName) {
    if (cookieName == null || cookieName.length() < 1) {
      throw new IllegalArgumentException();
    }
    for (SessionCookie c: credentials.getCookies()) {
      if (cookieName.equals(c.getName()))
        return c.getValue();
    }
    return null;
  }

  /** {@inheritDoc} */
  public String setCookie(String cookieName, String value) {
    SessionCookie sc = credentials.getCookie(cookieName);
    String oldValue = sc.getValue();
    sc.setValue(value);
    return oldValue;
  }

  /** {@inheritDoc} */
  public void setCookie(Cookie c) {
    SessionCookie sc = credentials.getCookie(c.getName());
    sc.setValue(c.getValue());
    sc.setDomain(c.getDomain());
    sc.setPath(c.getPath());
    sc.setComment(c.getComment());
    sc.setSecure(c.getSecure());
    sc.setMaxAge(c.getMaxAge());
    sc.setVersion(c.getVersion());
  }

  @SuppressWarnings("unchecked")
  /** {@inheritDoc} */
  public Set getCookieNames() {
    Set<String> names = new HashSet<String>();
    for (SessionCookie c: credentials.getCookies()) {
      names.add(c.getName());
    }
    return names;
  }

  /** {@inheritDoc} */
  public String getLoginUrl() {
    return credentials.getMetadata().getLoginUrl();
  }
  
  public void setVerified() {
    credentials.setVerified(true);
  }

  public boolean isVerified() {
    return credentials.isVerified();
  }

  public AuthnMechanism getMechanism() {
    return credentials.getMetadata().getMechanism();
  }
}
