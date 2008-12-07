// Copyright (C) 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.session.object;

import com.google.enterprise.security.manager.SessionInterface;

public class CredentialsGroup extends SessionObject {

  private static final String USERNAME_KEY = "username";
  private static final String PASSWORD_KEY = "password";
  private static final String DOMAIN_MAP_KEY = "domainMap";

  public CredentialsGroup(SessionInterface session) {
    super(session);
    setObject(DOMAIN_MAP_KEY, new SessionMap<AuthnDomain, DomainElement>(session));
  }

  private SessionMap<AuthnDomain, DomainElement> getDomainMap() {
    return getObject(DOMAIN_MAP_KEY);
  }

  public String getUsername() {
    return getString(USERNAME_KEY);
  }

  public void setUsername(String username) {
    setString(USERNAME_KEY, username);
  }

  public String getPassword() {
    return getString(PASSWORD_KEY);
  }

  public void setPassword(String password) {
    setString(PASSWORD_KEY, password);
  }

  public boolean hasDomain(AuthnDomain domain) {
    return (getDomainMap().get(domain) != null);
  }

  private DomainElement getDomainElement(AuthnDomain domain) {
    return getDomainMap().getRequired(domain);
  }

  public boolean isVerified(AuthnDomain domain) {
    return getDomainElement(domain).isVerified();
  }

  public void setVerified(boolean isVerified, AuthnDomain domain) {
    getDomainElement(domain).setVerified(isVerified);
  }

  public SessionCookie getCookie(AuthnDomain domain) {
    return getDomainElement(domain).getCookie();
  }

  public void setCookie(SessionCookie cookie, AuthnDomain domain) {
    getDomainElement(domain).setCookie(cookie);
  }

  public SessionTicket getTicket(AuthnDomain domain) {
    return getDomainElement(domain).getTicket();
  }

  public void setTicket(SessionTicket ticket, AuthnDomain domain) {
    getDomainElement(domain).setTicket(ticket);
  }

  static class DomainElement extends SessionObject {

    private static final String VERIFIED_KEY = "verified";
    private static final String COOKIE_KEY = "cookie";
    private static final String TICKET_KEY = "ticket";

    DomainElement(SessionInterface session) {
      super(session);
    }

    boolean isVerified() {
      return getBoolean(VERIFIED_KEY);
    }

    void setVerified(boolean isVerified) {
      setBoolean(VERIFIED_KEY, isVerified);
    }

    SessionCookie getCookie() {
      return getObject(COOKIE_KEY);
    }

    void setCookie(SessionCookie cookie) {
      setObject(COOKIE_KEY, cookie);
    }

    SessionTicket getTicket() {
      return getObject(TICKET_KEY);
    }

    void setTicket(SessionTicket ticket) {
      setObject(TICKET_KEY, ticket);
    }
  }
}
