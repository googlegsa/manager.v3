package com.google.enterprise.saml.server;

import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.sessionmanager.AuthnDomain;
import com.google.enterprise.sessionmanager.AuthnDomainGroup;
import com.google.enterprise.sessionmanager.AuthnMechanism;
import com.google.enterprise.sessionmanager.CredentialsGroup;
import com.google.enterprise.sessionmanager.DomainCredentials;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.Cookie;

/**
 * Keep track of the user identities we manage.
 *
 */
public class UserIdentity implements AuthenticationIdentity {

  private final DomainCredentials credentials;

  public UserIdentity(DomainCredentials credentials) {
    this.credentials = credentials;
  }

  public static UserIdentity compatNew(
      String username, String password, AuthnDomain domain) {
    if (domain == null) {
      domain = new AuthnDomain("foo", AuthnMechanism.FORMS_AUTH, new AuthnDomainGroup("Foo"));
    }
    CredentialsGroup group = new CredentialsGroup(domain.getGroup());
    UserIdentity id = new UserIdentity(new DomainCredentials(domain, group));
    group.setUsername(username);
    group.setPassword(password);
    return id;
  }

  public DomainCredentials getCredentials() {
    return credentials;
  }

  /** {@inheritDoc} */
  public String getUsername() {
    return credentials.getGroup().getUsername();
  }

  /** {@inheritDoc} */
  public String getPassword() {
    return credentials.getGroup().getPassword();
  }

  /** {@inheritDoc} */
  public String getCookie(String cookieName) {
    if (cookieName == null || cookieName.length() < 1) {
      throw new IllegalArgumentException();
    }
    for (Cookie c: credentials.getCookies()) {
      if (cookieName.equals(c.getName()))
        return c.getValue();
    }
    return null;
  }

  /** {@inheritDoc} */
  public String setCookie(String cookieName, String value) {
    Cookie sc = credentials.getCookie(cookieName);
    String oldValue = sc.getValue();
    sc.setValue(value);
    return oldValue;
  }

  /** {@inheritDoc} */
  public void setCookie(Cookie c) {
    credentials.getCookies().add(c);
  }

  @SuppressWarnings("unchecked")
  /** {@inheritDoc} */
  public Set getCookieNames() {
    Set<String> names = new HashSet<String>();
    for (Cookie c: credentials.getCookies()) {
      names.add(c.getName());
    }
    return names;
  }

  /** {@inheritDoc} */
  public String getLoginUrl() {
    return credentials.getDomain().getLoginUrl();
  }
  
  public void setVerified() {
    credentials.resetVerification();
  }

  public boolean isVerified() {
    return credentials.isVerified();
  }

  public AuthnMechanism getMechanism() {
    return credentials.getDomain().getMechanism();
  }
}
