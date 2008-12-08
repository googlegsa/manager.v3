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

import com.google.enterprise.session.metadata.AuthnDomainMetadata;

public class AuthnDomainCredentials extends SessionObject {

  private static enum AuthnDecision {
    TBD,            // haven't gone through verification yet
    VERIFIED,       // recognized by IdP
    REPUDIATED,     // unrecognized by IdP
  }

  private static final String NAME_KEY = "name";
  private static final String USERNAME_KEY = "username";
  private static final String PASSWORD_KEY = "password";
  private static final String VERIFIED_KEY = "verified";
  private static final String COOKIES_KEY = "cookies";
  private static final String TICKET_KEY = "ticket";

  AuthnDomainCredentials(SessionRoot root, AuthnDomainMetadata metadata) {
    super(root);
    setString(NAME_KEY, metadata.getName());
    setEnum(VERIFIED_KEY, AuthnDecision.TBD);
    setObject(COOKIES_KEY, new SessionList<SessionCookie>(root));
  }

  public String getName() {
    return getString(NAME_KEY);
  }

  public AuthnDomainMetadata getMetadata() {
    String name = getName();
    AuthnDomainMetadata metadata = AuthnDomainMetadata.getMetadata(name);
    if (metadata == null) {
      throw new IllegalStateException("unknown metadata name: " + name);
    }
    return metadata;
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

  public boolean isVerified() {
    return getEnum(VERIFIED_KEY, AuthnDecision.class) == AuthnDecision.VERIFIED;
  }

  public void setVerified(boolean isVerified) {
    setEnum(VERIFIED_KEY, isVerified ? AuthnDecision.VERIFIED : AuthnDecision.REPUDIATED);
  }

  public SessionList<SessionCookie> getCookies() {
    return getObject(COOKIES_KEY);
  }

  public SessionCookie getCookie(String name) {
    SessionCookie cookie = root.getCookie(name, true);
    SessionList<SessionCookie> cookies = getCookies();
    synchronized (cookies) {
      for (SessionCookie c: cookies) {
        if (c == cookie) {
          return cookie;
        }
      }
      cookies.add(cookie);
    }
    return cookie;
  }

  public SessionTicket getTicket() {
    return getObject(TICKET_KEY);
  }

  public void setTicket(SessionTicket ticket) {
    setObject(TICKET_KEY, ticket);
  }
}
